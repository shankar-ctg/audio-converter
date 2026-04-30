# MediaConverter - Project Structure

## 📁 Folder Organization

```
app/src/main/java/app/hill/converter01/
├── config/                    # Configuration & Constants
│   └── AppConfig.java        # App-wide constants (formats, bitrates, etc.)
│
├── media/                     # Media Operations Core
│   ├── MediaConverter.java    # Audio/Video conversion logic
│   ├── MediaTrimmer.java      # Media trimming/cutting functionality
│   └── MediaTrimInfo.java     # Model class for trim information
│
├── ui/                        # User Interface Activities
│   ├── MainActivity.java      # Main conversion activity
│   └── MediaTrimmerActivity.java  # Trimming/cutting activity
│
├── utils/                     # Utility Classes
│   ├── FileUtils.java         # File operations & path resolution
│   ├── CommonUtils.java       # Common utilities (time formatting, etc.)
│   └── AppLogger.java         # Logging & toast notifications
│
└── [Old Files - to be removed]
    ├── MainActivity.java      # (Original - to delete)
    └── FileUtils.java         # (Original - to delete)
```

## 🎯 Key Features

### 1. **Media Conversion** (`MediaConverter.java`)
- Convert videos to audio formats (MP3, AAC, WAV, OGG, FLAC, M4A)
- Adjustable bitrate settings
- Real-time progress tracking
- Error handling

### 2. **Media Trimming** (`MediaTrimmer.java`)
- Cut specific time ranges from media files
- Trim and convert in one operation
- Duration validation
- Precise time control

### 3. **Configuration** (`AppConfig.java`)
- Centralized format definitions
- Quality/bitrate presets
- Output directory settings

## 📋 Class Descriptions

### MediaConverter
```java
public void convertMedia(String inputPath, String outputPath, 
                        String outputFormat, int bitrate)
public void convertMediaWithBitrate(String inputPath, String outputPath,
                                   String outputFormat, int bitrate)
```

### MediaTrimmer
```java
public void trimMedia(MediaTrimInfo trimInfo)
public void trimAndConvert(MediaTrimInfo trimInfo, 
                          String outputFormat, int bitrate)
public long getMediaDuration(String mediaPath)
```

### MediaTrimInfo
Data model containing:
- `startTimeMs` - Start time in milliseconds
- `endTimeMs` - End time in milliseconds
- `inputPath` - Full path to input file
- `outputPath` - Full path to output file
- `totalDurationMs` - Total media duration

## 🚀 Usage Examples

### Basic Conversion
```java
MediaConverter converter = new MediaConverter();
converter.setListener(listener);
converter.convertMediaWithBitrate(
    "/path/to/video.mp4",
    "/path/to/output.mp3",
    "mp3",
    192  // bitrate in kbps
);
```

### Trimming Media
```java
MediaTrimmer trimmer = new MediaTrimmer();
trimmer.setListener(listener);

MediaTrimInfo trimInfo = new MediaTrimInfo();
trimInfo.setInputPath("/path/to/media.mp3");
trimInfo.setOutputPath("/path/to/trimmed.mp3");
trimInfo.setStartTimeMs(30000);  // 30 seconds
trimInfo.setEndTimeMs(120000);   // 2 minutes

trimmer.trimAndConvert(trimInfo, "mp3", 192);
```

## 📊 Supported Formats

- MP3 (MPEG-3)
- AAC (Advanced Audio Coding)
- WAV (Waveform Audio)
- OGG (Ogg Vorbis)
- FLAC (Free Lossless Audio Codec)
- M4A (MPEG-4 Audio)

## 🔧 Bitrate Presets

- 128 kbps - Low quality
- 192 kbps - Medium quality (default)
- 256 kbps - High quality
- 320 kbps - Very high quality

## 📝 Migration Steps

To migrate from old structure:

1. **Update Imports** in MainActivity:
   - Remove: `import app.hill.converter01.MainActivity;`
   - Add: `import app.hill.converter01.ui.MainActivity;`

2. **Update Imports** in FileUtils usage:
   - Old: `import app.hill.converter01.FileUtils;`
   - New: `import app.hill.converter01.utils.FileUtils;`

3. **Update AndroidManifest.xml**:
   ```xml
   <activity android:name=".ui.MainActivity" />
   <activity android:name=".ui.MediaTrimmerActivity" />
   ```

4. **Delete old files**:
   - `MainActivity.java` (old)
   - `FileUtils.java` (old)

## 🎨 Layout Files Required

Create new layout files:
- `activity_main.xml` (already exists)
- `activity_trimmer.xml` - New layout for MediaTrimmerActivity

## 📚 Dependencies

- FFmpegKit for media processing
- AndroidX AppCompat
- Dexter for permissions
- Karumi/Dexter for permission handling

## 🔐 Permissions Required

- `READ_EXTERNAL_STORAGE`
- `WRITE_EXTERNAL_STORAGE`
- `READ_MEDIA_VIDEO`
- `READ_MEDIA_AUDIO`

---

**Status**: ✅ Modular architecture implemented
**Last Updated**: 2026-04-29
