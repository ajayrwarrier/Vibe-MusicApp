package com.example.ajayrwarrier.vibe;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import wseemann.media.FFmpegMediaMetadataRetriever;
/**
 * Created by Ajay R Warrier on 29-12-2016.
 */
public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
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
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = songInf.inflate(R.layout.song_item, null);
            holder.songView = (TextView) view.findViewById(R.id.nameView);
            holder.artistView = (TextView) view.findViewById(R.id.artistView);
            holder.position = position;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            holder.position = position;
        }
        final Song currSong = songs.get(position);
        ImageView nextButton = (ImageView) view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currSong == MusicHomeActivity.musicSrv.getSong()) {
                    Intent intent = new Intent(view.getContext(), NowPlayingActivity.class);
                    intent.putExtra("check", 1);
                    view.getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(view.getContext(), NowPlayingActivity.class);
                    intent.putExtra("currSong", currSong);
                    intent.putExtra("check", 2);
                    intent.putExtra("position", position);
                    view.getContext().startActivity(intent);
                }
            }
        });
        holder.songView.setText(currSong.getTitle());
        holder.artistView.setText(currSong.getArtist());
        holder.image = (ImageView) view.findViewById(R.id.thumbnailView);
        holder.image.setImageResource(android.R.drawable.ic_menu_slideshow);
        new AsyncTask<ViewHolder, Void, byte[]>() {
            private ViewHolder v;
            FFmpegMediaMetadataRetriever retriever;
            @Override
            protected byte[] doInBackground(ViewHolder... params) {
                v = params[0];
                retriever = new FFmpegMediaMetadataRetriever();
                retriever.setDataSource(currSong.getUri().toString());
                byte[] data = retriever.getEmbeddedPicture();
                return data;
            }
            @Override
            protected void onPostExecute(byte[] data) {
                super.onPostExecute(data);
                if (v.position == position) {
                    if (data != null) {
                        v.image.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                    } else {
                        v.image.setImageResource(android.R.drawable.ic_menu_slideshow);
                        retriever.release();
                    }
                }
            }
        }.execute(holder);
        return view;
    }
    static class ViewHolder {
        TextView songView;
        TextView artistView;
        ImageView image;
        int position;
    }
}
