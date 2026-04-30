package app.hill.converter01.utils;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;

/**
 * Utility class for logging and user feedback
 */
public class AppLogger {
    private static final String TAG = "MediaConverter";
    private static Context context;
    
    public static void init(Context ctx) {
        context = ctx;
    }
    
    public static void d(String message) {
        Log.d(TAG, message);
    }
    
    public static void e(String message, Exception ex) {
        Log.e(TAG, message, ex);
    }
    
    public static void e(String message) {
        Log.e(TAG, message);
    }
    
    public static void i(String message) {
        Log.i(TAG, message);
    }
    
    public static void showToast(String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    public static void showLongToast(String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
