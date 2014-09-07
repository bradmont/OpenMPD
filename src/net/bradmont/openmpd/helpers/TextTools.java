package net.bradmont.openmpd.helpers;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.bradmont.openmpd.R;
import net.bradmont.openmpd.OpenMPD;

public class TextTools{



    
    private static String mThisMonth = null;
    public static String getThisMonth(){
        if (mThisMonth == null){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
            Calendar cal = Calendar.getInstance();
            mThisMonth = dateFormat.format(cal.getTime());
        }
        return mThisMonth;
    }

    public static String getToday(){
        if (mThisMonth == null){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            mThisMonth = dateFormat.format(cal.getTime());
        }
        return mThisMonth;
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
                return (new SimpleDateFormat("MMMM dd")).format(date.getTime());
            }
        } else {
            return (new SimpleDateFormat("MMMM dd, YYYY")).format(date.getTime());
        }
    }

    private static Calendar mkCalendar(String date_string){
        Calendar date = Calendar.getInstance();
        String [] parts = date_string.split("-");
        date.set(Calendar.YEAR, Integer.parseInt(parts[0]));
        date.set(Calendar.MONTH, Integer.parseInt(parts[1]) -1 );
        date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        return date;
    }

}
