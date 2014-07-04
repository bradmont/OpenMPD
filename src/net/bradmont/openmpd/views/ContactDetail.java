package net.bradmont.openmpd.views;

import net.bradmont.supergreen.models.*;
import net.bradmont.holograph.BarGraph;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.controllers.QuickMessenger;
import net.bradmont.openmpd.views.cards.*;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Runnable;

public class ContactDetail extends SherlockFragment implements OnClickListener{
    private HashMap<String, String> values = new HashMap();

    private Cursor cursor = null;
    private LinearLayout header = null;
    private View layout;

    private Contact contact = null;
    private Contact spouse = null;
    private ContactStatus status  = null;

    public static final int EMAIL = 1;
    public static final int PHONE = 2;
    public static final int ADDRESS = 3;
    public static final int NOTES = 4;

    public ContactDetail(int id){
        contact = new Contact(id);
    }

    public ContactDetail(){
        this.contact = null;
    }
    public ContactDetail(Contact contact){
        this.contact=contact;
    }

    public void setContact(int id){
        contact = new Contact(id);
    }
    public void setContact(Contact contact){
        this.contact=contact;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, 
            Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey("contact_id")){
            contact = new Contact(savedInstanceState.getInt("contact_id"));
        }


        layout = inflater.inflate(R.layout.contact_detail, null);
        setHasOptionsMenu(true);


        values.put("fname", contact.getString("fname"));
        values.put("lname", contact.getString("lname"));

        // spouse
        try {
            spouse = (Contact) contact.getRelated("spouse");
            values.put("spouse_fname", spouse.getString("fname"));
            if (spouse.getString("lname").equals(contact.getString("lname"))){
                values.put("name", 
                    contact.getString("fname") + " "+
                        getActivity().getResources().getString(R.string.and) + " " +
                        spouse.getString("fname") + " " +
                        contact.getString("lname")
                );
            } else {
                values.put("name", 
                    contact.getString("fname") + " "+
                        contact.getString("lname") + " " +
                        getActivity().getResources().getString(R.string.and) + " " +
                        spouse.getString("fname") + " " +
                        spouse.getString("lname"));
            }
        } catch (Exception e){ 
            values.put("name", 
                contact.getString("fname") + " "+
                    contact.getString("lname"));
        }
        ((BaseActivity)getActivity()).getSupportActionBar().setTitle(values.get("name"));

        ArrayList<Link> links = new ArrayList<Link>(4);

        // email
        try {
            EmailAddress email = (EmailAddress) MPDDBHelper
                    .getModelByField("email_address", "contact_id", contact.getInt("id"));
            values.put("email_address", email.getString("address"));
            Link link = new Link();
            link.title = R.string.Email;
            link.value = email.getString("address");
            link.type = EMAIL;
            if (link.value != "" && link.value != null && link.value.length() > 1){
                links.add(link);
            }
        } catch (Exception e){ }

        // phone number
        try {
            PhoneNumber phone = (PhoneNumber) MPDDBHelper
                    .getModelByField("phone_number", "contact_id", contact.getInt("id"));
            values.put("phone_number", phone.getString("number"));
            Link link = new Link();
            link.title = R.string.Phone;
            link.value = phone.getString("number");
            link.type = PHONE;
            if (link.value != "" && link.value != null && link.value.length() > 1){
                links.add(link);
            }
        } catch (Exception e){ }

        // address
        try {
            Address address = (Address) MPDDBHelper
                    .getModelByField("address", "contact_id", contact.getInt("id"));

            values.put("addr1", address.getString("addr1"));
            String fullAddress = address.getString("addr1");
            if (address.getString("addr2") != null
                && !address.getString("addr2").equals("")){
                values.put("addr2", address.getString("addr2"));
                fullAddress = fullAddress + ", " + values.get("addr2");
            }
            if (address.getString("addr3") != null
                && !address.getString("addr3").equals("")){
                values.put("addr3", address.getString("addr3"));
                fullAddress = fullAddress + ", " + values.get("addr3");
            }
            if (address.getString("addr4") != null
                && !address.getString("addr4").equals("")){
                values.put("addr4", address.getString("addr4"));
                fullAddress = fullAddress + ", " + values.get("addr4");
            }
            values.put("city", address.getString("city"));
            fullAddress = fullAddress + ", " + values.get("city");
            values.put("region", address.getString("region"));
            fullAddress = fullAddress + ", " + values.get("region");
            values.put("post_code", address.getString("post_code"));
            fullAddress = fullAddress + ", " + values.get("post_code");
            values.put("country_short", address.getString("country_short"));

            Link link = new Link();
            link.title = R.string.Address;
            link.value = fullAddress;
            link.type = ADDRESS;
            if (link.value != "" && link.value != null){
                links.add(link);
            }
        } catch (Exception e){ }

