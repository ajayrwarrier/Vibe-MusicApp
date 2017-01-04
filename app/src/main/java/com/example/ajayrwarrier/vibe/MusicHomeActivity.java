package com.example.ajayrwarrier.vibe;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.name;
import static android.R.attr.thumbnail;
import static android.R.drawable.ic_media_play;
public class MusicHomeActivity extends AppCompatActivity  {
    private ArrayList<Song> songList = new ArrayList<>();
    public static MusicService musicSrv;
    private Intent playIntent;
    private boolean isPlaying  = false;
    private boolean musicBound = false;
    @BindView(R.id.song_list)
    ListView songView;
    @BindView(R.id.nowSong)
    TextView nowSong;
    @BindView(R.id.nowArtist)
    TextView nowArtist;
    @BindView(R.id.previousButton)
    ImageButton previousButton;
    @BindView(R.id.nextButton)
    ImageButton nextButton;
    @BindView(R.id.playButton)
    ImageButton playPauseButton;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_home);
        ButterKnife.bind(this);
        getSongList();
                Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
                musicSrv.playSong();
                isPlaying=true;
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                TextView Songname= (TextView) view.findViewById(R.id.nameView);
                nowSong.setText(Songname.getText());
                TextView Songartist= (TextView) view.findViewById(R.id.artistView);
                nowArtist.setText(Songartist.getText());

            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
                ToolUpdate(musicSrv.getSong());
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               playNext();
               ToolUpdate(musicSrv.getSong());
            }
        });
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isPlaying) {
                    musicSrv.pausePlayer();
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                }else{
                    musicSrv.seek(musicSrv.getPosn());
                    musicSrv.go();
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                }
                isPlaying = !isPlaying;
            }
        });
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),NowPlayingActivity.class);
                intent.putExtra("check",1);
                startActivity(intent);

            }
        });
    }
    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisPath = musicCursor.getString(pathColumn);
                        songList.add(new Song(thisId, thisTitle, thisArtist,thisPath));
            }
            while (musicCursor.moveToNext());
        }
    }
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();
        if(musicSrv!=null){
            if(musicSrv.isPng()){
                isPlaying=true;
                ToolUpdate(musicSrv.getSong());}
            else{
                isPlaying=false;
                playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                ToolUpdate(musicSrv.getSong());
            }
        }
            }
       private void playNext(){
        musicSrv.playNext();
    }
    private void playPrev(){
        musicSrv.playPrev();
    }
    public void ToolUpdate(Song song){
        nowSong.setText(song.getTitle());
        nowArtist.setText(song.getArtist());
    }

}
