package net.bradmont.openmpd.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.bradmont.openmpd.R;
import net.bradmont.openmpd.MPDDBHelper;
import net.bradmont.openmpd.models.Notification;
import net.bradmont.openmpd.views.cards.*;
import net.bradmont.supergreen.models.ModelList;
import net.bradmont.holograph.BarGraph;

import com.fima.cardsui.views.CardUI;
import com.fima.cardsui.objects.CardStack;


public class HomeFragment extends Fragment {

    protected CardStack happyStack = new CardStack();
    protected CardStack sadStack = new CardStack();
    protected CardStack specialStack = new CardStack();

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        // cards UI
        CardUI cardsui = (CardUI) getView().findViewById(R.id.cardsui);
        cardsui.setSwipeable(true);


        SummaryCard summaryCard = new SummaryCard();
        summaryCard.setIsSwipeable(false); // uses minor mod to cardsui for dev purposes
                                           // can be savely removed 
        cardsui.addCard(summaryCard);

        GraphCard graphCard = new GraphCard();
        graphCard.setIsSwipeable(false);
        cardsui.addCardToLastStack(graphCard);


        // notifications
        ModelList notifications = MPDDBHelper.filter("notification", "status", Notification.STATUS_NOTIFIED);
        for (int i = 0; i < notifications.size(); i++){
            Notification n = (Notification) notifications.get(i);
            NotificationCard card = NotificationCardFactory.newCard(n);

            if (card instanceof SpecialGiftCard){
                specialStack.add(card);
            } else if (card instanceof LatePartnerCard ||
                    card instanceof LapsedPartnerCard ||
                    card instanceof DroppedPartnerCard){
                sadStack.add(card);
            } else if (card instanceof NewRegularPartnerCard ||
                    card instanceof RestartedPartnerCard ||
                    card instanceof AmountChangeCard){
                happyStack.add(card);
            } else if (card != null) {
                cardsui.addCard(card);
            }
        }
        if (happyStack.getCards().size() > 0){
            cardsui.addStack(happyStack);
        }
        if (sadStack.getCards().size() > 0){
            cardsui.addStack(sadStack);
        }
        if (specialStack.getCards().size() > 0){
            cardsui.addStack(specialStack);
        }

        MyCard card = new MyCard("Getting Started");
        cardsui.addCard(card);
        cardsui.refresh();

	}
}
