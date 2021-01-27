package android.telephony.ims.stub;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsConfigCallback;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.function.Consumer;

@SystemApi
public class ImsConfigImplBase {
    public static final int CONFIG_RESULT_FAILED = 1;
    public static final int CONFIG_RESULT_SUCCESS = 0;
    public static final int CONFIG_RESULT_UNKNOWN = -1;
    private static final String TAG = "ImsConfigImplBase";
    private final RemoteCallbackList<IImsConfigCallback> mCallbacks = new RemoteCallbackList<>();
    ImsConfigStub mImsConfigStub = new ImsConfigStub(this);

    @Retention(RetentionPolicy.SOURCE)
    public @interface SetConfigResult {
    }

    @VisibleForTesting
    public static class ImsConfigStub extends IImsConfig.Stub {
        WeakReference<ImsConfigImplBase> mImsConfigImplBaseWeakReference;
        private HashMap<Integer, Integer> mProvisionedIntValue = new HashMap<>();
        private HashMap<Integer, String> mProvisionedStringValue = new HashMap<>();

        @VisibleForTesting
        public ImsConfigStub(ImsConfigImplBase imsConfigImplBase) {
            this.mImsConfigImplBaseWeakReference = new WeakReference<>(imsConfigImplBase);
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public void addImsConfigCallback(IImsConfigCallback c) throws RemoteException {
            getImsConfigImpl().addImsConfigCallback(c);
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public void removeImsConfigCallback(IImsConfigCallback c) throws RemoteException {
            getImsConfigImpl().removeImsConfigCallback(c);
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public synchronized int getConfigInt(int item) throws RemoteException {
            if (this.mProvisionedIntValue.containsKey(Integer.valueOf(item))) {
                return this.mProvisionedIntValue.get(Integer.valueOf(item)).intValue();
            }
            int retVal = getImsConfigImpl().getConfigInt(item);
            if (retVal != -1) {
                updateCachedValue(item, retVal, false);
            }
            return retVal;
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public synchronized String getConfigString(int item) throws RemoteException {
            if (this.mProvisionedIntValue.containsKey(Integer.valueOf(item))) {
                return this.mProvisionedStringValue.get(Integer.valueOf(item));
            }
            String retVal = getImsConfigImpl().getConfigString(item);
            if (retVal != null) {
                updateCachedValue(item, retVal, false);
            }
            return retVal;
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public synchronized int setConfigInt(int item, int value) throws RemoteException {
            int retVal;
            this.mProvisionedIntValue.remove(Integer.valueOf(item));
            retVal = getImsConfigImpl().setConfig(item, value);
            if (retVal == 0) {
                updateCachedValue(item, value, true);
            } else {
                Log.d(ImsConfigImplBase.TAG, "Set provision value of " + item + " to " + value + " failed with error code " + retVal);
            }
            return retVal;
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public synchronized int setConfigString(int item, String value) throws RemoteException {
            int retVal;
            this.mProvisionedStringValue.remove(Integer.valueOf(item));
            retVal = getImsConfigImpl().setConfig(item, value);
            if (retVal == 0) {
                updateCachedValue(item, value, true);
            }
            return retVal;
        }

        private ImsConfigImplBase getImsConfigImpl() throws RemoteException {
            ImsConfigImplBase ref = this.mImsConfigImplBaseWeakReference.get();
            if (ref != null) {
                return ref;
            }
            throw new RemoteException("Fail to get ImsConfigImpl");
        }

        private void notifyImsConfigChanged(int item, int value) throws RemoteException {
            getImsConfigImpl().notifyConfigChanged(item, value);
        }

        private void notifyImsConfigChanged(int item, String value) throws RemoteException {
            getImsConfigImpl().notifyConfigChanged(item, value);
        }

        /* access modifiers changed from: protected */
        public synchronized void updateCachedValue(int item, int value, boolean notifyChange) throws RemoteException {
            this.mProvisionedIntValue.put(Integer.valueOf(item), Integer.valueOf(value));
            if (notifyChange) {
                notifyImsConfigChanged(item, value);
            }
        }

        /* access modifiers changed from: protected */
        public synchronized void updateCachedValue(int item, String value, boolean notifyChange) throws RemoteException {
            this.mProvisionedStringValue.put(Integer.valueOf(item), value);
            if (notifyChange) {
                notifyImsConfigChanged(item, value);
            }
        }

        @Override // android.telephony.ims.aidl.IImsConfig.Stub, android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (getImsConfigImpl().isHwCustCode(code)) {
                return getImsConfigImpl().onTransact(code, data, reply, flags);
            }
            return super.onTransact(code, data, reply, flags);
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public int setImsConfig(String configKey, PersistableBundle configValue) throws RemoteException {
            return getImsConfigImpl().setImsConfig(configKey, configValue);
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public PersistableBundle getImsConfig(String configKey) throws RemoteException {
            return getImsConfigImpl().getImsConfig(configKey);
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return false;
    }

    public boolean isHwCustCode(int code) {
        return false;
    }

    public ImsConfigImplBase(Context context) {
    }

    public ImsConfigImplBase() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addImsConfigCallback(IImsConfigCallback c) {
        this.mCallbacks.register(c);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeImsConfigCallback(IImsConfigCallback c) {
        this.mCallbacks.unregister(c);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void notifyConfigChanged(int item, int value) {
        RemoteCallbackList<IImsConfigCallback> remoteCallbackList = this.mCallbacks;
        if (remoteCallbackList != null) {
            remoteCallbackList.broadcast(new Consumer(item, value) {
                /* class android.telephony.ims.stub.$$Lambda$ImsConfigImplBase$yL4863kFoQyqg_FX2mWsLMqbyA */
                private final /* synthetic */ int f$0;
                private final /* synthetic */ int f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ImsConfigImplBase.lambda$notifyConfigChanged$0(this.f$0, this.f$1, (IImsConfigCallback) obj);
                }
            });
        }
    }

    static /* synthetic */ void lambda$notifyConfigChanged$0(int item, int value, IImsConfigCallback c) {
        try {
            c.onIntConfigChanged(item, value);
        } catch (RemoteException e) {
            Log.w(TAG, "notifyConfigChanged(int): dead binder in notify, skipping.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyConfigChanged(int item, String value) {
        RemoteCallbackList<IImsConfigCallback> remoteCallbackList = this.mCallbacks;
        if (remoteCallbackList != null) {
            remoteCallbackList.broadcast(new Consumer(item, value) {
                /* class android.telephony.ims.stub.$$Lambda$ImsConfigImplBase$GAuYvQ8qBc7KgCJhNp4Pt4j5t0 */
                private final /* synthetic */ int f$0;
                private final /* synthetic */ String f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ImsConfigImplBase.lambda$notifyConfigChanged$1(this.f$0, this.f$1, (IImsConfigCallback) obj);
                }
            });
        }
    }

    static /* synthetic */ void lambda$notifyConfigChanged$1(int item, String value, IImsConfigCallback c) {
        try {
            c.onStringConfigChanged(item, value);
        } catch (RemoteException e) {
            Log.w(TAG, "notifyConfigChanged(string): dead binder in notify, skipping.");
        }
    }

    public IImsConfig getIImsConfig() {
        return this.mImsConfigStub;
    }

    public final void notifyProvisionedValueChanged(int item, int value) {
        try {
            this.mImsConfigStub.updateCachedValue(item, value, true);
        } catch (RemoteException e) {
            Log.w(TAG, "notifyProvisionedValueChanged(int): Framework connection is dead.");
        }
    }

    public final void notifyProvisionedValueChanged(int item, String value) {
        try {
            this.mImsConfigStub.updateCachedValue(item, value, true);
        } catch (RemoteException e) {
            Log.w(TAG, "notifyProvisionedValueChanged(string): Framework connection is dead.");
        }
    }

    public int setConfig(int item, int value) {
        return 1;
    }

    public int setConfig(int item, String value) {
        return 1;
    }

    public int getConfigInt(int item) {
        return -1;
    }

    public String getConfigString(int item) {
        return null;
    }

    public int setImsConfig(String configKey, PersistableBundle configValue) throws RemoteException {
        return 1;
    }

    public PersistableBundle getImsConfig(String configKey) throws RemoteException {
        return null;
    }
}
