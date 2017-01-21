package com.example.ajayrwarrier.vibe;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import static com.example.ajayrwarrier.vibe.MusicHomeActivity.musicSrv;
import static com.example.ajayrwarrier.vibe.R.id.playButton;
/**
 * Created by Ajay R Warrier on 17-01-2017.
 */
public class SongProvider extends AppWidgetProvider {
    public static String PLAY_BUTTON = "android.appwidget.action.WIDGET_BUTTON";
    public static String NEXT_BUTTON = "android.appwidget.action.WIDGET_BUTTON_NEXT";
    public static String PREVIOUS_BUTTON = "android.appwidget.action.WIDGET_BUTTON_PREVIOUS";
    private static boolean isPlaying;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        if(musicSrv!=null)
        isPlaying = musicSrv.isPng();
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            //Intent to Main Activity
            Intent intent = new Intent(context, MusicHomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            UpdateView(context, views);
            if (isPlaying) {
                views.setImageViewResource(playButton, android.R.drawable.ic_media_pause);
            } else {
                views.setImageViewResource(playButton, android.R.drawable.ic_media_play);
            }
            //Intent for Play button
            Intent active = new Intent(context, SongProvider.class);
            active.setAction(PLAY_BUTTON);
            Log.v(context.getString(R.string.update), String.valueOf(isPlaying));
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
            if (actionPendingIntent != null) {
                views.setOnClickPendingIntent(playButton, actionPendingIntent);
            }
            //Intent for Next and Previous
            Intent next = new Intent(context, SongProvider.class);
            next.setAction(NEXT_BUTTON);
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0, next, 0);
            views.setOnClickPendingIntent(R.id.nextButton, nextPendingIntent);
            Intent prev = new Intent(context, SongProvider.class);
            prev.setAction(PREVIOUS_BUTTON);
            PendingIntent prePendingIntent = PendingIntent.getBroadcast(context, 0, prev, 0);
            views.setOnClickPendingIntent(R.id.previousButton, prePendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
        ComponentName thisWidget = new ComponentName(context, SongProvider.class);
        if (intent.getAction().equals(PLAY_BUTTON)) {
            Log.v("onRecieve", String.valueOf(isPlaying));
            if (musicSrv != null) {
                if (isPlaying) {
                    musicSrv.pausePlayer();
                    isPlaying = false;
                    remoteViews.setImageViewResource(playButton, android.R.drawable.ic_media_play);
                } else {
                    musicSrv.seek(musicSrv.getPosn());
                    musicSrv.go();
                    isPlaying = true;
                    UpdateView(context, remoteViews);
                    remoteViews.setImageViewResource(playButton, android.R.drawable.ic_media_pause);
                }
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            }
        } else if (intent.getAction().equals(NEXT_BUTTON)) {
            musicSrv.playNext();
            remoteViews.setImageViewResource(playButton, android.R.drawable.ic_media_pause);
            UpdateView(context, remoteViews);
        } else if (intent.getAction().equals(PREVIOUS_BUTTON)) {
            musicSrv.playPrev();
            remoteViews.setImageViewResource(playButton, android.R.drawable.ic_media_pause);
            UpdateView(context, remoteViews);
        } else {
            super.onReceive(context, intent);
        }
    }
    private void UpdateView(Context context, RemoteViews remoteViews) {
        if (musicSrv != null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, SongProvider.class);
            remoteViews.setTextViewText(R.id.nowSong, musicSrv.getSong().getTitle());
            remoteViews.setTextViewText(R.id.nowArtist, musicSrv.getSong().getArtist());
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }
    }
}