package app.hill.converter01.media;

/**
 * Model class for storing media trimming information
 */
public class MediaTrimInfo {
    private long startTimeMs;      // Start time in milliseconds
    private long endTimeMs;        // End time in milliseconds
    private long totalDurationMs;  // Total duration in milliseconds
    private String inputPath;      // Full path to input file
    private String outputPath;     // Full path to output file
    
    public MediaTrimInfo() {
        this.startTimeMs = 0;
        this.endTimeMs = 0;
        this.totalDurationMs = 0;
        this.inputPath = "";
        this.outputPath = "";
    }
    
    public MediaTrimInfo(long startTimeMs, long endTimeMs, String inputPath, String outputPath) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }
    
    // Getters
    public long getStartTimeMs() {
        return startTimeMs;
    }
    
    public long getEndTimeMs() {
        return endTimeMs;
    }
    
    public long getTotalDurationMs() {
        return totalDurationMs;
    }
    
    public String getInputPath() {
        return inputPath;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    // Setters
    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }
    
    public void setEndTimeMs(long endTimeMs) {
        this.endTimeMs = endTimeMs;
    }
    
    public void setTotalDurationMs(long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
    }
    
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    // Utility methods
    public long getDurationMs() {
        return endTimeMs - startTimeMs;
    }
    
    public String getFormattedStartTime() {
        return formatMillis(startTimeMs);
    }
    
    public String getFormattedEndTime() {
        return formatMillis(endTimeMs);
    }
    
    public String getFormattedDuration() {
        return formatMillis(getDurationMs());
    }
    
    private String formatMillis(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60));
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    public boolean isValid() {
        return startTimeMs >= 0 && 
               endTimeMs > startTimeMs && 
               !inputPath.isEmpty() && 
               !outputPath.isEmpty();
    }
    
    @Override
    public String toString() {
        return "MediaTrimInfo{" +
                "startTime=" + getFormattedStartTime() +
                ", endTime=" + getFormattedEndTime() +
                ", duration=" + getFormattedDuration() +
                ", inputPath='" + inputPath + '\'' +
                ", outputPath='" + outputPath + '\'' +
                '}';
    }
}
