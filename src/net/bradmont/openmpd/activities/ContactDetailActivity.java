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
import android.view.MenuItem;
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

import com.github.rubensousa.floatingtoolbar.FloatingToolbar;
import android.support.design.widget.FloatingActionButton;



public class ContactDetailActivity extends ActionBarActivity {

    private int mContactId = 1;
    private FloatingToolbar mFloatingToolbar = null;
    private FloatingActionButton mFab = null;
    private ContactDetailFragment mFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mContactId = intent.getIntExtra("contactId", 1);

        setContentView(R.layout.container_contact_detail);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingToolbar = (FloatingToolbar) findViewById(R.id.floatingToolbar);
        mFloatingToolbar.attachFab(mFab);


        mFragment = (ContactDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contact_detail_fragment);
        mFragment.setContact(mContactId);

        mFloatingToolbar.setClickListener(new FloatingToolbar.ItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                mFragment.onOptionsItemSelected(item);
            }
            @Override
            public void onItemLongClick(MenuItem item) {
            }
        });

    }

    @Override public void onBackPressed(){
        if (mFloatingToolbar.isShowing()){
            mFloatingToolbar.hide();
        } else {
            super.onBackPressed();
        }

    }


    private void toast(int message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT)
            .show();
    }
}


