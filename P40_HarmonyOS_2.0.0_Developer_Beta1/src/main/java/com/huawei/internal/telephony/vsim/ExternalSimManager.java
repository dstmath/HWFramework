package com.huawei.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ExternalSimManager {
    private static final int AUTO_RETRY_DURATION = 2000;
    private static final int DEFAULT_MAX_DATA_LENGTH = 512;
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static final int EVENT_VSIM_INDICATION = 1;
    private static final int INVALID = -1;
    private static final int MAX_VSIM_UICC_CMD_LEN = 269;
    private static final byte NO_RESPONSE_STATUS_WORD_BYTE1 = 0;
    private static final byte NO_RESPONSE_STATUS_WORD_BYTE2 = 0;
    private static final int NO_RESPONSE_TIMEOUT_DURATION = 13000;
    private static boolean PLUG_IN_AUTO_RETRY = true;
    private static final int PLUG_IN_AUTO_RETRY_TIMEOUT = 40000;
    private static final int SIM_STATE_RETRY_DURATION = 20000;
    private static final String TAG = "ExternalSimMgr";
    private static final int THREAD_SLEEP_TIME = 200;
    private static ExternalSimManager sInstance = null;
    private AtomicInteger mAtomicInteger = new AtomicInteger(0);
    private CommandsInterface[] mCi = null;
    private Context mContext = null;
    private VsimEvenHandler mEventHandler = null;
    private VsimIndEventHandler mIndHandler = null;
    private final Object mLock = new Object();
    private volatile ConcurrentHashMap<Integer, Message> mMap = new ConcurrentHashMap<>();

    public ExternalSimManager() {
        Rlog.d(TAG, "construtor 0 parameter is called - done");
    }

    private ExternalSimManager(Context context, CommandsInterface[] ci) {
        Rlog.d(TAG, "construtor 1 parameter is called - start");
        this.mContext = context;
        this.mCi = ci;
        new Thread() {
            /* class com.huawei.internal.telephony.vsim.ExternalSimManager.AnonymousClass1 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Looper.prepare();
                ExternalSimManager externalSimManager = ExternalSimManager.this;
                externalSimManager.mEventHandler = new VsimEvenHandler();
                ExternalSimManager externalSimManager2 = ExternalSimManager.this;
                externalSimManager2.mIndHandler = new VsimIndEventHandler();
                for (int i = 0; i < ExternalSimManager.this.mCi.length; i++) {
                    ExternalSimManager.this.mCi[i].registerForVsimIndication(ExternalSimManager.this.mIndHandler, 1, Integer.valueOf(i));
                }
                Looper.loop();
            }
        }.start();
        Rlog.d(TAG, "construtor is called - end");
    }

    public static ExternalSimManager make(Context context, CommandsInterface[] ci) {
        if (sInstance == null) {
            sInstance = new ExternalSimManager(context, ci);
        }
        return sInstance;
    }

    public static boolean isNonDsdaRemoteSimSupport() {
        return SystemProperties.getInt("ro.vendor.mtk_non_dsda_rsim_support", 0) == 1;
    }

    public static boolean isSupportVsimHotPlugOut() {
        for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
            TelephonyManager.getDefault();
            String capability = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.modem.vsim.capability", "0");
            if (capability != null && capability.length() > 0 && !"0".equals(capability) && (Integer.parseInt(capability) & 2) > 0) {
                return true;
            }
        }
        return false;
    }

    public static class VsimEvent {
        private static final int DEFAULT_MAX_DATA_LENGTH = 512;
        private String mChallenge;
        private byte[] mData;
        private int mDataLen;
        private int mEventMaxDataLen;
        private int mMessageId;
        private int mReadOffset;
        private int mSlotId;
        private int mTransactionId;

        public VsimEvent(int transactionId, int messageId) {
            this(transactionId, messageId, 0);
        }

        public VsimEvent(int transactionId, int messageId, int slotId) {
            this(transactionId, messageId, DEFAULT_MAX_DATA_LENGTH, slotId);
        }

        public VsimEvent(int transactionId, int messageId, int length, int slotId) {
            this.mEventMaxDataLen = DEFAULT_MAX_DATA_LENGTH;
            this.mTransactionId = transactionId;
            this.mMessageId = messageId;
            this.mSlotId = slotId;
            this.mEventMaxDataLen = length;
            this.mData = new byte[this.mEventMaxDataLen];
            this.mDataLen = 0;
            this.mReadOffset = 0;
        }

        public void resetOffset() {
            synchronized (this) {
                this.mReadOffset = 0;
            }
        }

        public int putInt(int value) {
            synchronized (this) {
                if (this.mDataLen > this.mEventMaxDataLen - 4) {
                    return -1;
                }
                for (int i = 0; i < 4; i++) {
                    this.mData[this.mDataLen] = (byte) ((value >> (i * 8)) & 255);
                    this.mDataLen++;
                }
                return 0;
            }
        }

        public int putShort(int value) {
            synchronized (this) {
                if (this.mDataLen > this.mEventMaxDataLen - 2) {
                    return -1;
                }
                for (int i = 0; i < 2; i++) {
                    this.mData[this.mDataLen] = (byte) ((value >> (i * 8)) & 255);
                    this.mDataLen++;
                }
                return 0;
            }
        }

        public int putByte(int value) {
            synchronized (this) {
                if (this.mDataLen > this.mEventMaxDataLen - 1) {
                    return -1;
                }
                this.mData[this.mDataLen] = (byte) (value & 255);
                this.mDataLen++;
                return 0;
            }
        }

        public int putString(String str, int len) {
            synchronized (this) {
                if (this.mDataLen > this.mEventMaxDataLen - len) {
                    return -1;
                }
                byte[] strBytes = str.getBytes();
                if (len < str.length()) {
                    System.arraycopy(strBytes, 0, this.mData, this.mDataLen, len);
                    this.mDataLen += len;
                } else {
                    int remain = len - str.length();
                    System.arraycopy(strBytes, 0, this.mData, this.mDataLen, str.length());
                    this.mDataLen += str.length();
                    for (int i = 0; i < remain; i++) {
                        this.mData[this.mDataLen] = 0;
                        this.mDataLen++;
                    }
                }
                return 0;
            }
        }

        public int putBytes(byte[] value) {
            synchronized (this) {
                int len = value.length;
                if (len > this.mEventMaxDataLen) {
                    return -1;
                }
                System.arraycopy(value, 0, this.mData, this.mDataLen, len);
                this.mDataLen += len;
                return 0;
            }
        }

        public byte[] getData() {
            byte[] tempData;
            synchronized (this) {
                tempData = new byte[this.mDataLen];
                System.arraycopy(this.mData, 0, tempData, 0, this.mDataLen);
            }
            return tempData;
        }

        public int getDataLen() {
            int i;
            synchronized (this) {
                i = this.mDataLen;
            }
            return i;
        }

        public byte[] getDataByReadOffest() {
            byte[] tempData;
            synchronized (this) {
                tempData = new byte[(this.mDataLen - this.mReadOffset)];
                System.arraycopy(this.mData, this.mReadOffset, tempData, 0, this.mDataLen - this.mReadOffset);
            }
            return tempData;
        }

        public int getMessageId() {
            return this.mMessageId;
        }

        public int getSlotBitMask() {
            return this.mSlotId;
        }

        public int getFirstSlotId() {
            int simCount = TelephonyManager.getDefault().getSimCount();
            if (getSlotBitMask() > (1 << (simCount - 1))) {
                Rlog.w(ExternalSimManager.TAG, "getFirstSlotId, invalid slot id: " + getSlotBitMask());
                return 0;
            }
            for (int i = 0; i < simCount; i++) {
                if ((getSlotBitMask() & (1 << i)) != 0) {
                    return i;
                }
            }
            Rlog.w(ExternalSimManager.TAG, "getFirstSlotId, invalid slot id: " + getSlotBitMask());
            return 0;
        }

        public int getTransactionId() {
            return this.mTransactionId;
        }

        public int getInt() {
            int ret = 0;
            synchronized (this) {
                if (this.mData.length >= 4) {
                    ret = ((this.mData[this.mReadOffset + 3] & 255) << 24) | ((this.mData[this.mReadOffset + 2] & 255) << 16) | ((this.mData[this.mReadOffset + 1] & 255) << 8) | (this.mData[this.mReadOffset] & 255);
                    this.mReadOffset += 4;
                }
            }
            return ret;
        }

        public int getShort() {
            int ret;
            synchronized (this) {
                ret = ((this.mData[this.mReadOffset + 1] & 255) << 8) | (this.mData[this.mReadOffset] & 255);
                this.mReadOffset += 2;
            }
            return ret;
        }

        public int getByte() {
            int ret;
            synchronized (this) {
                ret = this.mData[this.mReadOffset] & 255;
                this.mReadOffset++;
            }
            return ret;
        }

        public byte[] getBytes(int length) {
            synchronized (this) {
                if (length > this.mDataLen - this.mReadOffset) {
                    return new byte[0];
                }
                byte[] ret = new byte[length];
                for (int i = 0; i < length; i++) {
                    ret[i] = this.mData[this.mReadOffset];
                    this.mReadOffset++;
                }
                return ret;
            }
        }

        public String getString(int len) {
            byte[] buf = new byte[len];
            synchronized (this) {
                System.arraycopy(this.mData, this.mReadOffset, buf, 0, len);
                this.mReadOffset += len;
            }
            return new String(buf).trim();
        }

        public String getChallenge() {
            return this.mChallenge;
        }

        public void setChallenge(String challenge) {
            this.mChallenge = challenge;
        }

        public String toString() {
            return new String("dumpEvent: transaction_id: " + getTransactionId() + ", message_id:" + getMessageId() + ", slot_id:" + getSlotBitMask() + ", data_len:" + getDataLen() + ", event:" + ExternalSimManager.truncateString(IccUtils.bytesToHexString(getData())));
        }
    }

    /* access modifiers changed from: private */
    public static String truncateString(String original) {
        if (original == null || original.length() < 6) {
            return original;
        }
        return original.substring(0, 2) + "***" + original.substring(original.length() - 4);
    }

    public int sendVsimEvent(int slotId, int messageId, int dataLength, byte[] data, Message response) {
        if (this.mEventHandler == null) {
            Rlog.e(TAG, "sendVsimEvent handler is null, return");
            return -1;
        } else if (dataLength > MAX_VSIM_UICC_CMD_LEN || data == null || data.length > dataLength) {
            Rlog.e(TAG, "sendVsimEvent dataLength beyond 269 or null.");
            return -1;
        } else {
            VsimEvent event = new VsimEvent(this.mAtomicInteger.incrementAndGet(), messageId, dataLength, slotId);
            saveMessageIfNeed(response);
            Rlog.i(TAG, "sendVsimEvent event = " + event);
            event.putBytes(data);
            Message msg = new Message();
            msg.obj = event;
            this.mEventHandler.sendMessage(msg);
            return this.mAtomicInteger.get();
        }
    }

    public int sendVsimEvent(int slotId, int simType, int eventId, String challenge, Message response) {
        if (this.mEventHandler == null) {
            Rlog.e(TAG, "sendVsimEvent handler is null, return");
            return -1;
        }
        VsimEvent event = new VsimEvent(this.mAtomicInteger.incrementAndGet(), 3, slotId);
        saveMessageIfNeed(response);
        event.putInt(eventId);
        event.putInt(simType);
        if (challenge != null) {
            event.setChallenge(challenge);
        }
        Message msg = new Message();
        msg.obj = event;
        this.mEventHandler.sendMessage(msg);
        Rlog.i(TAG, "sendVsimEvent[" + slotId + "], event = " + event + "...");
        return this.mAtomicInteger.get();
    }

    private void saveMessageIfNeed(Message response) {
        if (response != null) {
            response.arg1 = this.mAtomicInteger.get();
            this.mMap.put(Integer.valueOf(this.mAtomicInteger.get()), response);
            Rlog.i(TAG, "saveMessageIfNeed, save message for" + response.arg1);
        }
    }

    public void handleMessageDone(int transactionId) {
        Rlog.i(TAG, "handleMessageDone, remove message for" + transactionId);
        this.mMap.remove(Integer.valueOf(transactionId));
    }

    public class VsimIndEventHandler extends Handler {
        public VsimIndEventHandler() {
        }

        /* access modifiers changed from: protected */
        public int getCiIndex(Message msg) {
            int index = 0;
            if (msg == null) {
                return 0;
            }
            if (msg.obj instanceof Integer) {
                index = ((Integer) msg.obj).intValue();
            }
            if (!(msg.obj instanceof AsyncResult)) {
                return index;
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.userObj instanceof Integer) {
                return ((Integer) ar.userObj).intValue();
            }
            return index;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int index = getCiIndex(msg);
            if (index < 0 || index >= ExternalSimManager.this.mCi.length) {
                Rlog.e(ExternalSimManager.TAG, "Invalid index : " + index + " received with event " + msg.what);
                return;
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (msg.what != 1) {
                Rlog.e(ExternalSimManager.TAG, " Unknown Event " + msg.what);
                return;
            }
            if (ExternalSimManager.ENG) {
                Rlog.d(ExternalSimManager.TAG, "Received EVENT_VSIM_INDICATION...");
            }
            if (ar.result instanceof VsimEvent) {
                VsimEvent indicationEvent = (VsimEvent) ar.result;
                dumpEvent(indicationEvent);
                Message vsimMsg = new Message();
                vsimMsg.obj = indicationEvent;
                ExternalSimManager.this.mEventHandler.sendMessage(vsimMsg);
            }
        }

        private void dumpEvent(VsimEvent event) {
            if (ExternalSimManager.ENG) {
                Rlog.d(ExternalSimManager.TAG, "dumpEvent: transaction_id: " + event.getTransactionId() + ", message_id:" + event.getMessageId() + ", slot_id:" + event.getSlotBitMask() + ", data_len:" + event.getDataLen() + ", event:" + ExternalSimManager.truncateString(IccUtils.bytesToHexString(event.getData())));
                return;
            }
            Rlog.d(ExternalSimManager.TAG, "dumpEvent: transaction_id: " + event.getTransactionId() + ", message_id:" + event.getMessageId() + ", slot_id:" + event.getSlotBitMask() + ", data_len:" + event.getDataLen());
        }
    }

    public class VsimEvenHandler extends Handler {
        private EventHandlerTread[] mEventHandlingThread = null;
        private boolean[] mIsMdWaitingResponse = null;

        public VsimEvenHandler() {
            int simCount = TelephonyManager.getDefault().getSimCount();
            this.mIsMdWaitingResponse = new boolean[simCount];
            this.mEventHandlingThread = new EventHandlerTread[simCount];
            for (int i = 0; i < simCount; i++) {
                this.mIsMdWaitingResponse[i] = false;
                this.mEventHandlingThread[i] = null;
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            VsimEvent event;
            if (msg.obj instanceof AsyncResult) {
                event = (VsimEvent) ((AsyncResult) msg.obj).userObj;
            } else {
                event = (VsimEvent) msg.obj;
            }
            int slotId = event.getFirstSlotId();
            if (slotId < 0 || slotId >= TelephonyManager.getDefault().getSimCount()) {
                Object object = msg.obj;
                if (object instanceof VsimEvent) {
                    new EventHandlerTread((VsimEvent) object).start();
                    return;
                }
                return;
            }
            while (true) {
                EventHandlerTread[] eventHandlerTreadArr = this.mEventHandlingThread;
                if (eventHandlerTreadArr[slotId] == null || !eventHandlerTreadArr[slotId].isWaiting()) {
                    break;
                }
                Rlog.d(ExternalSimManager.TAG, "handleMessage[" + slotId + "] thread running, delay 100 ms...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Rlog.e(ExternalSimManager.TAG, "occur an exception when handle message");
                }
            }
            this.mEventHandlingThread[slotId] = new EventHandlerTread(event);
            this.mEventHandlingThread[slotId].start();
        }

        public class EventHandlerTread extends Thread {
            boolean isWaiting = true;
            VsimEvent mEvent = null;

            public EventHandlerTread(VsimEvent event) {
                this.mEvent = event;
            }

            public boolean isWaiting() {
                return this.isWaiting;
            }

            public void setWaiting(boolean waiting) {
                this.isWaiting = waiting;
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Rlog.d(ExternalSimManager.TAG, "EventHandlerTread[ " + this.mEvent.getFirstSlotId() + "]: run...");
                VsimEvenHandler.this.dispatchCallback(this.mEvent);
                this.isWaiting = false;
            }
        }

        private void sendVsimNotification(int slotId, int transactionId, int eventId, int simType, Message message) {
            sendVsimNotification(slotId, transactionId, eventId, simType, null, message);
        }

        private void sendVsimNotification(int slotId, int transactionId, int eventId, int simType, String challenge, Message message) {
            boolean timeOut = true;
            ExternalSimManager.this.mCi[slotId].sendVsimNotification(transactionId, eventId, simType, challenge, message);
            Rlog.d(ExternalSimManager.TAG, "sendVsimNotification result = true, msg = " + message);
            if (message == null) {
                int timeOut2 = 0;
                boolean result = true;
                while (!result && timeOut2 < ExternalSimManager.PLUG_IN_AUTO_RETRY_TIMEOUT) {
                    try {
                        Thread.sleep(2000);
                        timeOut2 += ExternalSimManager.AUTO_RETRY_DURATION;
                    } catch (InterruptedException e) {
                        Rlog.e(ExternalSimManager.TAG, "occur an exception when send notification");
                    }
                    ExternalSimManager.this.mCi[slotId].sendVsimNotification(transactionId, eventId, simType, challenge, message);
                    timeOut2 = timeOut2;
                    result = true;
                }
                timeOut = result;
            }
            if (!timeOut) {
                Rlog.e(ExternalSimManager.TAG, "sendVsimNotification fail until 40000");
            }
        }

        private void setMdWaitingFlag(boolean isWaiting, int slotId) {
            setMdWaitingFlag(isWaiting, null, slotId);
        }

        private void setMdWaitingFlag(boolean isWaiting, VsimEvent event, int slotId) {
            Rlog.d(ExternalSimManager.TAG, "setMdWaitingFlag[" + slotId + "]: " + isWaiting);
            this.mIsMdWaitingResponse[slotId] = isWaiting;
        }

        private boolean getMdWaitingFlag(int slotId) {
            Rlog.d(ExternalSimManager.TAG, "getMdWaitingFlag[" + slotId + "]: " + this.mIsMdWaitingResponse[slotId]);
            return this.mIsMdWaitingResponse[slotId];
        }

        private void waitPlugOutDone(int slotId, int type) {
            IccCardConstants.State state;
            if (ExternalSimManager.isNonDsdaRemoteSimSupport() || ExternalSimManager.isSupportVsimHotPlugOut()) {
                SubscriptionController ctrl = SubscriptionController.getInstance();
                IccCardConstants.State state2 = IccCardConstants.State.NOT_READY;
                List<SubscriptionInfo> subInfos = null;
                int timeOut = 0;
                do {
                    try {
                        Thread.sleep(200);
                        timeOut += ExternalSimManager.THREAD_SLEEP_TIME;
                    } catch (InterruptedException e) {
                        Rlog.e(ExternalSimManager.TAG, "occur an exception when disable sim");
                    }
                    state = IccCardConstants.State.intToState(ctrl.getSimStateForSlotIndex(slotId));
                    if (type == 2) {
                        subInfos = ctrl.getSubInfoUsingSlotIndexPrivileged(slotId);
                    }
                    if ((state == IccCardConstants.State.ABSENT || state == IccCardConstants.State.NOT_READY || state == IccCardConstants.State.UNKNOWN) && subInfos == null) {
                        break;
                    }
                } while (timeOut < ExternalSimManager.SIM_STATE_RETRY_DURATION);
                if (type == 2) {
                    Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler DISABLE_EXTERNAL_SIM state: " + state);
                }
                if (type == 4) {
                    Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler REQUEST_TYPE_PLUG_IN state: " + state);
                }
            }
        }

        private void handleEventRequest(int type, VsimEvent event) {
            String str;
            int result;
            Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] start");
            int slotId = event.getFirstSlotId();
            int simType = event.getInt();
            Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler First slotId:" + slotId + ", simType:" + simType + ", id:" + event.mTransactionId + "size = " + ExternalSimManager.this.mMap.size());
            Message msg = ExternalSimManager.this.mMap.containsKey(Integer.valueOf(event.mTransactionId)) ? (Message) ExternalSimManager.this.mMap.get(Integer.valueOf(event.mTransactionId)) : null;
            String str2 = "] end";
            if (slotId < 0 || slotId >= TelephonyManager.getDefault().getSimCount()) {
                str = ExternalSimManager.TAG;
                if (0 == 0) {
                    result = -1;
                    new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, event.getSlotBitMask()).putInt(result);
                    Rlog.i(str, "VsimEvenHandler eventHandlerByType: type[" + type + str2);
                }
            } else if (type == 1) {
                str = ExternalSimManager.TAG;
                sendVsimNotification(slotId, event.mTransactionId, type, simType, msg);
            } else if (type == 2) {
                str = ExternalSimManager.TAG;
                waitPlugOutDone(slotId, 2);
                sendVsimNotification(slotId, event.mTransactionId, type, simType, msg);
            } else if (type == 3) {
                int i = event.mTransactionId;
                str = ExternalSimManager.TAG;
                sendVsimNotification(slotId, i, type, simType, msg);
            } else if (type == 4) {
                waitPlugOutDone(slotId, 4);
                sendVsimNotification(slotId, event.mTransactionId, type, simType, event.getChallenge(), msg);
                str = ExternalSimManager.TAG;
                str2 = str2;
            } else if (type == 5) {
                sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                str = ExternalSimManager.TAG;
            } else if (type != 204) {
                Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler invalid event id.");
                str = ExternalSimManager.TAG;
                result = -1;
                new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, event.getSlotBitMask()).putInt(result);
                Rlog.i(str, "VsimEvenHandler eventHandlerByType: type[" + type + str2);
            } else {
                sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + str2);
                return;
            }
            result = 0;
            new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, event.getSlotBitMask()).putInt(result);
            Rlog.i(str, "VsimEvenHandler eventHandlerByType: type[" + type + str2);
        }

        private Object getLock(int msgId) {
            return ExternalSimManager.this.mLock;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dispatchCallback(VsimEvent event) {
            synchronized (getLock(event.getMessageId())) {
                if (this.mEventHandlingThread[event.getFirstSlotId()] != null) {
                    this.mEventHandlingThread[event.getFirstSlotId()].setWaiting(false);
                }
                int msgId = event.getMessageId();
                Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler handleMessage[" + event.getFirstSlotId() + "]: msgId[" + msgId + "] start");
                if (msgId == 3) {
                    handleEventRequest(event.getInt(), event);
                } else if (msgId != 4) {
                    if (msgId != 5) {
                        switch (msgId) {
                            case ExternalSimConstants.MSG_ID_EVENT_RESPONSE /* 1003 */:
                                break;
                            case ExternalSimConstants.MSG_ID_UICC_RESET_REQUEST /* 1004 */:
                                setMdWaitingFlag(true, event, event.getFirstSlotId());
                                break;
                            case ExternalSimConstants.MSG_ID_UICC_APDU_REQUEST /* 1005 */:
                                setMdWaitingFlag(true, event, event.getFirstSlotId());
                                break;
                            default:
                                Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler handleMessage: default");
                                break;
                        }
                    } else if (getMdWaitingFlag(event.getFirstSlotId())) {
                        setMdWaitingFlag(false, event.getFirstSlotId());
                        ExternalSimManager.this.mCi[event.getFirstSlotId()].sendVsimOperation(event.getTransactionId(), event.getMessageId(), event.getInt(), event.getInt(), event.getDataByReadOffest(), (Message) null);
                    }
                } else if (getMdWaitingFlag(event.getFirstSlotId())) {
                    setMdWaitingFlag(false, event.getFirstSlotId());
                    ExternalSimManager.this.mCi[event.getFirstSlotId()].sendVsimOperation(event.getTransactionId(), event.getMessageId(), event.getInt(), event.getInt(), event.getDataByReadOffest(), (Message) null);
                }
                Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler handleMessage[" + event.getFirstSlotId() + "]: msgId[" + msgId + "] end");
            }
        }
    }
}
