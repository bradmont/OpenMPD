package net.bradmont.openmpd.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import net.bradmont.openmpd.dao.GivingSummaryCache;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "GIVING_SUMMARY_CACHE".
*/
public class GivingSummaryCacheDao extends AbstractDao<GivingSummaryCache, Void> {

    public static final String TABLENAME = "GIVING_SUMMARY_CACHE";

    /**
     * Properties of entity GivingSummaryCache.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Month = new Property(0, String.class, "month", false, "MONTH");
        public final static Property Base = new Property(1, String.class, "base", false, "BASE");
        public final static Property Regular = new Property(2, String.class, "regular", false, "REGULAR");
        public final static Property Frequent = new Property(3, String.class, "frequent", false, "FREQUENT");
        public final static Property Special = new Property(4, String.class, "special", false, "SPECIAL");
    };


    public GivingSummaryCacheDao(DaoConfig config) {
        super(config);
    }
    
    public GivingSummaryCacheDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"GIVING_SUMMARY_CACHE\" (" + //
                "\"MONTH\" TEXT," + // 0: month
                "\"BASE\" TEXT," + // 1: base
                "\"REGULAR\" TEXT," + // 2: regular
                "\"FREQUENT\" TEXT," + // 3: frequent
                "\"SPECIAL\" TEXT);"); // 4: special
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"GIVING_SUMMARY_CACHE\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, GivingSummaryCache entity) {
        stmt.clearBindings();
 
        String month = entity.getMonth();
        if (month != null) {
            stmt.bindString(1, month);
        }
 
        String base = entity.getBase();
        if (base != null) {
            stmt.bindString(2, base);
        }
 
        String regular = entity.getRegular();
        if (regular != null) {
            stmt.bindString(3, regular);
        }
 
        String frequent = entity.getFrequent();
        if (frequent != null) {
            stmt.bindString(4, frequent);
        }
 
        String special = entity.getSpecial();
        if (special != null) {
            stmt.bindString(5, special);
        }
    }

    /** @inheritdoc */
    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    /** @inheritdoc */
    @Override
    public GivingSummaryCache readEntity(Cursor cursor, int offset) {
        GivingSummaryCache entity = new GivingSummaryCache( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // month
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // base
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // regular
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // frequent
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // special
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, GivingSummaryCache entity, int offset) {
        entity.setMonth(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setBase(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setRegular(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setFrequent(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setSpecial(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Void updateKeyAfterInsert(GivingSummaryCache entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    /** @inheritdoc */
    @Override
    public Void getKey(GivingSummaryCache entity) {
        return null;
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}