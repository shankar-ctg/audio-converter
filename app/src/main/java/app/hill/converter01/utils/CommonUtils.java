package app.hill.converter01.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for common operations
 */
public class CommonUtils {
    
    /**
     * Format milliseconds to HH:MM:SS format
     */
    public static String formatMillis(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60));
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Convert time string (MM:SS or HH:MM:SS) to milliseconds
     */
    public static long parseTimeToMillis(String timeStr) {
        String[] parts = timeStr.split(":");
        long millis = 0;
        
        try {
            if (parts.length == 2) {
                // MM:SS format
                millis = Long.parseLong(parts[0]) * 60000 + Long.parseLong(parts[1]) * 1000;
            } else if (parts.length == 3) {
                // HH:MM:SS format
                millis = Long.parseLong(parts[0]) * 3600000 + 
                        Long.parseLong(parts[1]) * 60000 + 
                        Long.parseLong(parts[2]) * 1000;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        
        return millis;
    }
    
    /**
     * Get formatted timestamp
     */
    public static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    }
    
    /**
     * Calculate percentage
     */
    public static int calculatePercentage(long current, long total) {
        if (total <= 0) return 0;
        return (int) ((current * 100) / total);
    }
    
    /**
     * Format file size to human readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        
        return String.format("%.1f %s", 
            bytes / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }
}
