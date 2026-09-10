package com.deckerth.thomas.foobarremotecontroller2.phone_integration;

import android.telephony.PhoneStateListener;

import java.security.Provider;
import java.util.List;
import java.util.Map;

public class CallMonitoringService extends Provider.Service {
    public CallMonitoringService(Provider provider, String type, String algorithm, String className, List<String> aliases, Map<String, String> attributes) {
        super(provider, type, algorithm, className, aliases, attributes);
    }


}
