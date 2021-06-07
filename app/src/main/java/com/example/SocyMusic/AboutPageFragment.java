package com.example.SocyMusic;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.musicplayer.R;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

// Uses an imported Github library
// Refer to: https://github.com/medyo/android-about-page
// And: https://blog.fossasia.org/creating-an-awesome-about-us-page-for-the-open-event-organizer-android-app/
public class AboutPageFragment extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Just a template for now!
        Element adsElement = new Element();
        adsElement.setTitle("Advertise with us");

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.app_icon)
                .addItem(new Element().setTitle("Version 6.2"))
                .addItem(adsElement)
                .addGroup("Connect with us")
                .addEmail("elmehdi.sakout@gmail.com")
                .addWebsite("https://mehdisakout.com/")
                .addFacebook("the.medy")
                .addTwitter("medyo80")
                .addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
                .addPlayStore("com.ideashower.readitlater.pro")
                .addInstagram("medyo80")
                .addGitHub("medyo")
                //.addItem(getCopyRightsElement())
                .create();

        setContentView(aboutPage);

    }
}
