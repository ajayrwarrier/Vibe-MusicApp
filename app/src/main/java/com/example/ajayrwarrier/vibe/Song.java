package com.example.ajayrwarrier.vibe;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by Ajay R Warrier on 29-12-2016.
 */
public class Song implements Parcelable {
    private long id;
    private String title;
    private String artist;
    private String path;
    public Song(long songID, String songTitle, String songArtist, String path) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        this.path = path;
    }
    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        path = in.readString();
    }
    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }
        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    public long getID() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getArtist() {
        return artist;
    }
    public String getPath() {
        return path;
    }
    public Uri getUri() {
        return Uri.parse("file:///" + path);
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(artist);
        parcel.writeString(path);
    }
}
