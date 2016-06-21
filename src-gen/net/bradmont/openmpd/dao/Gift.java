package net.bradmont.openmpd.dao;

import net.bradmont.openmpd.dao.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "GIFT".
 */
public class Gift {

    private Long id;
    private Long contactId;
    private String date;
    private String month;
    private Long amount;
    private String motivationCode;
    private String tntDonationId;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient GiftDao myDao;

    private Contact contact;
    private Long contact__resolvedKey;


    public Gift() {
    }

    public Gift(Long id) {
        this.id = id;
    }

    public Gift(Long id, Long contactId, String date, String month, Long amount, String motivationCode, String tntDonationId) {
        this.id = id;
        this.contactId = contactId;
        this.date = date;
        this.month = month;
        this.amount = amount;
        this.motivationCode = motivationCode;
        this.tntDonationId = tntDonationId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getGiftDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getMotivationCode() {
        return motivationCode;
    }

    public void setMotivationCode(String motivationCode) {
        this.motivationCode = motivationCode;
    }

    public String getTntDonationId() {
        return tntDonationId;
    }

    public void setTntDonationId(String tntDonationId) {
        this.tntDonationId = tntDonationId;
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
