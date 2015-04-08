package net.bradmont.openmpd.controllers;

import net.bradmont.openmpd.models.*;
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
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
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
import android.widget.ProgressBar;
import android.util.Log;

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

import org.ini4j.Ini;

/** Controller to connect to and import data from TntDataServer instances.
  *
  */
public class TntImporter {

    private ProgressBar progressbar=null;
    private NotificationCompat.Builder builder = null;
    private NotificationManager notifyManager = null;
    private Context context;
    private HashMap<String, String> data ;
        // Initialize this HashMap only once to save time

    private ServiceAccount account;
    private TntService service = null;
    private int progress = 0;
    private int progressmax=0;
    private static int notification_id=ContactsEvaluator.NOTIFICATION_ID;
    private static String EPOCH_DATE="01/01/1970";

    public TntImporter(Context context, ServiceAccount account){
        this.context = context;
        this.account = account;
        service = (TntService) account.getRelated("tnt_service_id");
    }
    public TntImporter(Context context, ServiceAccount account, ProgressBar progressbar){
        this.context = context;
        this.account = account;
        service = (TntService) account.getRelated("tnt_service_id");
        this.progressbar = progressbar;
    }

    public TntImporter(Context context, ServiceAccount account, NotificationCompat.Builder builder){
        this.context = context;
        this.account = account;
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

        // upgrade from legacy tnt_service to using query.ini properly
        if (service.getUsernameKey() == null || service.getUsernameKey().length() < 2){
            processQueryIni(service);
            service.dirtySave();
        }

        if (getContacts() == false){
            return false;
        }
        if (progressbar != null){
            progressbar.setIndeterminate(false);
        }
        if (builder != null){
            builder.setProgress(progressmax, progress, false);
            notifyManager.notify(notification_id, builder.build());
        }
        if (getGifts() == false){
            return false;
        }
        account.setValue("last_import", getTodaysDate());
        account.dirtySave();
        return true;
    }

