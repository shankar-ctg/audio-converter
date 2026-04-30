package app.hill.converter01.ui;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

import app.hill.converter01.R;
import app.hill.converter01.config.AppConfig;
import app.hill.converter01.media.AudioPreviewPlayer;
import app.hill.converter01.media.AudioWaveformExtractor;
import app.hill.converter01.media.MediaTrimInfo;
import app.hill.converter01.media.MediaTrimmer;
import app.hill.converter01.utils.CommonUtils;
import app.hill.converter01.utils.AppLogger;

/**
 * Activity for trimming/cutting media files
 * Allows users to select start and end times, then extract that portion
 */
public class MediaTrimmerActivity extends AppCompatActivity {
    
    // UI Components
    private TextView mediaNameText;
    private TextView durationText;
    private TextView startTimeDisplay;
    private TextView endTimeDisplay;
    
    private SeekBar startTimeSeeker;
    private SeekBar endTimeSeeker;
    private SeekBar previewPlaybackSeeker;
    
    private EditText startTimeInput;
    private EditText endTimeInput;
    
    private Button previewButton;
    private Button trimButton;
    private Button backButton;
    private Button previewPlayButton;
    private Button previewPauseButton;
    
    private ImageView waveformImageView;
    private ProgressBar trimProgressBar;
    private ProgressBar waveformLoadingBar;
    
    private TextView playbackCurrentTimeText;
    private TextView playbackTotalTimeText;
    
    // Data
    private String mediaPath;
    private Uri mediaUri;
    private long mediaDuration = 0;
    private String outputFormat = AppConfig.DEFAULT_FORMAT;
    private int bitrate = AppConfig.DEFAULT_BITRATE;
    
