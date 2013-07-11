package net.bradmont.openmpd.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.bradmont.openmpd.R;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.views.*;

public class MenuFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] menu_items = getResources().getStringArray(R.array.menu_items);
		ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_1, android.R.id.text1, menu_items);
		setListAdapter(colorAdapter);
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = null;
		switch (position) {
		case 0:
			newContent = new HomeFragment();
			break;
		case 1:
			newContent = new ContactList();
			break;
		case 2:
			newContent = new GiftList();
			break;
		case 3:
			newContent = new ColorFragment(android.R.color.white);
			break;
		case 8:
			newContent = new ServiceAccountList();
			break;
		}
		if (newContent != null)
			switchFragment(newContent);
	}

	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;
		
		if (getActivity() instanceof OpenMPD) {
			OpenMPD om = (OpenMPD) getActivity();
			om.switchContent(fragment);
		}
	}


}
