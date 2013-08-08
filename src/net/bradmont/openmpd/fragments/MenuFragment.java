package net.bradmont.openmpd.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;

import net.bradmont.openmpd.R;
import net.bradmont.openmpd.*;
import net.bradmont.openmpd.views.*;

public class MenuFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.menu_background, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] menu_items = getResources().getStringArray(R.array.menu_items);
		ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(getActivity(), 
				R.layout.simple_list_item_1, android.R.id.text1, menu_items);
		setListAdapter(menuAdapter);
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = null;
        TextView tv = (TextView) v.findViewById(android.R.id.text1);
		switch (position) {
		case 0:
            if (OpenMPD.homeFragment == null){
                OpenMPD.homeFragment = new HomeFragment();
            }
			newContent = OpenMPD.homeFragment;
			break;
		case 1:
            if (OpenMPD.contactList == null){
                OpenMPD.contactList = new ContactList();
            }
			newContent = OpenMPD.contactList;
			break;
		/*case 2:
            if (OpenMPD.giftList == null){
                OpenMPD.giftList = new GiftList();
            }
			newContent = OpenMPD.giftList;
			break;*/
		case 2:
            if (OpenMPD.serviceAccountList == null){
                OpenMPD.serviceAccountList = new ServiceAccountList();
            }
			newContent = OpenMPD.serviceAccountList;
			break;
		case 3:
            if (OpenMPD.debugFragment == null){
                OpenMPD.debugFragment = new DebugFragment();
            }
			newContent = OpenMPD.debugFragment;
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
