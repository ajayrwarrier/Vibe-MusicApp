package com.example.ajayrwarrier.vibe;
import com.google.firebase.database.IgnoreExtraProperties;
/**
 * Created by Ajay R Warrier on 21-01-2017.
 */
@IgnoreExtraProperties
public class UserData {
    public String email;
    public UserData() {
    }
    public UserData(String email) {
        this.email = email;
    }
}
