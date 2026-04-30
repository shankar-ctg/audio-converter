package app.hill.converter01.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

import app.hill.converter01.R;
import app.hill.converter01.config.AppConfig;
import app.hill.converter01.media.MediaConverter;
import app.hill.converter01.utils.CommonUtils;
import app.hill.converter01.utils.FileUtils;
import app.hill.converter01.utils.AppLogger;

/**
 * Main Activity for Media Conversion
 * Handles video/audio selection and format conversion
 */
public class MainActivity extends AppCompatActivity {
    
    // UI Components
    private Button selectMediaButton;
    private Button convertButton;
    private Button trimButton;
    private TextView fileNameText;
    private TextView conversionStatusText;
    private ProgressBar progressBar;
    
    // Data variables
    private Uri selectedMediaUri;
    private String selectedMediaPath;
    private String outputFormat = AppConfig.DEFAULT_FORMAT;
    private int bitrate = AppConfig.DEFAULT_BITRATE;
    
    // Media converter instance
    private MediaConverter mediaConverter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize logger
        AppLogger.init(this);
        
        // Initialize UI
        initializeViews();
        
        // Setup listeners
        setupClickListeners();
        
        // Initialize media converter
        mediaConverter = new MediaConverter();
        mediaConverter.setListener(conversionListener);
        
        // Request permissions
        requestPermissions();
        
        // Check for shared files from intent
        handleSharedFile(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSharedFile(intent);
    }
    
