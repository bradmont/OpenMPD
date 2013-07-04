package net.bradmont.openmpd.controllers;

import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.Service;
import net.bradmont.supergreen.models.*;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.message.*;

import android.widget.ProgressBar;
import android.util.Log;

import java.lang.Runnable;
import java.lang.Thread;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;

public class ContactsEvaluator implements Runnable{

    private OpenMPD app;
    private ProgressBar progressbar;

    public ContactsEvaluator(OpenMPD app, ProgressBar progressbar){
        this.app = app;
        this.progressbar = progressbar;
    }

    public void run(){
        progressbar.setIndeterminate(true);
        ModelList contacts = new Contact().getAll(); // all!
        progressbar.setIndeterminate(false);
        progressbar.setMax(contacts.size());
        for (int i=0; i < contacts.size(); i++){
            ((Contact) contacts.get(i)).updateStatus();
            progressbar.incrementProgressBy(1);
        }
    }

}
