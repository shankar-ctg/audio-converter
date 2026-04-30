# 📖 API Reference - MediaConverter

## Class Overview

### 🎵 MediaConverter
**Location**: `app.hill.converter01.media.MediaConverter`

Handles video to audio conversion with multiple format support.

#### Methods
```java
// Set conversion listener
void setListener(OnConversionListener listener)

// Convert media file
void convertMedia(String inputPath, String outputPath, 
                 String outputFormat, int bitrate)

// Convert with specific bitrate
void convertMediaWithBitrate(String inputPath, String outputPath,
                             String outputFormat, int bitrate)
```

#### Listener Interface
```java
interface OnConversionListener {
    void onConversionStarted();
    void onConversionProgress(long timeMs);
    void onConversionCompleted(String outputPath);
    void onConversionError(String errorMessage);
}
```

---

### ✂️ MediaTrimmer
**Location**: `app.hill.converter01.media.MediaTrimmer`

Handles trimming and cutting media files with time precision.

#### Methods
```java
// Set trim listener
void setListener(OnTrimListener listener)

// Trim media only
void trimMedia(MediaTrimInfo trimInfo)

// Trim and convert in one operation
void trimAndConvert(MediaTrimInfo trimInfo, String outputFormat, int bitrate)

// Get media duration
long getMediaDuration(String mediaPath)
```

#### Listener Interface
```java
interface OnTrimListener {
    void onTrimStarted();
    void onTrimProgress(long timeMs);
    void onTrimCompleted(String outputPath);
    void onTrimError(String errorMessage);
}
```

---

### 📦 MediaTrimInfo
**Location**: `app.hill.converter01.media.MediaTrimInfo`

Data model for storing trim operation parameters.

#### Properties
```java
long startTimeMs;         // Start time in milliseconds
long endTimeMs;          // End time in milliseconds
long totalDurationMs;    // Total media duration
String inputPath;        // Full path to input file
String outputPath;       // Full path to output file
```

#### Methods
```java
// Getters
long getStartTimeMs()
long getEndTimeMs()
long getTotalDurationMs()
String getInputPath()
String getOutputPath()

// Setters
void setStartTimeMs(long startTimeMs)
void setEndTimeMs(long endTimeMs)
void setTotalDurationMs(long totalDurationMs)
void setInputPath(String inputPath)
void setOutputPath(String outputPath)

// Utilities
long getDurationMs()                    // endTime - startTime
String getFormattedStartTime()          // Returns HH:MM:SS or MM:SS
String getFormattedEndTime()
String getFormattedDuration()
boolean isValid()                       // Validates all parameters
```

---

### ⚙️ AppConfig
**Location**: `app.hill.converter01.config.AppConfig`

Centralized configuration constants.

#### Constants
```java
String[] SUPPORTED_FORMATS = {
    "mp3", "aac", "wav", "ogg", "flac", "m4a"
}

int[] QUALITY_BITRATES = {
    128, 192, 256, 320  // kbps
}

String DEFAULT_FORMAT = "mp3"
int DEFAULT_BITRATE = 192
String OUTPUT_DIR_NAME = "MediaConverted"
```

---

### 📄 FileUtils
**Location**: `app.hill.converter01.utils.FileUtils`

File and path management utilities.

#### Methods
```java
// Convert URI to file path
static String getPath(Context context, Uri uri)

// Get file name from URI
static String getFileName(Context context, Uri uri)

// Get output file path with timestamp
static String getOutputFilePath(Context context, String fileName, 
                               String extension)

// Create output directory
static boolean createOutputDirectory(String dirPath)

// Private helpers (internal use)
private static String getDataColumn(Context context, Uri uri, 
                                    String selection, String[] selectionArgs)
private static boolean isExternalStorageDocument(Uri uri)
private static boolean isDownloadsDocument(Uri uri)
private static boolean isMediaDocument(Uri uri)
```

---

### 🛠️ CommonUtils
**Location**: `app.hill.converter01.utils.CommonUtils`

Common utility functions.

#### Methods
```java
// Format milliseconds to time string
static String formatMillis(long millis)
// Returns: "HH:MM:SS" or "MM:SS"

// Parse time string to milliseconds
static long parseTimeToMillis(String timeStr)
// Accepts: "MM:SS" or "HH:MM:SS" format

// Get current timestamp
static String getTimestamp()
// Returns: "yyyyMMdd_HHmmss"

// Calculate percentage
static int calculatePercentage(long current, long total)

// Format file size
static String formatFileSize(long bytes)
// Returns: "1.5 MB", "256 KB", etc.
```

---

### 📢 AppLogger
**Location**: `app.hill.converter01.utils.AppLogger`

