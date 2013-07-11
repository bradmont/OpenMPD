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
import net.bradmont.openmpd.views.cards.*;
import net.bradmont.holograph.BarGraph;

import com.fima.cardsui.views.CardUI;


public class HomeFragment extends Fragment {


	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        // cards UI
        CardUI cardsui = (CardUI) getView().findViewById(R.id.cardsui);
        cardsui.setSwipeable(true);

        cardsui.addCard(new GraphCard());

        MyCard card = new MyCard("Test card");
        cardsui.addCard(card);
        cardsui.refresh();

	}
}
