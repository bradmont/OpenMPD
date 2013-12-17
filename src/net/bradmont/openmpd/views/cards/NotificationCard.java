package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.views.ContactDetail;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.fima.cardsui.objects.Card;

public abstract class NotificationCard extends Card implements View.OnClickListener{

    protected Notification n = null;
    protected Contact contact;
    protected ContactStatus status;

    protected View content;

	public NotificationCard(Notification n, Contact contact, ContactStatus status){
		super();
        this.n = n;
        this.contact=contact;
        this.status=status;
        setOnCardSwipedListener( new OnCardSwiped(){
            @Override
            public void onCardSwiped(Card card, View layout) {
                ((NotificationCard)card).dismissNotification();
            }
        });
        final int contact_id = n.getInt("contact");
        setOnClickListener( new OnClickListener(){
            @Override
            public void onClick(View v){
                OpenMPD.getInstance().moveToFragment(new ContactDetail(contact_id));
            }
        });
	}

    public void dismissNotification(){
        n.setValue("status", Notification.STATUS_ACKNOWLEDGED);
        n.dirtySave();
    }
	@Override
    public View getCardContent(Context context) {
        if (content == null){
            content = buildCardContent(context);
            return content;
        }
        if (content.getParent() != null){
            ((ViewGroup)content.getParent()).removeView(content);
        }
        return content;

    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_quick_call:
                String number = "";
                PhoneNumber phone = (PhoneNumber) MPDDBHelper
                    .getModelByField("phone_number", "contact_id", contact.getInt("id"));
                try {
                    number = phone.getString("number").replaceAll("[^\\d]", "");
                } catch (Exception e){
                    OpenMPD.getInstance().userMessage(R.string.no_phone_number);
                }
                if (number.length() == 0){
                    OpenMPD.getInstance().userMessage(R.string.no_phone_number);
                } else {
                    number = "tel:" + number;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(number));
                    OpenMPD.getInstance().startActivity(intent);
                }

                break;
            case R.id.button_quick_email:
                OpenMPD.getInstance().userMessage("Quick email");
                showQuickMessageDialog();
                break;
        }
    }
    public void showQuickMessageDialog(){

        String sql = "select _id, name, subject, body from quick_message where " + getQuickMessageFilter() + " or notification_type='blank' order by name;";
        AlertDialog.Builder ad = new AlertDialog.Builder(OpenMPD.getInstance());
        ad.setTitle(R.string.choose_a_template);

        // set up listview
        ListView message_list = (ListView) ((LayoutInflater) OpenMPD.getInstance()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.list, null);
        Cursor cur = MPDDBHelper.get().getReadableDatabase().rawQuery(sql, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(OpenMPD.getInstance(),
            R.layout.quick_message_list_item, cur,
            new String [] {"name"},
            new int [] {R.id.name});
        message_list.setAdapter(adapter);
        final AlertDialog dialog = ad.create();

        message_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                int position, long id) {
                QuickMessage message = new QuickMessage((int)id);
                if (message.getString("notification_type").equals("blank")){
                    sendMessage(message, "", "");
                    dialog.dismiss();
                } else {
                    showPreview(message, dialog);
                }
            }
        });

        dialog.setView(message_list);
        dialog.show();
    }
    public void showPreview(final QuickMessage message, final Dialog parentDialog){
        // show a preview of the message, and the option to show it
        AlertDialog.Builder builder = new AlertDialog.Builder(OpenMPD.getInstance());

        final String body = message.getString("body").replace("$name", contact.getString("fname"));
        final String subject = message.getString("subject").replace("$name", contact.getString("fname"));

        builder.setMessage(body)
                .setTitle(message.getString("subject"));

        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendMessage(message, subject, body);
                parentDialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { }
        });

        builder.show();

    }
    private void sendMessage(QuickMessage message, String subject, String body){
        String recipient_address = "";
        try {
            EmailAddress email = (EmailAddress) MPDDBHelper
                .getModelByField("email_address", "contact_id", contact.getInt("id"));
            recipient_address = email.getString("address");
        } catch (Exception e){
            Log.i("net.bradmont.openmpd", "Problem getting email address");
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String [] {recipient_address});
        if (!message.getString("notification_type").equals("blank")){
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
        }

        OpenMPD.getInstance()
            .startActivity(
                Intent.createChooser(intent, "Send Email"));
    }

    public abstract String getQuickMessageFilter();
    public abstract View buildCardContent(Context context);
	
}
