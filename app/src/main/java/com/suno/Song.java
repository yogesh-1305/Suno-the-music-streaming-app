package com.suno;

public class Song {
    private String songName,songUrl;
    private String imageUrl;

    public Song(String songName, String songUrl, String imageUrl) {
        this.songName = songName;
        this.songUrl = songUrl;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }
}
