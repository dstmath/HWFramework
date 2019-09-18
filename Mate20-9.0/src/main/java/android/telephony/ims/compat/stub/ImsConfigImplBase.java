package android.telephony.ims.compat.stub;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.ImsConfig;
import com.android.ims.ImsConfigListener;
import com.android.ims.internal.IImsConfig;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ImsConfigImplBase {
    private static final String TAG = "ImsConfigImplBase";
    ImsConfigStub mImsConfigStub;

    @VisibleForTesting
    public static class ImsConfigStub extends IImsConfig.Stub {
        Context mContext;
        WeakReference<ImsConfigImplBase> mImsConfigImplBaseWeakReference;
        private HashMap<Integer, Integer> mProvisionedIntValue = new HashMap<>();
        private HashMap<Integer, String> mProvisionedStringValue = new HashMap<>();

        @VisibleForTesting
        public ImsConfigStub(ImsConfigImplBase imsConfigImplBase, Context context) {
            this.mContext = context;
            this.mImsConfigImplBaseWeakReference = new WeakReference<>(imsConfigImplBase);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002f, code lost:
            return r0;
         */
        public synchronized int getProvisionedValue(int item) throws RemoteException {
            if (this.mProvisionedIntValue.containsKey(Integer.valueOf(item))) {
                return this.mProvisionedIntValue.get(Integer.valueOf(item)).intValue();
            }
            int retVal = getImsConfigImpl().getProvisionedValue(item);
            if (retVal != -1) {
                updateCachedValue(item, retVal, false);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002a, code lost:
            return r0;
         */
        public synchronized String getProvisionedStringValue(int item) throws RemoteException {
            if (this.mProvisionedIntValue.containsKey(Integer.valueOf(item))) {
                return this.mProvisionedStringValue.get(Integer.valueOf(item));
            }
            String retVal = getImsConfigImpl().getProvisionedStringValue(item);
            if (retVal != null) {
                updateCachedValue(item, retVal, false);
            }
        }

        public synchronized int setProvisionedValue(int item, int value) throws RemoteException {
            int retVal;
            this.mProvisionedIntValue.remove(Integer.valueOf(item));
            retVal = getImsConfigImpl().setProvisionedValue(item, value);
            if (retVal == 0) {
                updateCachedValue(item, value, true);
            } else {
                Log.d(ImsConfigImplBase.TAG, "Set provision value of " + item + " to " + value + " failed with error code " + retVal);
            }
            return retVal;
        }

        public synchronized int setProvisionedStringValue(int item, String value) throws RemoteException {
            int retVal;
            this.mProvisionedStringValue.remove(Integer.valueOf(item));
            retVal = getImsConfigImpl().setProvisionedStringValue(item, value);
            if (retVal == 0) {
                updateCachedValue(item, value, true);
            }
            return retVal;
        }

        public void getFeatureValue(int feature, int network, ImsConfigListener listener) throws RemoteException {
            getImsConfigImpl().getFeatureValue(feature, network, listener);
        }

        public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) throws RemoteException {
            getImsConfigImpl().setFeatureValue(feature, network, value, listener);
        }

        public boolean getVolteProvisioned() throws RemoteException {
            return getImsConfigImpl().getVolteProvisioned();
        }

        public void getVideoQuality(ImsConfigListener listener) throws RemoteException {
            getImsConfigImpl().getVideoQuality(listener);
        }

        public void setVideoQuality(int quality, ImsConfigListener listener) throws RemoteException {
            getImsConfigImpl().setVideoQuality(quality, listener);
        }

        private ImsConfigImplBase getImsConfigImpl() throws RemoteException {
            ImsConfigImplBase ref = (ImsConfigImplBase) this.mImsConfigImplBaseWeakReference.get();
            if (ref != null) {
                return ref;
            }
            throw new RemoteException("Fail to get ImsConfigImpl");
        }

        private void sendImsConfigChangedIntent(int item, int value) {
            sendImsConfigChangedIntent(item, Integer.toString(value));
        }

        private void sendImsConfigChangedIntent(int item, String value) {
            Intent configChangedIntent = new Intent(ImsConfig.ACTION_IMS_CONFIG_CHANGED);
            configChangedIntent.putExtra(ImsConfig.EXTRA_CHANGED_ITEM, item);
            configChangedIntent.putExtra("value", value);
            if (this.mContext != null) {
                this.mContext.sendBroadcast(configChangedIntent);
            }
        }

        /* access modifiers changed from: protected */
        public synchronized void updateCachedValue(int item, int value, boolean notifyChange) {
            this.mProvisionedIntValue.put(Integer.valueOf(item), Integer.valueOf(value));
            if (notifyChange) {
                sendImsConfigChangedIntent(item, value);
            }
        }

        /* access modifiers changed from: protected */
        public synchronized void updateCachedValue(int item, String value, boolean notifyChange) {
            this.mProvisionedStringValue.put(Integer.valueOf(item), value);
            if (notifyChange) {
                sendImsConfigChangedIntent(item, value);
            }
        }
    }

    public ImsConfigImplBase(Context context) {
        this.mImsConfigStub = new ImsConfigStub(this, context);
    }

    public int getProvisionedValue(int item) throws RemoteException {
        return -1;
    }

    public String getProvisionedStringValue(int item) throws RemoteException {
        return null;
    }

    public int setProvisionedValue(int item, int value) throws RemoteException {
        return 1;
    }

    public int setProvisionedStringValue(int item, String value) throws RemoteException {
        return 1;
    }

    public void getFeatureValue(int feature, int network, ImsConfigListener listener) throws RemoteException {
    }

    public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) throws RemoteException {
    }

    public boolean getVolteProvisioned() throws RemoteException {
        return false;
    }

    public void getVideoQuality(ImsConfigListener listener) throws RemoteException {
    }

    public void setVideoQuality(int quality, ImsConfigListener listener) throws RemoteException {
    }

    public IImsConfig getIImsConfig() {
        return this.mImsConfigStub;
    }

    public final void notifyProvisionedValueChanged(int item, int value) {
        this.mImsConfigStub.updateCachedValue(item, value, true);
    }

    public final void notifyProvisionedValueChanged(int item, String value) {
        this.mImsConfigStub.updateCachedValue(item, value, true);
    }
}
