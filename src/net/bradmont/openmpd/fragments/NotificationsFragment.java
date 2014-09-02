package net.bradmont.openmpd.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.View;
import android.widget.SimpleCursorAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import net.bradmont.openmpd.models.Notification;
import net.bradmont.openmpd.*;

import net.bradmont.openmpd.views.EnhancedListView;

public class NotificationsFragment extends ListFragment{
    
    private EnhancedListAdapter mAdapter;
    private EnhancedListView mListView;

    private final static String NOTIFICATIONS_QUERY = 
        "select * from notification left join contact on notification.contact_id = contact._id where status = ? order by date desc;";

    private static final String [] columns = { "lname", "giving_amount", "type", "status", "date"};
    private static final int [] fields = { R.id.name, R.id.amount, R.id.type, R.id.status, R.id.date};

    @Override
    public View  onCreateView(LayoutInflater inflater, ViewGroup container,
                          Bundle savedInstanceState) {
        mListView = (EnhancedListView) inflater.inflate(R.layout.enhancedlist, null);
        setHasOptionsMenu(true);
        return mListView;
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Cursor cursor = MPDDBHelper.get().getReadableDatabase()
                .rawQuery(NOTIFICATIONS_QUERY, 
                new String [] { Integer.toString(Notification.STATUS_NOTIFIED)});

        mAdapter = new EnhancedListAdapter(getActivity(),
                 R.layout.notification_list_item, cursor, columns, fields);
        mListView.setAdapter(mAdapter);

        mListView.setDismissCallback(new NotificationDismissCallback());
        //mListView.setSwipingLayout(R.id.swiping_layout);

        //mListView.setSwipingLayout(true);

        mListView.enableSwipeToDismiss();
        mListView.setSwipeDirection(EnhancedListView.SwipeDirection.BOTH);

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
                    new String [] {Integer.toString(Notification.STATUS_ACKNOWLEDGED), Integer.toString(archived_id)});
            Cursor cursor = MPDDBHelper.get().getReadableDatabase()
                .rawQuery(NOTIFICATIONS_QUERY, 
                new String [] { Integer.toString(Notification.STATUS_NOTIFIED)});
            swapCursor(cursor);
        }
    }

}
