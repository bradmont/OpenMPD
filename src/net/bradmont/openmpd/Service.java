package net.bradmont.openmpd;

public class Service {
    public static String name = "Power to Change";
    public static String abbreviation = "P2C";
    public static boolean http_auth = true;
    private static String BASE_URL="https://focus.powertochange.org/tntmpd/";

    public static String balance_url = BASE_URL + "account_balance.php";
    public static String balance_formdata = "Action=TntBalance&Username=%s&Password=%s";

    public static String donations_url = BASE_URL + "donordata.php";
    public static String donations_formdata = "Action=TntDonList&Username=%s&Password=%s&DateFrom=%s&DateTo=%s";

    public static String addresses_url = BASE_URL + "donordata.php";
    public static String addresses_formdata = "Action=TntAddrList&Username=%s&Password=%s&DateFrom=%s";

    public static String addresses_by_personids_url = BASE_URL + "donordata.php";
    public static String addresses_by_personids_formdata = "Action=TntAddrList&Username=%s&Password=%s&PID=%s";
}
