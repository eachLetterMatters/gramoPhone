package com.example.gramophone.activities.player;

import static com.example.gramophone.activities.albumdetails.AlbumDetailsAdapter.albumFiles;
import static com.example.gramophone.MainActivity.musicFiles;
import static com.example.gramophone.MainActivity.repeatBoolean;
import static com.example.gramophone.MainActivity.shuffleBoolean;
import static com.example.gramophone.activities.main.SongsAdapter.mFiles;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.viewpager2.widget.ViewPager2;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gramophone.MusicService;
import com.example.gramophone.R;
import com.example.gramophone.helpers.BlurBuilder;
import com.example.gramophone.models.MusicFile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements  ActionPlaying, ServiceConnection {

    TextView song_name, artist_name, duration_played, duration_total;
//    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    ImageView nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;
    public static ArrayList<MusicFile> listSongs = new ArrayList<>();
    static Uri uri;
    // moved to the service
//    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    MusicService musicService;

    //
    ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//usuniecie top bara
        getSupportActionBar().hide();//
        // setting top notch color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        setContentView(R.layout.activity_player);
        initViews();
        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser)
                {
                    musicService.seekTo(progress*1000);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null)
                {
                    int mCurrentPosition = musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                //"handler is used to postDelay for the seekBar to change the progress
                // and if without using this it is working for you then you can just go ahead"
                handler.postDelayed(this, 100);
            }
        });
        //shuffle and repeat handling
        shuffleBtn.setOnClickListener(v -> {
            int resId = shuffleBoolean ? R.drawable.ic_shuffle_off : R.drawable.ic_shuffle_on;
            shuffleBoolean = !shuffleBoolean;
            shuffleBtn.setImageResource(resId);
        });
        repeatBtn.setOnClickListener(v -> {
            int resId = repeatBoolean ? R.drawable.ic_repeat_off : R.drawable.ic_repeat_on;
            repeatBoolean = !repeatBoolean;
            repeatBtn.setImageResource(resId);
        });
        prevBtn.setOnClickListener(v -> prevBtnClicked());
        playPauseBtn.setOnClickListener(v -> playPauseBtnClicked());
        nextBtn.setOnClickListener(v -> nextBtnClicked());

        // handle viewPager
        setUpViewPager();
    }

    private void setUpViewPager() {
        // make list of path
        ViewPagerAdapter vpAdapter = new ViewPagerAdapter(listSongs, getApplicationContext());
        viewPager2.setAdapter(vpAdapter);
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        viewPager2.setOffscreenPageLimit(2);
        viewPager2.setCurrentItem(position, false);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int pos) {
                super.onPageSelected(position);
                position = pos;
                playNewSong();
//                Toast.makeText(getApplicationContext(),"Selected_Page" +  String.valueOf(position), Toast.LENGTH_LONG).show();
//                Log.e("Selected_Page", String.valueOf(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    public void playPauseBtnClicked() {
        if(musicService.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
            musicService.start();
        }
    }

    public void prevBtnClicked(){
        if(shuffleBoolean && !repeatBoolean){
            position = getRandom(listSongs.size() - 1);
        } else if (!shuffleBoolean && !repeatBoolean) {
            position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1) );
        }
        playNewSong();
    }

    public void nextBtnClicked(){
        if(shuffleBoolean && !repeatBoolean){
            position = getRandom(listSongs.size() - 1);

        } else if (!shuffleBoolean && !repeatBoolean) {
            position = ((position + 1) % listSongs.size());
        }
        playNewSong();
    }

    private void playNewSong(){
        boolean msWasPlaying = musicService.isPlaying();
        musicService.stop();
        musicService.release();
        uri = Uri.parse(listSongs.get(position).getPath());
        musicService.createMediaPlayer(position);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        seekBar.setMax(musicService.getDuration() / 1000);
        musicService.onCompleted();
        viewPager2.setCurrentItem(position);
        if(msWasPlaying){
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
        } else {
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    private String formattedTime(int mCurrentPosition)
    {
        String totalout = "";
        String totalnew = "";
        String seconds = String.valueOf(mCurrentPosition%60);
        String minutes = String.valueOf(mCurrentPosition/60);

        totalout = minutes + ":" + seconds;
        totalnew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1)
        {
            return totalnew;
        }
        else {
            return totalout;
        }
    }

    private void getIntentMethod() {

        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if(sender != null && sender.equals("albumDetails"))
        {
            listSongs = albumFiles;
        }
        else {
            listSongs = mFiles;     // jak było musicfiles to search option zle dzialal
        }
        if(listSongs != null)
        {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);
    }

    private void initViews() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
//        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.id_prev);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
        viewPager2 = findViewById(R.id.playerViewpager);
    }

    //method used to update metadata of a new song
    private void metaData (Uri uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));

        byte[] art = retriever.getEmbeddedPicture();    //background
        Bitmap bitmap; //calculate color

        if (art!= null)
        {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length); //calc color
//            ImageAnimation(this, cover_art, bitmap); //animation + background
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
                        Bitmap resultBmp = BlurBuilder.blur(getApplicationContext(), BitmapFactory.decodeByteArray(art,0, art.length));
                        ImageView img= (ImageView) findViewById(R.id.mContainerBackground);
                        img.setImageDrawable(new BitmapDrawable(getResources(),resultBmp));
                    }
                    else {
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000,0xff000000});
                        ImageView img= (ImageView) findViewById(R.id.mContainerBackground);
                        img.setImageDrawable(gradientDrawableBg);
                    }
                }
            });
        }
        else {
//            Glide.with(getApplicationContext()).asBitmap().load(R.drawable.hehe).into(cover_art);
            ImageView img= (ImageView) findViewById(R.id.mContainerBackground);
            img.setImageResource(R.drawable.main_bg);
        }
    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap)
    {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }
                    @Override
                    public void onAnimationEnd(Animation animation) { }
                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        MusicService.MyBinder myBinder = (MusicService.MyBinder) iBinder;
        musicService = myBinder.getService();
        ///XD????
        musicService.setCallBack(this);

        //set played song data
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }
}