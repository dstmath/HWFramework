package com.huawei.nearbysdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.nearbysdk.IAuthAdapter.Stub;
import com.huawei.nearbysdk.NearbyConfig.BusinessTypeEnum;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

public final class NearbyAdapter {
    private static final int INVALIDE_CHANNEL = -1;
    private static final int INVALIDE_TYPE_CHANNEL = -1;
    static final String TAG = "NearbyServiceJar";
    private static Boolean mAuthBound = Boolean.valueOf(false);
    private static NearbyAuthServiceConnection mAuthConnection;
    private static Object mAuthLock = new Object();
    private static IAuthAdapter mAuthService;
    private static NearbyServiceConnection mConnection;
    private static final HashMap<String, byte[]> mDeviceSum_SessionKeys = new HashMap();
    private static NearbyAdapter mNearbyAdapter;
    private static Boolean mNearbyBound = Boolean.valueOf(false);
    private static Object mNearbyLock = new Object();
    private static INearbyAdapter mNearbyService;
    private static HandlerThread mThread;
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

        public NearbyAuthServiceConnection(Context context, NAdapterGetCallback callback) {
            this.context = context;
            this.callback = callback;
            HwLog.d(NearbyAdapter.TAG, "MagiclinkAuthServiceConnection construct");
        }

        /* JADX WARNING: Missing block: B:17:0x0071, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            synchronized (this) {
                HwLog.d(NearbyAdapter.TAG, "client onAuthServiceConnected  || service " + service);
                NearbyAdapter.mAuthService = Stub.asInterface(service);
                if (NearbyAdapter.mNearbyAdapter == null) {
                    NearbyAdapter.mNearbyAdapter = new NearbyAdapter();
                }
                NearbyAdapter.mNearbyAdapter;
                NearbyAdapter.setAuthService(NearbyAdapter.mAuthService);
                NearbyAdapter.mAuthConnection = this;
                NearbyAdapter.mAuthBound = Boolean.valueOf(true);
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
                NearbyAdapter.mAuthService = null;
                NearbyAdapter.mAuthBound = Boolean.valueOf(false);
            }
        }
    }

    private static class NearbyServiceConnection implements ServiceConnection {
        private NAdapterGetCallback callback;
        private Context context;

        public NearbyServiceConnection(Context context, NAdapterGetCallback callback) {
            this.context = context;
            this.callback = callback;
            HwLog.d(NearbyAdapter.TAG, "NearbyServiceConnection construct");
        }

        /* JADX WARNING: Missing block: B:30:0x00ac, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            synchronized (this) {
                HwLog.d(NearbyAdapter.TAG, "client onServiceConnected  || service " + service);
                NearbyAdapter.mNearbyService = INearbyAdapter.Stub.asInterface(service);
                boolean hasInit = false;
                try {
                    hasInit = NearbyAdapter.mNearbyService.hasInit();
                } catch (RemoteException e) {
                    HwLog.e(NearbyAdapter.TAG, "error in onServiceConnected" + e.getLocalizedMessage());
                }
                if (!hasInit) {
                    HwLog.d(NearbyAdapter.TAG, "mNearbyService has not init. set mNearbyService = null");
                    NearbyAdapter.mNearbyService = null;
                }
                if (NearbyAdapter.mNearbyAdapter == null) {
                    NearbyAdapter.mNearbyAdapter = new NearbyAdapter();
                }
                NearbyAdapter.mNearbyAdapter;
                NearbyAdapter.setNearbySevice(NearbyAdapter.mNearbyService);
                NearbyAdapter.mConnection = this;
                NearbyAdapter.mNearbyBound = Boolean.valueOf(true);
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
                NearbyAdapter.mNearbyService = null;
                NearbyAdapter.mNearbyBound = Boolean.valueOf(false);
            }
        }
    }

    /* synthetic */ NearbyAdapter(NearbyAdapter -this0) {
        this();
    }

    private NearbyAdapter() {
        this.mPublishListeners = new HashMap();
        this.mSubscribeListeners = new HashMap();
        this.mSocketListeners = new HashMap();
        this.mConnectionListeners = new HashMap();
        this.mCreateSocketListeners = new HashMap();
        this.mAuthListeners = new HashMap();
        this.mMapBussinessIdListener = new HashMap();
        this.mDevice_RSAKeys = new HashMap();
        this.mAuthId_Device = new HashMap();
        mThread = new HandlerThread("NearbyAdapter Looper");
        mThread.start();
        HwLog.d(TAG, "NearbyAdapter init");
    }

