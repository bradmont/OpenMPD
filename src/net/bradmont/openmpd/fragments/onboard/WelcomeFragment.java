package net.bradmont.openmpd.fragments.onboard;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ViewFlipper;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.R;
import net.bradmont.openmpd.views.HelpDialog;
import net.bradmont.openmpd.helpers.Analytics;
import net.bradmont.holograph.BarGraph;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;



import net.bradmont.openmpd.views.StringPicker;
import net.bradmont.openmpd.controllers.TntImporter;



public class WelcomeFragment extends Fragment implements View.OnClickListener {

    StringPicker mPicker;
    private static String [] service_defs = null;
    private static String [] service_names = null;
    private static String [] service_urls = null;

    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.onboard_flipper, null);

        ((TextView) view.findViewById(R.id.action_welcome_next))
            .setOnClickListener(this);
        ((TextView) view.findViewById(R.id.action_login_next))
            .setOnClickListener(this);

        if (service_defs == null){
            service_defs = readServicesList(R.raw.tnt_organisations);
        }
        if (service_names == null){
            service_names = new String[service_defs.length];
            service_urls = new String[service_defs.length];
            for (int i = 0; i < service_defs.length; i++){
                String [] parts = TntImporter.csvLineSplit(service_defs[i]);
                service_names[i] = parts[0];
                service_urls[i] = parts[1];
            }
        }
        StringPicker mPicker = (StringPicker) view.findViewById(R.id.string_picker);
        mPicker.setValues(service_names);

        return view;
    }

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onResume(){
        super.onResume();
    }
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.action_welcome_next:
            case R.id.action_login_next:
                ((ViewFlipper) getView().findViewById(R.id.onboard_flipper)).showNext();
                break;
        }

    }

    public String [] readServicesList(int resource_id){
        InputStream inputStream = getActivity().getResources().openRawResource(resource_id);
        InputStreamReader inputReader = new InputStreamReader (inputStream);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String line;
        ArrayList<String> lines = new ArrayList<String>();

        try {
            while (( line = buffReader.readLine()) != null) {
                lines.add(line);
            }
            buffReader.close();
            inputReader.close();
            inputStream.close();
        } catch (IOException e) {
                return null;
        }
        return lines.toArray(new String[lines.size()]);

    }

}
