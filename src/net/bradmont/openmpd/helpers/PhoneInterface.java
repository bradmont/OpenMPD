package net.bradmont.openmpd.helpers;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Contacts;
import net.bradmont.openmpd.helpers.Log;


public class PhoneInterface {
    public static Uri getContactPhotoByPhoneNumber(Context context,
            String phoneNumber){
        phoneNumber = phoneNumber.replaceAll("[^\\d.]", "");
        if (phoneNumber.length() == 0){
            return null;
        }
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cur = context.getContentResolver()
            .query(uri, new String[]{BaseColumns._ID, PhoneLookup._ID, PhoneLookup.PHOTO_ID}, null, null, null);
        if (cur == null){
            return null;
        }
        if (cur.getCount() > 0){
            cur.moveToFirst();
            while (!cur.isAfterLast()){
                Uri photo = getContactPhotoByContactId(context, cur.getLong(1));
                if (photo != null){
                    cur.close();
                    return photo;
                }
            }
        } else {
            cur.close();
            return null;
        }
        return null;
    }
    public static Uri getContactPhotoByContactId(Context context, long id){
        try {
            Cursor cur = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null);
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

    }
}
