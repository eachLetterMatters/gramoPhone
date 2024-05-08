package com.example.gramophone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.SearchView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.example.gramophone.activities.main.AlbumsFragment;
import com.example.gramophone.activities.main.SongsFragment;
import com.example.gramophone.models.MusicFile;
//import com.example.gramophone.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static ArrayList<MusicFile> musicFiles;
    public static ArrayList<MusicFile> albums = new ArrayList<>();

    public static boolean shuffleBoolean = false, repeatBoolean = false;

    // variables resposible for bottom last played song player
//    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
//    public static final String MUSIC_FILE = "STORED_MUSIC";
//    public static boolean SHOW_MINI_PLAYER = false;
//    public static String PATH_TO_FRAG = null;
//    public static String ARTIST_TO_FRAG = null;
//    public static String SONG_NAME_TO_FRAG = null;
//    public static final String ARTIST_NAME = "ARTIST_NAME";
//    public static final String SONG_NAME = "SONG_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        permission();
    }

    private void permission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 1);
        }
        else
        {
            musicFiles = getAllFiles(this);
            initViewPager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                musicFiles = getAllFiles(this);
                initViewPager();
            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 1);

            }
        }
    }

    private void initViewPager(){
        TabLayout tabLayout = findViewById(R.id.main_tablayout);
        ViewPager viewPager = findViewById(R.id.main_viewpager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new AlbumsFragment(), "Albums");
        viewPagerAdapter.addFragments(new SongsFragment(), "All Songs");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;
        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }
        void addFragments (Fragment fragment, String title ){
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public CharSequence getPageTitle (int position){ return titles.get(position); }
    }

    public static ArrayList<MusicFile> getAllFiles(Context context){

        ArrayList<MusicFile> temporarylist = new ArrayList<>();
        ArrayList<String> duplicate = new ArrayList<>(); //duplikaty albumow
        albums = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST
        };
        Cursor cursor = context.getContentResolver().query(uri,projection,null,null,null);

        if(cursor!=null){
            while(cursor.moveToNext())
            {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);

                MusicFile musicFiles = new MusicFile(path,title,artist,album,duration);
                //Log.e("Path: " + path, "Album: " + album);
                temporarylist.add(musicFiles);

                if(!duplicate.contains(album))  //duplikaty albumow
                {
                    albums.add(musicFiles);
                    duplicate.add(album);
                }
            }
            cursor.close();
        }
        //sort albums alphabetically
        Comparator<MusicFile> nameComparator = Comparator.comparing(MusicFile::getAlbum);
        Collections.sort(albums, nameComparator);
        return temporarylist;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<MusicFile> filtered = new ArrayList<>();
        for(MusicFile song : musicFiles){
            if (song.getTitle().toLowerCase().contains(userInput)){
                filtered.add(song);
            }
        }
//        SongsFragment.songsAdapter.updateList(filtered);
        return true;
    }

    // part of code responsible for a properly working bottom player
//    @Override
//    protected void onResume() {
//        super.onResume();
//        SharedPreferences preferences = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
//        String path = preferences.getString(MUSIC_FILE, null);
//        String artist = preferences.getString(ARTIST_NAME, null);
//        String song_name = preferences.getString(SONG_NAME, null);
//        if(path != null) {
//            SHOW_MINI_PLAYER = true;
//            PATH_TO_FRAG = path;
//            ARTIST_TO_FRAG = artist;
//            SONG_NAME_TO_FRAG = song_name;
//        } else {
//            SHOW_MINI_PLAYER = false;
//            PATH_TO_FRAG = null;
//            ARTIST_TO_FRAG = null;
//            SONG_NAME_TO_FRAG = null;
//        }
//    }
}