package com.talksy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.talksy.R;
import com.talksy.activities.ProfileActivity;
import com.talksy.activities.SettingsActivity;

public class SettingsFragment extends Fragment {

	private CardView cardProfile, cardAppSettings;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_settings, container, false);

		cardProfile = root.findViewById(R.id.cardProfile);
		cardAppSettings = root.findViewById(R.id.cardSettings);

		cardProfile.setOnClickListener(v -> {
			Intent intent = new Intent(getContext(), ProfileActivity.class);
			startActivity(intent);
		});

		cardAppSettings.setOnClickListener(v -> {
			Intent intent = new Intent(getContext(), SettingsActivity.class);
			startActivity(intent);
		});

		return root;
	}
}
