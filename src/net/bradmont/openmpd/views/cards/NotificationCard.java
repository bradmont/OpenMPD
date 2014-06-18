package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.controllers.QuickMessenger;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.fima.cardsui.objects.Card;

public abstract class NotificationCard extends Card implements View.OnClickListener{

    protected Notification n = null;
    protected Contact contact;
    protected ContactStatus status;
    protected final BaseActivity activity ;

    protected View content;

	public NotificationCard(final BaseActivity activity, Notification n, Contact contact, ContactStatus status){
		super();
        this.activity = activity;
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
                activity.moveToFragment(new ContactDetail(contact_id));
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
                    activity.userMessage(R.string.no_phone_number);
                }
                if (number.length() == 0){
                    activity.userMessage(R.string.no_phone_number);
                } else {
                    number = "tel:" + number;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(number));
                    activity.startActivity(intent);
                }

                break;
            case R.id.button_quick_email:
                QuickMessenger q = new QuickMessenger(activity, contact, getNotificationType());
                q.showQuickMessageDialog();
                break;
        }
    }

    public abstract String getNotificationType();
    public abstract View buildCardContent(Context context);
	
}
