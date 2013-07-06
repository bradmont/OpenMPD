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

        addField(new IntField("tnt_people_id"));
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
        args[0] = Integer.toString(getInt("tnt_people_id"));
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
        ContactStatus oldStatus = (ContactStatus)MPDDBHelper
                .getReferenceModel("contact_status")
                .getByField("contact_id", getInt("id"));


        int partner = evaluate(giftPattern, cs);
        cs.setValue("partner_type", partner);
        cs.dirtySave();

        // create notifications for changes in status
        if (initialImport == false){
            Notification note = new Notification();
            note.setValue("contact", this);
            if (oldStatus == null){
                note.setValue("type", Notification.CHANGE_PARTNER_TYPE);
                note.dirtySave();
            } else if (oldStatus.getInt("partner_type") != cs.getInt("partner_type")){
                note.setValue("type", Notification.CHANGE_PARTNER_TYPE);
                note.dirtySave();
            } else if (oldStatus.getInt("status") != cs.getInt("status")){
                note.setValue("type", Notification.CHANGE_STATUS);
                note.setValue("text", Integer.toString(oldStatus.getInt("status")));
                note.dirtySave();
            } else if (oldStatus.getInt("giving_amount") != cs.getInt("giving_amount")){
                note.setValue("type", Notification.CHANGE_AMOUNT);
                note.setValue("text", Integer.toString(oldStatus.getInt("giving_amount")));
                note.dirtySave();
            }
            // we want to allow getting 2 notifications, eg, for a new one-time donor
            // we'll notify "new donor" and "gave a special gift".
            note = new Notification();
            note.setValue("contact", this);
            int monthAmount = getMonthAmount();
            if (monthAmount != 0){
                if (cs.getInt("partner_type") == ContactStatus.PARTNER_OCCASIONAL 
                    || cs.getInt("partner_type") == ContactStatus.PARTNER_ONETIME ){
                    note.setValue("type", Notification.SPECIAL_GIFT);
                    note.setValue("text", Integer.toString(monthAmount));
                    note.dirtySave();
                } else if (monthAmount != getInt("giving_amount")){
                    note.setValue("type", Notification.SPECIAL_GIFT);
                    note.setValue("text", Integer.toString(monthAmount));
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
                    cs.setValue("giving_amount", getGivingAmount());
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
            "select _id, sum(amount) as amount from gift "+
            "    where tnt_people_id=? "+
            "    group by month " +
            "    order by date desc "+
            "    limit 5; ";

        String [] args = new String [1];
        args[0] = Integer.toString(getInt("tnt_people_id"));

        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery( SQL, args);
        if (cur.getCount() == 0){
            return 0;
        }
        cur.moveToFirst();
        int mode = cur.getInt(0);
        cur.close();

        return mode;
    }
    private int getGivingAmount(){
        // giving amount is mode of last 5 months' gift totals
        // SQL groups last 5 months by amount, sorts by the number
        // of gifts of that amount.
        // First record in cursor holds the most common of the last 5
        // gifts (the mode). 
        String SQL = 
            " select amount, count(_id) as gift_count " +
            "   from "+
            "       (select _id, sum(amount) as amount from gift "+
            "           where tnt_people_id=? "+
            "           group by month " +
            "           order by date desc "+
            "           limit 5) "+
            "   group by amount "+
            "   order by gift_count desc;";

        String [] args = new String [1];
        args[0] = Integer.toString(getInt("tnt_people_id"));

        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery( SQL, args);
        if (cur.getCount() == 0){
            return 0;
        }
        cur.moveToFirst();
        int mode = cur.getInt(0);
        cur.close();

        return mode;
    }
}
