package com.example.ajayrwarrier.vibe;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
/**
 * Created by Ajay R Warrier on 29-12-2016.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    LocalBroadcastManager broadcaster;
    static final public String COPA_RESULT = "REQUEST_PROCESSED";
    private static final int NOTIFY_ID = 1;
    private final IBinder musicBind = new MusicBinder();
    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
        songPosn = 0;
        if (player == null) {
            player = new MediaPlayer();
        }
        player = new MediaPlayer();
        initMusicPlayer();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.reset();
        player.release();
        return false;
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (player.getCurrentPosition() > 0) {
            mediaPlayer.reset();
            playNext();
        }
    }
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        makeNotification();
        sendResult(getSong());
    }
    @Override
    public void onDestroy() {
        stopForeground(true);
    }
    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }
    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    public void playSong() {
        player.reset();
        Song playSong = songs.get(songPosn);
        makeNotification();
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }
    public void setSong(int songIndex) {
        songPosn = songIndex;
    }
    public Song getSong() {
        return songs.get(songPosn);
    }
    public int getPosn() {
        return player.getCurrentPosition();
    }
    public int getDur() {
        return player.getDuration();
    }
    public boolean isPng() {
        return player.isPlaying();
    }
    public void pausePlayer() {
        player.pause();
    }
    public void seek(int posn) {
        player.seekTo(posn);
    }
    public void go() {
        player.start();
    }
    public void playPrev() {
        songPosn--;
        if (songPosn <= 0) songPosn = songs.size() - 1;
        playSong();
    }
    public void playNext() {
        songPosn++;
        if (songPosn >= songs.size()) songPosn = 0;
        playSong();
    }
    public MediaPlayer getPlayer() {
        return player;
    }
    public void makeNotification() {
        Intent notIntent = new Intent(this, NowPlayingActivity.class);
        notIntent.putExtra("check", 1);
        notIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setTicker(getSong().getTitle())
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(getSong().getTitle());
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }
    public void sendResult(Song song) {
        Intent intent = new Intent(COPA_RESULT);
        if (song != null)
            intent.putExtra("song", song);
        broadcaster.sendBroadcast(intent);
    }
}
