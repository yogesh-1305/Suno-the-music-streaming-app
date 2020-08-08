package com.suno;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    private boolean checkPermission = false;
    Uri uri;
    private StorageReference storageReference;
    ProgressDialog progressDialog;
    ListView listView;
    ArrayList<String> songsNameList;
    ArrayList<String> songsUrlList;
    ArrayAdapter<String> adapter;
    JcPlayerView jcPlayerView;
    ArrayList<JcAudio> jcAudios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setMessage("Please Wait...");
        listView = findViewById(R.id.songsList);
        songsNameList = new ArrayList<>();
        songsUrlList = new ArrayList<>();
        jcAudios = new ArrayList<>();
        jcPlayerView = findViewById(R.id.jcplayer);
        retrieveSongs();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                jcPlayerView.playAudio(jcAudios.get(i));
                jcPlayerView.setVisibility(View.VISIBLE);
                jcPlayerView.createNotification();
            }
        });
    }

    // RETRIEVING THE SONGS FROM THE SERVER
    public void retrieveSongs(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Songs");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songsNameList.clear();
                songsUrlList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Song song = ds.getValue(Song.class);
                    songsNameList.add(song.getSongName());
                    songsUrlList.add(song.getSongUrl());
                    jcAudios.add(JcAudio.createFromURL(song.getSongName(),song.getSongUrl()));
                }
                adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,songsNameList);
                jcPlayerView.initPlaylist(jcAudios,null);
                listView.setAdapter(adapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "fAILED!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.uploadItem){
            if (validatePermissions()){
                pickSong();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // SELECT THE SONG TO UPLOAD FROM MOBILE STORAGE
    private void pickSong() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent,1);
    }

    // AFTER SELECTING THE SONG FROM MOBILE STORAGE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 1 && resultCode == RESULT_OK) {
                uri = data.getData();
                Log.i("uri", uri.toString());
                String fileName = getFileName(uri);
                Log.i("songName", fileName);
                uploadFileToServer(uri,getFileName(uri));
            }
        }
    }

    // METHOD TO HANDEL SONG UPLOAD TO THE STORAGE SERVER
    public void uploadFileToServer(Uri uri, final String songName){
        StorageReference filePath = storageReference.child("Audios").child(songName);
        progressDialog.show();
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("success", "upload");
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri urlSong = uriTask.getResult();
                String songUrl = urlSong.toString();
                Log.i("success url ", songUrl);
                uploadDetailsToDatabase(songName,songUrl);
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
            }

        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                int currentProgress = (int) progress;
                progressDialog.setMessage("Uploading: " + currentProgress + "%");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("success", "upload");
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Upload Failed! Please Try again!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // UPLOAD SONG NAME AND URL TO REALTIME DATABASE
    public void uploadDetailsToDatabase(String songName, String songUrl){

        Song song = new Song(songName,songUrl);
        FirebaseDatabase.getInstance().getReference("Songs")
                .push().setValue(song).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MainActivity.this, "Song Uploaded to Database", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // METHOD TO GET THE SONG NAME
    public String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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