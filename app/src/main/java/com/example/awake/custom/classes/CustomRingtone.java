package com.example.awake.custom.classes;

import android.media.MediaPlayer;

public class CustomRingtone {

    private boolean isPlaying = false;
    private MediaPlayer alarm;

    // Constructor that accepts MediaPlayer
    public CustomRingtone(MediaPlayer mediaPlayer) {
        this.alarm = mediaPlayer;
    }

    // Method to play the ringtone
    public void play() {
        if (!isPlaying) {
            alarm.start();
            isPlaying = true;
        }
    }

    // Method to stop the ringtone
    public void stop() {
        if (isPlaying) {
            alarm.stop();
            alarm.release();  // Make sure to release the MediaPlayer when you're done
            isPlaying = false;
        }
    }

    // Getter for isPlaying
    public boolean isPlaying() {
        return isPlaying;
    }
}
