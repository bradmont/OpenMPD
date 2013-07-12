package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class NotificationCard extends Card {

    protected Notification n = null;
    protected Contact contact;
    protected ContactStatus status;
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
	}

    public void dismissNotification(){
        n.setValue("status", Notification.STATUS_ACKNOWLEDGED);
        n.dirtySave();
    }
	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_notification, null);

		return view;
	}

	
	
	
}
