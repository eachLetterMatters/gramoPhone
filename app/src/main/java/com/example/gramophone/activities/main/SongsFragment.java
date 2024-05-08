package com.example.gramophone.activities.main;

import static com.example.gramophone.MainActivity.musicFiles;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gramophone.R;

//  A Fragment represents a reusable portion of your app's UI. A fragment definesand
//  manages its own layout, has its own lifecycle, and can handle its own input
//  events. Fragments can't live on their own. They must be hosted by an
//  activity or another fragment. The fragment’s view hierarchy becomes part of,
//  or attaches to, the host’s view hierarchy.
public class SongsFragment extends Fragment {

    RecyclerView recyclerView;
    public static SongsAdapter songsAdapter;

    public static SongsFragment newInstance() {
        SongsFragment fragment = new SongsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if(!(musicFiles.size() < 1))
        {
            songsAdapter = new SongsAdapter(getContext(), musicFiles);
            recyclerView.setAdapter(songsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false));
        }
        return view;
    }
}