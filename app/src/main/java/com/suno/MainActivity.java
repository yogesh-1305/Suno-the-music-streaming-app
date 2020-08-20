package com.suno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jean.jcplayer.JcPlayerManagerListener;
import com.example.jean.jcplayer.general.JcStatus;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private boolean checkPermission = false;

    TextView currentPlayingSongName, currentPlayingArtistName;
    ImageView currentPlayingSongThumbnail, largePlayerImage;
    int pos;
    ProgressDialog progressDialog;
    ListView listView;
    List<String> songsNameList;
    List<String> songsUrlList;
    List<String> songsArtistList;
    List<String> songsDurationList;
    ListAdapter adapter;
    JcPlayerView jcPlayerView;
    List<JcAudio> jcAudios;
    List<String> thumbnail;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentPlayingSongName = findViewById(R.id.CurrentPlayingSongName);
        currentPlayingArtistName = findViewById(R.id.CurrentPlayingArtistName);
        currentPlayingSongThumbnail = findViewById(R.id.cirrentPlayingSongThumbnail);
        largePlayerImage = findViewById(R.id.LargePlayerImage);

        listView = findViewById(R.id.songsList);
        jcPlayerView = findViewById(R.id.jcplayer);
        jcPlayerView.setJcPlayerManagerListener(new JcPlayerManagerListener() {
            @Override
            public void onPreparedAudio(@NonNull JcStatus jcStatus) {
                Log.i("|||||PrepareAudio|||||", jcStatus.getJcAudio().getTitle());
                currentPlayingSongName.setText(jcStatus.getJcAudio().getTitle());
                currentPlayingArtistName.setText(songsArtistList.get(jcStatus.getJcAudio().getPosition()));
                Picasso.get().load(thumbnail.get(jcStatus.getJcAudio().getPosition())).into(largePlayerImage);
                Picasso.get().load(thumbnail.get(jcStatus.getJcAudio().getPosition())).into(currentPlayingSongThumbnail);
            }

            @Override
            public void onCompletedAudio() {
                Log.i("||||CompletedAudio|||||", "]]]]]]]]]]]]]]");
            }

            @Override
            public void onPaused(@NonNull JcStatus jcStatus) {
                Log.i("|||||onPaused|||||", jcStatus.toString());
            }

            @Override
            public void onContinueAudio(@NonNull JcStatus jcStatus) {
                Log.i("|||ContinueAudio|||||", jcStatus.toString());
            }

            @Override
            public void onPlaying(@NonNull JcStatus jcStatus) {
                Log.i("|||||onPlaying|||||", jcStatus.toString());
            }

            @Override
            public void onTimeChanged(@NonNull JcStatus jcStatus) {
                Log.i("|||||onTimeChanged|||||", jcStatus.toString());
            }

            @Override
            public void onStopped(@NonNull JcStatus jcStatus) {
                Log.i("|||||onStopped|||||", jcStatus.toString());
            }

            @Override
            public void onJcpError(@NonNull Throwable throwable) {
                Log.i("|||||onJcpError|||||", throwable.toString());
            }
        });

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setMessage("Please Wait...");

        songsNameList = new ArrayList<>();
        songsUrlList = new ArrayList<>();
        songsArtistList = new ArrayList<>();
        songsDurationList = new ArrayList<>();
        jcAudios = new ArrayList<>();
        thumbnail = new ArrayList<>();

        retrieveSongs();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                pos = i;
                jcPlayerView.playAudio(jcAudios.get(i));
                jcPlayerView.setVisibility(View.VISIBLE);
                currentPlayingSongName.setText(songsNameList.get(i));
                currentPlayingArtistName.setText(songsArtistList.get(i));
                Picasso.get().load(thumbnail.get(i)).into(currentPlayingSongThumbnail);
                Picasso.get().load(thumbnail.get(i)).into(largePlayerImage);
                jcPlayerView.createNotification();
                adapter.notifyDataSetChanged();
            }
        });
    }

    // RETRIEVING THE SONGS FROM THE SERVER
    public void retrieveSongs() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Songs");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Song song = ds.getValue(Song.class);
                    assert song != null;
                    if (!songsNameList.contains(song.getSongName())) {
                        songsNameList.add(song.getSongName());
                        songsUrlList.add(song.getSongUrl());
                        songsArtistList.add(song.getSongArtist());
                        songsDurationList.add(song.getSongDuration());
                        thumbnail.add(song.getImageUrl());
                        jcAudios.add(JcAudio.createFromURL(song.getSongName(), song.getSongUrl()));
                    }
                }
                adapter = new ListAdapter(getApplicationContext(), songsNameList, thumbnail, songsArtistList, songsDurationList);
                jcPlayerView.initPlaylist(jcAudios, null);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "FAILED!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    // menu function
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.uploadItem){
            if (validatePermissions()){
                Intent intent = new Intent(this,UploadSongActivity.class);
                startActivity(intent);
            }
        }else if (item.getItemId() == R.id.signOut){
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // METHOD TO HANDEL RUNTIME PERMISSIONS
    private boolean validatePermissions(){
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        checkPermission = true;
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        checkPermission = false;
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
        return checkPermission;
    }
}