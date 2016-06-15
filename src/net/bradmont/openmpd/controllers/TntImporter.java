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
    private HashMap<String, String> data ;
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
        data = new HashMap<String, String>(headers.length);

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
        ImportActivity.setStatus(mAccount.getID(), R.string.done);
        return true;
    }

###
    public void parseGiftLine(String [] headers, String line){
        String [] values = csvLineSplit(line);
        for (int i=0; i < headers.length; i++){
            data.put(headers[i], values[i]);
            //Log.i("net.bradmont.openmpd", "" + headers[i] + ":'" + values[i]+"'");
        }

        // if this gift has already been imported, skip it.
        GiftDao giftDao = OpenMPD.getDaoSession().getGiftDao();
        List<net.bradmont.openmpd.dao.Gift> gifts = giftDao.queryBuilder()
            .where(GiftDao.Properties.TntDonationId.eq(data.get("DONATION_ID")))
                    .list();
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


        ContactDao contactDao = OpenMPD.getDaoSession().getContactDao();
        List<net.bradmont.openmpd.dao.Contact> contacts = contactDao.queryBuilder()
            .where(ContactDao.Properties.TntPeopleId.eq(data.get("PEOPLE_ID")))
            .where(ContactDao.Properties.SubId.eq("0"))
            .list();
        net.bradmont.openmpd.dao.Contact contact;
        if (contacts.size() != 0){
            contact = contacts.get(0);
        } else {
            contact = new net.bradmont.openmpd.dao.Contact();
        }
        contact.setTntPeopleId(data.get("PEOPLE_ID")); /// TODO: append account key
        contact.setTntAccountName(data.get("ACCT_NAME"));
        contact.setTntPersonType(data.get("PERSON_TYPE"));
        contact.setLname(data.get("LAST_NAME_ORG"));
        contact.setFname(data.get("FIRST_NAME"));
        contact.setMname(data.get("MIDDLE_NAME"));
        contact.setTitle(data.get("TITLE"));
        contact.setSuffix(data.get("SUFFIX"));
        //contact.setValue("account", account);

        if (contact.getFname() != "" || contact.getLname() != ""){
            contact.dirtySave();
            // contactDao.insert/update
            // TODO
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
            spouse.setValue("account", mAccount);
            if ((spouse.getString("fname") != "" && spouse.getString("fname") != null )
                    || (spouse.getString("lname") != "" && spouse.getString("lname") != null)){
                spouse.dirtySave();
                contact.setValue("spouse", spouse);
            } else {
                contact.setValue("spouse_id", 0);
                spouse.delete();
            }
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
        // TODO: verify query.ini is up to date 

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
        InputStream stream = getStreamFromUrl(url_raw, arguments, handleCertError, builder, notifyManager, mAccount.getID());
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
        return getStreamFromUrl(url_raw, arguments, handleCertError, null, null, -1);
    }

    public static InputStream getStreamFromUrl(String url_raw, ArrayList arguments, boolean handleCertError, NotificationCompat.Builder builder, NotificationManager notifyManager, int accountId){
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
            ImportActivity.setProgress(accountId, 0, 0, false);
            ImportActivity.setStatus(accountId, R.string.error_connecting_to_server);
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
            ImportActivity.setStatus(accountId, R.string.ssl_certificate_error);

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
