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
import net.bradmont.openmpd.helpers.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.bradmont.openmpd.dao.*;
import net.bradmont.openmpd.activities.ContactDetailActivity;
import net.bradmont.openmpd.helpers.TextTools;
import net.bradmont.openmpd.*;

import net.bradmont.openmpd.views.EnhancedListView;

public class NotificationsFragment extends ListFragment{
    
    private EnhancedListAdapter mAdapter;
    private EnhancedListView mListView;
    private OnClickListener mOnClickListener = null;

    public static final String NEW_PARTNER = "new_partner";
    public static final String CHANGE_PARTNER_TYPE = "change_partner_type";
    public static final String SPECIAL_GIFT = "special_gift";
    public static final String LATE = "late";
    public static final String LAPSED = "lapsed";
    public static final String DROPPED = "dropped";
    public static final String RESTARTED = "restarted";
    public static final String FULFILLED = "fulfilled";
    public static final String CHANGE_AMOUNT = "change_amount";
    public static final String REMINDER = "reminder";

    public static final String NEW_NOTIFICATON = "new";
    public static final String SEEN_NOTIFICATION = "seen";
    public static final String DISMISSED_NOTIFICATION = "dismissed";



    private final static String NOTIFICATIONS_QUERY = 
        "select " +
            "notification._id as _id, notification.type as n_type, " +
            "notification.status as n_status, notification.message, notification.date, " +
            "fname, lname, s_fname, s_lname, CONTACTS._id as _contact_id, contacts.type, " +
            "giving_amount, contacts.status, giving_frequency, last_gift, " +
            "manual_set_expires " +
        "from " +

            "notification join " +
                "(" +ContactListFragment.BASE_QUERY + ") as CONTACTS " + // contacts + statuses
                "on notification.contact_id = _contact_id "+
        "where "+
            "n_status != ? " +
        "order by date desc;";


    private static final String [] columns = { "lname", "giving_amount", "type", "fname", "fname", "s_fname", "DATE", "n_type"};
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

        Cursor cursor = OpenMPD.getDB()
                .rawQuery(NOTIFICATIONS_QUERY, 
                new String [] { DISMISSED_NOTIFICATION });

        mOnClickListener = new NotificationCardClickListener();

        String [] cols = cursor.getColumnNames();
        String res = "";
        for (int i = 0; i < cols.length; i++) res = res + cols[i] +", ";
        Log.i("net.bradmont.openmpd", res);

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

                    if (!cursor.isNull(cursor.getColumnIndex("s_fname"))){
                        value = cursor.getString(cursor.getColumnIndex("s_fname")) + " " +
                                cursor.getString(cursor.getColumnIndex("s_lname")) ;
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
                    if (!cursor.isNull(cursor.getColumnIndex("s_fname"))){
                        try {
                            value += cursor.getString(cursor.getColumnIndex("s_fname")).substring(0,1);
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
                    if (!cursor.isNull(cursor.getColumnIndex("s_fname"))){
                        value = cursor.getString(cursor.getColumnIndex("fname")) +
                                "&" + cursor.getString(cursor.getColumnIndex("s_fname")) +
                                " " + cursor.getString(cursor.getColumnIndex("lname")) ;
                    } else {
                        value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                                cursor.getString(cursor.getColumnIndex("lname")) ;
                    }

                    tv.setText(value);

                    return true;
                case R.id.amount:
                    tv = (TextView) view;
                    if (cursor.getString(cursor.getColumnIndex("type")).equals(SPECIAL_GIFT)){
                        // on special gifts, put the value of the special gift rather than the
                        // partner's regular giving amount
                        try {
                            String message = cursor.getString(cursor.getColumnIndex("MESSAGE"));
                            value = "$" + message.substring(0, message.length() - 2);
                            tv.setText(value);
                            return true;
                        } catch (Exception e){}
                    }
                    int amount = cursor.getInt(columnIndex)/100;
                    value = Integer.toString(amount);

                    if (!cursor.getString(cursor.getColumnIndex("n_type")).equals(SPECIAL_GIFT)){
                        switch (cursor.getString(cursor.getColumnIndex("type"))){
                            case "monthly":
                                value += getString(R.string.per_month);
                                break;
                            case "annual":
                                value += getString(R.string.per_year);
                                break;
                            case "regular":
                                value += getString(R.string.per_n_months);
                                int frequency = cursor.getInt(cursor.getColumnIndex("gift_frequency"));
                                value = value.replace("?", Integer.toString(frequency));

                        }
                    }
                    tv.setText("$" + value);
                    return true;
                case R.id.type:
                    tv = (TextView) view;
                    switch (cursor.getString(columnIndex)){
                        case CHANGE_PARTNER_TYPE:
                            String partnership = cursor.getString(cursor.getColumnIndex("type"));
                            if (partnership.equals("monthly")||
                                    partnership.equals("regular")){
                                String status = cursor.getString(cursor.getColumnIndex("status"));
                                if (status.equals("new")) {
                                    value = getString(R.string.new_partner);
                                } else if (status.equals("current")){
                                    value = getString(R.string.restarted_partner);
                                }
                            } else {
                                value = getString(R.string.change_type);
                            }
                            break;
                        case LATE:
                            value = getString(R.string.late_partner);
                            break;
                        case LAPSED:
                            value = getString(R.string.lapsed_partner);
                            break;
                        case DROPPED:
                            value = getString(R.string.dropped);
                            break;
                        case NEW_PARTNER:
                            value = getString(R.string.new_partner);
                            break;
                        case RESTARTED:
                            value = getString(R.string.restarted_partner);
                            break;

                        case FULFILLED:
                                value = "Fulfilled";
                            break;
                        case CHANGE_AMOUNT:
                            value = getString(R.string.amount_change);
                            break;
                        case SPECIAL_GIFT:
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
                    if (cursor.getString(columnIndex).equals(SPECIAL_GIFT)){
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
            OpenMPD.getDB().execSQL(
                    "update notification set status = ? where _id = ?",
                    new String [] {DISMISSED_NOTIFICATION, Integer.toString(notificationId)});
            Cursor cursor = OpenMPD.getDB()
                .rawQuery(NOTIFICATIONS_QUERY, 
                new String [] { SEEN_NOTIFICATION }) ;
            swapCursor(cursor);
            return notificationId;
        }

        public void insert(int archived_id){
            OpenMPD.getDB().execSQL(
                    "update notification set status = ? where _id = ?",
                    new String [] {SEEN_NOTIFICATION, Integer.toString(archived_id)});
            Cursor cursor = OpenMPD.getDB()
                .rawQuery(NOTIFICATIONS_QUERY, 
                new String [] { SEEN_NOTIFICATION });
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
                    mNotification = OpenMPD.getDaoSession().getNotificationDao()
                        .load((long)nID);

                    mContactStatus = mNotification.getContact().getStatus();
                    PopupMenu popup = new PopupMenu(getActivity(), v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.special_gift_card_actions, popup.getMenu());
                    popup.setOnMenuItemClickListener(this);
                    popup.show();
                    break;
                case R.id.action_icon:
                    /*
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
                    */
                    ((BaseActivity) getActivity()).userMessage("TODO!");

            }
        }
        public boolean onMenuItemClick(MenuItem item){
            ((BaseActivity) getActivity()).userMessage("TODO!");
            /*
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
            */
            return false;
        }
    }
}
