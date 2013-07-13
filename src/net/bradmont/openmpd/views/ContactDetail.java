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

import java.lang.Runnable;

public class ContactDetail extends Fragment implements OnClickListener{
    public static final String [] columns = {"fname", "lname", "spouse_fname", "partner_type", "giving_amount", "status", "notes", "last_gift", "email_address", "phone_number", "addr1", "addr2", "addr3", "addr4", "city", "region", "post_code", "country_short"};
    public static final int [] fields = {R.id.fname, R.id.lname, R.id.spouse_fname, R.id.partner_type, R.id.giving_amount, R.id.status, R.id.notes, R.id.last_gift, R.id.email_address, R.id.phone_number, R.id.addr1, R.id.addr2, R.id.addr3, R.id.addr4, R.id.city, R.id.region, R.id.post_code, R.id.country_short};

    private Cursor cursor = null;
    private LinearLayout header = null;
    private View layout;

    private Contact contact = null;
    private Contact spouse = null;
    private ContactStatus status  = null;

    public ContactDetail(int id){
        contact = new Contact(id);
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

        layout = inflater.inflate(R.layout.contact_detail, null);

        String [] values = new String[columns.length];
        values[0] = contact.getString("fname");
        values[1] = contact.getString("lname");

        try {
            spouse = (Contact) contact.getRelated("spouse");
            values[2] = spouse.getString("fname");
        } catch (Exception e){
            values[2] = "";
        }

        try {
            status = (ContactStatus) MPDDBHelper.getReferenceModel("contact_status").getByField("contact_id", contact.getInt("id"));
            values[3] = getActivity().getResources()
                            .getString(ContactStatus.partnership(status.getInt("partner_type")));
            values[4] = "$" + Integer.toString(status.getInt("giving_amount")/100);
            values[5] = Integer.toString(status.getInt("status"));
            values[6] = status.getString("notes");
            values[7] = status.getString("last_gift");
        } catch (Exception e){
            values[3] = values[4] = values[5] = values[6] = values[7] = "";
        }

        // email
        try {
            EmailAddress email = (EmailAddress) MPDDBHelper
                    .getModelByField("email_address", "contact_id", contact.getInt("id"));
            values[8] = email.getString("address");
        } catch (Exception e){
            values[8] = "";
        }

        // phone number
        try {
            PhoneNumber phone = (PhoneNumber) MPDDBHelper
                    .getModelByField("phone_number", "contact_id", contact.getInt("id"));
            values[9] = phone.getString("number");
        } catch (Exception e){
            values[9] = "";
        }

        // address
        try {
            Address address = (Address) MPDDBHelper
                    .getModelByField("address", "contact_id", contact.getInt("id"));

            values[9] = address.getString("addr1");
            values[11] = address.getString("addr2");
            values[12] = address.getString("addr3");
            values[13] = address.getString("addr4");
            values[14] = address.getString("city");
            values[15] = address.getString("region");
            values[16] = address.getString("post_code");
            values[17] = address.getString("country_short");
        } catch (Exception e){
            values[9] = values[11] = values[12] = values[13] = values[14] = 
                values[15] = values[16] = values[17] = "";
        }
        populateView(layout, fields, values);

        // set up gift list
        ListView gift_list = (ListView) layout.findViewById(R.id.gift_list);
        ModelList gifts = MPDDBHelper
                .getReferenceModel("gift")
                .filter("tnt_people_id", contact.getString("tnt_people_id"));
        gifts.setOrderBy("date desc");
        cursor = gifts.getCursor();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
            R.layout.contact_gift_list_item,
            cursor,
            new String [] {"date", "amount"},
            new int [] {R.id.date, R.id.amount});

        adapter.setViewBinder( new SimpleCursorAdapter.ViewBinder(){
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (columnIndex){
                    case 1:
                    TextView tv = (TextView) view;
                    tv.setText( String.format("$%.2f", cursor.getFloat(1)/100f));
                    return true;
                }
                return false;
            }
        });
        gift_list.setAdapter(adapter);
        return layout;
    }

    private void populateView(View layout, int [] fields, String [] values){
        for (int i = 0; i < fields.length; i++){
            TextView v = (TextView) layout.findViewById(fields[i]);
            v.setText(values[i]);
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
            case R.id.evaluate_button:
                break;
        }
    }

}
