package com.example.gramophone.activities.main;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gramophone.R;
import com.example.gramophone.activities.player.PlayerActivity;
import com.example.gramophone.models.MusicFile;

import java.io.IOException;
import java.util.ArrayList;

//adapter used for song tab display
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {


    private Context mContext;
    public static ArrayList<MusicFile> mFiles;

    public SongsAdapter(Context mContext, ArrayList<MusicFile> mFiles)
    {
        this.mFiles = mFiles;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.song_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) { //tu to supress dodałem chuj po co
        holder.file_name.setText(mFiles.get(position).getTitle());
//        byte[] image;
//        try {
//            image = getAlbumArt(mFiles.get(position).getPath());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        if (image != null)
//        {
//            Glide.with(mContext).asBitmap()
//                    .load(image)
//                    .into(holder.album_art);
//        }
//        else
//        {
//            Glide.with(mContext)
//                    .load(R.drawable.hehe)
//                    .into(holder.album_art);
//        }

        // when the tab is clicked, the Player starts
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView file_name;
//        ImageView album_art;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
//            album_art = itemView.findViewById(R.id.music_img);
        }
    }

    //change list of songs based on user input in search option
    public void updateList(ArrayList<MusicFile> musicFilesArrayList){
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }


}
