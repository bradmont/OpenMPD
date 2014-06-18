package net.bradmont.openmpd.fragments;

import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
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
		String[] menu_items = null;

        SharedPreferences prefs = getActivity().getSharedPreferences("openmpd", Context.MODE_PRIVATE);


        if (prefs.getBoolean("debugEnabled", false) == true){
            menu_items = getResources().getStringArray(R.array.menu_items_debug);
        } else {
            menu_items = getResources().getStringArray(R.array.menu_items);
        }
		ArrayAdapter<String> menuAdapter = menuAdapter = new ArrayAdapter<String>(getActivity(), 
                    R.layout.simple_list_item_1, android.R.id.text1, menu_items);

		setListAdapter(menuAdapter);
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = null;
        TextView tv = (TextView) v.findViewById(android.R.id.text1);
		switch (position) {
		case 0:
            if (((HomeActivity) getActivity()).homeFragment == null){
                ((HomeActivity) getActivity()).homeFragment = new HomeFragment();
            }
			newContent = ((HomeActivity) getActivity()).homeFragment;
			break;
		case 1:
            if (((HomeActivity) getActivity()).contactList == null){
                ((HomeActivity) getActivity()).contactList = new ContactList();
            }
			newContent = ((HomeActivity) getActivity()).contactList;
			break;
		/*case 2:
            if (OpenMPD.giftList == null){
                OpenMPD.giftList = new GiftList();
            }
			newContent = OpenMPD.giftList;
			break;*/
		case 2:
            if (((HomeActivity) getActivity()).serviceAccountList == null){
                ((HomeActivity) getActivity()).serviceAccountList = new ServiceAccountList();
            }
			newContent = ((HomeActivity) getActivity()).serviceAccountList;
			break;
		case 3:
            if (((HomeActivity) getActivity()).debugFragment == null){
                ((HomeActivity) getActivity()).debugFragment = new DebugFragment();
            }
			newContent = ((HomeActivity) getActivity()).debugFragment;
			break;
		}
		if (newContent != null)
			switchFragment(newContent);
	}

	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;
		
		if (getActivity() instanceof BaseActivity) {
			BaseActivity ba = (BaseActivity) getActivity();
			ba.switchContent(fragment);
		}
	}


}
