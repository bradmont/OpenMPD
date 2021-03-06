package net.bradmont.openmpd.dao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import net.bradmont.openmpd.dao.Gift;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "GIFT".
*/
public class GiftDao extends AbstractDao<Gift, Long> {

    public static final String TABLENAME = "GIFT";

    /**
     * Properties of entity Gift.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property ContactId = new Property(1, Long.class, "contactId", false, "CONTACT_ID");
        public final static Property Date = new Property(2, String.class, "date", false, "DATE");
        public final static Property Month = new Property(3, String.class, "month", false, "MONTH");
        public final static Property Amount = new Property(4, Long.class, "amount", false, "AMOUNT");
        public final static Property MotivationCode = new Property(5, String.class, "motivationCode", false, "MOTIVATION_CODE");
        public final static Property TntDonationId = new Property(6, String.class, "tntDonationId", false, "TNT_DONATION_ID");
    };

    private DaoSession daoSession;

    private Query<Gift> contact_GiftsQuery;

    public GiftDao(DaoConfig config) {
        super(config);
    }
    
    public GiftDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"GIFT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"CONTACT_ID\" INTEGER," + // 1: contactId
                "\"DATE\" TEXT," + // 2: date
                "\"MONTH\" TEXT," + // 3: month
                "\"AMOUNT\" INTEGER," + // 4: amount
                "\"MOTIVATION_CODE\" TEXT," + // 5: motivationCode
                "\"TNT_DONATION_ID\" TEXT);"); // 6: tntDonationId
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_GIFT_TNT_DONATION_ID ON GIFT" +
                " (\"TNT_DONATION_ID\");");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"GIFT\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Gift entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long contactId = entity.getContactId();
        if (contactId != null) {
            stmt.bindLong(2, contactId);
        }
 
        String date = entity.getDate();
        if (date != null) {
            stmt.bindString(3, date);
        }
 
        String month = entity.getMonth();
        if (month != null) {
            stmt.bindString(4, month);
        }
 
        Long amount = entity.getAmount();
        if (amount != null) {
            stmt.bindLong(5, amount);
        }
 
        String motivationCode = entity.getMotivationCode();
        if (motivationCode != null) {
            stmt.bindString(6, motivationCode);
        }
 
        String tntDonationId = entity.getTntDonationId();
        if (tntDonationId != null) {
            stmt.bindString(7, tntDonationId);
        }
    }

    @Override
    protected void attachEntity(Gift entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Gift readEntity(Cursor cursor, int offset) {
        Gift entity = new Gift( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // contactId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // date
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // month
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4), // amount
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // motivationCode
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // tntDonationId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Gift entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setContactId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setDate(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setMonth(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setAmount(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
        entity.setMotivationCode(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setTntDonationId(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Gift entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Gift entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "gifts" to-many relationship of Contact. */
    public List<Gift> _queryContact_Gifts(Long contactId) {
        synchronized (this) {
            if (contact_GiftsQuery == null) {
                QueryBuilder<Gift> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ContactId.eq(null));
                contact_GiftsQuery = queryBuilder.build();
            }
        }
        Query<Gift> query = contact_GiftsQuery.forCurrentThread();
        query.setParameter(0, contactId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getContactDao().getAllColumns());
            builder.append(" FROM GIFT T");
            builder.append(" LEFT JOIN CONTACT T0 ON T.\"CONTACT_ID\"=T0.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Gift loadCurrentDeep(Cursor cursor, boolean lock) {
        Gift entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Contact contact = loadCurrentOther(daoSession.getContactDao(), cursor, offset);
        entity.setContact(contact);

        return entity;    
    }

    public Gift loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<Gift> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Gift> list = new ArrayList<Gift>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<Gift> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Gift> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
