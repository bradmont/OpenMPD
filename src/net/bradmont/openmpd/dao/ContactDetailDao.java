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

import net.bradmont.openmpd.dao.ContactDetail;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CONTACT_DETAIL".
*/
public class ContactDetailDao extends AbstractDao<ContactDetail, Long> {

    public static final String TABLENAME = "CONTACT_DETAIL";

    /**
     * Properties of entity ContactDetail.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Uuid = new Property(1, String.class, "uuid", false, "UUID");
        public final static Property AddedDate = new Property(2, java.util.Date.class, "addedDate", false, "ADDED_DATE");
        public final static Property Operational = new Property(3, Boolean.class, "operational", false, "OPERATIONAL");
        public final static Property FromTnt = new Property(4, Boolean.class, "fromTnt", false, "FROM_TNT");
        public final static Property Type = new Property(5, String.class, "type", false, "TYPE");
        public final static Property Label = new Property(6, String.class, "label", false, "LABEL");
        public final static Property Data = new Property(7, String.class, "data", false, "DATA");
        public final static Property ContactId = new Property(8, Long.class, "contactId", false, "CONTACT_ID");
    };

    private DaoSession daoSession;

    private Query<ContactDetail> contact_DetailsQuery;

    public ContactDetailDao(DaoConfig config) {
        super(config);
    }
    
    public ContactDetailDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CONTACT_DETAIL\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"UUID\" TEXT," + // 1: uuid
                "\"ADDED_DATE\" INTEGER," + // 2: addedDate
                "\"OPERATIONAL\" INTEGER," + // 3: operational
                "\"FROM_TNT\" INTEGER," + // 4: fromTnt
                "\"TYPE\" TEXT," + // 5: type
                "\"LABEL\" TEXT," + // 6: label
                "\"DATA\" TEXT," + // 7: data
                "\"CONTACT_ID\" INTEGER);"); // 8: contactId
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CONTACT_DETAIL\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, ContactDetail entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String uuid = entity.getUuid();
        if (uuid != null) {
            stmt.bindString(2, uuid);
        }
 
        java.util.Date addedDate = entity.getAddedDate();
        if (addedDate != null) {
            stmt.bindLong(3, addedDate.getTime());
        }
 
        Boolean operational = entity.getOperational();
        if (operational != null) {
            stmt.bindLong(4, operational ? 1L: 0L);
        }
 
        Boolean fromTnt = entity.getFromTnt();
        if (fromTnt != null) {
            stmt.bindLong(5, fromTnt ? 1L: 0L);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(6, type);
        }
 
        String label = entity.getLabel();
        if (label != null) {
            stmt.bindString(7, label);
        }
 
        String data = entity.getData();
        if (data != null) {
            stmt.bindString(8, data);
        }
 
        Long contactId = entity.getContactId();
        if (contactId != null) {
            stmt.bindLong(9, contactId);
        }
    }

    @Override
    protected void attachEntity(ContactDetail entity) {
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
    public ContactDetail readEntity(Cursor cursor, int offset) {
        ContactDetail entity = new ContactDetail( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // uuid
            cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)), // addedDate
            cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0, // operational
            cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0, // fromTnt
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // type
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // label
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // data
            cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8) // contactId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, ContactDetail entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUuid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setAddedDate(cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)));
        entity.setOperational(cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0);
        entity.setFromTnt(cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0);
        entity.setType(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setLabel(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setData(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setContactId(cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(ContactDetail entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(ContactDetail entity) {
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
    
    /** Internal query to resolve the "details" to-many relationship of Contact. */
    public List<ContactDetail> _queryContact_Details(Long contactId) {
        synchronized (this) {
            if (contact_DetailsQuery == null) {
                QueryBuilder<ContactDetail> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ContactId.eq(null));
                contact_DetailsQuery = queryBuilder.build();
            }
        }
        Query<ContactDetail> query = contact_DetailsQuery.forCurrentThread();
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
            builder.append(" FROM CONTACT_DETAIL T");
            builder.append(" LEFT JOIN CONTACT T0 ON T.\"CONTACT_ID\"=T0.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected ContactDetail loadCurrentDeep(Cursor cursor, boolean lock) {
        ContactDetail entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Contact contact = loadCurrentOther(daoSession.getContactDao(), cursor, offset);
        entity.setContact(contact);

        return entity;    
    }

    public ContactDetail loadDeep(Long key) {
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
    public List<ContactDetail> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<ContactDetail> list = new ArrayList<ContactDetail>(count);
        
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
    
    protected List<ContactDetail> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<ContactDetail> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
