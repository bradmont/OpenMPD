package net.bradmont.openmpd.fragments;

import net.bradmont.openmpd.dao.*;
import net.bradmont.holograph.BarGraph;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.views.*;
import net.bradmont.openmpd.controllers.QuickMessenger;
import net.bradmont.openmpd.controllers.ContactsEvaluator;
import net.bradmont.openmpd.controllers.TntImporter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.sqlite.*;
import android.database.Cursor;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import net.bradmont.openmpd.helpers.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;


import android.app.ActionBar;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Runnable;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactDetailFragment extends Fragment implements OnClickListener{
    private HashMap<String, String> values = new HashMap();

    private Cursor cursor = null;
    private LinearLayout header = null;
    private View layout;

    private Contact contact = null;
    private Person mPrimaryPerson = null;
    private Person mSpouse = null;
    private List<Person> mPeople = null;
    private List<ContactDetail> mDetails = null;

    private ContactStatus status  = null;

    public static final int EMAIL = 1;
    public static final int PHONE = 2;
    public static final int ADDRESS = 3;
    public static final int NOTES = 4;

    public ContactDetailFragment(int id){
        contact = OpenMPD.getDaoSession().getContactDao().load((long)id);
    }

    public ContactDetailFragment(){
        this.contact = null;
    }
    public ContactDetailFragment(Contact contact){
        this.contact=contact;
    }

    public void setContact(int id){
        setContact((long) id);
    }
    public void setContact(long id){
        contact = OpenMPD.getDaoSession().getContactDao().load(id);
        populateView();
    }
    public void setContact(Contact contact){
        this.contact=contact;
        populateView();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey("contact_id")){
            setContact(savedInstanceState.getLong("contact_id"));
        }


        layout = inflater.inflate(R.layout.contact_detail, null);
        setHasOptionsMenu(true);
        if (contact != null){
            populateView();
        }
        setRetainInstance(true);
        if (contact != null){
            populateView();
        }
        return layout;
    }
    private void populateView(){
        View layout = getView();
        if (layout == null) return;

        mPrimaryPerson = contact.getPrimaryPerson();
        mSpouse = contact.getTntSpouse();

        values.put("fname", mPrimaryPerson.getFname());
        values.put("lname", mPrimaryPerson.getLname());

        if (mSpouse != null){
            values.put("spouse_fname", mSpouse.getFname());
            if (mSpouse.getLname().equals(mPrimaryPerson.getLname())){
                values.put("name", 
                    mPrimaryPerson.getFname()+ " "+
                        getActivity().getResources().getString(R.string.and) + " " +
                        mSpouse.getFname()+ " " +
                        mPrimaryPerson.getLname());
            } else {
                values.put("name", 
                    mPrimaryPerson.getFname()+ " "+ mPrimaryPerson.getLname()+ " " +
                        getActivity().getResources().getString(R.string.and) + " " +
                        mSpouse.getFname()+ " " + mSpouse.getLname());
            }
        } else { 
            values.put("name", 
                mPrimaryPerson.getFname()+ " "+ mPrimaryPerson.getLname());
        }
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(values.get("name"));

        loadDetails();
        // Status
        status = contact.getStatus();
        if (status != null){
            String partner_type = status.getType();
            String giving_status = status.getStatus();
            String subTitle = "";
            
            // header will be different depending on partner type
            if (status.getType().equals("annual") ||
                status.getType().equals("regular") ||
                status.getType().equals("monthly")){
                String giving_amount = "$" + Long.toString(status.getGivingAmount()/100);
                subTitle = giving_amount + partner_type + ", " + giving_status;
            } else if (status.getType().equals("frequent") ||
                status.getType().equals("occasional") ||
                status.getType().equals("onetime")){
                subTitle = partner_type + ". " + 
                    getActivity().getResources().getString(R.string.last_gift) + 
                    status.getLastGift();
            } else {
                subTitle = partner_type;
            }
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(subTitle);
        } 

        // barGraph
        if (!buildGraph((BarGraph) layout.findViewById(R.id.gifts_graph) )){
            // Hide bar graph if no gifts in last year
            layout.findViewById(R.id.bar_graph_layout).setVisibility(View.GONE);
        }
    }
    @Override
        public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.contact_detail, menu);
    }


    private void loadDetails(){
        contact.resetOrderedDetails();
        mDetails = contact.getOrderedDetails();

        // Add details to the view
        LinearLayout linkList = (LinearLayout) layout.findViewById(R.id.contactinfo_list);
        linkList.removeAllViews();
        String lastType = "";
        for (ContactDetail detail : mDetails){
            View v = null;
            if (lastType.equals(detail.getType())){
                v = buildLinkView(detail, linkList, false);
            } else {
                v = buildLinkView(detail, linkList, true);
                lastType = detail.getType();
            }
            v.setLayoutParams(linkList.getLayoutParams());
            linkList.addView(v);
        }
    }
    private void editNotes(){
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        Log.i("net.bradmont.openmpd", "menu_notes");
        ad.setTitle(R.string.Notes);
        final EditText notes_text = (EditText) ((LayoutInflater) getActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.notes_text, null);

        ContactDetail note = contact.getNoteOrNew();
        try {
            JSONObject json = new JSONObject(note.getData());
            notes_text.setText(json.getString("note"));
        } catch (JSONException | NullPointerException e){}
        ad.setView(notes_text);
        final ContactDetail local_note = note;
        ad.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                JSONObject json = new JSONObject();
                try {
                    json.put("note", notes_text.getText().toString());
                } catch (JSONException e){}
                local_note.setData(json.toString());
                local_note.setLabel(getActivity().getResources().getString(R.string.Notes));
                local_note.setAddedDate(new java.util.Date());
                OpenMPD.getDaoSession().getContactDetailDao().insertOrReplace(local_note);
                loadDetails();
            }
        });
        ad.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        ad.show();
    }
    private void addEmail(){
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        ad.setTitle(R.string.add_email);
        LinearLayout layout = (LinearLayout) ((LayoutInflater) getActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.dialog_new_email, null);

        final EditText emailView = (EditText) layout.findViewById(R.id.email);
        final Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);

        ad.setView(layout);

        ad.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ContactDetail detail = new ContactDetail();
                detail.setContact(contact);
                detail.setType("email");
                JSONObject json = new JSONObject();
                try {
                    json.put("email", emailView.getText().toString());
                } catch (JSONException e){}
                detail.setData(json.toString());
                detail.setLabel(spinner.getSelectedItem().toString());
                detail.setAddedDate(new java.util.Date());
                OpenMPD.getDaoSession().getContactDetailDao().insertOrReplace(detail);
                loadDetails();
            }
        });
        ad.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        ad.show();
    }
    private void addPhone(){
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        ad.setTitle(R.string.add_phone);
        LinearLayout layout = (LinearLayout) ((LayoutInflater) getActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.dialog_new_phone, null);

        final EditText phoneView = (EditText) layout.findViewById(R.id.phone);
        final Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);

        ad.setView(layout);

        ad.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ContactDetail detail = new ContactDetail();
                detail.setContact(contact);
                detail.setType("phone");
                JSONObject json = new JSONObject();
                try {
                    json.put("number", phoneView.getText().toString());
                } catch (JSONException e){}
                detail.setData(json.toString());
                detail.setLabel(spinner.getSelectedItem().toString());
                detail.setAddedDate(new java.util.Date());
                OpenMPD.getDaoSession().getContactDetailDao().insertOrReplace(detail);
                loadDetails();
            }
        });
        ad.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        ad.show();
    }
    private void addAddress(){
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        ad.setTitle(R.string.add_address);
        LinearLayout layout = (LinearLayout) ((LayoutInflater) getActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.dialog_new_address, null);

        final EditText addr1View = (EditText) layout.findViewById(R.id.addr1);
        final EditText addr2View = (EditText) layout.findViewById(R.id.addr2);
        final EditText cityView = (EditText) layout.findViewById(R.id.city);
        final EditText regionView = (EditText) layout.findViewById(R.id.region);
        final EditText postCodeView = (EditText) layout.findViewById(R.id.post_code);
        final EditText countryView = (EditText) layout.findViewById(R.id.country);
        final Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);

        ad.setView(layout);

        ad.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ContactDetail detail = new ContactDetail();
                detail.setContact(contact);
                detail.setType("address");
                JSONObject json = new JSONObject();
                try {
                    json.put("addr1", addr1View.getText().toString());
                    json.put("addr2", addr2View.getText().toString());
                    json.put("addr3", "");
                    json.put("addr4", "");
                    json.put("city", cityView.getText().toString());
                    json.put("region", regionView.getText().toString());
                    json.put("post_code", postCodeView.getText().toString());
                    json.put("country_short", countryView.getText().toString());
                    json.put("country_long", countryView.getText().toString());
                } catch (JSONException e){}
                detail.setData(json.toString());
                detail.setLabel(spinner.getSelectedItem().toString());
                detail.setAddedDate(new java.util.Date());
                OpenMPD.getDaoSession().getContactDetailDao().insertOrReplace(detail);
                loadDetails();
            }
        });
        ad.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        ad.show();
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        switch (item.getItemId() ){
            case R.id.menu_gift_history:
                Log.i("net.bradmont.openmpd", "menu_gift_history");
                ad.setTitle(R.string.gift_history);
                ListView gift_list = (ListView) ((LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.dialog_list, null);

                String [] args = new String[1]; args[0] = Long.toString(contact.getId());
                Cursor cur = OpenMPD.getDB().rawQuery(
                    "select _id, date, amount from gift where contact_id=? order by date desc; " , args);

                SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.contact_gift_list_item,
                    cur,
                    new String [] {"DATE", "AMOUNT"},
                    new int [] {R.id.date, R.id.amount});
                
                adapter.setViewBinder( new SimpleCursorAdapter.ViewBinder(){
                    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                        switch (columnIndex){
                            case 2:
                            TextView tv = (TextView) view;
                            tv.setText( String.format("$%.2f", cursor.getFloat(2)/100f));
                            return true;
                        }
                        return false;
                    }
                });
                gift_list.setAdapter(adapter);
                ad.setView(gift_list);
                ad.show();
                return true;
            case R.id.menu_notes:
                editNotes();
                return true;
            case R.id.add_email:
                addEmail();
                return true;
            case R.id.add_phone:
                addPhone();
                return true;
            case R.id.add_address:
                addAddress();
                return true;
            case R.id.menu_help:
                HelpDialog.showHelp(getActivity(), R.string.help_contact_title, R.string.help_contact);
                return true;
            case R.id.menu_evaluate:
                status = ContactsEvaluator.evaluateContact(contact, false);
                OpenMPD.getDaoSession().getContactStatusDao().insertOrReplace(status);
                getActivity().finish();
                getActivity().startActivity(getActivity().getIntent());
            return true;
        }
        return false;
    }



    private boolean buildGraph(BarGraph graph){
        String [] args = new String [1];

        args[0] = Long.toString(contact.getId());

        Cursor cur = OpenMPD.getDB().rawQuery(
            "select sum(amount) from (select amount, a.month from (select month from months order by month desc limit 13) a join gift b on a.month=b.month where contact_id=?);", args);
        cur.moveToFirst();
        if (cur.getInt(0) == 0){
            // if no gifts in last 13 months, return false
            cur.close();
            return false;
        }
        cur.close();

        cur = OpenMPD.getDB().rawQuery(
            "select a.month, group_concat(b.amount) from months a left outer join (select * from gift where contact_id=?) b on a.month=b.month group by a.month order by a.month; " , args);
        Float [][] values = new Float[cur.getCount()][];
        String [] labels = new String[cur.getCount()];
        cur.moveToFirst();
        for (int i = 0; i < cur.getCount(); i++){
            String [] gifts;
            if (cur.getString(1) != null){
                gifts = cur.getString(1).split(",");
            } else {
                gifts = new String[0];
            }
            values[i] = new Float[gifts.length];
            boolean negatives = false;
            float total = 0;
            for (int j = 0; j < gifts.length; j++){
                values[i][j] = new Float( Float.parseFloat(gifts[j])/100f);
                total = Float.parseFloat(gifts[j])/100f;
                if (values[i][j] < 0){
                    negatives = true;
                }
            }
            if (negatives == true){
                // if we have negative gifts in this month, combine month
                // total into a single gift rather than dealing with
                // ugly graphs
                values[i] = new Float[1];
                values[i][0] = new Float(total);
            }
            labels[i] = cur.getString(0);
            cur.moveToNext();
        }
        graph.setValues(values);
        graph.setLabels(labels);
        return true;
    }
    @Override
    public void onSaveInstanceState (Bundle outState){
        if (contact != null){
            outState.putLong("contact_id", contact.getId());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case 0: 
                break;
        }
    }
    /**
      * Represents a single link with a contact, such as an email
      * address or a phone number
      */
    private class Link {
        public int title = 0;
        public String value = null;
        public String subtitle = null;
        public int type = 0;
    }

    private void populateEmailView(ContactDetail detail, 
            ImageView icon, TextView value, TextView subtitle, TextView date){

        icon.setImageResource(R.drawable.at);
        try {
            JSONObject json = new JSONObject(detail.getData());
            value.setText(json.getString("email"));
        } catch (JSONException e){ 
            value.setText("--ERROR--" );// TODO
        }
        subtitle.setText(detail.getLabel());
        date.setText(new SimpleDateFormat("yyyy-MM-dd").format(detail.getAddedDate()));
    }
    private void populatePhoneView(ContactDetail detail, 
            ImageView icon, TextView value, TextView subtitle, TextView date){

        icon.setImageResource(R.drawable.cellphone_android);
        try {
            JSONObject json = new JSONObject(detail.getData());
            value.setText(json.getString("number"));
        } catch (JSONException e){ 
            value.setText("--ERROR--" );// TODO
        }
        subtitle.setText(detail.getLabel());
        date.setText(new SimpleDateFormat("yyyy-MM-dd").format(detail.getAddedDate()));
    }
    private void populateAddressView(ContactDetail detail, 
            ImageView icon, TextView value, TextView subtitle, TextView date){

        icon.setImageResource(R.drawable.map_marker);
        try {
            JSONObject json = new JSONObject(detail.getData());
            String address = json.getString("addr1");

            for ( String part : new String [] {"addr2", "addr3", "addr4", "city", "region", 
                    "post_code", "country_short" }){
                if (!json.getString(part).equals("")){
                    address = address + ", " + json.getString(part);
                }
            }

            value.setText(address);
        } catch (JSONException e){ 
            value.setText("--ERROR--" );// TODO
        }
        subtitle.setText(detail.getLabel());
        date.setText(new SimpleDateFormat("yyyy-MM-dd").format(detail.getAddedDate()));
    }

    private void populateNoteView(ContactDetail detail, 
            ImageView icon, TextView value, TextView subtitle, TextView date){

        icon.setImageResource(R.drawable.note_text);
        try {
            JSONObject json = new JSONObject(detail.getData());
            value.setText(json.getString("note"));
        } catch (JSONException e){ 
            value.setText("--ERROR--" );// TODO
        }
        subtitle.setText(detail.getLabel());
        date.setText(new SimpleDateFormat("yyyy-MM-dd").format(detail.getAddedDate()));
    }



    public View buildLinkView(final ContactDetail detail, ViewGroup parent, boolean showHeader) {
        LayoutInflater inflater = (LayoutInflater) getActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.contact_link_layout, parent, false);

        ImageView icon = (ImageView) rowView.findViewById(R.id.category_icon);
        TextView value = (TextView) rowView.findViewById(R.id.value);
        TextView subtitle = (TextView) rowView.findViewById(R.id.subtitle);
        TextView date = (TextView) rowView.findViewById(R.id.date);
        if (!showHeader){
            icon.setVisibility(View.INVISIBLE);
        }
        switch (detail.getType()){
            case "email":
                populateEmailView(detail, icon, value, subtitle, date);
                break;
            case "phone":
                populatePhoneView(detail, icon, value, subtitle, date);
                break;
            case "address":
                populateAddressView(detail, icon, value, subtitle, date);
                break;
            case "note":
                populateNoteView(detail, icon, value, subtitle, date);
                break;
        }

        value.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = null;
                JSONObject json = null;
                try {
                    json = new JSONObject(detail.getData());
                } catch (JSONException e){}
                switch (detail.getType()){
                    case "email":
                        // TODO: Log contact
                        //QuickMessenger q = new QuickMessenger(getActivity(), contact);
                        //q.showQuickMessageDialog();
                        ((BaseActivity) getActivity()).userMessage("TODO");
                        break;
                    case "phone":
                        // TODO: Log contact
                        String number ="";
                        try {
                            number = json.getString("number").replaceAll("[^\\d]", "");
                        } catch (JSONException e) { 
                            ((BaseActivity)getActivity()).userMessage("Invalid stored number...");
                            break;
                        }
                        number = "tel:" + number;
                        intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(number));
                        getActivity().startActivity(intent);
                        break;
                    case "address":
                        String uri = "";
                        try {
                            uri = "geo:0,0?q=" + Uri.encode(json.getString("addr1") + ", " + 
                                    json.getString("city") + ", " + json.getString("region") );
                        } catch (JSONException e) { 
                            ((BaseActivity)getActivity()).userMessage("Invalid stored address...");
                            break;
                        }
                        intent = new Intent(android.content.Intent.ACTION_VIEW, 
                            Uri.parse(uri));
                        getActivity().startActivity(intent);
                        break;
                    case "note":
                        editNotes();
                        break;
                }
            }
        });
        return rowView;
    }
}
