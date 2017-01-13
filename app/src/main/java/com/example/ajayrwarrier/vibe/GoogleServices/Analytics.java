package com.example.ajayrwarrier.vibe.googleservices;
import android.app.Application;

import com.example.ajayrwarrier.vibe.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
/**
 * Created by Ajay R Warrier on 12-01-2017.
 */
public class Analytics extends Application {
    private Tracker mTracker;

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
