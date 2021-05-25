package com.example.SocyMusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SocyMusic.SocyMusic.SocyMusic;
import com.example.musicplayer.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    SocyMusic music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listViewSong);

        checkRuntimePermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected( @NonNull MenuItem item ) {

        if (item.getItemId() == R.id.credits) {
            // Replace this
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.download) {
            // Replace this
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkRuntimePermissions() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        start();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    /**
     * To start the application
     */
    private void start(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("SocyMusic");

        this.music = new SocyMusic(Environment.getExternalStorageDirectory());

        SonglistAdapter songlistAdapter = new SonglistAdapter(music, getApplicationContext(), getLayoutInflater());
        listView.setAdapter(songlistAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = (String) listView.getItemAtPosition(position);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        //Convert the object to an ArrayList to pass it to a new Activity
                        .putExtra("songs", music.toArrayList())
                        .putExtra("songname", songName)
                        .putExtra("pos", position));
            }
        });
    }
}