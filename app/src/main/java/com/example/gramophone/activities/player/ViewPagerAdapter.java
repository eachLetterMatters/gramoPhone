package com.example.gramophone.activities.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gramophone.helpers.BlurBuilder;
import com.example.gramophone.models.MusicFile;

import java.util.ArrayList;
import com.example.gramophone.R;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {
    ArrayList<MusicFile> viewPagerItemArrayList;
    Context context;

    public ViewPagerAdapter(ArrayList<MusicFile> viewPagerItemArrayList, Context context) {
        this.viewPagerItemArrayList = viewPagerItemArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicFile viewPagerItem = viewPagerItemArrayList.get(position);
        String uri = viewPagerItem.getPath();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        byte[] art = null;
        try {
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();  //background
        } catch (Exception ex){

        }
//        holder.imageView.setImageResource(viewPagerItem.getPath());
//        byte[] art = retriever.getEmbeddedPicture();    //background
        Bitmap bitmap; //calculate color

        if (art!= null)
        {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length); //calc color


            Glide.with(context).load(bitmap).into(holder.imageView);
//            holder.imageView.setImageDrawable(new BitmapDrawable(resultBmp));
        }
        else {
//            Glide.with(context.asBitmap().load(R.drawable.hehe).into(holder.imageView);
//            ImageView img= (ImageView) findViewById(R.id.mContainerBackground);
//            img.setImageResource(R.drawable.main_bg);
            holder.imageView.setImageResource(R.drawable.hehe);
        }
    }



    @Override
    public int getItemCount() {
        return viewPagerItemArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
//        TextView tvHeading, tvDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.cover_art);
//            tvHeading = itemView.findViewById(R.id.tvheading);
//            tvDesc = itemView.findViewById(R.id.tvdesc);

        }
    }
}
