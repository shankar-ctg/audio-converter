package app.hill.converter01.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * Utility class for file operations
 */
public class FileUtils {
    
    /**
     * Get real file path from URI (supports both content:// and file:// schemes)
     * @param context Application context
     * @param uri URI to convert
     * @return Real file path or null if not found
     */
    public static String getPath(Context context, Uri uri) {
        // For file:// scheme
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        
        // For content:// scheme
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Try MediaStore first
            String path = getDataColumn(context, uri, null, null);
            if (path != null) {
                return path;
            }
            
            // Try DocumentsProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                    return getDataColumn(context, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get display name from URI
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    
    /**
     * Get data column from content resolver
     */
    private static String getDataColumn(Context context, Uri uri, String selection, 
                                        String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, 
                selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
    
    /**
     * Check if URI is external storage document
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    
    /**
     * Check if URI is downloads document
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    
    /**
     * Check if URI is media document
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    
    /**
     * Get output file path with timestamp
     */
    public static String getOutputFilePath(Context context, String fileName, String extension) {
        String outputDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS).toString();
        
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        long timestamp = System.currentTimeMillis();
        
        return outputDir + "/" + nameWithoutExt + "_" + timestamp + "." + extension;
    }
    
    /**
     * Create output directory if not exists
     */
    public static boolean createOutputDirectory(String dirPath) {
        java.io.File dir = new java.io.File(dirPath);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }
}