    public boolean getContacts(){
        ArrayList<BasicNameValuePair> arguments = new ArrayList<BasicNameValuePair>(4);

        if (builder != null){
            builder.setContentTitle("Importing Contacts");
            builder.setProgress(2, 3, true);
            notifyManager.notify(notification_id, builder.build());
        }
        arguments.add(new BasicNameValuePair( "Action", service.getAddressesAction()));
        arguments.add(new BasicNameValuePair( service.getUsernameKey(), account.getString("username")));
        arguments.add(new BasicNameValuePair( service.getPasswordKey(), account.getString("password")));

        // Always download contacts from all time, since TNTDataService only
        // gives donor info for donors who are new TO THE ORGANISATION,
        // not new to the user. So if I gain a new supporter who has 
        // already given to someone else, they will not show up in my donor
        // query unless we query from before their most recent address
        // change. Annoying, but donor import is much faster than gift
        // import, so it doesn't really cause much of a slow-down.
        arguments.add(new BasicNameValuePair( "DateFrom", EPOCH_DATE));

        ArrayList<String> content = null;
        try {
            content = getStringsFromUrl(service.getString("base_url") + service.getString("addresses_url"), arguments);
        } catch (ServerException e){
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

        data = new HashMap<String, String>(headers.length);

        if (progressbar != null){
            progressbar.setIndeterminate(false);
            progressbar.setMax(content.size()*2);
        }
        if (builder != null){
            progressmax = content.size();
            builder.setProgress(progressmax, progress, false);
            notifyManager.notify(notification_id, builder.build());
        }
        Contact temp = new Contact();
        Contact.beginTransaction();
        for(String s:content){
            //Log.i(Config.PACKAGE, s);
            try{
                parseAddressLine(headers, s);
            } catch (Exception e){
                LogItem.logError(header_line, s, e);
            }
            if (progressbar != null){
                progressbar.incrementProgressBy(1);
            }
            if (builder != null){
                progress++;
                builder.setProgress(progressmax, progress, false);
                notifyManager.notify(notification_id, builder.build());
                if (progress % 1000 == 0){
                    Contact.endTransaction();
                    Contact.beginTransaction();
                }
            }
        }
        Contact.endTransaction();
        return true;
    }
    public boolean getGifts(){

        if (builder != null){
            builder.setContentTitle("Importing Gifts");
            builder.setProgress(progressmax, progress, true);
            notifyManager.notify(notification_id, builder.build());
        }
        ArrayList<BasicNameValuePair> arguments = new ArrayList<BasicNameValuePair>(4);

        arguments.add(new BasicNameValuePair( "Action", service.getDonationsAction()));
        arguments.add(new BasicNameValuePair( service.getUsernameKey(), account.getString("username")));
        arguments.add(new BasicNameValuePair( service.getPasswordKey(), account.getString("password")));

        if (account.getString("last_import") == null){
            arguments.add(new BasicNameValuePair( "DateFrom", EPOCH_DATE));
        } else {
            Calendar cal = account.getCalendar("last_import");
            cal.add(Calendar.DAY_OF_MONTH, -14); // gifts can take a while to 
                                                        // appear in the system

            DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            arguments.add(new BasicNameValuePair( "DateFrom", 
                format.format(cal.getTime())
            ));
        }

        arguments.add(new BasicNameValuePair( "DateTo", stupidDateFormat(getTodaysDate())));

        ArrayList<String> content = null;
        try {
            content = getStringsFromUrl(service.getString("base_url") + service.getString("donations_url"), arguments);
        } catch (ServerException e){
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
        data = new HashMap<String, String>(headers.length);

        if (progressbar != null){
            progressbar.setMax((progressbar.getMax()/2) + content.size());
        }
        if (builder != null){
            progressmax = content.size();
            progress = 0;
            builder.setProgress(progressmax, progress, false);
            notifyManager.notify(notification_id, builder.build());
        }
        Gift temp = new Gift();
        Gift.beginTransaction();
        for(String s:content){
            try {
                parseGiftLine(headers, s);
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
            if (progress % 1000 == 0){
                Gift.endTransaction();
                Gift.beginTransaction();
            }
        }
        Gift.endTransaction();
        return true;
    }

    /** Takes an ISO 8601 date string and converts it to a MM/DD/YYYY string,
     * as required by TntDataServer.
     */
    public String stupidDateFormat(String date){
        String [] parts = date.split("-");
        return String.format("%s/%s/%s", parts[1], parts[2], parts[0]);
    }

    /** Returns today's date in ISO 8601 format
     */
    public static String getTodaysDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    public void parseGiftLine(String [] headers, String line){
        String [] values = csvLineSplit(line);
        for (int i=0; i < headers.length; i++){
            data.put(headers[i], values[i]);
            //Log.i("net.bradmont.openmpd", "" + headers[i] + ":'" + values[i]+"'");
        }

        // if this gift has already been imported, skip it.
        ModelList gifts = MPDDBHelper.filter("gift", "tnt_donation_id", data.get("DONATION_ID"));
        if (gifts.size() != 0){
            return;
        }

        Gift gift = new Gift();
        String date = data.get("DISPLAY_DATE");
        String [] dateParts = date.split("/");
        date = String.format("%04d-%02d-%02d", 
            Integer.parseInt(dateParts[2]),
            Integer.parseInt(dateParts[0]),
            Integer.parseInt(dateParts[1]));
        String month = String.format("%04d-%02d", 
            Integer.parseInt(dateParts[2]),
            Integer.parseInt(dateParts[0]));


        gift.setValue("tnt_people_id", data.get("PEOPLE_ID"));
        gift.setValue("date", date);
        gift.setValue("month", month);
        gift.setValue("amount", data.get("AMOUNT"));
        gift.setValue("motivation_code", data.get("MOTIVATION"));
        gift.setValue("account", data.get("DESIGNATION"));
        gift.setValue("tnt_donation_id", data.get("DONATION_ID"));
        gift.dirtySave();
    }

    public void parseAddressLine(String [] headers, String line){
        String [] values = csvLineSplit(line);

        for (int i=0; i < headers.length; i++){
            data.put(headers[i], values[i]);
        }


        ModelList contacts = MPDDBHelper.filter("contact", "tnt_people_id", data.get("PEOPLE_ID"));
        Contact contact;
        if (contacts.size() != 0){
            contact = (Contact) contacts.get(0);
        } else {
            contact = new Contact();
        }
        contact.setValue("tnt_people_id", data.get("PEOPLE_ID"));
        contact.setValue("tnt_account_name", data.get("ACCT_NAME"));
        contact.setValue("tnt_person_type", data.get("PERSON_TYPE"));
        contact.setValue("lname", data.get("LAST_NAME_ORG"));
        contact.setValue("fname", data.get("FIRST_NAME"));
        contact.setValue("mname", data.get("MIDDLE_NAME"));
        contact.setValue("title", data.get("TITLE"));
        contact.setValue("suffix", data.get("SUFFIX"));
        contact.setValue("account", account);

        if (contact.getString("fname") != "" || contact.getString("lname") != ""){
            contact.dirtySave();
        }

        if (data.get("SP_FIRST_NAME") != null && data.get("SP_FIRST_NAME").length() > 1){
            Contact spouse = null;
            if (contacts.size() != 0){
                try {
                    spouse = (Contact) contact.getRelated("spouse");
                } catch (Exception e){
                    spouse = new Contact();
                }
            } else {
                spouse = new Contact();
            }

            // create spouse object
            spouse.setValue("tnt_people_id", "-" + data.get("PEOPLE_ID"));
                // kind of silly to set a negative people_id, but this
                // allows us to make the field unique and still support
                // married people as individual contacts
            spouse.setValue("tnt_account_name", data.get("ACCT_NAME"));
            spouse.setValue("tnt_person_type", data.get("PERSON_TYPE"));
            spouse.setValue("lname", data.get("SP_LAST_NAME"));
            spouse.setValue("fname", data.get("SP_FIRST_NAME"));
            spouse.setValue("mname", data.get("SP_MIDDLE_NAME"));
            spouse.setValue("title", data.get("SP_TITLE"));
            spouse.setValue("suffix", data.get("SP_SUFFIX"));
            spouse.setValue("primary_contact", false);
            spouse.setValue("spouse", contact);
            spouse.setValue("account", account);
            if (spouse.getString("fname") != "" || spouse.getString("lname") != ""){
                spouse.dirtySave();
            }
            contact.setValue("spouse", spouse);
            if (contact.getString("fname") != "" || contact.getString("lname") != ""){
                contact.dirtySave();
            }
        }

        Address address = null;
        PhoneNumber phone = null;
        EmailAddress email = null;

        if (contacts.size() == 0){
            address = new Address();
            phone = new PhoneNumber();
            email = new EmailAddress();
        } else {
            address = contact.getAddress();
            phone = contact.getPhone();
            email = contact.getEmail();
        }

        if (address != null && 
                (address.getString("valid_from") == null ||
                !address.getString("valid_from").equals(data.get("ADDR_CHANGED")) )
                ){
            // TODO: evaluate if address has changed.
            address.setValue("addr1", data.get("ADDR1"));
            address.setValue("addr2", data.get("ADDR2"));
            address.setValue("addr3", data.get("ADDR3"));
            address.setValue("addr4", data.get("ADDR4"));
            address.setValue("city", data.get("CITY"));
            address.setValue("region", data.get("STATE"));
            address.setValue("post_code", data.get("ZIP"));
            address.setValue("country_short", data.get("COUNTRY"));
            address.setValue("country_long", data.get("COUNTRY_DESCR"));
            // TODO:
            address.setValue("valid_from", data.get("ADDR_CHANGED"));
            //address.setValue("deliverable", data.get("ADDR_DELIVERABLE"));
            address.setValue("contact_id", contact);
            address.dirtySave();
        }


        if (phone != null && 
            ( phone.getString("added_date") == null ||
            !phone.getString("added_date").equals(data.get("PHONE_CHANGED")))
        ){

            phone.setValue("number", data.get("PHONE"));
            phone.setValue("added_date", data.get("PHONE_CHANGED"));
            //phone.setValue("operational", data.get("PHONE_OPERATIONAL"));
            phone.setValue("label", "Default");
            phone.setValue("contact_id", contact);
            phone.dirtySave();
        }

        if (phone != null && 
            (email.getString("added_date") == null ||
            !email.getString("added_date").equals(data.get("EMAIL_CHANGED")))
        ){
            email.setValue("address", data.get("EMAIL"));
            email.setValue("added_date", data.get("EMAIL_CHANGED"));
            //email.setValue("operational", data.get("EMAIL_OPERATIONAL"));
            email.setValue("label", "Default");
            email.setValue("contact_id", contact);
            email.dirtySave();
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

    public ArrayList<String> getStringsFromUrl(String url, ArrayList arguments) throws ServerException{
        return getStringsFromUrl(url, arguments, true);
    }

    public ArrayList<String> getStringsFromUrl(String url_raw, ArrayList arguments, boolean handleCertError) throws ServerException{

        URL url = null;
        try {
            url = new URL(url_raw);
        } catch (Exception e){
            return null;
        }

        if (builder != null){
            builder.setContentText(url.getHost());
            notifyManager.notify(notification_id, builder.build());
        }
        InputStream stream = getStreamFromUrl(url_raw, arguments, handleCertError, builder, notifyManager);
        if (stream == null){
            return null;
        }

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(stream), 4096);
        String line;
        ArrayList<String> lines = new ArrayList<String>(50);
        try{
            while ((line = rd.readLine()) != null) {
                lines.add(line);
                //Log.i("net.bradmont.openmpd", line);
            }
            rd.close();
            //Log.i("net.bradmont.openmpd", "finished reading");
        } catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.i("net.bradmont.openmpd.controllers.TntImporter",sw.toString());
        }
        Log.i("net.bradmont.openmpd", String.format("returning %d lines", lines.size()));
        /*for (String l : lines){
            Log.i("net.bradmont.openmpd", l);
        }*/
        if (lines == null ||
                 lines.get(0).contains("ERROR") ||
                 lines.get(0).contains("BAD_PASSWORD")){
            // TODO: we may need to detect other errors here....
            String error = "";
            for (String s : lines){
                error += s;
            }
            throw new ServerException(error);
         }
        return lines;
    }

    public class ServerException extends Exception{
        public ServerException() { super(); }
        public ServerException(String message) { super(message); }
        public ServerException(String message, Throwable cause) { super(message, cause); }
        public ServerException(Throwable cause) { super(cause); }
    }

    /**
     * Check if the account login is valid
     */
    public boolean verifyAccount(){
        try {
            getBalance();
        } catch (ServerException e){
            Log.i("net.bradmont.openmpd", "server error");
            return false;
        } catch (RuntimeException e){
            Log.i("net.bradmont.openmpd", "runtime exception");
            URL u = null;
            try { u = new URL(service.getString("base_url") + service.getString("balance_url")); } catch (Exception f){}
            if (u.getHost().contains("focus.powertochange.org")){
                SharedPreferences.Editor prefs = OpenMPD.get().getSharedPreferences("openmpd", Context.MODE_PRIVATE).edit();
                prefs.putBoolean("ignore_ssl_" + u.getHost(), true);
                prefs.commit();
                try {
                    getBalance();
                } catch (Exception f){
                    return false;
                }
            } else {
                throw e;
            }
        }
        return true;
    }

    /**
     * returns account balance in cents
     */
    public int getBalance() throws ServerException{
        ArrayList<BasicNameValuePair> arguments = new ArrayList<BasicNameValuePair>(4);
        arguments.add(new BasicNameValuePair( "Action", service.getBalanceAction()));
        arguments.add(new BasicNameValuePair( service.getUsernameKey(), account.getString("username")));
        arguments.add(new BasicNameValuePair( service.getPasswordKey(), account.getString("password")));
        ArrayList<String> content = null;
        content = getStringsFromUrl(service.getString("base_url") + service.getString("balance_url"), arguments, false);
        String [] headers = csvLineSplit(content.get(0));
        String [] values = csvLineSplit(content.get(1));
        for (int i = 0; i < headers.length; i++){
            if (headers[i].equals("BALANCE")){
                DecimalFormat format = new DecimalFormat();
                format.setParseBigDecimal(true);
                BigDecimal balance = null;
                try {
                    balance = (BigDecimal) format.parse(values[i]);
                } catch (Exception e){
                    Log.i("net.bradmont.openmpd", "Invalid balance format: " + values[i]);
                    return 0;
                }
                balance = balance.multiply(new BigDecimal(100));

                return balance.intValue();
            }
        }
        return 0;
    }

    private static StringReader getCleanedStringReaderFromUrl(String url){
        try {
            InputStream stream = getStreamFromUrl(url, null, false);
            if (stream == null) { return null; }
            StringBuilder sb = new StringBuilder();
            int character = 0;
            boolean started = false;
            // read and chop off non-ascii gunk at the beginning
            character = stream.read();
            while (character != -1){
                if ( (char) character == '['){
                    started=true;
                }
                if (started){
                    sb.append( (char) character);
                }
                character = stream.read();
            }
            stream.close();
            return new StringReader(sb.toString());

        } catch (IOException e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.i("net.bradmont.openmpd",sw.toString());
            return null;
        }
    }

    public static InputStream getStreamFromUrl(String url_raw, ArrayList arguments, boolean handleCertError){
        return getStreamFromUrl(url_raw, arguments, handleCertError, null, null);
    }

    public static InputStream getStreamFromUrl(String url_raw, ArrayList arguments, boolean handleCertError, NotificationCompat.Builder builder, NotificationManager notifyManager){
        InputStream content = null;
        
        URL url = null;
        try {
            url = new URL(url_raw);
        } catch (Exception e){
            return null;
        }
        try {
            DefaultHttpClient httpclient = null;
            SharedPreferences prefs = OpenMPD.get().getSharedPreferences("openmpd", Context.MODE_PRIVATE);
            if (prefs.getBoolean("ignore_ssl_" + url.getHost(), false) == true){ 
                httpclient = getNewHttpClient();
            } else {
                httpclient = new DefaultHttpClient();
            }

            HttpHost targetHost;
            if (url.getProtocol() == "https"){
                targetHost = new HttpHost(url.getHost(), 443, url.getProtocol()); 
            } else if (url.getProtocol() == "http"){
                targetHost = new HttpHost(url.getHost(), 80, url.getProtocol()); 
            } else {
                targetHost = new HttpHost(url.getHost(), 443, url.getProtocol()); 
            }


            HttpUriRequest httpRequest = null;
            if (arguments != null){
                HttpPost httpPost = new HttpPost(url_raw);
                httpPost.setEntity(new UrlEncodedFormEntity(arguments));

                httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(targetHost.getHostName(), targetHost.getPort()), 
                    new UsernamePasswordCredentials(
                        ((BasicNameValuePair) arguments.get(1)).getValue(), // username
                        ((BasicNameValuePair) arguments.get(2)).getValue()
                        ));
                httpRequest = httpPost;
            } else {
                httpRequest = new HttpGet(url_raw);
            }

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httpRequest);
            content = response.getEntity().getContent();

            return content;
        } catch (org.apache.http.conn.HttpHostConnectException e){
            if (builder != null){
                builder.setProgress(0, 0, false);
                builder.setContentTitle("Error connecting to server.");
                notifyManager.notify(notification_id, builder.build());
            }
        } catch (javax.net.ssl.SSLPeerUnverifiedException e){
            // SSL certs on the server are not accepted
            if (handleCertError == false){
                throw new RuntimeException("SSLError Certificate not accepted");
            }
            NotificationManager notificationManager =
                (NotificationManager) OpenMPD.get().getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder nBuilder =
                new NotificationCompat.Builder(OpenMPD.get())
                .setSmallIcon(R.drawable.notification_icon);

            nBuilder.setContentTitle("SSL Certificate Error");
            nBuilder.setContentText(url.getHost());

            Intent sslCertIntent = new Intent(OpenMPD.get(), HomeActivity.class);
            sslCertIntent.putExtra("net.bradmont.openmpd.SSLErrorServer", url.getHost());

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(OpenMPD.get());
            stackBuilder.addParentStack(HomeActivity.class);
            stackBuilder.addNextIntent(sslCertIntent);
            PendingIntent sslPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            nBuilder.setContentIntent(sslPendingIntent);


            notificationManager.notify(666, nBuilder.build());
            return null;



        } catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.i("net.bradmont.openmpd",sw.toString());
        }
        return content;
    }

