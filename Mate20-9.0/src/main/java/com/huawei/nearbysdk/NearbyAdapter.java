package com.huawei.nearbysdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import com.huawei.nearbysdk.IAuthAdapter;
import com.huawei.nearbysdk.INearbyAdapter;
import com.huawei.nearbysdk.NearbyConfig;
import com.huawei.nearbysdk.closeRange.CloseRangeAdapter;
import com.huawei.nearbysdk.closeRange.CloseRangeBusinessType;
import com.huawei.nearbysdk.closeRange.CloseRangeDeviceFilter;
import com.huawei.nearbysdk.closeRange.CloseRangeDeviceListener;
import com.huawei.nearbysdk.closeRange.CloseRangeEventFilter;
import com.huawei.nearbysdk.closeRange.CloseRangeEventListener;
import com.huawei.nearbysdk.closeRange.CloseRangeInterface;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

public final class NearbyAdapter implements CloseRangeInterface {
    private static final int INVALIDE_CHANNEL = -1;
    private static final int INVALIDE_TYPE_CHANNEL = -1;
    static final String TAG = "NearbyServiceJar";
    private static CloseRangeAdapter closeRangeAdapter = null;
    private static final Object closeRangeLock = new Object();
    /* access modifiers changed from: private */
    public static Boolean mAuthBound = false;
    /* access modifiers changed from: private */
    public static NearbyAuthServiceConnection mAuthConnection;
    private static final Object mAuthLock = new Object();
    /* access modifiers changed from: private */
    public static IAuthAdapter mAuthService;
    /* access modifiers changed from: private */
    public static NearbyServiceConnection mConnection;
    private static final HashMap<String, byte[]> mDeviceSum_SessionKeys = new HashMap<>();
    /* access modifiers changed from: private */
    public static NearbyAdapter mNearbyAdapter;
    /* access modifiers changed from: private */
    public static Boolean mNearbyBound = false;
    private static Context mNearbyContext;
    private static final Object mNearbyLock = new Object();
    /* access modifiers changed from: private */
    public static INearbyAdapter mNearbyService;
    private static HandlerThread mThread;
    /* access modifiers changed from: private */
    public static Boolean mUnBoundFlag = false;
    private final HashMap<Long, NearbyDevice> mAuthId_Device;
    private final HashMap<AuthListener, AuthListenerTransport> mAuthListeners;
    private final HashMap<ConnectionListener, ConnectionListenerTransport> mConnectionListeners;
    private final HashMap<CreateSocketListener, CreateSocketListenerTransport> mCreateSocketListeners;
    private final HashMap<String, byte[]> mDevice_RSAKeys;
    private final HashMap<AuthListener, Integer> mMapBussinessIdListener;
    private final HashMap<PublishListener, PublishListenerTransport> mPublishListeners;
    private final HashMap<SocketListener, SocketListenerTransport> mSocketListeners;
    private final HashMap<SubscribeListener, SubscribeListenerTransport> mSubscribeListeners;

    public static class EncryptCfbFailException extends Exception {
        EncryptCfbFailException(String msg) {
            super(msg);
        }
    }

    public interface NAdapterGetCallback {
        void onAdapterGet(NearbyAdapter nearbyAdapter);
    }

    private static class NearbyAuthServiceConnection implements ServiceConnection {
        private NAdapterGetCallback callback;
        private Context context;

        public NearbyAuthServiceConnection(Context context2, NAdapterGetCallback callback2) {
            this.context = context2.getApplicationContext();
            this.callback = callback2;
            HwLog.d(NearbyAdapter.TAG, "NearbyAuthServiceConnection construct");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0070, code lost:
            return;
         */
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            synchronized (this) {
                HwLog.d(NearbyAdapter.TAG, "client onAuthServiceConnected  || service " + service);
                IAuthAdapter unused = NearbyAdapter.mAuthService = IAuthAdapter.Stub.asInterface(service);
                if (NearbyAdapter.mNearbyAdapter == null) {
                    NearbyAdapter unused2 = NearbyAdapter.mNearbyAdapter = new NearbyAdapter();
                }
                NearbyAdapter unused3 = NearbyAdapter.mNearbyAdapter;
                NearbyAdapter.setAuthService(NearbyAdapter.mAuthService);
                NearbyAdapter unused4 = NearbyAdapter.mNearbyAdapter;
                NearbyAuthServiceConnection unused5 = NearbyAdapter.mAuthConnection = this;
                Boolean unused6 = NearbyAdapter.mAuthBound = true;
                if (NearbyAdapter.mNearbyBound.booleanValue() && this.callback != null) {
                    if (NearbyAdapter.mNearbyAdapter.hasNullService()) {
                        NearbyAdapter.callBackNull(this.context, this.callback);
                        return;
                    }
                    this.callback.onAdapterGet(NearbyAdapter.mNearbyAdapter);
                }
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            synchronized (this) {
                HwLog.d(NearbyAdapter.TAG, "onAuthServiceDisconnected");
                if (!NearbyAdapter.mUnBoundFlag.booleanValue()) {
                    NearbyAdapter.unbindAidlService(this.context);
                } else {
                    IAuthAdapter unused = NearbyAdapter.mAuthService = null;
                    Boolean unused2 = NearbyAdapter.mAuthBound = false;
                }
            }
        }
    }

    private static class NearbyServiceConnection implements ServiceConnection {
        private NAdapterGetCallback callback;
        private Context context;

