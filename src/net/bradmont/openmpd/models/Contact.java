package net.bradmont.openmpd.models;
import net.bradmont.openmpd.*;

import net.bradmont.supergreen.*;
import net.bradmont.supergreen.fields.*;
import net.bradmont.supergreen.fields.constraints.*;
import net.bradmont.supergreen.models.DBModel;
import net.bradmont.supergreen.models.ModelList;

import android.database.Cursor;
import android.database.sqlite.*;
import android.app.Activity;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.SimpleCursorAdapter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Set;
import java.util.Calendar;



public class Contact extends DBModel{
    public static final String TABLE = "contact";
    private static Contact reference_instance = null;

    public Contact(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public Contact(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }

    @Override
    public DBModel newInstance(){
        return new Contact();
    }

    @Override
    public DBModel newInstance(int id){
        return new Contact(id);
    }
    public static Contact getReferenceInstance(){
        if (reference_instance == null){
            reference_instance = new Contact();
        }
        return reference_instance;
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new StringField("tnt_people_id"));
        getField("tnt_people_id").setExtraArguments("unique");
        addField(new StringField("tnt_account_name"));
        addField(new StringField("tnt_person_type"));

        addField(new StringField("lname"));
        addField(new StringField("fname"));
        addField(new StringField("mname"));
        addField(new StringField("title"));
        addField(new StringField("suffix"));
        addField(new DateField("last_updated"));
        addField(new BooleanField("primary_contact"));
        getField("primary_contact").setDefault(true);

        addField(new ForeignKeyField("spouse", this)); // don't want to make an infinite recursion loop, do we?
        getField("spouse").setColumnName("spouse_id");
        addField(new ForeignKeyField("account", MPDDBHelper.get().getReferenceModel("service_account"))); // don't want to make an infinite recursion loop, do we?
        getField("account").setColumnName("account_id");

        TABLE_NAME=TABLE;
        super.init();
    }

    /** This really should be in a controller, not the model...
      */
    public void updateStatus(boolean initialImport){
        // build a string that matches \d+, where each character
        // is the number of gifts in a given month. eg: 
        // a monthly partner will be 111111111...
        // a quarterly partner may be 10010010010010...
        // a dropped monthly may be 1111110000...
        // a monthly with special gifts may be 11111211121111
        String SQL = 
        "Select 'true' as grouper, group_concat(gift_count,'') " +
        "   from " +
        "       (select b.tnt_people_id as id, a.month, min(count(b.amount),9) as gift_count " +
        "           from "+
        "               (select distinct month from gift) a "+
        "               left outer join "+
        "               (select * from gift where tnt_people_id=?) b "+
        "               on a.month=b.month "+
        "           group by  a.month "+
        "           order by a.month) "+
        "   group by grouper;";

        String [] args = new String [1];
        args[0] = getString("tnt_people_id");
        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery( SQL, args);
        cur.moveToFirst();
        String giftPattern = cur.getString(1);
        cur.close();

        // check if this contact already has an associated ContactStatus
        ContactStatus cs = (ContactStatus)MPDDBHelper
                .getReferenceModel("contact_status")
                .getByField("contact_id", getInt("id"));

        if (cs == null){
            cs = new ContactStatus();
        }

        cs.setValue("contact_id", this);

        // a copy for comparison
        ContactStatus oldStatus = null;
        if (initialImport == true ){
            // If this is our first data import, we evaluate twice, once
            // with data from two months ago, and again with the latest
            // data, then compare the two to generate notifications. This
            // allows us to give recent notifications for new users,
            // without flooding them with years of history.
            oldStatus = new ContactStatus();
            int partner = 0;
            if (giftPattern.length() > 2){
                String oldPattern = giftPattern.substring(0, giftPattern.length()-3);
                partner = evaluate(oldPattern, oldStatus);
                oldStatus.setValue("partner_type", partner);
            }  else {
                // if the user has less than 2 months history, they wont
                // get any notifications.
                partner = evaluate(giftPattern, oldStatus);
                oldStatus.setValue("partner_type", partner);
            }
        } else {
            oldStatus = (ContactStatus)MPDDBHelper
                    .getReferenceModel("contact_status")
                    .getByField("contact_id", getInt("id"));
        }


        int partner = evaluate(giftPattern, cs);
        cs.setValue("partner_type", partner);
        cs.dirtySave();

        // create notifications for changes in status (unless we're importing
        // data for a new account)
        Notification note = new Notification();
        note.setValue("contact", this);
        if (oldStatus == null){
            if (cs.getInt("partner_type") != ContactStatus.PARTNER_NONE){
                note.setValue("type", Notification.CHANGE_PARTNER_TYPE);
                note.dirtySave();
            }
        } else if (oldStatus.getInt("partner_type") != cs.getInt("partner_type")){
            note.setValue("type", Notification.CHANGE_PARTNER_TYPE);
            note.setValue("message", Integer.toString(oldStatus.getInt("partner_type")));
            note.dirtySave();
        } else if (oldStatus.getInt("status") != cs.getInt("status")){
            note.setValue("type", Notification.CHANGE_STATUS);
            note.setValue("message", Integer.toString(oldStatus.getInt("status")));
            note.dirtySave();
        } else if (oldStatus.getInt("giving_amount") != cs.getInt("giving_amount") && 
                (cs.getInt("partner_type") == ContactStatus.PARTNER_MONTHLY ||
                 cs.getInt("partner_type") == ContactStatus.PARTNER_REGULAR ||
                 cs.getInt("partner_type") == ContactStatus.PARTNER_ANNUAL)
                && cs.getInt("giving_amount") != 0
                ){
            note.setValue("type", Notification.CHANGE_AMOUNT);
            note.setValue("message", Integer.toString(oldStatus.getInt("giving_amount")));
            note.dirtySave();
        }

        // check if we've already made a notification for this partner's last gift
        SQL = "select date from gift where tnt_people_id=? order by date desc limit 1;";
        String lastGift = "";
        cur = MPDDBHelper.get().getReadableDatabase().rawQuery( SQL, args);
        if (cur.getCount() > 0){
            cur.moveToFirst();
            lastGift = cur.getString(0);
        }
        cur.close();

        if (oldStatus == null || !lastGift.equals(oldStatus.getString("last_notify"))){
            cs.setValue("last_notify", lastGift);
            cs.dirtySave();
            note.setValue("contact", this);
            int monthAmount = getMonthAmount();
            if (monthAmount != 0){
                if (cs.getInt("partner_type") == ContactStatus.PARTNER_OCCASIONAL 
                    || cs.getInt("partner_type") == ContactStatus.PARTNER_ONETIME ){
                    note.setValue("type", Notification.SPECIAL_GIFT);
                    note.setValue("message", Integer.toString(monthAmount));
                    note.dirtySave();
                } else if (monthAmount != cs.getInt("giving_amount")){
                    note.setValue("type", Notification.SPECIAL_GIFT);
                    note.setValue("message", Integer.toString(monthAmount));
                    note.dirtySave();
                }
            }
        }
    }

