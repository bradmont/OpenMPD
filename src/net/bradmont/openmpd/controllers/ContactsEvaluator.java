package net.bradmont.openmpd.controllers;

import net.bradmont.openmpd.activities.ImportActivity;
import net.bradmont.openmpd.dao.*;
import net.bradmont.openmpd.helpers.*;
import net.bradmont.openmpd.*;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.message.*;

import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.ProgressBar;

import android.support.v4.app.NotificationCompat;


import java.lang.Runnable;
import java.lang.Thread;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;

public class ContactsEvaluator implements Runnable{

    private NotificationCompat.Builder builder = null;
    private NotificationManager notifyManager = null;
    private Context context;
    private ArrayList<Boolean> initialImport = null; 
    private ArrayList<Long> newdata = null; 

    public final static int NOTIFICATION_ID = 1;
    private final static String CONTACT_MONTHLY_GIFT_SQL = 
        "select "+
            "months.month, "+
            "?*(2016-substr(months.month, 1, 4)) + (?-substr(months.month, 6,  2)) as months_ago,  "+
            "count(gift.month)  "+
            "from months left outer join (select * from gift where contact_id=?) as gift on months.month = gift.month "+
            "group by  months.month  "+
            "order by months.month desc;";
            // ?, ?, ? == this year, this month, contact_id


    public ContactsEvaluator(Context context, NotificationCompat.Builder builder){
        this.builder = builder;
        this.context = context;
    }

    public ContactsEvaluator(Context context, NotificationCompat.Builder builder, ArrayList<Long> newdata, ArrayList<Boolean> initialImport){
        this(context, builder);
        this.initialImport = initialImport;
        this.newdata = newdata;
    }

    public void run(){
        ArrayList<List<Contact>> contact_lists = new ArrayList<List<Contact>>();
        int total_contacts = 0;
        ContactDao contactDao = OpenMPD.getDaoSession().getContactDao();
        ContactStatusDao statusDao = OpenMPD.getDaoSession().getContactStatusDao();
        if (newdata == null){
            contact_lists.add( contactDao.loadAll());
            total_contacts = contact_lists.get(0).size();
            initialImport = new ArrayList<Boolean>(1);
            initialImport.add(true);
        } else {
            for (int i = 0; i < newdata.size(); i++){
                ServiceAccount account = OpenMPD.getDaoSession().getServiceAccountDao()
                    .load(newdata.get(i));
                contact_lists.add(account.getContacts());
                total_contacts += contact_lists.get(i).size();
            }
        }

        notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int progress = 0;
        ArrayList<ContactStatus> statuses = new ArrayList<ContactStatus>(total_contacts);

        Log.i("net.bradmont.openmpd", "evaluating " + total_contacts + " contacts");
        for (int j = 0; j < contact_lists.size(); j++){
            List<Contact> contacts = contact_lists.get(j);
            for (int i=0; i < contacts.size(); i++){
                try {
                    statuses.add(
                            evaluateContact(
                                contacts.get(i), 
                                initialImport.get(j).booleanValue()));
                } catch (Exception e){
                    String contact_id = "Contact index " + i + " of " + contacts.size() + ", Contact_id: " + contacts.get(i).getId();
                    Log.i("Error evaluating contact", contact_id, e);
                }
                progress++;
                if (progress % 1000 == 0){
                    statusDao.insertOrReplaceInTx(statuses, false);
                    statuses.clear();
                }
                if (builder != null){
                    builder.setProgress(total_contacts, progress, false);
                    notifyManager.notify(NOTIFICATION_ID, builder.build());
                }
                ImportActivity.setProgress(-1, total_contacts, progress, false);
            }
        }
        statusDao.insertOrReplaceInTx(statuses, false);
        statuses.clear();
    }

    private ContactStatus evaluateContact(Contact contact, boolean initialImport){
        HashMap<String, Integer> map = analyseContact(contact);
        ContactStatus status = null;
        status = contact.getStatus();// should only be one...
        boolean giving = true;
        if (status == null){
            status = new ContactStatus();
            status.setContact(contact);
        }

        // None if : contains "nogifts"
        if (map.containsKey("nogifts")){
            status.setType("none");
            status.setStatus("none");
            giving = false;
        }
        // Onetime if: totalGifts1
        else if (map.containsKey("totalGifts") &&
                map.get("totalGifts") == 1){
            status.setType("onetime");
            status.setStatus("none");
        }
        // Monthly if: any of give5, recentGifts4, recentGifts3
        else if ((map.containsKey("give") && map.get("give") >= 5)
                || (map.containsKey("recentGifts")  && map.get("recentGifts") >= 3)){
            status.setType("monthly");
            status.setStatus("current");
            status.setGivingFrequency(1);
        }
        // annual if : 1in12months && 2in24months
        else if (map.containsKey("12months") && map.get("12months") == 1
                && map.containsKey("24months") && map.get("24months") == 2){
            status.setType("annual");
            status.setStatus("current");
            status.setGivingFrequency(12);
        }
        // annual late if : 0 in 12months 1 in 24months & 2 in 36months
        else if (map.containsKey("12months") && map.get("12months") == 0
                && map.containsKey("24months") && map.get("24months") == 1
                && map.containsKey("36months") && map.get("36months") == 2){
            status.setType("annual");
            status.setStatus("late"); 
            status.setGivingFrequency(12);
        }
        // semiannual if : 12months : 2 && (24months : 3 || 24months : 4)
        else if (map.containsKey("12months") && map.get("12months") == 2
                && map.containsKey("24months") && 
                (map.get("24months") == 3 || map.get("24months") == 4)){
            status.setType("regular");
            status.setStatus("current");
            status.setGivingFrequency(6);
        } 
        // quarterly if : 4in12months && maxGiftStreak == 1
        else if (map.containsKey("12months") && map.get("12months") == 4
                && map.containsKey("maxGiftStreak") && map.get("maxGiftStreak") == 1){
            status.setType("regular");
            status.setStatus("current");
            status.setGivingFrequency(3);
        }
        // monthly dropped if: miss5, maxGiftStreak>2
        else if (map.containsKey("miss") && map.get("miss") == 5
                && map.containsKey("maxGiftStreak") && map.get("maxGiftStreak") > 2){
            status.setType("monthly");
            status.setStatus("dropped");
            status.setGivingFrequency(1);
        }
        // monthly late if   : miss2 || miss3 & maxGiftStreak > 2
        else if (map.containsKey("miss") && (map.get("miss") > 1 && map.get("miss") < 5)
                && map.containsKey("maxGiftStreak") && map.get("maxGiftStreak") > 2){
            status.setType("monthly");
            status.setStatus("late");
            status.setGivingFrequency(1);
        } 
        // frequent if 12months > 3 ###
        else if (map.containsKey("12months") && map.get("12months") > 3){
            status.setType("frequent");
            status.setStatus("unknown");
            status.setGivingFrequency(map.get("avgGiftSpace"));
        }
        else {
            status.setType("unknown");
            status.setStatus("unknown");
            status.setGivingFrequency(map.get("avgGiftSpace"));
            giving = false;
        }
        // otherwise: tokenize and try bayes? TODO
        
        if (giving == true){
            status.setGivingAmount(calcGivingAmount(contact));
            status.setLastGift(calcLastGift(contact));
        }
        return status;
    }

