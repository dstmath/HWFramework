package com.android.internal.telephony.ims;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.telephony.ims.aidl.IImsServiceController;
import android.telephony.ims.stub.ImsFeatureConfiguration;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ImsServiceFeatureQueryManager {
    private final Map<ComponentName, ImsServiceFeatureQuery> mActiveQueries = new HashMap();
    private final Context mContext;
    private final Listener mListener;
    private final Object mLock = new Object();

    public interface Listener {
        void onComplete(ComponentName componentName, Set<ImsFeatureConfiguration.FeatureSlotPair> set);

        void onError(ComponentName componentName);
    }

    /* access modifiers changed from: private */
    public final class ImsServiceFeatureQuery implements ServiceConnection {
        private static final String LOG_TAG = "ImsServiceFeatureQuery";
        private final String mIntentFilter;
        private final ComponentName mName;

        ImsServiceFeatureQuery(ComponentName name, String intentFilter) {
            this.mName = name;
            this.mIntentFilter = intentFilter;
        }

        public boolean start() {
            Log.d(LOG_TAG, "start: intent filter=" + this.mIntentFilter + ", name=" + this.mName);
            boolean bindStarted = ImsServiceFeatureQueryManager.this.mContext.bindService(new Intent(this.mIntentFilter).setComponent(this.mName), this, 67108929);
            if (!bindStarted) {
                cleanup();
            }
            return bindStarted;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(LOG_TAG, "onServiceConnected for component: " + name);
            if (service != null) {
                queryImsFeatures(IImsServiceController.Stub.asInterface(service));
                return;
            }
            Log.w(LOG_TAG, "onServiceConnected: " + name + " binder null, cleaning up.");
            cleanup();
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.w(LOG_TAG, "onServiceDisconnected for component: " + name);
        }

        private void queryImsFeatures(IImsServiceController controller) {
            try {
                ImsFeatureConfiguration config = controller.querySupportedImsFeatures();
                if (config != null) {
                    Set<ImsFeatureConfiguration.FeatureSlotPair> servicePairs = config.getServiceFeatures();
                    cleanup();
                    ImsServiceFeatureQueryManager.this.mListener.onComplete(this.mName, servicePairs);
                }
            } catch (Exception e) {
                Log.w(LOG_TAG, "queryImsFeatures - error: exception");
                cleanup();
                ImsServiceFeatureQueryManager.this.mListener.onError(this.mName);
            }
        }

        private void cleanup() {
            ImsServiceFeatureQueryManager.this.mContext.unbindService(this);
            synchronized (ImsServiceFeatureQueryManager.this.mLock) {
                ImsServiceFeatureQueryManager.this.mActiveQueries.remove(this.mName);
            }
        }
    }

    public ImsServiceFeatureQueryManager(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public boolean startQuery(ComponentName name, String intentFilter) {
        synchronized (this.mLock) {
            if (this.mActiveQueries.containsKey(name)) {
                return true;
            }
            ImsServiceFeatureQuery query = new ImsServiceFeatureQuery(name, intentFilter);
            this.mActiveQueries.put(name, query);
            return query.start();
        }
    }

    public boolean isQueryInProgress() {
        boolean z;
        synchronized (this.mLock) {
            z = !this.mActiveQueries.isEmpty();
        }
        return z;
    }
}
