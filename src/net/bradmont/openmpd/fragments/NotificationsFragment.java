package net.bradmont.openmpd.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import android.util.TypedValue;

import net.bradmont.openmpd.models.Notification;
import net.bradmont.openmpd.models.ContactStatus;
import net.bradmont.openmpd.views.Analytics;
import net.bradmont.openmpd.activities.ContactDetailActivity;
import net.bradmont.openmpd.*;

import net.bradmont.openmpd.views.EnhancedListView;

public class NotificationsFragment extends ListFragment{
    
    private EnhancedListAdapter mAdapter;
    private EnhancedListView mListView;

    private final static String NOTIFICATIONS_QUERY = 
        "select notification.*, contact.*, contact_status.status as contact_status, contact_status.partner_type, contact_status.giving_amount, contact_status.manual_set_expires, contact_status.gift_frequency from notification left join contact on notification.contact_id = contact._id left join contact_status on contact._id=contact_status.contact_id where notification.status = ? and not (type = 2 and contact_status = 5 and manual_set_expires > date) order by date desc;";
    // and not (type = 2 and contact_status = 5 and manual_set_expires > date) 
    // filters out  "Continued" notifications for donors set as regular by 
    // the user


    private static final String [] columns = { "lname", "giving_amount", "type", "fname", "fname", "date", "type"};
    private static final int [] fields = { R.id.name, R.id.amount, R.id.type,  R.id.initials, R.id.user_icon, R.id.date, R.id.quickactions};
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

        mAdapter = new EnhancedListAdapter(getActivity(),
                 R.layout.notification_list_item, cursor, columns, fields);
        mAdapter.setViewBinder(new NotificationListViewBinder());
        mListView.setAdapter(mAdapter);

        mListView.setDismissCallback(new NotificationDismissCallback());
        mListView.setOnItemClickListener(new NotificationClickListener());
        //mListView.setSwipingLayout(R.id.swiping_layout);

        //mListView.setSwipingLayout(true);

        mListView.enableSwipeToDismiss();
        mListView.setSwipeDirection(EnhancedListView.SwipeDirection.BOTH);

    }

    private class NotificationListViewBinder implements SimpleCursorAdapter.ViewBinder{

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            TextView tv = null;
            String value = "";
            switch (view.getId()){
                case R.id.user_icon :
                    value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                            cursor.getString(cursor.getColumnIndex("lname")) ;
                    ImageView iconView = (ImageView) view;
                    Drawable icon = getActivity().getResources().getDrawable(R.drawable.user_icon_circle);
                    icon.setColorFilter( getColor(value), Mode.MULTIPLY );
                    iconView.setImageDrawable(icon);
                    return true;
                case R.id.initials :
                    tv = (TextView) view;
                    try {
                        value += cursor.getString(cursor.getColumnIndex("fname")).substring(0,1);
                    } catch (Exception e){}
                    try {
                        value += cursor.getString(cursor.getColumnIndex("lname")).substring(0,1);
                    } catch (Exception e){}

                    tv.setText(value);
                    return true;
                case R.id.name :
                    tv = (TextView) view;
                    value = cursor.getString(cursor.getColumnIndex("fname")) + " " +
                            cursor.getString(cursor.getColumnIndex("lname")) ;

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
                                        }
                                    } catch (Exception e){
                                        value = value + "Weird";
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
                    if (cursor.getInt(columnIndex) == Notification.SPECIAL_GIFT){
                        TextView phone = (TextView) view.findViewById(R.id.action_icon);
                        phone.setTextSize(TypedValue.COMPLEX_UNIT_PX, 
                            getResources().getDimension(R.dimen.icon_text_size_small));
                        view.findViewById(R.id.overflow).setVisibility(View.VISIBLE);
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
                        ((TextView) view).setText(date);
                    } else {
                        cursor.moveToPrevious();
                        if (!date.equals(cursor.getString(columnIndex))){
                            view.setVisibility(View.VISIBLE);
                            ((TextView) view).setText(date);
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
    static int getColor(String value){
        int total = 0;
        for (int i = 0; i < value.length(); i++){
            total += (int) value.charAt(i);
        }
        total = total % icon_colors.length;
        return icon_colors[total];
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
            intent.putExtra("contactId", (int) id);
            startActivity(intent);

        }
    }

}
