package net.bradmont.openmpd.daogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;
import de.greenrobot.daogenerator.Index;

/**
 * Generates entities and DAOs for Prayson
 * 
 * Run it as a Java application (not Android).
 * 
 */
public class OpenMPDDaoGenerator {
    private static final int SCHEMA_VERSION = 2;

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(SCHEMA_VERSION, "net.bradmont.openmpd.dao");

        Entity contact = addContact(schema);
        contact.setHasKeepSections(true);

        Entity contact_detail = addContactDetail(schema, contact);
        Entity contact_interacton = addContactInteraction(schema);
        Entity contact_status = addContactStatus(schema);
        Entity gift = addGift(schema);
        Entity notification = addNotification(schema);
        Entity tnt_service = addTntService(schema);
        Entity service_account = addServiceAccount(schema);

        Entity quick_message = addQuickMessage(schema);
        Entity giving_summary_cache = addGivingSummaryCache(schema);
        Entity log = addLog(schema);
        Entity contact_sublist = addContactSublist(schema);

        new DaoGenerator().generateAll(schema, "../src-gen");
    }

    private static Entity addContact(Schema schema) {
        Entity contact = schema.addEntity("Contact");
        contact.addIdProperty();
        contact.addStringProperty("tntPeopleId");
        contact.addStringProperty("subId"); // "primary", "spouse" or UUID if added by user
        contact.addStringProperty("tntAccountName");
        contact.addStringProperty("tntPersonType");
        contact.addStringProperty("lname");
        contact.addStringProperty("fname");
        contact.addStringProperty("mname");
        contact.addStringProperty("title");
        contact.addStringProperty("suffix");
        contact.addBooleanProperty("isSubcontact");
        Property parentContact = contact.addLongProperty("parentContactId").getProperty();
        contact.addToOne(contact, parentContact).setName("parent");
        contact.addToMany(contact, parentContact).setName("children");
        return contact;
    }

    /** A ContactDetail is information about a contact -- email, phone, address,
     * note, etc.
    */
    private static Entity addContactDetail(Schema schema, Entity contact) {
        Entity contactDetail = schema.addEntity("ContactDetail");
        contactDetail.addIdProperty();
        contactDetail.addStringProperty("uuid");
        contactDetail.addStringProperty("tntPeopleId");
        contactDetail.addDateProperty("addedDate");
        contactDetail.addBooleanProperty("operational");
        contactDetail.addStringProperty("type"); // phone, email, mailing, note
        contactDetail.addStringProperty("label"); // home, work, cell, ...
        contactDetail.addStringProperty("data"); // JSON formatted details
        return contactDetail;
    }

    /** ContactInteraction is a log of my interactions with my contacts.
    */
    private static Entity addContactInteraction(Schema schema){
        Entity contactInteraction = schema.addEntity("ContactInteraction");
        contactInteraction.addIdProperty();
        contactInteraction.addStringProperty("tntPeopleId");
        contactInteraction.addStringProperty("uuid");
        contactInteraction.addDateProperty("date");
        contactInteraction.addStringProperty("interactionType"); // phone, email, ask
                    // meeting, letter, card, reminder ...
        contactInteraction.addStringProperty("notes");
        return contactInteraction;
    }

    /** ContactStatus tracks the type of contact, their giving, and whether
     * they're up to date.
     * "type" can be:  monthly, regular, annual, frequent, occasional, onetime, unknown, namestormed
     * "status" can be:
     *      Partner: current, new, late, lapsed, dropped, pledged
     *      Namestormed: to_contact, contact_later, followup, not_interested
     *      Other: unknown
    */
    private static Entity addContactStatus(Schema schema){
        Entity contactStatus = schema.addEntity("ContactStatus");
        contactStatus.addIdProperty();
        Property tntPeopleId = contactStatus.addStringProperty("tntPeopleId").getProperty();
        contactStatus.addStringProperty("type");  // monthly, regular, annual,
                    // frequent, occasional, onetime, unknown, namestormed
        contactStatus.addStringProperty("status"); 
            // Partner: current, new, late, lapsed, dropped, pledged
            // Namestormed: to_contact, contact_later, followup, not_interested
            // Other: unknown

        contactStatus.addDateProperty("lastGift");
        contactStatus.addLongProperty("givingAmount");
        contactStatus.addIntProperty("givingFrequency"); // in months
        contactStatus.addDateProperty("lastNotify");
        contactStatus.addDateProperty("manualSetExpires");

        Index unique = new Index();
        unique.addProperty(tntPeopleId);
        unique.makeUnique();
        contactStatus.addIndex(unique);
        return contactStatus;
    }

    private static Entity addGift(Schema schema){
        Entity gift = schema.addEntity("Gift");
        gift.addIdProperty();
        gift.addStringProperty("tntPeopleId");
        gift.addDateProperty("date");
        gift.addStringProperty("month");
        gift.addLongProperty("amount");
        gift.addStringProperty("motivationCode");
        gift.addStringProperty("tntDonationId"); 
        return gift;
    }
    /* Notifications of partner activity
     * type: new_partner, change_partner_type, special_gift, late, lapsed,
     *       dropped, change_amount, reminder
     * status (of the notification, not the partner) : new, seen, dismissed
     */
    private static Entity addNotification(Schema schema){
        Entity notification = schema.addEntity("Notification");
        notification.addIdProperty();
        notification.addStringProperty("tntPeopleId");
        notification.addStringProperty("type");
        notification.addStringProperty("status");
        notification.addStringProperty("message");
        notification.addDateProperty("date");
        return notification;
    }

    private static Entity addTntService(Schema schema){
        Entity service = schema.addEntity("TntService");
        service.addIdProperty();
        service.addStringProperty("name");
        service.addStringProperty("name_short");
        service.addStringProperty("domain");
        service.addBooleanProperty("httpAuth");
        service.addStringProperty("balanceUrl");
        service.addStringProperty("balanceFormdata");
        service.addStringProperty("donationsUrl");
        service.addStringProperty("donationsFormdata");
        service.addStringProperty("addressesUrl");
        service.addStringProperty("addressesFormdata");
        service.addStringProperty("addressesByPersonidsUrl");
        service.addStringProperty("addressesByPersonidsFormdata");
        service.addStringProperty("queryIniUrl");
        return service;
    }

    /* An account on a server to import donor data
     * identity is in format username@domain, used to make a unique key for
     * tntPeopleId fields, in case a people_id is repeated from two accounts
     */
    private static Entity addServiceAccount(Schema schema){
        Entity notification = schema.addEntity("ServiceAccount");
        notification.addIdProperty();
        notification.addStringProperty("tntServiceName");
        notification.addStringProperty("username");
        notification.addStringProperty("password");
        notification.addDateProperty("lastImport");
        notification.addStringProperty("identity"); // username@domain@domain
        return notification;
    }
    private static Entity addQuickMessage(Schema schema) {
        Entity quick_message = schema.addEntity("QuickMessage");
        quick_message.addIdProperty();
        quick_message.addStringProperty("name");
        quick_message.addStringProperty("subject");
        quick_message.addStringProperty("body");
        quick_message.addDateProperty("notificationType");
        quick_message.addBooleanProperty("customized");
        return quick_message;
    }

    private static Entity addGivingSummaryCache(Schema schema){
        Entity giving_summary_cache = schema.addEntity("GivingSummaryCache");
        giving_summary_cache.addStringProperty("month");
        giving_summary_cache.addStringProperty("base");
        giving_summary_cache.addStringProperty("regular");
        giving_summary_cache.addStringProperty("frequent");
        giving_summary_cache.addStringProperty("special");
        return giving_summary_cache;
    }
    private static Entity addLog(Schema schema){
        Entity log = schema.addEntity("Log");
        log.addIdProperty();
        log.addStringProperty("msg1");
        log.addStringProperty("msg2");
        log.addStringProperty("msg3");
        return log;
    }
    private static Entity addContactSublist(Schema schema){
        Entity contact_sublist = schema.addEntity("ContactSublist");
        contact_sublist.addIdProperty();
        Property tntPeopleId = contact_sublist.addStringProperty("tntPeopleId").getProperty();
        Property list_name = contact_sublist.addStringProperty("listName").getProperty();

        Index unique = new Index();
        unique.addProperty(tntPeopleId);
        unique.addProperty(list_name);
        unique.makeUnique();
        contact_sublist.addIndex(unique);
        return contact_sublist;
    }


}
