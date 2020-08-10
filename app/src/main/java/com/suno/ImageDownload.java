package com.suno;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownload extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            // RETURNING THE INPUT STREAM AFTER CONVERTING IT TP AN IMAGE
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }
}
