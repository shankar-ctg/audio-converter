package app.hill.converter01.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import app.hill.converter01.R;
import app.hill.converter01.media.AudioPreviewPlayer;
import app.hill.converter01.media.AudioWaveformExtractor;
import app.hill.converter01.utils.CommonUtils;
import app.hill.converter01.utils.AppLogger;

/**
 * Activity to display conversion results with share and play options
 */
public class ConversionResultActivity extends AppCompatActivity {
    
    private static final int REQUEST_CODE_SAVE_FILE = 1001;
    
    private TextView fileNameText;
    private TextView fileSizeText;
    private TextView durationText;
    private TextView currentTimeText;
    private ImageView waveformImageView;
    private SeekBar playbackSeekBar;
    private Button playPauseButton;
    private Button stopButton;
    private Button shareButton;
    private Button saveLocationButton;
    private Button openButton;
    private ProgressBar loadingBar;
    
    private String outputFilePath;
    private AudioPreviewPlayer audioPlayer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion_result);
        
        AppLogger.init(this);
        
        // Get output file path from intent
        Intent intent = getIntent();
        outputFilePath = intent.getStringExtra("outputPath");
        
        if (outputFilePath == null) {
            Toast.makeText(this, "No output file provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize UI
        initializeViews();
        
        // Initialize audio player
        audioPlayer = new AudioPreviewPlayer(this);
        audioPlayer.setListener(playbackListener);
        
        // Load file information
        loadFileInfo();
        
        // Setup listeners
        setupListeners();
    }
    
    /**
     * Initialize all UI views
     */
    private void initializeViews() {
        fileNameText = findViewById(R.id.fileNameText);
        fileSizeText = findViewById(R.id.fileSizeText);
        durationText = findViewById(R.id.durationText);
        currentTimeText = findViewById(R.id.currentTimeText);
        waveformImageView = findViewById(R.id.waveformImageView);
        playbackSeekBar = findViewById(R.id.playbackSeekBar);
        playPauseButton = findViewById(R.id.playPauseButton);
        stopButton = findViewById(R.id.stopButton);
        shareButton = findViewById(R.id.shareButton);
        saveLocationButton = findViewById(R.id.saveLocationButton);
        openButton = findViewById(R.id.openButton);
        loadingBar = findViewById(R.id.loadingBar);
    }
    
    /**
     * Load and display file information
     */
    private void loadFileInfo() {
        File outputFile = new File(outputFilePath);
        
        // File name
        fileNameText.setText("File: " + outputFile.getName());
        
        // File size
        long fileSizeBytes = outputFile.length();
        fileSizeText.setText("Size: " + formatFileSize(fileSizeBytes));
        
        // Duration
        long durationMs = AudioWaveformExtractor.getAudioDuration(outputFilePath);
        durationText.setText("Duration: " + CommonUtils.formatMillis(durationMs));
        
        // Setup seek bar
        playbackSeekBar.setMax((int) durationMs);
        
        // Extract and display waveform
        loadWaveformPreview();
    }
    
    /**
     * Load waveform visualization
     */
    private void loadWaveformPreview() {
        loadingBar.setVisibility(ProgressBar.VISIBLE);
        
        new Thread(() -> {
            try {
                // Extract waveform data
                int[] waveformData = AudioWaveformExtractor.extractWaveformData(
                    outputFilePath, 
                    200  // 200 samples for display
                );
                
                runOnUiThread(() -> {
                    // Draw waveform (in real app, use custom view)
                    // For now, just show as message
                    AppLogger.d("Waveform data loaded: " + waveformData.length + " samples");
                    loadingBar.setVisibility(ProgressBar.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    AppLogger.e("Error loading waveform: " + e.getMessage());
                    loadingBar.setVisibility(ProgressBar.GONE);
                });
            }
        }).start();
    }
    
    /**
     * Setup button listeners
     */
    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayback());
        stopButton.setOnClickListener(v -> stopPlayback());
        shareButton.setOnClickListener(v -> shareFile());
        saveLocationButton.setOnClickListener(v -> openSaveLocationDialog());
        openButton.setOnClickListener(v -> openFile());
        
        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioPlayer.seekTo(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    /**
     * Toggle play/pause
     */
    private void togglePlayback() {
        if (audioPlayer.isPlaying()) {
            audioPlayer.pause();
        } else {
            if (audioPlayer.getCurrentPosition() == 0) {
                // Start from beginning
                audioPlayer.play(outputFilePath);
            } else {
                // Resume from pause
                audioPlayer.resume();
            }
        }
    }
    
    /**
     * Stop playback
     */
    private void stopPlayback() {
        audioPlayer.stop();
        playbackSeekBar.setProgress(0);
        currentTimeText.setText("00:00");
    }
    
    /**
     * Share audio file
     */
    private void shareFile() {
        File file = new File(outputFilePath);
        Uri fileUri = Uri.fromFile(file);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("audio/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        startActivity(Intent.createChooser(shareIntent, "Share Audio File"));
    }
    
    /**
     * Open save location dialog
     */
    private void openSaveLocationDialog() {
        File file = new File(outputFilePath);
        String fileName = file.getName();
        String mimeType = getMimeTypeForFile(fileName);
        
        // Use Storage Access Framework to let user choose save location
        Intent saveIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        saveIntent.addCategory(Intent.CATEGORY_OPENABLE);
        saveIntent.setType(mimeType);
        saveIntent.putExtra(Intent.EXTRA_TITLE, fileName);
        
        try {
            startActivityForResult(saveIntent, REQUEST_CODE_SAVE_FILE);
        } catch (Exception e) {
            AppLogger.showToast("Unable to open save dialog: " + e.getMessage());
        }
    }
    
    /**
     * Get MIME type based on file extension
     */
    private String getMimeTypeForFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".mp3")) return "audio/mpeg";
        if (lowerName.endsWith(".wav")) return "audio/wav";
        if (lowerName.endsWith(".aac")) return "audio/aac";
        if (lowerName.endsWith(".m4a")) return "audio/mp4";
        if (lowerName.endsWith(".ogg")) return "audio/ogg";
        if (lowerName.endsWith(".flac")) return "audio/flac";
        return "audio/*";
    }
    
    /**
     * Copy file to selected location
     */
    private void copyFileToUri(Uri destinationUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = new FileInputStream(outputFilePath);
                OutputStream outputStream = getContentResolver().openOutputStream(destinationUri);
                
                if (outputStream != null) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    
                    inputStream.close();
                    outputStream.close();
                    
                    runOnUiThread(() -> 
                        AppLogger.showToast("File saved successfully!")
                    );
                }
            } catch (Exception e) {
                runOnUiThread(() -> 
                    AppLogger.showToast("Error saving file: " + e.getMessage())
                );
            }
        }).start();
    }
    
    /**
     * Open file with default player
     */
    private void openFile() {
        File file = new File(outputFilePath);
        Uri fileUri = Uri.fromFile(file);
        
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(fileUri, "audio/*");
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        startActivity(Intent.createChooser(openIntent, "Open Audio File"));
    }
    
    /**
     * Playback listener for progress updates
     */
    private final AudioPreviewPlayer.OnPlaybackListener playbackListener =
        new AudioPreviewPlayer.OnPlaybackListener() {
            @Override
            public void onPlaybackStarted() {
                playPauseButton.setText("Pause");
            }
            
            @Override
            public void onPlaybackPaused() {
                playPauseButton.setText("Resume");
            }
            
            @Override
            public void onPlaybackResumed() {
                playPauseButton.setText("Pause");
            }
            
            @Override
            public void onPlaybackStopped() {
                playPauseButton.setText("Play");
            }
            
            @Override
            public void onPlaybackCompleted() {
                playPauseButton.setText("Play");
                playbackSeekBar.setProgress(0);
                currentTimeText.setText("00:00");
                AppLogger.showToast("Playback completed");
            }
            
            @Override
            public void onPlaybackProgress(int currentMs, int totalMs) {
                playbackSeekBar.setProgress(currentMs);
                currentTimeText.setText(CommonUtils.formatMillis(currentMs));
            }
            
            @Override
            public void onPlaybackError(String error) {
                AppLogger.showToast("Playback error: " + error);
                playPauseButton.setText("Play");
            }
        };
    
    /**
     * Format file size to human readable
     */
    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", 
            bytes / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.release();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_SAVE_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri destinationUri = data.getData();
                copyFileToUri(destinationUri);
            }
        }
    }
}
