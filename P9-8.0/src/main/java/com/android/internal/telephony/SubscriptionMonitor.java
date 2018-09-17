package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.util.LocalLog;
import com.android.internal.telephony.IOnSubscriptionsChangedListener.Stub;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SubscriptionMonitor {
    private static final String LOG_TAG = "SubscriptionMonitor";
    private static final int MAX_LOGLINES = 100;
    private static final boolean VDBG = true;
    private final Context mContext;
    private int mDefaultDataPhoneId;
    private final RegistrantList[] mDefaultDataSubChangedRegistrants;
    private int mDefaultDataSubId;
    private final BroadcastReceiver mDefaultDataSubscriptionChangedReceiver;
    private final LocalLog mLocalLog;
    private final Object mLock;
    private final int[] mPhoneSubId;
    private final SubscriptionController mSubscriptionController;
    private final IOnSubscriptionsChangedListener mSubscriptionsChangedListener;
    private final RegistrantList[] mSubscriptionsChangedRegistrants;

    public SubscriptionMonitor(ITelephonyRegistry tr, Context context, SubscriptionController subscriptionController, int numPhones) {
        this.mLock = new Object();
        this.mLocalLog = new LocalLog(100);
        this.mSubscriptionsChangedListener = new Stub() {
            public void onSubscriptionsChanged() {
                synchronized (SubscriptionMonitor.this.mLock) {
                    int newDefaultDataPhoneId = -1;
                    for (int phoneId = 0; phoneId < SubscriptionMonitor.this.mPhoneSubId.length; phoneId++) {
                        int newSubId = SubscriptionMonitor.this.mSubscriptionController.getSubIdUsingPhoneId(phoneId);
                        int oldSubId = SubscriptionMonitor.this.mPhoneSubId[phoneId];
                        if (oldSubId != newSubId) {
                            SubscriptionMonitor.this.log("Phone[" + phoneId + "] subId changed " + oldSubId + "->" + newSubId + ", " + SubscriptionMonitor.this.mSubscriptionsChangedRegistrants[phoneId].size() + " registrants");
                            SubscriptionMonitor.this.mPhoneSubId[phoneId] = newSubId;
                            SubscriptionMonitor.this.mSubscriptionsChangedRegistrants[phoneId].notifyRegistrants();
                            if (SubscriptionMonitor.this.mDefaultDataSubId == -1) {
                            } else if (newSubId == SubscriptionMonitor.this.mDefaultDataSubId || oldSubId == SubscriptionMonitor.this.mDefaultDataSubId) {
                                SubscriptionMonitor.this.log("mDefaultDataSubId = " + SubscriptionMonitor.this.mDefaultDataSubId + ", " + SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[phoneId].size() + " registrants");
                                SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[phoneId].notifyRegistrants();
                            }
                        }
                        if (newSubId == SubscriptionMonitor.this.mDefaultDataSubId) {
                            newDefaultDataPhoneId = phoneId;
                        }
                    }
                    SubscriptionMonitor.this.mDefaultDataPhoneId = newDefaultDataPhoneId;
                }
            }
        };
        this.mDefaultDataSubscriptionChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int i = 0;
                int newDefaultDataSubId = SubscriptionMonitor.this.mSubscriptionController.getDefaultDataSubId();
                synchronized (SubscriptionMonitor.this.mLock) {
                    if (SubscriptionMonitor.this.mDefaultDataSubId != newDefaultDataSubId) {
                        SubscriptionMonitor.this.log("Default changed " + SubscriptionMonitor.this.mDefaultDataSubId + "->" + newDefaultDataSubId);
                        int oldDefaultDataSubId = SubscriptionMonitor.this.mDefaultDataSubId;
                        int oldDefaultDataPhoneId = SubscriptionMonitor.this.mDefaultDataPhoneId;
                        SubscriptionMonitor.this.mDefaultDataSubId = newDefaultDataSubId;
                        int newDefaultDataPhoneId = SubscriptionMonitor.this.mSubscriptionController.getPhoneId(-1);
                        if (newDefaultDataSubId != -1) {
                            for (int phoneId = 0; phoneId < SubscriptionMonitor.this.mPhoneSubId.length; phoneId++) {
                                if (SubscriptionMonitor.this.mPhoneSubId[phoneId] == newDefaultDataSubId) {
                                    newDefaultDataPhoneId = phoneId;
                                    SubscriptionMonitor.this.log("newDefaultDataPhoneId=" + newDefaultDataPhoneId);
                                    break;
                                }
                            }
                        }
                        if (newDefaultDataPhoneId != oldDefaultDataPhoneId) {
                            int i2;
                            SubscriptionMonitor subscriptionMonitor = SubscriptionMonitor.this;
                            StringBuilder append = new StringBuilder().append("Default phoneId changed ").append(oldDefaultDataPhoneId).append("->").append(newDefaultDataPhoneId).append(", ");
                            if (SubscriptionMonitor.this.invalidPhoneId(oldDefaultDataPhoneId)) {
                                i2 = 0;
                            } else {
                                i2 = SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[oldDefaultDataPhoneId].size();
                            }
                            StringBuilder append2 = append.append(i2).append(",");
                            if (!SubscriptionMonitor.this.invalidPhoneId(newDefaultDataPhoneId)) {
                                i = SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[newDefaultDataPhoneId].size();
                            }
                            subscriptionMonitor.log(append2.append(i).append(" registrants").toString());
                            SubscriptionMonitor.this.mDefaultDataPhoneId = newDefaultDataPhoneId;
                            if (!SubscriptionMonitor.this.invalidPhoneId(oldDefaultDataPhoneId)) {
                                SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[oldDefaultDataPhoneId].notifyRegistrants();
                            }
                            if (!SubscriptionMonitor.this.invalidPhoneId(newDefaultDataPhoneId)) {
                                SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[newDefaultDataPhoneId].notifyRegistrants();
                            }
                        }
                    }
                }
            }
        };
        this.mSubscriptionController = subscriptionController;
        this.mContext = context;
        this.mSubscriptionsChangedRegistrants = new RegistrantList[numPhones];
        this.mDefaultDataSubChangedRegistrants = new RegistrantList[numPhones];
        this.mPhoneSubId = new int[numPhones];
        this.mDefaultDataSubId = this.mSubscriptionController.getDefaultDataSubId();
        this.mDefaultDataPhoneId = this.mSubscriptionController.getPhoneId(this.mDefaultDataSubId);
        for (int phoneId = 0; phoneId < numPhones; phoneId++) {
            this.mSubscriptionsChangedRegistrants[phoneId] = new RegistrantList();
            this.mDefaultDataSubChangedRegistrants[phoneId] = new RegistrantList();
            this.mPhoneSubId[phoneId] = this.mSubscriptionController.getSubIdUsingPhoneId(phoneId);
        }
        this.mContext.registerReceiver(this.mDefaultDataSubscriptionChangedReceiver, new IntentFilter("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED"));
        try {
            tr.addOnSubscriptionsChangedListener(LOG_TAG, this.mSubscriptionsChangedListener);
        } catch (RemoteException e) {
        }
    }

    public SubscriptionMonitor() {
        this.mLock = new Object();
        this.mLocalLog = new LocalLog(100);
        this.mSubscriptionsChangedListener = /* anonymous class already generated */;
        this.mDefaultDataSubscriptionChangedReceiver = /* anonymous class already generated */;
        this.mSubscriptionsChangedRegistrants = null;
        this.mDefaultDataSubChangedRegistrants = null;
        this.mSubscriptionController = null;
        this.mContext = null;
        this.mPhoneSubId = null;
    }

    public void registerForSubscriptionChanged(int phoneId, Handler h, int what, Object o) {
        if (invalidPhoneId(phoneId)) {
            throw new IllegalArgumentException("Invalid PhoneId");
        }
        Registrant r = new Registrant(h, what, o);
        this.mSubscriptionsChangedRegistrants[phoneId].add(r);
        r.notifyRegistrant();
    }

    public void unregisterForSubscriptionChanged(int phoneId, Handler h) {
        if (invalidPhoneId(phoneId)) {
            throw new IllegalArgumentException("Invalid PhoneId");
        }
        this.mSubscriptionsChangedRegistrants[phoneId].remove(h);
    }

    public void registerForDefaultDataSubscriptionChanged(int phoneId, Handler h, int what, Object o) {
        if (invalidPhoneId(phoneId)) {
            throw new IllegalArgumentException("Invalid PhoneId");
        }
        Registrant r = new Registrant(h, what, o);
        this.mDefaultDataSubChangedRegistrants[phoneId].add(r);
        r.notifyRegistrant();
    }

    public void unregisterForDefaultDataSubscriptionChanged(int phoneId, Handler h) {
        if (invalidPhoneId(phoneId)) {
            throw new IllegalArgumentException("Invalid PhoneId");
        }
        this.mDefaultDataSubChangedRegistrants[phoneId].remove(h);
    }

    private boolean invalidPhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mPhoneSubId.length) {
            return true;
        }
        return false;
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
        this.mLocalLog.log(s);
    }

    public void dump(FileDescriptor fd, PrintWriter printWriter, String[] args) {
        synchronized (this.mLock) {
            this.mLocalLog.dump(fd, printWriter, args);
        }
    }
}
