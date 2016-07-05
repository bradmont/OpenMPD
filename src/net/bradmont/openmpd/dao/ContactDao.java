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

import net.bradmont.openmpd.dao.Contact;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CONTACT".
*/
public class ContactDao extends AbstractDao<Contact, Long> {

    public static final String TABLENAME = "CONTACT";

    /**
     * Properties of entity Contact.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property TntPeopleId = new Property(1, String.class, "tntPeopleId", false, "TNT_PEOPLE_ID");
        public final static Property Uuid = new Property(2, String.class, "uuid", false, "UUID");
        public final static Property TntAccountName = new Property(3, String.class, "tntAccountName", false, "TNT_ACCOUNT_NAME");
        public final static Property TntPersonType = new Property(4, String.class, "tntPersonType", false, "TNT_PERSON_TYPE");
        public final static Property IsSubcontact = new Property(5, Boolean.class, "isSubcontact", false, "IS_SUBCONTACT");
        public final static Property ServiceAccountId = new Property(6, Long.class, "serviceAccountId", false, "SERVICE_ACCOUNT_ID");
    };

    private DaoSession daoSession;

    private Query<Contact> serviceAccount_ContactsQuery;

    public ContactDao(DaoConfig config) {
        super(config);
    }
    
    public ContactDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CONTACT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"TNT_PEOPLE_ID\" TEXT," + // 1: tntPeopleId
                "\"UUID\" TEXT," + // 2: uuid
                "\"TNT_ACCOUNT_NAME\" TEXT," + // 3: tntAccountName
                "\"TNT_PERSON_TYPE\" TEXT," + // 4: tntPersonType
                "\"IS_SUBCONTACT\" INTEGER," + // 5: isSubcontact
                "\"SERVICE_ACCOUNT_ID\" INTEGER);"); // 6: serviceAccountId
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_CONTACT_TNT_PEOPLE_ID_UUID ON CONTACT" +
                " (\"TNT_PEOPLE_ID\",\"UUID\");");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CONTACT\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Contact entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String tntPeopleId = entity.getTntPeopleId();
        if (tntPeopleId != null) {
            stmt.bindString(2, tntPeopleId);
        }
 
        String uuid = entity.getUuid();
        if (uuid != null) {
            stmt.bindString(3, uuid);
        }
 
        String tntAccountName = entity.getTntAccountName();
        if (tntAccountName != null) {
            stmt.bindString(4, tntAccountName);
        }
 
        String tntPersonType = entity.getTntPersonType();
        if (tntPersonType != null) {
            stmt.bindString(5, tntPersonType);
        }
 
        Boolean isSubcontact = entity.getIsSubcontact();
        if (isSubcontact != null) {
            stmt.bindLong(6, isSubcontact ? 1L: 0L);
        }
 
        Long serviceAccountId = entity.getServiceAccountId();
        if (serviceAccountId != null) {
            stmt.bindLong(7, serviceAccountId);
        }
    }

    @Override
    protected void attachEntity(Contact entity) {
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
    public Contact readEntity(Cursor cursor, int offset) {
        Contact entity = new Contact( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // tntPeopleId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // uuid
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // tntAccountName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // tntPersonType
            cursor.isNull(offset + 5) ? null : cursor.getShort(offset + 5) != 0, // isSubcontact
            cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6) // serviceAccountId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Contact entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTntPeopleId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setUuid(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTntAccountName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setTntPersonType(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setIsSubcontact(cursor.isNull(offset + 5) ? null : cursor.getShort(offset + 5) != 0);
        entity.setServiceAccountId(cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Contact entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Contact entity) {
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
    
    /** Internal query to resolve the "contacts" to-many relationship of ServiceAccount. */
    public List<Contact> _queryServiceAccount_Contacts(Long serviceAccountId) {
        synchronized (this) {
            if (serviceAccount_ContactsQuery == null) {
                QueryBuilder<Contact> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ServiceAccountId.eq(null));
                serviceAccount_ContactsQuery = queryBuilder.build();
            }
        }
        Query<Contact> query = serviceAccount_ContactsQuery.forCurrentThread();
        query.setParameter(0, serviceAccountId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getServiceAccountDao().getAllColumns());
            builder.append(" FROM CONTACT T");
            builder.append(" LEFT JOIN SERVICE_ACCOUNT T0 ON T.\"SERVICE_ACCOUNT_ID\"=T0.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Contact loadCurrentDeep(Cursor cursor, boolean lock) {
        Contact entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        ServiceAccount account = loadCurrentOther(daoSession.getServiceAccountDao(), cursor, offset);
        entity.setAccount(account);

        return entity;    
    }

    public Contact loadDeep(Long key) {
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
    public List<Contact> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Contact> list = new ArrayList<Contact>(count);
        
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
    
    protected List<Contact> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Contact> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}