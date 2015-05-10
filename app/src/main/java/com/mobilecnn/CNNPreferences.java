package com.mobilecnn;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import java.util.List;

/**
 * Created by daniel on 5/9/15.
 */
public class CNNPreferences extends PreferenceActivity
{
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return CNNPreferencesFragment.class.getName().equals(fragmentName);
    }



    public static class CNNPreferencesFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fragment_preference);

            CheckBoxPreference useLocalCNN = (CheckBoxPreference)findPreference("useLocalCNN");
            useLocalCNN.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MainActivity.useLocal = ((CheckBoxPreference) preference).isChecked();
                    return true;
                }
            });
        }
    }
}
