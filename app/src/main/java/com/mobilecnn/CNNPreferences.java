package com.mobilecnn;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import java.util.List;

/**
 * Created by daniel on 5/9/15.
 */
public class CNNPreferences extends PreferenceActivity
{
    public static CheckBoxPreference useLocalCNN;

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

            useLocalCNN = (CheckBoxPreference)findPreference("useLocalCNN");
        }
    }
}
