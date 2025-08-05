package com.talksy.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.talksy.R;
import com.talksy.fragments.GroupsFragment;
import com.talksy.fragments.HomeFragment;
import com.talksy.fragments.NotesFragment;
import com.talksy.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

	private BottomNavigationView bottomNav;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bottomNav = findViewById(R.id.bottomNavigation);

		// Load default
		loadFragment(new HomeFragment());

		bottomNav.setOnItemSelectedListener(item -> {
			Fragment selectedFragment = getFragmentForItemId(item.getItemId());
			return loadFragment(selectedFragment);
		});
	}

	/**
	 * Returns corresponding fragment for bottom nav item.
	 */
	private Fragment getFragmentForItemId(int itemId) {
		if (itemId == R.id.nav_groups) return new GroupsFragment();
		if (itemId == R.id.nav_notes) return new NotesFragment();
		if (itemId == R.id.nav_settings) return new SettingsFragment();
		return new HomeFragment(); // Default
	}

	/**
	 * Loads the given fragment into the container.
	 */
	private boolean loadFragment(@NonNull Fragment fragment) {
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.mainContainer, fragment)
				.commit();
		return true;
	}
}
