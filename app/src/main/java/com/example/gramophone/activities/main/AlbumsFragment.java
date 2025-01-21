package com.example.gramophone.activities.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gramophone.R;
import com.example.gramophone.models.PlaylistManager;


public class AlbumsFragment extends Fragment {

    RecyclerView recyclerView;
    AlbumsAdapter albumAdapter;
    //MusicAdapter albumAdapter;

    public AlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if(!(PlaylistManager.allAlbumFiles.size() < 1))
        {
            albumAdapter = new AlbumsAdapter(getContext(), PlaylistManager.allAlbumFiles);
            //albumAdapter = new MusicAdapter(getContext(), musicFiles);
            recyclerView.setAdapter(albumAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }

        return view;
    }
}