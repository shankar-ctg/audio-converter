package app.hill.converter01.media;

import android.media.MediaMetadataRetriever;
import android.util.Log;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;

/**
 * Handles media trimming/cutting operations using FFmpeg
 * Can extract specific time ranges from audio/video files
 */
public class MediaTrimmer {
    private static final String TAG = "MediaTrimmer";
    private OnTrimListener listener;
    
    public interface OnTrimListener {
        void onTrimStarted();
        void onTrimProgress(long timeMs);
        void onTrimCompleted(String outputPath);
        void onTrimError(String errorMessage);
    }
    
    public MediaTrimmer() {}
    
    public void setListener(OnTrimListener listener) {
        this.listener = listener;
    }
    
    /**
     * Trim/cut media file from startTime to endTime
     * @param trimInfo Contains input path, output path, start time, and end time
     */
    public void trimMedia(MediaTrimInfo trimInfo) {
        if (!trimInfo.isValid()) {
            notifyError("Invalid trim information");
            return;
        }
        
        // Get duration for validation
        long totalDuration = getMediaDuration(trimInfo.getInputPath());
        if (totalDuration <= 0) {
            notifyError("Could not determine media duration");
            return;
        }
        
        // Validate time range
        if (trimInfo.getEndTimeMs() > totalDuration) {
            notifyError("End time exceeds media duration");
            return;
        }
        
        trimInfo.setTotalDurationMs(totalDuration);
        notifyStarted();
        
        // Build FFmpeg trim command
        String ffmpegCommand = buildTrimCommand(trimInfo);
        executeFFmpeg(ffmpegCommand, trimInfo.getOutputPath());
    }
    
    /**
     * Trim and convert media in one operation
     * @param trimInfo Trimming information
     * @param outputFormat Output audio format (mp3, aac, wav, etc.)
     * @param bitrate Audio bitrate in kbps
     */
    public void trimAndConvert(MediaTrimInfo trimInfo, String outputFormat, int bitrate) {
        if (!trimInfo.isValid()) {
            notifyError("Invalid trim information");
            return;
        }
        
        long totalDuration = getMediaDuration(trimInfo.getInputPath());
        if (totalDuration <= 0) {
            notifyError("Could not determine media duration");
            return;
        }
        
        if (trimInfo.getEndTimeMs() > totalDuration) {
            notifyError("End time exceeds media duration");
            return;
        }
        
        trimInfo.setTotalDurationMs(totalDuration);
        notifyStarted();
        
        String ffmpegCommand = buildTrimAndConvertCommand(trimInfo, outputFormat, bitrate);
        executeFFmpeg(ffmpegCommand, trimInfo.getOutputPath());
    }
    
    /**
     * Get media duration in milliseconds
     */
    public long getMediaDuration(String mediaPath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mediaPath);
            
            String durationStr = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            
            if (durationStr != null) {
                return Long.parseLong(durationStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting media duration", e);
        }
        return -1;
    }
    
    /**
     * Build FFmpeg trim command
     * Uses -ss for seek input and -t for duration
     */
    private String buildTrimCommand(MediaTrimInfo trimInfo) {
        long startSeconds = trimInfo.getStartTimeMs() / 1000;
        long durationSeconds = (trimInfo.getEndTimeMs() - trimInfo.getStartTimeMs()) / 1000;
        
        return String.format(
            "-ss %d -i \"%s\" -t %d -q:a 0 -map a \"%s\"",
            startSeconds,
            trimInfo.getInputPath(),
            durationSeconds,
            trimInfo.getOutputPath()
        );
    }
    
    /**
     * Build FFmpeg trim + convert command
     */
    private String buildTrimAndConvertCommand(MediaTrimInfo trimInfo, 
                                             String format, int bitrate) {
        long startSeconds = trimInfo.getStartTimeMs() / 1000;
        long durationSeconds = (trimInfo.getEndTimeMs() - trimInfo.getStartTimeMs()) / 1000;
        
        return String.format(
            "-ss %d -i \"%s\" -t %d -b:a %dk -q:a 0 -map a \"%s\"",
            startSeconds,
            trimInfo.getInputPath(),
            durationSeconds,
            bitrate,
            trimInfo.getOutputPath()
        );
    }
    
    /**
     * Execute FFmpeg command asynchronously
     */
    private void executeFFmpeg(String command, String outputPath) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Executing FFmpeg: " + command);
                FFmpegKit.executeAsync(command, completeCallback -> {
                    if (ReturnCode.isSuccess(completeCallback.getReturnCode())) {
                        Log.d(TAG, "Trim successful");
                        notifyCompleted(outputPath);
                    } else {
                        Log.e(TAG, "Trim failed with code: " + completeCallback.getReturnCode());
                        notifyError("Trim operation failed");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error executing FFmpeg", e);
                notifyError("Error: " + e.getMessage());
            }
        }).start();
    }
    
    private void notifyStarted() {
        if (listener != null) {
            listener.onTrimStarted();
        }
    }
    
    private void notifyProgress(long timeMs) {
        if (listener != null) {
            listener.onTrimProgress(timeMs);
        }
    }
    
    private void notifyCompleted(String outputPath) {
        if (listener != null) {
            listener.onTrimCompleted(outputPath);
        }
    }
    
    private void notifyError(String error) {
        if (listener != null) {
            listener.onTrimError(error);
        }
    }
}
