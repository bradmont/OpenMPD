    package net.bradmont.openmpd.dao;

    // THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

    // KEEP INCLUDES - put your custom includes here
    import android.app.NotificationManager;
    import android.app.PendingIntent;
    import android.content.Intent;
    import android.util.Log;
    import android.content.Context;
    import android.content.SharedPreferences;
    import android.support.v4.app.NotificationCompat;
    import android.support.v4.app.TaskStackBuilder;

    import java.util.ArrayList;
    import java.io.BufferedReader;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.io.IOException;
    import java.io.PrintWriter;
    import java.io.StringReader;
    import java.io.StringWriter;
    import java.math.BigDecimal;
    import java.net.Socket;
    import java.net.URL;
    import java.net.UnknownHostException;
    import java.security.KeyManagementException;
    import java.security.KeyStore;
    import java.security.KeyStoreException;
    import java.security.NoSuchAlgorithmException;
    import java.security.UnrecoverableKeyException;
    import java.security.cert.CertificateException;
    import java.security.cert.X509Certificate;
    import java.text.DateFormat;
    import java.text.DecimalFormat;
    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import javax.net.ssl.SSLContext;
    import javax.net.ssl.TrustManager;
    import javax.net.ssl.X509TrustManager;

    import org.apache.http.*;
    import org.apache.http.auth.*;
    import org.apache.http.client.methods.*;
    import org.apache.http.impl.client.*;
    import org.apache.http.client.entity.*;
    import org.apache.http.conn.scheme.SchemeRegistry;
    import org.apache.http.conn.scheme.Scheme;
    import org.apache.http.conn.ClientConnectionManager;
    import org.apache.http.message.*;
    import org.apache.http.conn.HttpHostConnectException;
    import org.apache.http.impl.conn.SingleClientConnManager;
    import org.apache.http.conn.ssl.SSLSocketFactory;
    import org.apache.http.conn.scheme.PlainSocketFactory;
    import org.apache.http.params.BasicHttpParams;
    import org.apache.http.params.HttpParams;
    import org.apache.http.params.HttpProtocolParams;
    import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
    import org.apache.http.protocol.HTTP;

    import org.ini4j.Ini;

    import net.bradmont.openmpd.*;
    import net.bradmont.openmpd.activities.*;
    import net.bradmont.openmpd.helpers.TextTools;
    // KEEP INCLUDES END
    /**
     * Entity mapped to table "TNT_SERVICE".
     */
    public class TntService {

        private Long id;
        private String name;
        private String nameShort;
        private String domain;
        private Boolean httpAuth;
        private String balanceUrl;
        private String balanceFormdata;
        private String donationsUrl;
        private String donationsFormdata;
        private String addressesUrl;
        private String addressesFormdata;
        private String addressesByPersonidsUrl;
        private String addressesByPersonidsFormdata;
        private String queryIniUrl;

        // KEEP FIELDS - put your custom fields here
        private boolean mQueryIniProcessed = false;
        private static String EPOCH_DATE="01/01/1970";
        // KEEP FIELDS END

        public TntService() {
        }

        public TntService(Long id) {
            this.id = id;
        }

        public TntService(Long id, String name, String nameShort, String domain, Boolean httpAuth, String balanceUrl, String balanceFormdata, String donationsUrl, String donationsFormdata, String addressesUrl, String addressesFormdata, String addressesByPersonidsUrl, String addressesByPersonidsFormdata, String queryIniUrl) {
            this.id = id;
            this.name = name;
            this.nameShort = nameShort;
            this.domain = domain;
            this.httpAuth = httpAuth;
            this.balanceUrl = balanceUrl;
            this.balanceFormdata = balanceFormdata;
            this.donationsUrl = donationsUrl;
            this.donationsFormdata = donationsFormdata;
            this.addressesUrl = addressesUrl;
            this.addressesFormdata = addressesFormdata;
            this.addressesByPersonidsUrl = addressesByPersonidsUrl;
            this.addressesByPersonidsFormdata = addressesByPersonidsFormdata;
            this.queryIniUrl = queryIniUrl;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNameShort() {
            return nameShort;
        }

        public void setNameShort(String nameShort) {
            this.nameShort = nameShort;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public Boolean getHttpAuth() {
            return httpAuth;
        }

        public void setHttpAuth(Boolean httpAuth) {
            this.httpAuth = httpAuth;
        }

        public String getBalanceUrl() {
            return balanceUrl;
        }

        public void setBalanceUrl(String balanceUrl) {
            this.balanceUrl = balanceUrl;
        }

        public String getBalanceFormdata() {
            return balanceFormdata;
        }

        public void setBalanceFormdata(String balanceFormdata) {
            this.balanceFormdata = balanceFormdata;
        }

        public String getDonationsUrl() {
            return donationsUrl;
        }

        public void setDonationsUrl(String donationsUrl) {
            this.donationsUrl = donationsUrl;
        }

        public String getDonationsFormdata() {
            return donationsFormdata;
        }

        public void setDonationsFormdata(String donationsFormdata) {
            this.donationsFormdata = donationsFormdata;
        }

        public String getAddressesUrl() {
            return addressesUrl;
        }

        public void setAddressesUrl(String addressesUrl) {
            this.addressesUrl = addressesUrl;
        }

        public String getAddressesFormdata() {
            return addressesFormdata;
        }

        public void setAddressesFormdata(String addressesFormdata) {
            this.addressesFormdata = addressesFormdata;
        }

        public String getAddressesByPersonidsUrl() {
            return addressesByPersonidsUrl;
        }

        public void setAddressesByPersonidsUrl(String addressesByPersonidsUrl) {
            this.addressesByPersonidsUrl = addressesByPersonidsUrl;
        }

        public String getAddressesByPersonidsFormdata() {
            return addressesByPersonidsFormdata;
        }

        public void setAddressesByPersonidsFormdata(String addressesByPersonidsFormdata) {
            this.addressesByPersonidsFormdata = addressesByPersonidsFormdata;
        }

        public String getQueryIniUrl() {
            return queryIniUrl;
        }

        public void setQueryIniUrl(String queryIniUrl) {
            this.queryIniUrl = queryIniUrl;
        }

        // KEEP METHODS - put your custom methods here
        
        /**
         * returns account balance in cents
         */
        public int getBalance(String username, String password) throws ServerException{
            ArrayList<BasicNameValuePair> arguments = new ArrayList<BasicNameValuePair>(4);

            arguments.add(new BasicNameValuePair( "Action", getBalanceAction()));
            arguments.add(new BasicNameValuePair( getUsernameKey(), username));
            arguments.add(new BasicNameValuePair( getPasswordKey(), password));
            ArrayList<String> content = 
                getStringsFromUrl(getBalanceUrl(), arguments, false);
            if (content.size() == 1){
                // Query was succesful, but no result returned
                // this happens, eg, for CRU US accounts that redirect funds to
                // an international ministry
                return 0;
            }
            String [] headers = TextTools.csvLineSplit(content.get(0));
            String [] values = TextTools.csvLineSplit(content.get(1));
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

        /**
         * Execute the Addresses TNT query to download contacts.
         */
        public ArrayList<String> getAddresses(String username, String password) throws ServerException{
            ArrayList<BasicNameValuePair> arguments = new ArrayList<BasicNameValuePair>(4);
            arguments.add(new BasicNameValuePair( "Action", getAddressesAction()));
            arguments.add(new BasicNameValuePair( getUsernameKey(), username));
            arguments.add(new BasicNameValuePair( getPasswordKey(), password));

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
            content = getStringsFromUrl(getAddressesUrl(), arguments);
        } catch (ServerException e){
            return null;
        }
        return content;
    }

    /**
     * Execute the Addresses TNT query to download gifts.
     */
    public ArrayList<String> getGifts(String username, String password, java.util.Date fromDate) throws ServerException{

        ArrayList<BasicNameValuePair> arguments = new ArrayList<BasicNameValuePair>(4);

        arguments.add(new BasicNameValuePair( "Action", getDonationsAction()));
        arguments.add(new BasicNameValuePair( getUsernameKey(), username));
        arguments.add(new BasicNameValuePair( getPasswordKey(), password));

        if (fromDate == null){
            arguments.add(new BasicNameValuePair( "DateFrom", EPOCH_DATE));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.add(Calendar.DAY_OF_MONTH, -14); // gifts can take a while to 
                                                        // appear in the system

            DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            arguments.add(new BasicNameValuePair( "DateFrom", 
                format.format(cal.getTime())
            ));
        }

        arguments.add(new BasicNameValuePair( "DateTo", TextTools.stupidDateFormat(TextTools.getToday())));

        ArrayList<String> content = null;
        try {
            content = getStringsFromUrl(getDonationsUrl(), arguments);
        } catch (ServerException e){
            return null;
        }
        return content;
    }

    /**
     * Find the current query.ini.
     * query.ini URLs in our master service list are not always up to date.
     * query.ini files provide a forwarding mechanism if organisations move
     * their TntDataServer; this method traverses query.ini forwards to find
     * the up-to-date one and saves updated service information.
     */
    public boolean processQueryIni() throws ServerException{
        if (mQueryIniProcessed == true) { return true;}
        Ini ini = null;
        StringReader reader = null;
        try {
            URL u = new URL(getQueryIniUrl());
        } catch (Exception e){
            Log.i("net.bradmont.openmpd", "malformed query.ini url");
            return false;
        }
        try {
            reader = getCleanedStringReaderFromUrl(getQueryIniUrl());
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
            Log.i("net.bradmont.openmpd", getQueryIniUrl());
            StringBuilder sb = new StringBuilder();
            try {
                reader = getCleanedStringReaderFromUrl(getQueryIniUrl());
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
            setQueryIniUrl(org.get("RedirectQueryIni"));
            return processQueryIni();
        }

        if (org.containsKey("Name")){
            setName(org.get("Name"));
        }
        if (org.containsKey("Code")){
            setNameShort(org.get("Code"));
        }
        if (org.containsKey("QueryAuthentication") && org.get("QueryAuthentication") == "1"){
            setHttpAuth(true);
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

        setBalanceUrl(balance.get("Url"));
        setBalanceFormdata(balance.get("Post"));
        try {
            setDomain(new URL(getBalanceUrl()).getHost());
        } catch (Exception e){
            // if URL is malformed, account won't authenticate anyway
        }

        setDonationsUrl(donations.get("Url"));
        setDonationsFormdata(donations.get("Post"));

        setAddressesUrl(addresses.get("Url"));
        setAddressesFormdata(addresses.get("Post"));

        setAddressesByPersonidsUrl(addresses_by_personids.get("Url"));
        setAddressesByPersonidsFormdata(addresses_by_personids.get("Post"));
        OpenMPD.getDaoSession().getTntServiceDao().update(this);

        mQueryIniProcessed = true;
        return true; 
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

        InputStream stream = getStreamFromUrl(url_raw, arguments, handleCertError);
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

    public static class ServerException extends Exception{
        public ServerException() { super(); }
        public ServerException(String message) { super(message); }
        public ServerException(String message, Throwable cause) { super(message, cause); }
        public ServerException(Throwable cause) { super(cause); }
    }


    private static StringReader getCleanedStringReaderFromUrl(String url) throws ServerException{
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

    public static InputStream getStreamFromUrl(String url_raw, ArrayList arguments, boolean handleCertError) throws ServerException{
        return getStreamFromUrl(url_raw, arguments, handleCertError, -1);
    }

    public static InputStream getStreamFromUrl(String url_raw, ArrayList arguments, boolean handleCertError,  int accountId) throws ServerException{
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
            ImportActivity.setProgress(accountId, 0, 0, false);
            ImportActivity.setStatus(accountId, R.string.error_connecting_to_server);
            throw new ServerException("Error connecting to server.");
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

     private String [] [] getArgs(String formdata){
        String [] pairs = formdata.split("&");
        String [] [] results = new String [pairs.length][];
        for (int i = 0; i < pairs.length; i++){
            results[i] = pairs[i].split("=");
        }
        return results;
     }


     private String getArgNameByValue(String formdata, String value){
        String [] [] args = getArgs(formdata);
        for (int i = 0; i < args.length; i++){
            if (args[i][1].equals(value)){
                return args[i][0];
            }
        }
        return null;
     }

     private String getValueByArgName(String formdata, String key){
        String [] [] args = getArgs(formdata);
        for (int i = 0; i < args.length; i++){
            if (args[i][0].equals(key)){
                return args[i][1];
            }
        }
        return null;
     }
     
     public String getUsernameKey(){
        return getArgNameByValue(getBalanceFormdata(), "$ACCOUNT$");
     }
     public String getPasswordKey(){
        return getArgNameByValue(getBalanceFormdata(), "$PASSWORD$");
     }

     public String getBalanceAction(){
        return getValueByArgName(getBalanceFormdata(), "Action");
     }

     public String getDonationsAction(){
        return getValueByArgName(getDonationsFormdata(), "Action");
     }

     public String getAddressesAction(){
        return getValueByArgName(getAddressesFormdata(), "Action");
     }

     public String getAddressesByPersonidsAction(){
        return getValueByArgName(getAddressesByPersonidsFormdata(), "Action");
     }
    // KEEP METHODS END

}
