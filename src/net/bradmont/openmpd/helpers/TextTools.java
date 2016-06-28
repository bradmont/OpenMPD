package net.bradmont.openmpd.helpers;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.lang.StringBuffer;
import java.util.Date;
import java.util.Calendar;
import java.util.Vector;

import net.bradmont.openmpd.R;
import net.bradmont.openmpd.OpenMPD;

public class TextTools{



    
    private static String mThisYearMonth = null;
    private static String mThisMonth = null;
    private static String mThisYear = null;
    private static String mToday = null;
    public static String getThisYearMonth(){
        if (mThisYearMonth == null){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
            Calendar cal = Calendar.getInstance();
            mThisYearMonth = dateFormat.format(cal.getTime());
        }
        return mThisYearMonth;
    }
    public static String getThisMonth(){
        if (mThisMonth == null){
            DateFormat dateFormat = new SimpleDateFormat("MM");
            Calendar cal = Calendar.getInstance();
            mThisMonth = dateFormat.format(cal.getTime());
        }
        return mThisMonth;
    }
    public static String getThisYear(){
        if (mThisYear == null){
            DateFormat dateFormat = new SimpleDateFormat("yyyy");
            Calendar cal = Calendar.getInstance();
            mThisYear = dateFormat.format(cal.getTime());
        }
        return mThisYear;
    }

    public static String getToday(){
        if (mToday == null){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            mToday = dateFormat.format(cal.getTime());
        }
        return mToday;
    }

    public static String prettyDate(String date_string){
        Calendar date = mkCalendar(date_string);
        Calendar today = Calendar.getInstance();
        if (date_string.equals(getToday())){
            return OpenMPD.get().getResources().getString(R.string.today);
        } else if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
            if (date.get(Calendar.DAY_OF_YEAR) + 1 == today.get(Calendar.DAY_OF_YEAR)){
                return OpenMPD.get().getResources().getString(R.string.yesterday);
            } else if (date.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)){
                return (new SimpleDateFormat("EEEE")).format(date.getTime());
            } else {
                return (new SimpleDateFormat("MMMM d")).format(date.getTime());
            }
        } else {
            return (new SimpleDateFormat("MMMM d, yyyy")).format(date.getTime());
        }
    }
    public static String prettyShortDate(String date_string){
        Calendar date = mkCalendar(date_string);
        Calendar today = Calendar.getInstance();
        if (date_string.equals(getToday())){
            return OpenMPD.get().getResources().getString(R.string.today);
        } else if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
            if (date.get(Calendar.DAY_OF_YEAR) + 1 == today.get(Calendar.DAY_OF_YEAR)){
                return OpenMPD.get().getResources().getString(R.string.yesterday);
            } else if (date.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)){
                return (new SimpleDateFormat("EEEE")).format(date.getTime());
            } else {
                return (new SimpleDateFormat("d MMM")).format(date.getTime());
            }
        } else {
            return (new SimpleDateFormat("MMM d, yyyy")).format(date.getTime());
        }
    }

    public static Calendar mkCalendar(String date_string){
        Calendar date = Calendar.getInstance();
        String [] parts = date_string.split("-");
        date.set(Calendar.YEAR, Integer.parseInt(parts[0]));
        date.set(Calendar.MONTH, Integer.parseInt(parts[1]) -1 );
        date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        date.set(Calendar.HOUR,0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.SECOND,0);
        return date;
    }

    public static Date mkDate(String date_string){
        return mkCalendar(date_string).getTime();
    }

    /**  Split a line of CSV data into a string array
      */
    public static String [] csvLineSplit(String line){
        Vector<String> result = new Vector<String>();
        StringBuffer element = new StringBuffer();

        if (line==null){ return null;}

        boolean inQuotes=false;
        for (int i=0; i < line.length(); i++){
            char ch = line.charAt(i);
            if (ch == ','){
                if (inQuotes){
                    element.append(ch);
                } else {
                    result.add(element.toString());
                    element = new StringBuffer();
                }
            } else if (ch == '\"'){
                inQuotes = inQuotes?false:true;
            } else {
                element.append(ch);
            }
        }
        result.add(element.toString());
        String [] return_value = new String[result.size()];
        return_value = result.toArray(return_value);
        return return_value;
    }

    /** Takes an ISO 8601 date string and converts it to a MM/DD/YYYY string,
     * as required by TntDataServer.
     */
    public static String stupidDateFormat(String date){
        String [] parts = date.split("-");
        return String.format("%s/%s/%s", parts[1], parts[2], parts[0]);
    }

    /** Takes an MM/DD/YYYY date (from TntDataServer) and returns an 
     * ISO 8601 date string and converts it to a MM/DD/YYYY string.
     */
    public static String fixDate(String date){
        String [] parts = date.split("/");
        return String.format("%s-%s-%s", parts[2], parts[0], parts[1]);
    }

}