    /**
     * Handle shared media files from other apps
     */
    private void handleSharedFile(Intent intent) {
        if (intent == null) return;
        
        String action = intent.getAction();
        String type = intent.getType();
        
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("audio/") || type.startsWith("video/")) {
                Uri sharedUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (sharedUri != null) {
                    selectedMediaUri = sharedUri;
                    selectedMediaPath = FileUtils.getPath(this, sharedUri);
                    
                    if (selectedMediaPath != null) {
                        File file = new File(selectedMediaPath);
                        fileNameText.setText("Selected: " + file.getName());
                        convertButton.setEnabled(true);
                        if (trimButton != null) {
                            trimButton.setEnabled(true);
                        }
                        conversionStatusText.setText("Ready to convert");
                        
                        // Show format selection dialog
                        showFormatSelectionDialog();
                    } else {
                        AppLogger.showToast("Could not access shared file");
                    }
                }
            }
        }
    }
    
    /**
     * Initialize all UI view references
     */
    private void initializeViews() {
        selectMediaButton = findViewById(R.id.selectVideoButton);
        convertButton = findViewById(R.id.convertButton);
        fileNameText = findViewById(R.id.fileNameText);
        conversionStatusText = findViewById(R.id.conversionStatusText);
        progressBar = findViewById(R.id.progressBar);
        
        // Find trim button if it exists
        try {
            trimButton = findViewById(R.id.trimButton);
        } catch (Exception e) {
            AppLogger.d("Trim button not found in layout");
        }
        
        // Initially disable buttons
        convertButton.setEnabled(false);
        if (trimButton != null) {
            trimButton.setEnabled(false);
        }
        progressBar.setVisibility(ProgressBar.GONE);
    }
    
    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        selectMediaButton.setOnClickListener(v -> selectMediaFile());
        convertButton.setOnClickListener(v -> startConversion());
        
        if (trimButton != null) {
            trimButton.setOnClickListener(v -> startTrimming());
        }
    }
    
    /**
     * Open file picker to select media file
     */
    private void selectMediaFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
            "video/*", "audio/mpeg", "audio/wav", "audio/*"
        });
        startActivityForResult(intent, 100);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedMediaUri = data.getData();
            if (selectedMediaUri != null) {
                // Get real path from URI
                selectedMediaPath = FileUtils.getPath(this, selectedMediaUri);
                if (selectedMediaPath != null) {
                    // Display file name
                    File file = new File(selectedMediaPath);
                    fileNameText.setText("Selected: " + file.getName());
                    convertButton.setEnabled(true);
                    if (trimButton != null) {
                        trimButton.setEnabled(true);
                    }
                    conversionStatusText.setText("Ready to convert");
                    
                    // Show format selection dialog
                    showFormatSelectionDialog();
                } else {
                    AppLogger.showToast("Could not get file path");
                }
            }
        }
    }
    
    /**
     * Show dialog to select output format
     */
    private void showFormatSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Output Audio Format");
        builder.setItems(AppConfig.SUPPORTED_FORMATS, (dialog, which) -> {
            outputFormat = AppConfig.SUPPORTED_FORMATS[which];
            conversionStatusText.setText("Format: " + outputFormat.toUpperCase());
            AppLogger.showToast("Format set to: " + outputFormat.toUpperCase());
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    /**
     * Start media conversion process
     */
    private void startConversion() {
        if (selectedMediaPath == null) {
            AppLogger.showToast("Please select a media file first");
            return;
        }
        
        // Validate input file
        File inputFile = new File(selectedMediaPath);
        if (!inputFile.exists()) {
            AppLogger.showToast("Input file not found");
            return;
        }
        
        // Generate output file name
        String outputFileName = generateOutputFileName();
        File outputDir = getOutputDirectory();
        
        if (!outputDir.canWrite()) {
            AppLogger.e("Output directory not writable: " + outputDir.getAbsolutePath());
            AppLogger.showToast("Cannot write to output directory");
            return;
        }
        
        File outputFile = new File(outputDir, outputFileName);
        
        // Show progress
        showProgress(true);
        
        // Start conversion
        mediaConverter.convertMediaWithBitrate(
            selectedMediaPath,
            outputFile.getAbsolutePath(),
            outputFormat,
            bitrate
        );
    }
    
    /**
     * Start trimming activity
     */
    private void startTrimming() {
        if (selectedMediaPath == null) {
            AppLogger.showToast("Please select a media file first");
            return;
        }
        
        Intent intent = new Intent(this, MediaTrimmerActivity.class);
        intent.putExtra("mediaPath", selectedMediaPath);
        intent.putExtra("mediaUri", selectedMediaUri);
        startActivity(intent);
    }
    
    /**
     * Media conversion listener
     */
    private final MediaConverter.OnConversionListener conversionListener = 
        new MediaConverter.OnConversionListener() {
            @Override
            public void onConversionStarted() {
                runOnUiThread(() -> {
                    conversionStatusText.setText("Converting... Please wait");
                });
            }
            
            @Override
            public void onConversionProgress(long timeMs) {
                runOnUiThread(() -> {
                    conversionStatusText.setText("Converting: " + CommonUtils.formatMillis(timeMs));
                });
            }
            
            @Override
            public void onConversionCompleted(String outputPath) {
                runOnUiThread(() -> {
                    showProgress(false);
                    conversionStatusText.setText("✓ Conversion complete!");
                    
                    File outputFile = new File(outputPath);
                    AppLogger.showLongToast("Saved to: " + outputFile.getName());
                    
                    showConversionSuccessDialog(outputFile);
                });
            }
            
            @Override
            public void onConversionError(String errorMessage) {
                runOnUiThread(() -> {
                    showProgress(false);
                    conversionStatusText.setText("✗ Conversion failed");
                    AppLogger.showLongToast("Error: " + errorMessage);
                });
            }
        };
    
    /**
     * Generate output filename with timestamp
     */
    private String generateOutputFileName() {
        File inputFile = new File(selectedMediaPath);
        String originalName = inputFile.getName();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex > 0) ? originalName.substring(0, dotIndex) : originalName;
        
        return baseName + "_" + CommonUtils.getTimestamp() + "." + outputFormat;
    }
    
    /**
     * Get output directory for converted files
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
     * Show/hide progress indicators
     */
    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            selectMediaButton.setEnabled(false);
            convertButton.setEnabled(false);
            if (trimButton != null) {
                trimButton.setEnabled(false);
            }
        } else {
            progressBar.setVisibility(ProgressBar.GONE);
            selectMediaButton.setEnabled(true);
            convertButton.setEnabled(true);
            if (trimButton != null) {
                trimButton.setEnabled(true);
            }
        }
    }
    
    /**
     * Show success dialog with options
     */
    private void showConversionSuccessDialog(File outputFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Conversion Complete");
        builder.setMessage("Saved to:\n" + outputFile.getAbsolutePath());
        builder.setPositiveButton("Play", (dialog, which) -> playMedia(outputFile));
        builder.setNeutralButton("Share", (dialog, which) -> shareMedia(outputFile));
        builder.setNegativeButton("Close", null);
        builder.show();
    }
    
    /**
     * Play converted media
     */
    private void playMedia(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Play with"));
    }
    
    /**
     * Share converted media
     */
    private void shareMedia(File file) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("audio/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(shareIntent, "Share audio"));
    }
    
    /**
     * Request storage permissions
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
                        AppLogger.showToast("All permissions granted");
                    }
                }
                
                @Override
                public void onPermissionRationaleShouldBeShown(
                        List<PermissionRequest> permissions, 
                        PermissionToken token) {
                    token.continuePermissionRequest();
                }
            })
            .check();
    }
}
