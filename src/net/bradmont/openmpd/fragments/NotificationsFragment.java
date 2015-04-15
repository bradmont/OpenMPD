package net.bradmont.openmpd.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import android.util.TypedValue;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.activities.ContactDetailActivity;
import net.bradmont.openmpd.helpers.TextTools;
import net.bradmont.openmpd.*;

import net.bradmont.openmpd.views.EnhancedListView;

public class NotificationsFragment extends ListFragment{
    
    private EnhancedListAdapter mAdapter;
    private EnhancedListView mListView;
    private OnClickListener mOnClickListener = null;

    private final static String NOTIFICATIONS_QUERY = 
        "select notification._id, notification.contact_id, notification.type, notification.status, notification.message, notification.date, notification.last_gift, notification.giving_amount, notification.partner_type, notification.partner_status, contact.*, contact_status.status as contact_status, contact_status.partner_type, contact_status.giving_amount, contact_status.manual_set_expires, contact_status.gift_frequency, spouse.fname as spouse_fname, spouse.lname as spouse_lname from notification left join contact on notification.contact_id = contact._id left join contact_status on contact._id=contact_status.contact_id left outer join contact as spouse on contact.spouse_id=spouse._id where notification.status = ? and not (type = 2 and contact_status = 5 and manual_set_expires > date) and not (notification.partner_type < 10 and notification.partner_type > 0) and not (contact_Status = 5 and message = 4) order by date desc;";
    // and not (type = 2 and contact_status = 5 and manual_set_expires > date) 
    // filters out  "Continued" notifications for donors set as regular by 
    // the user
    // and not (0 < partner_type < 10) is to weed out old notifications from a previous app version
    // and not (contact_status = "5" and message="4") weeds out notification change from "new" to "current"


    private static final String [] columns = { "lname", "giving_amount", "type", "fname", "fname", "spouse_fname", "date", "type"};
    private static final int [] fields = { R.id.name, R.id.amount, R.id.type,  R.id.initials, R.id.user_icon_left, R.id.user_icon_right, R.id.date, R.id.quickactions};
    private static int[] icon_colors = null;

    @Override
    public View  onCreateView(LayoutInflater inflater, ViewGroup container,
                          Bundle savedInstanceState) {
        mListView = (EnhancedListView) inflater.inflate(R.layout.enhancedlist, null);
        setHasOptionsMenu(true);
        return mListView;
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        icon_colors = getActivity().getResources().getIntArray(R.array.user_icon_colors);

        Cursor cursor = MPDDBHelper.get().getReadableDatabase()
                .rawQuery(NOTIFICATIONS_QUERY, 
                new String [] { Integer.toString(Notification.STATUS_NOTIFIED)});

        mOnClickListener = new NotificationCardClickListener();
        mAdapter = new EnhancedListAdapter(getActivity(),
                 R.layout.notification_list_item, cursor, columns, fields);
        mAdapter.setViewBinder(new NotificationListViewBinder());
        mListView.setAdapter(mAdapter);

        mListView.setDismissCallback(new NotificationDismissCallback());
        mListView.setOnItemClickListener(new NotificationClickListener());
        mListView.setSwipingLayout(R.id.card);


        mListView.enableSwipeToDismiss();
        mListView.setSwipeDirection(EnhancedListView.SwipeDirection.BOTH);

    }

