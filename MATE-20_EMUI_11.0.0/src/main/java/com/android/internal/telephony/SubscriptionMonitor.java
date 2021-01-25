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
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SubscriptionMonitor {
    private static final String LOG_TAG = "SubscriptionMonitor";
    private static final int MAX_LOGLINES = 10;
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
        this.mLocalLog = new LocalLog(10);
        this.mSubscriptionsChangedListener = new IOnSubscriptionsChangedListener.Stub() {
            /* class com.android.internal.telephony.SubscriptionMonitor.AnonymousClass1 */

            public void onSubscriptionsChanged() {
                synchronized (SubscriptionMonitor.this.mLock) {
                    int newDefaultDataPhoneId = -1;
                    for (int phoneId = 0; phoneId < SubscriptionMonitor.this.mPhoneSubId.length; phoneId++) {
                        int newSubId = SubscriptionMonitor.this.mSubscriptionController.getSubIdUsingPhoneId(phoneId);
                        int oldSubId = SubscriptionMonitor.this.mPhoneSubId[phoneId];
                        if (oldSubId != newSubId) {
                            SubscriptionMonitor subscriptionMonitor = SubscriptionMonitor.this;
                            subscriptionMonitor.log("Phone[" + phoneId + "] subId changed " + oldSubId + "->" + newSubId + ", " + SubscriptionMonitor.this.mSubscriptionsChangedRegistrants[phoneId].size() + " registrants");
                            SubscriptionMonitor.this.mPhoneSubId[phoneId] = newSubId;
                            SubscriptionMonitor.this.mSubscriptionsChangedRegistrants[phoneId].notifyRegistrants();
                            if (SubscriptionMonitor.this.mDefaultDataSubId != -1) {
                                if (newSubId == SubscriptionMonitor.this.mDefaultDataSubId || oldSubId == SubscriptionMonitor.this.mDefaultDataSubId) {
                                    SubscriptionMonitor subscriptionMonitor2 = SubscriptionMonitor.this;
                                    subscriptionMonitor2.log("mDefaultDataSubId = " + SubscriptionMonitor.this.mDefaultDataSubId + ", " + SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[phoneId].size() + " registrants");
                                    SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[phoneId].notifyRegistrants();
                                }
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
            /* class com.android.internal.telephony.SubscriptionMonitor.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int i;
                int newDefaultDataSubId = SubscriptionMonitor.this.mSubscriptionController.getDefaultDataSubId();
                synchronized (SubscriptionMonitor.this.mLock) {
                    if (SubscriptionMonitor.this.mDefaultDataSubId != newDefaultDataSubId) {
                        SubscriptionMonitor.this.log("Default changed " + SubscriptionMonitor.this.mDefaultDataSubId + "->" + newDefaultDataSubId);
                        int i2 = SubscriptionMonitor.this.mDefaultDataSubId;
                        int oldDefaultDataPhoneId = SubscriptionMonitor.this.mDefaultDataPhoneId;
                        SubscriptionMonitor.this.mDefaultDataSubId = newDefaultDataSubId;
                        int newDefaultDataPhoneId = SubscriptionMonitor.this.mSubscriptionController.getPhoneId(-1);
                        int i3 = 0;
                        if (newDefaultDataSubId != -1) {
                            int phoneId = 0;
                            while (true) {
                                if (phoneId >= SubscriptionMonitor.this.mPhoneSubId.length) {
                                    break;
                                } else if (SubscriptionMonitor.this.mPhoneSubId[phoneId] == newDefaultDataSubId) {
                                    newDefaultDataPhoneId = phoneId;
                                    SubscriptionMonitor.this.log("newDefaultDataPhoneId=" + newDefaultDataPhoneId);
                                    break;
                                } else {
                                    phoneId++;
                                }
                            }
                        }
                        if (newDefaultDataPhoneId != oldDefaultDataPhoneId) {
                            SubscriptionMonitor subscriptionMonitor = SubscriptionMonitor.this;
                            StringBuilder sb = new StringBuilder();
                            sb.append("Default phoneId changed ");
                            sb.append(oldDefaultDataPhoneId);
                            sb.append("->");
                            sb.append(newDefaultDataPhoneId);
                            sb.append(", ");
                            if (SubscriptionMonitor.this.invalidPhoneId(oldDefaultDataPhoneId)) {
                                i = 0;
                            } else {
                                i = SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[oldDefaultDataPhoneId].size();
                            }
                            sb.append(i);
                            sb.append(",");
                            if (!SubscriptionMonitor.this.invalidPhoneId(newDefaultDataPhoneId)) {
                                i3 = SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[newDefaultDataPhoneId].size();
                            }
                            sb.append(i3);
                            sb.append(" registrants");
                            subscriptionMonitor.log(sb.toString());
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
        try {
            tr.addOnSubscriptionsChangedListener(context.getOpPackageName(), this.mSubscriptionsChangedListener);
        } catch (RemoteException e) {
        }
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
    }

    @VisibleForTesting
    public SubscriptionMonitor() {
        this.mLock = new Object();
        this.mLocalLog = new LocalLog(10);
        this.mSubscriptionsChangedListener = new IOnSubscriptionsChangedListener.Stub() {
            /* class com.android.internal.telephony.SubscriptionMonitor.AnonymousClass1 */

            public void onSubscriptionsChanged() {
                synchronized (SubscriptionMonitor.this.mLock) {
                    int newDefaultDataPhoneId = -1;
                    for (int phoneId = 0; phoneId < SubscriptionMonitor.this.mPhoneSubId.length; phoneId++) {
                        int newSubId = SubscriptionMonitor.this.mSubscriptionController.getSubIdUsingPhoneId(phoneId);
                        int oldSubId = SubscriptionMonitor.this.mPhoneSubId[phoneId];
                        if (oldSubId != newSubId) {
                            SubscriptionMonitor subscriptionMonitor = SubscriptionMonitor.this;
                            subscriptionMonitor.log("Phone[" + phoneId + "] subId changed " + oldSubId + "->" + newSubId + ", " + SubscriptionMonitor.this.mSubscriptionsChangedRegistrants[phoneId].size() + " registrants");
                            SubscriptionMonitor.this.mPhoneSubId[phoneId] = newSubId;
                            SubscriptionMonitor.this.mSubscriptionsChangedRegistrants[phoneId].notifyRegistrants();
                            if (SubscriptionMonitor.this.mDefaultDataSubId != -1) {
                                if (newSubId == SubscriptionMonitor.this.mDefaultDataSubId || oldSubId == SubscriptionMonitor.this.mDefaultDataSubId) {
                                    SubscriptionMonitor subscriptionMonitor2 = SubscriptionMonitor.this;
                                    subscriptionMonitor2.log("mDefaultDataSubId = " + SubscriptionMonitor.this.mDefaultDataSubId + ", " + SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[phoneId].size() + " registrants");
                                    SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[phoneId].notifyRegistrants();
                                }
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
            /* class com.android.internal.telephony.SubscriptionMonitor.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int i;
                int newDefaultDataSubId = SubscriptionMonitor.this.mSubscriptionController.getDefaultDataSubId();
                synchronized (SubscriptionMonitor.this.mLock) {
                    if (SubscriptionMonitor.this.mDefaultDataSubId != newDefaultDataSubId) {
                        SubscriptionMonitor.this.log("Default changed " + SubscriptionMonitor.this.mDefaultDataSubId + "->" + newDefaultDataSubId);
                        int i2 = SubscriptionMonitor.this.mDefaultDataSubId;
                        int oldDefaultDataPhoneId = SubscriptionMonitor.this.mDefaultDataPhoneId;
                        SubscriptionMonitor.this.mDefaultDataSubId = newDefaultDataSubId;
                        int newDefaultDataPhoneId = SubscriptionMonitor.this.mSubscriptionController.getPhoneId(-1);
                        int i3 = 0;
                        if (newDefaultDataSubId != -1) {
                            int phoneId = 0;
                            while (true) {
                                if (phoneId >= SubscriptionMonitor.this.mPhoneSubId.length) {
                                    break;
                                } else if (SubscriptionMonitor.this.mPhoneSubId[phoneId] == newDefaultDataSubId) {
                                    newDefaultDataPhoneId = phoneId;
                                    SubscriptionMonitor.this.log("newDefaultDataPhoneId=" + newDefaultDataPhoneId);
                                    break;
                                } else {
                                    phoneId++;
                                }
                            }
                        }
                        if (newDefaultDataPhoneId != oldDefaultDataPhoneId) {
                            SubscriptionMonitor subscriptionMonitor = SubscriptionMonitor.this;
                            StringBuilder sb = new StringBuilder();
                            sb.append("Default phoneId changed ");
                            sb.append(oldDefaultDataPhoneId);
                            sb.append("->");
                            sb.append(newDefaultDataPhoneId);
                            sb.append(", ");
                            if (SubscriptionMonitor.this.invalidPhoneId(oldDefaultDataPhoneId)) {
                                i = 0;
                            } else {
                                i = SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[oldDefaultDataPhoneId].size();
                            }
                            sb.append(i);
                            sb.append(",");
                            if (!SubscriptionMonitor.this.invalidPhoneId(newDefaultDataPhoneId)) {
                                i3 = SubscriptionMonitor.this.mDefaultDataSubChangedRegistrants[newDefaultDataPhoneId].size();
                            }
                            sb.append(i3);
                            sb.append(" registrants");
                            subscriptionMonitor.log(sb.toString());
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
        this.mSubscriptionsChangedRegistrants = null;
        this.mDefaultDataSubChangedRegistrants = null;
        this.mSubscriptionController = null;
        this.mContext = null;
        this.mPhoneSubId = null;
    }

    public void registerForSubscriptionChanged(int phoneId, Handler h, int what, Object o) {
        if (!invalidPhoneId(phoneId)) {
            Registrant r = new Registrant(h, what, o);
            this.mSubscriptionsChangedRegistrants[phoneId].add(r);
            r.notifyRegistrant();
            return;
        }
        throw new IllegalArgumentException("Invalid PhoneId");
    }

    public void unregisterForSubscriptionChanged(int phoneId, Handler h) {
        if (!invalidPhoneId(phoneId)) {
            this.mSubscriptionsChangedRegistrants[phoneId].remove(h);
            return;
        }
        throw new IllegalArgumentException("Invalid PhoneId");
    }

    public void registerForDefaultDataSubscriptionChanged(int phoneId, Handler h, int what, Object o) {
        if (!invalidPhoneId(phoneId)) {
            Registrant r = new Registrant(h, what, o);
            this.mDefaultDataSubChangedRegistrants[phoneId].add(r);
            r.notifyRegistrant();
            return;
        }
        throw new IllegalArgumentException("Invalid PhoneId");
    }

    public void unregisterForDefaultDataSubscriptionChanged(int phoneId, Handler h) {
        if (!invalidPhoneId(phoneId)) {
            this.mDefaultDataSubChangedRegistrants[phoneId].remove(h);
            return;
        }
        throw new IllegalArgumentException("Invalid PhoneId");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean invalidPhoneId(int phoneId) {
        if (phoneId < 0 || phoneId >= this.mPhoneSubId.length) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
