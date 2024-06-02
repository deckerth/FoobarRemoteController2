package com.deckerth.thomas.foobarremotecontroller2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

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
import com.deckerth.thomas.foobarremotecontroller2.model.VolumeControl;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;

import java.util.Objects;

public class TitleDetailHostActivity extends AppCompatActivity {

    private ActivityTitleDetailBinding mBinding;
    private TitleDetailHostActivity mActivity;

    public static Bitmap FOOBAR_BITMAP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

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

        PlayerViewModel playerViewModel = PlayerViewModel.getInstance();
        playerViewModel.getVolumeControlViewModel().getVolume().observe(this, Objects.requireNonNull(mBinding.volumeControl)::setVolumePercent);

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

    private int startedWaits = 0;

    void changeVolumePercent(int delta) {
        VolumeControl control = PlayerViewModel.getInstance().getVolumeControlViewModel().getVolumeControl();
        int newValue = control.getModifiedValuePercent(delta);
        if (newValue != control.getCurrentValuePercent())
            PlayerAccess.getInstance().setVolume(control.getDecibelValue(newValue));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (prefs.getBoolean("foobar_volume_control", true)) {
            switch (keyCode){
                case KeyEvent.KEYCODE_VOLUME_UP:
                    mBinding.volumeControl.setVisibility(View.VISIBLE);
                    changeVolumePercent(10);
                    startedWaits++;
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                        mActivity.runOnUiThread(() -> {
                            startedWaits--;
                            if (startedWaits == 0)
                                mBinding.volumeControl.setVisibility(View.INVISIBLE);
                        });
                    }).start();

                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    mBinding.volumeControl.setVisibility(View.VISIBLE);
                    changeVolumePercent(-10);
                    startedWaits++;
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                        mActivity.runOnUiThread(() -> {
                            startedWaits--;
                            if (startedWaits == 0)
                                mBinding.volumeControl.setVisibility(View.INVISIBLE);
                        });
                    }).start();
                    return true;
                default:
                    return super.onKeyDown(keyCode,event);
            }

        } else
            return false;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            // Create a single-color bitmap (1x1 pixel) if intrinsic dimensions are invalid
        } else {
            bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (prefs.getString("theme", "dark").equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Drawable drawable =  getDrawable(R.drawable.foobar2000white);
            FOOBAR_BITMAP = drawableToBitmap(drawable);

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
            Drawable drawable =  getDrawable(R.drawable.foobar2000);
            FOOBAR_BITMAP = drawableToBitmap(drawable);

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