    public static boolean processQueryIni(TntService service){
        Ini ini = null;
        StringReader reader = null;
        try {
            URL u = new URL(service.getString("query_ini_url"));
        } catch (Exception e){
            Log.i("net.bradmont.openmpd", "malformed query.ini url");
            return false;
        }
        try {
            //ini = new Ini(getStreamFromUrl(service.getString("query_ini_url"), null, false));
            reader = getCleanedStringReaderFromUrl(service.getString("query_ini_url"));
            if (reader == null){ 
                Log.i("net.bradmont.openmpd", "StringReader is null");
                return false;
            }
            ini = new Ini(reader);
        } catch (IOException e){
            Log.i("net.bradmont.openmpd", "IOException");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.i("net.bradmont.openmpd",sw.toString());
            return false;
        }
        if (!ini.containsKey("ORGANIZATION")){
            Log.i("net.bradmont.openmpd", "no section ORGANIZATION. huh.");
            Log.i("net.bradmont.openmpd", service.getString("query_ini_url"));
            StringBuilder sb = new StringBuilder();
            try {
                reader = getCleanedStringReaderFromUrl(service.getString("query_ini_url"));
                int character = reader.read();
                while (character != -1){
                    sb.append((char) character);
                    character = reader.read();
                }
            } catch (Exception e){}
            Log.i("net.bradmont.openmpd", "No ORGANIZATION section in query.ini");
            return false;
        }
        Ini.Section org = ini.get("ORGANIZATION");

        if (org.containsKey("RedirectQueryIni") && org.get("RedirectQueryIni").startsWith("http")){
            // if this query.ini url is outdated, it should redirect to a current one.
            service.setValue("query_ini_url", org.get("RedirectQueryIni"));
            return processQueryIni(service);
        }
        service.setValue("base_url", ""); // eventually get rid of base_url; for the moment leave
                                         // it blank for new services
        if (org.containsKey("Code")){
            service.setValue("name_short", org.get("Code"));
        }
        if (org.containsKey("QueryAuthentication") && org.get("QueryAuthentication") == "1"){
            service.setValue("http_auth", true);
        }

        if (!ini.containsKey("ACCOUNT_BALANCE") || 
                !ini.containsKey("DONATIONS") ||
                !ini.containsKey("ADDRESSES") ||
                !ini.containsKey("ADDRESSES_BY_PERSONIDS") ){
            Log.i("net.bradmont.openmpd", "query.ini missing section(s)");
            return false;

        }

        Ini.Section balance = ini.get("ACCOUNT_BALANCE");
        Ini.Section donations = ini.get("DONATIONS");
        Ini.Section addresses = ini.get("ADDRESSES");
        Ini.Section addresses_by_personids = ini.get("ADDRESSES_BY_PERSONIDS");

        if (!balance.containsKey("Url") || !balance.containsKey("Post") ||
                !donations.containsKey("Url") || !donations.containsKey("Post") ||
                !addresses.containsKey("Url") || !addresses.containsKey("Post") ||
                !addresses_by_personids.containsKey("Url") || !addresses_by_personids.containsKey("Post") ){
            Log.i("net.bradmont.openmpd", "query.ini missing url");
            return false;
        }

        service.setValue("balance_url", balance.get("Url"));
        service.setValue("balance_formdata", balance.get("Post"));

        service.setValue("donations_url", donations.get("Url"));
        service.setValue("donations_formdata", donations.get("Post"));

        service.setValue("addresses_url", addresses.get("Url"));
        service.setValue("addresses_formdata", addresses.get("Post"));

        service.setValue("addresses_by_personids_url", addresses_by_personids.get("Url"));
        service.setValue("addresses_by_personids_formdata", addresses_by_personids.get("Post"));
        service.dirtySave();

        return true; 
    }

    public static DefaultHttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }


}
