package net.bradmont.openmpd.dao;

import net.bradmont.openmpd.dao.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "CONTACT_DETAIL".
 */
public class ContactDetail {

    private Long id;
    private String uuid;
    private java.util.Date addedDate;
    private Boolean operational;
    private Boolean fromTnt;
    private String type;
    private String label;
    private String data;
    private Long contactId;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient ContactDetailDao myDao;

    private Contact contact;
    private Long contact__resolvedKey;


    public ContactDetail() {
    }

    public ContactDetail(Long id) {
        this.id = id;
    }

    public ContactDetail(Long id, String uuid, java.util.Date addedDate, Boolean operational, Boolean fromTnt, String type, String label, String data, Long contactId) {
        this.id = id;
        this.uuid = uuid;
        this.addedDate = addedDate;
        this.operational = operational;
        this.fromTnt = fromTnt;
        this.type = type;
        this.label = label;
        this.data = data;
        this.contactId = contactId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getContactDetailDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public java.util.Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(java.util.Date addedDate) {
        this.addedDate = addedDate;
    }

    public Boolean getOperational() {
        return operational;
    }

    public void setOperational(Boolean operational) {
        this.operational = operational;
    }

    public Boolean getFromTnt() {
        return fromTnt;
    }

    public void setFromTnt(Boolean fromTnt) {
        this.fromTnt = fromTnt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    /** To-one relationship, resolved on first access. */
    public Contact getContact() {
        Long __key = this.contactId;
        if (contact__resolvedKey == null || !contact__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ContactDao targetDao = daoSession.getContactDao();
            Contact contactNew = targetDao.load(__key);
            synchronized (this) {
                contact = contactNew;
            	contact__resolvedKey = __key;
            }
        }
        return contact;
    }

    public void setContact(Contact contact) {
        synchronized (this) {
            this.contact = contact;
            contactId = contact == null ? null : contact.getId();
            contact__resolvedKey = contactId;
        }
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}
