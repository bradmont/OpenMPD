package net.bradmont.openmpd.fragments;

import net.bradmont.supergreen.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.controllers.TntImportService;

import android.content.Context;
import android.content.Intent;

import android.database.sqlite.*;
import android.database.Cursor;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

import java.lang.Runnable;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class DebugFragment extends SherlockFragment {

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView( LayoutInflater inflater, 
            ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.contact_list_header, null);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.debug, menu);
    }

    @Override
    public boolean onOptionsItemSelected (com.actionbarsherlock.view.MenuItem item){
        switch (item.getItemId() ){
            case R.id.menu_refresh:
                Log.i("net.bradmont.openmpd", "menu_refresh");
                ModelList accounts = MPDDBHelper.getReferenceModel("service_account").getAll();
                int [] account_ids = new int [accounts.size()];
                for (int i = 0; i < accounts.size(); i++){
                    account_ids[i] = accounts.get(i).getID();
                }

                getActivity().startService(
                    new Intent(getActivity(), TntImportService.class).putExtra("net.bradmont.openmpd.account_ids", account_ids));

                return true;
        }
        return false;
    }
}
