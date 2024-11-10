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
            alarm.setOnCompletionListener(mp -> {
                // Reset the player when the sound finishes playing
                isPlaying = false;
            });
        } else {
            // If it's already playing, reset the MediaPlayer and play again
            alarm.seekTo(0);
            alarm.start();
        }
    }

    // Method to stop the ringtone
    public void stop() {
        if (isPlaying) {
            alarm.stop();
            alarm.reset();  // Reset the MediaPlayer to prepare it for reuse
            alarm.release(); // Release resources
            isPlaying = false;
        }
    }

    // Getter for isPlaying
    public boolean isPlaying() {
        return isPlaying;
    }
}
