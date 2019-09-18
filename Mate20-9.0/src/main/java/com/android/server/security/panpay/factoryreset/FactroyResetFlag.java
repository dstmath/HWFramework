package com.android.server.security.panpay.factoryreset;

import android.util.Log;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import vendor.huawei.hardware.huawei_security_vnode.V1_0.IHwSecurityVNode;

public class FactroyResetFlag {
    private static final int ACTIVE = 1;
    private static final String INSE_HIDL_SERVICE_NAME = "HwInSExtNode";
    private static final String TAG = "FactroyResetFlag";

    public static synchronized boolean isActive() {
        boolean z;
        synchronized (FactroyResetFlag.class) {
            z = true;
            if (getRstFactoryFlag() != 1) {
                z = false;
            }
        }
        return z;
    }

    public static synchronized void clear() {
        synchronized (FactroyResetFlag.class) {
            clearRstFactoryFlag();
        }
    }

    private static void clearRstFactoryFlag() {
        Log.d(TAG, "clearRstFactoryFlag start");
        try {
            IHwSecurityVNode vNode = IHwSecurityVNode.getService(INSE_HIDL_SERVICE_NAME);
            if (vNode != null) {
                Log.d(TAG, "clearRstFactoryFlag write ret : " + vNode.write(1, 0, ""));
            }
        } catch (Exception e) {
            Log.e(TAG, "Try get inse hidl deamon servcie failed when get system status sync.");
        }
    }

    private static int getRstFactoryFlag() {
        Log.d(TAG, "getRstFactoryFlag start");
        AtomicInteger ret = new AtomicInteger(0);
        try {
            IHwSecurityVNode vNode = IHwSecurityVNode.getService(INSE_HIDL_SERVICE_NAME);
            if (vNode != null) {
                vNode.read(1, 1, new IHwSecurityVNode.readCallback(ret) {
                    private final /* synthetic */ AtomicInteger f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onValues(int i, ArrayList arrayList) {
                        FactroyResetFlag.lambda$getRstFactoryFlag$0(this.f$0, i, arrayList);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Try get inse hidl deamon servcie failed when get system status sync.");
        }
        Log.d(TAG, "lyx read flag: " + ret.get());
        return ret.get();
    }

    static /* synthetic */ void lambda$getRstFactoryFlag$0(AtomicInteger ret, int i, ArrayList arrayList) {
        Log.d(TAG, "read flag: " + i);
        ret.set(i);
    }
}
