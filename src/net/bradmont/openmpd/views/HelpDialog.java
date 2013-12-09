package net.bradmont.openmpd.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;



public class HelpDialog{
    public static void showHelp(Context context, int title_id, int body_id){
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(title_id);
        ad.setMessage(body_id);
        ad.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int id) { }
        });
        ad.show();
        return;
    }
}