Centralized logging and notifications.

#### Methods
```java
// Initialize (call once in Application or MainActivity)
static void init(Context ctx)

// Logging methods
static void d(String message)           // Debug
static void i(String message)           // Info
static void e(String message)           // Error
static void e(String message, Exception ex)

// Toast notifications
static void showToast(String message)       // Short duration
static void showLongToast(String message)   // Long duration
```

---

## Usage Examples

### Example 1: Simple Conversion
```java
public class MyActivity extends AppCompatActivity {
    private MediaConverter converter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        converter = new MediaConverter();
        converter.setListener(new MediaConverter.OnConversionListener() {
            @Override
            public void onConversionStarted() {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onConversionProgress(long timeMs) {
                statusText.setText("Converting: " + 
                    CommonUtils.formatMillis(timeMs));
            }
            
            @Override
            public void onConversionCompleted(String outputPath) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Done: " + outputPath, 
                    Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onConversionError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                AppLogger.showToast("Error: " + errorMessage);
            }
        });
    }
    
    private void convertVideo(String inputPath) {
        String outputPath = FileUtils.getOutputFilePath(
            this, "output", "mp3");
        
        converter.convertMediaWithBitrate(
            inputPath,
            outputPath,
            "mp3",
            192
        );
    }
}
```

### Example 2: Trim Media
```java
private void trimAudio(String audioPath, long startMs, long endMs) {
    MediaTrimmer trimmer = new MediaTrimmer();
    trimmer.setListener(new MediaTrimmer.OnTrimListener() {
        @Override
        public void onTrimStarted() {
            AppLogger.showToast("Trimming started...");
        }
        
        @Override
        public void onTrimProgress(long timeMs) {}
        
        @Override
        public void onTrimCompleted(String outputPath) {
            AppLogger.showLongToast("Trimmed: " + outputPath);
        }
        
        @Override
        public void onTrimError(String errorMessage) {
            AppLogger.showToast("Trim error: " + errorMessage);
        }
    });
    
    // Create trim info
    MediaTrimInfo trimInfo = new MediaTrimInfo();
    trimInfo.setInputPath(audioPath);
    trimInfo.setOutputPath(getOutputDir() + "/trimmed.mp3");
    trimInfo.setStartTimeMs(startMs);
    trimInfo.setEndTimeMs(endMs);
    
    // Validate
    if (trimInfo.isValid()) {
        trimmer.trimAndConvert(trimInfo, "mp3", 256);
    }
}
```

### Example 3: Work with URIs
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == FILE_REQUEST && resultCode == RESULT_OK) {
        Uri fileUri = data.getData();
        
        // Get file path from URI
        String filePath = FileUtils.getPath(this, fileUri);
        String fileName = FileUtils.getFileName(this, fileUri);
        
        // Get media duration
        MediaTrimmer trimmer = new MediaTrimmer();
        long durationMs = trimmer.getMediaDuration(filePath);
        
        // Use for further processing
        String formattedDuration = CommonUtils.formatMillis(durationMs);
        statusText.setText("Duration: " + formattedDuration);
    }
}
```

---

## Error Handling

### Common Errors and Solutions

#### ❌ "Invalid input parameters"
```java
// Check this before calling
if (inputPath != null && new File(inputPath).exists() &&
    outputPath != null && !outputPath.isEmpty() &&
    !format.isEmpty()) {
    converter.convertMedia(...);
}
```

#### ❌ "Could not determine media duration"
```java
// Ensure media file is readable
MediaTrimmer trimmer = new MediaTrimmer();
long duration = trimmer.getMediaDuration(path);
if (duration <= 0) {
    AppLogger.e("File not readable or invalid format");
}
```

#### ❌ "End time exceeds media duration"
```java
// Validate time range before trim
long duration = trimmer.getMediaDuration(mediaPath);
if (endTimeMs > duration) {
    AppLogger.showToast("End time exceeds file duration");
    return;
}
```

---

## Thread Safety

All heavy operations run on separate threads:
```java
// These run async - don't block UI
converter.convertMedia(...);
trimmer.trimMedia(...);

// UI updates must happen on main thread
runOnUiThread(() -> {
    progressBar.setVisibility(View.GONE);
});
```

---

## Best Practices

1. **Always set listeners** before calling operations
2. **Validate file paths** before operations
3. **Check permissions** at runtime (API 23+)
4. **Handle errors gracefully** - show user-friendly messages
5. **Use AppConfig** for constants, don't hardcode
6. **Format times** using CommonUtils for consistency
7. **Log important events** using AppLogger
8. **Destroy resources** when activity finishes

---

**Last Updated**: 2026-04-29  
**API Version**: 2.0
