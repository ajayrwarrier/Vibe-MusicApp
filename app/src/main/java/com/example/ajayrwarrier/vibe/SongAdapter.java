package com.example.ajayrwarrier.vibe;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.R.attr.data;
/**
 * Created by Ajay R Warrier on 29-12-2016.
 */
public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs=theSongs;
        songInf=LayoutInflater.from(c);
    }
    @Override
    public int getCount() {
        return songs.size();
    }
    @Override
    public Object getItem(int i) {
        return null;
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ConstraintLayout songLay = (ConstraintLayout) songInf.inflate
                (R.layout.song_item, parent, false);

        TextView songView = (TextView)songLay.findViewById(R.id.nameView);
        TextView artistView = (TextView)songLay.findViewById(R.id.artistView);
        Song currSong = songs.get(position);

        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        songLay.setTag(position);
        ImageView image=(ImageView)songLay.findViewById(R.id.thumbnailView);
        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
        retriever.setDataSource(currSong.getPath());
        byte[] data = retriever.getEmbeddedPicture();
        if (data != null) {
            image.setImageBitmap(BitmapFactory.decodeByteArray(data, 0,data.length));
        }else{
            image.setImageResource(android.R.drawable.ic_menu_slideshow);
            retriever.release();}

        return songLay;
    }

}
