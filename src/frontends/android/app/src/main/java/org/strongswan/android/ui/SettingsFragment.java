/*
 * Copyright (C) 2017 Tobias Brunner
 * HSR Hochschule fuer Technik Rapperswil
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.  See <http://www.fsf.org/copyleft/gpl.txt>.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 */

package org.strongswan.android.ui;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import org.strongswan.android.R;
import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.data.VpnProfileDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.strongswan.android.utils.Constants.PREF_ALWAYS_ON_VPN_PROFILE;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener
{
	private ListPreference mAlwaysOnVPNProfile;

	@Override
	public void onCreatePreferences(Bundle bundle, String s)
	{
		setPreferencesFromResource(R.xml.settings, s);

		mAlwaysOnVPNProfile = (ListPreference)findPreference(PREF_ALWAYS_ON_VPN_PROFILE);
		mAlwaysOnVPNProfile.setOnPreferenceChangeListener(this);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
		{
			mAlwaysOnVPNProfile.setEnabled(false);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		VpnProfileDataSource profiles = new VpnProfileDataSource(getActivity());
		profiles.open();

		List<VpnProfile> all = profiles.getAllVpnProfiles();
		Collections.sort(all, new Comparator<VpnProfile>() {
			@Override
			public int compare(VpnProfile lhs, VpnProfile rhs)
			{
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});

		ArrayList<CharSequence> entries = new ArrayList<>();
		ArrayList<CharSequence> entryvalues = new ArrayList<>();

		for (VpnProfile profile : all)
		{
			entries.add(profile.getName());
			if (profile.getUUID() == null)
			{
				profile.setUUID(UUID.randomUUID());
				profiles.updateVpnProfile(profile);
			}
			entryvalues.add(profile.getUUID().toString());
		}
		profiles.close();

		if (entries.size() == 0)
		{
			mAlwaysOnVPNProfile.setEnabled(false);
		}
		else
		{
			mAlwaysOnVPNProfile.setEnabled(true);
			mAlwaysOnVPNProfile.setEntries(entries.toArray(new CharSequence[0]));
			mAlwaysOnVPNProfile.setEntryValues(entryvalues.toArray(new CharSequence[0]));
		}

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		setCurrentProfileName(pref.getString(PREF_ALWAYS_ON_VPN_PROFILE, null));
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		if (preference == mAlwaysOnVPNProfile)
		{
			setCurrentProfileName((String)newValue);
		}
		return true;
	}

	private void setCurrentProfileName(String uuid)
	{
		VpnProfileDataSource profiles = new VpnProfileDataSource(getActivity());
		profiles.open();

		VpnProfile current = profiles.getVpnProfile(uuid);
		if (current != null)
		{
			mAlwaysOnVPNProfile.setSummary(current.getName());
		}
		else
		{
			mAlwaysOnVPNProfile.setSummary(R.string.no_profile_selected);
		}

		profiles.close();
	}
}
