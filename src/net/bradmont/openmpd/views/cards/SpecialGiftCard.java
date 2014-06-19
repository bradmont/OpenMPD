package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.views.ContactDetail;
import net.bradmont.openmpd.views.cards.GraphCard;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.PopupMenu;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


import com.fima.cardsui.objects.Card;

public class SpecialGiftCard extends NotificationCard implements PopupMenu.OnMenuItemClickListener {

	public SpecialGiftCard(BaseActivity a, Notification n, final Contact contact, ContactStatus status){
		super(a, n, contact, status);


	}

	@Override
	public View buildCardContent(final Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_notification, null);
		TextView title = ((TextView) view.findViewById(R.id.title));
		TextView description = ((TextView) view.findViewById(R.id.description));
        ((ImageView) view.findViewById(R.id.stripe)).setBackgroundResource(R.color.purple);
        ((TextView) view.findViewById(R.id.title)).setTextColor(
            context.getResources().getColor(R.color.purple)
            );
		TextView date = ((TextView) view.findViewById(R.id.date));

        // set date
        date.setText(n.getString("date"));

        // set title
        String title_string = String.format(
                context.getResources().getString(R.string.special_gift_title),
                contact.getString("fname") + " " + contact.getString("lname"));
        title.setText(title_string);

        int partnership = status.partnership(status.getInt("partner_type"));
        float amount = 0f;
        try{
            amount = Float.parseFloat(n.getString("message"));
        } catch (Exception e){}

        String text = String.format("%s %s gave $%.2f",
                contact.getString("fname"),
                contact.getString("lname"),
                amount/100f);

        description.setText(text);

        // overflow listener
        final PopupMenu.OnMenuItemClickListener menulistener = this;
        view.findViewById(R.id.overflow).setVisibility(View.VISIBLE);
        view.findViewById(R.id.overflow).setOnClickListener( new OnClickListener(){
            @Override
            public void onClick(View v){
                PopupMenu popup = new PopupMenu(context, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.special_gift_card_actions, popup.getMenu());
                popup.setOnMenuItemClickListener(menulistener);
                popup.show();
            }
        });

        // add click handlers to buttons
        view.findViewById(R.id.button_quick_call).setOnClickListener(this);
        view.findViewById(R.id.button_quick_email).setOnClickListener(this);
		return view;
	}

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_make_monthly:
                activity.userMessage(R.string.assigned_monthly);
                // do stuff to the ContactStatus
                status.setValue("partner_type", ContactStatus.PARTNER_MONTHLY); 
                status.setValue("status", ContactStatus.STATUS_CURRENT); 
                status.setValue("giving_amount", Integer.parseInt(n.getString("message")));
                status.setValue("gift_frequency", 1); 

                // set up expiry date for manual status
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 3); // expire in 3 months
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String expires_date = dateFormat.format(cal.getTime());
                status.setValue("manual_set_expires", expires_date);

                status.dirtySave();
                GraphCard.clearCache();
                return true;
            case R.id.menu_make_annual:
                activity.userMessage(R.string.assigned_annual);
                // do stuff to the ContactStatus
                status.setValue("partner_type", ContactStatus.PARTNER_ANNUAL); 
                status.setValue("status", ContactStatus.STATUS_CURRENT); 
                status.setValue("giving_amount", Integer.parseInt(n.getString("message")));
                status.setValue("gift_frequency", 12); 

                // set up expiry date for manual status
                cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 14); // expire in 14 months
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                expires_date = dateFormat.format(cal.getTime());
                status.setValue("manual_set_expires", expires_date);

                status.dirtySave();
                GraphCard.clearCache();
                return true;
        }
        return false;
    }
	
    @Override
    public String getNotificationType(){
        return "special_gift";
    }
}
