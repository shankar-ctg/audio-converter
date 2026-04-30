package app.hill.converter01.config;

/**
 * Application configuration constants
 */
public class AppConfig {
    
    // Supported audio formats
    public static final String[] SUPPORTED_FORMATS = {
        "mp3",      // MPEG-3
        "aac",      // Advanced Audio Coding
        "wav",      // Waveform Audio
        "ogg",      // Ogg Vorbis
        "flac",     // Free Lossless Audio Codec
        "m4a"       // MPEG-4 Audio
    };
    
    // Audio quality settings (bitrate in kbps)
    public static final int[] QUALITY_BITRATES = {
        128,    // Low quality
        192,    // Medium quality
        256,    // High quality
        320     // Very high quality
    };
    
    // Default settings
    public static final String DEFAULT_FORMAT = "mp3";
    public static final int DEFAULT_BITRATE = 192;
    
    // Output directory
    public static final String OUTPUT_DIR_NAME = "MediaConverted";
}
