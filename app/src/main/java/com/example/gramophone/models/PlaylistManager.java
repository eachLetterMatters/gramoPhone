package com.example.gramophone.models;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PlaylistManager {

    public static ArrayList<MusicFile> allSongFiles;

    public static ArrayList<MusicFile> allAlbumFiles;

    public static void getAllFiles(Context context) {

        allSongFiles = new ArrayList<>();
        allAlbumFiles = new ArrayList<>();
        ArrayList<String> albumDuplicates = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DISC_NUMBER,
        };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                int discNumber = cursor.getInt(5);
                MusicFile musicFiles = new MusicFile(path, title, artist, album, duration, discNumber);
                allSongFiles.add(musicFiles);

                if (!albumDuplicates.contains(album)) {
                    allAlbumFiles.add(musicFiles);
                    albumDuplicates.add(album);
                }
            }
            cursor.close();
        }
        //sort albums alphabetically
        Comparator<MusicFile> nameComparator = Comparator.comparing(MusicFile::getAlbum);
        Collections.sort(allAlbumFiles, nameComparator);
    }


}
