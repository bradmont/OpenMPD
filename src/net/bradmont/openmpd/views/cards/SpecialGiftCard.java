package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.views.ContactDetail;

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

import com.fima.cardsui.objects.Card;

public class SpecialGiftCard extends NotificationCard implements PopupMenu.OnMenuItemClickListener {

	public SpecialGiftCard(Notification n, final Contact contact, ContactStatus status){
		super(n, contact, status);


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
		return view;
	}

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_make_monthly:
                OpenMPD.getInstance().userMessage("clicked make monthly");
                return true;
        }
        return false;
    }

	
	
	
}
