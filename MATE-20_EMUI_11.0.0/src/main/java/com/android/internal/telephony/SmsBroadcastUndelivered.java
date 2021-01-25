package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class SmsBroadcastUndelivered {
    private static final boolean DBG = true;
    static final long DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE = 604800000;
    private static final String[] PDU_PENDING_MESSAGE_PROJECTION = {"pdu", "sequence", "destination_port", "date", "reference_number", "count", "address", HbpcdLookup.ID, "message_body", "display_originating_addr", "receive_time", "sub_id"};
    private static final String TAG = "SmsBroadcastUndelivered";
    private static SmsBroadcastUndelivered instance;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.SmsBroadcastUndelivered.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Rlog.d(SmsBroadcastUndelivered.TAG, "Received broadcast " + intent.getAction());
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                new ScanRawTableThread(context).start();
            }
        }
    };
    private final CdmaInboundSmsHandler mCdmaInboundSmsHandler;
    private final GsmInboundSmsHandler mGsmInboundSmsHandler;
    private final ContentResolver mResolver;

    private class ScanRawTableThread extends Thread {
        private final Context context;

        private ScanRawTableThread(Context context2) {
            this.context = context2;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            SmsBroadcastUndelivered.scanRawTable(this.context, SmsBroadcastUndelivered.this.mCdmaInboundSmsHandler, SmsBroadcastUndelivered.this.mGsmInboundSmsHandler, System.currentTimeMillis() - SmsBroadcastUndelivered.this.getUndeliveredSmsExpirationTime(this.context));
            InboundSmsHandler.cancelNewMessageNotification(this.context);
        }
    }

    public static void initialize(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        instance = new SmsBroadcastUndelivered(context, gsmInboundSmsHandler, cdmaInboundSmsHandler);
        if (gsmInboundSmsHandler != null) {
            gsmInboundSmsHandler.sendMessage(6);
        }
        if (cdmaInboundSmsHandler != null) {
            cdmaInboundSmsHandler.sendMessage(6);
        }
    }

    @UnsupportedAppUsage
    private SmsBroadcastUndelivered(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        this.mResolver = context.getContentResolver();
        this.mGsmInboundSmsHandler = gsmInboundSmsHandler;
        this.mCdmaInboundSmsHandler = cdmaInboundSmsHandler;
        if (((UserManager) context.getSystemService("user")).isUserUnlocked()) {
            new ScanRawTableThread(context).start();
            return;
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_UNLOCKED");
        context.registerReceiver(this.mBroadcastReceiver, userFilter);
    }

    /* JADX INFO: Multiple debug info for r1v9 'phoneId'  int: [D('phoneId' int), D('contentResolver' android.content.ContentResolver)] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0202  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0228  */
    static void scanRawTable(Context context, CdmaInboundSmsHandler cdmaInboundSmsHandler, GsmInboundSmsHandler gsmInboundSmsHandler, long oldMessageTimestamp) {
        Cursor cursor;
        Throwable th;
        SQLException e;
        String str;
        Iterator<SmsReferenceKey> it;
        int phoneId;
        boolean isCurrentFormat3gpp2;
        Rlog.d(TAG, "scanning raw table for undelivered messages");
        long startTime = System.nanoTime();
        ContentResolver contentResolver = context.getContentResolver();
        HashMap<SmsReferenceKey, Integer> multiPartReceivedCount = new HashMap<>(4);
        HashSet<SmsReferenceKey> oldMultiPartMessages = new HashSet<>(4);
        Cursor cursor2 = null;
        try {
            int phoneId2 = getPhoneId(gsmInboundSmsHandler, cdmaInboundSmsHandler);
            StringBuilder sb = new StringBuilder();
            sb.append("deleted = 0");
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                try {
                    str = " AND sub_id=" + phoneId2;
                } catch (SQLException e2) {
                    e = e2;
                } catch (Throwable th2) {
                    th = th2;
                    cursor = null;
                    if (cursor != null) {
                    }
                    Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                    throw th;
                }
            } else {
                str = PhoneConfigurationManager.SSSS;
            }
            sb.append(str);
            int phoneId3 = phoneId2;
            ContentResolver contentResolver2 = contentResolver;
            try {
                cursor = contentResolver.query(InboundSmsHandler.sRawUri, PDU_PENDING_MESSAGE_PROJECTION, sb.toString(), null, null);
                if (cursor == null) {
                    try {
                        Rlog.e(TAG, "error getting pending message cursor");
                        if (cursor != null) {
                            cursor.close();
                        }
                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                    } catch (SQLException e3) {
                        e = e3;
                        cursor2 = cursor;
                        try {
                            Rlog.e(TAG, "error reading pending SMS messages", e);
                            if (cursor2 != null) {
                                cursor2.close();
                            }
                            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        } catch (Throwable th3) {
                            th = th3;
                            cursor = cursor2;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        if (cursor != null) {
                            cursor.close();
                        }
                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        throw th;
                    }
                } else {
                    boolean isCurrentFormat3gpp22 = InboundSmsHandler.isCurrentFormat3gpp2();
                    while (cursor.moveToNext()) {
                        try {
                            InboundSmsTracker tracker = TelephonyComponentFactory.getInstance().inject(InboundSmsTracker.class.getName()).makeInboundSmsTracker(cursor, isCurrentFormat3gpp22);
                            if (tracker.getMessageCount() == 1) {
                                broadcastSms(tracker, cdmaInboundSmsHandler, gsmInboundSmsHandler);
                                isCurrentFormat3gpp2 = isCurrentFormat3gpp22;
                            } else {
                                Rlog.i(TAG, "scanRawTable: process multi-part message");
                                SmsReferenceKey reference = new SmsReferenceKey(tracker);
                                Integer receivedCount = multiPartReceivedCount.get(reference);
                                if (receivedCount == null) {
                                    multiPartReceivedCount.put(reference, 1);
                                    if (tracker.getTimestamp() < oldMessageTimestamp) {
                                        oldMultiPartMessages.add(reference);
                                        isCurrentFormat3gpp2 = isCurrentFormat3gpp22;
                                    } else {
                                        isCurrentFormat3gpp2 = isCurrentFormat3gpp22;
                                    }
                                } else {
                                    int newCount = receivedCount.intValue() + 1;
                                    isCurrentFormat3gpp2 = isCurrentFormat3gpp22;
                                    if (newCount == tracker.getMessageCount()) {
                                        Rlog.d(TAG, "found complete multi-part message");
                                        broadcastSms(tracker, cdmaInboundSmsHandler, gsmInboundSmsHandler);
                                        oldMultiPartMessages.remove(reference);
                                    } else {
                                        multiPartReceivedCount.put(reference, Integer.valueOf(newCount));
                                    }
                                }
                            }
                            isCurrentFormat3gpp22 = isCurrentFormat3gpp2;
                        } catch (IllegalArgumentException e4) {
                            Rlog.e(TAG, "error loading SmsTracker: " + e4);
                            isCurrentFormat3gpp22 = isCurrentFormat3gpp22;
                        }
                    }
                    Iterator<SmsReferenceKey> it2 = oldMultiPartMessages.iterator();
                    while (it2.hasNext()) {
                        SmsReferenceKey message = it2.next();
                        try {
                            int rows = contentResolver2.delete(InboundSmsHandler.sRawUriPermanentDelete, message.getDeleteWhere(), message.getDeleteWhereArgs());
                            if (rows == 0) {
                                try {
                                    Rlog.e(TAG, "No rows were deleted from raw table!");
                                } catch (SQLException e5) {
                                    e = e5;
                                    cursor2 = cursor;
                                } catch (Throwable th5) {
                                    th = th5;
                                    if (cursor != null) {
                                    }
                                    Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                                    throw th;
                                }
                            } else {
                                Rlog.d(TAG, "Deleted " + rows + " rows from raw table for incomplete " + message.mMessageCount + " part message");
                            }
                            if (rows > 0) {
                                it = it2;
                                contentResolver2 = contentResolver2;
                                phoneId = phoneId3;
                                TelephonyMetrics.getInstance().writeDroppedIncomingMultipartSms(phoneId, message.mFormat, rows, message.mMessageCount);
                            } else {
                                it = it2;
                                contentResolver2 = contentResolver2;
                                phoneId = phoneId3;
                            }
                            phoneId3 = phoneId;
                            it2 = it;
                        } catch (SQLException e6) {
                            e = e6;
                            cursor2 = cursor;
                            Rlog.e(TAG, "error reading pending SMS messages", e);
                            if (cursor2 != null) {
                            }
                            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                        } catch (Throwable th6) {
                            th = th6;
                            if (cursor != null) {
                            }
                            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                            throw th;
                        }
                    }
                    cursor.close();
                    Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                }
            } catch (SQLException e7) {
                e = e7;
                Rlog.e(TAG, "error reading pending SMS messages", e);
                if (cursor2 != null) {
                }
                Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
            }
        } catch (SQLException e8) {
            e = e8;
            Rlog.e(TAG, "error reading pending SMS messages", e);
            if (cursor2 != null) {
            }
            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
        } catch (Throwable th7) {
            th = th7;
            cursor = null;
            if (cursor != null) {
            }
            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
            throw th;
        }
    }

    private static int getPhoneId(GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        if (gsmInboundSmsHandler != null) {
            return gsmInboundSmsHandler.getPhone().getPhoneId();
        }
        if (cdmaInboundSmsHandler != null) {
            return cdmaInboundSmsHandler.getPhone().getPhoneId();
        }
        return -1;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: com.android.internal.telephony.gsm.GsmInboundSmsHandler */
    /* JADX WARN: Multi-variable type inference failed */
    private static void broadcastSms(InboundSmsTracker tracker, CdmaInboundSmsHandler cdmaInboundSmsHandler, GsmInboundSmsHandler gsmInboundSmsHandler) {
        InboundSmsHandler handler;
        if (tracker.is3gpp2()) {
            handler = cdmaInboundSmsHandler;
        } else {
            handler = gsmInboundSmsHandler;
        }
        if (handler != null) {
            handler.sendMessage(2, tracker);
            return;
        }
        Rlog.e(TAG, "null handler for " + tracker.getFormat() + " format, can't deliver.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getUndeliveredSmsExpirationTime(Context context) {
        PersistableBundle bundle = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfigForSubId(SubscriptionManager.getDefaultSmsSubscriptionId());
        if (bundle != null) {
            return bundle.getLong("undelivered_sms_message_expiration_time", DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE);
        }
        return DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE;
    }

    /* access modifiers changed from: private */
    public static class SmsReferenceKey {
        final String mAddress;
        final String mFormat;
        final int mMessageCount;
        final String mQuery;
        final int mReferenceNumber;

        SmsReferenceKey(InboundSmsTracker tracker) {
            this.mAddress = tracker.getAddress();
            this.mReferenceNumber = tracker.getReferenceNumber();
            this.mMessageCount = tracker.getMessageCount();
            this.mQuery = tracker.getQueryForSegments();
            this.mFormat = tracker.getFormat();
        }

        /* access modifiers changed from: package-private */
        public String[] getDeleteWhereArgs() {
            return new String[]{this.mAddress, Integer.toString(this.mReferenceNumber), Integer.toString(this.mMessageCount)};
        }

        /* access modifiers changed from: package-private */
        public String getDeleteWhere() {
            return this.mQuery;
        }

        public int hashCode() {
            return (((this.mReferenceNumber * 31) + this.mMessageCount) * 31) + this.mAddress.hashCode();
        }

        public boolean equals(Object o) {
            if (!(o instanceof SmsReferenceKey)) {
                return false;
            }
            SmsReferenceKey other = (SmsReferenceKey) o;
            if (other.mAddress.equals(this.mAddress) && other.mReferenceNumber == this.mReferenceNumber && other.mMessageCount == this.mMessageCount) {
                return true;
            }
            return false;
        }
    }
}
