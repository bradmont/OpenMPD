package net.bradmont.openmpd.models;
import net.bradmont.openmpd.*;

import net.bradmont.supergreen.*;
import net.bradmont.supergreen.fields.*;
import net.bradmont.supergreen.fields.constraints.*;
import net.bradmont.supergreen.models.DBModel;

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
import java.util.Date;



public class ContactStatus extends DBModel{
    public static final String TABLE = "contact_status";
    public static final int PARTNER_MONTHLY = 6;
    public static final int PARTNER_REGULAR = 5;
    public static final int PARTNER_ANNUAL = 4;
    public static final int PARTNER_OCCASIONAL = 3;
    public static final int PARTNER_ONETIME = 2;
    public static final int PARTNER_NONE = 1;
    public static final int PARTNER_UNKNOWN = 0;

            
            // current, late, lapsed, dropped, none
    public static final int STATUS_CURRENT = 5;
    public static final int STATUS_NEW = 4;
    public static final int STATUS_LATE = 3;
    public static final int STATUS_LAPSED = 2;
    public static final int STATUS_DROPPED = 1;
    public static final int STATUS_NONE = 0;

    public static final int [] STATUS_COLORS = {
        0xFF707070,  // none
        0xFFA0A0A0,  // dropped
        0xFFFF4444,  // lapsed
        0xFFFFBB33,  // late
        0xFF33B5E5,  // new
        0xFF99CC00  // current
    };

    // parallel arrays for regex & the values it'll result in
    // kinda ugly to do this way, but I can't think of a better option
    public static final String [] REGEXES = {
        // Monthly partners:
        "000[1-9]{2,4}0?$", // Gifts in last two months (new partner)
        "[1-9]{3}0?$", // gifts in last three months
        "[1-9]{2}0[1-9]{2}0?$",
        "[1-9]{3}0[1-9]0?$", // missed one of last 5 months
        "[1-9]{4}0{2,4}$", 
        "[1-9]{4}0{5,12}$",
        "[1-9]{4}0{13}0*$",
        // Regular Partners
        "(01){3}0?$",
        "(001){3}0{0,3}$",
        "(0001){3}0{0,4}$",
        "(000001){3}0{0,6}$",
        // Regular, Late
        "(01){3}0{3,5}$",
        "(001){3}0{4,6}$",
        "(0001){3}0{5,7}$",
        "(000001){3}0{7,8}$",
        // Regular, Lapsed
        "(01){3}0{5,12}$",
        "(001){3}0{6,12}$",
        "(0001){3}0{7,12}$",
        "(000001){3}0{8,12}$",
        // Regular, Dropped
        "(01){3}0{13,}$",
        "(001){3}0{13,}$",
        "(0001){3}0{13,}$",
        "(000001){3}0{13,}$",
        // Annual
        "10{11}10{0,12}$",
        "10{11}10{13,15}$",
        "10{11}10{16,23}$",
        "10{11}10{24,}$",
        // Occasional
        "0*10*10*",
        // One-Time
        "0*10*$",
        // None
        "^0*$",
    };

        // partner_type, gift_frequency, set_amount (bool, 0 or 1), status
    public static final int [] [] STATUSES = {
        // Monthly partners:
        { PARTNER_MONTHLY, 1, 1, STATUS_NEW},
        { PARTNER_MONTHLY, 1, 1, STATUS_CURRENT},
        { PARTNER_MONTHLY, 1, 1, STATUS_CURRENT},
        { PARTNER_MONTHLY, 1, 1, STATUS_CURRENT},
        { PARTNER_MONTHLY, 1, 1, STATUS_LATE},
        { PARTNER_MONTHLY, 1, 1, STATUS_LAPSED},
        { PARTNER_MONTHLY, 1, 1, STATUS_DROPPED},
        // Regular Partners
        { PARTNER_REGULAR, 2, 1, STATUS_CURRENT},
        { PARTNER_REGULAR, 3, 1, STATUS_CURRENT},
        { PARTNER_REGULAR, 4, 1, STATUS_CURRENT},
        { PARTNER_REGULAR, 6, 1, STATUS_CURRENT},
        // Regular Partners, late
        { PARTNER_REGULAR, 2, 1, STATUS_LATE},
        { PARTNER_REGULAR, 3, 1, STATUS_LATE},
        { PARTNER_REGULAR, 4, 1, STATUS_LATE},
        { PARTNER_REGULAR, 6, 1, STATUS_LATE},
        // Regular Partners, lapsed
        { PARTNER_REGULAR, 2, 1, STATUS_LAPSED},
        { PARTNER_REGULAR, 3, 1, STATUS_LAPSED},
        { PARTNER_REGULAR, 4, 1, STATUS_LAPSED},
        { PARTNER_REGULAR, 6, 1, STATUS_LAPSED},
        // Regular Partners, dropped
        { PARTNER_REGULAR, 2, 1, STATUS_DROPPED},
        { PARTNER_REGULAR, 3, 1, STATUS_DROPPED},
        { PARTNER_REGULAR, 4, 1, STATUS_DROPPED},
        { PARTNER_REGULAR, 6, 1, STATUS_DROPPED},
        // Annual
        { PARTNER_ANNUAL, 12, 1, STATUS_CURRENT},
        { PARTNER_ANNUAL, 12, 1, STATUS_LATE},
        { PARTNER_ANNUAL, 12, 1, STATUS_LAPSED},
        { PARTNER_ANNUAL, 12, 1, STATUS_DROPPED},
        // Occasional
        { PARTNER_OCCASIONAL, 0, 0, STATUS_NONE},
        // One-Time
        { PARTNER_ONETIME, 0, 0, STATUS_NONE},
        // None
        { PARTNER_NONE, 0, 0, STATUS_NONE},
    };

