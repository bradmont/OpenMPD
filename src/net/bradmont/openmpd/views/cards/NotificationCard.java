package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.views.ContactDetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public abstract class NotificationCard extends Card {

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

    public abstract View buildCardContent(Context context);
	
}