        public NearbyServiceConnection(Context context2, NAdapterGetCallback callback2) {
            this.context = context2.getApplicationContext();
            this.callback = callback2;
            HwLog.d(NearbyAdapter.TAG, "NearbyServiceConnection construct");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a2, code lost:
            return;
         */
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            synchronized (this) {
                HwLog.d(NearbyAdapter.TAG, "client onServiceConnected  || service " + service);
                INearbyAdapter unused = NearbyAdapter.mNearbyService = INearbyAdapter.Stub.asInterface(service);
                boolean hasInit = false;
                try {
                    hasInit = NearbyAdapter.mNearbyService.hasInit();
                } catch (RemoteException e) {
                    HwLog.e(NearbyAdapter.TAG, "error in onServiceConnected" + e.getLocalizedMessage());
                }
                if (!hasInit) {
                    HwLog.d(NearbyAdapter.TAG, "mNearbyService has not init. set mNearbyService = null");
                    INearbyAdapter unused2 = NearbyAdapter.mNearbyService = null;
                }
                if (NearbyAdapter.mNearbyAdapter == null) {
                    NearbyAdapter unused3 = NearbyAdapter.mNearbyAdapter = new NearbyAdapter();
                }
                NearbyAdapter unused4 = NearbyAdapter.mNearbyAdapter;
                NearbyAdapter.setNearbySevice(NearbyAdapter.mNearbyService);
                NearbyAdapter unused5 = NearbyAdapter.mNearbyAdapter;
                NearbyServiceConnection unused6 = NearbyAdapter.mConnection = this;
                Boolean unused7 = NearbyAdapter.mNearbyBound = true;
                if (NearbyAdapter.mAuthBound.booleanValue() && this.callback != null) {
                    if (NearbyAdapter.mNearbyAdapter.hasNullService()) {
                        NearbyAdapter.callBackNull(this.context, this.callback);
                        return;
                    }
                    this.callback.onAdapterGet(NearbyAdapter.mNearbyAdapter);
                }
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            synchronized (this) {
                HwLog.d(NearbyAdapter.TAG, "onServiceDisconnected");
                if (!NearbyAdapter.mUnBoundFlag.booleanValue()) {
                    NearbyAdapter.unbindAidlService(this.context);
                } else {
                    INearbyAdapter unused = NearbyAdapter.mNearbyService = null;
                    Boolean unused2 = NearbyAdapter.mNearbyBound = false;
                }
            }
        }
    }

    public boolean subscribeEvent(CloseRangeEventFilter eventFilter, CloseRangeEventListener eventListener) {
        synchronized (closeRangeLock) {
            if (closeRangeAdapter == null) {
                HwLog.e(TAG, "empty adapter");
                return false;
            }
            boolean subscribeEvent = closeRangeAdapter.subscribeEvent(eventFilter, eventListener);
            return subscribeEvent;
        }
    }

    public boolean unSubscribeEvent(CloseRangeEventFilter eventFilter) {
        synchronized (closeRangeLock) {
            if (closeRangeAdapter == null) {
                HwLog.e(TAG, "empty adapter");
                return false;
            }
            boolean unSubscribeEvent = closeRangeAdapter.unSubscribeEvent(eventFilter);
            return unSubscribeEvent;
        }
    }

    public boolean subscribeDevice(CloseRangeDeviceFilter deviceFilter, CloseRangeDeviceListener deviceListener) {
        synchronized (closeRangeLock) {
            if (closeRangeAdapter == null) {
                HwLog.e(TAG, "empty adapter");
                return false;
            }
            boolean subscribeDevice = closeRangeAdapter.subscribeDevice(deviceFilter, deviceListener);
            return subscribeDevice;
        }
    }

    public boolean unSubscribeDevice(CloseRangeDeviceFilter deviceFilter) {
        synchronized (closeRangeLock) {
            if (closeRangeAdapter == null) {
                HwLog.e(TAG, "empty adapter");
                return false;
            }
            boolean unSubscribeDevice = closeRangeAdapter.unSubscribeDevice(deviceFilter);
            return unSubscribeDevice;
        }
    }

    public boolean setFrequency(CloseRangeBusinessType type, BleScanLevel frequency) {
        synchronized (closeRangeLock) {
            if (closeRangeAdapter == null) {
                HwLog.e(TAG, "empty adapter");
                return false;
            }
            boolean frequency2 = closeRangeAdapter.setFrequency(type, frequency);
            return frequency2;
        }
    }

    private NearbyAdapter() {
        this.mPublishListeners = new HashMap<>();
        this.mSubscribeListeners = new HashMap<>();
        this.mSocketListeners = new HashMap<>();
        this.mConnectionListeners = new HashMap<>();
        this.mCreateSocketListeners = new HashMap<>();
        this.mAuthListeners = new HashMap<>();
        this.mMapBussinessIdListener = new HashMap<>();
        this.mDevice_RSAKeys = new HashMap<>();
        this.mAuthId_Device = new HashMap<>();
        if (mThread == null) {
            mThread = new HandlerThread("NearbyAdapter Looper");
            mThread.start();
        }
        synchronized (closeRangeLock) {
            closeRangeAdapter = new CloseRangeAdapter(mThread);
        }
        HwLog.d(TAG, "NearbyAdapter init");
    }

