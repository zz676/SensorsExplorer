package edu.nyu.zhisheng.sensorsexplorer.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

public class NetworkTypePreferences extends MultiSelectListPreference {

	private Context mContext;

	public NetworkTypePreferences(Context context) {
		super(context);
		mContext = context;
		updateNetworks();
	}

	public NetworkTypePreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		updateNetworks();
	}

	/**
	 * Regenerates the list of selectable networks.
	 */
	public void updateNetworks() {
		NetworkInfo[] allnetinfo = ((ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getAllNetworkInfo();
		if (allnetinfo != null) {
			List<CharSequence> entries = new ArrayList<CharSequence>();
			List<CharSequence> values = new ArrayList<CharSequence>();

			for (NetworkInfo ni : allnetinfo) {
				if ((ni.getType() < ConnectivityManager.TYPE_MOBILE_MMS)
						|| (ni.getType() > ConnectivityManager.TYPE_MOBILE_HIPRI)) {
					// filter out specific mobile data connections, we'll catch
					// those with the Mobile setting
					entries.add(ni.getTypeName());
					// entries.add(ni.getTypeName() + " (" +
					// Integer.toString(ni.getType()) + ")");
					values.add(Integer.toString(ni.getType()));
				}
			}

			setEntries(entries.toArray(new CharSequence[] {}));
			setEntryValues(values.toArray(new CharSequence[] {}));
		}
	}

}
