package com.huawei.android.telephony;

import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import com.huawei.android.util.NoExtAPIException;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class PhoneStateListenerEx extends PhoneStateListener {
    protected int mSubscription;

    /* renamed from: com.huawei.android.telephony.PhoneStateListenerEx.1 */
    static class AnonymousClass1 implements PrivilegedAction {
        final /* synthetic */ Field val$filed;

        AnonymousClass1(Field val$filed) {
            this.val$filed = val$filed;
        }

        public Object run() {
            this.val$filed.setAccessible(true);
            return null;
        }
    }

    /* renamed from: com.huawei.android.telephony.PhoneStateListenerEx.2 */
    static class AnonymousClass2 implements PrivilegedAction {
        final /* synthetic */ Field val$filed;

        AnonymousClass2(Field val$filed) {
            this.val$filed = val$filed;
        }

        public Object run() {
            this.val$filed.setAccessible(true);
            return null;
        }
    }

    public PhoneStateListenerEx() {
        this.mSubscription = 0;
        throw new NoExtAPIException("method not supported.");
    }

    public PhoneStateListenerEx(int subscription) {
        this.mSubscription = 0;
        this.mSubscription = subscription;
    }

    public static int getSubscription(PhoneStateListener obj) {
        try {
            Field filed = Class.forName("android.telephony.PhoneStateListener").getDeclaredField("mSubId");
            AccessController.doPrivileged(new AnonymousClass1(filed));
            return SubscriptionManager.getSlotId(filed.getInt(obj));
        } catch (RuntimeException e) {
            throw new NoExtAPIException("method not supported.");
        } catch (Exception e2) {
            throw new NoExtAPIException("method not supported.");
        }
    }

    public static void setSubscription(PhoneStateListener obj, int subscription) {
        try {
            Field filed = Class.forName("android.telephony.PhoneStateListener").getDeclaredField("mSubId");
            AccessController.doPrivileged(new AnonymousClass2(filed));
            filed.setInt(obj, subscription);
        } catch (RuntimeException e) {
            throw new NoExtAPIException("method not supported.");
        } catch (Exception e2) {
            throw new NoExtAPIException("method not supported.");
        }
    }
}
