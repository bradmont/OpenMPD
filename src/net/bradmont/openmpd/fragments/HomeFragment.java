package net.bradmont.openmpd.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.bradmont.openmpd.controllers.TntImportService;
import net.bradmont.openmpd.R;
import net.bradmont.openmpd.MPDDBHelper;
import net.bradmont.openmpd.models.Notification;
import net.bradmont.openmpd.views.cards.*;
import net.bradmont.openmpd.views.EditServiceAccountDialog;
import net.bradmont.supergreen.models.ModelList;
import net.bradmont.holograph.BarGraph;

import com.fima.cardsui.views.CardUI;
import com.fima.cardsui.objects.CardStack;


public class HomeFragment extends Fragment {

    public static final int ONBOARD_FIRST_RUN=0;
    public static final int ONBOARD_ACCOUNT_ADDED=1;
    public static final int ONBOARD_IMPORTING=2;
    public static final int ONBOARD_FINISHED=3;


	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        addCards();
    }
    private void addCards() {
        // cards UI
        CardUI cardsui = (CardUI) getView().findViewById(R.id.cardsui);
        cardsui.setSwipeable(true);
        cardsui.clearCards();

        // Are we in the onboarding process?
        SharedPreferences prefs = getActivity().getSharedPreferences("openmpd", Context.MODE_PRIVATE);
        int onboardState = prefs.getInt("onboardState", ONBOARD_FIRST_RUN);


        switch (onboardState){
            case ONBOARD_FINISHED:
                // ### if initial import is finished
                SummaryCard summaryCard = new SummaryCard();
                summaryCard.setIsSwipeable(false); // uses minor mod to cardsui for dev purposes
                                                   // can be savely removed 
                cardsui.addCard(summaryCard);

                GraphCard graphCard = new GraphCard();
                graphCard.setIsSwipeable(false);
                cardsui.addCardToLastStack(graphCard);


                CardStack happyStack = new CardStack();
                CardStack sadStack = new CardStack();
                CardStack specialStack = new CardStack();
                // notifications
                ModelList notifications = MPDDBHelper
                        .filter("notification", "status", Notification.STATUS_NOTIFIED)
                        .orderBy("message");
                for (int i = 0; i < notifications.size(); i++){
                    Notification n = (Notification) notifications.get(i);
                    NotificationCard card = NotificationCardFactory.newCard(n);

                    if (card instanceof SpecialGiftCard ||
                        card instanceof AmountChangeCard){
                        specialStack.add(card);
                    } else if (card instanceof LatePartnerCard ||
                            card instanceof LapsedPartnerCard ||
                            card instanceof DroppedPartnerCard){
                        sadStack.add(card);
                    } else if (card instanceof NewRegularPartnerCard ||
                            card instanceof RestartedPartnerCard ){
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
                break;
            case ONBOARD_FIRST_RUN:
                // if this is first run:
                MyCard card = new MyCard(R.string.getting_started, R.string.getting_started_body);
                card.setOnClickListener( new addAccountClickListener());
                cardsui.addCard(card);
                cardsui.refresh();
                break;
            case ONBOARD_ACCOUNT_ADDED:
                MyCard another = new MyCard(R.string.add_another_account, R.string.add_another_account_body);
                cardsui.addCard(another);
                another.setOnClickListener( new addAccountClickListener());

                MyCard done = new MyCard(R.string.done_adding_accounts, R.string.done_adding_accounts_body);
                done.setOnClickListener( new updateClickListener());
                cardsui.addCard(done);
                cardsui.refresh();
                break;
            case ONBOARD_IMPORTING:
                // if we're in the middle of the initial import
                MyCard importing = new MyCard(R.string.importing_data, R.string.importing_data_body);
                cardsui.addCard(importing);
                cardsui.refresh();
                break;

        }
	}
    private class addAccountClickListener implements OnClickListener{
        @Override
        public void onClick(View v){
            EditServiceAccountDialog dialog = new EditServiceAccountDialog();
            dialog.show(getFragmentManager(), "edit_account_dialog");
            SharedPreferences prefs = getActivity().getSharedPreferences("openmpd", Context.MODE_PRIVATE);
            prefs.edit()
                .putInt("onboardState", ONBOARD_ACCOUNT_ADDED)
                  .apply();
            addCards();
        }
    }
    private class updateClickListener implements OnClickListener{
        @Override
        public void onClick(View v){
            // launch our intent to trigger the TntImportService
            ModelList accounts = MPDDBHelper.getReferenceModel("service_account").getAll();
            int [] account_ids = new int [accounts.size()];
            for (int i = 0; i < accounts.size(); i++){
                account_ids[i] = accounts.get(i).getID();
            }

            getActivity().startService(
                new Intent(getActivity(), TntImportService.class).putExtra("net.bradmont.openmpd.account_ids", account_ids));

            SharedPreferences prefs = getActivity().getSharedPreferences("openmpd", Context.MODE_PRIVATE);
            prefs.edit()
                .putInt("onboardState", ONBOARD_IMPORTING)
                 .apply();
            addCards();
            prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
                    if (key.equals("onboardState")){
                        addCards();
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
                    }
                }
            });
        }
    }
}