    private static void setNearbySevice(INearbyAdapter remoteNearbySevice) {
        mNearbyService = remoteNearbySevice;
    }

    private static void setAuthService(IAuthAdapter remoteAuthSevice) {
        mAuthService = remoteAuthSevice;
    }

    private boolean hasNullService() {
        return mNearbyService == null || mAuthService == null;
    }

    /* JADX WARNING: Missing block: B:22:0x0040, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void getNearbyAdapter(Context context, NAdapterGetCallback callback) {
        synchronized (NearbyAdapter.class) {
            HwLog.d(TAG, "getNearbyAdapter");
            if (context == null) {
                HwLog.e(TAG, "context is null && return.");
            } else if (callback == null) {
                HwLog.e(TAG, "callback is null && return.");
            } else if (mNearbyAdapter != null && mNearbyBound.booleanValue() && mAuthBound.booleanValue()) {
                callback.onAdapterGet(mNearbyAdapter);
            } else {
                bindAidlService(context, callback);
            }
        }
    }

    private static void callBackNull(Context context, NAdapterGetCallback callback) {
        callback.onAdapterGet(null);
        unbindAidlService(context);
    }

    public static void bindAidlService(Context context, NAdapterGetCallback callback) {
        HwLog.d(TAG, "bindAidlService mNearbyBound = " + mNearbyBound + ";mAuthBound : " + mAuthBound);
        synchronized (mNearbyLock) {
            if (!mNearbyBound.booleanValue()) {
                Intent intent = new Intent();
                intent.setAction("com.huawei.nearby.NEARBY_SERVICE");
                intent.setPackage("com.huawei.nearby");
                try {
                    context.bindServiceAsUser(intent, new NearbyServiceConnection(context, callback), 1, UserHandle.CURRENT);
                } catch (Exception e) {
                    HwLog.e(TAG, "bindAidlService bindServiceAsUser NearbyServiceConnection ERROR:" + e.getLocalizedMessage());
                }
            }
        }
        synchronized (mAuthLock) {
            if (!mAuthBound.booleanValue()) {
                Intent intentAuth = new Intent();
                intentAuth.setAction("com.huawei.nearby.NEARBY_AUTH_SERVICE");
                intentAuth.setPackage("com.huawei.nearby");
                try {
                    context.bindServiceAsUser(intentAuth, new NearbyAuthServiceConnection(context, callback), 1, UserHandle.CURRENT);
                } catch (Exception e2) {
                    HwLog.e(TAG, "bindAidlService bindServiceAsUser NearbyAuthServiceConnection ERROR:" + e2.getLocalizedMessage());
                }
            }
        }
        return;
    }

    public static void unbindAidlService(Context context) {
        HwLog.d(TAG, "unbindAidlService mNearbyBound = " + mNearbyBound + ";mAuthBound : " + mAuthBound);
        try {
            synchronized (mNearbyLock) {
                if (mNearbyBound.booleanValue()) {
                    mNearbyService = null;
                    mNearbyAdapter = null;
                    context.unbindService(mConnection);
                    mNearbyBound = Boolean.valueOf(false);
                    if (mThread != null) {
                        mThread.quitSafely();
                        mThread = null;
                    }
                }
            }
        } catch (Exception e) {
            HwLog.e(TAG, "error in unbindAidlService mNearbyService" + e.getLocalizedMessage());
            mNearbyBound = Boolean.valueOf(false);
        }
        try {
            synchronized (mAuthLock) {
                if (mAuthBound.booleanValue()) {
                    mAuthService = null;
                    mNearbyAdapter = null;
                    context.unbindService(mAuthConnection);
                    mAuthBound = Boolean.valueOf(false);
                    if (mThread != null) {
                        mThread.quitSafely();
                        mThread = null;
                    }
                }
            }
        } catch (Exception e2) {
            HwLog.e(TAG, "error in unbindAidlService mAuthService" + e2.getLocalizedMessage());
            mAuthBound = Boolean.valueOf(false);
        }
    }

    protected void finalize() throws Throwable {
        HwLog.d(TAG, "Adapter finalize");
        super.finalize();
    }

    public boolean publish(int businessId, PublishListener listener) {
        return publish(BusinessTypeEnum.AllType, businessId, listener);
    }

    public boolean publish(BusinessTypeEnum businessType, int businessId, PublishListener listener) {
        return publish(businessType, businessId, -1, listener);
    }

    public boolean publish(BusinessTypeEnum businessType, int businessId, int typeChannel, PublishListener listener) {
        HwLog.d(TAG, "publish");
        return publish(businessType, businessId, typeChannel, listener, Looper.myLooper());
    }

    public boolean publish(BusinessTypeEnum businessType, int businessId, int typeChannel, PublishListener listener, Looper looper) {
        HwLog.d(TAG, "publish with new looper");
        boolean result = false;
        if (businessType == null || listener == null) {
            HwLog.e(TAG, "publish get null param");
            return false;
        } else if (this.mPublishListeners.get(listener) != null) {
            HwLog.d(TAG, "PublishListener already registered && return");
            return false;
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. Publish return false");
            return false;
        } else {
            if (looper == null) {
                HwLog.e(TAG, "PublishListener looper can not be null");
                looper = mThread.getLooper();
            }
            try {
                PublishListenerTransport transport = new PublishListenerTransport(listener, looper);
                HwLog.d(TAG, "mNearbyService.publish start");
                result = mNearbyService.publish(businessType.toNumber(), businessId, typeChannel, transport);
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
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. unPublish return false");
            return false;
        } else {
            try {
                PublishListenerTransport transport = (PublishListenerTransport) this.mPublishListeners.remove(listener);
                if (transport != null) {
                    HwLog.d(TAG, "mNearbyService.unPublish start");
                    result = mNearbyService.unPublish(transport);
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in unPublish" + e.getLocalizedMessage());
            }
            return result;
        }
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
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. subscribe return false");
            return false;
        } else {
            if (looper == null) {
                HwLog.e(TAG, "SubscribeListener looper can not be null");
                looper = mThread.getLooper();
            }
            try {
                SubscribeListenerTransport transport = new SubscribeListenerTransport(listener, looper);
                HwLog.d(TAG, "mNearbyService.subscribe start");
                result = mNearbyService.subscribe(allowWakeupById, businessId, transport);
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
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. unSubscribe return false");
            return false;
        } else {
            try {
                SubscribeListenerTransport transport = (SubscribeListenerTransport) this.mSubscribeListeners.remove(listener);
                if (transport != null) {
                    HwLog.d(TAG, "mNearbyService.unSubscribe start");
                    result = mNearbyService.unSubscribe(transport);
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in unSubscribe" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public boolean openNearbySocket(BusinessTypeEnum businessType, int channel, int businessId, String businessTag, NearbyDevice device, int timeout, CreateSocketListener listener) {
        HwLog.d(TAG, "openNearbySocket with channel");
        return openNearbySocket(businessType, channel, businessId, businessTag, device, timeout, listener, Looper.myLooper());
    }

    public boolean openNearbySocket(BusinessTypeEnum businessType, int businessId, String businessTag, NearbyDevice device, int timeout, CreateSocketListener listener) {
        HwLog.d(TAG, "openNearbySocket");
        return openNearbySocket(businessType, -1, businessId, businessTag, device, timeout, listener);
    }

    public boolean openNearbySocket(BusinessTypeEnum businessType, int channel, int businessId, String businessTag, NearbyDevice device, int timeout, CreateSocketListener listener, Looper looper) {
        HwLog.d(TAG, "openNearbySocket with new looper");
        boolean result = false;
        if (businessType == null || listener == null || device == null) {
            HwLog.e(TAG, "openNearbySocket get null param");
            return result;
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. openNearbySocket return false");
            return false;
        } else {
            if (looper == null) {
                HwLog.e(TAG, "CreateSocketListener looper can not be null");
                looper = mThread.getLooper();
            }
            if (this.mCreateSocketListeners.get(listener) != null) {
                HwLog.d(TAG, "CreateSocketListener already registered && return");
                return false;
            }
            CreateSocketListenerTransport createSocketListenerTransport = new CreateSocketListenerTransport(this, listener, looper);
            createSocketListenerTransport.setTimeOut(timeout);
            createSocketListenerTransport.setStartTime(System.currentTimeMillis());
            this.mCreateSocketListeners.put(listener, createSocketListenerTransport);
            try {
                HwLog.d(TAG, "mNearbyService.openNearbySocket start");
                result = mNearbyService.openNearbySocket(businessType.toNumber(), channel, businessId, businessTag, device, timeout, createSocketListenerTransport);
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in openNearbySocket" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public boolean stopOpen(CreateSocketListener listener) {
        if (this.mCreateSocketListeners.get(listener) == null) {
            HwLog.d(TAG, "CreateSocketListener have not in list. stopOpen do nothing. Return false.");
            return false;
        }
        ((CreateSocketListenerTransport) this.mCreateSocketListeners.get(listener)).cancel();
        return true;
    }

    public boolean registerSocketListener(BusinessTypeEnum businessType, int businessId, SocketListener listener) {
        HwLog.d(TAG, "registerSocketListener");
        return registerSocketListener(businessType, businessId, listener, Looper.myLooper());
    }

    public boolean registerSocketListener(BusinessTypeEnum businessType, int businessId, SocketListener listener, Looper looper) {
        HwLog.d(TAG, "registerSocketListener with new looper");
        boolean result = false;
        if (businessType == null || listener == null) {
            HwLog.e(TAG, "registerSocketListener get null param");
            return false;
        } else if (this.mSocketListeners.get(listener) != null) {
            HwLog.d(TAG, "SocketListener already registered && return");
            return true;
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. registerSocketListener return false");
            return false;
        } else {
            if (looper == null) {
                HwLog.e(TAG, "SocketListener looper can not be null");
                looper = mThread.getLooper();
            }
            try {
                SocketListenerTransport transport = new SocketListenerTransport(listener, looper);
                HwLog.d(TAG, "mNearbyService.registerSocketListener start");
                result = mNearbyService.registerInternalSocketListener(businessType.toNumber(), businessId, transport);
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
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. unRegisterSocketListener return false");
            return false;
        } else {
            try {
                SocketListenerTransport transport = (SocketListenerTransport) this.mSocketListeners.remove(listener);
                if (transport != null) {
                    HwLog.d(TAG, "mNearbyService.unRegisterSocketListener start");
                    result = mNearbyService.unRegisterInternalSocketListener(transport);
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in unRegisterSocketListener" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public boolean registerConnectionListener(Context context, BusinessTypeEnum businessType, int businessId, ConnectionListener listener) {
        HwLog.d(TAG, "registerConnectionListener");
        return registerConnectionListener(context, businessType, businessId, listener, Looper.myLooper());
    }

    public boolean registerConnectionListener(Context context, BusinessTypeEnum businessType, int businessId, ConnectionListener listener, Looper looper) {
        HwLog.d(TAG, "registerConnectionListener with new looper");
        boolean result = false;
        if (businessType == null || listener == null) {
            HwLog.e(TAG, "registerConnectionListener get null param");
            return false;
        } else if (this.mConnectionListeners.get(listener) != null) {
            HwLog.d(TAG, "ConnectionListener already registered && return");
            return true;
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. registerConnectionListener return false");
            return false;
        } else {
            if (looper == null) {
                HwLog.e(TAG, "ConnectionListener looper can not be null");
                looper = mThread.getLooper();
            }
            try {
                ConnectionListenerTransport transport = new ConnectionListenerTransport(context, businessId, listener, looper);
                HwLog.d(TAG, "mNearbyService.registerConnectionListener start ");
                result = mNearbyService.registerConnectionListener(businessType.toNumber(), businessId, transport);
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
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. unRegisterConnectionListener return false");
            return false;
        } else {
            try {
                HwLog.d(TAG, "mNearbyService.unRegisterConnectionListener start ");
                if (this.mConnectionListeners.get(listener) == null) {
                    return false;
                }
                result = mNearbyService.unRegisterConnectionListener((IInternalConnectionListener) this.mConnectionListeners.get(listener));
                if (result) {
                    this.mConnectionListeners.remove(listener);
                }
                return result;
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in unRegisterConnectionListener" + e.getLocalizedMessage());
            }
        }
    }

    public boolean open(BusinessTypeEnum businessType, int businessId, NearbyDevice device, int timeoutMs) {
        HwLog.d(TAG, "open " + timeoutMs);
        boolean result = false;
        if (businessType == null || device == null) {
            HwLog.e(TAG, "open get null param");
            return result;
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. open return false");
            return false;
        } else {
            try {
                HwLog.d(TAG, "mNearbyService.open start");
                result = mNearbyService.open(businessType.toNumber(), businessId, device, timeoutMs);
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in open" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public int write(BusinessTypeEnum businessType, int businessId, NearbyDevice device, byte[] message) {
        HwLog.d(TAG, "write");
        int result = -1;
        if (businessType == null || device == null) {
            HwLog.e(TAG, "write get null param");
            return result;
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. write return -1");
            return result;
        } else {
            try {
                HwLog.d(TAG, "mNearbyService.write start");
                result = mNearbyService.write(businessType.toNumber(), businessId, device, message);
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in write" + e.getLocalizedMessage());
            }
            return result;
        }
    }

    public void close(BusinessTypeEnum businessType, int businessId, NearbyDevice device) {
        HwLog.d(TAG, "close");
        if (businessType == null || device == null) {
            HwLog.e(TAG, "close get null param");
        } else if (mNearbyService == null) {
            HwLog.e(TAG, "mNearbyService is null. close return");
        } else {
            try {
                HwLog.d(TAG, "mNearbyService.close start");
                mNearbyService.close(businessType.toNumber(), businessId, device);
            } catch (RemoteException e) {
                HwLog.e(TAG, "error in close" + e.getLocalizedMessage());
            }
        }
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
                result = mAuthService.unRegisterAuthentification((IAuthListener) this.mAuthListeners.get(listener));
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
            return result;
        } else if (device == null) {
            HwLog.e(TAG, "device is null .getAuthentification return false");
            return result;
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
            if (rsa_bytes == null) {
                if (summara != null) {
                    bytes = (byte[]) this.mDevice_RSAKeys.get(summara);
                } else {
                    HwLog.d(TAG, "summara = null");
                    return;
                }
            }
            if (sessionKey == null) {
                HwLog.d(TAG, "SessionKey = null");
                return;
            } else if (sessionIV == null) {
                HwLog.d(TAG, "SessionIV = null");
                return;
            } else if (bytes == null) {
                HwLog.d(TAG, "bytes = null");
                return;
            } else {
                try {
                    byte[] trueKey = AESUtils.decrypt(sessionKey, bytes, sessionIV);
                    mDeviceSum_SessionKeys.put(summara, trueKey);
                    HwLog.d(TAG, "put trueKey." + trueKey.length);
                } catch (Exception e) {
                    HwLog.e(TAG, "error in setSessionKey" + e.getLocalizedMessage());
                }
                return;
            }
        }
        HwLog.d(TAG, "device = null");
    }

    public NearbyDevice getDevice(long authId) {
        if (this.mAuthId_Device != null) {
            return (NearbyDevice) this.mAuthId_Device.get(Long.valueOf(authId));
        }
        return null;
    }

    void removeCreateSocketListener(CreateSocketListener listener) {
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
            byte[] sessionKey = (byte[]) mDeviceSum_SessionKeys.get(device.getSummary());
            if (sessionKey != null) {
                try {
                    HwLog.logByteArray(TAG, AESUtils.encrypt(data, sessionKey, sessionIV, true));
                    byte[] value = SDKDataHelper.packageTLV(57345, NearbySDKUtils.jointByteArrays(SDKDataHelper.packageTLV(57601, NearbySDKUtils.Int2Byte(0)), SDKDataHelper.packageTLV(57602, sessionIV), SDKDataHelper.packageTLV(57603, content)));
                    HwLog.logByteArray(TAG, value);
                    return value;
                } catch (Exception e) {
                    throw new EncryptCfbFailException("encrypt faild");
                }
            }
            throw new EncryptCfbFailException("encrypt faild sessionKey = null");
        }
        throw new EncryptCfbFailException("encrypt faild device = null");
    }

    public static byte[] decrypt(byte[] data, NearbyDevice device) throws EncryptCfbFailException {
        HwLog.d(TAG, "decrypt");
        ArrayList<SDKTlvData> params = new ArrayList();
        if (SDKDataHelper.parseDataToParam(data, params) != 57345) {
            throw new EncryptCfbFailException("parseDataToParam did not find hotspot");
        }
        int version = -1;
        byte[] sessionIV = null;
        byte[] content = null;
        for (SDKTlvData param : params) {
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
                default:
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
            byte[] sessionKey = (byte[]) mDeviceSum_SessionKeys.get(device.getSummary());
            if (sessionKey != null) {
                try {
                    return AESUtils.decrypt(content, sessionKey, sessionIV, true);
                } catch (Exception e) {
                    throw new EncryptCfbFailException("decrypt faild");
                }
            }
            throw new EncryptCfbFailException("decrypt faild sessionKey = null");
        } else {
            throw new EncryptCfbFailException("decrypt faild device = null");
        }
    }

    public static void removeSessionkey(String summary) {
        if (summary != null) {
            mDeviceSum_SessionKeys.remove(summary);
        }
    }
}
