# 🚀 Quick Start Guide - MediaConverter

## Installation

### 1. Update Project Structure ✅
New modular structure is already in place:
```
app/src/main/java/app/hill/converter01/
├── config/      (AppConfig.java)
├── media/       (MediaConverter, MediaTrimmer, MediaTrimInfo)
├── ui/          (MainActivity, MediaTrimmerActivity)
└── utils/       (FileUtils, CommonUtils, AppLogger)
```

### 2. Update Dependencies
Add to `app/build.gradle`:
```gradle
dependencies {
    // ... existing dependencies
    
    // FFmpeg for media conversion
    implementation 'com.arthenica:ffmpeg-kit-android:6.0'
    
    // Permission handling
    implementation 'com.karumi:dexter:6.2.3'
    
    // Material Design
    implementation 'com.google.android.material:material:1.9.0'
}
```

### 3. Update AndroidManifest.xml ✅
Already updated with:
```xml
<activity android:name=".ui.MainActivity" />
<activity android:name=".ui.MediaTrimmerActivity" />
```

### 4. Sync & Build
```
- File → Sync Now
- Build → Rebuild Project
```

---

## First Run

### 1. Launch App
The app will automatically request permissions for:
- READ_EXTERNAL_STORAGE
- WRITE_EXTERNAL_STORAGE
- READ_MEDIA_VIDEO
- READ_MEDIA_AUDIO

**Grant all permissions for full functionality.**

### 2. Select Media
- Tap **"Select Video File"** button
- Choose an audio or video file from your device
- The file name will display below the button

### 3. Convert Media
```
Flow:
1. Select media file
2. Choose output format (MP3, AAC, WAV, etc.)
3. Tap "Convert to Audio"
4. Wait for completion
5. File saved to: Music/MediaConverted/
```

### 4. Trim Media (New!)
```
Flow:
1. Select media file (must do this first)
2. Tap "Trim & Convert" button
3. Use seekbars to set start and end times
4. Choose output format
5. Tap "Trim & Convert"
6. Done! File in: Music/MediaConverted/
```

---

## Output Locations

Converted files are saved in:
```
/Music/MediaConverted/
```

File naming pattern:
```
{original_name}_{timestamp}.{format}

Example: video_20260429_143025.mp3
```

---

## Format Selection

### For Podcasts/Speech
- **MP3** (most compatible)
- **AAC** (modern, smaller)

### For Music
- **MP3** (universal)
- **FLAC** (lossless, large files)
- **WAV** (professional editing)

### For Streaming
- **AAC** (best quality/size ratio)
- **OGG** (open format)

### For Apple/iTunes
- **M4A** (native support)
- **AAC** (compatible)

---

## Quality Settings

Bitrates are automatically applied:
- **128 kbps** - Acceptable (speech, podcasts)
- **192 kbps** - Good (default, streaming)
- **256 kbps** - Better (music, detailed audio)
- **320 kbps** - Best (high-quality music)

To change default bitrate, edit `AppConfig.java`:
```java
public static final int DEFAULT_BITRATE = 192;  // Change to 256 or 320
```

---

## Troubleshooting

### ❌ App Crashes on Start
**Solution**: Check AndroidManifest.xml has correct activity names:
```xml
<activity android:name=".ui.MainActivity" />
<activity android:name=".ui.MediaTrimmerActivity" />
```

### ❌ "Permission Denied"
**Solution**: 
- Grant permissions when prompted
- For Android 6+, permissions are requested at runtime
- Check System Settings → Apps → Permissions

### ❌ "File Not Found"
**Solution**:
- Ensure file exists before selection
- Try selecting a different file
- Check file format is supported

### ❌ "Conversion Failed"
**Solution**:
- Try with a different media file
- Check if file is corrupted
- Try a different output format
- Check if output directory exists

### ❌ "Trim Times Invalid"
**Solution**:
- End time must be after start time
- End time must not exceed file duration
- Times are in MM:SS or HH:MM:SS format

---

## Common Tasks

