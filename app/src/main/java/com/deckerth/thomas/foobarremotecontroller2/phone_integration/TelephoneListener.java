package com.deckerth.thomas.foobarremotecontroller2.phone_integration;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.deckerth.thomas.foobarremotecontroller2.TitleDetailHostActivity;
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess;

import java.util.Objects;
import java.util.concurrent.Executor;

public class TelephoneListener {

     private final ActivityResultLauncher<String> mRequestSinglePermissionLauncher;
     private final TitleDetailHostActivity mHostActivity;
    public TelephoneListener(ActivityResultLauncher<String> requestSinglePermissionLauncher, TitleDetailHostActivity hostActivity) {
        this.mRequestSinglePermissionLauncher = requestSinglePermissionLauncher;
        this.mHostActivity = hostActivity;
    }

    private class Callback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        public void onCallStateChanged(int state) {
            // Handle call state changes (e.g., ringing, off-hook, idle)
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // Handle incoming call (e.g., display a notification)
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    PlayerAccess.getInstance().pausePlayback();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    // Call ended (e.g., stop tracking call duration)
                    break;
            }
        }
    }

    public void register(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
                telephonyManager.registerTelephonyCallback(
                        context.getMainExecutor(),
                        new Callback()
                );
            else {
                mRequestSinglePermissionLauncher.launch(
                        Manifest.permission.READ_PHONE_STATE);
                if (mHostActivity.getGrantedState() == TitleDetailHostActivity.GrantedState.GRANTED) {
                    telephonyManager.registerTelephonyCallback(
                            context.getMainExecutor(),
                            new Callback()
                    );
                }
            }
        }
    }
}
