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

import net.bradmont.openmpd.dao.ContactInteraction;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CONTACT_INTERACTION".
*/
public class ContactInteractionDao extends AbstractDao<ContactInteraction, Long> {

    public static final String TABLENAME = "CONTACT_INTERACTION";

    /**
     * Properties of entity ContactInteraction.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Uuid = new Property(1, String.class, "uuid", false, "UUID");
        public final static Property Date = new Property(2, java.util.Date.class, "date", false, "DATE");
        public final static Property InteractionType = new Property(3, String.class, "interactionType", false, "INTERACTION_TYPE");
        public final static Property Notes = new Property(4, String.class, "notes", false, "NOTES");
        public final static Property ContactId = new Property(5, Long.class, "contactId", false, "CONTACT_ID");
    };

    private DaoSession daoSession;

    private Query<ContactInteraction> contact_InteractionsQuery;

    public ContactInteractionDao(DaoConfig config) {
        super(config);
    }
    
    public ContactInteractionDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CONTACT_INTERACTION\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"UUID\" TEXT," + // 1: uuid
                "\"DATE\" INTEGER," + // 2: date
                "\"INTERACTION_TYPE\" TEXT," + // 3: interactionType
                "\"NOTES\" TEXT," + // 4: notes
                "\"CONTACT_ID\" INTEGER);"); // 5: contactId
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CONTACT_INTERACTION\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, ContactInteraction entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String uuid = entity.getUuid();
        if (uuid != null) {
            stmt.bindString(2, uuid);
        }
 
        java.util.Date date = entity.getDate();
        if (date != null) {
            stmt.bindLong(3, date.getTime());
        }
 
        String interactionType = entity.getInteractionType();
        if (interactionType != null) {
            stmt.bindString(4, interactionType);
        }
 
        String notes = entity.getNotes();
        if (notes != null) {
            stmt.bindString(5, notes);
        }
 
        Long contactId = entity.getContactId();
        if (contactId != null) {
            stmt.bindLong(6, contactId);
        }
    }

    @Override
    protected void attachEntity(ContactInteraction entity) {
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
    public ContactInteraction readEntity(Cursor cursor, int offset) {
        ContactInteraction entity = new ContactInteraction( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // uuid
            cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)), // date
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // interactionType
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // notes
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5) // contactId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, ContactInteraction entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUuid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setDate(cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)));
        entity.setInteractionType(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setNotes(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setContactId(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(ContactInteraction entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(ContactInteraction entity) {
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
    
    /** Internal query to resolve the "interactions" to-many relationship of Contact. */
    public List<ContactInteraction> _queryContact_Interactions(Long contactId) {
        synchronized (this) {
            if (contact_InteractionsQuery == null) {
                QueryBuilder<ContactInteraction> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ContactId.eq(null));
                contact_InteractionsQuery = queryBuilder.build();
            }
        }
        Query<ContactInteraction> query = contact_InteractionsQuery.forCurrentThread();
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
            builder.append(" FROM CONTACT_INTERACTION T");
            builder.append(" LEFT JOIN CONTACT T0 ON T.\"CONTACT_ID\"=T0.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected ContactInteraction loadCurrentDeep(Cursor cursor, boolean lock) {
        ContactInteraction entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Contact contact = loadCurrentOther(daoSession.getContactDao(), cursor, offset);
        entity.setContact(contact);

        return entity;    
    }

    public ContactInteraction loadDeep(Long key) {
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
    public List<ContactInteraction> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<ContactInteraction> list = new ArrayList<ContactInteraction>(count);
        
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
    
    protected List<ContactInteraction> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<ContactInteraction> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
