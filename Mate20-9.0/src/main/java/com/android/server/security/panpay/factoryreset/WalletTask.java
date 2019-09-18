package com.android.server.security.panpay.factoryreset;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class WalletTask {
    private static final String TAG = "WalletTask";
    private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);
    private static final String WALLET_FACTORY_CALLBACK_PACKAGE_NAME = "com.huawei.wallet.sdk.WalletFactoryCallBack";
    private static final String WALLET_FACTORY_GET_INSTANCE_METHOD = "getInstance";
    private static final String WALLET_FACTORY_METHOD = "walletFactoryStart";
    private static final String WALLET_FACTORY_PACKAGE_NAME = "com.huawei.wallet.sdk.WalletFactory";
    private static final String WALLET_JAR_PATH = "/system/framework/walletReset.jar";
    private static final String WALLET_SO_PATH = "/system/lib64";
    private static PathClassLoader walletClassLoader = null;
    private static Class<?> walletFactoryClass = null;
    private static Object walletFactoryObject = null;
    private Handler handler = new Handler();
    /* access modifiers changed from: private */
    public final AtomicBoolean isInProcess = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public final AtomicBoolean isSucceeded = new AtomicBoolean(false);

    class WalletCallbackHandler implements InvocationHandler {
        WalletCallbackHandler() {
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("onSuccess".equals(method.getName())) {
                WalletTask.this.isInProcess.set(false);
                WalletTask.this.isSucceeded.set(true);
                Log.d(WalletTask.TAG, "wallet onSuccess");
            } else if ("onError".equals(method.getName())) {
                WalletTask.this.isInProcess.set(false);
                WalletTask.this.isSucceeded.set(false);
                Log.d(WalletTask.TAG, "wallet onError " + args[0]);
            }
            return null;
        }
    }

    WalletTask(Context context) {
        try {
            if (walletClassLoader == null) {
                walletClassLoader = new PathClassLoader(WALLET_JAR_PATH, WALLET_SO_PATH, getClass().getClassLoader());
            }
            Thread.currentThread().setContextClassLoader(walletClassLoader);
            Class<?> walletFactoryCallbackClass = walletClassLoader.loadClass(WALLET_FACTORY_CALLBACK_PACKAGE_NAME);
            if (walletFactoryClass == null) {
                walletFactoryClass = walletClassLoader.loadClass(WALLET_FACTORY_PACKAGE_NAME);
            }
            Object mCallBack = Proxy.newProxyInstance(walletClassLoader, new Class[]{walletFactoryCallbackClass}, new WalletCallbackHandler());
            walletFactoryObject = walletFactoryClass.getMethod("getInstance", new Class[]{Context.class, walletFactoryCallbackClass}).invoke(null, new Object[]{context, mCallBack});
        } catch (Throwable e) {
            walletFactoryClass = null;
            walletFactoryObject = null;
            Log.e(TAG, "open walletReset.jar faild: " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInProc() {
        return this.isInProcess.get();
    }

    /* access modifiers changed from: package-private */
    public boolean isSucceed() {
        return this.isSucceeded.get();
    }

    /* access modifiers changed from: package-private */
    public void startProcess() {
        try {
            if (walletFactoryObject == null || walletFactoryClass == null) {
                throw new Exception("null walletFactoryObject or walletFactoryClass");
            }
            walletFactoryClass.getMethod(WALLET_FACTORY_METHOD, new Class[0]).invoke(walletFactoryObject, new Object[0]);
            this.handler.postDelayed(new Runnable() {
                public final void run() {
                    WalletTask.this.isInProcess.set(false);
                }
            }, TIMEOUT);
            this.isInProcess.set(true);
            Log.d(TAG, "wallet WalletFactory started");
        } catch (Throwable e) {
            this.isSucceeded.set(true);
            this.isInProcess.set(false);
            Log.e(TAG, "get method walletFactoryStart faild: " + e.getMessage());
        }
    }
}
