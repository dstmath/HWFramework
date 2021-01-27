package com.android.internal.telephony.separated;

import android.common.FactoryLoader;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class TelephonySeparatedFactory {
    private static final int HASH_MAP_INIT_SIZE = 2;
    private static final Object LOCK = new Object();
    private static final String TAG = "TelephonySeparatedFactory";
    private static final String TELEPHONY_SEPARATED_IMPL_FACOTRY_NAME = "com.android.internal.telephony.separated.TelephonySeparatedFactoryImpl";
    private static volatile TelephonySeparatedFactory obj = null;
    private static Map<String, Object> sFactory = new HashMap(2);

    private TelephonySeparatedFactory() {
    }

    public static TelephonySeparatedFactory getTelephonyFactory() {
        synchronized (LOCK) {
            if (obj != null) {
                return obj;
            }
            obj = new TelephonySeparatedFactory();
            return obj;
        }
    }

    public DefaultTelephonySeparatedFactory getTelephonySeparatedFactory() {
        if (sFactory.containsKey(TELEPHONY_SEPARATED_IMPL_FACOTRY_NAME)) {
            return (DefaultTelephonySeparatedFactory) sFactory.get(TELEPHONY_SEPARATED_IMPL_FACOTRY_NAME);
        }
        DefaultTelephonySeparatedFactory factory = (DefaultTelephonySeparatedFactory) FactoryLoader.loadFactory(TELEPHONY_SEPARATED_IMPL_FACOTRY_NAME);
        if (factory == null) {
            Log.d(TAG, "getTelephonySeparatedFactory: factory is null, load default");
            factory = DefaultTelephonySeparatedFactory.getInstance();
        }
        sFactory.put(TELEPHONY_SEPARATED_IMPL_FACOTRY_NAME, factory);
        Log.i(TAG, "add " + factory.getClass().getCanonicalName() + " to memory.");
        return factory;
    }
}
