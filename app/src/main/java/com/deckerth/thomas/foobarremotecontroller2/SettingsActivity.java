package com.deckerth.thomas.foobarremotecontroller2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.deckerth.thomas.foobarremotecontroller2.databinding.SettingsActivityBinding;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistViewModel;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static Activity mActivity;
    private SettingsActivityBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mBinding = SettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // assigning ID of the toolbar to a variable
        Toolbar toolbar = findViewById(R.id.toolbar);
        // using toolbar as ActionBar
        setSupportActionBar(toolbar);

        displayCopyright();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_frame, new SettingsFragment())
                    .commit();
        }
    }

    private void displayCopyright() {
        String icons8 = getResources().getString(R.string.icons8); // "Icons by <a href=\"https://icons8.com/\">Icons8"
        String htmlString =
                "<html>\n" +
                "<head>\n" +
                "\t<title></title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<p style=\"text-align: center;\">&copy; 2024 Thomas Decker</p>\n" +
                "\n" +
                "<p style=\"text-align: center;\">" + icons8 + "</a></p>\n" +
                "\n" +
                "<p style=\"text-align: center;\">&nbsp;</p>\n" +
                "</body>\n" +
                "</html>\n";
        Spanned spannedText = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT);
        mBinding.copyright.setText(spannedText, TextView.BufferType.SPANNABLE);
        mBinding.copyright.setOnClickListener(view -> {
            String urlToOpen = "https://icons8.com/";
            // Create an Intent with ACTION_VIEW and the URL
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (prefs.getString("theme", "dark").equals("dark")) {
            try {
                Drawable logo = ContextCompat.getDrawable(getBaseContext(), R.drawable.foobar2000white);
                mBinding.foobarLogo.setImageDrawable(logo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Drawable logo = ContextCompat.getDrawable(getBaseContext(), R.drawable.foobar2000);
                mBinding.foobarLogo.setImageDrawable(logo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference ipAddressPreference = findPreference("ip_address");
            Objects.requireNonNull(ipAddressPreference).setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                String ipAddress = preference.getText();
                if (ipAddress != null) {
                    return "http://" + ipAddress;
                } else {
                    return requireContext().getString(R.string.value_not_set);
                }
            });

            ipAddressPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue != null && newValue != "")
                    Objects.requireNonNull(PlaylistViewModel.getInstance()).refreshPlaylist(false, mActivity);
                return true;
            });

            ListPreference titleFormat = findPreference("title_layout");
            Objects.requireNonNull(titleFormat).setSummaryProvider((Preference.SummaryProvider<ListPreference>) preference -> {
                String setting = preference.getValue();
                if (setting.equals("contemporary"))
                    return requireContext().getString(R.string.contemporary_title);
                else
                    return requireContext().getString(R.string.classical_title);
            });

            titleFormat.setOnPreferenceChangeListener((preference, newValue) -> {
                Toast.makeText(getContext(), requireContext().getString(R.string.restart_app), Toast.LENGTH_LONG).show();
                return true;
            });

            ListPreference theme = findPreference("theme");
            Objects.requireNonNull(theme).setOnPreferenceChangeListener((preference, newValue) -> {
                String newTheme = (String) newValue;
                if (newTheme.equals("dark"))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            });
        }
    }
}