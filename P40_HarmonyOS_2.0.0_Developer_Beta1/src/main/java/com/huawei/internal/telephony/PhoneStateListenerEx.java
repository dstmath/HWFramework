package com.huawei.internal.telephony;

import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.annotation.HwSystemApi;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

@HwSystemApi
public class PhoneStateListenerEx extends PhoneStateListener {
    private static final String LOG_TAG = "PhoneStateListenerEx";
    protected int mSubscription = 0;

    public PhoneStateListenerEx() {
    }

    public PhoneStateListenerEx(int subscription) {
        super(Integer.valueOf(subscription));
        this.mSubscription = subscription;
    }

    public PhoneStateListenerEx(int subId, Looper looper) {
        super(Integer.valueOf(subId), looper);
    }

    @HwSystemApi
    public PhoneStateListenerEx(Looper looper) {
        super(looper);
    }

    public static int getSubscription(PhoneStateListener obj) {
        try {
            final Field filed = Class.forName("android.telephony.PhoneStateListener").getDeclaredField("mSubId");
            AccessController.doPrivileged(new PrivilegedAction() {
                /* class com.huawei.internal.telephony.PhoneStateListenerEx.AnonymousClass1 */

                @Override // java.security.PrivilegedAction
                public Object run() {
                    filed.setAccessible(true);
                    return null;
                }
            });
            return SubscriptionManager.getSlotIndex(((Integer) filed.get(obj)).intValue());
        } catch (RuntimeException e) {
            throw new NoExtAPIException("method not supported.");
        } catch (Exception e2) {
            throw new NoExtAPIException("method not supported.");
        }
    }

    public static void setSubscription(PhoneStateListener obj, int subscription) {
        try {
            final Field filed = Class.forName("android.telephony.PhoneStateListener").getDeclaredField("mSubId");
            AccessController.doPrivileged(new PrivilegedAction() {
                /* class com.huawei.internal.telephony.PhoneStateListenerEx.AnonymousClass2 */

                @Override // java.security.PrivilegedAction
                public Object run() {
                    filed.setAccessible(true);
                    return null;
                }
            });
            filed.set(obj, Integer.valueOf(subscription));
        } catch (RuntimeException e) {
            throw new NoExtAPIException("method not supported.");
        } catch (Exception e2) {
            throw new NoExtAPIException("method not supported.");
        }
    }
}
