package app.hill.converter01.media;

import android.util.Log;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;
import app.hill.converter01.config.AppConfig;

/**
 * Handles media conversion operations using FFmpeg
 */
public class MediaConverter {
    private static final String TAG = "MediaConverter";
    private OnConversionListener listener;
    
    public interface OnConversionListener {
        void onConversionStarted();
        void onConversionProgress(long time);
        void onConversionCompleted(String outputPath);
        void onConversionError(String errorMessage);
    }
    
    public MediaConverter() {}
    
    public void setListener(OnConversionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Convert media file to specified format
     * @param inputPath Full path to input file
     * @param outputPath Full path to output file
     * @param outputFormat Output format (mp3, aac, wav, etc.)
     * @param bitrate Audio bitrate in kbps
     */
    public void convertMedia(String inputPath, String outputPath, String outputFormat, int bitrate) {
        if (!isValidInput(inputPath, outputPath, outputFormat)) {
            notifyError("Invalid input parameters");
            return;
        }
        
        notifyStarted();
        
        // FFmpeg command for simple conversion
        String ffmpegCommand = String.format(
            "-i \"%s\" -q:a 0 -map a \"%s\"",
            inputPath,
            outputPath
        );
        
        executeFFmpeg(ffmpegCommand, outputPath);
    }
    
    /**
     * Convert media with specific bitrate
     */
    public void convertMediaWithBitrate(String inputPath, String outputPath, 
                                       String outputFormat, int bitrate) {
        if (!isValidInput(inputPath, outputPath, outputFormat)) {
            notifyError("Invalid input parameters");
            return;
        }
        
        notifyStarted();
        
        String ffmpegCommand = String.format(
            "-i \"%s\" -b:a %dk -q:a 0 -map a \"%s\"",
            inputPath,
            bitrate,
            outputPath
        );
        
        executeFFmpeg(ffmpegCommand, outputPath);
    }
    
    /**
     * Execute FFmpeg command
     */
    private void executeFFmpeg(String command, String outputPath) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Executing FFmpeg: " + command);
                FFmpegKit.executeAsync(command, completeCallback -> {
                    if (ReturnCode.isSuccess(completeCallback.getReturnCode())) {
                        Log.d(TAG, "Conversion successful");
                        notifyCompleted(outputPath);
                    } else {
                        Log.e(TAG, "Conversion failed with code: " + completeCallback.getReturnCode());
                        notifyError("Conversion failed");
                    }
                    
                });
            } catch (Exception e) {
                Log.e(TAG, "Error executing FFmpeg", e);
                notifyError("Error: " + e.getMessage());
            }
        }).start();
    }
    
    private boolean isValidInput(String inputPath, String outputPath, String format) {
        return inputPath != null && !inputPath.isEmpty() &&
               outputPath != null && !outputPath.isEmpty() &&
               format != null && !format.isEmpty();
    }
    
    private void notifyStarted() {
        if (listener != null) {
            listener.onConversionStarted();
        }
    }
    
    private void notifyProgress(long time) {
        if (listener != null) {
            listener.onConversionProgress(time);
        }
    }
    
    private void notifyCompleted(String outputPath) {
        if (listener != null) {
            listener.onConversionCompleted(outputPath);
        }
    }
    
    private void notifyError(String error) {
        if (listener != null) {
            listener.onConversionError(error);
        }
    }
}
