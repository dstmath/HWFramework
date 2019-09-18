package android.telephony.ims.compat.feature;

import android.content.Context;
import android.content.Intent;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.internal.IImsFeatureStatusCallback;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class ImsFeature {
    public static final String ACTION_IMS_SERVICE_DOWN = "com.android.ims.IMS_SERVICE_DOWN";
    public static final String ACTION_IMS_SERVICE_UP = "com.android.ims.IMS_SERVICE_UP";
    public static final int EMERGENCY_MMTEL = 0;
    public static final String EXTRA_PHONE_ID = "android:phone_id";
    public static final int INVALID = -1;
    private static final String LOG_TAG = "ImsFeature";
    public static final int MAX = 3;
    public static final int MMTEL = 1;
    public static final int RCS = 2;
    public static final int STATE_INITIALIZING = 1;
    public static final int STATE_NOT_AVAILABLE = 0;
    public static final int STATE_READY = 2;
    protected Context mContext;
    private int mSlotId = -1;
    private int mState = 0;
    private final Set<IImsFeatureStatusCallback> mStatusCallbacks = Collections.newSetFromMap(new WeakHashMap());

    @Retention(RetentionPolicy.SOURCE)
    public @interface ImsState {
    }

    public abstract IInterface getBinder();

    public abstract void onFeatureReady();

    public abstract void onFeatureRemoved();

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void setSlotId(int slotId) {
        this.mSlotId = slotId;
    }

    public int getFeatureState() {
        return this.mState;
    }

    /* access modifiers changed from: protected */
    public final void setFeatureState(int state) {
        if (this.mState != state) {
            this.mState = state;
            notifyFeatureState(state);
        }
    }

    public void addImsFeatureStatusCallback(IImsFeatureStatusCallback c) {
        if (c != null) {
            try {
                c.notifyImsFeatureStatus(this.mState);
                synchronized (this.mStatusCallbacks) {
                    this.mStatusCallbacks.add(c);
                }
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Couldn't notify feature state: " + e.getMessage());
            }
        }
    }

    public void removeImsFeatureStatusCallback(IImsFeatureStatusCallback c) {
        if (c != null) {
            synchronized (this.mStatusCallbacks) {
                this.mStatusCallbacks.remove(c);
            }
        }
    }

    private void notifyFeatureState(int state) {
        synchronized (this.mStatusCallbacks) {
            Iterator<IImsFeatureStatusCallback> iter = this.mStatusCallbacks.iterator();
            while (iter.hasNext()) {
                IImsFeatureStatusCallback callback = iter.next();
                try {
                    Log.i(LOG_TAG, "notifying ImsFeatureState=" + state);
                    callback.notifyImsFeatureStatus(state);
                } catch (RemoteException e) {
                    iter.remove();
                    Log.w(LOG_TAG, "Couldn't notify feature state: " + e.getMessage());
                }
            }
        }
        sendImsServiceIntent(state);
    }

    private void sendImsServiceIntent(int state) {
        Intent intent;
        if (this.mContext != null && this.mSlotId != -1) {
            switch (state) {
                case 0:
                case 1:
                    intent = new Intent("com.android.ims.IMS_SERVICE_DOWN");
                    break;
                case 2:
                    intent = new Intent("com.android.ims.IMS_SERVICE_UP");
                    break;
                default:
                    intent = new Intent("com.android.ims.IMS_SERVICE_DOWN");
                    break;
            }
            intent.putExtra("android:phone_id", this.mSlotId);
            this.mContext.sendBroadcast(intent);
        }
    }
}
