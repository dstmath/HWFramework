package com.android.internal.telephony;

import android.common.FactoryLoader;
import android.util.Log;
import com.android.internal.telephony.fullnetwork.DefaultHwPartTelephonyFullnetworkFactory;
import com.android.internal.telephony.vsim.DefaultHwPartTelephonyVSimFactory;
import java.util.HashMap;
import java.util.Map;

public class HwPartOptTelephonyFactory {
    private static final int HASH_MAP_INIT_SIZE = 2;
    private static final Object LOCK = new Object();
    private static final String TAG = "HwPartOptTelephonyFactory";
    private static final String TELEPHONY_FULL_NETWORK_FACOTRY_NAME = "com.android.internal.telephony.fullnetwork.HwPartTelephonyFullnetworkFactoryImpl";
    private static final String TELEPHONY_VSIM_FACOTRY_NAME = "com.android.internal.telephony.vsim.HwPartTelephonyVSimFactoryImpl";
    private static volatile HwPartOptTelephonyFactory obj = null;
    private static Map<String, Object> sFactory = new HashMap(2);

    private HwPartOptTelephonyFactory() {
    }

    public static HwPartOptTelephonyFactory getTelephonyFactory() {
        synchronized (LOCK) {
            if (obj != null) {
                return obj;
            }
            obj = new HwPartOptTelephonyFactory();
            return obj;
        }
    }

    public DefaultHwPartTelephonyFullnetworkFactory getFullnetworkFactory() {
        if (sFactory.containsKey(TELEPHONY_FULL_NETWORK_FACOTRY_NAME)) {
            return (DefaultHwPartTelephonyFullnetworkFactory) sFactory.get(TELEPHONY_FULL_NETWORK_FACOTRY_NAME);
        }
        DefaultHwPartTelephonyFullnetworkFactory factory = (DefaultHwPartTelephonyFullnetworkFactory) FactoryLoader.loadFactory(TELEPHONY_FULL_NETWORK_FACOTRY_NAME);
        if (factory == null) {
            Log.d(TAG, "getFullnetworkFactory: factory is null, load default");
            factory = DefaultHwPartTelephonyFullnetworkFactory.getInstance();
        }
        sFactory.put(TELEPHONY_FULL_NETWORK_FACOTRY_NAME, factory);
        Log.i(TAG, "add " + factory + " to memory.");
        return factory;
    }

    public DefaultHwPartTelephonyVSimFactory getVSimFactory() {
        if (sFactory.containsKey(TELEPHONY_VSIM_FACOTRY_NAME)) {
            return (DefaultHwPartTelephonyVSimFactory) sFactory.get(TELEPHONY_VSIM_FACOTRY_NAME);
        }
        DefaultHwPartTelephonyVSimFactory factory = (DefaultHwPartTelephonyVSimFactory) FactoryLoader.loadFactory(TELEPHONY_VSIM_FACOTRY_NAME);
        if (factory == null) {
            Log.d(TAG, "getFullnetworkFactory: factory is null, load default");
            factory = DefaultHwPartTelephonyVSimFactory.getInstance();
        }
        sFactory.put(TELEPHONY_VSIM_FACOTRY_NAME, factory);
        Log.i(TAG, "add " + factory + " to memory.");
        return factory;
    }
}
