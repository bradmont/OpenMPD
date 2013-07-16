package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;

import com.fima.cardsui.objects.Card;

public class RestartedPartnerCard extends NotificationCard {

	public RestartedPartnerCard(Notification n, Contact contact, ContactStatus status){
		super(n, contact, status);
	}

	@Override
	public View buildCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_notification, null);
		TextView title = ((TextView) view.findViewById(R.id.title));
		TextView description = ((TextView) view.findViewById(R.id.description));
        ((ImageView) view.findViewById(R.id.stripe)).setBackgroundResource(R.color.blue);
        ((TextView) view.findViewById(R.id.title)).setTextColor(
            context.getResources().getColor(R.color.blue)
            );

        // set title
        String title_string = String.format(
                context.getResources().getString(R.string.restarted_partner_title),
                contact.getString("fname") + " " + contact.getString("lname"));
        title.setText(title_string);

        int partnership = status.partnership(status.getInt("partner_type"));
        String text = "";
        if (partnership == R.string.monthly ){
            text = String.format("%s %s has resumed giving! ($%.2f/mo)",
                    contact.getString("fname"),
                    contact.getString("lname"),
                    status.getFloat("giving_amount")/100f);
        } else if (partnership == R.string.regular){
            text = String.format("%s %s has resumed giving! ($%.2f/%dmo)",
                    contact.getString("fname"),
                    contact.getString("lname"),
                    status.getFloat("giving_amount")/100f,
                    status.getInt("gift_frequency"));
        }
        description.setText(text);

		return view;
	}

	
	
	
}