    // Trimmer and player instances
    private MediaTrimmer mediaTrimmer;
    private AudioPreviewPlayer previewPlayer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trimmer);
        
        AppLogger.init(this);
        
        // Get media path from intent
        Intent intent = getIntent();
        mediaPath = intent.getStringExtra("mediaPath");
        mediaUri = intent.getParcelableExtra("mediaUri");
        
        if (mediaPath == null) {
            Toast.makeText(this, "Media path not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        initializeViews();
        
        // Initialize media trimmer
        mediaTrimmer = new MediaTrimmer();
        mediaTrimmer.setListener(trimListener);
        
        // Initialize preview player
        previewPlayer = new AudioPreviewPlayer(this);
        previewPlayer.setListener(playbackListener);
        
        // Load media info
        loadMediaInfo();
        
        // Setup listeners
        setupListeners();
    }
    
    /**
     * Initialize UI views
     */
    private void initializeViews() {
        mediaNameText = findViewById(R.id.mediaNameText);
        durationText = findViewById(R.id.durationText);
        startTimeDisplay = findViewById(R.id.startTimeDisplay);
        endTimeDisplay = findViewById(R.id.endTimeDisplay);
        
        startTimeSeeker = findViewById(R.id.startTimeSeeker);
        endTimeSeeker = findViewById(R.id.endTimeSeeker);
        previewPlaybackSeeker = findViewById(R.id.previewPlaybackSeeker);
        
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        
        previewButton = findViewById(R.id.previewButton);
        trimButton = findViewById(R.id.trimButton);
        backButton = findViewById(R.id.backButton);
        previewPlayButton = findViewById(R.id.previewPlayButton);
        previewPauseButton = findViewById(R.id.previewPauseButton);
        
        waveformImageView = findViewById(R.id.waveformImageView);
        trimProgressBar = findViewById(R.id.trimProgressBar);
        waveformLoadingBar = findViewById(R.id.waveformLoadingBar);
        
        playbackCurrentTimeText = findViewById(R.id.playbackCurrentTimeText);
        playbackTotalTimeText = findViewById(R.id.playbackTotalTimeText);
        
        trimProgressBar.setVisibility(ProgressBar.GONE);
    }
    
    /**
     * Load media information
     */
    private void loadMediaInfo() {
        File mediaFile = new File(mediaPath);
        mediaNameText.setText("File: " + mediaFile.getName());
        
        // Get duration
        mediaDuration = mediaTrimmer.getMediaDuration(mediaPath);
        if (mediaDuration > 0) {
            durationText.setText("Duration: " + CommonUtils.formatMillis(mediaDuration));
            
            // Set seekbar max to duration
            startTimeSeeker.setMax((int) mediaDuration);
            endTimeSeeker.setMax((int) mediaDuration);
            previewPlaybackSeeker.setMax((int) mediaDuration);
            
            // Set initial end time to full duration
            endTimeSeeker.setProgress((int) mediaDuration);
            updateEndTimeDisplay((int) mediaDuration);
            
            // Set playback total time
            playbackTotalTimeText.setText(CommonUtils.formatMillis(mediaDuration));
            
            // Load waveform preview
            loadWaveformPreview();
        }
    }
    
    /**
     * Load waveform visualization
     */
    private void loadWaveformPreview() {
        waveformLoadingBar.setVisibility(ProgressBar.VISIBLE);
        
        new Thread(() -> {
            try {
                // Extract waveform data
                int[] waveformData = AudioWaveformExtractor.extractWaveformData(
                    mediaPath,
                    200  // 200 samples for display
                );
                
                runOnUiThread(() -> {
                    AppLogger.d("Waveform data loaded: " + waveformData.length + " samples");
                    waveformLoadingBar.setVisibility(ProgressBar.GONE);
                    // In a real app, draw the waveform visualization here
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    AppLogger.e("Error loading waveform: " + e.getMessage());
                    waveformLoadingBar.setVisibility(ProgressBar.GONE);
                });
            }
        }).start();
    }
    
    /**
     * Setup button and seekbar listeners
     */
    private void setupListeners() {
        startTimeSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= endTimeSeeker.getProgress()) {
                    startTimeSeeker.setProgress(endTimeSeeker.getProgress() - 1000);
                    return;
                }
                updateStartTimeDisplay(progress);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        endTimeSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= startTimeSeeker.getProgress()) {
                    endTimeSeeker.setProgress(startTimeSeeker.getProgress() + 1000);
                    return;
                }
                updateEndTimeDisplay(progress);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Preview playback seekbar listener
        previewPlaybackSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    previewPlayer.seekTo(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        previewButton.setOnClickListener(v -> showFormatSelectionDialog());
        previewPlayButton.setOnClickListener(v -> togglePlayback());
        previewPauseButton.setOnClickListener(v -> pausePlayback());
        trimButton.setOnClickListener(v -> startTrimming());
        backButton.setOnClickListener(v -> finish());
    }
    
    /**
     * Toggle playback
     */
    private void togglePlayback() {
        if (previewPlayer.isPlaying()) {
            previewPlayer.pause();
        } else {
            if (previewPlayer.getCurrentPosition() == 0) {
                previewPlayer.play(mediaPath);
            } else {
                previewPlayer.resume();
            }
        }
    }
    
    /**
     * Pause playback
     */
    private void pausePlayback() {
        previewPlayer.pause();
    }
    
    /**
     * Update start time display
     */
    private void updateStartTimeDisplay(int timeMs) {
        startTimeDisplay.setText("Start: " + CommonUtils.formatMillis(timeMs));
        startTimeInput.setText(CommonUtils.formatMillis(timeMs));
    }
    
    /**
     * Update end time display
     */
    private void updateEndTimeDisplay(int timeMs) {
        endTimeDisplay.setText("End: " + CommonUtils.formatMillis(timeMs));
        endTimeInput.setText(CommonUtils.formatMillis(timeMs));
    }
    
    /**
     * Show format selection dialog
     */
    private void showFormatSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Output Format");
        builder.setItems(AppConfig.SUPPORTED_FORMATS, (dialog, which) -> {
            outputFormat = AppConfig.SUPPORTED_FORMATS[which];
            Toast.makeText(this, "Format: " + outputFormat.toUpperCase(), Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    /**
     * Start trimming operation
     */
    private void startTrimming() {
        int startTime = startTimeSeeker.getProgress();
        int endTime = endTimeSeeker.getProgress();
        
        if (startTime >= endTime) {
            Toast.makeText(this, "Invalid time range", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Generate output filename
        String outputFileName = generateTrimmedFileName();
        File outputDir = getOutputDirectory();
        File outputFile = new File(outputDir, outputFileName);
        
        // Create trim info
        MediaTrimInfo trimInfo = new MediaTrimInfo();
        trimInfo.setInputPath(mediaPath);
        trimInfo.setOutputPath(outputFile.getAbsolutePath());
        trimInfo.setStartTimeMs(startTime);
        trimInfo.setEndTimeMs(endTime);
        
        // Show progress
        showProgress(true);
        
        // Start trim and convert
        mediaTrimmer.trimAndConvert(trimInfo, outputFormat, bitrate);
    }
    
    /**
     * Trim listener for operation feedback
     */
    private final MediaTrimmer.OnTrimListener trimListener = 
        new MediaTrimmer.OnTrimListener() {
            @Override
            public void onTrimStarted() {
                runOnUiThread(() -> {
                    Toast.makeText(MediaTrimmerActivity.this, "Trimming started...", 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onTrimProgress(long timeMs) {
                runOnUiThread(() -> {
                    // Update progress bar if needed
                });
            }
            
            @Override
            public void onTrimCompleted(String outputPath) {
                runOnUiThread(() -> {
                    showProgress(false);
                    File outputFile = new File(outputPath);
                    Toast.makeText(MediaTrimmerActivity.this,
                        "Trim successful!\nSaved: " + outputFile.getName(),
                        Toast.LENGTH_LONG).show();
                    
                    // Launch ConversionResultActivity
                    launchResultActivity(outputPath);
                });
            }
            
            @Override
            public void onTrimError(String errorMessage) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(MediaTrimmerActivity.this,
                        "Error: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                });
            }
        };
    
    /**
     * Playback listener for preview playback
     */
    private final AudioPreviewPlayer.OnPlaybackListener playbackListener =
        new AudioPreviewPlayer.OnPlaybackListener() {
            @Override
            public void onPlaybackStarted() {
                runOnUiThread(() -> previewPlayButton.setText("Playing"));
            }
            
            @Override
            public void onPlaybackPaused() {
                runOnUiThread(() -> previewPlayButton.setText("Resume"));
            }
            
            @Override
            public void onPlaybackResumed() {
                runOnUiThread(() -> previewPlayButton.setText("Pause"));
            }
            
            @Override
            public void onPlaybackStopped() {
                runOnUiThread(() -> {
                    previewPlayButton.setText("Play");
                    previewPlaybackSeeker.setProgress(0);
                    playbackCurrentTimeText.setText("00:00");
                });
            }
            
            @Override
            public void onPlaybackCompleted() {
                runOnUiThread(() -> {
                    previewPlayButton.setText("Play");
                    previewPlaybackSeeker.setProgress(0);
                    playbackCurrentTimeText.setText("00:00");
                });
            }
            
            @Override
            public void onPlaybackProgress(int currentMs, int totalMs) {
                runOnUiThread(() -> {
                    previewPlaybackSeeker.setProgress(currentMs);
                    playbackCurrentTimeText.setText(CommonUtils.formatMillis(currentMs));
                });
            }
            
            @Override
            public void onPlaybackError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MediaTrimmerActivity.this,
                        "Playback error: " + error,
                        Toast.LENGTH_SHORT).show();
                    previewPlayButton.setText("Play");
                });
            }
        };
    
    /**
     * Generate trimmed filename
     */
    private String generateTrimmedFileName() {
        File inputFile = new File(mediaPath);
        String originalName = inputFile.getName();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex > 0) ? originalName.substring(0, dotIndex) : originalName;
        
        return baseName + "_trimmed_" + CommonUtils.getTimestamp() + "." + outputFormat;
    }
    
    /**
     * Get output directory
     */
    private File getOutputDirectory() {
        File outputDir = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            AppConfig.OUTPUT_DIR_NAME
        );
        
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        return outputDir;
    }
    
    /**
     * Show/hide progress
     */
    private void showProgress(boolean show) {
        if (show) {
            trimProgressBar.setVisibility(ProgressBar.VISIBLE);
            trimButton.setEnabled(false);
            startTimeSeeker.setEnabled(false);
            endTimeSeeker.setEnabled(false);
        } else {
            trimProgressBar.setVisibility(ProgressBar.GONE);
            trimButton.setEnabled(true);
            startTimeSeeker.setEnabled(true);
            endTimeSeeker.setEnabled(true);
        }
    }
    
    /**
     * Launch conversion result activity with output file
     */
    private void launchResultActivity(String outputPath) {
        Intent intent = new Intent(this, ConversionResultActivity.class);
        intent.putExtra("outputPath", outputPath);
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (previewPlayer != null) {
            previewPlayer.release();
        }
    }
}
