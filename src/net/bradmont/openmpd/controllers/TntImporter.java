package net.bradmont.openmpd.controllers;

import net.bradmont.openmpd.dao.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.helpers.TextTools;
import net.bradmont.supergreen.models.ModelList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.lang.StackTraceElement;
import java.lang.Thread;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.ProgressBar;
import net.bradmont.openmpd.helpers.Log;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;


import java.lang.StringBuffer;
import java.lang.Runnable;
import java.lang.RuntimeException;
import java.lang.Thread;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


import net.bradmont.openmpd.activities.ImportActivity;


/** Controller to connect to and import data from TntDataServer instances.
  *
  */
public class TntImporter {

    private ProgressBar progressbar=null;
    private NotificationCompat.Builder builder = null;
    private NotificationManager notifyManager = null;
    private Context context;
    private HashMap<String, String> mDataHash ;
    private HashMap<String, Long> mContactIdByTntId ;
        // Initialize this HashMap only once to save time

    private ServiceAccount mAccount;
    private TntService service = null;
    private int progress = 0;
    private int progressmax=0;
    private static int notification_id=ContactsEvaluator.NOTIFICATION_ID;

    public TntImporter(Context context, ServiceAccount account){
        this.context = context;
        this.mAccount = account;
        service = mAccount.getTntService();
    }
    public TntImporter(Context context, ServiceAccount account, ProgressBar progressbar){
        this.context = context;
        this.mAccount = account;
        service = account.getTntService();
        this.progressbar = progressbar;
    }

    public TntImporter(Context context, ServiceAccount account, NotificationCompat.Builder builder){
        this.context = context;
        this.mAccount = account;
        service = account.getTntService();
        this.builder = builder;
        notifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //notification_id = account.getId();

    }

    public boolean run() throws Exception{
        if (progressbar != null){
            progressbar.setIndeterminate(true);
        }
        if (builder != null){
            builder.setProgress(progressmax, progress, true);
            notifyManager.notify(notification_id, builder.build());
        }
        try {
            Thread.sleep(500);
            // we tend to get to this point before the accounts listview 
            // on the importing screen gets populated, so the progress bar
            // for the first account doesn't get set to indeterminate, 
            // leaving the user thinking the app has hung. Hackish workaround,
            // but it does the job.
        } catch(Exception ex) { }

        ImportActivity.setProgress(mAccount.getId().intValue(), progressmax, progress, true);

        // upgrade from legacy tnt_service to using query.ini properly
        if (service.getUsernameKey() == null || service.getUsernameKey().length() < 2){
            try {
                service.processQueryIni();
            } catch (TntService.ServerException e){
                Log.i("net.bradmont.openmpd", "ServerException from getAddresses()", e);
                return false;
            }
        }

        if (builder != null){
            builder.setContentText(mAccount.getName());
            notifyManager.notify(notification_id, builder.build());
        }
        if (getContacts() == false){
            return false;
        }
        mContactIdByTntId = MakeContactIdHash();
        if (progressbar != null){
            progressbar.setIndeterminate(false);
        }
        if (builder != null){
            builder.setProgress(progressmax, progress, false);
            notifyManager.notify(notification_id, builder.build());
        }
        ImportActivity.setProgress(mAccount.getId().intValue(), progressmax, progress, false);
        if (getGifts() == false){
            return false;
        }
        mAccount.setLastImport(Calendar.getInstance().getTime());
        OpenMPD.getDaoSession().getServiceAccountDao().update(mAccount);
        return true;
    }

