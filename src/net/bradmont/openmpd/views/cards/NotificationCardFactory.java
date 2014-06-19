package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class NotificationCardFactory {

	public static NotificationCard newCard(BaseActivity activity, Notification n){
        Contact contact = (Contact) n.getRelated("contact");
        ContactStatus status = (ContactStatus)MPDDBHelper
            .getReferenceModel("contact_status")
            .getByField("contact_id", contact.getInt("id"));

        switch (n.getInt("type")){
            case Notification.CHANGE_PARTNER_TYPE:
                int partnership = status.partnership(status.getInt("partner_type"));
                if (partnership == R.string.monthly || partnership == R.string.regular){
                    if (status.getInt("status") == ContactStatus.STATUS_NEW){
                        return new NewRegularPartnerCard(activity, n, contact, status);
                    } else if (status.getInt("status") == ContactStatus.STATUS_CURRENT){
                        // not sure why these are getting marked as new partners
                        return new RestartedPartnerCard(activity, n, contact, status);
                    }
                }
                break;

            case Notification.CHANGE_STATUS:

                switch (status.getInt("status")){
                    case ContactStatus.STATUS_LATE:
                        return new LatePartnerCard(activity, n, contact, status);

                    case ContactStatus.STATUS_LAPSED:
                        return new LapsedPartnerCard(activity, n, contact, status);

                    case ContactStatus.STATUS_DROPPED:
                        return new DroppedPartnerCard(activity, n, contact, status);

                    case ContactStatus.STATUS_CURRENT:
                        try {
                            int oldstatus = Integer.parseInt(status.getString("message"));
                            if (oldstatus == ContactStatus.STATUS_LATE || 
                                oldstatus == ContactStatus.STATUS_LAPSED || 
                                oldstatus == ContactStatus.STATUS_DROPPED){
                                return new RestartedPartnerCard(activity, n, contact, status);
                            } else {
                                return null;
                            }
                        } catch (Exception e){
                                return null;
                        }
                }
                break;
            case Notification.CHANGE_AMOUNT:
                return new AmountChangeCard(activity, n, contact, status);
            case Notification.SPECIAL_GIFT:
                return new SpecialGiftCard(activity, n, contact, status);
            
        }
        return null;
    }
	
}
