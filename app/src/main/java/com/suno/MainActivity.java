package com.suno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jean.jcplayer.JcPlayerManagerListener;
import com.example.jean.jcplayer.general.JcStatus;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private boolean checkPermission = false;

    ConstraintLayout bottomSheetLayout;
    BottomSheetBehavior bottomSheetBehavior;

    ImageButton collapsePlayer, playListButton;
    CardView currentPlayingCardView;

    TextView currentPlayingSongName, currentPlayingArtistName;
    ImageView largePlayerImage, currentPlayingImage;
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

        largePlayerImage = findViewById(R.id.LargePlayerImage);
        listView = findViewById(R.id.songsList);
        jcPlayerView = findViewById(R.id.jcplayer);
        bottomSheetLayout = findViewById(R.id.playerBottomSheetLayout);
        collapsePlayer = findViewById(R.id.collapsePlayerButton);
        playListButton = findViewById(R.id.playListButton);
        currentPlayingSongName = findViewById(R.id.currentlyPlayingSongName);
        currentPlayingArtistName = findViewById(R.id.currentlyPlayingArtistName);
        currentPlayingImage = findViewById(R.id.currentPlayingImage);
        currentPlayingCardView = findViewById(R.id.currentPlayingCardView);

        currentPlayingCardView.setOnClickListener(this);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

        collapsePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        jcPlayerView.setJcPlayerManagerListener(new JcPlayerManagerListener() {
            @Override
            public void onPreparedAudio(@NonNull JcStatus jcStatus) {
                Log.i("|||||PrepareAudio|||||", jcStatus.getJcAudio().getTitle());
                currentPlayingSongName.setText(jcStatus.getJcAudio().getTitle());
                currentPlayingArtistName.setText(songsArtistList.get(jcStatus.getJcAudio().getPosition()));
                Picasso.get().load(thumbnail.get(jcStatus.getJcAudio().getPosition())).into(largePlayerImage);
                Picasso.get().load(thumbnail.get(jcStatus.getJcAudio().getPosition())).into(currentPlayingImage);
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
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                jcPlayerView.playAudio(jcAudios.get(i));
                currentPlayingCardView.setVisibility(View.VISIBLE);
                currentPlayingSongName.setText(songsNameList.get(i));
                currentPlayingArtistName.setText(songsArtistList.get(i));
                Picasso.get().load(thumbnail.get(i)).into(largePlayerImage);
                Picasso.get().load(thumbnail.get(i)).into(currentPlayingImage);
                jcPlayerView.createNotification();
                adapter.notifyDataSetChanged();
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                Log.i("scroll", String.valueOf(i));
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                Log.i("scroll", i + "/" + i1 + "/" + i2 +"/"+ absListView);
                if (i>2){
                    currentPlayingCardView.setVisibility(View.INVISIBLE);
                }else if (i == 0){
                    currentPlayingCardView.setVisibility(View.VISIBLE);
                }
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
                startActivity(new Intent(this,UploadSongActivity.class));
            }
        }else if (item.getItemId() == R.id.signOut){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Confirm Sign Out?")
                    .setIcon(R.drawable.com_facebook_close)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mAuth.signOut();
                            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton("NO",null).show();
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.currentPlayingCardView){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}