    /**
     * Convert an int representing a partnership type into a resource id
     * for its string equivalent.
     */
    public static int partnership(int p){
        switch (p){
            case 0:
                return R.string.unknown;
            case 1:
                return R.string.none;
            case 2:
                return R.string.onetime;
            case 3:
                return R.string.occasional;
            case 4:
                return R.string.annual;
            case 5:
                return R.string.regular;
            case 6:
                return R.string.monthly ;
        }
        return 0;
    }
    public static int getStatusStringRes(int p){
        switch(p){
            case STATUS_CURRENT:
                return R.string.current;
            case STATUS_NEW:
                return R.string.new_;
            case STATUS_LATE:
                return R.string.late;
            case STATUS_LAPSED:
                return R.string.lapsed;
            case STATUS_DROPPED:
                return R.string.dropped;
            case STATUS_NONE:
                return R.string.none;
        }
        return R.string.unknown;
    }

    public ContactStatus(){
        super(MPDDBHelper.get(), TABLE);
        init();
    }
    public ContactStatus(int _id){
        super(MPDDBHelper.get(), TABLE, _id);
        init();
    }

    @Override
    public DBModel newInstance(){
        return new ContactStatus();
    }

    @Override
    public DBModel newInstance(int id){
        return new ContactStatus(id);
    }

    @Override
    protected void init(){
        addField(new IntField("id"));
        setPrimaryKey(getField("id"));
        getField("id").setColumnName("_id");
        getField("id").setExtraArguments("autoincrement");

        addField(new ForeignKeyField("contact_id", Contact.getReferenceInstance()));

        addField(new DateField("last_gift"));
        addField(new IntField("partner_type"));
            // monthly, regular, annual, occasional, one-time, none
        getField("partner_type").setDefault(PARTNER_NONE);
        addField(new IntField("gift_frequency"));
            // # of months between gifts (1 is monthly, 3 is quarterly, etc).

        addField(new MoneyField("giving_amount"));
        addField(new IntField("status"));
            // current, late, lapsed, dropped
        getField("status").setDefault(STATUS_NONE);
        addField(new StringField("notes"));
        addField(new DateField("last_notify"));


        TABLE_NAME=TABLE;
        super.init();
    }
    public String [] generateUpdateSQL(int oldversion){
        if (oldversion < 2){
            String [] result = new String[3];
            result[0] = generateCreateSQL();
            result[1] = "alter table contact_status add " + getField("notes").getSQLDefinition() + ";";
            result[2] = "alter table contact_status add " + getField("last_notify").getSQLDefinition() + ";";
            return result;
        }
        if (oldversion < 3){
            String [] result = new String[2];
            result[0] = "alter table contact_status add " + getField("notes").getSQLDefinition() + ";";
            result[1] = "alter table contact_status add " + getField("last_notify").getSQLDefinition() + ";";
            return result;
        }
        if (oldversion < 6){
            String [] result = new String[1];
            result[0] = "alter table contact_status add " + getField("last_notify").getSQLDefinition() + ";";
            return result;
        }
        return null;
    }

}
