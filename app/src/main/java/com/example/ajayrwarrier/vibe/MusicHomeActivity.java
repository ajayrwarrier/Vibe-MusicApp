package com.example.ajayrwarrier.vibe;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ajayrwarrier.vibe.googleservices.Analytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
public class MusicHomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private ArrayList<Song> songList = new ArrayList<>();
    public static MusicService musicSrv;
    private Intent playIntent;
    private boolean isPlaying = false;
    private boolean musicBound = false;
    private boolean status = false;
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
    BroadcastReceiver receiver;
    Tracker mTracker;
    String ActivityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_home);
        ButterKnife.bind(this);
        getLoaderManager().initLoader(0, null, this);
        Analytics application = (Analytics) getApplication();
        ActivityName = getString(R.string.home_name);
        mTracker = application.getDefaultTracker();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song song = intent.getExtras().getParcelable("song");
                ToolUpdate(song);
            }
        };
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
                status = true;
                Log.e("THIS IS SPARTAAAAAA", view.getTag().toString());
                musicSrv.setSong(position);
                musicSrv.playSong();
                musicSrv.makeNotification();
                ToolUpdate(musicSrv.getSong());
                isPlaying = true;
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                TextView Songname = (TextView) view.findViewById(R.id.nameView);
                nowSong.setText(Songname.getText());
                TextView Songartist = (TextView) view.findViewById(R.id.artistView);
                nowArtist.setText(Songartist.getText());
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
                musicSrv.makeNotification();
                ToolUpdate(musicSrv.getSong());
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
                musicSrv.makeNotification();
                ToolUpdate(musicSrv.getSong());
            }
        });
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isPlaying) {
                    musicSrv.pausePlayer();
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
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
                if (status) {
                    Intent intent = new Intent(view.getContext(), NowPlayingActivity.class);
                    intent.putExtra("check", 1);
                    startActivity(intent);
                }
            }
        });
        nowArtist.setSelected(true);
        nowSong.setSelected(true);
    }
    public void getSongList() {
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
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            this.startService(playIntent);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MusicService.COPA_RESULT)
        );
        super.onStart();
    }
    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        stopService(playIntent);
        unbindService(musicConnection);
        musicSrv = null;
        super.onDestroy();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i("ANALYTICS: ", "Setting screen name: " + ActivityName);
        mTracker.setScreenName("Image~" + ActivityName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        if (musicSrv != null) {
            if (musicBound) {
                if (musicSrv.isPng()) {
                    isPlaying = true;
                    ToolUpdate(musicSrv.getSong());
                } else {
                    isPlaying = false;
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                    ToolUpdate(musicSrv.getSong());
                }
            }
        }
    }
    private void playNext() {
        musicSrv.playNext();
    }
    private void playPrev() {
        musicSrv.playPrev();
    }
    public void ToolUpdate(Song song) {
        nowSong.setText(song.getTitle());
        nowArtist.setText(song.getArtist());
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return new CursorLoader(this, musicUri, null, null, null, null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor musicCursor) {
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
                songList.add(new Song(thisId, thisTitle, thisArtist, thisPath));
            }
            while (musicCursor.moveToNext());
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.reset();
    }
}
