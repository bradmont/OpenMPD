package net.bradmont.openmpd.helpers;

import net.bradmont.openmpd.models.LogItem;
import java.lang.System;
import net.bradmont.openmpd.BuildConfig;

/**
 * Android Log wrapper class that can use {@link String#format(String, Object...)} in logging message
 */
public class Log {

    private static long mLastTimeStamp = 0;
    private static boolean DEBUGGING = false;


    public static int     d(String tag, String msg) {
        saveLog(tag, msg);
        return android.util.Log.d(tag, msg);
    }

    public static int  d(String tag, String msg, Throwable tr){
        saveLog(tag, msg, tr);
        return android.util.Log.d(tag, msg, tr);

    }

    public static int  e(String tag, String msg){
        saveLog(tag, msg);
        return android.util.Log.e(tag, msg);

    }

    public static int  e(String tag, String msg, Throwable tr){
        saveLog(tag, msg, tr);
        return android.util.Log.e(tag, msg, tr);

    }

    public static String   getStackTraceString(Throwable tr){
        return android.util.Log.getStackTraceString(tr);

    }

    public static int  i(String tag, String msg){
        saveLog(tag, msg);
        return android.util.Log.i(tag, msg);

    }

    public static int  i(String tag, String msg, Throwable tr){
        saveLog(tag, msg, tr);
        return android.util.Log.i(tag, msg, tr);

    }

    public static boolean  isLoggable(String tag, int level){
        return android.util.Log.isLoggable(tag, level);
    }

    public static int  println(int priority, String tag, String msg){
        saveLog(tag, msg);
        return android.util.Log.println(priority, tag, msg);

    }

    public static int  v(String tag, String msg, Throwable tr){
        saveLog(tag, msg, tr);
        return android.util.Log.v(tag, msg, tr);

    }

    public static int  v(String tag, String msg){
        saveLog(tag, msg);
        return android.util.Log.v(tag, msg);

    }

    public static int  w(String tag, Throwable tr){
        saveLog(tag, tr);
        return android.util.Log.w(tag, tr);

    }

    public static int  w(String tag, String msg, Throwable tr){
        saveLog(tag, msg, tr);
        return android.util.Log.w(tag, msg, tr);

    }

    public static int  w(String tag, String msg){
        saveLog(tag, msg);
        return android.util.Log.w(tag, msg);

    }

    public static int  wtf(String tag, Throwable tr){
        saveLog(tag, tr);
        return android.util.Log.wtf(tag, tr);

    }

    public static int  wtf(String tag, String msg, Throwable tr){
        saveLog(tag, msg, tr);
        return android.util.Log.wtf(tag, msg, tr);

    }

    public static int  wtf(String tag, String msg){
        saveLog(tag, msg);
        return android.util.Log.wtf(tag, msg);

    }

    private static void saveLog(String tag, Throwable tr){
        saveLog(tag, getStackTraceString(tr));
    }
    private static void saveLog(String tag, String msg, Throwable tr){
        saveLog(tag, msg);
        saveLog(tag, getStackTraceString(tr));
    }
    private static void saveLog(String tag, String msg){
        if (DEBUGGING == true && BuildConfig.DEBUG == true){
            LogItem.logError(tag, msg);
        }
    }



    // more efficient timestamp
    public static String makeTimestamp() {
        return formatTime(System.currentTimeMillis(), 7);
    }

    public static String formatTime(long millis, int fractionDigits) {
        int integerDigits = (int) Math.log10(millis / 1000.0) + 1;

        char[] chars = new char[integerDigits + fractionDigits + 1];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = '0';
        }

        millis *= Math.pow(10, fractionDigits - 3);
        for (int i = chars.length - 1; i >= 0; i--) {
            if (i == integerDigits) {
                chars[i] = '.';
                i--;
            }

            chars[i] = (char) (millis % 10);
            chars[i] += '0';

            millis /= 10;
        }

        return new String(chars);
    }

}
