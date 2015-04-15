package net.bradmont.openmpd.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import net.bradmont.openmpd.R;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
