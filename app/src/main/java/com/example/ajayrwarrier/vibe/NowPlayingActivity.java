package com.example.ajayrwarrier.vibe;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ajayrwarrier.vibe.googleservices.Analytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import butterknife.BindView;
import butterknife.ButterKnife;
import wseemann.media.FFmpegMediaMetadataRetriever;

import static com.example.ajayrwarrier.vibe.MusicHomeActivity.musicSrv;
public class NowPlayingActivity extends AppCompatActivity {
    @BindView(R.id.nameView)
    TextView nameSong;
    @BindView(R.id.artistView)
    TextView nameArtist;
    @BindView(R.id.albumArt)
    ImageView albumArt;
    @BindView(R.id.seekBar)
    SeekBar seekBar;
    @BindView(R.id.startTime)
    TextView startTime;
    @BindView(R.id.totalTime)
    TextView totalTime;
    @BindView(R.id.playButton)
    ImageButton playButton;
    @BindView(R.id.previousButton)
    ImageButton previousButton;
    @BindView(R.id.nextButton)
    ImageButton nextButton;
    MusicService musService;
    Song currSong;
    int check;
    Song currentSong;
    int position;
    Tracker mTracker;
    String ActivityName;
    InterstitialAd mInterstitialAd;
    Handler seekHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        ButterKnife.bind(this);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E5984852F624B87B06F860B7AC30F713")
                .build();
        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });
        ActivityName = getString(R.string.nowplaying_name);
        mTracker = ((Analytics) getApplication()).getDefaultTracker();
        if (getIntent().getExtras() != null) {
            check = getIntent().getExtras().getInt("check");
            Toast.makeText(this, String.valueOf(check), Toast.LENGTH_SHORT).show();
            currentSong = getIntent().getExtras().getParcelable("currSong");
            position = getIntent().getExtras().getInt("position");
            if (check == 2) {
                UpdateUi(currentSong, check);
            }
        }
        startTime.setText(R.string.start_time);
        totalTime.setText("");
        musService = musicSrv;
        if (check == 1) {
            currSong = musService.getSong();
            if (musService.isPng()) {
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                UpdateUi(currSong, check);
            } else {
                playButton.setImageResource(android.R.drawable.ic_media_play);
                musicSrv.seek(musicSrv.getPosn());
                UpdateUi(currSong, 1);
            }
        }
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musService.playPrev();
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                UpdateUi(musicSrv.getSong(), 3);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musService.playNext();
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                UpdateUi(musicSrv.getSong(), 3);
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (check == 2) {
                    musicSrv.setSong(position);
                    musService.playSong();
                    musicSrv.makeNotification();
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    UpdateUi(musicSrv.getSong(), 3);
                    check = 1;
                } else if (musService.isPng()) {
                    musicSrv.pausePlayer();
                    playButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    musicSrv.seek(musicSrv.getPosn());
                    musicSrv.go();
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (musService != null && b) {
                    musService.seek(i);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i("ANALYTICS: ", "Setting screen name: " + ActivityName);
        mTracker.setScreenName("Image~" + ActivityName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };
    public void seekUpdation() {
        if (musService != null) {
            startTime.setText(getTimeString(musService.getPosn()));
            seekBar.setProgress(musService.getPosn());
            seekHandler.postDelayed(run, 1);
        }
    }
    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
        buf
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));
        return buf.toString();
    }
    private void UpdateUi(Song currSong, int check) {
        if (currSong != null) {
            nameSong.setText(currSong.getTitle());
            nameArtist.setText(currSong.getArtist());
            FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
            retriever.setDataSource(currSong.getPath());
            byte[] data = retriever.getEmbeddedPicture();
            if (data != null) {
                albumArt.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
            } else {
                albumArt.setImageResource(R.drawable.no_thumbnail);
                retriever.release();
            }
            if (check == 1) {
                seekUpdation();
                totalTime.setText(getTimeString(musService.getDur()));
                seekBar.setMax(musService.getDur());
            }
            if (check == 3) {
                MediaPlayer mediaPlayer = musService.getPlayer();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        musicSrv.go();
                        seekUpdation();
                        totalTime.setText(getTimeString(musService.getDur()));
                        seekBar.setMax(musService.getDur());
                    }
                });
            }
        }
    }
    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
}
