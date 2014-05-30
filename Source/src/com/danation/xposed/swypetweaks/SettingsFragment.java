package com.danation.xposed.swypetweaks;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

public class SettingsFragment extends PreferenceFragment
{
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.preferences);
        
        // Create the new ListPref
        ListPreference imeListPreference = new ListPreference(getActivity());

        // Get the Preference Category which we want to add the ListPreference to
        PreferenceCategory targetCategory = (PreferenceCategory) findPreference("otherCategory");

        //Get list of all input methods
        InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService("input_method");
        List<InputMethodInfo> inputMethodInfo = inputMethodManager.getInputMethodList();

        ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
        entries.add("None");
        ArrayList<CharSequence> entryValues = new ArrayList<CharSequence>();
        entryValues.add("None");
        
        //Search list for the voice keyboard
        for (InputMethodInfo info : inputMethodInfo)
        {
        	entries.add(info.loadLabel(getActivity().getPackageManager()));
        	entryValues.add(info.getId());
        }
        
         // IMPORTANT - This is where set entries...looks OK to me
         imeListPreference.setEntries(entries.toArray(new String[0]));
         imeListPreference.setEntryValues(entryValues.toArray(new String[0]));

         imeListPreference.setTitle("Long Press Enter: Change IME");
         imeListPreference.setKey("longPressAction");
         imeListPreference.setSummary("None");
         
         // Get current value
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
         String currentValue = sharedPreferences.getString("longPressAction", "None");
         int currentIndex = imeListPreference.findIndexOfValue(currentValue);
         if (currentIndex != -1)
         {
        	 imeListPreference.setSummary(imeListPreference.getEntries()[currentIndex]);
         }
         

         imeListPreference.setPersistent(true);
         imeListPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				int selectedIndex = ((ListPreference)preference).findIndexOfValue((String)newValue);
				preference.setSummary(((ListPreference)preference).getEntries()[selectedIndex]);
				return true;
			}
         });

         // Add the ListPref to the Pref category
         targetCategory.addPreference(imeListPreference);
    }
}
