package net.bradmont.openmpd.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.DialogFragment;

import net.bradmont.openmpd.helpers.Log;

import net.bradmont.openmpd.fragments.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.R;


public class ContactSublistActivity extends ActionBarActivity {

    private String mListName = "";
    private ContactSublistFragment mContactSublistFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mListName = intent.getStringExtra("listName");

        setContentView(R.layout.container_contact_sublist);

        mContactSublistFragment = (ContactSublistFragment) getSupportFragmentManager().findFragmentById(R.id.contact_sublist_fragment);
        mContactSublistFragment.setListName(mListName);

        findViewById(R.id.add_contacts).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mContactSublistFragment.addContacts(v);
            }
        });

    }


    private void toast(int message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT)
            .show();
    }
}


