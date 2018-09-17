package com.huawei.android.telephony;

import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import com.huawei.android.util.NoExtAPIException;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class PhoneStateListenerEx extends PhoneStateListener {
    protected int mSubscription = 0;

    public PhoneStateListenerEx() {
        throw new NoExtAPIException("method not supported.");
    }

    public PhoneStateListenerEx(int subscription) {
        this.mSubscription = subscription;
    }

    public static int getSubscription(PhoneStateListener obj) {
        try {
            final Field filed = Class.forName("android.telephony.PhoneStateListener").getDeclaredField("mSubId");
            AccessController.doPrivileged(new PrivilegedAction() {
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
