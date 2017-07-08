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
import android.provider.Telephony.Carriers;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import java.util.HashMap;
import java.util.HashSet;

public class SmsBroadcastUndelivered {
    private static final boolean DBG = true;
    static final long PARTIAL_SEGMENT_EXPIRE_AGE = 2592000000L;
    private static final String[] PDU_PENDING_MESSAGE_PROJECTION = null;
    private static final String TAG = "SmsBroadcastUndelivered";
    private static SmsBroadcastUndelivered instance;
    private final BroadcastReceiver mBroadcastReceiver;
    private final CdmaInboundSmsHandler mCdmaInboundSmsHandler;
    private final GsmInboundSmsHandler mGsmInboundSmsHandler;
    private final ContentResolver mResolver;
    private int mSubId;

    private class ScanRawTableThread extends Thread {
        private final Context context;

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
        final int mReferenceNumber;

        SmsReferenceKey(InboundSmsTracker tracker) {
            this.mAddress = tracker.getAddress();
            this.mReferenceNumber = tracker.getReferenceNumber();
            this.mMessageCount = tracker.getMessageCount();
        }

        String[] getDeleteWhereArgs() {
            return new String[]{this.mAddress, Integer.toString(this.mReferenceNumber), Integer.toString(this.mMessageCount)};
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
                z = SmsBroadcastUndelivered.DBG;
            }
            return z;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.SmsBroadcastUndelivered.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.SmsBroadcastUndelivered.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SmsBroadcastUndelivered.<clinit>():void");
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
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Rlog.d(SmsBroadcastUndelivered.TAG, "Received broadcast " + intent.getAction());
                if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                    new ScanRawTableThread(context, null).start();
                }
            }
        };
        this.mResolver = context.getContentResolver();
        this.mGsmInboundSmsHandler = gsmInboundSmsHandler;
        this.mCdmaInboundSmsHandler = cdmaInboundSmsHandler;
        if (((UserManager) context.getSystemService(Carriers.USER)).isUserUnlocked()) {
            new ScanRawTableThread(context, null).start();
            return;
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_UNLOCKED");
        context.registerReceiver(this.mBroadcastReceiver, userFilter);
    }

    public SmsBroadcastUndelivered(Context context, GsmInboundSmsHandler gsmInboundSmsHandler, CdmaInboundSmsHandler cdmaInboundSmsHandler, int subId) {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Rlog.d(SmsBroadcastUndelivered.TAG, "Received broadcast " + intent.getAction());
                if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                    new ScanRawTableThread(context, null).start();
                }
            }
        };
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
                    try {
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
                    } catch (SQLException e) {
                        Rlog.e(TAG, "error reading pending SMS messages", e);
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                        Rlog.d(TAG, "finished scanning raw table in " + ((System.nanoTime() - startTime) / 1000000) + " ms");
                    }
                }
            } catch (IllegalArgumentException e2) {
                Rlog.e(TAG, "error loading SmsTracker: " + e2);
            }
        }
        for (SmsReferenceKey message : hashSet) {
            int rows = this.mResolver.delete(InboundSmsHandler.sRawUriPermanentDelete, InboundSmsHandler.SELECT_BY_REFERENCE, message.getDeleteWhereArgs());
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
