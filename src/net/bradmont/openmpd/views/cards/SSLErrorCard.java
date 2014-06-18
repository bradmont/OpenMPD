package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;
import net.bradmont.openmpd.fragments.HomeFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.fima.cardsui.objects.Card;

public class SSLErrorCard extends Card {


    Button addButton = null;
    Button doneButton = null;
    View.OnClickListener addListener = null;
    View.OnClickListener doneListener = null;
    private String host = null;

	public SSLErrorCard(String host){
		super();
        this.host=host;
	}

	@Override
	public View getCardContent(final Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_sslerror, null);

        ((TextView) view.findViewById(R.id.title)).setText(host);
        ((TextView) view.findViewById(R.id.description)).setText(R.string.ask_add_ssl_exception);

        addButton = (Button) view.findViewById(R.id.button_ignore_certificate);
        addButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                SharedPreferences.Editor prefs = context.getSharedPreferences("openmpd", Context.MODE_PRIVATE).edit();
                prefs.putBoolean("ignore_ssl_" + host, true);
                prefs.commit();
                ((BaseActivity) context).userMessage(R.string.ignoring_ssl);
            }
        });

        return view;
	}

    public void setAddClickListener(View.OnClickListener listener){
        if (addButton != null){
            addButton.setOnClickListener(listener);
        }
        addListener = listener;
    }
}
