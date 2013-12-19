package net.bradmont.openmpd.fragments;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
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

import net.bradmont.openmpd.OpenMPD;
import net.bradmont.openmpd.controllers.TntImportService;
import net.bradmont.openmpd.R;
import net.bradmont.openmpd.MPDDBHelper;
import net.bradmont.openmpd.models.Notification;
import net.bradmont.openmpd.views.HelpDialog;
import net.bradmont.openmpd.views.cards.*;
import net.bradmont.openmpd.views.EditServiceAccountDialog;
import net.bradmont.supergreen.models.ModelList;
import net.bradmont.holograph.BarGraph;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.fima.cardsui.views.CardUI;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.objects.Card;


public class HomeFragment extends SherlockFragment {

    public static final int ONBOARD_FIRST_RUN=0;
    public static final int ONBOARD_ACCOUNT_ADDED=1;
    public static final int ONBOARD_IMPORTING=2;
    public static final int ONBOARD_FINISHED=3;


    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
        final SharedPreferences prefs = getActivity().getSharedPreferences("openmpd", Context.MODE_PRIVATE);
        int onboardState = prefs.getInt("onboardState", ONBOARD_FIRST_RUN);
        int tutorialSwipe = prefs.getInt("tutorialSwipe", 0);
        int tutorialClickTitle = prefs.getInt("tutorialClickTitle", 0);
        int updateNews = prefs.getInt("updateNews", 0);


        switch (onboardState){
            case ONBOARD_FINISHED:
                // ### if initial import is finished
                SummaryCard summaryCard = new SummaryCard();
                summaryCard.setIsSwipeable(false); // uses minor mod to cardsui for dev purposes
                                                   // can be savely removed 
                // click title tutorial card
                if (tutorialClickTitle == 0){
                    Card tutClickCard = new MyCard(R.string.click_me, R.string.click_tutorial);
                    tutClickCard.setOnCardSwipedListener(new Card.OnCardSwiped(){
                        public void onCardSwiped(Card card, View layout){
                            prefs.edit()
                                .putInt("tutorialClickTitle", 1)
                                  .apply();
                        }
                    });
                    cardsui.addCard(tutClickCard);
                    cardsui.addCardToLastStack(summaryCard);
                }
                else {
                    cardsui.addCard(summaryCard);
                }

                GraphCard graphCard = new GraphCard();
                graphCard.setIsSwipeable(false);
                cardsui.addCardToLastStack(graphCard);


                CardStack happyStack = new CardStack();
                CardStack sadStack = new CardStack();
                CardStack specialStack = new CardStack();


                // notifications
                ModelList notifications = MPDDBHelper
                        .filter("notification", "status", Notification.STATUS_NOTIFIED)
                        .orderBy("date");
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
                // swipe tutorial card
                if (tutorialSwipe == 0){
                    Card tutSwipeCard = new MyCard(R.string.swipe_me, R.string.swipe_tutorial);
                    tutSwipeCard.setOnCardSwipedListener(new Card.OnCardSwiped(){
                        public void onCardSwiped(Card card, View layout){
                            prefs.edit()
                                .putInt("tutorialSwipe", 1)
                                  .apply();
                        }
                    });
                    happyStack.add(tutSwipeCard);
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

                // update news
                if (updateNews < OpenMPD.getVersion()){
                    showUpdateNews( OpenMPD.getVersion());
                }
                break;
            case ONBOARD_FIRST_RUN:
            case ONBOARD_ACCOUNT_ADDED:
                // if we're still adding accounts
                OnboardCard card = new OnboardCard(onboardState);
                card.setAddClickListener( new addAccountClickListener(this));
                card.setDoneClickListener( new updateClickListener());
                card.setIsSwipeable(false); 
                cardsui.addCard(card);
                cardsui.refresh();
                break;
            case ONBOARD_IMPORTING:
                // if we're currently doing the initial import
                MyCard importing = new MyCard(R.string.importing_data, R.string.importing_data_body);
                importing.setIsSwipeable(false); 
                cardsui.addCard(importing);
                cardsui.refresh();
                break;

        }
	}
    @Override
    public void onResume(){
        super.onResume();
        // set title
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar()
            .setTitle(R.string.app_name);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar()
            .setSubtitle(null);
    }
    @Override
    public boolean onOptionsItemSelected (com.actionbarsherlock.view.MenuItem item){
        switch (item.getItemId() ){
            case R.id.menu_help:
                HelpDialog.showHelp(getActivity(), R.string.help_main_title, R.string.help_main);
            return true;
        }
        return false;
    }

    public void showUpdateNews(final int version){
        final SharedPreferences prefs = getActivity().getSharedPreferences("openmpd", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(OpenMPD.getInstance());
        builder.setMessage(R.string.update_news)
            .setTitle(R.string.whats_new);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                prefs.edit()
                    .putInt("updateNews", version)
                      .apply();
            }
        });
        builder.show();

    }

    private class addAccountClickListener implements OnClickListener{
        HomeFragment fragment = null;
        public addAccountClickListener(HomeFragment fragment){
            this.fragment = fragment;
        }
        @Override
        public void onClick(View v){
            EditServiceAccountDialog dialog = new EditServiceAccountDialog();
            final Context context = getActivity();
            dialog.setSaveCallback(new Runnable(){
                @Override
                public void run(){
                    SharedPreferences prefs = context.getSharedPreferences("openmpd", Context.MODE_PRIVATE);
                    prefs.edit()
                        .putInt("onboardState", ONBOARD_ACCOUNT_ADDED)
                          .apply();
                    fragment.addCards();
                }
            });
            dialog.show(getFragmentManager(), "edit_account_dialog");
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

            // release write lock for background thread
            OpenMPD.getInstance().closeDB();

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
