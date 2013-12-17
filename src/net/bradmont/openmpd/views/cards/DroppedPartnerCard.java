package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;

import com.fima.cardsui.objects.Card;

public class DroppedPartnerCard extends NotificationCard {

	public DroppedPartnerCard(Notification n, Contact contact, ContactStatus status){
		super(n, contact, status);
	}

	@Override
	public View buildCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_notification, null);
		TextView title = ((TextView) view.findViewById(R.id.title));
		TextView description = ((TextView) view.findViewById(R.id.description));
        ((ImageView) view.findViewById(R.id.stripe)).setBackgroundResource(R.color.gray);
        ((TextView) view.findViewById(R.id.title)).setTextColor(
            context.getResources().getColor(R.color.gray)
            );
		TextView date = ((TextView) view.findViewById(R.id.date));

        // set date
        date.setText(n.getString("date"));

        // set title
        String title_string = String.format(
                context.getResources().getString(R.string.dropped_partner_title),
                contact.getString("fname") + " " + contact.getString("lname"));
        title.setText(title_string);

        int partnership = status.partnership(status.getInt("partner_type"));
        String text = "";
        if (partnership == R.string.monthly ){
            text = String.format("%s %s has dropped off ($%.2f/mo, more than 12 months late)",
                    contact.getString("fname"),
                    contact.getString("lname"),
                    status.getFloat("giving_amount")/100f);
        } else if (partnership == R.string.regular){
            text = String.format("%s %s has dropped off ($%.2f/%dmo, more than 12 months late)",
                    contact.getString("fname"),
                    contact.getString("lname"),
                    status.getFloat("giving_amount")/100f,
                    status.getInt("gift_frequency"));
        }
        description.setText(text);

        // add click handlers to buttons
        view.findViewById(R.id.button_quick_call).setOnClickListener(this);
        view.findViewById(R.id.button_quick_email).setOnClickListener(this);

		return view;
	}
	
    @Override
    public String getQuickMessageFilter(){
        return "notification_type = 'dropped'";
    }
	
}
