package app.hill.converter01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arthenica.ffmpegkit.Statistics;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.Session;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    // UI Components
    private Button selectVideoButton;
    private Button convertButton;
    private TextView fileNameText;
    private TextView conversionStatusText;
    private ProgressBar progressBar;
    
    // Data variables
    private Uri selectedVideoUri;
    private String selectedVideoPath;
    private String outputFormat = "mp3"; // Default format
    
    // Constants for format selection
    private static final String[] SUPPORTED_FORMATS = {"mp3", "aac", "wav", "ogg", "flac", "m4a"};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        initializeViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Initialize FFmpeg
        initializeFFmpeg();
        
        // Request permissions
        requestPermissions();
    }
    
    /**
     * Initialize all UI view references
     */
    private void initializeViews() {
        selectVideoButton = findViewById(R.id.selectVideoButton);
        convertButton = findViewById(R.id.convertButton);
        fileNameText = findViewById(R.id.fileNameText);
        conversionStatusText = findViewById(R.id.conversionStatusText);
        progressBar = findViewById(R.id.progressBar);
        
        // Initially disable convert button
        convertButton.setEnabled(false);
        progressBar.setVisibility(ProgressBar.GONE);
    }
    
    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        // Select video button click
        selectVideoButton.setOnClickListener(v -> selectVideoFile());
        
        // Convert button click
        convertButton.setOnClickListener(v -> startConversion());
    }
    
    /**
     * Initialize FFmpeg library
     */
    private void initializeFFmpeg() {
        FFmpegKitConfig.enableStatisticsCallback(statistics -> {
            // Update progress based on FFmpeg statistics
            runOnUiThread(() -> {
                // Parse statistics to get progress
                long time = statistics.getTime();
                if (time > 0) {
                    conversionStatusText.setText("Converting: " + time + "ms");
                }
            });
        });
    }
    
    /**
     * Request storage permissions using Dexter library
     */
    private void requestPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            .withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(MainActivity.this, 
                            "All permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                      //  Toast.makeText(MainActivity.this, "Permissions required for conversion", Toast.LENGTH_LONG).show();
                    }
                }
                
                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, 
                                                                PermissionToken token) {
                    token.continuePermissionRequest();
                }
            })
            .check();
    }
    
    /**
     * Open file picker to select video file
     */
    private void selectVideoFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/mp4", "video/mpeg", "video/avi"});
        startActivityForResult(intent, 100);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedVideoUri = data.getData();
            if (selectedVideoUri != null) {
                // Get real path from URI
                selectedVideoPath = FileUtils.getPath(this, selectedVideoUri);
                if (selectedVideoPath != null) {
                    // Display file name
                    File file = new File(selectedVideoPath);
                    fileNameText.setText("Selected: " + file.getName());
                    convertButton.setEnabled(true);
                    conversionStatusText.setText("Ready to convert");
                    
                    // Show format selection dialog
                    showFormatSelectionDialog();
                } else {
                    Toast.makeText(this, "Could not get file path", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    /**
     * Show dialog to select output audio format
     */
    private void showFormatSelectionDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = 
            new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Output Audio Format");
        builder.setItems(SUPPORTED_FORMATS, (dialog, which) -> {
            outputFormat = SUPPORTED_FORMATS[which];
            conversionStatusText.setText("Format: " + outputFormat.toUpperCase());
            Toast.makeText(this, "Format set to: " + outputFormat.toUpperCase(), 
                Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    /**
     * Start the conversion process
     */
    private void startConversion() {
        if (selectedVideoPath == null) {
            Toast.makeText(this, "Please select a video file first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if file exists
        File inputFile = new File(selectedVideoPath);
        if (!inputFile.exists()) {
            Toast.makeText(this, "Input file not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Generate output file name
        String outputFileName = generateOutputFileName();
        File outputDir = getOutputDirectory();
        
        // Check if output directory is writable
        if (!outputDir.canWrite()) {
            android.util.Log.e("Conversion", "Output directory not writable: " + outputDir.getAbsolutePath());
            Toast.makeText(this, "Cannot write to output directory", Toast.LENGTH_SHORT).show();
            return;
        }
        
        File outputFile = new File(outputDir, outputFileName);
        
        // Build FFmpeg command based on format
        String ffmpegCommand = buildFFmpegCommand(selectedVideoPath, outputFile.getAbsolutePath());
        
        // Show progress and disable buttons
        showProgress(true);
        
        // Log the FFmpeg command for debugging
        android.util.Log.d("FFmpegKit", "Command: " + ffmpegCommand);
        android.util.Log.d("FFmpegKit", "Output Path: " + outputFile.getAbsolutePath());
        
        // Execute FFmpeg command
        FFmpegKit.executeAsync(ffmpegCommand, session -> {
            runOnUiThread(() -> {
                showProgress(false);

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    // Conversion successful
                    conversionStatusText.setText("✓ Conversion complete!");
                    Toast.makeText(MainActivity.this,
                            "Conversion successful!\nSaved to: " + outputFile.getName(),
                            Toast.LENGTH_LONG).show();

                    // Offer to play or share
                    showConversionSuccessDialog(outputFile);
                } else {
                    // Conversion failed
                    String errorMsg = "Return Code: " + session.getReturnCode();
                    
                    // Try to get more details
                    if (session.getOutput() != null && !session.getOutput().isEmpty()) {
                        errorMsg += "\nDetails: " + session.getOutput().substring(0, 
                            Math.min(200, session.getOutput().length()));
                    }
                    
                    android.util.Log.e("FFmpegKit", "Full Output: " + session.getOutput());
                    
                    conversionStatusText.setText("✗ Conversion failed");
                    Toast.makeText(MainActivity.this,
                            "Conversion failed.\n" + errorMsg,
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }
    
    /**
     * Build FFmpeg command based on selected output format
     * @param inputPath Path to input video file
     * @param outputPath Path to output audio file
     * @return FFmpeg command string
     */
    private String buildFFmpegCommand(String inputPath, String outputPath) {
        String command;
        
        switch (outputFormat) {
            case "mp3":
                // MP3: 192 kbps bitrate, stereo
                command = "-i " + inputPath + " -vn -ar 44100 -ac 2 -b:a 192k -f mp3 " + outputPath;
                break;
                
            case "aac":
                // AAC: 192 kbps bitrate
                command = "-i " + inputPath + " -vn -c:a aac -b:a 192k " + outputPath;
                break;
                
            case "wav":
                // WAV: Lossless PCM format
                command = "-i " + inputPath + " -vn -c:a pcm_s16le -ar 44100 " + outputPath;
                break;
                
            case "ogg":
                // OGG: Quality level 5 (good quality)
                command = "-i " + inputPath + " -vn -c:a libvorbis -q:a 5 " + outputPath;
                break;
                
            case "flac":
                // FLAC: Lossless compression, level 5
                command = "-i " + inputPath + " -vn -c:a flac -compression_level 5 " + outputPath;
                break;
                
            case "m4a":
                // M4A: AAC in MP4 container
                command = "-i " + inputPath + " -vn -c:a aac -b:a 192k -f mp4 " + outputPath;
                break;
                
            default:
                // Default to MP3
                command = "-i " + inputPath + " -vn -ar 44100 -ac 2 -b:a 192k -f mp3 " + outputPath;
                break;
        }
        
        return command;
    }
    
    /**
     * Generate output filename with timestamp
     * @return Generated filename
     */
    private String generateOutputFileName() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(new Date());
        
        // Get original filename without extension
        File inputFile = new File(selectedVideoPath);
        String originalName = inputFile.getName();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex > 0) ? originalName.substring(0, dotIndex) : originalName;
        
        return baseName + "_" + timestamp + "." + outputFormat;
    }
    
    /**
     * Get output directory for converted files
     * @return Output directory file
     */
    private File getOutputDirectory() {
        // Use Music directory: /music/media-converter/
        File outputDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MUSIC), "media-converter");
        
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            android.util.Log.d("OutputDir", "Directory created: " + created + 
                " at " + outputDir.getAbsolutePath());
        }
        
        android.util.Log.d("OutputDir", "Output directory: " + outputDir.getAbsolutePath() +
            " | Exists: " + outputDir.exists() + " | Writable: " + outputDir.canWrite());
        
        return outputDir;
    }
    
    /**
     * Show/hide progress indicators
     * @param show True to show progress, false to hide
     */
    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            selectVideoButton.setEnabled(false);
            convertButton.setEnabled(false);
            conversionStatusText.setText("Converting... Please wait");
        } else {
            progressBar.setVisibility(ProgressBar.GONE);
            selectVideoButton.setEnabled(true);
            convertButton.setEnabled(true);
        }
    }
    
    /**
     * Show success dialog with options to play or share
     * @param outputFile Converted audio file
     */
    private void showConversionSuccessDialog(File outputFile) {
        androidx.appcompat.app.AlertDialog.Builder builder = 
            new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Conversion Complete");
        builder.setMessage("Audio saved to:\n" + outputFile.getAbsolutePath());
        builder.setPositiveButton("Play", (dialog, which) -> {
            // Play the audio file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(outputFile), "audio/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Play with"));
        });
        builder.setNeutralButton("Share", (dialog, which) -> {
            // Share the audio file
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("audio/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(outputFile));
            startActivity(Intent.createChooser(shareIntent, "Share audio"));
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }
}