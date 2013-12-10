package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.fragments.HomeFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.fima.cardsui.objects.Card;

public class OnboardCard extends Card {

    protected int onboardState = 0;

    Button addButton = null;
    Button doneButton = null;
    View.OnClickListener addListener = null;
    View.OnClickListener doneListener = null;

	public OnboardCard(int onboardState){
		super();
        this.onboardState=onboardState;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_onboard, null);

        ((TextView) view.findViewById(R.id.title)).setText(R.string.getting_started);
        ((TextView) view.findViewById(R.id.description)).setText(R.string.getting_started_body);

        addButton = (Button) view.findViewById(R.id.button_add_account);
        addButton.setOnClickListener(addListener);

        doneButton = (Button) view.findViewById(R.id.button_done_adding_accounts);
        doneButton.setOnClickListener(doneListener);

        if (onboardState == HomeFragment.ONBOARD_FIRST_RUN){
            // hide the "Done" button when no accounts have been added yet
            doneButton.setVisibility(View.INVISIBLE);
        } else if (onboardState == HomeFragment.ONBOARD_ACCOUNT_ADDED){
            ((TextView) view.findViewById(R.id.description)).setText(R.string.getting_started_body_2);
            ((TextView) view.findViewById(R.id.title)).setText(R.string.add_another_account);
        }
		
		return view;
	}

    public void setAddClickListener(View.OnClickListener listener){
        if (addButton != null){
            addButton.setOnClickListener(listener);
        }
        addListener = listener;
    }
    public void setDoneClickListener(View.OnClickListener listener){
        if (doneButton != null){
            doneButton.setOnClickListener(listener);
        }
        doneListener = listener;
    }
}
