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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import com.example.ajayrwarrier.vibe.googleservices.Analytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
public class MusicHomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static MusicService musicSrv;
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
    @BindView(R.id.logout_button)
    ImageButton signoutButton;
    BroadcastReceiver receiver;
    Tracker mTracker;
    String ActivityName;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private ArrayList<Song> songList = new ArrayList<>();
    private Intent playIntent;
    private boolean isPlaying;
    private boolean musicBound = false;
    private boolean status = false;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_home);
        ButterKnife.bind(this);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // if user is null launch login activity
                    startActivity(new Intent(MusicHomeActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(MusicHomeActivity.this, (getString(R.string.hello) + user.getEmail() + ""), Toast.LENGTH_SHORT).show();
                    ;
                }
            }
        };
        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutButton();
            }
        });
        isStoragePermissionGranted();
        getLoaderManager().initLoader(0, null, this);
        Analytics application = (Analytics) getApplication();
        ActivityName = getString(R.string.home_name);
        mTracker = application.getDefaultTracker();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song song = intent.getExtras().getParcelable(getString(R.string.song));
                ToolUpdate(song);
            }
        };
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
                    intent.putExtra(getString(R.string.check), 1);
                    startActivity(intent);
                }
            }
        });
        nowArtist.setSelected(true);
        nowSong.setSelected(true);
        if (musicSrv != null)
            isPlaying = musicSrv.isPng();
    }
    @Override
    protected void onStart() {
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            this.startService(playIntent);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MusicService.COPA_RESULT)
        );
        super.onStart();
        auth.addAuthStateListener(authListener);
    }
    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
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
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        Log.i(getString(R.string.analytics), getString(R.string.setscreenname) + ActivityName);
        mTracker.setScreenName(getString(R.string.image) + ActivityName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        if (musicSrv != null) {
            if (musicBound) {
                if (musicSrv.isPng()) {
                    isPlaying = true;
                    ToolUpdate(musicSrv.getSong());
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    isPlaying = false;
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                    if (musicSrv.getSong() != null)
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
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("", getString(R.string.prem_granted));
                return true;
            } else {
                Log.v("", getString(R.string.perm_revoked));
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("", getString(R.string.prem_granted));
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("", getString(R.string.perm) + permissions[0] + getString(R.string.was) + grantResults[0]);
        }
    }
    public void signOutButton() {
        auth.signOut();
    }
}
