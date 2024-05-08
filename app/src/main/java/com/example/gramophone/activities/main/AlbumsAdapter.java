package com.example.gramophone.activities.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gramophone.R;
import com.example.gramophone.activities.albumdetails.AlbumDetails;
import com.example.gramophone.models.MusicFile;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.MyHolder> {

    private Context mContext;
    private ArrayList<MusicFile> albumFiles;
    View view;

    public AlbumsAdapter(Context mContext, ArrayList<MusicFile> albumFiles) {
        this.mContext = mContext;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) { //co robi to supress XD???
        holder.album_name.setText(albumFiles.get(position).getAlbum());
        byte[] image;
        try {
            image = getAlbumArt(albumFiles.get(position).getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (image != null)
        {
//            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
//            holder.album_image.setImageBitmap(Bitmap.createScaledBitmap(bmp, 100, 100, false));


            Glide.with(mContext).asBitmap()
                    .load(image)
//                    .override(20,20)
                    .into(holder.album_image);
        }
        else
        {
//            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.hehe);
//            holder.album_image.setImageBitmap(Bitmap.createScaledBitmap(bmp, 100, 100, false));
            Glide.with(mContext)
                    .load(R.drawable.hehe)
//                    .override(20,20)
                    .into(holder.album_image);
        }
        //dodaj album details potem i odkomentuj
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AlbumDetails.class);
                intent.putExtra("albumName", albumFiles.get(position).getAlbum());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView album_image;
        TextView album_name;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);
        }
    }
    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
