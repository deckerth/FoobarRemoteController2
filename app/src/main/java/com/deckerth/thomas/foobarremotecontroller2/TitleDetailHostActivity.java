package com.deckerth.thomas.foobarremotecontroller2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess;
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess;
import com.deckerth.thomas.foobarremotecontroller2.databinding.ActivityTitleDetailBinding;

public class TitleDetailHostActivity extends AppCompatActivity {

    private
    ActivityTitleDetailBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityTitleDetailBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        Activity activity = this;
        mBinding.settings.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(activity, SettingsActivity.class);
            startActivity(settingsIntent);
        });

        // assigning ID of the toolbar to a variable
        Toolbar toolbar = findViewById(R.id.toolbar);
        // using toolbar as ActionBar
        setSupportActionBar(toolbar);

        PlaylistAccess playlistAccess = PlaylistAccess.getInstance();
        playlistAccess.getCurrentPlaylist(this);

        PlayerAccess playerAccess = PlayerAccess.getInstance(this);
        playerAccess.startPlayerObserver();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = null;
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
        AppBarConfiguration appBarConfiguration = null;
        if (navController != null) {
            appBarConfiguration = new AppBarConfiguration.
                    Builder(navController.getGraph())
                    .build();
        }

        if (navController != null) {
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (prefs.getString("theme", "dark").equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
/*
            try {
                Drawable logo = ContextCompat.getDrawable(getBaseContext(), R.drawable.foobar2000white);
                mBinding.foobarLogo.setImageDrawable(logo);
            } catch (Exception e) {
                e.printStackTrace();
            }
*/
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
/*
            try {
                Drawable logo = ContextCompat.getDrawable(getBaseContext(), R.drawable.foobar2000);
                mBinding.foobarLogo.setImageDrawable(logo);
            } catch (Exception e) {
                e.printStackTrace();
            }
*/
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}