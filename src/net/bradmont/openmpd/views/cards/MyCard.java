package net.bradmont.openmpd.views.cards;

import net.bradmont.openmpd.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class MyCard extends Card {

    protected int title_resource = 0;
    protected int body_resource = 0;

	public MyCard(String title){
		super(title);
	}

	public MyCard(int title_resource, int body_resource){
		super();
        this.title_resource = title_resource;
        this.body_resource = body_resource;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_notification_plain, null);

        if (title_resource != 0){
		    ((TextView) view.findViewById(R.id.title)).setText(title_resource);
        } else {
		    ((TextView) view.findViewById(R.id.title)).setText(title);
        }
        if (body_resource != 0){
            ((TextView) view.findViewById(R.id.description)).setText(body_resource);
        }

		
		return view;
	}

	
	
	
}
