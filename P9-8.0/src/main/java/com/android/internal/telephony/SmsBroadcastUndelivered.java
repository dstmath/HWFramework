package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import java.util.HashMap;
import java.util.HashSet;

public class SmsBroadcastUndelivered {
    private static final boolean DBG = true;
    static final long PARTIAL_SEGMENT_EXPIRE_AGE = 2592000000L;
    private static final String[] PDU_PENDING_MESSAGE_PROJECTION = new String[]{"pdu", "sequence", "destination_port", "date", "reference_number", "count", "address", HbpcdLookup.ID, "message_body", "display_originating_addr", "receive_time", "sub_id"};
    private static final String TAG = "SmsBroadcastUndelivered";
    private static SmsBroadcastUndelivered instance;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Rlog.d(SmsBroadcastUndelivered.TAG, "Received broadcast " + intent.getAction());
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                new ScanRawTableThread(SmsBroadcastUndelivered.this, context, null).start();
            }
        }
    };
    private final CdmaInboundSmsHandler mCdmaInboundSmsHandler;
    private final GsmInboundSmsHandler mGsmInboundSmsHandler;
    private final ContentResolver mResolver;
    private int mSubId;

    private class ScanRawTableThread extends Thread {
        private final Context context;

        /* synthetic */ ScanRawTableThread(SmsBroadcastUndelivered this$0, Context context, ScanRawTableThread -this2) {
            this(context);
        }

        private ScanRawTableThread(Context context) {
            this.context = context;
        }

        public void run() {
            SmsBroadcastUndelivered.this.scanRawTable();
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

        String[] getDeleteWhereArgs() {
            return new String[]{this.mAddress, Integer.toString(this.mReferenceNumber), Integer.toString(this.mMessageCount)};
        }

        String getDeleteWhere() {
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

    public static void initialize(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler, int subId) {
        instance = new SmsBroadcastUndelivered(context, gsmInboundSmsHandler, cdmaInboundSmsHandler, subId);
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
            new ScanRawTableThread(this, context, null).start();
            return;
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_UNLOCKED");
        context.registerReceiver(this.mBroadcastReceiver, userFilter);
    }

    public SmsBroadcastUndelivered(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler, int subId) {
        this.mResolver = context.getContentResolver();
        this.mGsmInboundSmsHandler = gsmInboundSmsHandler;
        this.mCdmaInboundSmsHandler = cdmaInboundSmsHandler;
        this.mSubId = subId;
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_UNLOCKED");
        context.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, userFilter, null, null);
    }

    private void scanRawTable() {
        Rlog.d(TAG, "scanning raw table for undelivered messages");
        long startTime = System.nanoTime();
        HashMap<SmsReferenceKey, Integer> multiPartReceivedCount = new HashMap(4);
        HashSet<SmsReferenceKey> hashSet = new HashSet(4);
        Cursor cursor = null;
        try {
            String where = "deleted = 0" + (TelephonyManager.getDefault().isMultiSimEnabled() ? " AND sub_id=" + this.mSubId : "");
            Rlog.d(TAG, "scanRawTable where=" + where);
            cursor = this.mResolver.query(InboundSmsHandler.sRawUri, PDU_PENDING_MESSAGE_PROJECTION, where, null, null);
            if (cursor == null) {
                Rlog.e(TAG, "error getting pending message cursor");
                return;
            }
            boolean isCurrentFormat3gpp2 = InboundSmsHandler.isCurrentFormat3gpp2();
            while (cursor.moveToNext()) {
                try {
                    InboundSmsTracker tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(cursor, isCurrentFormat3gpp2);
                    if (tracker.getMessageCount() == 1) {
                        Rlog.i(TAG, "scanRawTable: deliver single-part message");
                        broadcastSms(tracker);
                    } else {
                        Rlog.i(TAG, "scanRawTable: process multi-part message");
                        SmsReferenceKey smsReferenceKey = new SmsReferenceKey(tracker);
                        Integer receivedCount = (Integer) multiPartReceivedCount.get(smsReferenceKey);
                        if (receivedCount == null) {
                            multiPartReceivedCount.put(smsReferenceKey, Integer.valueOf(1));
                            if (tracker.getTimestamp() < System.currentTimeMillis() - PARTIAL_SEGMENT_EXPIRE_AGE) {
                                hashSet.add(smsReferenceKey);
                            }
                        } else {
                            int newCount = receivedCount.intValue() + 1;
                            if (newCount == tracker.getMessageCount()) {
                                Rlog.d(TAG, "found complete multi-part message");
                                broadcastSms(tracker);
                                hashSet.remove(smsReferenceKey);
                            } else {
                                multiPartReceivedCount.put(smsReferenceKey, Integer.valueOf(newCount));
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Rlog.e(TAG, "error loading SmsTracker: " + e);
                }
            }
            for (SmsReferenceKey message : hashSet) {
                int rows = this.mResolver.delete(InboundSmsHandler.sRawUriPermanentDelete, message.getDeleteWhere(), message.getDeleteWhereArgs());
                if (rows == 0) {
                    Rlog.e(TAG, "No rows were deleted from raw table!");
                } else {
                    Rlog.d(TAG, "Deleted " + rows + " rows from raw table for incomplete " + message.mMessageCount + " part message");
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
        } catch (SQLException e2) {
            Rlog.e(TAG, "error reading pending SMS messages", e2);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
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
        } else {
            Rlog.e(TAG, "null handler for " + tracker.getFormat() + " format, can't deliver.");
        }
    }
}
