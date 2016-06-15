package net.bradmont.openmpd.dao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
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
            getStringsFromUrl(getBaseUrl() + getBalanceUrl(), arguments, false);
        if (content.size() == 1){
            // Query was succesful, but no result returned
            // this happens, eg, for CRU US accounts that redirect funds to
            // an international ministry
            return 0;
        }
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

    /**
     * Execute the Addresses TNT query to download contacts.
     */
    public ArrayList<String> getAddresses(String username, String password){
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
            content = getStringsFromUrl(service.getString("addresses_url"), arguments);
        } catch (ServerException e){
            return false;
        }
        return content;
    }

    /**
     * Execute the Addresses TNT query to download gifts.
     */
    public ArrayList<String> getGifts(String username, String password, java.util.Date fromDate){

        ArrayList<BasicNameValuePair> arguments = new ArrayList<BasicNameValuePair>(4);

        arguments.add(new BasicNameValuePair( "Action", getDonationsAction()));
        arguments.add(new BasicNameValuePair( getUsernameKey(), username));
        arguments.add(new BasicNameValuePair( getPasswordKey(), password));

        if (dateFrom == null){
            arguments.add(new BasicNameValuePair( "DateFrom", EPOCH_DATE));
        } else {
            Calendar cal = Calendar.getInstance().setTime(dateFrom);
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
            content = getStringsFromUrl(getDonationsUrl(), arguments);
        } catch (ServerException e){
            return false;
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
    public static boolean processQueryIni(TntService service){
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
            return processQueryIni(service);
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

        setDonationsUrl(donations.get("Url"));
        setDonationsFormdata(donations.get("Post"));

        setAddressesUrl(addresses.get("Url"));
        setAddressesFormdata(addresses.get("Post"));

        setAddressesByPersonidsUrl(addresses_by_personids.get("Url"));
        setAddressesByPersonidsFormdata(addresses_by_personids.get("Post"));
        OpenMPD.getDaoSession().getServiceAccountDao().update(this);

        mQueryIniProcessed = true;
        return true; 
    }
    // KEEP METHODS END

}
