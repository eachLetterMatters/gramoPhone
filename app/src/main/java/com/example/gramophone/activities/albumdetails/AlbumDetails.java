package com.example.gramophone.activities.albumdetails;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.gramophone.R;
import com.example.gramophone.models.MusicFile;
import com.example.gramophone.models.PlaylistManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto;

    TextView albumTitle;
    String albumName;
    ArrayList<MusicFile> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//usuniecie top bara
        getSupportActionBar().hide();//
        // setting top notch color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        setContentView(R.layout.activity_album_details);
        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto= findViewById(R.id.albumPhoto);
        albumName = getIntent().getStringExtra("albumName");
        albumTitle = findViewById(R.id.album_title);
        albumTitle.setText(albumName);

        int j = 0;
        for (int i = 0; i < PlaylistManager.allSongFiles.size(); i++)
        {
            if(albumName.equals(PlaylistManager.allSongFiles.get(i).getAlbum()))
            {
                albumSongs.add(j, PlaylistManager.allSongFiles.get(i));
                j++;
            }
        }
        // sort album songs per disc_number and then alphabetically
        Comparator<MusicFile> comparator = Comparator.comparingInt(MusicFile::getDiscNumber).thenComparing(MusicFile::getTitle);
        Collections.sort(albumSongs, comparator);

        byte[] image;
        try {
            image = getAlbumArt(albumSongs.get(0).getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(image != null) {
            Glide.with(this).load(image).into(albumPhoto);
        }
        else
        {
            Glide.with(this).load(R.drawable.hehe).into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!(albumSongs.size() < 1))
        {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        }
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        byte[] art = null;
        try {
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();
        }catch(Exception ex){
        }
        return art;
    }





}