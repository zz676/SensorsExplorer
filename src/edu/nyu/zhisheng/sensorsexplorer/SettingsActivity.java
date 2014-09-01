/*
 * Copyright © 2014 Zhisheng Zhou.
 */
package edu.nyu.zhisheng.sensorsexplorer;

import java.util.HashSet;
import java.util.Set;

import edu.nyu.zhisheng.sensorsexplorer.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends Activity implements
		OnSharedPreferenceChangeListener {

	public static final String KEY_PREF_NOTIFY_FIX = "pref_notify_fix";
	public static final String KEY_PREF_NOTIFY_SEARCH = "pref_notify_search";
	public static final String KEY_PREF_UPDATE_WIFI = "pref_update_wifi";
	public static final String KEY_PREF_UPDATE_NETWORKS = "pref_update_networks";
	public static final String KEY_PREF_UPDATE_NETWORKS_WIFI = Integer
			.toString(ConnectivityManager.TYPE_WIFI);
	public static final String KEY_PREF_UPDATE_NETWORKS_MOBILE = Integer
			.toString(ConnectivityManager.TYPE_MOBILE);
	public static final String KEY_PREF_UPDATE_FREQ = "pref_update_freq";
	public static final String KEY_PREF_UPDATE_LAST = "pref_update_last";

	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		// Show the Up button in the action bar.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		// some logic to use the pre-1.7 setting KEY_PREF_UPDATE_WIFI as a
		// fallback if KEY_PREF_UPDATE_NETWORKS is not set
		if (!mSharedPreferences.contains(KEY_PREF_UPDATE_NETWORKS)) {
			Set<String> fallbackUpdateNetworks = new HashSet<String>();
			if (mSharedPreferences.getBoolean(KEY_PREF_UPDATE_WIFI, false)) {
				fallbackUpdateNetworks.add(KEY_PREF_UPDATE_NETWORKS_WIFI);
			}
			SharedPreferences.Editor spEditor = mSharedPreferences.edit();
			spEditor.putStringSet(KEY_PREF_UPDATE_NETWORKS,
					fallbackUpdateNetworks);
			spEditor.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		SettingsFragment sf = (SettingsFragment) getFragmentManager()
				.findFragmentById(android.R.id.content);
		Preference prefUpdateLast = sf.findPreference(KEY_PREF_UPDATE_LAST);
		final long value = mSharedPreferences.getLong(KEY_PREF_UPDATE_LAST, 0);
		prefUpdateLast.setSummary(String.format(
				getString(R.string.pref_lastupdate_summary), value));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsActivity.KEY_PREF_NOTIFY_FIX)
				|| key.equals(SettingsActivity.KEY_PREF_NOTIFY_SEARCH)) {
			boolean notifyFix = sharedPreferences.getBoolean(
					SettingsActivity.KEY_PREF_NOTIFY_FIX, false);
			boolean notifySearch = sharedPreferences.getBoolean(
					SettingsActivity.KEY_PREF_NOTIFY_SEARCH, false);
			if (!(notifyFix || notifySearch)) {
				Intent stopServiceIntent = new Intent(this,
						PasvLocListenerService.class);
				this.stopService(stopServiceIntent);
			}
		} else if (key.equals(SettingsActivity.KEY_PREF_UPDATE_FREQ)) {
			// this piece of code is necessary because Android has no way
			// of updating the preference summary automatically. I am
			// told the absence of such functionality is a feature...
			SettingsFragment sf = (SettingsFragment) getFragmentManager()
					.findFragmentById(android.R.id.content);
			ListPreference prefUpdateFreq = (ListPreference) sf
					.findPreference(KEY_PREF_UPDATE_FREQ);
			final String value = sharedPreferences.getString(key, key);
			final int index = prefUpdateFreq.findIndexOfValue(value);
			if (index >= 0) {
				final String summary = (String) prefUpdateFreq.getEntries()[index];
				prefUpdateFreq.setSummary(summary);
			}
		}
	}

	@Override
	protected void onStop() {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onStop();
	}

	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
		}

	}
}
