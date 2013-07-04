package net.bradmont.openmpd.views;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.controllers.TntImporter;

import android.content.Context;

import android.database.sqlite.*;
import android.database.Cursor;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import java.lang.Runnable;

public class GiftList extends ListFragment {
    public static final String [] columns = {"fname", "lname", "date", "amount"};
    public static final int [] fields = {R.id.fname, R.id.lname, R.id.date, R.id.amount};

    private SQLiteDatabase db_read = MPDDBHelper.get()
            .getReadableDatabase();
    private Cursor cursor = null;

    private LinearLayout header = null;
    private SimpleCursorAdapter adapter = null;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set header
        ListView lv = getListView();
        LayoutInflater layoutInflater = (LayoutInflater)getActivity()
                .getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        // set up adapter
        cursor = db_read.rawQuery("select fname, lname, date, amount, gift._id from " + 
                "contact join gift on contact.tnt_people_id=gift.tnt_people_id;", null);
        adapter = new SimpleCursorAdapter(getActivity(),
            R.layout.gift_list_item, cursor, columns, fields);

        setListAdapter(adapter);

    }
}
