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

import  javax.net.ssl.SSLContext;
import  javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import  org.apache.http.conn.scheme.SchemeRegistry;
import  org.apache.http.conn.scheme.Scheme;
import  org.apache.http.conn.ClientConnectionManager;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.io.IOException;
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
    private int notification_id=ContactsEvaluator.NOTIFICATION_ID;
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
        arguments.add(new BasicNameValuePair( "Action", "TntAddrList"));
        arguments.add(new BasicNameValuePair( "Username", account.getString("username")));
        arguments.add(new BasicNameValuePair( "Password", account.getString("password")));

        // Always download contacts from all time, since TNTDataService only
        // gives donor info for donors who are new TO THE ORGANISATION,
        // not new to the user. So if I gain a new supporter who has 
        // already given to someone else, they will not show up in my donor
        // query unless we query from before their most recent address
        // change. Annoying, but donor import is much faster than gift
        // import, so it doesn't really cause much of a slow-down.
        arguments.add(new BasicNameValuePair( "DateFrom", EPOCH_DATE));

        ArrayList<String> content = getStringsFromUrl(service.getString("base_url") + service.getString("addresses_url"), arguments);
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

        arguments.add(new BasicNameValuePair( "Action", "TntDonList"));
        arguments.add(new BasicNameValuePair( "Username", account.getString("username")));
        arguments.add(new BasicNameValuePair( "Password", account.getString("password")));

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

        ArrayList<String> content = getStringsFromUrl(service.getString("base_url") + service.getString("donations_url"), arguments);
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

        if (contacts.size() == 0){
            // TODO: evaluate if address has changed.
            Address address = new Address();
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
            //address.setValue("valid_from", values["ADDR_CHANGED"]);
            //address.setValue("deliverable", values["ADDR_DELIVERABLE"]);
            address.setValue("contact_id", contact);
            address.dirtySave();

            PhoneNumber phone = new PhoneNumber();
            phone.setValue("number", data.get("PHONE"));
            //phone.setValue("added", values["PHONE_CHANGED"]);
            //phone.setValue("operational", values["PHONE_OPERATIONAL"]);
            phone.setValue("label", "Default");
            phone.setValue("contact_id", contact);
            phone.dirtySave();

            EmailAddress email = new EmailAddress();
            email.setValue("address", data.get("EMAIL"));
            //email.setValue("added_date", values["EMAIL_CHANGED"]);
            //email.setValue("operational", values["EMAIL_OPERATIONAL"]);
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

    public ArrayList<String> getStringsFromUrl(String url, ArrayList arguments){
        return getStringsFromUrl(url, arguments, true);
    }

    public ArrayList<String> getStringsFromUrl(String url, ArrayList arguments, boolean handleCertError){
        Log.i("net.bradmont.openmpd", "getStringsFromUrl: " + url);

        InputStream stream = getStreamFromUrl(url, arguments, handleCertError);
        if (stream == null){
            return null;
        }

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(stream), 4096);
        //Log.i("net.bradmont.openmpd", "InputStream & BufferedReader initialized");
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
        return lines;
    }

    public InputStream getStreamFromUrl(String url_raw, ArrayList arguments, boolean handleCertError){
        InputStream content = null;
        
        URL url = null;
        try {
            url = new URL(url_raw);
        } catch (Exception e){
            return null;
        }
        try {
            DefaultHttpClient httpclient = null;
            SharedPreferences prefs = context.getSharedPreferences("openmpd", Context.MODE_PRIVATE);
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

            if (builder != null){
                builder.setContentText(url.getHost());
                notifyManager.notify(notification_id, builder.build());
            }

            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(targetHost.getHostName(), targetHost.getPort()), 
                    new UsernamePasswordCredentials(
                        ((BasicNameValuePair) arguments.get(1)).getValue(), // username
                        ((BasicNameValuePair) arguments.get(2)).getValue()
                        ));

            HttpPost httpPost = new HttpPost(url_raw);
            httpPost.setEntity(new UrlEncodedFormEntity(arguments));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httpPost);
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
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_icon);

            builder.setContentTitle("SSL Certificate Error");
            builder.setContentText(url.getHost());

            Intent sslCertIntent = new Intent(context, OpenMPD.class);
            sslCertIntent.putExtra("net.bradmont.openmpd.SSLErrorServer", url.getHost());

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(OpenMPD.class);
            stackBuilder.addNextIntent(sslCertIntent);
            PendingIntent sslPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(sslPendingIntent);


            notificationManager.notify(666, builder.build());
            return null;



        } catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.i("net.bradmont.openmpd.controllers.TntImporter",sw.toString());
        }
        return content;
    }
    public DefaultHttpClient getNewHttpClient() {
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



    public class MySSLSocketFactory extends SSLSocketFactory {
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