    /**
     * Download the account's contacts.
     */
    public boolean getContacts(){

        if (builder != null){
            builder.setContentTitle("Importing Contacts");
            builder.setProgress(2, 3, true);
            notifyManager.notify(notification_id, builder.build());
        }
        ImportActivity.setProgress(mAccount.getId().intValue(), 2, 3, true);
        ImportActivity.setStatus(mAccount.getId().intValue(), R.string.importing_contacts);

        ArrayList<String> content = null;
        try {
            content = mAccount.getAddresses();
        } catch (TntService.ServerException e){
            Log.i("net.bradmont.openmpd", "ServerException from getAddresses()", e);
            return false;
        }
        if (content == null){
            return false;
        }

        String header_line = content.remove(0); 

        // p2c's server pads the header line with some fugly non-ascii
        // characters; strip out anything like that
        header_line = header_line.replaceAll("[^\\w,]", "");
        String [] headers = header_line.split(",");

        mDataHash = new HashMap<String, String>(headers.length);

        if (progressbar != null){
            progressbar.setIndeterminate(false);
            progressbar.setMax(content.size()*2);
        }
        if (builder != null){
            progressmax = content.size();
            builder.setProgress(progressmax, progress, false);
            notifyManager.notify(notification_id, builder.build());
        }
        ImportActivity.setProgress(mAccount.getId().intValue(), progressmax, progress, false);
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        for(String s:content){
            //Log.i(Config.PACKAGE, s);
            progress++;
            try{
                Contact c = parseAddressLine(headers, s);
                if (c != null){
                    contacts.add(c);
                }
                // TODO: we're transactionising Contacts, but not ContactDetails
            } catch (Exception e){
                //LogItem.logError(header_line, s, e);
                Log.i("net.bradmont.openmpd", header_line+" "+ s, e);
                // TODO: log errors
            }
            if (progressbar != null){
                progressbar.incrementProgressBy(1);
            }
            if (builder != null){
                builder.setProgress(progressmax, progress, false);
                notifyManager.notify(notification_id, builder.build());
                if (progress % 1000 == 0){
                    OpenMPD.getDaoSession().getContactDao().insertOrReplaceInTx(contacts, false);
                    contacts.clear();
                }
            }
            ImportActivity.setProgress(mAccount.getId().intValue(), progressmax, progress, false);
        }
        OpenMPD.getDaoSession().getContactDao().insertOrReplaceInTx(contacts, false);

        return true;
    }

    /**
     * Instead of running hundreds of queries to map tnt_people_id on gifts
     * to a contact, cache all that in a HashMap.
     */
    public HashMap<String, Long> MakeContactIdHash(){
        HashMap<String, Long> ids = new HashMap<String, Long>();
        Cursor cur = OpenMPD.getDB().rawQuery("select _id, tnt_people_id from contact", null);
        cur.moveToFirst();
        while (!cur.isAfterLast()){
            ids.put(cur.getString(1), cur.getLong(0));
        }
        return ids;
    }

    public boolean getGifts(){

        if (builder != null){
            builder.setContentTitle("Importing Gifts");
            builder.setProgress(progressmax, progress, true);
            notifyManager.notify(notification_id, builder.build());
        }
        ImportActivity.setProgress(mAccount.getId().intValue(), progressmax, progress, true);
        ImportActivity.setStatus(mAccount.getId().intValue(), R.string.importing_gifts);

        ArrayList<String> content = null;
        try {
            content = mAccount.getGifts();
        } catch (TntService.ServerException e){
            Log.i("net.bradmont.openmpd", "ServerException from ServiceAccount.getGifts()", e);
            return false;
        }

        if (content == null){
            return false;
        }
        if (content.size() == 1 ){
            // no data; consider update accomplished
            return true;
        }

        String header_line = content.remove(0);
        header_line = header_line.replaceAll("[^\\w,]", "");
        String [] headers = header_line.split(",");
        mDataHash = new HashMap<String, String>(headers.length);

        if (progressbar != null){
            progressbar.setMax((progressbar.getMax()/2) + content.size());
        }
        progressmax = content.size();
        progress = 0;
        if (builder != null){
            builder.setProgress(progressmax, progress, false);
            notifyManager.notify(notification_id, builder.build());
        }
        ImportActivity.setProgress(mAccount.getId().intValue(), progressmax, progress, false);
        ArrayList<Gift> gifts = new ArrayList<Gift>();
        for(String s:content){
            try {
                Gift gift = parseGiftLine(headers, s);
                if (gift != null){
                    gifts.add(gift);
                }
            } catch (Exception e){
                //LogItem.logError(header_line, s, e);
                Log.i("net.bradmont.openmpd", header_line + " " + s, e);
            }
            if (progressbar != null){
                progressbar.incrementProgressBy(1);
            }
            if (builder != null){
                builder.setProgress(progressmax, ++progress, false);
                notifyManager.notify(notification_id, builder.build());
            }
            ImportActivity.setProgress(mAccount.getId().intValue(), progressmax, progress, false);
            if (progress % 1000 == 0){
                OpenMPD.getDaoSession().getGiftDao().insertOrReplaceInTx(gifts);
                gifts.clear();
            }
        }
        OpenMPD.getDaoSession().getGiftDao().insertOrReplaceInTx(gifts);
        gifts.clear();
        ImportActivity.setStatus(mAccount.getId().intValue(), R.string.done);
        return true;
    }

