package com.huawei.dmsdpsdk2.publisher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdp.publishercenter.IBinderAuth;
import com.huawei.dmsdp.publishercenter.IPublisherAdapter;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.Version;

public class PublisherAdapter {
    private static final String DMSDP_PACKAGE_NAME = "com.huawei.dmsdpdevice";
    private static final Object SPUBLOCK = new Object();
    private static final String TAG = "PublisherAdapter";
    private HandlerThread mThread;
    private PublisherServiceConnection sConnection;
    private Context sContext;
    private IPublisherAdapter sPublisherService;
    private boolean sServiceBind;

    public interface PublisherAdapterCallback {
        void onAdapterGet(PublisherAdapter publisherAdapter);

        void onBinderDied();
    }

    private PublisherAdapter() {
        this.sContext = null;
        this.sPublisherService = null;
        this.sConnection = null;
        this.sServiceBind = false;
    }

    /* access modifiers changed from: private */
    public static PublisherAdapter getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /* access modifiers changed from: private */
    public static class SingletonHolder {
        private static final PublisherAdapter INSTANCE = new PublisherAdapter();

        private SingletonHolder() {
        }
    }

    public static void createInstance(Context context, PublisherAdapterCallback callback) {
        HwLog.i(TAG, "publisher service create instance");
        if (context == null || callback == null) {
            HwLog.e(TAG, "context or callback is null");
            throw new IllegalArgumentException("context or callback is null");
        }
        synchronized (SPUBLOCK) {
            if (getInstance().sServiceBind) {
                getInstance().sContext = context;
                callback.onAdapterGet(getInstance());
                return;
            }
            getInstance().sContext = context;
            bindAidlService(context, callback);
        }
    }

    public void releaseInstance() {
        HwLog.i(TAG, "release publisher service");
        synchronized (SPUBLOCK) {
            if (getInstance().sContext == null) {
                HwLog.e(TAG, "Context of Publisher Adapter is null, release failed");
            } else {
                unbindAidlService(getInstance().sContext);
            }
        }
    }

    public boolean sendMsg(int businessType, String msg) {
        IPublisherAdapter iPublisherAdapter = this.sPublisherService;
        if (iPublisherAdapter == null) {
            HwLog.e(TAG, "Publisher service is null");
            return false;
        }
        try {
            return iPublisherAdapter.sendMsg(businessType, msg);
        } catch (RemoteException e) {
            HwLog.e(TAG, "send msg failed");
            return false;
        }
    }

    public String queryMsg(int key) {
        try {
            return this.sPublisherService.queryMsg(key);
        } catch (RemoteException e) {
            HwLog.e(TAG, "send msg failed");
            return BuildConfig.FLAVOR;
        }
    }

    public boolean registerPublisherListener(int businessType, PublisherListener listener) {
        HandlerThread handlerThread;
        IPublisherAdapter iPublisherAdapter = this.sPublisherService;
        if (iPublisherAdapter == null || (handlerThread = this.mThread) == null) {
            HwLog.e(TAG, "publisher service not bind success");
            return false;
        }
        try {
            return iPublisherAdapter.registerPublisherListener(businessType, new PublisherListenerTransport(listener, handlerThread.getLooper()));
        } catch (RemoteException e) {
            HwLog.e(TAG, "register publisher listener failed");
            return false;
        }
    }

    public boolean unregisterPublisherListener(int businessType) {
        IPublisherAdapter iPublisherAdapter = this.sPublisherService;
        if (iPublisherAdapter == null) {
            HwLog.e(TAG, "publisher service not bind success");
            return false;
        }
        try {
            return iPublisherAdapter.unregisterPublisherListener(businessType);
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregister publisher listener failed");
            return false;
        }
    }

    private static void bindAidlService(Context context, PublisherAdapterCallback callback) {
        HwLog.i(TAG, "bind Publisher Service");
        synchronized (SPUBLOCK) {
            if (getInstance().sServiceBind) {
                HwLog.w(TAG, "Publisher service has been bind, no need bind again");
                return;
            }
            Intent intent = new Intent();
            intent.setAction("com.huawei.dmsdpdevice.PUBLISHER_SERVICE");
            intent.setPackage(DMSDP_PACKAGE_NAME);
            context.bindService(intent, new PublisherServiceConnection(context, callback), 65);
        }
    }

    private static void unbindAidlService(Context context) {
        HwLog.i(TAG, "unbind publisher service");
        synchronized (SPUBLOCK) {
            if (!getInstance().sServiceBind) {
                HwLog.w(TAG, "publisher service unbind already, no need unbind again");
                return;
            }
            context.unbindService(getInstance().sConnection);
            cleanServiceCache();
        }
    }

    /* access modifiers changed from: private */
    public static void cleanServiceCache() {
        getInstance().sServiceBind = false;
        getInstance().sConnection = null;
        getInstance().sPublisherService = null;
        if (getInstance().mThread != null) {
            if (Build.VERSION.SDK_INT >= 18) {
                getInstance().mThread.quitSafely();
            } else {
                getInstance().mThread.quit();
            }
            getInstance().mThread = null;
        }
    }

    /* access modifiers changed from: private */
    public static class PublisherServiceConnection implements ServiceConnection {
        private PublisherAdapterCallback callback;

        PublisherServiceConnection(Context context, PublisherAdapterCallback callback2) {
            this.callback = callback2;
            HwLog.d(PublisherAdapter.TAG, "PublisherServiceConnection ctor");
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (PublisherAdapter.SPUBLOCK) {
                HwLog.d(PublisherAdapter.TAG, "client onService connected, service: " + service);
                IPublisherAdapter sPublisherService = getPublisherService(service);
                if (sPublisherService == null) {
                    HwLog.e(PublisherAdapter.TAG, "Publisher Service bind failed");
                    return;
                }
                PublisherAdapter.getInstance().sPublisherService = sPublisherService;
                PublisherAdapter.getInstance().sConnection = this;
                PublisherAdapter.getInstance().sServiceBind = true;
                if (PublisherAdapter.getInstance().mThread == null) {
                    PublisherAdapter.getInstance().mThread = new HandlerThread("PublisherAdapter Looper");
                    PublisherAdapter.getInstance().mThread.start();
                }
                if (this.callback != null) {
                    this.callback.onAdapterGet(PublisherAdapter.getInstance());
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            synchronized (PublisherAdapter.SPUBLOCK) {
                HwLog.d(PublisherAdapter.TAG, "onService Disconnect");
                PublisherAdapter.cleanServiceCache();
                this.callback = null;
            }
        }

        private IPublisherAdapter getPublisherService(IBinder service) {
            try {
                IBinderAuth binderAuth = IBinderAuth.Stub.asInterface(service);
                HwLog.i(PublisherAdapter.TAG, "Get IBinderAuth");
                IBinder serviceBinder = binderAuth.getAuthcation(Version.VERSION);
                if (serviceBinder != null) {
                    return IPublisherAdapter.Stub.asInterface(serviceBinder);
                }
                HwLog.e(PublisherAdapter.TAG, "Authcation faild, can not bind serivce");
                return null;
            } catch (RemoteException e) {
                HwLog.e(PublisherAdapter.TAG, "error in getPublisherService " + e);
                return null;
            } catch (SecurityException e2) {
                HwLog.e(PublisherAdapter.TAG, "error in getPublisherService " + e2);
                return null;
            }
        }
    }
}
