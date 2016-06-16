package net.bradmont.openmpd.controllers;

import net.bradmont.openmpd.dao.*;
import net.bradmont.openmpd.*;
import net.bradmont.supergreen.models.ModelList;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.message.*;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.conn.SingleClientConnManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ClientConnectionManager;
import org.json.JSONObject;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.lang.StackTraceElement;
import java.lang.Thread;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.HTTP;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
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

import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;

import net.bradmont.openmpd.activities.ImportActivity;

import org.ini4j.Ini;

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
    private static String EPOCH_DATE="01/01/1970";

    public TntImporter(Context context, ServiceAccount account){
        this.context = context;
        this.mAccount = account;
        service = (TntService) mAccount.getRelated("tnt_service_id");
    }
    public TntImporter(Context context, ServiceAccount account, ProgressBar progressbar){
        this.context = context;
        this.mAccount = account;
        service = (TntService) account.getRelated("tnt_service_id");
        this.progressbar = progressbar;
    }

    public TntImporter(Context context, ServiceAccount account, NotificationCompat.Builder builder){
        this.context = context;
        this.mAccount = account;
        service = (TntService) account.getRelated("tnt_service_id");
        this.builder = builder;
        notifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //notification_id = account.getID();

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

        ImportActivity.setProgress(mAccount.getID(), progressmax, progress, true);

        // upgrade from legacy tnt_service to using query.ini properly
        if (service.getUsernameKey() == null || service.getUsernameKey().length() < 2){
            service.processQueryIni();
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
        ImportActivity.setProgress(mAccount.getID(), progressmax, progress, false);
        if (getGifts() == false){
            return false;
        }
        mAccount.setLastImport(getTodaysDate());
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
        ImportActivity.setProgress(mAccount.getId(), 2, 3, true);
        ImportActivity.setStatus(mAccount.getId(), R.string.importing_contacts);

        ArrayList<String> content = mAccount.getAddresses();
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
        ImportActivity.setProgress(mAccount.getId(), progressmax, progress, false);
        Arraylist<Contact> contacts = new Arraylist<Contact>();
        for(String s:content){
            //Log.i(Config.PACKAGE, s);
            progress++;
            try{
                Contact c = parseAddressLine(headers, s);
                contacts.add(c);
            } catch (Exception e){
                LogItem.logError(header_line, s, e);
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
            ImportActivity.setProgress(mAccount.getID(), progressmax, progress, false);
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
        Cursor cur = OpenMPD.getDB.rawQuery("select _id, tnt_people_id from contact");
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
        ImportActivity.setProgress(mAccount.getID(), progressmax, progress, true);
        ImportActivity.setStatus(mAccount.getID(), R.string.importing_gifts);

        ArrayList<String> content = mAccount.getGifts();

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
        ImportActivity.setProgress(mAccount.getID(), progressmax, progress, false);
        ArrayList<Gift> gifts = new ArrayList<Gift>();
        for(String s:content){
            try {
                Gift gift = parseGiftLine(headers, s);
                gifts.add(gift);
            } catch (Exception e){
                LogItem.logError(header_line, s, e);
            }
            if (progressbar != null){
                progressbar.incrementProgressBy(1);
            }
            if (builder != null){
                builder.setProgress(progressmax, ++progress, false);
                notifyManager.notify(notification_id, builder.build());
            }
            ImportActivity.setProgress(mAccount.getID(), progressmax, progress, false);
            if (progress % 1000 == 0){
                OpenMPD.getDaoSession().getGiftDao().insertOrReplaceInTx(gifts);
                gifts.clear();
            }
        }
        OpenMPD.getDaoSession().getGiftDao().insertOrReplaceInTx(gifts);
        gifts.clear();
        ImportActivity.setStatus(mAccount.getID(), R.string.done);
        return true;
    }

    public Gift parseGiftLine(String [] headers, String line){
        String [] values = csvLineSplit(line);
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
            return;
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


        gift.setContact(mContactIdByTntId.get (mDataHash.get("PEOPLE_ID") ) );
        gift.setDate(date);
        gift.setMonth(month);
        gift.setAmount(mDataHash.get("AMOUNT"));
        gift.setMotivationCode(mDataHash.get("MOTIVATION"));
        //gift.setValue("account", mDataHash.get("DESIGNATION"));
        gift.setTntDonationId(mDataHash.get("DONATION_ID"));
        return gift;
    }

    public void parseAddressLine(String [] headers, String line){
        String [] values = csvLineSplit(line);

        for (int i=0; i < headers.length; i++){
            if (headers[i].equals("PEOPLE_ID")){
                mDataHash.put(headers[i], values[i] + mAccount.getName());
            } else {
                mDataHash.put(headers[i], values[i]);
            }
        }

        // Retrieve existing contact or create a new one
        ContactDao contactDao = OpenMPD.getDaoSession().getContactDao();
        List<net.bradmont.openmpd.dao.Contact> contacts = contactDao.queryBuilder()
            .where(ContactDao.Properties.TntPeopleId.eq(mDataHash.get("PEOPLE_ID")))
            .where(ContactDao.Properties.SubId.eq("0"))
            .list();
        net.bradmont.openmpd.dao.Contact contact;
        boolean newContact = false;
        if (contacts.size() != 0){
            contact = contacts.get(0);
        } else {
            contact = new Contact();
            new_contact = true;
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
                spouse = new Spouse();
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
        address(getTodaysDate());
        address.setAddedDate(mDataHash.get("ADDR_CHANGED");
        address.setFromTnt(true);
        address.setLabel(mAccount.getTntService().getName());
        address.setOperational(address.setValue("deliverable", mDataHash.get("ADDR_DELIVERABLE")));

        JSONObject addressData = new JSONObject();
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
        try {
            address.update();
        } catch (Exception e){
            OpenMPD.getDaoSession().getContactDetailDao().insert(address);
        }

        phone.setFromTnt(true);
        phone.setContact(contact);
        phone.setAddedDate(getTodaysDate());
        phone.setAddedDate(mDataHash.get("PHONE_CHANGED");
        phone.setLabel(mAccount.getTntService().getName());
        phone.setOperational(address.setValue("deliverable", mDataHash.get("ADDR_DELIVERABLE")));
        
        JSONObject phoneData = new JSONObject();
        phoneData.put("number", mDataHash.get("PHONE"));
        phone.setData(phoneData.toString());
        try {
            phone.update();
        } catch (Exception e){
            OpenMPD.getDaoSession().getContactDetailDao().insert(phone);
        }


        email.setFromTnt(true);
        email.setContact(contact);
        email.setAddedDate(mDataHash.get("EMAIL_CHANGED");
        email.setLabel(mAccount.getTntService().getName());
        email.setOperational(address.setValue("deliverable", mDataHash.get("ADDR_DELIVERABLE")));
        
        JSONObject emailData = new JSONObject();
        emailData.put("number", mDataHash.get("EMAIL"));
        email.setData(phoneData.toString());
        try {
            phone.update();
        } catch (Exception e){
            OpenMPD.getDaoSession().getContactDetailDao().insert(phone);
        }

    }

    /**  Split a line of CSV data into a string array
      *
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

}
