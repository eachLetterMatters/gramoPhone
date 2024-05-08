package com.example.gramophone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import static com.example.gramophone.ApplicationClass.ACTION_NEXT;
import static com.example.gramophone.ApplicationClass.ACTION_PLAY;
import static com.example.gramophone.ApplicationClass.ACTION_PREVIOUS;
import static com.example.gramophone.ApplicationClass.CHANNEL_ID_2;
import static com.example.gramophone.activities.player.PlayerActivity.listSongs;

import com.example.gramophone.activities.player.ActionPlaying;
import com.example.gramophone.activities.player.PlayerActivity;
import com.example.gramophone.models.MusicFile;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener{

    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFile> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;

    // instacja interfejsu zeby móc wywoływać nxtBtnClicked XD???????
    ActionPlaying actionPlaying;

    // needed for showing notification
    MediaSessionCompat mediaSessionCompat;

    // variables resposible for bottom last played song player
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    public static final String SONG_NAME = "SONG_NAME";

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName= intent.getStringExtra("ActionName");
        if (myPosition != -1){
            playMedia(myPosition);
        }
        if (actionName != null){
            switch (actionName){
                case "playPause":
                    playPauseBtnClicked();
                    break;
                case "next":
                    nextBtnClicked();
                    break;
                case "previous":
                    previousBtnClicked();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = listSongs;
        position = startPosition;
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles != null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public void start(){
        mediaPlayer.start();
    }
    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    public void stop(){
        mediaPlayer.stop();
    }
    public void release(){
        mediaPlayer.release();
    }
    public void pause(){
        mediaPlayer.pause();
    }
    public int getDuration(){
        return mediaPlayer.getDuration();
    }
    public void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    public void createMediaPlayer(int positionInner){
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());

        // part of code resposible for a properly working bottom last song player
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    public void onCompleted(){
        // no i po co to jest tu zamiast w oncompletion XDDDDD
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying != null){
            actionPlaying.nextBtnClicked();
            if(mediaPlayer != null) {
                createMediaPlayer(position);
                start();
                onCompleted();
            }
        }
    }

    public void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    public void showNotification(int playPauseBtn) {
        // jest do tego tutorial
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent
                .getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE); //TODO: flags powinno byc 0
        // meant for the previous button
        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent
//                .getBroadcast(this, 0, prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                .getBroadcast(this, 0, prevIntent,PendingIntent.FLAG_MUTABLE);
        // meant for the previous button
        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent
                .getBroadcast(this, 0, pauseIntent,PendingIntent.FLAG_MUTABLE);
        // meant for the previous button
        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent
                .getBroadcast(this, 0, nextIntent,PendingIntent.FLAG_MUTABLE);
        // player than can be opened when clicking notification
        Intent playerIntent = new Intent(this, PlayerActivity.class);
        PendingIntent playerPendingIntent = PendingIntent.getActivity(
                this, 0, playerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if(picture != null){
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.hehe);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_skip_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
//                .setContentIntent(playerPendingIntent)
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        //TODO: w razie co to odkomentuj to
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notification);
        //TODO: a zakomentuj to
        // to wymaga foreground permission w manifescie
        // mozliwe ze kod jest zbugowany przez to ze nigdzie tego notification id=1 nie resetuje
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(FM_NOTIFICATION_ID, builder.build());
//        manager.cancel(1);
        startForeground(1, notification);
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        try {
            retriever.release();
        } catch (Exception ex){
            // XDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
            throw new RuntimeException(ex);
        }
        return art;
    }

    void nextBtnClicked(){
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
        }
    }
    void previousBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.prevBtnClicked();
        }
    }

    void playPauseBtnClicked(){
        if (actionPlaying != null) {
            actionPlaying.playPauseBtnClicked();
        }
    }
}
