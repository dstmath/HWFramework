package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class SmsBroadcastUndelivered {
    private static final boolean DBG = true;
    static final long DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE = 2592000000L;
    private static final String[] PDU_PENDING_MESSAGE_PROJECTION = {"pdu", "sequence", "destination_port", "date", "reference_number", "count", "address", HbpcdLookup.ID, "message_body", "display_originating_addr", "receive_time", "sub_id"};
    private static final String TAG = "SmsBroadcastUndelivered";
    private static SmsBroadcastUndelivered instance;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
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
    private int mSubId;

    private class ScanRawTableThread extends Thread {
        private final Context context;

        private ScanRawTableThread(Context context2) {
            this.context = context2;
        }

        public void run() {
            SmsBroadcastUndelivered.this.scanRawTable(this.context);
            InboundSmsHandler.cancelNewMessageNotification(this.context);
        }
    }

    private static class SmsReferenceKey {
        final String mAddress;
        final int mMessageCount;
        final String mQuery;
        final int mReferenceNumber;

        SmsReferenceKey(InboundSmsTracker tracker) {
            this.mAddress = tracker.getAddress();
            this.mReferenceNumber = tracker.getReferenceNumber();
            this.mMessageCount = tracker.getMessageCount();
            this.mQuery = tracker.getQueryForSegments();
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
            boolean z = false;
            if (!(o instanceof SmsReferenceKey)) {
                return false;
            }
            SmsReferenceKey other = (SmsReferenceKey) o;
            if (other.mAddress.equals(this.mAddress) && other.mReferenceNumber == this.mReferenceNumber && other.mMessageCount == this.mMessageCount) {
                z = true;
            }
            return z;
        }
    }

    public static void initialize(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler) {
        if (instance == null) {
            instance = new SmsBroadcastUndelivered(context, gsmInboundSmsHandler, cdmaInboundSmsHandler);
        }
        if (gsmInboundSmsHandler != null) {
            gsmInboundSmsHandler.sendMessage(6);
        }
        if (cdmaInboundSmsHandler != null) {
            cdmaInboundSmsHandler.sendMessage(6);
        }
    }

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

    /* access modifiers changed from: private */
    public void scanRawTable(Context context) {
        StringBuilder sb;
        String str;
        String str2;
        String where;
        Rlog.d(TAG, "scanning raw table for undelivered messages");
        long startTime = System.nanoTime();
        HashMap hashMap = new HashMap(4);
        HashSet hashSet = new HashSet(4);
        Cursor cursor = null;
        try {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("deleted = 0");
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                str2 = " AND sub_id=" + this.mSubId;
            } else {
                str2 = "";
            }
            sb2.append(str2);
            Rlog.d(TAG, "scanRawTable where=" + where);
            cursor = this.mResolver.query(InboundSmsHandler.sRawUri, PDU_PENDING_MESSAGE_PROJECTION, where, null, null);
            if (cursor == null) {
                Rlog.e(TAG, "error getting pending message cursor");
                if (cursor != null) {
                    cursor.close();
                }
                Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                return;
            }
            boolean isCurrentFormat3gpp2 = InboundSmsHandler.isCurrentFormat3gpp2();
            while (true) {
                boolean isCurrentFormat3gpp22 = isCurrentFormat3gpp2;
                if (!cursor.moveToNext()) {
                    break;
                }
                try {
                    InboundSmsTracker tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(cursor, isCurrentFormat3gpp22);
                    if (tracker.getMessageCount() == 1) {
                        Rlog.i(TAG, "scanRawTable: deliver single-part message");
                        broadcastSms(tracker);
                    } else {
                        Rlog.i(TAG, "scanRawTable: process multi-part message");
                        SmsReferenceKey reference = new SmsReferenceKey(tracker);
                        Integer receivedCount = (Integer) hashMap.get(reference);
                        if (receivedCount == null) {
                            hashMap.put(reference, 1);
                            if (tracker.getTimestamp() < System.currentTimeMillis() - getUndeliveredSmsExpirationTime(context)) {
                                hashSet.add(reference);
                            }
                        } else {
                            int newCount = receivedCount.intValue() + 1;
                            if (newCount == tracker.getMessageCount()) {
                                Rlog.d(TAG, "found complete multi-part message");
                                broadcastSms(tracker);
                                hashSet.remove(reference);
                            } else {
                                hashMap.put(reference, Integer.valueOf(newCount));
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Rlog.e(TAG, "error loading SmsTracker: " + e);
                }
                isCurrentFormat3gpp2 = isCurrentFormat3gpp22;
            }
            Iterator it = hashSet.iterator();
            while (it.hasNext()) {
                SmsReferenceKey message = (SmsReferenceKey) it.next();
                if (this.mResolver.delete(InboundSmsHandler.sRawUriPermanentDelete, message.getDeleteWhere(), message.getDeleteWhereArgs()) == 0) {
                    Rlog.e(TAG, "No rows were deleted from raw table!");
                } else {
                    Rlog.d(TAG, "Deleted " + rows + " rows from raw table for incomplete " + message.mMessageCount + " part message");
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            str = TAG;
            sb = new StringBuilder();
            sb.append("finished scanning raw table in ");
            sb.append((System.nanoTime() - startTime) / 1000000);
            sb.append(" ms");
            Rlog.d(str, sb.toString());
        } catch (SQLException e2) {
            Rlog.e(TAG, "error reading pending SMS messages", e2);
            if (cursor != null) {
                cursor.close();
            }
            str = TAG;
            sb = new StringBuilder();
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
            throw th;
        }
    }

    private void broadcastSms(InboundSmsTracker tracker) {
        InboundSmsHandler handler;
        if (tracker.is3gpp2()) {
            handler = this.mCdmaInboundSmsHandler;
        } else {
            handler = this.mGsmInboundSmsHandler;
        }
        if (handler != null) {
            handler.sendMessage(2, tracker);
            return;
        }
        Rlog.e(TAG, "null handler for " + tracker.getFormat() + " format, can't deliver.");
    }

    private long getUndeliveredSmsExpirationTime(Context context) {
        PersistableBundle bundle = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfigForSubId(SubscriptionManager.getDefaultSmsSubscriptionId());
        if (bundle != null) {
            return bundle.getLong("undelivered_sms_message_expiration_time", DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE);
        }
        return DEFAULT_PARTIAL_SEGMENT_EXPIRE_AGE;
    }

    public static void initialize(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler, int subId) {
        instance = new SmsBroadcastUndelivered(context, gsmInboundSmsHandler, cdmaInboundSmsHandler, subId);
        if (gsmInboundSmsHandler != null) {
            gsmInboundSmsHandler.sendMessage(6);
        }
        if (cdmaInboundSmsHandler != null) {
            cdmaInboundSmsHandler.sendMessage(6);
        }
    }

    public SmsBroadcastUndelivered(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler, int subId) {
        this.mResolver = context.getContentResolver();
        this.mGsmInboundSmsHandler = gsmInboundSmsHandler;
        this.mCdmaInboundSmsHandler = cdmaInboundSmsHandler;
        this.mSubId = subId;
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_UNLOCKED");
        context.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.CURRENT, userFilter, null, null);
    }
}