    private class NotificationListViewBinder implements SimpleCursorAdapter.ViewBinder{

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            TextView tv = null;
            String value = "";
            switch (view.getId()){
                case R.id.user_icon_left :
                    value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                            cursor.getString(cursor.getColumnIndex("lname")) ;
                    ImageView iconView = (ImageView) view;
                    iconView.getDrawable().setColorFilter( getColor(value), Mode.MULTIPLY );

                    iconView.getDrawable().setLevel(5000);

                    return true;
                case R.id.user_icon_right :
                    iconView = (ImageView) view;

                    if (!cursor.isNull(cursor.getColumnIndex("spouse_fname"))){
                        value = cursor.getString(cursor.getColumnIndex("spouse_fname")) + " " +
                                cursor.getString(cursor.getColumnIndex("spouse_lname")) ;
                        String spouse_value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                                cursor.getString(cursor.getColumnIndex("lname")) ;
                        iconView.getDrawable().setColorFilter( getColor(value, spouse_value), Mode.MULTIPLY );
                    } else {
                        value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                                cursor.getString(cursor.getColumnIndex("lname")) ;
                        iconView.getDrawable().setColorFilter( getColor(value), Mode.MULTIPLY );
                    }
                    iconView.getDrawable().setLevel(5000);

                    return true;
                case R.id.initials :
                    tv = (TextView) view;
                    try {
                        value += cursor.getString(cursor.getColumnIndex("fname")).substring(0,1);
                    } catch (Exception e){}
                    if (!cursor.isNull(cursor.getColumnIndex("spouse_fname"))){
                        try {
                            value += cursor.getString(cursor.getColumnIndex("spouse_fname")).substring(0,1);
                        } catch (Exception e){}
                    } else {
                        try {
                            value += cursor.getString(cursor.getColumnIndex("lname")).substring(0,1);
                        } catch (Exception e){}
                    }

                    tv.setText(value);
                    return true;
                case R.id.name :
                    tv = (TextView) view;
                    if (!cursor.isNull(cursor.getColumnIndex("spouse_fname"))){
                        value = cursor.getString(cursor.getColumnIndex("fname")) +
                                "&" + cursor.getString(cursor.getColumnIndex("spouse_fname")) +
                                " " + cursor.getString(cursor.getColumnIndex("lname")) ;
                    } else {
                        value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                                cursor.getString(cursor.getColumnIndex("lname")) ;
                    }

                    tv.setText(value);

                    return true;
                case R.id.amount:
                    tv = (TextView) view;
                    if (cursor.getInt(cursor.getColumnIndex("type")) == Notification.SPECIAL_GIFT){
                        // on special gifts, put the value of the special gift rather than the
                        // partner's regular giving amount
                        try {
                            String message = cursor.getString(cursor.getColumnIndex("message"));
                            value = "$" + message.substring(0, message.length() - 2);
                            tv.setText(value);
                            return true;
                        } catch (Exception e){}
                    }
                    int amount = cursor.getInt(columnIndex)/100;
                    value = Integer.toString(amount);

                    switch (cursor.getInt(cursor.getColumnIndex("type"))){
                        case Notification.SPECIAL_GIFT:
                            break;
                        case Notification.CHANGE_PARTNER_TYPE:
                        case Notification.CHANGE_AMOUNT:
                        case Notification.CHANGE_STATUS:
                            switch (cursor.getInt(cursor.getColumnIndex("partner_type"))){
                                case ContactStatus.PARTNER_MONTHLY:
                                    value += getString(R.string.per_month);
                                    break;
                                case ContactStatus.PARTNER_ANNUAL:
                                    value += getString(R.string.per_year);
                                    break;
                                case ContactStatus.PARTNER_REGULAR:
                                    value += getString(R.string.per_n_months);
                                    int frequency = cursor.getInt(cursor.getColumnIndex("gift_frequency"));
                                    value = value.replace("?", Integer.toString(frequency));

                            }
                    }
                    tv.setText("$" + value);
                    return true;
                case R.id.type:
                    tv = (TextView) view;
                    switch (cursor.getInt(columnIndex)){
                        case Notification.CHANGE_PARTNER_TYPE:
                            int partnership = cursor.getInt(cursor.getColumnIndex("partner_type"));
                            if (partnership == ContactStatus.PARTNER_MONTHLY ||
                                    partnership == ContactStatus.PARTNER_REGULAR){
                                int status = cursor.getInt(cursor.getColumnIndex("contact_status"));
                                if (status == ContactStatus.STATUS_NEW){
                                    value = getString(R.string.new_partner);
                                } else if (status == ContactStatus.STATUS_CURRENT){
                                    value = getString(R.string.restarted_partner);
                                }
                            } else {
                                value = getString(R.string.change_type);
                            }
                            break;
                        case Notification.CHANGE_STATUS:
                            switch (cursor.getInt(cursor.getColumnIndex("contact_status"))){
                                case ContactStatus.STATUS_LATE:
                                    value = getString(R.string.late_partner);
                                    break;
                                case ContactStatus.STATUS_LAPSED:
                                    value = getString(R.string.lapsed_partner);
                                    break;
                                case ContactStatus.STATUS_DROPPED:
                                    value = getString(R.string.dropped);
                                    break;
                                case ContactStatus.STATUS_NEW:
                                    value = getString(R.string.new_partner);
                                    break;
                                case ContactStatus.STATUS_CURRENT:
                                    try { 
                                        int oldstatus = Integer.parseInt(cursor.getString(
                                                    cursor.getColumnIndex("message")));
                                        if (oldstatus == ContactStatus.STATUS_LATE ||
                                            oldstatus == ContactStatus.STATUS_LAPSED ||
                                            oldstatus == ContactStatus.STATUS_DROPPED){
                                            value = getString(R.string.restarted_partner);
                                        } else if (cursor.getString(cursor.getColumnIndex("manual_set_expires"))
                                                .compareTo( cursor.getString(cursor.getColumnIndex("date"))) >= 0){
                                            value = getString(R.string.partner_continued);
                                        } else if (oldstatus == ContactStatus.STATUS_NEW){
                                            value = "Maintained";
                                        }
                                    } catch (Exception e){
                                    }
                                    break;
                            }
                            break;
                        case Notification.CHANGE_AMOUNT:
                            value = getString(R.string.amount_change);
                            break;
                        case Notification.SPECIAL_GIFT:
                            value = getString(R.string.special_gift);
                            break;
                    }
                    if (value != ""){
                        tv.setText(value);
                        return true;
                    } else {
                        return false;
                    }
                case R.id.quickactions:
                    view.findViewById(R.id.action_icon).setOnClickListener(mOnClickListener);
                    view.findViewById(R.id.action_icon).setTag(cursor.getInt(1)); // contact ID
                    if (cursor.getInt(columnIndex) == Notification.SPECIAL_GIFT){
                        TextView phone = (TextView) view.findViewById(R.id.action_icon);
                        phone.setTextSize(TypedValue.COMPLEX_UNIT_PX, 
                            getResources().getDimension(R.dimen.icon_text_size_small));
                        view.findViewById(R.id.overflow).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.overflow).setOnClickListener(mOnClickListener);
                        view.findViewById(R.id.overflow).setTag(cursor.getInt(0));
                        return true;
                    } else {
                        TextView phone = (TextView) view.findViewById(R.id.action_icon);
                        phone.setTextSize(TypedValue.COMPLEX_UNIT_PX, 
                            getResources().getDimension(R.dimen.icon_text_size));
                        view.findViewById(R.id.overflow).setVisibility(View.GONE);
                        return true;
                    }
                case R.id.status:
                    tv = (TextView) view;
                    return false;
                case R.id.date:
                    String date = cursor.getString(columnIndex);
                    if (cursor.getPosition() ==0){
                        view.setVisibility(View.VISIBLE);
                        ((TextView) view).setText(TextTools.prettyDate(date));
                    } else {
                        cursor.moveToPrevious();
                        if (!date.equals(cursor.getString(columnIndex))){
                            view.setVisibility(View.VISIBLE);
                            ((TextView) view).setText(TextTools.prettyDate(date));
                        } else {
                            view.setVisibility(View.GONE);
                        }
                        cursor.moveToNext();
                    }
                    return true;
            }
            return false;
        }

        private String getString(int id){
            return getActivity().getResources().getString(id);
        }

        /* Simple hash & modulo to select a color from our list based on a
         * string (so we can have a consistent color for a contact)
         */
    }

    /*
     * Color for a given string (name), ensuring it is not thhe same color
     * provided by value2
     */
    static int getColor(String value, String value2){
        int spouse_color = getColorIndex(value2, -1);
        return icon_colors[(spouse_color + (icon_colors.length/2)) % icon_colors.length];
    }

    static int getColor(String value){
        return icon_colors[getColorIndex(value, -1)];
    }

    static int getColorIndex(String value, int unwantedIndex){
        int total = 0;
        for (int i = 0; i < value.length(); i++){
            total += (int) value.charAt(i);
        }
        total = total % icon_colors.length;
        if (total == unwantedIndex){
            total = (total +1) % icon_colors.length;
        }
        return total;
    }

    private class NotificationDismissCallback implements EnhancedListView.OnDismissCallback{
        @Override
        public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
           final int archived_id = mAdapter.remove(position);
            return new EnhancedListView.Undoable(){
                @Override
                public void undo() {
                    mAdapter.insert(archived_id);
                }
            };
           }
    }

    private class EnhancedListAdapter extends SimpleCursorAdapter{
        public EnhancedListAdapter(Context context, int layout, Cursor c, String[] from, int[] to){
            super(context, layout, c, from, to);
        }

        public int remove(int position){
            getCursor().moveToPosition(position);
            int notificationId = getCursor().getInt(0);
            MPDDBHelper.get().getWritableDatabase().execSQL(
                    "update notification set status = ? where _id = ?",
                    new String [] {Integer.toString(Notification.STATUS_ACKNOWLEDGED), Integer.toString(notificationId)});
            Cursor cursor = MPDDBHelper.get().getReadableDatabase()
                .rawQuery(NOTIFICATIONS_QUERY, 
                new String [] { Integer.toString(Notification.STATUS_NOTIFIED)});
            swapCursor(cursor);
            return notificationId;
        }

        public void insert(int archived_id){
            MPDDBHelper.get().getWritableDatabase().execSQL(
                    "update notification set status = ? where _id = ?",
                    new String [] {Integer.toString(Notification.STATUS_NOTIFIED), Integer.toString(archived_id)});
            Cursor cursor = MPDDBHelper.get().getReadableDatabase()
                .rawQuery(NOTIFICATIONS_QUERY, 
                new String [] { Integer.toString(Notification.STATUS_NOTIFIED)});
            swapCursor(cursor);
        }
    }

    private class NotificationClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
            mAdapter.getCursor().moveToPosition(position);
            int contact_id = mAdapter.getCursor().getInt(
                    mAdapter.getCursor().getColumnIndex("contact_id"));
            intent.putExtra("contactId", contact_id);
            startActivity(intent);

        }
    }

    private class NotificationCardClickListener implements View.OnClickListener, PopupMenu.OnMenuItemClickListener{

        private View mView = null;
        private Notification mNotification = null; 
        private ContactStatus mContactStatus = null; 

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.overflow:
                    mView = v;
                    Integer nID = (Integer) v.getTag();
                    Log.i("net.bradmont.openmpd", "Loading notification " +nID);
                    mNotification = new Notification(nID);
                    mContactStatus = (ContactStatus) MPDDBHelper.getReferenceModel("contact_status")
                        .getByField("contact_id", mNotification.getInt("contact"));
                    PopupMenu popup = new PopupMenu(getActivity(), v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.special_gift_card_actions, popup.getMenu());
                    popup.setOnMenuItemClickListener(this);
                    popup.show();
                    break;
                case R.id.action_icon:
                    PhoneNumber phone = (PhoneNumber) MPDDBHelper
                            .getModelByField("phone_number", "contact_id", (Integer)v.getTag());
                    Log.i("net.bradmont.openmpd", "Calling contact " + v.getTag());

                    String number = phone.getString("number");
                    if (number.length() < 3){
                        ((BaseActivity) getActivity()).userMessage(R.string.no_phone_number);
                    } else {
                        number = "tel:" + number;
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(number));
                        getActivity().startActivity(intent);
                    }

            }
        }
        public boolean onMenuItemClick(MenuItem item){
            switch (item.getItemId()) {
                case R.id.menu_make_monthly:
                    ((BaseActivity) getActivity()).userMessage(R.string.assigned_monthly);
                    // do stuff to the ContactStatus
                    mContactStatus.setValue("partner_type", ContactStatus.PARTNER_MONTHLY); 
                    mContactStatus.setValue("status", ContactStatus.STATUS_CURRENT); 
                    mContactStatus.setValue("giving_amount", Integer.parseInt(mNotification.getString("message")));
                    mContactStatus.setValue("gift_frequency", 1); 

                    // set up expiry date for manual status
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, 3); // expire in 3 months
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String expires_date = dateFormat.format(cal.getTime());
                    mContactStatus.setValue("manual_set_expires", expires_date);

                    mContactStatus.dirtySave();
                    AnalyticsFragment.clearCache();
                    return true;
                case R.id.menu_make_quarterly:
                    ((BaseActivity) getActivity()).userMessage(R.string.assigned_quarterly);
                    // do stuff to the ContactStatus
                    mContactStatus.setValue("partner_type", ContactStatus.PARTNER_REGULAR); 
                    mContactStatus.setValue("status", ContactStatus.STATUS_CURRENT); 
                    mContactStatus.setValue("giving_amount", Integer.parseInt(mNotification.getString("message")));
                    mContactStatus.setValue("gift_frequency", 6); 

                    // set up expiry date for manual status
                    cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, 6); // expire in 14 months
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    expires_date = dateFormat.format(cal.getTime());
                    mContactStatus.setValue("manual_set_expires", expires_date);

                    mContactStatus.dirtySave();
                    AnalyticsFragment.clearCache();
                    return true;
                case R.id.menu_make_annual:
                    ((BaseActivity) getActivity()).userMessage(R.string.assigned_annual);
                    // do stuff to the ContactStatus
                    mContactStatus.setValue("partner_type", ContactStatus.PARTNER_ANNUAL); 
                    mContactStatus.setValue("status", ContactStatus.STATUS_CURRENT); 
                    mContactStatus.setValue("giving_amount", Integer.parseInt(mNotification.getString("message")));
                    mContactStatus.setValue("gift_frequency", 12); 

                    // set up expiry date for manual status
                    cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, 14); // expire in 14 months
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    expires_date = dateFormat.format(cal.getTime());
                    mContactStatus.setValue("manual_set_expires", expires_date);

                    mContactStatus.dirtySave();
                    AnalyticsFragment.clearCache();
                    return true;
            }
            return false;
        }
    }
}
