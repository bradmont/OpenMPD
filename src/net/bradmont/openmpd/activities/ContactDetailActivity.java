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

import android.util.Log;

import net.bradmont.openmpd.fragments.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.R;


public class ContactDetailActivity extends ActionBarActivity {

    private int mContactId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mContactId = intent.getIntExtra("contactId", 1);

        setContentView(R.layout.container_contact_detail);

        ContactDetailFragment contactDetailFragment = (ContactDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contact_detail_fragment);
        contactDetailFragment.setContact(mContactId);

    }


    private void toast(int message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT)
            .show();
    }
}