### Convert Multiple Files
Repeat for each file:
1. Select media
2. Choose format  
3. Convert
4. Select next media
5. Repeat

### Extract Audio from Video
1. Select video file
2. Choose MP3 format
3. Tap "Convert to Audio"
4. Audio extracted and saved

### Cut Audio Clip
1. Select audio file
2. Tap "Trim & Convert"
3. Set start time (where to cut from)
4. Set end time (where to cut to)
5. Tap "Trim & Convert"
6. Only that portion extracted

### Split Long Audio
To split a 1-hour audio into 3 parts:
1. **Part 1**: Start: 00:00, End: 20:00
2. **Part 2**: Start: 20:00, End: 40:00  
3. **Part 3**: Start: 40:00, End: 60:00

Trim each with these ranges and convert.

### Reduce File Size
1. Select media
2. Choose format (AAC or OGG)
3. Convert (bitrate set to 128-192 kbps)
4. Smaller file in output directory

---

## Settings

### Change Output Directory
Edit `AppConfig.java`:
```java
public static final String OUTPUT_DIR_NAME = "MyAudio";  // Changes to Music/MyAudio/
```

### Change Default Format
Edit `AppConfig.java`:
```java
public static final String DEFAULT_FORMAT = "aac";  // Was "mp3"
```

### Add New Format
Edit `AppConfig.java`:
```java
public static final String[] SUPPORTED_FORMATS = {
    "mp3", "aac", "wav", "ogg", "flac", "m4a", "m4b"  // Added m4b
};
```

---

## Tips & Tricks

💡 **Fastest Conversion**
- Use MP3 format (smallest output, fast)
- Use 128 kbps bitrate (lower quality = faster)

💡 **Best Quality**
- Use FLAC format (lossless)
- Use 320 kbps bitrate (maximum quality)

💡 **Smallest File Size**
- Use OGG format with 128 kbps
- Use AAC with 192 kbps (good balance)

💡 **Quick Edit**
- Trim first, then convert
- Avoids processing entire file

💡 **Batch Processing**
- Write a simple loop to convert multiple files
- Can create batch conversion feature (future update)

---

## File Types Supported

### Input
- Video: MP4, MKV, AVI, MOV, FLV, WMV
- Audio: MP3, WAV, AAC, OGG, FLAC, M4A

### Output
- MP3 (MPEG-3)
- AAC (Advanced Audio Codec)
- WAV (Waveform Audio Format)
- OGG (Ogg Vorbis)
- FLAC (Free Lossless Audio Codec)
- M4A (MPEG-4 Audio)

---

## Performance Notes

### Conversion Time (Rough Estimates)
- **Short clip** (30 seconds): 5-10 seconds
- **Normal song** (3-4 minutes): 15-30 seconds
- **Long file** (1 hour): 5-10 minutes

**Note**: Times vary based on:
- Device processing power
- Input format complexity
- Output bitrate selected
- Device storage speed

### File Sizes
**MP3 at different bitrates** (per minute of audio):
- 128 kbps: ~1 MB
- 192 kbps: ~1.5 MB
- 256 kbps: ~2 MB
- 320 kbps: ~2.4 MB

---

## Getting Help

### Check Logs
Open `Logcat` in Android Studio:
- Run → Edit Configurations
- Look for "MediaConverter" tag
- Check error messages

### Common Log Messages
```
✓ D/MediaConverter: Executing FFmpeg: -i "/path/file" ...
✓ D/MediaConverter: Conversion successful
✗ E/MediaConverter: Conversion failed with code: 1
✗ E/MediaConverter: Error getting media duration
```

---

## What's New (v2.0)

✅ Modular architecture  
✅ Media trimming feature  
✅ Enhanced error handling  
✅ Better logging system  
✅ Improved UI with trim activity  
✅ Listener-based callbacks  
✅ Centralized configuration  

---

**Happy Converting! 🎵**

For advanced usage, see [API_REFERENCE.md](API_REFERENCE.md)  
For full documentation, see [README_RESTRUCTURING.md](README_RESTRUCTURING.md)
