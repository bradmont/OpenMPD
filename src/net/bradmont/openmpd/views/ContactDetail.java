package net.bradmont.openmpd.views;

import net.bradmont.supergreen.models.*;
import net.bradmont.holograph.BarGraph;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.controllers.ContactsEvaluator;
import net.bradmont.openmpd.controllers.TntImporter;

import android.content.Context;

import android.database.sqlite.*;
import android.database.Cursor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import java.util.HashMap;
import java.lang.Runnable;

public class ContactDetail extends Fragment implements OnClickListener{
    public static final String [] columns = {"fname", "lname", "spouse_fname", "partner_type", "giving_amount", "status", "notes", "last_gift", "email_address", "phone_number", "addr1", "addr2", "addr3", "addr4", "city", "region", "post_code", "country_short", "name"};
    public static final int [] fields = {R.id.fname, R.id.lname, R.id.spouse_fname, R.id.partner_type, R.id.giving_amount, R.id.status, R.id.notes, R.id.last_gift, R.id.email_address, R.id.phone_number, R.id.addr1, R.id.addr2, R.id.addr3, R.id.addr4, R.id.city, R.id.region, R.id.post_code, R.id.country_short, R.id.name};
    private HashMap<String, String> values = new HashMap();

    private Cursor cursor = null;
    private LinearLayout header = null;
    private View layout;

    private Contact contact = null;
    private Contact spouse = null;
    private ContactStatus status  = null;

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

        values.put("fname", contact.getString("fname"));
        values.put("lname", contact.getString("lname"));

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

        try {
            status = (ContactStatus) MPDDBHelper.getReferenceModel("contact_status").getByField("contact_id", contact.getInt("id"));
            values.put("partner_type", getActivity().getResources()
                            .getString(ContactStatus.partnership(status.getInt("partner_type"))));
            values.put("giving_amount", "$" + Integer.toString(status.getInt("giving_amount")/100));
            values.put("status", 
                getActivity().getResources().getString(
                    ContactStatus.getStatusStringRes(status.getInt("status"))
                ));
            
            values.put("notes", status.getString("notes"));
            values.put("last_gift", status.getString("last_gift"));
        } catch (Exception e){ }

        // email
        try {
            EmailAddress email = (EmailAddress) MPDDBHelper
                    .getModelByField("email_address", "contact_id", contact.getInt("id"));
            values.put("email_address", email.getString("address"));
        } catch (Exception e){ }

        // phone number
        try {
            PhoneNumber phone = (PhoneNumber) MPDDBHelper
                    .getModelByField("phone_number", "contact_id", contact.getInt("id"));
            values.put("phone_number", phone.getString("number"));
        } catch (Exception e){ }

        // address
        try {
            Address address = (Address) MPDDBHelper
                    .getModelByField("address", "contact_id", contact.getInt("id"));

            values.put("addr1", address.getString("addr1"));
            if (address.getString("addr2") != null
                && !address.getString("addr2").equals("")){
                values.put("addr2", address.getString("addr2"));
            }
            if (address.getString("addr3") != null
                && !address.getString("addr3").equals("")){
                values.put("addr3", address.getString("addr3"));
            }
            if (address.getString("addr4") != null
                && !address.getString("addr4").equals("")){
                values.put("addr4", address.getString("addr4"));
            }
            values.put("city", address.getString("city"));
            values.put("region", address.getString("region"));
            values.put("post_code", address.getString("post_code"));
            values.put("country_short", address.getString("country_short"));
        } catch (Exception e){ }
        populateView(layout, values);

        // set up gift list
        ListView gift_list = (ListView) layout.findViewById(R.id.gift_list);
        String [] args = new String [1];
        args[0] = contact.getString("tnt_people_id");
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
        return layout;
    }

    @Override
    public void onSaveInstanceState (Bundle outState){
        if (contact != null){
            outState.putInt("contact_id", contact.getID());
        }
    }

    private void populateView(View layout, HashMap<String, String> values){
        for (int i = 0; i < fields.length; i++){
            if (values.containsKey(columns[i])){
                TextView v = (TextView) layout.findViewById(fields[i]);
                if (v != null){
                    v.setText(values.get(columns[i]));
                    v.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final OpenMPD app = (OpenMPD)getActivity();
        String [] args = new String [1];
        args[0] = contact.getString("tnt_people_id");
        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery(
            "select a.month, group_concat(b.amount) from (select distinct month from gift order by month desc) a left outer join (select * from gift where tnt_people_id=?) b on a.month=b.month group by a.month order by a.month; " , args);
        Float [][] values = new Float[cur.getCount()][];
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
            cur.moveToNext();
        }
        BarGraph graph = (BarGraph) getView().findViewById(R.id.gifts_graph);
        graph.setValues(values);


    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case 0: 
                break;
        }
    }

}