    /* returns the most common amount, in the lasts five gifts or the
     * average of the last five gifts.
     */
    private long calcGivingAmount (Contact contact){
        List<Gift> gifts = contact.getOrderedGifts();
        HashMap<Long, Integer> map = new HashMap<Long, Integer>();

        for (int i = 0; i < gifts.size() && i < 5; i++){
            if (map.containsKey(gifts.get(i).getAmount())){
                map.put(gifts.get(i).getAmount(), 
                        map.get(gifts.get(i).getAmount()) +1);
            } else {
                map.put(gifts.get(i).getAmount(), 1);
            }
        }
        Long mostCommon = 0L;
        int keyCount = 0;
        Long totalGiving = 0L;
        int totalGifts = 0;
        for (Long key : map.keySet()){
            if (map.get(key) > keyCount){
                keyCount = map.get(key);
                mostCommon = key;
                totalGifts += keyCount;
                totalGiving += (key.intValue() * map.get(key).intValue());
            }
        }

        if (keyCount > 1){
            return mostCommon;
        }
        // otherwise return an average
        return totalGiving / totalGifts;

    }
    private String calcLastGift(Contact contact){
        List<Gift> gifts = contact.getOrderedGifts();
        if (gifts.size() == 0) return null;
        return (gifts.get(0).getDate());
    }

    /**
     * Look at giving history and create a series of tags to describe it.
     * Tags: give5, give10, give15, ... : gifts the last N months
     *       miss5, miss10, miss15, .. : no gifts in last n months
     *       recentGifts# -- the last # months have had gifts
     *       recentMisses# -- the last # months have had no gifts
     *       NinMmonths -- N gifts in M months (M is multiple of 12)
     *       totalGifts# -- # total gifts
     *       avgGiftSpace# -- # average months between gifts
     */
    private HashMap<String, Integer> analyseContact(Contact contact){
        HashMap<String, Integer> tokens = new HashMap<String, Integer>();
        if (contact.getGifts().size() == 0){
            tokens.put("nogifts", 1);
            return tokens;
        }

        Cursor cur = OpenMPD.getDB().rawQuery(
                CONTACT_MONTHLY_GIFT_SQL, 
                new String [] { TextTools.getThisYear(), TextTools.getThisMonth(), Long.toString(contact.getId())} );


        int totalGifts = 0;
        int firstGift = 0;
        int recentGifts = 0;
        boolean recentGiftsDone= false;
        int recentMisses = 0;
        boolean recentMissesDone= false;
        int avgGiftSpace = 0;

        int maxGiftStreak = 0;
        int giftStreak = 0;
        int maxMissStreak = 0;
        int missStreak = 0;

        cur.moveToFirst();
        while (!cur.isAfterLast()){
            int month = cur.getInt(1); // months_ago
            int gift = cur.getInt(2); // gifts in that month
            if (gift == 0){
                if (cur.getPosition() > 0){ // skip this month as gift may not be in yet
                    recentGiftsDone = true;
                }
                if (!recentMissesDone){
                    recentMisses++;
                    if (recentMisses %5 == 0) tokens.put("miss", recentMisses);
                }
                missStreak++;
                giftStreak = 0;
                if (missStreak > maxMissStreak) maxMissStreak = missStreak;
            } else {
                firstGift = cur.getPosition();
                recentMissesDone = true;
                totalGifts += gift;
                if (!recentGiftsDone){
                    recentGifts++;
                    if (recentGifts %5 == 0) tokens.put("give", recentGifts);
                }
                giftStreak++;
                missStreak = 0;
                if (giftStreak > maxMissStreak) maxMissStreak = giftStreak;
            }
            if (cur.getPosition() %12 == 11){
                tokens.put((cur.getPosition()+1) + "months", totalGifts);
            }
            cur.moveToNext();
        }
        tokens.put("totalGifts" , totalGifts);
        tokens.put("recentGifts" , recentGifts);
        tokens.put("recentMisses" , (recentMisses-1));
        tokens.put("avgMonthsPerGift" , (firstGift/totalGifts));
        tokens.put("maxGiftStreak" , maxGiftStreak);
        tokens.put("maxMissStreak" , maxMissStreak);
        return tokens;

    }

}
