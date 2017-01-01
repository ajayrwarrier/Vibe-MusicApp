package com.example.ajayrwarrier.vibe;
import android.net.Uri;
/**
 * Created by Ajay R Warrier on 29-12-2016.
 */
public class Song {
    private long id;
    private String title;
    private String artist;
    private String path;
    public Song(long songID, String songTitle, String songArtist,String path) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        this.path =path;
    }
    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getPath() {
        return path;
    }
    public Uri getUri(){
        return Uri.parse("file:///"+path);
    }
}
