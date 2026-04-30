package app.hill.converter01.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Handles audio preview and playback
 */
public class AudioPreviewPlayer {
    private static final String TAG = "AudioPreviewPlayer";
    
    private MediaPlayer mediaPlayer;
    private Context context;
    private OnPlaybackListener listener;
    
    public interface OnPlaybackListener {
        void onPlaybackStarted();
        void onPlaybackPaused();
        void onPlaybackResumed();
        void onPlaybackStopped();
        void onPlaybackCompleted();
        void onPlaybackProgress(int currentMs, int totalMs);
        void onPlaybackError(String error);
    }
    
    public AudioPreviewPlayer(Context context) {
        this.context = context;
        initializeMediaPlayer();
    }
    
    /**
     * Initialize media player with audio attributes
     */
    private void initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            );
        }
    }
    
    public void setListener(OnPlaybackListener listener) {
        this.listener = listener;
    }
    
    /**
     * Start playing audio file
     */
    public void play(String audioPath) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                notifyPlaybackStarted();
                updateProgress();
            });
            
            mediaPlayer.setOnCompletionListener(mp -> {
                notifyPlaybackCompleted();
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Playback error: " + what + ", " + extra);
                notifyPlaybackError("Playback error: " + what);
                return true;
            });
            
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Error preparing media player", e);
            notifyPlaybackError(e.getMessage());
        }
    }
    
    /**
     * Play from specific time
     */
    public void playFromTime(String audioPath, int startMs) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.seekTo(startMs);
                mp.start();
                notifyPlaybackStarted();
                updateProgress();
            });
            
            mediaPlayer.setOnCompletionListener(mp -> {
                notifyPlaybackCompleted();
            });
            
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Error preparing media player", e);
            notifyPlaybackError(e.getMessage());
        }
    }
    
    /**
     * Pause playback
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notifyPlaybackPaused();
        }
    }
    
    /**
     * Resume playback
     */
    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            notifyPlaybackResumed();
            updateProgress();
        }
    }
    
    /**
     * Stop playback
     */
    public void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            notifyPlaybackStopped();
        }
    }
    
    /**
     * Seek to specific position
     */
    public void seekTo(int positionMs) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(positionMs);
        }
    }
    
    /**
     * Get current playback position
     */
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }
    
    /**
     * Get total duration
     */
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }
    
    /**
     * Check if currently playing
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
    
    /**
     * Update progress with thread
     */
    private void updateProgress() {
        new Thread(() -> {
            while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                try {
                    notifyPlaybackProgress(
                        mediaPlayer.getCurrentPosition(),
                        mediaPlayer.getDuration()
                    );
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    
    /**
     * Release resources
     */
    public void release() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing media player", e);
            }
            mediaPlayer = null;
        }
    }
    
    // Notification methods
    private void notifyPlaybackStarted() {
        if (listener != null) {
            listener.onPlaybackStarted();
        }
    }
    
    private void notifyPlaybackPaused() {
        if (listener != null) {
            listener.onPlaybackPaused();
        }
    }
    
    private void notifyPlaybackResumed() {
        if (listener != null) {
            listener.onPlaybackResumed();
        }
    }
    
    private void notifyPlaybackStopped() {
        if (listener != null) {
            listener.onPlaybackStopped();
        }
    }
    
    private void notifyPlaybackCompleted() {
        if (listener != null) {
            listener.onPlaybackCompleted();
        }
    }
    
    private void notifyPlaybackProgress(int currentMs, int totalMs) {
        if (listener != null) {
            listener.onPlaybackProgress(currentMs, totalMs);
        }
    }
    
    private void notifyPlaybackError(String error) {
        if (listener != null) {
            listener.onPlaybackError(error);
        }
    }
}