    public Gift parseGiftLine(String [] headers, String line){
        String [] values = TextTools.csvLineSplit(line);
        for (int i=0; i < headers.length; i++){
            mDataHash.put(headers[i], values[i]);
            //Log.i("net.bradmont.openmpd", "" + headers[i] + ":'" + values[i]+"'");
        }

        // if this gift has already been imported, skip it.
        GiftDao giftDao = OpenMPD.getDaoSession().getGiftDao();
        List<net.bradmont.openmpd.dao.Gift> gifts = giftDao.queryBuilder()
            .where(GiftDao.Properties.TntDonationId.eq(mDataHash.get("DONATION_ID")))
                    .list();
        if (gifts.size() != 0){
            return null;
        }

        Gift gift = new Gift();
        String date = mDataHash.get("DISPLAY_DATE");
        String [] dateParts = date.split("/");
        date = String.format("%04d-%02d-%02d", 
            Integer.parseInt(dateParts[2]),
            Integer.parseInt(dateParts[0]),
            Integer.parseInt(dateParts[1]));
        String month = String.format("%04d-%02d", 
            Integer.parseInt(dateParts[2]),
            Integer.parseInt(dateParts[0]));


        gift.setContactId(mContactIdByTntId.get (mDataHash.get("PEOPLE_ID") ) );
        gift.setDate(date);
        gift.setMonth(month);
        gift.setAmount(Long.parseLong(mDataHash.get("AMOUNT")));
        gift.setMotivationCode(mDataHash.get("MOTIVATION"));
        //gift.setValue("account", mDataHash.get("DESIGNATION"));
        gift.setTntDonationId(mDataHash.get("DONATION_ID"));
        return gift;
    }

