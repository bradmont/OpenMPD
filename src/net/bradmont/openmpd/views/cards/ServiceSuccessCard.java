package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.models.*;
import net.bradmont.openmpd.fragments.HomeFragment;
import net.bradmont.supergreen.models.*;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.fima.cardsui.objects.Card;

public class ServiceSuccessCard extends Card {


    Button sendButton = null;
    View.OnClickListener sendListener = null;
    TntService service = null;

	public ServiceSuccessCard(DBModel service){
		super();
        this.service = (TntService) service;
	}

	@Override
	public View getCardContent(final Context context) {
		final View view = LayoutInflater.from(context).inflate(R.layout.card_service_success, null);

        ((TextView) view.findViewById(R.id.title)).setText(R.string.report_success);
        ((TextView) view.findViewById(R.id.description)).setText(R.string.report_success_body);

        sendButton = (Button) view.findViewById(R.id.button_ok);
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String [] {"brad.stewart@p2c.com"});

                intent.putExtra(Intent.EXTRA_SUBJECT, "OpenMPD Login Report");

                String body = "This is to report that I successfully imported donor data from the organisation " + service.getString("name") + " via " + service.getString("query_ini_url");
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(
                        Intent.createChooser(intent, "Send Email"));
                service.setValue("untested_service", false);
                service.dirtySave();
                sendButton.setVisibility(View.INVISIBLE);
                ((TextView) view.findViewById(R.id.description)).setText(R.string.thank_you_swipe_away);
            }
        });

		return view;
	}

}
