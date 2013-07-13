package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class SpecialGiftCard extends NotificationCard {

	public SpecialGiftCard(Notification n, Contact contact, ContactStatus status){
		super(n, contact, status);
	}

	@Override
	public View buildCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_notification, null);
		TextView title = ((TextView) view.findViewById(R.id.title));
		TextView description = ((TextView) view.findViewById(R.id.description));

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

		return view;
	}

	
	
	
}