    public Contact parseAddressLine(String [] headers, String line){
        String [] values = TextTools.csvLineSplit(line);

        for (int i=0; i < headers.length; i++){
            if (headers[i].equals("PEOPLE_ID")){
                mDataHash.put(headers[i], values[i] + mAccount.getName());
            } else {
                mDataHash.put(headers[i], values[i]);
            }
        }

        // Retrieve existing contact or create a new one
        ContactDao contactDao = OpenMPD.getDaoSession().getContactDao();
        List<Contact> contacts = contactDao.queryBuilder()
            .where(ContactDao.Properties.TntPeopleId.eq(mDataHash.get("PEOPLE_ID")))
            .list();
        Contact contact;
        boolean newContact = false;
        if (contacts.size() != 0){
            contact = contacts.get(0);
        } else {
            contact = new Contact();
            newContact = true;
        }
        contact.setTntPeopleId(mDataHash.get("PEOPLE_ID")); 
        contact.setTntAccountName(mDataHash.get("ACCT_NAME"));
        contact.setTntPersonType(mDataHash.get("PERSON_TYPE"));

        // retrieve or create the primary Person for this contact
        Person person = contact.getPrimaryPerson();
        if (person == null) {
            person = new Person();
        }
        person.setContact(contact);
        person.setLname(mDataHash.get("LAST_NAME_ORG"));
        person.setFname(mDataHash.get("FIRST_NAME"));
        person.setMname(mDataHash.get("MIDDLE_NAME"));
        person.setTitle(mDataHash.get("TITLE"));
        person.setSuffix(mDataHash.get("SUFFIX"));
        person.setIsContactPrimary(true);
        person.setIsTntSpouse(false);

        if (person.getFname() != "" || person.getLname() != ""){
            if (newContact){
                OpenMPD.getDaoSession().getContactDao().insert(contact);
                OpenMPD.getDaoSession().getPersonDao().insert(person);
            } else {
                OpenMPD.getDaoSession().getContactDao().update(contact);
                OpenMPD.getDaoSession().getPersonDao().update(person);
            }
        }

        // if the record includes a spouse
        if (mDataHash.get("SP_FIRST_NAME") != null && mDataHash.get("SP_FIRST_NAME").length() > 1){
            // create or retrieve spouse
            Person spouse = contact.getTntSpouse();
            boolean newSpouse = false;
            if (spouse == null){
                spouse = new Person();
                newSpouse = true;
            }

            spouse.setContact(contact);
            spouse.setLname(mDataHash.get("SP_LAST_NAME"));
            spouse.setFname(mDataHash.get("SP_FIRST_NAME"));
            spouse.setMname(mDataHash.get("SP_MIDDLE_NAME"));
            spouse.setTitle(mDataHash.get("SP_TITLE"));
            spouse.setSuffix(mDataHash.get("SP_SUFFIX"));
            spouse.setIsContactPrimary(false);
            spouse.setIsTntSpouse(true);
            if (newSpouse){
                OpenMPD.getDaoSession().getPersonDao().insert(spouse);
            } else {
                OpenMPD.getDaoSession().getPersonDao().update(spouse);
            }
        }

        // retreive or create the address, phone, email ContactDetail items
        ContactDetail address = contact.getTntAddressOrNew();
        ContactDetail phone = contact.getTntPhoneOrNew();
        ContactDetail email = contact.getTntEmailOrNew();


        address.setContact(contact);
        address.setAddedDate(TextTools.mkDate(mDataHash.get("ADDR_CHANGED")));
        address.setFromTnt(true);
        address.setLabel(mAccount.getTntService().getName());
        //address.setOperational(mDataHash.get("ADDR_DELIVERABLE"));

        JSONObject addressData = new JSONObject();
        try {
            addressData.put("addr1", mDataHash.get("ADDR1"));
            addressData.put("addr2", mDataHash.get("ADDR2"));
            addressData.put("addr3", mDataHash.get("ADDR3"));
            addressData.put("addr4", mDataHash.get("ADDR4"));
            addressData.put("city", mDataHash.get("CITY"));
            addressData.put("region", mDataHash.get("STATE"));
            addressData.put("post_code", mDataHash.get("ZIP"));
            addressData.put("country_short", mDataHash.get("COUNTRY"));
            addressData.put("country_long", mDataHash.get("COUNTRY_DESCR"));
            addressData.put("valid_from", mDataHash.get("ADDR_CHANGED"));
            address.setData(addressData.toString());
        } catch (JSONException e){
            Log.i("net.bradmont.openmpd", "JSONException", e);
        }
        try {
            address.update();
        } catch (Exception e){
            OpenMPD.getDaoSession().getContactDetailDao().insert(address);
        }

        phone.setFromTnt(true);
        phone.setContact(contact);
        phone.setAddedDate(TextTools.mkDate(mDataHash.get("PHONE_CHANGED")));
        phone.setLabel(mAccount.getTntService().getName());
        // phone.setOperational(mDataHash.get("ADDR_DELIVERABLE"));
        
        JSONObject phoneData = new JSONObject();
        try {
            phoneData.put("number", mDataHash.get("PHONE"));
        } catch (JSONException e){
            Log.i("net.bradmont.openmpd", "JSONException", e);
        }
        phone.setData(phoneData.toString());
        try {
            phone.update();
        } catch (Exception e){
            OpenMPD.getDaoSession().getContactDetailDao().insert(phone);
        }


        email.setFromTnt(true);
        email.setContact(contact);
        email.setAddedDate(TextTools.mkDate(mDataHash.get("EMAIL_CHANGED")));
        email.setLabel(mAccount.getTntService().getName());
        //email.setOperational(mDataHash.get("ADDR_DELIVERABLE"));
        
        JSONObject emailData = new JSONObject();
        try{
            emailData.put("number", mDataHash.get("EMAIL"));
        } catch (JSONException e){
            Log.i("net.bradmont.openmpd", "JSONException", e);
        }
        email.setData(phoneData.toString());
        try {
            phone.update();
        } catch (Exception e){
            OpenMPD.getDaoSession().getContactDetailDao().insert(phone);
        }

        return contact;
    }


}
