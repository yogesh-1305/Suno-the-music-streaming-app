package com.suno;

public class Song {
    private String songName,songUrl;
    private String imageUrl, songArtist, songDuration;
    private String userEmail;

    public Song() {
    }

    public Song(String userEmail, String songName, String songUrl, String imageUrl, String songArtist, String songDuration) {
        this.songName = songName;
        this.songUrl = songUrl;
        this.imageUrl = imageUrl;
        this.songArtist = songArtist;
        this.songDuration = songDuration;
        this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
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