        // Partner status & notes
        try {
            status = (ContactStatus) MPDDBHelper.getReferenceModel("contact_status").getByField("contact_id", contact.getInt("id"));

            String partner_type = getActivity().getResources()
                            .getString(ContactStatus.partnership(status.getInt("partner_type")));
            String giving_amount = "$" + Integer.toString(status.getInt("giving_amount")/100);
            String giving_status = 
                getActivity().getResources().getString(
                    ContactStatus.getStatusStringRes(status.getInt("status")));
            
            // header will be different depending on partner type
            if (status.getInt("partner_type") >= ContactStatus.PARTNER_ANNUAL){
                String subTitle = giving_amount + partner_type + ", " + giving_status;
                ((BaseActivity)getActivity()).getSupportActionBar().setSubtitle(subTitle);
            } else if (status.getInt("partner_type") >= ContactStatus.PARTNER_ONETIME){
                // TODO: set up last_gift in partner_status
                String subTitle = partner_type + ". " + 
                    getActivity().getResources().getString(R.string.last_gift) + 
                    status.getString("last_gift");
                ((BaseActivity)getActivity()).getSupportActionBar().setSubtitle(subTitle);
            } else {
                String subTitle = partner_type;
                ((BaseActivity)getActivity()).getSupportActionBar().setSubtitle(subTitle);
            }
            Link link = new Link();
            link.title = R.string.Notes;
            link.value = status.getString("notes");
            link.type = NOTES;
            if (link.value != "" && link.value != null && link.value.length() > 1){
                links.add(link);
            }
        } catch (Exception e){ }

        // Add links to the view
        LinearLayout linkList = (LinearLayout) layout.findViewById(R.id.contactinfo_list);
        for (int i = 0; i < links.size(); i++){
            Log.i("net.bradmont.openmpd", "Adding view " + links.get(i).title);
            View v = buildLinkView(links.get(i), linkList);
            v.setLayoutParams(linkList.getLayoutParams());
            linkList.addView(v);
        }

        // barGraph
        if (!buildGraph((BarGraph) layout.findViewById(R.id.gifts_graph), contact.getString("tnt_people_id"))){
            // Hide bar graph if no gifts in last year
            layout.findViewById(R.id.bar_graph_layout).setVisibility(View.GONE);
        }
        return layout;
    }
    @Override
        public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.contact_detail, menu);
    }
    private void editNotes(){
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        Log.i("net.bradmont.openmpd", "menu_notes");
        ad.setTitle(R.string.Notes);
        final EditText notes_text = (EditText) ((LayoutInflater) getActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.notes_text, null);

        notes_text.setText(status.getString("notes"));
        ad.setView(notes_text);
        final ContactStatus local_status = status;
        ad.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                local_status.setValue("notes", notes_text.getText().toString());
                local_status.dirtySave();
                ((BaseActivity)getActivity()).switchContent(new ContactDetail(contact.getID()));
            }
        });
        ad.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        ad.show();
    }

    @Override
    public boolean onOptionsItemSelected (com.actionbarsherlock.view.MenuItem item){
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        switch (item.getItemId() ){
            case R.id.menu_gift_history:
                Log.i("net.bradmont.openmpd", "menu_gift_history");
                ad.setTitle(R.string.gift_history);
                ListView gift_list = (ListView) ((LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.list, null);

                String [] args = new String[1]; args[0] = contact.getString("tnt_people_id");
                Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery(
                    "select _id, date, amount as amount from gift where tnt_people_id=? order by date desc; " , args);

                SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                    R.layout.contact_gift_list_item,
                    cur,
                    new String [] {"date", "amount"},
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
            case R.id.menu_help:
                HelpDialog.showHelp(getActivity(), R.string.help_contact_title, R.string.help_contact);
            return true;
        }
        return false;
    }



    private boolean buildGraph(BarGraph graph, String tnt_people_id){
        String [] args = new String [1];
        args[0] = contact.getString("tnt_people_id");

        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery(
            "select sum(amount) from (select amount, a.month from (select distinct month from gift order by month desc limit 13) a join gift b on a.month=b.month where tnt_people_id=?);", args);
        cur.moveToFirst();
        if (cur.getInt(0) == 0){
            // if no gifts in last 13 months, return false
            return false;
        }

        cur = MPDDBHelper.get().getReadableDatabase().rawQuery(
            "select a.month, group_concat(b.amount) from (select distinct month from gift order by month desc) a left outer join (select * from gift where tnt_people_id=?) b on a.month=b.month group by a.month order by a.month; " , args);
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
            for (int j = 0; j < gifts.length; j++){
                values[i][j] = new Float( Float.parseFloat(gifts[j])/100f);
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
            outState.putInt("contact_id", contact.getID());
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

    public View buildLinkView(final Link link, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.contact_link_layout, parent, false);

        TextView title = (TextView) rowView.findViewById(R.id.title);
        TextView value = (TextView) rowView.findViewById(R.id.value);
        TextView subtitle = (TextView) rowView.findViewById(R.id.subtitle);

        title.setText(link.title);
        value.setText(link.value);
        if (link.subtitle != null){
            subtitle.setText(link.subtitle);
        } else {
            subtitle.setVisibility(View.GONE);
        }
        value.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = null;
                switch (link.type){
                    case EMAIL:
                        // TODO: Log contact
                        QuickMessenger q = new QuickMessenger(getActivity(), contact);
                        q.showQuickMessageDialog();
                        break;
                    case PHONE:
                        // TODO: Log contact
                        String number = link.value.replaceAll("[^\\d]", "");
                        number = "tel:" + number;
                        intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(number));
                        getActivity().startActivity(intent);
                        break;
                    case ADDRESS:
                        String uri = "geo:0,0?q=" + Uri.encode(link.value);
                        intent = new Intent(android.content.Intent.ACTION_VIEW, 
                            Uri.parse(uri));
                        getActivity().startActivity(intent);
                        break;
                    case NOTES:
                        editNotes();
                        break;
                }
            }
        });
        return rowView;
    }
}