    private int evaluate(String giftPattern, ContactStatus cs){
        cs.setValue("notes", giftPattern);
        for (int i = 0; i < ContactStatus.REGEXES.length; i++){
            String regex = ContactStatus.REGEXES[i];
            if (!regex.startsWith("^")){
                regex = ".*" + regex;
            }
            if (giftPattern.matches(regex)){
                cs.setValue("partner_type", ContactStatus.STATUSES[i][0]);
                cs.setValue("gift_frequency", ContactStatus.STATUSES[i][1]);
                if (ContactStatus.STATUSES[i][2] == 1) {
                    boolean current = false;
                    if (ContactStatus.STATUSES[i][3] == ContactStatus.STATUS_CURRENT
                        ||ContactStatus.STATUSES[i][3] == ContactStatus.STATUS_NEW){
                            current = true;
                        }
                    cs.setValue("giving_amount", getGivingAmount(current));
                }
                cs.setValue("status", ContactStatus.STATUSES[i][3]);
                return ContactStatus.STATUSES[i][0];
            }
        }
        return ContactStatus.PARTNER_NONE;
    }

    /**
      * Get the amount of the latest month's givings
      */
    private int getMonthAmount(){
        String SQL = 
            " select distinct month from gift order by month desc limit 2";
        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery( SQL, null);
        String [] args = null;
        if (cur.getCount() > 1){
            cur.moveToPosition(1);
            String lastMonth = cur.getString(0);
            SQL = "select month, sum(amount) from gift "+
                  "     where tnt_people_id=? and month >= ? "+
                  "     group by month "+
                  "     order by month desc;";
            args = new String [2];
            args[0] = getString("tnt_people_id");
            args[1] = lastMonth;
        }  else {
            SQL = "select month, sum(amount) from gift "+
                  "     where tnt_people_id=? "+
                  "     group by month "+
                  "     order by month desc;";
            args = new String [1];
            args[0] = getString("tnt_people_id");
        }

        cur.close();

        cur = MPDDBHelper.get().getReadableDatabase().rawQuery( SQL, args);
        if (cur.getCount() == 0){
            cur.close();
            return 0;
        }
        cur.moveToFirst();
        int result = cur.getInt(1);
        if (result == 0){
            // check last two months, as end-of-month gifts can show up when there's already
            // gifts from the next month
            cur.moveToNext();
            result = cur.getInt(1);
        }
        cur.close();

        return result;
    }
    private int getGivingAmount(boolean current){
        // giving amount is mode of last 5 months' gift totals
        // SQL groups last 5 months by amount, sorts by the number
        // of gifts of that amount.
        // First record in cursor holds the most common of the last 5
        // gifts (the mode). 
        String SQL;
        if (current == false){
            SQL = 
                " select amount, count(_id) as gift_count " +
                "   from "+
                "       (select _id, sum(amount) as amount from gift "+
                "           where tnt_people_id=? "+
                "           group by month " +
                "           order by date desc "+
                "           limit 5) "+
                "   group by amount "+
                "   order by gift_count desc;";
        }else {
            // if the donor is current, we have to account for
            // periods of inactivity, in case a donor drops then
            // resumes at a different amount. We cannot do this
            // for late, lapsed or dropped donors, as it will
            // change their giving_amount to 0
            SQL = 
                "select amount, count(month) as gift_count from "+
                       "(select months.month, amount from  "+
                       "(select distinct month from gift) months "+
                       "left outer join "+
                       "(select month, sum(amount) as amount  "+
                        "from gift "+
                        "where tnt_people_id=? "+
                        "group by month) B "+
                        "on months.month=B.month "+
                        "order by months.month desc "+
                        "limit 5) "+
                    "group by amount "+
                    "order by amount is null, gift_count desc;";
        }
        String [] args = new String [1];
        args[0] = getString("tnt_people_id");

        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery( SQL, args);
        if (cur.getCount() == 0){
            return 0;
        }
        cur.moveToFirst();
        int mode = cur.getInt(0);
        cur.close();

        return mode;
    }
    @Override
    public String [] generateUpdateSQL(int oldVersion){
        if (oldVersion < 7){
            String [] sqls = {"alter table contact add account_id int;"};
            return sqls;
        }
        return null;
    }
}
