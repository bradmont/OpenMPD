package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class NotificationCardFactory {

	public static NotificationCard newCard(Notification n){
        Contact contact = (Contact) n.getRelated("contact");
        ContactStatus status = (ContactStatus)MPDDBHelper
            .getReferenceModel("contact_status")
            .getByField("contact_id", contact.getInt("id"));

        switch (n.getInt("type")){
            case Notification.CHANGE_PARTNER_TYPE:
                int partnership = status.partnership(status.getInt("partner_type"));
                if (partnership == R.string.monthly || partnership == R.string.regular){
                    return new NewRegularPartnerCard(n, contact, status);
                }

            case Notification.CHANGE_STATUS:

                switch (status.getInt("status")){
                    case ContactStatus.STATUS_LATE:
                        return new LatePartnerCard(n, contact, status);

                    case ContactStatus.STATUS_LAPSED:
                        return new LapsedPartnerCard(n, contact, status);

                    case ContactStatus.STATUS_DROPPED:
                        return new DroppedPartnerCard(n, contact, status);

                    case ContactStatus.STATUS_CURRENT:
                        int oldstatus = Integer.parseInt(status.getString("message"));
                        if (oldstatus == ContactStatus.STATUS_LATE || 
                            oldstatus == ContactStatus.STATUS_LAPSED || 
                            oldstatus == ContactStatus.STATUS_DROPPED){
                            return new RestartedPartnerCard(n, contact, status);
                        } else {
                            return null;
                        }
                    }
            case Notification.CHANGE_AMOUNT:
                return new AmountChangeCard(n, contact, status);
            case Notification.SPECIAL_GIFT:
                return new SpecialGiftCard(n, contact, status);
            
        }
        return null;
    }
	
}