    /* access modifiers changed from: private */
    public static void setNearbySevice(INearbyAdapter remoteNearbySevice) {
        mNearbyService = remoteNearbySevice;
        synchronized (closeRangeLock) {
            closeRangeAdapter.setNearbyService(mNearbyService);
        }
    }

    /* access modifiers changed from: private */
    public static void setAuthService(IAuthAdapter remoteAuthSevice) {
        mAuthService = remoteAuthSevice;
    }

    /* access modifiers changed from: private */
    public boolean hasNullService() {
        return mNearbyService == null || mAuthService == null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003e, code lost:
        return;
     */
    public static synchronized void getNearbyAdapter(Context context, NAdapterGetCallback callback) {
        synchronized (NearbyAdapter.class) {
            HwLog.d(TAG, "getNearbyAdapter");
            if (context == null) {
                HwLog.e(TAG, "context is null && return.");
            } else if (callback == null) {
                HwLog.e(TAG, "callback is null && return.");
            } else if (mNearbyAdapter == null || !mNearbyBound.booleanValue() || !mAuthBound.booleanValue()) {
                bindAidlService(context, callback);
            } else {
                callback.onAdapterGet(mNearbyAdapter);
            }
        }
    }

    public static synchronized void createInstance(Context context, NearbyAdapterCallback callback) {
        synchronized (NearbyAdapter.class) {
            HwLog.d(TAG, "createInstance start " + mNearbyContext);
            if (mNearbyContext == null) {
                if (context == null || callback == null) {
                    HwLog.e(TAG, "createInstance context or callback null");
                    throw new IllegalArgumentException("createInstance context or callback null");
                }
                mNearbyContext = context;
                getNearbyAdapter(context, callback);
            }
        }
    }

    public static synchronized void releaseInstance() {
        synchronized (NearbyAdapter.class) {
            if (mNearbyContext == null) {
                HwLog.e(TAG, "Instance of NearbyAdapter already released or have not got yet");
                return;
            }
            unbindAidlService(mNearbyContext);
            mNearbyContext = null;
        }
    }

    public Looper getLooper() {
        synchronized (NearbyAdapter.class) {
            if (mThread == null) {
                return null;
            }
            Looper looper = mThread.getLooper();
            return looper;
        }
    }

    public INearbyAdapter getNearbyService() {
        INearbyAdapter iNearbyAdapter;
        synchronized (this) {
            iNearbyAdapter = mNearbyService;
        }
        return iNearbyAdapter;
    }

    /* access modifiers changed from: private */
    public static void callBackNull(Context context, NAdapterGetCallback callback) {
        callback.onAdapterGet(null);
        unbindAidlService(context);
    }

    public static void bindAidlService(Context context, NAdapterGetCallback callback) {
        HwLog.d(TAG, "bindAidlService mNearbyBound = " + mNearbyBound + ";mAuthBound : " + mAuthBound);
        String runningActivity = NearbyConfig.getCurPackageName(mNearbyContext);
        StringBuilder sb = new StringBuilder();
        sb.append("nearbyJar final runningActivity name: ");
        sb.append(runningActivity);
        HwLog.i(TAG, sb.toString());
        mUnBoundFlag = false;
        synchronized (mNearbyLock) {
            if (!mNearbyBound.booleanValue()) {
                Intent intent = new Intent();
                intent.setAction("com.huawei.nearby.NEARBY_SERVICE");
                intent.setPackage(runningActivity);
                try {
                    if (NearbyConfig.isRunAsAar(mNearbyContext)) {
                        HwLog.e(TAG, "run as aar try bind service!");
                        context.bindService(intent, new NearbyServiceConnection(context, callback), 1);
                    } else {
                        try {
                            HwLog.e(TAG, "try bind service!");
                            context.bindService(intent, new NearbyServiceConnection(context, callback), 1);
                        } catch (Throwable e) {
                            HwLog.e(TAG, "bindServiceAsUser ERROR:" + e.getLocalizedMessage());
                        }
                    }
                } catch (Exception e2) {
                    HwLog.e(TAG, "bindAidlService bindService NearbyServiceConnection ERROR:" + e2.getLocalizedMessage());
                }
            }
        }
        synchronized (mAuthLock) {
            if (!mAuthBound.booleanValue()) {
                Intent intentAuth = new Intent();
                intentAuth.setAction("com.huawei.nearby.NEARBY_AUTH_SERVICE");
                intentAuth.setPackage(runningActivity);
                try {
                    if (NearbyConfig.isRunAsAar(mNearbyContext)) {
                        context.bindService(intentAuth, new NearbyAuthServiceConnection(context, callback), 1);
                    } else {
                        try {
                            HwLog.e(TAG, "try bind Auth service!");
                            context.bindService(intentAuth, new NearbyAuthServiceConnection(context, callback), 1);
                        } catch (Throwable e3) {
                            HwLog.e(TAG, "bindAuthServiceAsUser ERROR:" + e3.getLocalizedMessage());
                        }
                    }
                } catch (Exception e4) {
                    HwLog.e(TAG, "bindAidlService bindService NearbyAuthServiceConnection ERROR:" + e4.getLocalizedMessage());
                }
            }
        }
    }

    public static void unbindAidlService(Context context) {
        HwLog.d(TAG, "unbindAidlService mNearbyBound = " + mNearbyBound + ";mAuthBound : " + mAuthBound);
        mUnBoundFlag = true;
        try {
            synchronized (mNearbyLock) {
                if (mNearbyBound.booleanValue()) {
                    mNearbyService = null;
                    mNearbyAdapter = null;
                    context.unbindService(mConnection);
                    mNearbyBound = false;
                    if (mThread != null) {
                        if (Build.VERSION.SDK_INT >= 18) {
                            mThread.quitSafely();
                        } else {
                            mThread.quit();
                        }
                        mThread = null;
                    }
                }
            }
        } catch (Exception e) {
            HwLog.e(TAG, "error in unbindAidlService mNearbyService" + e.getLocalizedMessage());
            mNearbyBound = false;
        }
        try {
            synchronized (mAuthLock) {
                if (mAuthBound.booleanValue()) {
                    mAuthService = null;
                    mNearbyAdapter = null;
                    context.unbindService(mAuthConnection);
                    mAuthBound = false;
                    if (mThread != null) {
                        if (Build.VERSION.SDK_INT >= 18) {
                            mThread.quitSafely();
                        } else {
                            mThread.quit();
                        }
                        mThread = null;
                    }
                }
            }
        } catch (Exception e2) {
            HwLog.e(TAG, "error in unbindAidlService mAuthService" + e2.getLocalizedMessage());
            mAuthBound = false;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        HwLog.d(TAG, "Adapter finalize");
        super.finalize();
    }

    public boolean publish(int businessId, PublishListener listener) {
        return publish(NearbyConfig.BusinessTypeEnum.AllType, businessId, listener);
    }

    public boolean publish(NearbyConfig.BusinessTypeEnum businessType, int businessId, PublishListener listener) {
        return publish(businessType, businessId, -1, listener);
    }

    public boolean publish(NearbyConfig.BusinessTypeEnum businessType, int businessId, int typeChannel, PublishListener listener) {
        HwLog.d(TAG, "publish");
        return publish(businessType, businessId, typeChannel, listener, Looper.myLooper());
    }

    public boolean publish(NearbyConfig.BusinessTypeEnum businessType, int businessId, int typeChannel, PublishListener listener, Looper looper) {
        HwLog.d(TAG, "publish with new looper");
        boolean result = false;
        if (businessType == null || listener == null) {
            HwLog.e(TAG, "publish get null param");
            return false;
        } else if (this.mPublishListeners.get(listener) != null) {
            HwLog.d(TAG, "PublishListener already registered && return");
            return false;
        } else {
            INearbyAdapter nearbyAdapter = mNearbyService;
            if (nearbyAdapter == null) {
                HwLog.e(TAG, "mNearbyService is null. Publish return false");
                return false;
            }
            if (looper == null) {
                HwLog.e(TAG, "PublishListener looper can not be null");
                looper = mThread.getLooper();
            }
            try {
                PublishListenerTransport transport = new PublishListenerTransport(listener, looper);
                HwLog.d(TAG, "mNearbyService.publish start");
                result = nearbyAdapter.publish(businessType.toNumber(), businessId, typeChannel, transport);
                if (result) {
                    HwLog.d(TAG, "put PublishListener into map");
                    this.mPublishListeners.put(listener, transport);
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in publish" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public boolean unPublish(PublishListener listener) {
        HwLog.d(TAG, "unPublish");
        boolean result = false;
        if (listener == null) {
            HwLog.e(TAG, "unPublish get null param");
            return false;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. unPublish return false");
            return false;
        }
        try {
            PublishListenerTransport transport = this.mPublishListeners.remove(listener);
            if (transport != null) {
                HwLog.d(TAG, "mNearbyService.unPublish start");
                result = nearbyAdapter.unPublish(transport);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in unPublish" + e.getLocalizedMessage());
        }
        return result;
    }

    public boolean subscribe(boolean allowWakeupById, int businessId, SubscribeListener listener) {
        HwLog.d(TAG, "subscribe");
        return subscribe(allowWakeupById, businessId, listener, Looper.myLooper());
    }

    public boolean subscribe(boolean allowWakeupById, int businessId, SubscribeListener listener, Looper looper) {
        HwLog.d(TAG, "subscribe with new looper");
        boolean result = false;
        if (listener == null) {
            HwLog.e(TAG, "subscribe get null param");
            return false;
        } else if (this.mSubscribeListeners.get(listener) != null) {
            HwLog.d(TAG, "SubscribeListener already registered && return");
            return false;
        } else {
            INearbyAdapter nearbyAdapter = mNearbyService;
            if (nearbyAdapter == null) {
                HwLog.e(TAG, "mNearbyService is null. subscribe return false");
                return false;
            }
            if (looper == null) {
                HwLog.e(TAG, "SubscribeListener looper can not be null");
                looper = mThread.getLooper();
            }
            try {
                SubscribeListenerTransport transport = new SubscribeListenerTransport(listener, looper);
                HwLog.d(TAG, "mNearbyService.subscribe start");
                result = nearbyAdapter.subscribe(allowWakeupById, businessId, transport);
                if (result) {
                    HwLog.d(TAG, "put SubscribeListener into map");
                    this.mSubscribeListeners.put(listener, transport);
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in subscribe" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public boolean unSubscribe(SubscribeListener listener) {
        HwLog.d(TAG, "unSubscribe");
        boolean result = false;
        if (listener == null) {
            HwLog.e(TAG, "unSubscribe get null param");
            return false;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. unSubscribe return false");
            return false;
        }
        try {
            SubscribeListenerTransport transport = this.mSubscribeListeners.remove(listener);
            if (transport != null) {
                HwLog.d(TAG, "mNearbyService.unSubscribe start");
                result = nearbyAdapter.unSubscribe(transport);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in unSubscribe" + e.getLocalizedMessage());
        }
        return result;
    }

    public boolean openNearbySocket(NearbyConfig.BusinessTypeEnum businessType, int channel, int businessId, String businessTag, NearbyDevice device, int timeout, CreateSocketListener listener) {
        HwLog.d(TAG, "openNearbySocket with channel");
        return openNearbySocket(businessType, channel, businessId, businessTag, device, timeout, listener, Looper.myLooper());
    }

    public boolean openNearbySocket(NearbyConfig.BusinessTypeEnum businessType, int businessId, String businessTag, NearbyDevice device, int timeout, CreateSocketListener listener) {
        HwLog.d(TAG, "openNearbySocket");
        return openNearbySocket(businessType, -1, businessId, businessTag, device, timeout, listener);
    }

    public boolean openNearbySocket(NearbyConfig.BusinessTypeEnum businessType, int channel, int businessId, String businessTag, NearbyDevice device, int timeout, CreateSocketListener listener, Looper looper) {
        Looper looper2;
        CreateSocketListener createSocketListener = listener;
        HwLog.d(TAG, "openNearbySocket with new looper");
        boolean result = false;
        if (businessType == null || createSocketListener == null || device == null) {
            HwLog.e(TAG, "openNearbySocket get null param");
            return false;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. openNearbySocket return false");
            return false;
        }
        if (looper == null) {
            HwLog.e(TAG, "CreateSocketListener looper can not be null");
            looper2 = mThread.getLooper();
        } else {
            looper2 = looper;
        }
        if (this.mCreateSocketListeners.get(createSocketListener) != null) {
            HwLog.d(TAG, "CreateSocketListener already registered && return");
            return false;
        }
        CreateSocketListenerTransport createSocketListenerTransport = new CreateSocketListenerTransport(this, createSocketListener, looper2);
        createSocketListenerTransport.setTimeOut(timeout);
        createSocketListenerTransport.setStartTime(System.currentTimeMillis());
        this.mCreateSocketListeners.put(createSocketListener, createSocketListenerTransport);
        try {
            HwLog.d(TAG, "mNearbyService.openNearbySocket start");
            CreateSocketListenerTransport createSocketListenerTransport2 = createSocketListenerTransport;
            try {
                result = nearbyAdapter.openNearbySocket(businessType.toNumber(), channel, businessId, businessTag, device, timeout, createSocketListenerTransport);
            } catch (RemoteException e) {
                e = e;
                HwLog.e(TAG, "error in openNearbySocket" + e.getLocalizedMessage());
                return result;
            }
        } catch (RemoteException e2) {
            e = e2;
            CreateSocketListenerTransport createSocketListenerTransport3 = createSocketListenerTransport;
            HwLog.e(TAG, "error in openNearbySocket" + e.getLocalizedMessage());
            return result;
        }
        return result;
    }

    public boolean stopOpen(CreateSocketListener listener) {
        if (this.mCreateSocketListeners.get(listener) == null) {
            HwLog.d(TAG, "CreateSocketListener have not in list. stopOpen do nothing. Return false.");
            return false;
        }
        this.mCreateSocketListeners.get(listener).cancel();
        return true;
    }

    public boolean registerSocketListener(NearbyConfig.BusinessTypeEnum businessType, int businessId, SocketListener listener) {
        HwLog.d(TAG, "registerSocketListener");
        return registerSocketListener(businessType, businessId, listener, Looper.myLooper());
    }

    public boolean registerSocketListener(NearbyConfig.BusinessTypeEnum businessType, int businessId, SocketListener listener, Looper looper) {
        HwLog.d(TAG, "registerSocketListener with new looper");
        boolean result = false;
        if (businessType == null || listener == null) {
            HwLog.e(TAG, "registerSocketListener get null param");
            return false;
        } else if (this.mSocketListeners.get(listener) != null) {
            HwLog.d(TAG, "SocketListener already registered && return");
            return true;
        } else {
            INearbyAdapter nearbyAdapter = mNearbyService;
            if (nearbyAdapter == null) {
                HwLog.e(TAG, "mNearbyService is null. registerSocketListener return false");
                return false;
            }
            if (looper == null) {
                HwLog.e(TAG, "SocketListener looper can not be null");
                looper = mThread.getLooper();
            }
            try {
                SocketListenerTransport transport = new SocketListenerTransport(listener, looper);
                HwLog.d(TAG, "mNearbyService.registerSocketListener start");
                result = nearbyAdapter.registerInternalSocketListener(businessType.toNumber(), businessId, transport);
                if (result) {
                    this.mSocketListeners.put(listener, transport);
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in registerSocketListener" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public boolean unRegisterSocketListener(SocketListener listener) {
        HwLog.d(TAG, "unRegisterSocketListener");
        boolean result = false;
        if (listener == null) {
            HwLog.e(TAG, "unRegisterSocketListener get null param");
            return false;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. unRegisterSocketListener return false");
            return false;
        }
        try {
            SocketListenerTransport transport = this.mSocketListeners.remove(listener);
            if (transport != null) {
                HwLog.d(TAG, "mNearbyService.unRegisterSocketListener start");
                result = nearbyAdapter.unRegisterInternalSocketListener(transport);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in unRegisterSocketListener" + e.getLocalizedMessage());
        }
        return result;
    }

    public boolean registerConnectionListener(Context context, NearbyConfig.BusinessTypeEnum businessType, int businessId, ConnectionListener listener) {
        HwLog.d(TAG, "registerConnectionListener");
        return registerConnectionListener(context, businessType, businessId, null, listener, Looper.myLooper());
    }

    public boolean registerConnectionListener(Context context, NearbyConfig.BusinessTypeEnum businessType, int businessId, NearbyConfiguration configuration, ConnectionListener listener, Looper looper) {
        HwLog.d(TAG, "registerConnectionListener with new looper");
        boolean result = false;
        if (businessType == null || listener == null) {
            HwLog.e(TAG, "registerConnectionListener get null param");
            return false;
        } else if (this.mConnectionListeners.get(listener) != null) {
            HwLog.d(TAG, "ConnectionListener already registered && return");
            return true;
        } else {
            INearbyAdapter nearbyAdapter = mNearbyService;
            if (nearbyAdapter == null) {
                HwLog.e(TAG, "mNearbyService is null. registerConnectionListener return false");
                return false;
            }
            if (looper == null) {
                HwLog.e(TAG, "ConnectionListener looper can not be null");
                looper = mThread.getLooper();
            }
            try {
                ConnectionListenerTransport transport = new ConnectionListenerTransport(context, businessId, listener, looper);
                HwLog.d(TAG, "mNearbyService.registerConnectionListener start ");
                result = nearbyAdapter.registerConnectionListener(businessType.toNumber(), businessId, configuration, transport);
                if (result) {
                    this.mConnectionListeners.put(listener, transport);
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in registerConnectionListener" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public boolean unRegisterConnectionListener(ConnectionListener listener) {
        HwLog.d(TAG, "unRegisterConnectionListener");
        boolean result = false;
        if (listener == null) {
            HwLog.e(TAG, "unRegisterConnectionListener get null param");
            return false;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. unRegisterConnectionListener return false");
            return false;
        }
        try {
            HwLog.d(TAG, "mNearbyService.unRegisterConnectionListener start ");
            if (this.mConnectionListeners.get(listener) == null) {
                return false;
            }
            result = nearbyAdapter.unRegisterConnectionListener(this.mConnectionListeners.get(listener));
            if (result) {
                this.mConnectionListeners.remove(listener);
            }
            return result;
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in unRegisterConnectionListener" + e.getLocalizedMessage());
        }
    }

    public boolean open(NearbyConfig.BusinessTypeEnum businessType, int businessId, NearbyDevice device, int timeoutMs) {
        return open(businessType, 0, businessId, device, timeoutMs);
    }

    public boolean open(NearbyConfig.BusinessTypeEnum businessType, int channelId, int businessId, NearbyDevice device, int timeoutMs) {
        HwLog.d(TAG, "open " + timeoutMs);
        boolean result = false;
        if (businessType == null || device == null) {
            HwLog.e(TAG, "open get null param");
            return false;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. open return false");
            return false;
        }
        try {
            HwLog.d(TAG, "mNearbyService.open start");
            result = nearbyAdapter.open(businessType.toNumber(), channelId, businessId, device, timeoutMs);
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in open" + e.getLocalizedMessage());
        }
        return result;
    }

    public int write(NearbyConfig.BusinessTypeEnum businessType, int businessId, NearbyDevice device, byte[] message) {
        HwLog.d(TAG, "write");
        int result = -1;
        if (businessType == null || device == null) {
            HwLog.e(TAG, "write get null param");
            return -1;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. write return -1");
            return -1;
        }
        try {
            HwLog.d(TAG, "mNearbyService.write start");
            result = nearbyAdapter.write(businessType.toNumber(), businessId, device, message);
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in write" + e.getLocalizedMessage());
        }
        return result;
    }

    public void close(NearbyConfig.BusinessTypeEnum businessType, int businessId, NearbyDevice device) {
        HwLog.d(TAG, "close");
        if (businessType == null || device == null) {
            HwLog.e(TAG, "close get null param");
            return;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. close return");
            return;
        }
        try {
            HwLog.d(TAG, "mNearbyService.close start");
            nearbyAdapter.close(businessType.toNumber(), businessId, device);
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in close" + e.getLocalizedMessage());
        }
    }

    public boolean findVendorDevice(int manu, int devType, DevFindListener listener, Looper looper) {
        HwLog.d(TAG, "findVendorDevice");
        boolean result = false;
        if (listener == null) {
            HwLog.e(TAG, "listen is null");
            return false;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. close return");
            return false;
        }
        if (looper == null) {
            HwLog.e(TAG, "PublishListener looper can not be null");
            looper = mThread.getLooper();
        }
        DevFindListenerTransport tansport = new DevFindListenerTransport(listener, looper);
        try {
            HwLog.d(TAG, "mNearbyService.findVendorDevice start");
            result = nearbyAdapter.findVendorDevice(manu, devType, tansport);
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in findVendorDevice" + e.getLocalizedMessage());
        }
        return result;
    }

    public boolean stopFindVendorDevice(int manu, int devType) {
        boolean result = false;
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. close return");
            return false;
        }
        try {
            HwLog.d(TAG, "mNearbyService.stopFindVendorDevice start");
            result = nearbyAdapter.stopFindVendorDevice(manu, devType);
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in findVendorDevice" + e.getLocalizedMessage());
        }
        return result;
    }

    public boolean connectVendorDevice(NearbyDevice dev, int timeout, DevConnectListener listener, Looper looper) {
        HwLog.d(TAG, "connectVendorDevice");
        boolean result = false;
        if (listener == null) {
            HwLog.e(TAG, "listen is null");
            return false;
        }
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. close return");
            return false;
        }
        if (looper == null) {
            HwLog.e(TAG, "DevConnectListener looper can not be null");
            looper = mThread.getLooper();
        }
        DevConnectListenTransport tansport = new DevConnectListenTransport(listener, looper);
        try {
            HwLog.d(TAG, "mNearbyService.connectVendorDevice start");
            result = nearbyAdapter.connectVendorDevice(dev, timeout, tansport);
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in connectVendorDevice" + e.getLocalizedMessage());
        }
        return result;
    }

    public boolean disconnectVendorDevice(NearbyDevice dev) {
        HwLog.d(TAG, "disConnectVendorDevice");
        boolean result = false;
        INearbyAdapter nearbyAdapter = mNearbyService;
        if (nearbyAdapter == null) {
            HwLog.e(TAG, "mNearbyService is null. close return");
            return false;
        }
        try {
            HwLog.d(TAG, "mNearbyService.disConnectVendorDevice start");
            result = nearbyAdapter.disconnectVendorDevice(dev);
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in disConnectVendorDevice" + e.getLocalizedMessage());
        }
        return result;
    }

    public void setNickname(String nickname) {
        HwLog.d(TAG, "setNickname");
        if (mAuthService == null) {
            HwLog.e(TAG, "mAuthService is null. unRegisterAuthentification return false");
            return;
        }
        try {
            mAuthService.setNickname(nickname);
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in setNickname" + e.getLocalizedMessage());
        }
    }

    public boolean registerAuthentification(int businessId, AuthListener listener) {
        HwLog.d(TAG, "registerAuthentification");
        return registerAuthentification(businessId, listener, Looper.myLooper());
    }

    public boolean registerAuthentification(int businessId, AuthListener listener, Looper looper) {
        HwLog.d(TAG, "registerAuthentification with new looper");
        boolean result = false;
        if (listener == null) {
            HwLog.e(TAG, "registerAuthentification get null param");
            return false;
        } else if (mAuthService == null) {
            HwLog.e(TAG, "mAuthService is null. registerAuthentification return false");
            return false;
        } else {
            if (looper == null) {
                HwLog.e(TAG, "AuthListener looper can not be null");
                looper = mThread.getLooper();
            }
            try {
                AuthListenerTransport transport = new AuthListenerTransport(this, listener, looper);
                HwLog.d(TAG, "mAuthService.registerAuthentification start ");
                result = mAuthService.registerAuthentification(businessId, transport);
                if (result) {
                    HwLog.d(TAG, "put AuthListener into map");
                    this.mMapBussinessIdListener.put(listener, Integer.valueOf(businessId));
                    this.mAuthListeners.put(listener, transport);
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in registerAuthentification" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public boolean unRegisterAuthentification(AuthListener listener) {
        HwLog.d(TAG, "unRegisterAuthentification");
        boolean result = false;
        if (listener == null) {
            HwLog.e(TAG, "unRegisterAuthentification get null param");
            return false;
        } else if (mAuthService == null) {
            HwLog.e(TAG, "mAuthService is null. unRegisterAuthentification return false");
            return false;
        } else {
            try {
                HwLog.d(TAG, "mAuthService.unRegisterAuthentification start ");
                result = mAuthService.unRegisterAuthentification(this.mAuthListeners.get(listener));
                this.mMapBussinessIdListener.remove(listener);
                this.mAuthListeners.remove(listener);
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in unRegisterAuthentification" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public long startAuthentification(NearbyDevice device, int mode) {
        HwLog.d(TAG, "getAuthentification with new looper");
        long result = -1;
        if (mAuthService == null) {
            HwLog.e(TAG, "mAuthService is null. getAuthentification return false");
            return -1;
        } else if (device == null) {
            HwLog.e(TAG, "device is null .getAuthentification return false");
            return -1;
        } else {
            byte[] bytes = new byte[16];
            new SecureRandom().nextBytes(bytes);
            byte[] encryptedAesKey = RSAUtils.encryptUsingPubKey(bytes);
            HwLog.logByteArray(TAG, bytes);
            try {
                HwLog.d(TAG, "mAuthService.getAuthentification start");
                result = mAuthService.startAuthentification(device, mode, encryptedAesKey);
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in startAuthentification" + e.getLocalizedMessage());
            }
            if (result != -1) {
                if (device.getSummary() == null) {
                    HwLog.d(TAG, "authId != -1.but device Summary is null.");
                } else {
                    this.mDevice_RSAKeys.put(device.getSummary(), bytes);
                    this.mAuthId_Device.put(Long.valueOf(result), device);
                }
            }
            return result;
        }
    }

    public boolean hasLoginHwId() {
        HwLog.d(TAG, "hasLoginHwId");
        boolean result = false;
        if (mAuthService == null) {
            HwLog.e(TAG, "mAuthService is null. hasLoginHwId return false");
            return false;
        }
        try {
            result = mAuthService.hasLoginHwId();
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in hasLoginHwId" + e.getLocalizedMessage());
        }
        return result;
    }

    public boolean setUserId(String userId) {
        HwLog.d(TAG, "setUserId");
        boolean result = false;
        if (mAuthService == null) {
            HwLog.e(TAG, "mAuthService is null. setUserId return false");
            return false;
        }
        try {
            result = mAuthService.setUserIdFromAdapter(userId);
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in setUserId" + e.getLocalizedMessage());
        }
        return result;
    }

    public void setSessionKey(long authId, byte[] sessionKey, byte[] sessionIV, byte[] rsa_bytes, NearbyDevice device) {
        HwLog.d(TAG, "setSessionKey");
        if (device != null) {
            String summara = device.getSummary();
            byte[] bytes = rsa_bytes;
            if (bytes == null) {
                if (summara != null) {
                    bytes = this.mDevice_RSAKeys.get(summara);
                } else {
                    HwLog.d(TAG, "summara = null");
                    return;
                }
            }
            if (sessionKey == null) {
                HwLog.d(TAG, "SessionKey = null");
            } else if (sessionIV == null) {
                HwLog.d(TAG, "SessionIV = null");
            } else if (bytes == null) {
                HwLog.d(TAG, "bytes = null");
            } else {
                try {
                    byte[] trueKey = AESUtils.decrypt(sessionKey, bytes, sessionIV);
                    mDeviceSum_SessionKeys.put(summara, trueKey);
                    HwLog.d(TAG, "put trueKey." + trueKey.length);
                } catch (Exception e) {
                    HwLog.e(TAG, "error in setSessionKey" + e.getLocalizedMessage());
                }
            }
        } else {
            HwLog.d(TAG, "device = null");
        }
    }

    public NearbyDevice getDevice(long authId) {
        if (this.mAuthId_Device != null) {
            return this.mAuthId_Device.get(Long.valueOf(authId));
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void removeCreateSocketListener(CreateSocketListener listener) {
        if (this.mCreateSocketListeners != null) {
            this.mCreateSocketListeners.remove(listener);
            HwLog.d(TAG, "remove CreateSocketListener. CreateSocketListenerList left length = " + this.mCreateSocketListeners.size());
        }
    }

    public static byte[] encrypt(byte[] data, NearbyDevice device) throws EncryptCfbFailException {
        HwLog.d(TAG, "encrypt");
        byte[] sessionIV = new byte[16];
        new SecureRandom().nextBytes(sessionIV);
        HwLog.d(TAG, "sessionIV.length>>>" + sessionIV.length);
        HwLog.logByteArray(TAG, sessionIV);
        if (device != null) {
            byte[] sessionKey = mDeviceSum_SessionKeys.get(device.getSummary());
            if (sessionKey != null) {
                try {
                    byte[] content = AESUtils.encrypt(data, sessionKey, sessionIV, true);
                    HwLog.logByteArray(TAG, content);
                    byte[] value = SDKDataHelper.packageTLV(57345, NearbySDKUtils.jointByteArrays(SDKDataHelper.packageTLV(57601, NearbySDKUtils.Int2Byte(0)), SDKDataHelper.packageTLV(57602, sessionIV), SDKDataHelper.packageTLV(57603, content)));
                    HwLog.logByteArray(TAG, value);
                    return value;
                } catch (Exception e) {
                    throw new EncryptCfbFailException("encrypt faild");
                }
            } else {
                throw new EncryptCfbFailException("encrypt faild sessionKey = null");
            }
        } else {
            throw new EncryptCfbFailException("encrypt faild device = null");
        }
    }

    public static byte[] decrypt(byte[] data, NearbyDevice device) throws EncryptCfbFailException {
        HwLog.d(TAG, "decrypt");
        ArrayList<SDKTlvData> params = new ArrayList<>();
        if (SDKDataHelper.parseDataToParam(data, params) == 57345) {
            int version = -1;
            byte[] sessionIV = null;
            byte[] content = null;
            int size = params.size();
            for (int i = 0; i < size; i++) {
                SDKTlvData param = params.get(i);
                switch (param.getType()) {
                    case 57601:
                        version = NearbySDKUtils.Byte2Int(param.getData());
                        HwLog.d(TAG, "version>>>" + version);
                        break;
                    case 57602:
                        sessionIV = param.getData();
                        HwLog.d(TAG, "sessionIV.length>>>" + sessionIV.length);
                        HwLog.logByteArray(TAG, sessionIV);
                        break;
                    case 57603:
                        content = param.getData();
                        HwLog.d(TAG, "content.length>>>" + content.length);
                        HwLog.logByteArray(TAG, content);
                        break;
                }
            }
            if (sessionIV == null || content == null) {
                throw new EncryptCfbFailException("parseDataToParam faild Iv is null or content is null");
            }
            HwLog.d(TAG, "version = " + version);
            if (version != 0) {
                throw new EncryptCfbFailException("In this Hotspot_version data can not be decrypt");
            } else if (device != null) {
                byte[] sessionKey = mDeviceSum_SessionKeys.get(device.getSummary());
                if (sessionKey != null) {
                    try {
                        return AESUtils.decrypt(content, sessionKey, sessionIV, true);
                    } catch (Exception e) {
                        throw new EncryptCfbFailException("decrypt faild");
                    }
                } else {
                    throw new EncryptCfbFailException("decrypt faild sessionKey = null");
                }
            } else {
                throw new EncryptCfbFailException("decrypt faild device = null");
            }
        } else {
            throw new EncryptCfbFailException("parseDataToParam did not find hotspot");
        }
    }

    public static void removeSessionkey(String summary) {
        if (summary != null) {
            mDeviceSum_SessionKeys.remove(summary);
        }
    }
}
