package com.deckerth.thomas.foobarremotecontroller2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess;
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess;
import com.deckerth.thomas.foobarremotecontroller2.databinding.ActivityTitleDetailBinding;

public class TitleDetailHostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        ActivityTitleDetailBinding binding = ActivityTitleDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // assigning ID of the toolbar to a variable
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // using toolbar as ActionBar
        setSupportActionBar(toolbar);

        ImageButton settings = findViewById(R.id.settings);
        Activity activity = this;
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(activity, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        PlaylistAccess playlistAccess = PlaylistAccess.getInstance();
        playlistAccess.getCurrentPlaylist(this);

        PlayerAccess playerAccess = PlayerAccess.getInstance(this);
        playerAccess.startPlayerObserver();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_title_detail);
        NavController navController = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.
                Builder(navController.getGraph())
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_title_detail);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}