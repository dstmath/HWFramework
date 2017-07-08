package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.net.ConnectivityManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.Telephony.Carriers;
import android.telephony.CellInfo;
import android.telephony.ModemActivityInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Jlog;
import android.util.SparseArray;
import android.view.Display;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.cdma.CdmaInformationRecords;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaDisplayInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaLineControlInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaNumberInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaRedirectingNumberInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaSignalInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaT53AudioControlInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaT53ClirInfoRec;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.dataconnection.DataCallResponse;
import com.android.internal.telephony.dataconnection.DataProfile;
import com.android.internal.telephony.dataconnection.DcFailCause;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.android.internal.telephony.gsm.SsData;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import com.huawei.internal.telephony.HwRadarUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RIL extends AbstractRIL implements CommandsInterface {
    private static final int CDMA_BROADCAST_SMS_NO_OF_SERVICE_CATEGORIES = 31;
    private static final int CDMA_BSI_NO_OF_INTS_STRUCT = 3;
    private static final int DEFAULT_ACK_WAKE_LOCK_TIMEOUT_MS = 200;
    private static final int DEFAULT_BLOCKING_MESSAGE_RESPONSE_TIMEOUT_MS = 2000;
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT_MS = 60000;
    static final int EVENT_ACK_WAKE_LOCK_TIMEOUT = 4;
    static final int EVENT_BLOCKING_RESPONSE_TIMEOUT = 5;
    static final int EVENT_SEND = 1;
    static final int EVENT_SEND_ACK = 3;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 2;
    public static final int FOR_ACK_WAKELOCK = 1;
    public static final int FOR_WAKELOCK = 0;
    private static final int INT_SIZE = 4;
    public static final int INVALID_WAKELOCK = -1;
    public static final String OEM_IDENTIFIER = "QOEMHOOK";
    static final int RADIO_SCREEN_OFF = 0;
    static final int RADIO_SCREEN_ON = 1;
    static final int RADIO_SCREEN_UNSET = -1;
    static final int RESPONSE_SOLICITED = 0;
    static final int RESPONSE_SOLICITED_ACK = 2;
    static final int RESPONSE_SOLICITED_ACK_EXP = 3;
    static final int RESPONSE_UNSOLICITED = 1;
    static final int RESPONSE_UNSOLICITED_ACK_EXP = 4;
    static final String RILJ_ACK_WAKELOCK_NAME = "RILJ_ACK_WL";
    static final boolean RILJ_LOGD = true;
    static final boolean RILJ_LOGV = false;
    static final String RILJ_LOG_TAG = "RILJ";
    static final int RIL_MAX_COMMAND_BYTES = 8192;
    static final int SOCKET_CLOSE_WAIT_MILLIS = 500;
    static final String[] SOCKET_NAME_RIL = null;
    static final int SOCKET_OPEN_RETRY_MILLIS = 4000;
    static final int SOCKET_OPEN_WAIT_MILLIS = 600;
    private final String PROP_LTE_ENABLED;
    final WakeLock mAckWakeLock;
    final int mAckWakeLockTimeout;
    volatile int mAckWlSequenceNum;
    private final BroadcastReceiver mBatteryStateListener;
    Display mDefaultDisplay;
    int mDefaultDisplayState;
    private final DisplayListener mDisplayListener;
    private TelephonyEventLog mEventLog;
    int mHeaderSize;
    private Integer mInstanceId;
    boolean mIsDevicePlugged;
    Object[] mLastNITZTimeInfo;
    protected Object mPendingRilSocketLock;
    protected boolean mRadioAvailable;
    int mRadioScreenState;
    RILReceiver mReceiver;
    Thread mReceiverThread;
    SparseArray<RILRequest> mRequestList;
    protected Message mResultMessage;
    protected boolean mRilSocketMapEnable;
    protected int[] mRilSocketMaps;
    RILSender mSender;
    HandlerThread mSenderThread;
    LocalSocket mSocket;
    AtomicBoolean mTestingEmergencyCall;
    final WakeLock mWakeLock;
    int mWakeLockCount;
    final int mWakeLockTimeout;
    volatile int mWlSequenceNum;
    String sRILClassname;
    protected boolean shouldBreakRilSocket;

    class RILReceiver implements Runnable {
        byte[] buffer;

        RILReceiver() {
            this.buffer = new byte[RIL.RIL_MAX_COMMAND_BYTES];
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            int retryCount = RIL.RESPONSE_SOLICITED;
            String rilSocket = "rild";
            while (true) {
                int isSlotsSwitched;
                boolean isHisiPlt;
                LocalSocket s;
                int length;
                InputStream is;
                LocalSocket localSocket = null;
                if (RIL.this.mInstanceId != null) {
                    if (RIL.this.mInstanceId.intValue() != 0) {
                        rilSocket = RIL.SOCKET_NAME_RIL[RIL.this.mInstanceId.intValue()];
                        if (!HwModemCapability.isCapabilitySupport(16)) {
                            if (RIL.this.isPlatformTwoModems()) {
                                if (RIL.this.mRilSocketMapEnable) {
                                    if (RIL.this.mInstanceId == null) {
                                        RIL.this.mInstanceId = Integer.valueOf(RIL.RESPONSE_SOLICITED);
                                    }
                                    if (RIL.this.mInstanceId.intValue() < RIL.RESPONSE_SOLICITED_ACK) {
                                        if (RIL.this.mRilSocketMaps[RIL.this.mInstanceId.intValue()] < RIL.RESPONSE_SOLICITED_ACK) {
                                            rilSocket = RIL.SOCKET_NAME_RIL[RIL.this.mRilSocketMaps[RIL.this.mInstanceId.intValue()]];
                                        }
                                    }
                                    synchronized (RIL.this.mPendingRilSocketLock) {
                                        try {
                                            Rlog.d(RIL.RILJ_LOG_TAG, "[2Cards]begin to wait for mPendingRilSocketLock!");
                                            if (RIL.this.mRilSocketMaps[RIL.this.mInstanceId.intValue()] == RIL.RESPONSE_SOLICITED_ACK) {
                                                RIL.this.sendResultMessage();
                                                RIL.this.mPendingRilSocketLock.wait();
                                            }
                                        } catch (InterruptedException e) {
                                            Rlog.e(RIL.RILJ_LOG_TAG, "wait for mPendingRilSocketLock exception!");
                                        }
                                        try {
                                        } catch (InterruptedException e2) {
                                            Rlog.e(RIL.RILJ_LOG_TAG, "sleep interrupted exception!");
                                        } catch (Throwable th) {
                                            Throwable tr = th;
                                        }
                                    }
                                    Thread.sleep(600);
                                }
                            }
                            isSlotsSwitched = System.getInt(RIL.this.mContext.getContentResolver(), "switch_dual_card_slots", RIL.RESPONSE_SOLICITED);
                            isHisiPlt = "HwHisiRIL".equals(RIL.this.sRILClassname);
                            Rlog.d(RIL.RILJ_LOG_TAG, "bHisiPlt = " + isHisiPlt);
                            if (RIL.this.mInstanceId != null) {
                                if (RIL.this.mInstanceId.intValue() != 0) {
                                    rilSocket = RIL.this.mInstanceId.intValue() != RIL.RESPONSE_UNSOLICITED ? (isSlotsSwitched == RIL.RESPONSE_UNSOLICITED || !isHisiPlt) ? RIL.SOCKET_NAME_RIL[RIL.RESPONSE_UNSOLICITED] : RIL.SOCKET_NAME_RIL[RIL.RESPONSE_SOLICITED] : RIL.SOCKET_NAME_RIL[RIL.this.mInstanceId.intValue()];
                                    Rlog.d(RIL.RILJ_LOG_TAG, "isSlotsSwitched = " + isSlotsSwitched + ", rilSocket = " + rilSocket + ", mInstanceId = " + RIL.this.mInstanceId);
                                }
                            }
                            rilSocket = (isSlotsSwitched == RIL.RESPONSE_UNSOLICITED || !isHisiPlt) ? RIL.SOCKET_NAME_RIL[RIL.RESPONSE_SOLICITED] : RIL.SOCKET_NAME_RIL[RIL.RESPONSE_UNSOLICITED];
                            Rlog.d(RIL.RILJ_LOG_TAG, "isSlotsSwitched = " + isSlotsSwitched + ", rilSocket = " + rilSocket + ", mInstanceId = " + RIL.this.mInstanceId);
                        }
                        s = new LocalSocket();
                        s.connect(new LocalSocketAddress(rilSocket, Namespace.RESERVED));
                        retryCount = RIL.RESPONSE_SOLICITED;
                        RIL.this.mSocket = s;
                        Rlog.i(RIL.RILJ_LOG_TAG, "(" + RIL.this.mInstanceId + ") Connected to '" + rilSocket + "' socket");
                        if (RIL.this.isPlatformTwoModems()) {
                            RIL.this.mRadioAvailable = RIL.RILJ_LOGD;
                            RIL.this.sendResultMessage();
                        }
                        length = RIL.RESPONSE_SOLICITED;
                        try {
                            is = RIL.this.mSocket.getInputStream();
                            while (true) {
                                if (RIL.this.isPlatformTwoModems()) {
                                    if (RIL.this.shouldBreakRilSocket) {
                                        break;
                                    }
                                }
                                length = RIL.readRilMessage(is, this.buffer);
                                if (length >= 0) {
                                    Parcel p = Parcel.obtain();
                                    p.unmarshall(this.buffer, RIL.RESPONSE_SOLICITED, length);
                                    p.setDataPosition(RIL.RESPONSE_SOLICITED);
                                    RIL.this.processResponse(p);
                                    p.recycle();
                                }
                                break;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e3) {
                            }
                            Rlog.i(RIL.RILJ_LOG_TAG, "(" + RIL.this.mInstanceId + ")after wait shouldBreakRilSocket, do it");
                        } catch (IOException ex) {
                            Rlog.i(RIL.RILJ_LOG_TAG, "'" + rilSocket + "' socket closed", ex);
                        } catch (Throwable th2) {
                            tr = th2;
                        }
                        break;
                        Rlog.i(RIL.RILJ_LOG_TAG, "(" + RIL.this.mInstanceId + ") Disconnected from '" + rilSocket + "' socket");
                        if (RIL.this.isPlatformTwoModems()) {
                            RIL.this.mRadioAvailable = RIL.RILJ_LOGV;
                        }
                        RIL.this.setRadioState(RadioState.RADIO_UNAVAILABLE);
                        try {
                            RIL.this.mSocket.close();
                        } catch (IOException e4) {
                        }
                        RIL.this.mSocket = null;
                        RILRequest.resetSerial();
                        RIL.this.clearRequestList(RIL.RESPONSE_UNSOLICITED, RIL.RILJ_LOGV);
                        if (RIL.this.isPlatformTwoModems()) {
                            if (RIL.this.shouldBreakRilSocket) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e5) {
                                }
                                RIL.this.shouldBreakRilSocket = RIL.RILJ_LOGV;
                                Rlog.i(RIL.RILJ_LOG_TAG, "wait for 500ms!");
                            }
                        } else {
                            localSocket = s;
                        }
                    }
                }
                rilSocket = RIL.SOCKET_NAME_RIL[RIL.RESPONSE_SOLICITED];
                if (HwModemCapability.isCapabilitySupport(16)) {
                    if (RIL.this.isPlatformTwoModems()) {
                        if (RIL.this.mRilSocketMapEnable) {
                            if (RIL.this.mInstanceId == null) {
                                RIL.this.mInstanceId = Integer.valueOf(RIL.RESPONSE_SOLICITED);
                            }
                            if (RIL.this.mInstanceId.intValue() < RIL.RESPONSE_SOLICITED_ACK) {
                                if (RIL.this.mRilSocketMaps[RIL.this.mInstanceId.intValue()] < RIL.RESPONSE_SOLICITED_ACK) {
                                    rilSocket = RIL.SOCKET_NAME_RIL[RIL.this.mRilSocketMaps[RIL.this.mInstanceId.intValue()]];
                                }
                            }
                            synchronized (RIL.this.mPendingRilSocketLock) {
                                Rlog.d(RIL.RILJ_LOG_TAG, "[2Cards]begin to wait for mPendingRilSocketLock!");
                                if (RIL.this.mRilSocketMaps[RIL.this.mInstanceId.intValue()] == RIL.RESPONSE_SOLICITED_ACK) {
                                    RIL.this.sendResultMessage();
                                    RIL.this.mPendingRilSocketLock.wait();
                                }
                            }
                            Thread.sleep(600);
                        }
                    }
                    isSlotsSwitched = System.getInt(RIL.this.mContext.getContentResolver(), "switch_dual_card_slots", RIL.RESPONSE_SOLICITED);
                    isHisiPlt = "HwHisiRIL".equals(RIL.this.sRILClassname);
                    Rlog.d(RIL.RILJ_LOG_TAG, "bHisiPlt = " + isHisiPlt);
                    if (RIL.this.mInstanceId != null) {
                        if (RIL.this.mInstanceId.intValue() != 0) {
                            if (RIL.this.mInstanceId.intValue() != RIL.RESPONSE_UNSOLICITED) {
                            }
                            Rlog.d(RIL.RILJ_LOG_TAG, "isSlotsSwitched = " + isSlotsSwitched + ", rilSocket = " + rilSocket + ", mInstanceId = " + RIL.this.mInstanceId);
                        }
                    }
                    if (isSlotsSwitched == RIL.RESPONSE_UNSOLICITED) {
                    }
                    Rlog.d(RIL.RILJ_LOG_TAG, "isSlotsSwitched = " + isSlotsSwitched + ", rilSocket = " + rilSocket + ", mInstanceId = " + RIL.this.mInstanceId);
                }
                try {
                    s = new LocalSocket();
                    try {
                        s.connect(new LocalSocketAddress(rilSocket, Namespace.RESERVED));
                        retryCount = RIL.RESPONSE_SOLICITED;
                        RIL.this.mSocket = s;
                        Rlog.i(RIL.RILJ_LOG_TAG, "(" + RIL.this.mInstanceId + ") Connected to '" + rilSocket + "' socket");
                        if (RIL.this.isPlatformTwoModems()) {
                            RIL.this.mRadioAvailable = RIL.RILJ_LOGD;
                            RIL.this.sendResultMessage();
                        }
                        length = RIL.RESPONSE_SOLICITED;
                        is = RIL.this.mSocket.getInputStream();
                        while (true) {
                            if (RIL.this.isPlatformTwoModems()) {
                                if (RIL.this.shouldBreakRilSocket) {
                                    break;
                                    Thread.sleep(500);
                                    Rlog.i(RIL.RILJ_LOG_TAG, "(" + RIL.this.mInstanceId + ")after wait shouldBreakRilSocket, do it");
                                    break;
                                    Rlog.i(RIL.RILJ_LOG_TAG, "(" + RIL.this.mInstanceId + ") Disconnected from '" + rilSocket + "' socket");
                                    if (RIL.this.isPlatformTwoModems()) {
                                        RIL.this.mRadioAvailable = RIL.RILJ_LOGV;
                                    }
                                    RIL.this.setRadioState(RadioState.RADIO_UNAVAILABLE);
                                    RIL.this.mSocket.close();
                                    RIL.this.mSocket = null;
                                    RILRequest.resetSerial();
                                    RIL.this.clearRequestList(RIL.RESPONSE_UNSOLICITED, RIL.RILJ_LOGV);
                                    if (RIL.this.isPlatformTwoModems()) {
                                        localSocket = s;
                                    } else {
                                        if (RIL.this.shouldBreakRilSocket) {
                                        } else {
                                            Thread.sleep(500);
                                            RIL.this.shouldBreakRilSocket = RIL.RILJ_LOGV;
                                            Rlog.i(RIL.RILJ_LOG_TAG, "wait for 500ms!");
                                        }
                                    }
                                }
                            }
                            length = RIL.readRilMessage(is, this.buffer);
                            if (length >= 0) {
                                Parcel p2 = Parcel.obtain();
                                p2.unmarshall(this.buffer, RIL.RESPONSE_SOLICITED, length);
                                p2.setDataPosition(RIL.RESPONSE_SOLICITED);
                                RIL.this.processResponse(p2);
                                p2.recycle();
                            }
                            break;
                            Rlog.i(RIL.RILJ_LOG_TAG, "(" + RIL.this.mInstanceId + ") Disconnected from '" + rilSocket + "' socket");
                            if (RIL.this.isPlatformTwoModems()) {
                                RIL.this.mRadioAvailable = RIL.RILJ_LOGV;
                            }
                            RIL.this.setRadioState(RadioState.RADIO_UNAVAILABLE);
                            RIL.this.mSocket.close();
                            RIL.this.mSocket = null;
                            RILRequest.resetSerial();
                            RIL.this.clearRequestList(RIL.RESPONSE_UNSOLICITED, RIL.RILJ_LOGV);
                            if (RIL.this.isPlatformTwoModems()) {
                                if (RIL.this.shouldBreakRilSocket) {
                                    Thread.sleep(500);
                                    RIL.this.shouldBreakRilSocket = RIL.RILJ_LOGV;
                                    Rlog.i(RIL.RILJ_LOG_TAG, "wait for 500ms!");
                                }
                            } else {
                                localSocket = s;
                            }
                        }
                    } catch (IOException e6) {
                        localSocket = s;
                        if (localSocket != null) {
                            try {
                                localSocket.close();
                            } catch (IOException e7) {
                            }
                        }
                        if (retryCount != 8) {
                            Rlog.e(RIL.RILJ_LOG_TAG, "Couldn't find '" + rilSocket + "' socket after " + retryCount + " times, continuing to retry silently");
                        } else if (retryCount >= 0 && retryCount < 8) {
                            Rlog.i(RIL.RILJ_LOG_TAG, "Couldn't find '" + rilSocket + "' socket; retrying after timeout");
                        }
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e8) {
                        }
                        retryCount += RIL.RESPONSE_UNSOLICITED;
                    }
                } catch (IOException e9) {
                    if (localSocket != null) {
                        localSocket.close();
                    }
                    if (retryCount != 8) {
                        Rlog.i(RIL.RILJ_LOG_TAG, "Couldn't find '" + rilSocket + "' socket; retrying after timeout");
                    } else {
                        Rlog.e(RIL.RILJ_LOG_TAG, "Couldn't find '" + rilSocket + "' socket after " + retryCount + " times, continuing to retry silently");
                    }
                    Thread.sleep(4000);
                    retryCount += RIL.RESPONSE_UNSOLICITED;
                }
            }
        }
    }

    class RILSender extends Handler implements Runnable {
        byte[] dataLength;

        public RILSender(Looper looper) {
            super(looper);
            this.dataLength = new byte[RIL.RESPONSE_UNSOLICITED_ACK_EXP];
        }

        public void run() {
        }

        public void handleMessage(Message msg) {
            RILRequest rr = msg.obj;
            switch (msg.what) {
                case RIL.RESPONSE_UNSOLICITED /*1*/:
                case RIL.RESPONSE_SOLICITED_ACK_EXP /*3*/:
                    try {
                        LocalSocket s = RIL.this.mSocket;
                        if (s == null) {
                            rr.onError(RIL.RESPONSE_UNSOLICITED, null);
                            RIL.this.decrementWakeLock(rr);
                            rr.release();
                            return;
                        }
                        if (msg.what != RIL.RESPONSE_SOLICITED_ACK_EXP) {
                            synchronized (RIL.this.mRequestList) {
                                RIL.this.mRequestList.append(rr.mSerial, rr);
                                break;
                            }
                        }
                        byte[] data = rr.mParcel.marshall();
                        rr.mParcel.recycle();
                        rr.mParcel = null;
                        if (data.length > RIL.RIL_MAX_COMMAND_BYTES) {
                            throw new RuntimeException("Parcel larger than max bytes allowed! " + data.length);
                        }
                        byte[] bArr = this.dataLength;
                        this.dataLength[RIL.RESPONSE_UNSOLICITED] = (byte) 0;
                        bArr[RIL.RESPONSE_SOLICITED] = (byte) 0;
                        this.dataLength[RIL.RESPONSE_SOLICITED_ACK] = (byte) ((data.length >> 8) & PduHeaders.STORE_STATUS_ERROR_END);
                        this.dataLength[RIL.RESPONSE_SOLICITED_ACK_EXP] = (byte) (data.length & PduHeaders.STORE_STATUS_ERROR_END);
                        s.getOutputStream().write(this.dataLength);
                        s.getOutputStream().write(data);
                        if (msg.what == RIL.RESPONSE_SOLICITED_ACK_EXP) {
                            rr.release();
                            return;
                        }
                    } catch (IOException ex) {
                        Rlog.e(RIL.RILJ_LOG_TAG, "IOException", ex);
                        if (RIL.this.findAndRemoveRequestFromList(rr.mSerial) != null) {
                            rr.onError(RIL.RESPONSE_UNSOLICITED, null);
                            RIL.this.decrementWakeLock(rr);
                            rr.release();
                            return;
                        }
                    } catch (RuntimeException exc) {
                        Rlog.e(RIL.RILJ_LOG_TAG, "Uncaught exception ", exc);
                        if (RIL.this.findAndRemoveRequestFromList(rr.mSerial) != null) {
                            rr.onError(RIL.RESPONSE_SOLICITED_ACK, null);
                            RIL.this.decrementWakeLock(rr);
                            rr.release();
                            return;
                        }
                    }
                    break;
                case RIL.RESPONSE_SOLICITED_ACK /*2*/:
                    synchronized (RIL.this.mRequestList) {
                        if (msg.arg1 == RIL.this.mWlSequenceNum && RIL.this.clearWakeLock(RIL.RESPONSE_SOLICITED)) {
                            int count = RIL.this.mRequestList.size();
                            Rlog.d(RIL.RILJ_LOG_TAG, "WAKE_LOCK_TIMEOUT  mRequestList=" + count);
                            for (int i = RIL.RESPONSE_SOLICITED; i < count; i += RIL.RESPONSE_UNSOLICITED) {
                                rr = (RILRequest) RIL.this.mRequestList.valueAt(i);
                                Rlog.d(RIL.RILJ_LOG_TAG, i + ": [" + rr.mSerial + "] " + RIL.requestToString(rr.mRequest));
                            }
                        }
                        break;
                    }
                    break;
                case RIL.RESPONSE_UNSOLICITED_ACK_EXP /*4*/:
                    if (msg.arg1 == RIL.this.mAckWlSequenceNum && RIL.this.clearWakeLock(RIL.RESPONSE_UNSOLICITED)) {
                        Rlog.d(RIL.RILJ_LOG_TAG, "ACK_WAKE_LOCK_TIMEOUT");
                        break;
                    }
                case RIL.EVENT_BLOCKING_RESPONSE_TIMEOUT /*5*/:
                    rr = RIL.this.findAndRemoveRequestFromList(msg.arg1);
                    if (rr != null) {
                        if (rr.mResult != null) {
                            AsyncResult.forMessage(rr.mResult, RIL.getResponseForTimedOutRILRequest(rr), null);
                            rr.mResult.sendToTarget();
                            RIL.this.mEventLog.writeOnRilTimeoutResponse(rr.mSerial, rr.mRequest);
                        }
                        RIL.this.decrementWakeLock(rr);
                        rr.release();
                        break;
                    }
                    break;
            }
        }
    }

    public static final class UnsolOemHookBuffer {
        private byte[] mData;
        private int mRilInstance;

        public UnsolOemHookBuffer(int rilInstance, byte[] data) {
            this.mRilInstance = rilInstance;
            if (data != null) {
                this.mData = new byte[data.length];
                System.arraycopy(data, RIL.RESPONSE_SOLICITED, this.mData, RIL.RESPONSE_SOLICITED, data.length);
                return;
            }
            this.mData = null;
        }

        public int getRilInstance() {
            return this.mRilInstance;
        }

        public byte[] getUnsolOemHookBuffer() {
            if (this.mData == null) {
                return null;
            }
            byte[] Data = new byte[this.mData.length];
            System.arraycopy(this.mData, RIL.RESPONSE_SOLICITED, Data, RIL.RESPONSE_SOLICITED, this.mData.length);
            return Data;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.RIL.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.RIL.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.<clinit>():void");
    }

    private static Object getResponseForTimedOutRILRequest(RILRequest rr) {
        if (rr == null) {
            return null;
        }
        Object timeoutResponse = null;
        switch (rr.mRequest) {
            case PduPart.P_DIFFERENCES /*135*/:
                timeoutResponse = new ModemActivityInfo(0, RESPONSE_SOLICITED, RESPONSE_SOLICITED, new int[EVENT_BLOCKING_RESPONSE_TIMEOUT], RESPONSE_SOLICITED, RESPONSE_SOLICITED);
                break;
        }
        return timeoutResponse;
    }

    private static int readRilMessage(InputStream is, byte[] buffer) throws IOException {
        int offset = RESPONSE_SOLICITED;
        int remaining = RESPONSE_UNSOLICITED_ACK_EXP;
        do {
            int countRead = is.read(buffer, offset, remaining);
            if (countRead < 0) {
                Rlog.e(RILJ_LOG_TAG, "Hit EOS reading message length");
                return RADIO_SCREEN_UNSET;
            }
            offset += countRead;
            remaining -= countRead;
        } while (remaining > 0);
        int messageLength = ((((buffer[RESPONSE_SOLICITED] & PduHeaders.STORE_STATUS_ERROR_END) << 24) | ((buffer[RESPONSE_UNSOLICITED] & PduHeaders.STORE_STATUS_ERROR_END) << 16)) | ((buffer[RESPONSE_SOLICITED_ACK] & PduHeaders.STORE_STATUS_ERROR_END) << 8)) | (buffer[RESPONSE_SOLICITED_ACK_EXP] & PduHeaders.STORE_STATUS_ERROR_END);
        offset = RESPONSE_SOLICITED;
        remaining = messageLength;
        do {
            countRead = is.read(buffer, offset, remaining);
            if (countRead < 0) {
                Rlog.e(RILJ_LOG_TAG, "Hit EOS reading message.  messageLength=" + messageLength + " remaining=" + remaining);
                return RADIO_SCREEN_UNSET;
            }
            offset += countRead;
            remaining -= countRead;
        } while (remaining > 0);
        return messageLength;
    }

    public RIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    public RIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context);
        this.sRILClassname = SystemProperties.get("ro.telephony.ril_class", "HwHisiRIL").trim();
        this.mHeaderSize = OEM_IDENTIFIER.length() + 8;
        this.shouldBreakRilSocket = RILJ_LOGV;
        this.mRilSocketMapEnable = RILJ_LOGV;
        this.mResultMessage = null;
        this.mRilSocketMaps = new int[RESPONSE_SOLICITED_ACK_EXP];
        this.mPendingRilSocketLock = new Object();
        this.mRadioAvailable = RILJ_LOGV;
        this.mDefaultDisplayState = RESPONSE_SOLICITED;
        this.mRadioScreenState = RADIO_SCREEN_UNSET;
        this.mIsDevicePlugged = RILJ_LOGV;
        this.mWlSequenceNum = RESPONSE_SOLICITED;
        this.mAckWlSequenceNum = RESPONSE_SOLICITED;
        this.mRequestList = new SparseArray();
        this.mTestingEmergencyCall = new AtomicBoolean(RILJ_LOGV);
        this.mDisplayListener = new DisplayListener() {
            public void onDisplayAdded(int displayId) {
            }

            public void onDisplayRemoved(int displayId) {
            }

            public void onDisplayChanged(int displayId) {
                if (displayId == 0) {
                    int oldState = RIL.this.mDefaultDisplayState;
                    RIL.this.mDefaultDisplayState = RIL.this.mDefaultDisplay.getState();
                    if (RIL.this.mDefaultDisplayState != oldState) {
                        RIL.this.updateScreenState();
                    }
                }
            }
        };
        this.mBatteryStateListener = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = RIL.RILJ_LOGV;
                RIL ril = RIL.this;
                if (intent.getIntExtra("plugged", RIL.RESPONSE_SOLICITED) != 0) {
                    z = RIL.RILJ_LOGD;
                }
                ril.mIsDevicePlugged = z;
            }
        };
        this.PROP_LTE_ENABLED = "persist.radio.lte_enabled";
        riljLog("RIL(context, preferredNetworkType=" + preferredNetworkType + " cdmaSubscription=" + cdmaSubscription + ")");
        this.mContext = context;
        this.mCdmaSubscription = cdmaSubscription;
        this.mPreferredNetworkType = preferredNetworkType;
        this.mPhoneType = RESPONSE_SOLICITED;
        this.mInstanceId = instanceId;
        if (this.mInstanceId != null) {
            setHwRILReferenceInstanceId(this.mInstanceId.intValue());
        }
        this.mEventLog = new TelephonyEventLog(this.mInstanceId.intValue());
        PowerManager pm = (PowerManager) context.getSystemService("power");
        this.mWakeLock = pm.newWakeLock(RESPONSE_UNSOLICITED, RILJ_LOG_TAG);
        this.mWakeLock.setReferenceCounted(RILJ_LOGV);
        this.mAckWakeLock = pm.newWakeLock(RESPONSE_UNSOLICITED, RILJ_ACK_WAKELOCK_NAME);
        this.mAckWakeLock.setReferenceCounted(RILJ_LOGV);
        this.mWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", DEFAULT_WAKE_LOCK_TIMEOUT_MS);
        this.mAckWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", DEFAULT_ACK_WAKE_LOCK_TIMEOUT_MS);
        this.mWakeLockCount = RESPONSE_SOLICITED;
        if (isPlatformTwoModems()) {
            initVSimRilSocketMap();
        }
        this.mSenderThread = new HandlerThread("RILSender" + this.mInstanceId);
        this.mSenderThread.start();
        this.mSender = new RILSender(this.mSenderThread.getLooper());
        if (((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(RESPONSE_SOLICITED)) {
            riljLog("Starting RILReceiver" + this.mInstanceId);
            DisplayManager dm = (DisplayManager) context.getSystemService("display");
            this.mDefaultDisplay = dm.getDisplay(RESPONSE_SOLICITED);
            this.mReceiver = new RILReceiver();
            this.mReceiverThread = new Thread(this.mReceiver, "RILReceiver" + this.mInstanceId);
            this.mReceiverThread.start();
            dm.registerDisplayListener(this.mDisplayListener, null);
            this.mDefaultDisplayState = this.mDefaultDisplay.getState();
            Intent batteryStatus = context.registerReceiver(this.mBatteryStateListener, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (batteryStatus != null) {
                this.mIsDevicePlugged = batteryStatus.getIntExtra("plugged", RESPONSE_SOLICITED) != 0 ? RILJ_LOGD : RILJ_LOGV;
            }
        } else {
            riljLog("Not starting RILReceiver: wifi-only");
        }
        TelephonyDevController tdc = TelephonyDevController.getInstance();
        TelephonyDevController.registerRIL(this);
    }

    public Context getContext() {
        return this.mContext;
    }

    private void initVSimRilSocketMap() {
        try {
            if (System.getInt(this.mContext.getContentResolver(), "vsim_enabled_subid") != RADIO_SCREEN_UNSET) {
                int isSlotsSwitched = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots", RESPONSE_SOLICITED);
                if (isSlotsSwitched == 0) {
                    this.mRilSocketMapEnable = RILJ_LOGD;
                    this.mRilSocketMaps[RESPONSE_SOLICITED] = RESPONSE_SOLICITED_ACK;
                    this.mRilSocketMaps[RESPONSE_UNSOLICITED] = RESPONSE_UNSOLICITED;
                    this.mRilSocketMaps[RESPONSE_SOLICITED_ACK] = RESPONSE_SOLICITED;
                    riljLog("[2Cards]> vsim enabled , ril socket map: 2,1,0");
                    return;
                } else if (RESPONSE_UNSOLICITED == isSlotsSwitched) {
                    this.mRilSocketMapEnable = RILJ_LOGD;
                    this.mRilSocketMaps[RESPONSE_SOLICITED] = RESPONSE_UNSOLICITED;
                    this.mRilSocketMaps[RESPONSE_UNSOLICITED] = RESPONSE_SOLICITED_ACK;
                    this.mRilSocketMaps[RESPONSE_SOLICITED_ACK] = RESPONSE_SOLICITED;
                    riljLog("[2Cards]> vsim enabled , ril socket map: 1,2,0");
                    return;
                } else {
                    return;
                }
            }
            riljLog("[2Cards]> vsim disabled");
        } catch (SettingNotFoundException e) {
            Rlog.e(RILJ_LOG_TAG, "SettingNotFoundException reading vsim_enabled_subid or switch_dual_card_slots ");
        }
    }

    public boolean isRadioAvailable() {
        if (this.mRilSocketMapEnable) {
            return this.mRadioAvailable;
        }
        return RILJ_LOGD;
    }

    public void setResultMessage(Message msg) {
        this.mResultMessage = msg;
    }

    protected void notifyPendingRilSocket() {
        synchronized (this.mPendingRilSocketLock) {
            this.mPendingRilSocketLock.notifyAll();
        }
    }

    protected boolean isPlatformTwoModems() {
        return VSimUtilsInner.isPlatformTwoModems();
    }

    public void hvCheckCard(Message response) {
        send(RILRequest.obtain(2111, response));
    }

    public void getVoiceRadioTechnology(Message result) {
        RILRequest rr = RILRequest.obtain(AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getImsRegistrationState(Message result) {
        RILRequest rr = RILRequest.obtain(112, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setOnNITZTime(Handler h, int what, Object obj) {
        super.setOnNITZTime(h, what, obj);
        if (this.mLastNITZTimeInfo != null) {
            this.mNITZTimeRegistrant.notifyRegistrant(new AsyncResult(null, this.mLastNITZTimeInfo, null));
        }
    }

    public void getIccCardStatus(Message result) {
        RILRequest rr = RILRequest.obtain(RESPONSE_UNSOLICITED, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message result) {
        RILRequest rr = RILRequest.obtain(122, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " slot: " + slotId + " appIndex: " + appIndex + " subId: " + subId + " subStatus: " + subStatus);
        rr.mParcel.writeInt(slotId);
        rr.mParcel.writeInt(appIndex);
        rr.mParcel.writeInt(subId);
        rr.mParcel.writeInt(subStatus);
        send(rr);
    }

    public void setDataAllowed(boolean allowed, Message result) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(123, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " allowed: " + allowed);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (!allowed) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        send(rr);
    }

    public void supplyIccPin(String pin, Message result) {
        supplyIccPinForApp(pin, null, result);
    }

    public void supplyIccPinForApp(String pin, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(RESPONSE_SOLICITED_ACK, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeString(pin);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void supplyIccPuk(String puk, String newPin, Message result) {
        supplyIccPukForApp(puk, newPin, null, result);
    }

    public void supplyIccPukForApp(String puk, String newPin, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(RESPONSE_SOLICITED_ACK_EXP, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK_EXP);
        rr.mParcel.writeString(puk);
        rr.mParcel.writeString(newPin);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void supplyIccPin2(String pin, Message result) {
        supplyIccPin2ForApp(pin, null, result);
    }

    public void supplyIccPin2ForApp(String pin, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(RESPONSE_UNSOLICITED_ACK_EXP, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeString(pin);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void supplyIccPuk2(String puk2, String newPin2, Message result) {
        supplyIccPuk2ForApp(puk2, newPin2, null, result);
    }

    public void supplyIccPuk2ForApp(String puk, String newPin2, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(EVENT_BLOCKING_RESPONSE_TIMEOUT, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK_EXP);
        rr.mParcel.writeString(puk);
        rr.mParcel.writeString(newPin2);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void changeIccPin(String oldPin, String newPin, Message result) {
        changeIccPinForApp(oldPin, newPin, null, result);
    }

    public void changeIccPinForApp(String oldPin, String newPin, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(6, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK_EXP);
        rr.mParcel.writeString(oldPin);
        rr.mParcel.writeString(newPin);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void changeIccPin2(String oldPin2, String newPin2, Message result) {
        changeIccPin2ForApp(oldPin2, newPin2, null, result);
    }

    public void changeIccPin2ForApp(String oldPin2, String newPin2, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(7, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK_EXP);
        rr.mParcel.writeString(oldPin2);
        rr.mParcel.writeString(newPin2);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message result) {
        RILRequest rr = RILRequest.obtain(44, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK_EXP);
        rr.mParcel.writeString(facility);
        rr.mParcel.writeString(oldPwd);
        rr.mParcel.writeString(newPwd);
        send(rr);
    }

    public void supplyNetworkDepersonalization(String netpin, Message result) {
        RILRequest rr = RILRequest.obtain(8, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK_EXP);
        rr.mParcel.writeString(netpin);
        send(rr);
    }

    public void getCurrentCalls(Message result) {
        RILRequest rr = RILRequest.obtain(9, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    @Deprecated
    public void getPDPContextList(Message result) {
        getDataCallList(result);
    }

    public void getDataCallList(Message result) {
        RILRequest rr = RILRequest.obtain(57, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void dial(String address, int clirMode, Message result) {
        dial(address, clirMode, null, result);
    }

    public void dial(String address, int clirMode, UUSInfo uusInfo, Message result) {
        RILRequest rr = RILRequest.obtain(10, result);
        rr.mParcel.writeString(address);
        rr.mParcel.writeInt(clirMode);
        if (uusInfo == null) {
            rr.mParcel.writeInt(RESPONSE_SOLICITED);
        } else {
            rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
            rr.mParcel.writeInt(uusInfo.getType());
            rr.mParcel.writeInt(uusInfo.getDcs());
            rr.mParcel.writeByteArray(uusInfo.getUserData());
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mEventLog.writeRilDial(rr.mSerial, clirMode, uusInfo);
        send(rr);
    }

    public void getIMSI(Message result) {
        getIMSIForApp(null, result);
    }

    public void getIMSIForApp(String aid, Message result) {
        RILRequest rr = RILRequest.obtain(11, result);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeString(aid);
        riljLog(rr.serialString() + "> getIMSI: " + requestToString(rr.mRequest) + " aid: " + aid);
        send(rr);
    }

    public void getIMEI(Message result) {
        RILRequest rr = RILRequest.obtain(38, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getIMEISV(Message result) {
        RILRequest rr = RILRequest.obtain(39, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void hangupConnection(int gsmIndex, Message result) {
        riljLog("hangupConnection: gsmIndex=" + gsmIndex);
        RILRequest rr = RILRequest.obtain(12, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + gsmIndex);
        this.mEventLog.writeRilHangup(rr.mSerial, 12, gsmIndex);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(gsmIndex);
        send(rr);
    }

    public void hangupWaitingOrBackground(Message result) {
        RILRequest rr = RILRequest.obtain(13, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mEventLog.writeRilHangup(rr.mSerial, 13, RADIO_SCREEN_UNSET);
        send(rr);
    }

    public void hangupForegroundResumeBackground(Message result) {
        RILRequest rr = RILRequest.obtain(14, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mEventLog.writeRilHangup(rr.mSerial, 14, RADIO_SCREEN_UNSET);
        send(rr);
    }

    public void switchWaitingOrHoldingAndActive(Message result) {
        RILRequest rr = RILRequest.obtain(15, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void conference(Message result) {
        RILRequest rr = RILRequest.obtain(16, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setPreferredVoicePrivacy(boolean enable, Message result) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(82, result);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (!enable) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        send(rr);
    }

    public void getPreferredVoicePrivacy(Message result) {
        send(RILRequest.obtain(83, result));
    }

    public void separateConnection(int gsmIndex, Message result) {
        RILRequest rr = RILRequest.obtain(52, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + gsmIndex);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(gsmIndex);
        send(rr);
    }

    public void acceptCall(Message result) {
        RILRequest rr = RILRequest.obtain(40, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mEventLog.writeRilAnswer(rr.mSerial);
        send(rr);
    }

    public void rejectCall(Message result) {
        RILRequest rr = RILRequest.obtain(17, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void explicitCallTransfer(Message result) {
        RILRequest rr = RILRequest.obtain(72, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getLastCallFailCause(Message result) {
        RILRequest rr = RILRequest.obtain(18, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    @Deprecated
    public void getLastPdpFailCause(Message result) {
        getLastDataCallFailCause(result);
    }

    public void getLastDataCallFailCause(Message result) {
        RILRequest rr = RILRequest.obtain(56, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setMute(boolean enableMute, Message response) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(53, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + enableMute);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (!enableMute) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        send(rr);
    }

    public void getMute(Message response) {
        RILRequest rr = RILRequest.obtain(54, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getSignalStrength(Message result) {
        RILRequest rr = RILRequest.obtain(19, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getVoiceRegistrationState(Message result) {
        RILRequest rr = RILRequest.obtain(20, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getDataRegistrationState(Message result) {
        RILRequest rr = RILRequest.obtain(21, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getOperator(Message result) {
        RILRequest rr = RILRequest.obtain(22, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getHardwareConfig(Message result) {
        RILRequest rr = RILRequest.obtain(124, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void sendDtmf(char c, Message result) {
        RILRequest rr = RILRequest.obtain(24, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeString(Character.toString(c));
        send(rr);
    }

    public void startDtmf(char c, Message result) {
        RILRequest rr = RILRequest.obtain(49, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeString(Character.toString(c));
        send(rr);
    }

    public void stopDtmf(Message result) {
        RILRequest rr = RILRequest.obtain(50, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message result) {
        RILRequest rr = RILRequest.obtain(85, result);
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK_EXP);
        rr.mParcel.writeString(dtmfString);
        rr.mParcel.writeString(Integer.toString(on));
        rr.mParcel.writeString(Integer.toString(off));
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + CharacterSets.MIMENAME_ANY_CHARSET);
        send(rr);
    }

    private void constructGsmSendSmsRilRequest(RILRequest rr, String smscPDU, String pdu) {
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeString(smscPDU);
        rr.mParcel.writeString(pdu);
    }

    public void sendSMS(String smscPDU, String pdu, Message result) {
        RILRequest rr = RILRequest.obtain(25, result);
        constructGsmSendSmsRilRequest(rr, smscPDU, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mEventLog.writeRilSendSms(rr.mSerial, rr.mRequest);
        send(rr);
    }

    public void sendSMSExpectMore(String smscPDU, String pdu, Message result) {
        RILRequest rr = RILRequest.obtain(26, result);
        constructGsmSendSmsRilRequest(rr, smscPDU, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mEventLog.writeRilSendSms(rr.mSerial, rr.mRequest);
        send(rr);
    }

    private void constructCdmaSendSmsRilRequest(RILRequest rr, byte[] pdu) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pdu));
        try {
            int i;
            rr.mParcel.writeInt(dis.readInt());
            rr.mParcel.writeByte((byte) dis.readInt());
            rr.mParcel.writeInt(dis.readInt());
            rr.mParcel.writeInt(dis.read());
            rr.mParcel.writeInt(dis.read());
            rr.mParcel.writeInt(dis.read());
            rr.mParcel.writeInt(dis.read());
            int address_nbr_of_digits = (byte) dis.read();
            rr.mParcel.writeByte((byte) address_nbr_of_digits);
            for (i = RESPONSE_SOLICITED; i < address_nbr_of_digits; i += RESPONSE_UNSOLICITED) {
                rr.mParcel.writeByte(dis.readByte());
            }
            rr.mParcel.writeInt(dis.read());
            rr.mParcel.writeByte((byte) dis.read());
            int subaddr_nbr_of_digits = (byte) dis.read();
            rr.mParcel.writeByte((byte) subaddr_nbr_of_digits);
            for (i = RESPONSE_SOLICITED; i < subaddr_nbr_of_digits; i += RESPONSE_UNSOLICITED) {
                rr.mParcel.writeByte(dis.readByte());
            }
            int bearerDataLength = dis.read();
            rr.mParcel.writeInt(bearerDataLength);
            for (i = RESPONSE_SOLICITED; i < bearerDataLength; i += RESPONSE_UNSOLICITED) {
                rr.mParcel.writeByte(dis.readByte());
            }
        } catch (IOException ex) {
            riljLog("sendSmsCdma: conversion from input stream to object failed: " + ex);
        }
    }

    public void sendCdmaSms(byte[] pdu, Message result) {
        RILRequest rr = RILRequest.obtain(87, result);
        constructCdmaSendSmsRilRequest(rr, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mEventLog.writeRilSendSms(rr.mSerial, rr.mRequest);
        send(rr);
    }

    public void sendImsGsmSms(String smscPDU, String pdu, int retry, int messageRef, Message result) {
        RILRequest rr = RILRequest.obtain(113, result);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeByte((byte) retry);
        rr.mParcel.writeInt(messageRef);
        constructGsmSendSmsRilRequest(rr, smscPDU, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mEventLog.writeRilSendSms(rr.mSerial, rr.mRequest);
        send(rr);
    }

    public void sendImsCdmaSms(byte[] pdu, int retry, int messageRef, Message result) {
        RILRequest rr = RILRequest.obtain(113, result);
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeByte((byte) retry);
        rr.mParcel.writeInt(messageRef);
        constructCdmaSendSmsRilRequest(rr, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mEventLog.writeRilSendSms(rr.mSerial, rr.mRequest);
        send(rr);
    }

    public void deleteSmsOnSim(int index, Message response) {
        RILRequest rr = RILRequest.obtain(64, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(index);
        send(rr);
    }

    public void deleteSmsOnRuim(int index, Message response) {
        RILRequest rr = RILRequest.obtain(97, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(index);
        send(rr);
    }

    public void writeSmsToSim(int status, String smsc, String pdu, Message response) {
        status = translateStatus(status);
        RILRequest rr = RILRequest.obtain(63, response);
        rr.mParcel.writeInt(status);
        rr.mParcel.writeString(pdu);
        rr.mParcel.writeString(smsc);
        send(rr);
    }

    public void writeSmsToRuim(int status, String pdu, Message response) {
        status = translateStatus(status);
        RILRequest rr = RILRequest.obtain(96, response);
        rr.mParcel.writeInt(status);
        writeContent(new RILRequestReference(rr), pdu);
        send(rr);
    }

    private int translateStatus(int status) {
        switch (status & 7) {
            case RESPONSE_UNSOLICITED /*1*/:
                return RESPONSE_UNSOLICITED;
            case RESPONSE_SOLICITED_ACK_EXP /*3*/:
                return RESPONSE_SOLICITED;
            case EVENT_BLOCKING_RESPONSE_TIMEOUT /*5*/:
                return RESPONSE_SOLICITED_ACK_EXP;
            case CharacterSets.ISO_8859_4 /*7*/:
                return RESPONSE_SOLICITED_ACK;
            default:
                return RESPONSE_UNSOLICITED;
        }
    }

    public void setupDataCall(int radioTechnology, int profile, String apn, String user, String password, int authType, String protocol, Message result) {
        try {
            Map<String, String> map = correctApnAuth(user, authType, password);
            user = (String) map.get("userName");
            password = (String) map.get(Carriers.PASSWORD);
            authType = Integer.parseInt((String) map.get("authType"));
        } catch (Exception e) {
            riljLog(e + "The authType is not number");
        }
        RILRequest rr = RILRequest.obtain(27, result);
        rr.mParcel.writeInt(7);
        rr.mParcel.writeString(Integer.toString(radioTechnology + RESPONSE_SOLICITED_ACK));
        rr.mParcel.writeString(Integer.toString(profile));
        rr.mParcel.writeString(apn);
        rr.mParcel.writeString(user);
        rr.mParcel.writeString(password);
        rr.mParcel.writeString(Integer.toString(authType));
        rr.mParcel.writeString(protocol);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + radioTechnology + " " + profile + " " + apn + " " + user + " " + authType + " " + protocol);
        this.mEventLog.writeRilSetupDataCall(rr.mSerial, radioTechnology, profile, apn, user, password, authType, protocol);
        send(rr);
    }

    public void deactivateDataCall(int cid, int reason, Message result) {
        RILRequest rr = RILRequest.obtain(41, result);
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeString(Integer.toString(cid));
        rr.mParcel.writeString(Integer.toString(reason));
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + cid + " " + reason);
        this.mEventLog.writeRilDeactivateDataCall(rr.mSerial, cid, reason);
        send(rr);
    }

    public void setRadioPower(boolean on, Message result) {
        int i = RESPONSE_UNSOLICITED;
        if (ServiceStateTracker.ISDEMO) {
            on = RILJ_LOGV;
        }
        setShouldReportRoamingPlusInfo(on);
        RILRequest rr = RILRequest.obtain(23, result);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (!on) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + (on ? " on" : " off"));
        send(rr);
    }

    public void requestShutdown(Message result) {
        RILRequest rr = RILRequest.obtain(PduPart.P_DISPOSITION_ATTACHMENT, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setSuppServiceNotifications(boolean enable, Message result) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(62, result);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (!enable) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message result) {
        RILRequest rr = RILRequest.obtain(37, result);
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeInt(success ? RESPONSE_UNSOLICITED : RESPONSE_SOLICITED);
        rr.mParcel.writeInt(cause);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + success + " " + cause);
        send(rr);
    }

    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause, Message result) {
        RILRequest rr = RILRequest.obtain(88, result);
        rr.mParcel.writeInt(success ? RESPONSE_SOLICITED : RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(cause);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + success + " " + cause);
        send(rr);
    }

    public void acknowledgeIncomingGsmSmsWithPdu(boolean success, String ackPdu, Message result) {
        RILRequest rr = RILRequest.obtain(CharacterSets.UTF_8, result);
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeString(success ? ProxyController.MODEM_1 : ProxyController.MODEM_0);
        rr.mParcel.writeString(ackPdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ' ' + success + " [" + ackPdu + ']');
        send(rr);
    }

    public void iccIO(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, Message result) {
        iccIOForApp(command, fileid, path, p1, p2, p3, data, pin2, null, result);
    }

    public void iccIOForApp(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(28, result);
        rr.mParcel.writeInt(command);
        rr.mParcel.writeInt(fileid);
        rr.mParcel.writeString(path);
        rr.mParcel.writeInt(p1);
        rr.mParcel.writeInt(p2);
        rr.mParcel.writeInt(p3);
        rr.mParcel.writeString(data);
        rr.mParcel.writeString(pin2);
        rr.mParcel.writeString(aid);
        riljLog(rr.serialString() + "> iccIO: " + requestToString(rr.mRequest) + " 0x" + Integer.toHexString(command) + " 0x" + Integer.toHexString(fileid) + " " + " path: " + path + "," + p1 + "," + p2 + "," + p3 + " aid: " + aid);
        send(rr);
    }

    public void getCLIR(Message result) {
        RILRequest rr = RILRequest.obtain(CDMA_BROADCAST_SMS_NO_OF_SERVICE_CATEGORIES, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCLIR(int clirMode, Message result) {
        RILRequest rr = RILRequest.obtain(32, result);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(clirMode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + clirMode);
        send(rr);
    }

    public void queryCallWaiting(int serviceClass, Message response) {
        RILRequest rr = RILRequest.obtain(35, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(serviceClass);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + serviceClass);
        send(rr);
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message response) {
        RILRequest rr = RILRequest.obtain(36, response);
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeInt(enable ? RESPONSE_UNSOLICITED : RESPONSE_SOLICITED);
        rr.mParcel.writeInt(serviceClass);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + enable + ", " + serviceClass);
        send(rr);
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        RILRequest rr = RILRequest.obtain(46, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setNetworkSelectionModeManual(String operatorNumeric, Message response) {
        RILRequest rr = RILRequest.obtain(47, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + operatorNumeric);
        rr.mParcel.writeString(operatorNumeric);
        send(rr);
    }

    public void getNetworkSelectionMode(Message response) {
        RILRequest rr = RILRequest.obtain(45, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getAvailableNetworks(Message response) {
        RILRequest rr = RILRequest.obtain(48, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message response) {
        RILRequest rr = RILRequest.obtain(34, response);
        rr.mParcel.writeInt(action);
        rr.mParcel.writeInt(cfReason);
        rr.mParcel.writeInt(serviceClass);
        rr.mParcel.writeInt(PhoneNumberUtils.toaFromString(number));
        rr.mParcel.writeString(number);
        rr.mParcel.writeInt(timeSeconds);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + action + " " + cfReason + " " + serviceClass + timeSeconds);
        send(rr);
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message response) {
        RILRequest rr = RILRequest.obtain(33, response);
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeInt(cfReason);
        rr.mParcel.writeInt(serviceClass);
        rr.mParcel.writeInt(PhoneNumberUtils.toaFromString(number));
        rr.mParcel.writeString(number);
        rr.mParcel.writeInt(RESPONSE_SOLICITED);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + cfReason + " " + serviceClass);
        send(rr);
    }

    public void queryCLIP(Message response) {
        RILRequest rr = RILRequest.obtain(55, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getBasebandVersion(Message response) {
        RILRequest rr = RILRequest.obtain(51, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void queryFacilityLock(String facility, String password, int serviceClass, Message response) {
        queryFacilityLockForApp(facility, password, serviceClass, null, response);
    }

    public void queryFacilityLockForApp(String facility, String password, int serviceClass, String appId, Message response) {
        RILRequest rr = RILRequest.obtain(42, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " [" + facility + " " + serviceClass + " " + appId + "]");
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED_ACK_EXP);
        rr.mParcel.writeString(facility);
        rr.mParcel.writeString(password);
        rr.mParcel.writeString(Integer.toString(serviceClass));
        rr.mParcel.writeString(appId);
        send(rr);
    }

    public void setFacilityLock(String facility, boolean lockState, String password, int serviceClass, Message response) {
        setFacilityLockForApp(facility, lockState, password, serviceClass, null, response);
    }

    public void setFacilityLockForApp(String facility, boolean lockState, String password, int serviceClass, String appId, Message response) {
        RILRequest rr = RILRequest.obtain(43, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " [" + facility + " " + lockState + " " + serviceClass + " " + appId + "]");
        rr.mParcel.writeInt(EVENT_BLOCKING_RESPONSE_TIMEOUT);
        rr.mParcel.writeString(facility);
        rr.mParcel.writeString(lockState ? ProxyController.MODEM_1 : ProxyController.MODEM_0);
        rr.mParcel.writeString(password);
        rr.mParcel.writeString(Integer.toString(serviceClass));
        rr.mParcel.writeString(appId);
        send(rr);
    }

    public void sendUSSD(String ussdString, Message response) {
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mInstanceId.intValue(), RESPONSE_SOLICITED, "AP_FLOW_SUC");
        RILRequest rr = RILRequest.obtain(29, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + "*******");
        rr.mParcel.writeString(ussdString);
        send(rr);
    }

    public void cancelPendingUssd(Message response) {
        RILRequest rr = RILRequest.obtain(30, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void resetRadio(Message result) {
        RILRequest rr = RILRequest.obtain(58, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        RILRequest rr = RILRequest.obtain(59, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "[" + IccUtils.bytesToHexString(data) + "]");
        rr.mParcel.writeByteArray(data);
        send(rr);
    }

    public void invokeOemRilRequestStrings(String[] strings, Message response) {
        RILRequest rr = RILRequest.obtain(60, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeStringArray(strings);
        send(rr);
    }

    public void setBandMode(int bandMode, Message response) {
        RILRequest rr = RILRequest.obtain(65, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(bandMode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + bandMode);
        send(rr);
    }

    public void queryAvailableBandMode(Message response) {
        RILRequest rr = RILRequest.obtain(66, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void sendTerminalResponse(String contents, Message response) {
        RILRequest rr = RILRequest.obtain(70, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeString(contents);
        send(rr);
    }

    public void sendEnvelope(String contents, Message response) {
        RILRequest rr = RILRequest.obtain(69, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeString(contents);
        send(rr);
    }

    public void sendEnvelopeWithStatus(String contents, Message response) {
        RILRequest rr = RILRequest.obtain(107, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + '[' + contents + ']');
        rr.mParcel.writeString(contents);
        send(rr);
    }

    public void handleCallSetupRequestFromSim(boolean accept, Message response) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(71, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        int[] param = new int[RESPONSE_UNSOLICITED];
        if (!accept) {
            i = RESPONSE_SOLICITED;
        }
        param[RESPONSE_SOLICITED] = i;
        rr.mParcel.writeIntArray(param);
        send(rr);
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        RILRequest rr = RILRequest.obtain(73, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(networkType);
        this.mPreferredNetworkType = networkType;
        custSetModemProperties();
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + networkType);
        this.mEventLog.writeSetPreferredNetworkType(networkType);
        send(rr);
    }

    public void getPreferredNetworkType(Message response) {
        RILRequest rr = RILRequest.obtain(74, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getNeighboringCids(Message response) {
        RILRequest rr = RILRequest.obtain(75, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setLocationUpdates(boolean enable, Message response) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(76, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (!enable) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + enable);
        send(rr);
    }

    public void getSmscAddress(Message result) {
        RILRequest rr = RILRequest.obtain(100, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setSmscAddress(String address, Message result) {
        RILRequest rr = RILRequest.obtain(CallFailCause.MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE, result);
        rr.mParcel.writeString(address);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + address);
        send(rr);
    }

    public void reportSmsMemoryStatus(boolean available, Message result) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(CallFailCause.RECOVERY_ON_TIMER_EXPIRED, result);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (!available) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + available);
        send(rr);
    }

    public void reportStkServiceIsRunning(Message result) {
        RILRequest rr = RILRequest.obtain(103, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getGsmBroadcastConfig(Message response) {
        RILRequest rr = RILRequest.obtain(89, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message response) {
        int i;
        RILRequest rr = RILRequest.obtain(90, response);
        int numOfConfig = config.length;
        rr.mParcel.writeInt(numOfConfig);
        for (i = RESPONSE_SOLICITED; i < numOfConfig; i += RESPONSE_UNSOLICITED) {
            int i2;
            rr.mParcel.writeInt(config[i].getFromServiceId());
            rr.mParcel.writeInt(config[i].getToServiceId());
            rr.mParcel.writeInt(config[i].getFromCodeScheme());
            rr.mParcel.writeInt(config[i].getToCodeScheme());
            Parcel parcel = rr.mParcel;
            if (config[i].isSelected()) {
                i2 = RESPONSE_UNSOLICITED;
            } else {
                i2 = RESPONSE_SOLICITED;
            }
            parcel.writeInt(i2);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + numOfConfig + " configs : ");
        for (i = RESPONSE_SOLICITED; i < numOfConfig; i += RESPONSE_UNSOLICITED) {
            riljLog(config[i].toString());
        }
        send(rr);
    }

    public void setGsmBroadcastActivation(boolean activate, Message response) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(91, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (activate) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    private void updateScreenState() {
        int i;
        boolean z = RILJ_LOGD;
        int oldState = this.mRadioScreenState;
        this.mDefaultDisplayState = this.mDefaultDisplay.getState();
        if (this.mDefaultDisplayState == RESPONSE_SOLICITED_ACK || this.mIsDevicePlugged) {
            i = RESPONSE_UNSOLICITED;
        } else {
            i = RESPONSE_SOLICITED;
        }
        this.mRadioScreenState = i;
        if (this.mRadioScreenState != oldState) {
            if (this.mRadioScreenState != RESPONSE_UNSOLICITED) {
                z = RILJ_LOGV;
            }
            sendScreenState(z);
        }
    }

    private void sendScreenState(boolean on) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(61, null);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (!on) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + on);
        send(rr);
    }

    protected void onRadioAvailable() {
        updateScreenStateOnRadioAvailable();
    }

    private int convertRadioState(int stateInt) {
        if (stateInt <= RESPONSE_UNSOLICITED || stateInt >= 10) {
            return stateInt;
        }
        return 10;
    }

    private RadioState getRadioStateFromInt(int stateInt) {
        int newState = convertRadioState(stateInt);
        riljLog("UNSOL_RESPONSE_RADIO_STATE_CHANGED stateInt: " + stateInt);
        switch (newState) {
            case RESPONSE_SOLICITED /*0*/:
                return RadioState.RADIO_OFF;
            case RESPONSE_UNSOLICITED /*1*/:
                return RadioState.RADIO_UNAVAILABLE;
            case CharacterSets.ISO_8859_7 /*10*/:
                return RadioState.RADIO_ON;
            default:
                throw new RuntimeException("Unrecognized RIL_RadioState: " + stateInt);
        }
    }

    private void switchToRadioState(RadioState newState) {
        setRadioState(newState);
        handleUnsolicitedRadioStateChanged(newState.isOn(), this.mContext);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void acquireWakeLock(RILRequest rr, int wakeLockType) {
        synchronized (rr) {
            if (rr.mWakeLockType != RADIO_SCREEN_UNSET) {
                Rlog.d(RILJ_LOG_TAG, "Failed to aquire wakelock for " + rr.serialString());
                return;
            }
            Message msg;
            switch (wakeLockType) {
                case RESPONSE_SOLICITED /*0*/:
                    synchronized (this.mWakeLock) {
                        this.mWakeLock.acquire();
                        this.mWakeLockCount += RESPONSE_UNSOLICITED;
                        this.mWlSequenceNum += RESPONSE_UNSOLICITED;
                        msg = this.mSender.obtainMessage(RESPONSE_SOLICITED_ACK);
                        msg.arg1 = this.mWlSequenceNum;
                        this.mSender.sendMessageDelayed(msg, (long) this.mWakeLockTimeout);
                        break;
                    }
                case RESPONSE_UNSOLICITED /*1*/:
                    synchronized (this.mAckWakeLock) {
                        this.mAckWakeLock.acquire();
                        this.mAckWlSequenceNum += RESPONSE_UNSOLICITED;
                        msg = this.mSender.obtainMessage(RESPONSE_UNSOLICITED_ACK_EXP);
                        msg.arg1 = this.mAckWlSequenceNum;
                        this.mSender.sendMessageDelayed(msg, (long) this.mAckWakeLockTimeout);
                        break;
                    }
                    break;
                default:
                    Rlog.w(RILJ_LOG_TAG, "Acquiring Invalid Wakelock type " + wakeLockType);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void decrementWakeLock(RILRequest rr) {
        synchronized (rr) {
            switch (rr.mWakeLockType) {
                case RADIO_SCREEN_UNSET /*-1*/:
                case RESPONSE_UNSOLICITED /*1*/:
                    break;
                case RESPONSE_SOLICITED /*0*/:
                    synchronized (this.mWakeLock) {
                        if (this.mWakeLockCount <= RESPONSE_UNSOLICITED) {
                            this.mWakeLockCount = RESPONSE_SOLICITED;
                            this.mWakeLock.release();
                            break;
                        }
                        this.mWakeLockCount += RADIO_SCREEN_UNSET;
                        break;
                    }
                    break;
                default:
                    Rlog.w(RILJ_LOG_TAG, "Decrementing Invalid Wakelock type " + rr.mWakeLockType);
                    break;
            }
        }
    }

    private boolean clearWakeLock(int wakeLockType) {
        if (wakeLockType == 0) {
            synchronized (this.mWakeLock) {
                if (this.mWakeLockCount != 0 || this.mWakeLock.isHeld()) {
                    Rlog.d(RILJ_LOG_TAG, "NOTE: mWakeLockCount is " + this.mWakeLockCount + "at time of clearing");
                    this.mWakeLockCount = RESPONSE_SOLICITED;
                    this.mWakeLock.release();
                    return RILJ_LOGD;
                }
                return RILJ_LOGV;
            }
        }
        synchronized (this.mAckWakeLock) {
            if (this.mAckWakeLock.isHeld()) {
                this.mAckWakeLock.release();
                return RILJ_LOGD;
            }
            return RILJ_LOGV;
        }
    }

    public void send(RILRequestReference rr) {
        send(rr.mRilRequest);
    }

    private void send(RILRequest rr) {
        if (this.mSocket == null) {
            rr.onError(RESPONSE_UNSOLICITED, null);
            rr.release();
            return;
        }
        Message msg = this.mSender.obtainMessage(RESPONSE_UNSOLICITED, rr);
        acquireWakeLock(rr, RESPONSE_SOLICITED);
        msg.sendToTarget();
    }

    private void processResponse(Parcel p) {
        int type = p.readInt();
        if (type == RESPONSE_UNSOLICITED || type == RESPONSE_UNSOLICITED_ACK_EXP) {
            processUnsolicited(p, type);
        } else if (type == 0 || type == RESPONSE_SOLICITED_ACK_EXP) {
            rr = processSolicited(p, type);
            if (rr != null) {
                if (type == 0) {
                    decrementWakeLock(rr);
                }
                rr.release();
            }
        } else if (type == RESPONSE_SOLICITED_ACK) {
            int serial = p.readInt();
            synchronized (this.mRequestList) {
                rr = (RILRequest) this.mRequestList.get(serial);
            }
            if (rr == null) {
                Rlog.w(RILJ_LOG_TAG, "Unexpected solicited ack response! sn: " + serial);
            } else {
                decrementWakeLock(rr);
                riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
            }
        }
    }

    private void clearRequestList(int error, boolean loggable) {
        synchronized (this.mRequestList) {
            int count = this.mRequestList.size();
            if (loggable) {
                Rlog.d(RILJ_LOG_TAG, "clearRequestList  mWakeLockCount=" + this.mWakeLockCount + " mRequestList=" + count);
            }
            for (int i = RESPONSE_SOLICITED; i < count; i += RESPONSE_UNSOLICITED) {
                RILRequest rr = (RILRequest) this.mRequestList.valueAt(i);
                if (loggable) {
                    Rlog.d(RILJ_LOG_TAG, i + ": [" + rr.mSerial + "] " + requestToString(rr.mRequest));
                }
                rr.onError(error, null);
                decrementWakeLock(rr);
                rr.release();
            }
            this.mRequestList.clear();
        }
    }

    private RILRequest findAndRemoveRequestFromList(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = (RILRequest) this.mRequestList.get(serial);
            if (rr != null) {
                this.mRequestList.remove(serial);
            }
        }
        return rr;
    }

    private RILRequest processSolicited(Parcel p, int type) {
        int serial = p.readInt();
        int error = p.readInt();
        RILRequest rr = findAndRemoveRequestFromList(serial);
        if (rr == null) {
            Rlog.w(RILJ_LOG_TAG, "Unexpected solicited response! sn: " + serial + " error: " + error);
            return null;
        }
        if (getRilVersion() >= 13 && type == RESPONSE_SOLICITED_ACK_EXP) {
            Message msg = this.mSender.obtainMessage(RESPONSE_SOLICITED_ACK_EXP, RILRequest.obtain(800, null));
            acquireWakeLock(rr, RESPONSE_UNSOLICITED);
            msg.sendToTarget();
            riljLog("Response received for " + rr.serialString() + " " + requestToString(rr.mRequest) + " Sending ack to ril.cpp");
        }
        Object ret = null;
        if (error == 0 || p.dataAvail() > 0) {
            try {
                switch (rr.mRequest) {
                    case RESPONSE_UNSOLICITED /*1*/:
                        ret = responseIccCardStatus(p);
                        break;
                    case RESPONSE_SOLICITED_ACK /*2*/:
                        ret = responseInts(p);
                        break;
                    case RESPONSE_SOLICITED_ACK_EXP /*3*/:
                        ret = responseInts(p);
                        break;
                    case RESPONSE_UNSOLICITED_ACK_EXP /*4*/:
                        ret = responseInts(p);
                        break;
                    case EVENT_BLOCKING_RESPONSE_TIMEOUT /*5*/:
                        ret = responseInts(p);
                        break;
                    case CharacterSets.ISO_8859_3 /*6*/:
                        ret = responseInts(p);
                        break;
                    case CharacterSets.ISO_8859_4 /*7*/:
                        ret = responseInts(p);
                        break;
                    case CharacterSets.ISO_8859_5 /*8*/:
                        ret = responseInts(p);
                        break;
                    case CharacterSets.ISO_8859_6 /*9*/:
                        ret = responseCallList(p);
                        break;
                    case CharacterSets.ISO_8859_7 /*10*/:
                        ret = responseVoid(p);
                        break;
                    case CharacterSets.ISO_8859_8 /*11*/:
                        ret = responseString(p);
                        break;
                    case CharacterSets.ISO_8859_9 /*12*/:
                        ret = responseVoid(p);
                        break;
                    case UserData.ASCII_CR_INDEX /*13*/:
                        ret = responseVoid(p);
                        break;
                    case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                        if (this.mTestingEmergencyCall.getAndSet(RILJ_LOGV) && this.mEmergencyCallbackModeRegistrant != null) {
                            riljLog("testing emergency call, notify ECM Registrants");
                            this.mEmergencyCallbackModeRegistrant.notifyRegistrant();
                        }
                        ret = responseVoid(p);
                        break;
                    case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                        ret = responseVoid(p);
                        break;
                    case PduHeaders.MMS_VERSION_1_0 /*16*/:
                        ret = responseVoid(p);
                        break;
                    case PduHeaders.MMS_VERSION_1_1 /*17*/:
                        ret = responseVoid(p);
                        break;
                    case PduHeaders.MMS_VERSION_1_2 /*18*/:
                        ret = responseFailCause(p);
                        break;
                    case PduHeaders.MMS_VERSION_1_3 /*19*/:
                        ret = responseSignalStrength(p);
                        break;
                    case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                        ret = responseStrings(p);
                        break;
                    case SmsHeader.ELT_ID_REUSED_EXTENDED_OBJECT /*21*/:
                        ret = responseStrings(p);
                        break;
                    case CallFailCause.NUMBER_CHANGED /*22*/:
                        ret = responseStrings(p);
                        break;
                    case SmsHeader.ELT_ID_OBJECT_DISTR_INDICATOR /*23*/:
                        ret = responseVoid(p);
                        break;
                    case SmsHeader.ELT_ID_STANDARD_WVG_OBJECT /*24*/:
                        ret = responseVoid(p);
                        break;
                    case SmsHeader.ELT_ID_CHARACTER_SIZE_WVG_OBJECT /*25*/:
                        ret = responseSMS(p);
                        break;
                    case SmsHeader.ELT_ID_EXTENDED_OBJECT_DATA_REQUEST_CMD /*26*/:
                        ret = responseSMS(p);
                        break;
                    case CallFailCause.CALL_FAIL_DESTINATION_OUT_OF_ORDER /*27*/:
                        ret = responseSetupDataCall(p);
                        break;
                    case CallFailCause.INVALID_NUMBER /*28*/:
                        ret = responseICC_IO(p);
                        break;
                    case CallFailCause.FACILITY_REJECTED /*29*/:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.STATUS_ENQUIRY /*30*/:
                        ret = responseVoid(p);
                        break;
                    case CDMA_BROADCAST_SMS_NO_OF_SERVICE_CATEGORIES /*31*/:
                        ret = responseInts(p);
                        break;
                    case UserData.PRINTABLE_ASCII_MIN_INDEX /*32*/:
                        ret = responseVoid(p);
                        break;
                    case SmsHeader.ELT_ID_HYPERLINK_FORMAT_ELEMENT /*33*/:
                        ret = responseCallForward(p);
                        break;
                    case CallFailCause.NO_CIRCUIT_AVAIL /*34*/:
                        ret = responseVoid(p);
                        break;
                    case SmsHeader.ELT_ID_ENHANCED_VOICE_MAIL_INFORMATION /*35*/:
                        ret = responseInts(p);
                        break;
                    case CdmaSmsAddress.SMS_SUBADDRESS_MAX /*36*/:
                        ret = responseVoid(p);
                        break;
                    case SmsHeader.ELT_ID_NATIONAL_LANGUAGE_LOCKING_SHIFT /*37*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_MIP_PROFILE_HA_SPI /*38*/:
                        ret = responseString(p);
                        break;
                    case RadioNVItems.RIL_NV_MIP_PROFILE_AAA_SPI /*39*/:
                        ret = responseString(p);
                        break;
                    case RadioNVItems.RIL_NV_MIP_PROFILE_MN_HA_SS /*40*/:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.TEMPORARY_FAILURE /*41*/:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.SWITCHING_CONGESTION /*42*/:
                        ret = responseInts(p);
                        break;
                    case CallFailCause.ACCESS_INFORMATION_DISCARDED /*43*/:
                        ret = responseInts(p);
                        break;
                    case CallFailCause.CHANNEL_NOT_AVAIL /*44*/:
                        ret = responseVoid(p);
                        break;
                    case 45:
                        ret = responseInts(p);
                        break;
                    case 46:
                        ret = responseVoid(p);
                        break;
                    case WspTypeDecoder.PARAMETER_ID_X_WAP_APPLICATION_ID /*47*/:
                        ret = responseVoid(p);
                        break;
                    case 48:
                        ret = responseOperatorInfos(p);
                        break;
                    case CallFailCause.QOS_NOT_AVAIL /*49*/:
                        ret = responseVoid(p);
                        break;
                    case SmsCbConstants.MESSAGE_ID_GSMA_ALLOCATED_CHANNEL_50 /*50*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_CDMA_PRL_VERSION /*51*/:
                        ret = responseString(p);
                        break;
                    case RadioNVItems.RIL_NV_CDMA_BC10 /*52*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_CDMA_BC14 /*53*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_CDMA_SO68 /*54*/:
                        ret = responseInts(p);
                        break;
                    case RadioNVItems.RIL_NV_CDMA_SO73_COP0 /*55*/:
                        ret = responseInts(p);
                        break;
                    case RadioNVItems.RIL_NV_CDMA_SO73_COP1TO7 /*56*/:
                        ret = responseInts(p);
                        break;
                    case RadioNVItems.RIL_NV_CDMA_1X_ADVANCED_ENABLED /*57*/:
                        ret = responseDataCallList(p);
                        break;
                    case CallFailCause.BEARER_NOT_AVAIL /*58*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_CDMA_EHRPD_FORCED /*59*/:
                        ret = responseRaw(p);
                        break;
                    case 60:
                        ret = responseStrings(p);
                        break;
                    case 61:
                        ret = responseVoid(p);
                        break;
                    case 62:
                        ret = responseVoid(p);
                        break;
                    case SignalToneUtil.IS95_CONST_IR_SIG_TONE_NO_TONE /*63*/:
                        ret = responseInts(p);
                        break;
                    case CommandsInterface.SERVICE_CLASS_PACKET /*64*/:
                        ret = responseVoid(p);
                        break;
                    case HwRadarUtils.RADAR_LEVEL_A /*65*/:
                        ret = responseVoid(p);
                        break;
                    case HwRadarUtils.RADAR_LEVEL_B /*66*/:
                        ret = responseInts(p);
                        break;
                    case HwRadarUtils.RADAR_LEVEL_C /*67*/:
                        ret = responseString(p);
                        break;
                    case HwRadarUtils.RADAR_LEVEL_D /*68*/:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.REQUESTED_FACILITY_NOT_IMPLEMENTED /*69*/:
                        ret = responseString(p);
                        break;
                    case CallFailCause.ONLY_DIGITAL_INFORMATION_BEARER_AVAILABLE /*70*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_25 /*71*/:
                        ret = responseInts(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /*72*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_41 /*73*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /*74*/:
                        ret = responseGetPreferredNetworkType(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_26 /*75*/:
                        ret = responseCellList(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_41 /*76*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_25 /*77*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_26 /*78*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_41 /*79*/:
                        ret = responseInts(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_BSR_TIMER /*81*/:
                        ret = responseInts(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /*82*/:
                        ret = responseVoid(p);
                        break;
                    case 83:
                        ret = responseInts(p);
                        break;
                    case 84:
                        ret = responseVoid(p);
                        break;
                    case 85:
                        ret = responseVoid(p);
                        break;
                    case 86:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.USER_NOT_MEMBER_OF_CUG /*87*/:
                        ret = responseSMS(p);
                        break;
                    case CallFailCause.INCOMPATIBLE_DESTINATION /*88*/:
                        ret = responseVoid(p);
                        break;
                    case 89:
                        ret = responseGmsBroadcastConfig(p);
                        break;
                    case 90:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.INVALID_TRANSIT_NW_SELECTION /*91*/:
                        ret = responseVoid(p);
                        break;
                    case 92:
                        ret = responseCdmaBroadcastConfig(p);
                        break;
                    case 93:
                        ret = responseVoid(p);
                        break;
                    case 94:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.SEMANTICALLY_INCORRECT_MESSAGE /*95*/:
                        ret = responseStrings(p);
                        break;
                    case CallFailCause.INVALID_MANDATORY_INFORMATION /*96*/:
                        ret = responseInts(p);
                        break;
                    case CallFailCause.MESSAGE_TYPE_NON_IMPLEMENTED /*97*/:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE /*98*/:
                        ret = responseStrings(p);
                        break;
                    case CallFailCause.INFORMATION_ELEMENT_NON_EXISTENT /*99*/:
                        ret = responseVoid(p);
                        break;
                    case IccRecords.EVENT_GET_ICC_RECORD_DONE /*100*/:
                        ret = responseString(p);
                        break;
                    case CallFailCause.MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE /*101*/:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.RECOVERY_ON_TIMER_EXPIRED /*102*/:
                        ret = responseVoid(p);
                        break;
                    case 103:
                        ret = responseVoid(p);
                        break;
                    case AbstractPhoneBase.EVENT_ECC_NUM /*104*/:
                        ret = responseInts(p);
                        break;
                    case AbstractPhoneBase.EVENT_GET_IMSI_DONE /*105*/:
                        ret = responseString(p);
                        break;
                    case CharacterSets.UTF_8 /*106*/:
                        ret = responseVoid(p);
                        break;
                    case 107:
                        ret = responseICC_IO(p);
                        break;
                    case AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE /*108*/:
                        ret = responseInts(p);
                        break;
                    case 109:
                        ret = responseCellInfoList(p);
                        break;
                    case 110:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.PROTOCOL_ERROR_UNSPECIFIED /*111*/:
                        ret = responseVoid(p);
                        break;
                    case 112:
                        ret = responseInts(p);
                        break;
                    case 113:
                        ret = responseSMS(p);
                        break;
                    case 114:
                        ret = responseICC_IO(p);
                        break;
                    case 115:
                        ret = responseInts(p);
                        break;
                    case 116:
                        ret = responseVoid(p);
                        break;
                    case 117:
                        ret = responseICC_IO(p);
                        break;
                    case 118:
                        ret = responseString(p);
                        break;
                    case 119:
                        ret = responseVoid(p);
                        break;
                    case AbstractPhoneBase.BUFFER_SIZE /*120*/:
                        ret = responseVoid(p);
                        break;
                    case 121:
                        ret = responseVoid(p);
                        break;
                    case 122:
                        ret = responseVoid(p);
                        break;
                    case 123:
                        ret = responseVoid(p);
                        break;
                    case 124:
                        ret = responseHardwareConfig(p);
                        break;
                    case 125:
                        ret = responseICC_IOBase64(p);
                        break;
                    case PduPart.P_Q /*128*/:
                        ret = responseVoid(p);
                        break;
                    case PduPart.P_DISPOSITION_ATTACHMENT /*129*/:
                        ret = responseVoid(p);
                        break;
                    case PduPart.P_LEVEL /*130*/:
                        ret = responseRadioCapability(p);
                        break;
                    case PduPart.P_TYPE /*131*/:
                        ret = responseRadioCapability(p);
                        break;
                    case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
                        ret = responseLceStatus(p);
                        break;
                    case PduPart.P_DEP_NAME /*133*/:
                        ret = responseLceStatus(p);
                        break;
                    case PduPart.P_DEP_FILENAME /*134*/:
                        ret = responseLceData(p);
                        break;
                    case PduPart.P_DIFFERENCES /*135*/:
                        ret = responseActivityData(p);
                        break;
                    default:
                        ret = processSolicitedEx(rr.mRequest, p);
                        break;
                }
            } catch (Throwable tr) {
                Rlog.w(RILJ_LOG_TAG, rr.serialString() + "< " + requestToString(rr.mRequest) + " exception, possible invalid RIL response", tr);
                if (rr.mResult != null) {
                    AsyncResult.forMessage(rr.mResult, null, tr);
                    rr.mResult.sendToTarget();
                }
                return rr;
            }
        }
        if (rr.mRequest == PduPart.P_DISPOSITION_ATTACHMENT) {
            riljLog("Response to RIL_REQUEST_SHUTDOWN received. Error is " + error + " Setting Radio State to Unavailable regardless of error.");
            setRadioState(RadioState.RADIO_UNAVAILABLE);
        }
        switch (rr.mRequest) {
            case RESPONSE_SOLICITED_ACK_EXP /*3*/:
            case EVENT_BLOCKING_RESPONSE_TIMEOUT /*5*/:
                if (this.mIccStatusChangedRegistrants != null) {
                    riljLog("ON enter sim puk fakeSimStatusChanged: reg count=" + this.mIccStatusChangedRegistrants.size());
                    this.mIccStatusChangedRegistrants.notifyRegistrants();
                    break;
                }
                break;
        }
        if (error != 0) {
            switch (rr.mRequest) {
                case RESPONSE_SOLICITED_ACK /*2*/:
                case RESPONSE_UNSOLICITED_ACK_EXP /*4*/:
                case CharacterSets.ISO_8859_3 /*6*/:
                case CharacterSets.ISO_8859_4 /*7*/:
                case CallFailCause.ACCESS_INFORMATION_DISCARDED /*43*/:
                    if (this.mIccStatusChangedRegistrants != null) {
                        riljLog("ON some errors fakeSimStatusChanged: reg count=" + this.mIccStatusChangedRegistrants.size());
                        this.mIccStatusChangedRegistrants.notifyRegistrants();
                        break;
                    }
                    break;
                case PduPart.P_LEVEL /*130*/:
                    if (6 == error || RESPONSE_SOLICITED_ACK == error) {
                        ret = makeStaticRadioCapability();
                        error = RESPONSE_SOLICITED;
                        break;
                    }
                case PduPart.P_DIFFERENCES /*135*/:
                    ret = new ModemActivityInfo(0, RESPONSE_SOLICITED, RESPONSE_SOLICITED, new int[EVENT_BLOCKING_RESPONSE_TIMEOUT], RESPONSE_SOLICITED, RESPONSE_SOLICITED);
                    error = RESPONSE_SOLICITED;
                    break;
            }
            if (error != 0) {
                rr.onError(error, ret);
            }
            if (error != 0 && isPlatformTwoModems()) {
                breakSocketOnError(rr.mRequest);
            }
        }
        if (error == 0) {
            if (rr.mRequest == 98) {
                riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " " + "XXXXXX");
            } else {
                riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " " + retToString(rr.mRequest, ret));
            }
            if (rr.mRequest == 11 && SystemProperties.get("ro.config.hw_wifipro_enable", "false").equals("true")) {
                SystemProperties.set("gsm.signalplus.support.tas", "true");
                SystemProperties.set("gsm.signalplus.support.xpass", "true");
                SystemProperties.set("gsm.linkplus.support.roaming", "true");
            }
            handleRequestGetImsiMessage(rr, ret, this.mContext);
            if (rr.mResult != null) {
                AsyncResult.forMessage(rr.mResult, ret, null);
                rr.mResult.sendToTarget();
            }
        }
        if (this.mEventLog != null) {
            this.mEventLog.writeOnRilSolicitedResponse(rr.mSerial, error, rr.mRequest, ret);
        }
        return rr;
    }

    private RadioCapability makeStaticRadioCapability() {
        int raf = RESPONSE_UNSOLICITED;
        String rafString = this.mContext.getResources().getString(17039465);
        if (!TextUtils.isEmpty(rafString)) {
            raf = RadioAccessFamily.rafTypeFromString(rafString);
        }
        RadioCapability rc = new RadioCapability(this.mInstanceId.intValue(), RESPONSE_SOLICITED, RESPONSE_SOLICITED, raf, "", RESPONSE_UNSOLICITED);
        riljLog("Faking RIL_REQUEST_GET_RADIO_CAPABILITY response using " + raf);
        return rc;
    }

    static String retToString(int req, Object ret) {
        if (ret == null) {
            return "";
        }
        switch (req) {
            case CharacterSets.ISO_8859_8 /*11*/:
            case RadioNVItems.RIL_NV_MIP_PROFILE_HA_SPI /*38*/:
            case RadioNVItems.RIL_NV_MIP_PROFILE_AAA_SPI /*39*/:
            case CallFailCause.MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE /*98*/:
            case 115:
            case 117:
            case 529:
                return "";
            default:
                String s;
                int length;
                StringBuilder stringBuilder;
                int i;
                int i2;
                if (ret instanceof int[]) {
                    int[] intArray = (int[]) ret;
                    length = intArray.length;
                    stringBuilder = new StringBuilder("{");
                    if (length > 0) {
                        stringBuilder.append(intArray[RESPONSE_SOLICITED]);
                        i = RESPONSE_UNSOLICITED;
                        while (i < length) {
                            i2 = i + RESPONSE_UNSOLICITED;
                            stringBuilder.append(", ").append(intArray[i]);
                            i = i2;
                        }
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (ret instanceof String[]) {
                    String[] strings = (String[]) ret;
                    length = strings.length;
                    stringBuilder = new StringBuilder("{");
                    if (length > 0) {
                        stringBuilder.append(strings[RESPONSE_SOLICITED]);
                        i = RESPONSE_UNSOLICITED;
                        while (i < length) {
                            i2 = i + RESPONSE_UNSOLICITED;
                            stringBuilder.append(", ").append(strings[i]);
                            i = i2;
                        }
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 9) {
                    ArrayList<DriverCall> calls = (ArrayList) ret;
                    stringBuilder = new StringBuilder("{");
                    for (DriverCall dc : calls) {
                        stringBuilder.append("[").append(dc).append("] ");
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 75) {
                    ArrayList<NeighboringCellInfo> cells = (ArrayList) ret;
                    stringBuilder = new StringBuilder("{");
                    for (NeighboringCellInfo cell : cells) {
                        stringBuilder.append("[").append(cell).append("] ");
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 33) {
                    CallForwardInfo[] cinfo = (CallForwardInfo[]) ret;
                    length = cinfo.length;
                    stringBuilder = new StringBuilder("{");
                    for (i2 = RESPONSE_SOLICITED; i2 < length; i2 += RESPONSE_UNSOLICITED) {
                        stringBuilder.append("[").append(cinfo[i2]).append("] ");
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 124) {
                    ArrayList<HardwareConfig> hwcfgs = (ArrayList) ret;
                    stringBuilder = new StringBuilder(" ");
                    for (HardwareConfig hwcfg : hwcfgs) {
                        stringBuilder.append("[").append(hwcfg).append("] ");
                    }
                    s = stringBuilder.toString();
                } else {
                    s = ret.toString();
                }
                return s;
        }
    }

    private void processUnsolicited(Parcel p, int type) {
        Object ret;
        int response = p.readInt();
        if (getRilVersion() >= 13 && type == RESPONSE_UNSOLICITED_ACK_EXP) {
            RILRequest rr = RILRequest.obtain(800, null);
            Message msg = this.mSender.obtainMessage(RESPONSE_SOLICITED_ACK_EXP, rr);
            acquireWakeLock(rr, RESPONSE_UNSOLICITED);
            msg.sendToTarget();
            riljLog("Unsol response received for " + responseToString(response) + " Sending ack to ril.cpp");
        }
        switch (response) {
            case CharacterSets.UCS2 /*1000*/:
                ret = responseVoid(p);
                break;
            case TelephonyEventLog.TAG_RIL_REQUEST /*1001*/:
                ret = responseVoid(p);
                break;
            case TelephonyEventLog.TAG_RIL_RESPONSE /*1002*/:
                ret = responseVoid(p);
                break;
            case TelephonyEventLog.TAG_RIL_UNSOL_RESPONSE /*1003*/:
                ret = responseString(p);
                break;
            case TelephonyEventLog.TAG_RIL_TIMEOUT_RESPONSE /*1004*/:
                ret = responseString(p);
                break;
            case ServiceStateTracker.CS_NORMAL_ENABLED /*1005*/:
                ret = responseInts(p);
                break;
            case ServiceStateTracker.CS_EMERGENCY_ENABLED /*1006*/:
                ret = responseStrings(p);
                break;
            case CallFailCause.CDMA_NOT_EMERGENCY /*1008*/:
                ret = responseString(p);
                break;
            case CallFailCause.CDMA_ACCESS_BLOCKED /*1009*/:
                ret = responseSignalStrength(p);
                break;
            case 1010:
                ret = responseDataCallList(p);
                break;
            case 1011:
                ret = responseSuppServiceNotification(p);
                break;
            case 1012:
                ret = responseVoid(p);
                break;
            case 1013:
                ret = responseString(p);
                break;
            case 1014:
                ret = responseString(p);
                break;
            case CharacterSets.UTF_16 /*1015*/:
                ret = responseInts(p);
                break;
            case 1016:
                ret = responseVoid(p);
                break;
            case 1017:
                ret = responseSimRefresh(p);
                break;
            case 1018:
                ret = responseCallRing(p);
                break;
            case 1019:
                ret = responseVoid(p);
                break;
            case 1020:
                ret = responseCdmaSms(p);
                break;
            case 1021:
                ret = responseRaw(p);
                break;
            case 1022:
                ret = responseVoid(p);
                break;
            case 1023:
                ret = responseInts(p);
                break;
            case 1024:
                ret = responseVoid(p);
                break;
            case 1025:
                ret = responseCdmaCallWaiting(p);
                break;
            case 1026:
                ret = responseInts(p);
                break;
            case 1027:
                ret = responseCdmaInformationRecord(p);
                break;
            case 1028:
                ret = responseRaw(p);
                break;
            case 1029:
                ret = responseInts(p);
                break;
            case 1030:
                ret = responseVoid(p);
                break;
            case 1031:
                ret = responseInts(p);
                break;
            case 1032:
                ret = responseInts(p);
                break;
            case 1033:
                ret = responseVoid(p);
                break;
            case 1034:
                ret = responseInts(p);
                break;
            case 1035:
                ret = responseInts(p);
                break;
            case 1036:
                ret = responseCellInfoList(p);
                break;
            case 1037:
                ret = responseVoid(p);
                break;
            case 1038:
                ret = responseInts(p);
                break;
            case 1039:
                ret = responseInts(p);
                break;
            case 1040:
                ret = responseHardwareConfig(p);
                break;
            case 1042:
                ret = responseRadioCapability(p);
                break;
            case 1043:
                ret = responseSsData(p);
                break;
            case 1044:
                ret = responseString(p);
                break;
            case 1045:
                ret = responseLceData(p);
                break;
            case 1046:
                ret = responseVoid(p);
                break;
            case 3020:
                ret = responseInts(p);
                break;
            default:
                try {
                    ret = handleUnsolicitedDefaultMessagePara(response, p);
                    break;
                } catch (Throwable tr) {
                    Rlog.e(RILJ_LOG_TAG, "Exception processing unsol response: " + response + "Exception:" + tr.toString());
                    return;
                }
        }
        SmsMessage sms;
        int length;
        switch (response) {
            case CharacterSets.UCS2 /*1000*/:
                RadioState newState = getRadioStateFromInt(p.readInt());
                unsljLogMore(response, newState.toString());
                switchToRadioState(newState);
                break;
            case TelephonyEventLog.TAG_RIL_REQUEST /*1001*/:
                unsljLog(response);
                this.mCallStateRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                break;
            case TelephonyEventLog.TAG_RIL_RESPONSE /*1002*/:
                unsljLog(response);
                this.mVoiceNetworkStateRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                break;
            case TelephonyEventLog.TAG_RIL_UNSOL_RESPONSE /*1003*/:
                unsljLog(response);
                this.mEventLog.writeRilNewSms(response);
                String[] a = new String[RESPONSE_SOLICITED_ACK];
                a[RESPONSE_UNSOLICITED] = (String) ret;
                sms = SmsMessage.newFromCMT(a);
                if (this.mGsmSmsRegistrant != null) {
                    Jlog.d(47, "JL_RIL_RESPONSE_NEW_SMS");
                    this.mGsmSmsRegistrant.notifyRegistrant(new AsyncResult(null, sms, null));
                    break;
                }
                break;
            case TelephonyEventLog.TAG_RIL_TIMEOUT_RESPONSE /*1004*/:
                unsljLogRet(response, ret);
                if (this.mSmsStatusRegistrant != null) {
                    this.mSmsStatusRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case ServiceStateTracker.CS_NORMAL_ENABLED /*1005*/:
                unsljLogRet(response, ret);
                Object smsIndex = (int[]) ret;
                length = smsIndex.length;
                if (r0 == RESPONSE_UNSOLICITED) {
                    if (this.mSmsOnSimRegistrant != null) {
                        this.mSmsOnSimRegistrant.notifyRegistrant(new AsyncResult(null, smsIndex, null));
                        break;
                    }
                }
                riljLog(" NEW_SMS_ON_SIM ERROR with wrong length " + smsIndex.length);
                break;
                break;
            case ServiceStateTracker.CS_EMERGENCY_ENABLED /*1006*/:
                String[] resp = (String[]) ret;
                length = resp.length;
                if (r0 < RESPONSE_SOLICITED_ACK) {
                    resp = new String[RESPONSE_SOLICITED_ACK];
                    resp[RESPONSE_SOLICITED] = ((String[]) ret)[RESPONSE_SOLICITED];
                    resp[RESPONSE_UNSOLICITED] = null;
                }
                unsljLogMore(response, resp[RESPONSE_SOLICITED]);
                if (this.mUSSDRegistrant != null) {
                    this.mUSSDRegistrant.notifyRegistrant(new AsyncResult(null, resp, null));
                    break;
                }
                break;
            case CallFailCause.CDMA_NOT_EMERGENCY /*1008*/:
                unsljLogRet(response, ret);
                long nitzReceiveTime = p.readLong();
                Object result = new Object[RESPONSE_SOLICITED_ACK];
                result[RESPONSE_SOLICITED] = ret;
                result[RESPONSE_UNSOLICITED] = Long.valueOf(nitzReceiveTime);
                if (!SystemProperties.getBoolean("telephony.test.ignore.nitz", RILJ_LOGV)) {
                    if (this.mNITZTimeRegistrant != null) {
                        this.mNITZTimeRegistrant.notifyRegistrant(new AsyncResult(null, result, null));
                    }
                    this.mLastNITZTimeInfo = result;
                    break;
                }
                riljLog("ignoring UNSOL_NITZ_TIME_RECEIVED");
                break;
            case CallFailCause.CDMA_ACCESS_BLOCKED /*1009*/:
                if (this.mSignalStrengthRegistrant != null) {
                    this.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1010:
                unsljLogRet(response, ret);
                this.mDataNetworkStateRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                break;
            case 1011:
                unsljLogRet(response, ret);
                if (this.mSsnRegistrant != null) {
                    this.mSsnRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1012:
                unsljLog(response);
                if (this.mCatSessionEndRegistrant != null) {
                    this.mCatSessionEndRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1013:
                unsljLog(response);
                if (this.mCatProCmdRegistrant != null) {
                    this.mCatProCmdRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1014:
                unsljLog(response);
                if (this.mCatEventRegistrant != null) {
                    this.mCatEventRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case CharacterSets.UTF_16 /*1015*/:
                unsljLogRet(response, ret);
                if (this.mCatCallSetUpRegistrant != null) {
                    this.mCatCallSetUpRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1016:
                unsljLog(response);
                if (this.mIccSmsFullRegistrant != null) {
                    this.mIccSmsFullRegistrant.notifyRegistrant();
                    break;
                }
                break;
            case 1017:
                unsljLogRet(response, ret);
                if (this.mIccRefreshRegistrants != null) {
                    this.mIccRefreshRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1018:
                unsljLogRet(response, ret);
                if (this.mRingRegistrant != null) {
                    this.mRingRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1019:
                unsljLog(response);
                if (this.mIccStatusChangedRegistrants != null) {
                    this.mIccStatusChangedRegistrants.notifyRegistrants();
                    break;
                }
                break;
            case 1020:
                unsljLog(response);
                this.mEventLog.writeRilNewSms(response);
                sms = (SmsMessage) ret;
                if (this.mCdmaSmsRegistrant != null) {
                    Jlog.d(47, "JL_RIL_RESPONSE_NEW_SMS");
                    this.mCdmaSmsRegistrant.notifyRegistrant(new AsyncResult(null, sms, null));
                    break;
                }
                break;
            case 1021:
                unsljLogvRet(response, IccUtils.bytesToHexString((byte[]) ret));
                if (this.mGsmBroadcastSmsRegistrant != null) {
                    this.mGsmBroadcastSmsRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1022:
                unsljLog(response);
                if (this.mIccSmsFullRegistrant != null) {
                    this.mIccSmsFullRegistrant.notifyRegistrant();
                    break;
                }
                break;
            case 1023:
                unsljLogvRet(response, ret);
                if (this.mRestrictedStateRegistrant != null) {
                    this.mRestrictedStateRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1024:
                unsljLog(response);
                if (this.mEmergencyCallbackModeRegistrant != null) {
                    this.mEmergencyCallbackModeRegistrant.notifyRegistrant();
                    break;
                }
                break;
            case 1025:
                unsljLogRet(response, ret);
                if (this.mCallWaitingInfoRegistrants != null) {
                    this.mCallWaitingInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1026:
                unsljLogRet(response, ret);
                if (this.mOtaProvisionRegistrants != null) {
                    this.mOtaProvisionRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1027:
                try {
                    for (CdmaInformationRecords rec : (ArrayList) ret) {
                        unsljLogRet(response, rec);
                        notifyRegistrantsCdmaInfoRec(rec);
                    }
                    break;
                } catch (ClassCastException e) {
                    Rlog.e(RILJ_LOG_TAG, "Unexpected exception casting to listInfoRecs", e);
                    break;
                }
            case 1028:
                unsljLogvRet(response, IccUtils.bytesToHexString((byte[]) ret));
                notifyUnsolOemHookResponse(ret);
                break;
            case 1029:
                unsljLogvRet(response, ret);
                if (this.mRingbackToneRegistrants != null) {
                    this.mRingbackToneRegistrants.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(((int[]) ret)[RESPONSE_SOLICITED] == RESPONSE_UNSOLICITED ? RILJ_LOGD : RILJ_LOGV), null));
                    break;
                }
                break;
            case 1030:
                unsljLogRet(response, ret);
                if (this.mResendIncallMuteRegistrants != null) {
                    this.mResendIncallMuteRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1031:
                unsljLogRet(response, ret);
                if (this.mCdmaSubscriptionChangedRegistrants != null) {
                    this.mCdmaSubscriptionChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1032:
                unsljLogRet(response, ret);
                if (this.mCdmaPrlChangedRegistrants != null) {
                    this.mCdmaPrlChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1033:
                unsljLogRet(response, ret);
                if (this.mExitEmergencyCallbackModeRegistrants != null) {
                    this.mExitEmergencyCallbackModeRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                    break;
                }
                break;
            case 1034:
                unsljLogRet(response, ret);
                riljLog("mPreferredNetworkType = " + this.mPreferredNetworkType + ", mCdmaSubscription is " + this.mCdmaSubscription);
                setCellInfoListRate(Integer.MAX_VALUE, null);
                notifyRegistrantsRilConnectionChanged(((int[]) ret)[RESPONSE_SOLICITED]);
                break;
            case 1035:
                unsljLogRet(response, ret);
                if (this.mVoiceRadioTechChangedRegistrants != null) {
                    this.mVoiceRadioTechChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1036:
                unsljLogRet(response, ret);
                if (this.mRilCellInfoListRegistrants != null) {
                    this.mRilCellInfoListRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1037:
                unsljLog(response);
                this.mImsNetworkStateChangedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                break;
            case 1038:
                unsljLogRet(response, ret);
                if (this.mSubscriptionStatusRegistrants != null) {
                    this.mSubscriptionStatusRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1039:
                unsljLogRet(response, ret);
                this.mEventLog.writeRilSrvcc(((int[]) ret)[RESPONSE_SOLICITED]);
                if (this.mSrvccStateRegistrants != null) {
                    this.mSrvccStateRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1040:
                unsljLogRet(response, ret);
                if (this.mHardwareConfigChangeRegistrants != null) {
                    this.mHardwareConfigChangeRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1042:
                unsljLogRet(response, ret);
                if (this.mPhoneRadioCapabilityChangedRegistrants != null) {
                    this.mPhoneRadioCapabilityChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1043:
                unsljLogRet(response, ret);
                if (this.mSsRegistrant != null) {
                    this.mSsRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1044:
                unsljLogRet(response, ret);
                if (this.mCatCcAlphaRegistrant != null) {
                    this.mCatCcAlphaRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1045:
                unsljLogRet(response, ret);
                if (this.mLceInfoRegistrant != null) {
                    this.mLceInfoRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1046:
                notifyUnsolRSrvccState(response, ret);
                break;
            case 3020:
                unsljLogRet(response, ret);
                notifyIccUimLockRegistrants();
                break;
            default:
                handleUnsolicitedDefaultMessage(response, ret, this.mContext);
                break;
        }
    }

    private void notifyRegistrantsRilConnectionChanged(int rilVer) {
        this.mRilVersion = rilVer;
        if (this.mRilConnectedRegistrants != null) {
            this.mRilConnectedRegistrants.notifyRegistrants(new AsyncResult(null, new Integer(rilVer), null));
        }
    }

    private Object responseInts(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[numInts];
        for (int i = RESPONSE_SOLICITED; i < numInts; i += RESPONSE_UNSOLICITED) {
            response[i] = p.readInt();
        }
        return response;
    }

    private Object responseFailCause(Parcel p) {
        LastCallFailCause failCause = new LastCallFailCause();
        failCause.causeCode = p.readInt();
        if (p.dataAvail() > 0) {
            failCause.vendorCause = p.readString();
        }
        return failCause;
    }

    private Object responseVoid(Parcel p) {
        return null;
    }

    private Object responseCallForward(Parcel p) {
        int numInfos = p.readInt();
        CallForwardInfo[] infos = new CallForwardInfo[numInfos];
        int i = RESPONSE_SOLICITED;
        while (i < numInfos) {
            infos[i] = new CallForwardInfo();
            infos[i].status = p.readInt();
            infos[i].reason = p.readInt();
            infos[i].serviceClass = p.readInt();
            infos[i].toa = p.readInt();
            infos[i].number = p.readString();
            infos[i].timeSeconds = p.readInt();
            if (RESPONSE_UNSOLICITED == infos[i].status && infos[i].number == null) {
                infos[i].number = "";
                Rlog.d(RILJ_LOG_TAG, "number is null pointer, set number to a null string");
            }
            i += RESPONSE_UNSOLICITED;
        }
        return infos;
    }

    private Object responseSuppServiceNotification(Parcel p) {
        SuppServiceNotification notification = new SuppServiceNotification();
        notification.notificationType = p.readInt();
        notification.code = p.readInt();
        notification.index = p.readInt();
        notification.type = p.readInt();
        notification.number = p.readString();
        return notification;
    }

    private Object responseCdmaSms(Parcel p) {
        return SmsMessage.newFromParcel(p);
    }

    private Object responseString(Parcel p) {
        return p.readString();
    }

    private Object responseStrings(Parcel p) {
        return p.readStringArray();
    }

    private Object responseRaw(Parcel p) {
        return p.createByteArray();
    }

    private Object responseSMS(Parcel p) {
        return new SmsResponse(p.readInt(), p.readString(), p.readInt());
    }

    private Object responseICC_IO(Parcel p) {
        return new IccIoResult(p.readInt(), p.readInt(), p.readString());
    }

    private Object responseICC_IOBase64(Parcel p) {
        int sw1 = p.readInt();
        int sw2 = p.readInt();
        String s = p.readString();
        return new IccIoResult(sw1, sw2, s != null ? Base64.decode(s, RESPONSE_SOLICITED) : (byte[]) null);
    }

    private Object responseIccCardStatus(Parcel p) {
        IccCardStatus cardStatus = new IccCardStatus();
        cardStatus.setCardState(p.readInt());
        cardStatus.setUniversalPinState(p.readInt());
        cardStatus.mGsmUmtsSubscriptionAppIndex = p.readInt();
        cardStatus.mCdmaSubscriptionAppIndex = p.readInt();
        cardStatus.mImsSubscriptionAppIndex = p.readInt();
        int numApplications = p.readInt();
        if (numApplications > 8) {
            numApplications = 8;
        }
        cardStatus.mApplications = new IccCardApplicationStatus[numApplications];
        for (int i = RESPONSE_SOLICITED; i < numApplications; i += RESPONSE_UNSOLICITED) {
            IccCardApplicationStatus appStatus = new IccCardApplicationStatus();
            appStatus.app_type = appStatus.AppTypeFromRILInt(p.readInt());
            appStatus.app_state = appStatus.AppStateFromRILInt(p.readInt());
            appStatus.perso_substate = appStatus.PersoSubstateFromRILInt(p.readInt());
            appStatus.aid = p.readString();
            appStatus.app_label = p.readString();
            appStatus.pin1_replaced = p.readInt();
            appStatus.pin1 = appStatus.PinStateFromRILInt(p.readInt());
            appStatus.pin2 = appStatus.PinStateFromRILInt(p.readInt());
            cardStatus.mApplications[i] = appStatus;
        }
        return cardStatus;
    }

    private Object responseSimRefresh(Parcel p) {
        IccRefreshResponse response = new IccRefreshResponse();
        response.refreshResult = p.readInt();
        response.efId = p.readInt();
        response.aid = p.readString();
        return response;
    }

    private Object responseCallList(Parcel p) {
        int num = p.readInt();
        ArrayList<DriverCall> response = new ArrayList(num);
        for (int i = RESPONSE_SOLICITED; i < num; i += RESPONSE_UNSOLICITED) {
            boolean z;
            DriverCall dc = new DriverCall();
            dc.state = DriverCall.stateFromCLCC(p.readInt());
            dc.index = p.readInt();
            dc.TOA = p.readInt();
            if (p.readInt() != 0) {
                z = RILJ_LOGD;
            } else {
                z = RILJ_LOGV;
            }
            dc.isMpty = z;
            if (p.readInt() != 0) {
                z = RILJ_LOGD;
            } else {
                z = RILJ_LOGV;
            }
            dc.isMT = z;
            dc.als = p.readInt();
            if (p.readInt() == 0) {
                z = RILJ_LOGV;
            } else {
                z = RILJ_LOGD;
            }
            dc.isVoice = z;
            if (p.readInt() != 0) {
                z = RILJ_LOGD;
            } else {
                z = RILJ_LOGV;
            }
            dc.isVoicePrivacy = z;
            dc.number = p.readString();
            dc.numberPresentation = DriverCall.presentationFromCLIP(p.readInt());
            dc.name = p.readString();
            dc.namePresentation = DriverCall.presentationFromCLIP(p.readInt());
            if (p.readInt() == RESPONSE_UNSOLICITED) {
                dc.uusInfo = new UUSInfo();
                dc.uusInfo.setType(p.readInt());
                dc.uusInfo.setDcs(p.readInt());
                dc.uusInfo.setUserData(p.createByteArray());
                Object[] objArr = new Object[RESPONSE_SOLICITED_ACK_EXP];
                objArr[RESPONSE_SOLICITED] = Integer.valueOf(dc.uusInfo.getType());
                objArr[RESPONSE_UNSOLICITED] = Integer.valueOf(dc.uusInfo.getDcs());
                objArr[RESPONSE_SOLICITED_ACK] = Integer.valueOf(dc.uusInfo.getUserData().length);
                riljLogv(String.format("Incoming UUS : type=%d, dcs=%d, length=%d", objArr));
                riljLogv("Incoming UUS : data (string)=" + new String(dc.uusInfo.getUserData()));
                riljLogv("Incoming UUS : data (hex): " + IccUtils.bytesToHexString(dc.uusInfo.getUserData()));
            } else {
                riljLogv("Incoming UUS : NOT present!");
            }
            dc.number = PhoneNumberUtils.stringFromStringAndTOA(dc.number, dc.TOA);
            if (SystemProperties.getBoolean("ro.hwpp.clir_ril_hide_number", RILJ_LOGV) && RESPONSE_SOLICITED_ACK == dc.numberPresentation) {
                riljLogv("ro.hwpp.clir_ril_hide_number:" + dc.numberPresentation);
                dc.number = "";
                dc.name = "";
            }
            response.add(dc);
            if (dc.isVoicePrivacy) {
                this.mVoicePrivacyOnRegistrants.notifyRegistrants();
                riljLog("InCall VoicePrivacy is enabled");
            } else {
                this.mVoicePrivacyOffRegistrants.notifyRegistrants();
                riljLog("InCall VoicePrivacy is disabled");
            }
        }
        Collections.sort(response);
        if (num == 0 && this.mTestingEmergencyCall.getAndSet(RILJ_LOGV) && this.mEmergencyCallbackModeRegistrant != null) {
            riljLog("responseCallList: call ended, testing emergency call, notify ECM Registrants");
            this.mEmergencyCallbackModeRegistrant.notifyRegistrant();
        }
        return response;
    }

    private DataCallResponse getDataCallResponse(Parcel p, int version) {
        DataCallResponse dataCall = new DataCallResponse();
        dataCall.version = version;
        String addresses;
        if (version < EVENT_BLOCKING_RESPONSE_TIMEOUT) {
            dataCall.cid = p.readInt();
            dataCall.active = p.readInt();
            dataCall.type = p.readString();
            addresses = p.readString();
            if (!TextUtils.isEmpty(addresses)) {
                dataCall.addresses = addresses.split(" ");
            }
        } else {
            dataCall.status = p.readInt();
            dataCall.suggestedRetryTime = p.readInt();
            dataCall.cid = p.readInt();
            dataCall.active = p.readInt();
            dataCall.type = p.readString();
            dataCall.ifname = p.readString();
            if (dataCall.status == DcFailCause.NONE.getErrorCode() && TextUtils.isEmpty(dataCall.ifname)) {
                throw new RuntimeException("getDataCallResponse, no ifname");
            }
            addresses = p.readString();
            if (!TextUtils.isEmpty(addresses)) {
                dataCall.addresses = addresses.split(" ");
            }
            String dnses = p.readString();
            if (!TextUtils.isEmpty(dnses)) {
                dataCall.dnses = dnses.split(" ");
            }
            String gateways = p.readString();
            if (!TextUtils.isEmpty(gateways)) {
                dataCall.gateways = gateways.split(" ");
            }
            if (version >= 10) {
                String pcscf = p.readString();
                if (!TextUtils.isEmpty(pcscf)) {
                    dataCall.pcscf = pcscf.split(" ");
                }
            }
            if (version >= 11) {
                dataCall.mtu = p.readInt();
            }
        }
        return dataCall;
    }

    private Object responseDataCallList(Parcel p) {
        int ver = p.readInt();
        int num = p.readInt();
        riljLog("responseDataCallList ver=" + ver + " num=" + num);
        ArrayList<DataCallResponse> response = new ArrayList(num);
        for (int i = RESPONSE_SOLICITED; i < num; i += RESPONSE_UNSOLICITED) {
            response.add(getDataCallResponse(p, ver));
        }
        this.mEventLog.writeRilDataCallList(response);
        return response;
    }

    private Object responseSetupDataCall(Parcel p) {
        int ver = p.readInt();
        int num = p.readInt();
        if (ver < EVENT_BLOCKING_RESPONSE_TIMEOUT) {
            DataCallResponse dataCall = new DataCallResponse();
            dataCall.version = ver;
            dataCall.cid = Integer.parseInt(p.readString());
            dataCall.ifname = p.readString();
            if (TextUtils.isEmpty(dataCall.ifname)) {
                throw new RuntimeException("RIL_REQUEST_SETUP_DATA_CALL response, no ifname");
            }
            String addresses = p.readString();
            if (!TextUtils.isEmpty(addresses)) {
                dataCall.addresses = addresses.split(" ");
            }
            if (num >= RESPONSE_UNSOLICITED_ACK_EXP) {
                String dnses = p.readString();
                riljLog("responseSetupDataCall got dnses=" + dnses);
                if (!TextUtils.isEmpty(dnses)) {
                    dataCall.dnses = dnses.split(" ");
                }
            }
            if (num >= EVENT_BLOCKING_RESPONSE_TIMEOUT) {
                String gateways = p.readString();
                riljLog("responseSetupDataCall got gateways=" + gateways);
                if (!TextUtils.isEmpty(gateways)) {
                    dataCall.gateways = gateways.split(" ");
                }
            }
            if (num < 6) {
                return dataCall;
            }
            String pcscf = p.readString();
            riljLog("responseSetupDataCall got pcscf=" + pcscf);
            if (TextUtils.isEmpty(pcscf)) {
                return dataCall;
            }
            dataCall.pcscf = pcscf.split(" ");
            return dataCall;
        } else if (num == RESPONSE_UNSOLICITED) {
            return getDataCallResponse(p, ver);
        } else {
            throw new RuntimeException("RIL_REQUEST_SETUP_DATA_CALL response expecting 1 RIL_Data_Call_response_v5 got " + num);
        }
    }

    private Object responseOperatorInfos(Parcel p) {
        String[] strings = (String[]) responseStrings(p);
        if (strings.length % RESPONSE_UNSOLICITED_ACK_EXP != 0) {
            throw new RuntimeException("RIL_REQUEST_QUERY_AVAILABLE_NETWORKS: invalid response. Got " + strings.length + " strings, expected multible of 4");
        }
        ArrayList<OperatorInfo> ret = new ArrayList(strings.length / RESPONSE_UNSOLICITED_ACK_EXP);
        for (int i = RESPONSE_SOLICITED; i < strings.length; i += RESPONSE_UNSOLICITED_ACK_EXP) {
            ret.add(new OperatorInfo(strings[i + RESPONSE_SOLICITED], strings[i + RESPONSE_UNSOLICITED], strings[i + RESPONSE_SOLICITED_ACK], strings[i + RESPONSE_SOLICITED_ACK_EXP]));
        }
        return ret;
    }

    private Object responseCellList(Parcel p) {
        int num = p.readInt();
        ArrayList<NeighboringCellInfo> response = new ArrayList();
        int radioType = ((TelephonyManager) this.mContext.getSystemService("phone")).getDataNetworkType(SubscriptionManager.getSubId(this.mInstanceId.intValue())[RESPONSE_SOLICITED]);
        if (radioType != 0) {
            for (int i = RESPONSE_SOLICITED; i < num; i += RESPONSE_UNSOLICITED) {
                response.add(new NeighboringCellInfo(p.readInt(), p.readString(), radioType));
            }
        }
        return response;
    }

    private Object responseGetPreferredNetworkType(Parcel p) {
        int[] response = (int[]) responseInts(p);
        if (response.length >= RESPONSE_UNSOLICITED) {
            this.mPreferredNetworkType = response[RESPONSE_SOLICITED];
            custSetModemProperties();
        }
        return response;
    }

    private boolean convertToLteEnableProp(int networkType) {
        if (networkType == 8 || networkType == 9 || networkType == 30 || networkType == CDMA_BROADCAST_SMS_NO_OF_SERVICE_CATEGORIES || networkType == 61 || networkType == 10 || networkType == 11 || networkType == 12) {
            return RILJ_LOGD;
        }
        return RILJ_LOGV;
    }

    private void custSetModemProperties() {
        int isSlotsSwitched = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots", RESPONSE_SOLICITED);
        if ((isSlotsSwitched != RESPONSE_UNSOLICITED || (this.mInstanceId != null && this.mInstanceId.intValue() != 0)) && (isSlotsSwitched != 0 || this.mInstanceId.intValue() != RESPONSE_UNSOLICITED)) {
            boolean lte_enabled = convertToLteEnableProp(this.mPreferredNetworkType);
            riljLog("mInstanceId = " + this.mInstanceId + ", setprop lte_enabled = " + lte_enabled);
            SystemProperties.set("persist.radio.lte_enabled", String.valueOf(lte_enabled));
        }
    }

    private Object responseGmsBroadcastConfig(Parcel p) {
        int num = p.readInt();
        ArrayList<SmsBroadcastConfigInfo> response = new ArrayList(num);
        for (int i = RESPONSE_SOLICITED; i < num; i += RESPONSE_UNSOLICITED) {
            response.add(new SmsBroadcastConfigInfo(p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt() == RESPONSE_UNSOLICITED ? RILJ_LOGD : RILJ_LOGV));
        }
        return response;
    }

    private Object responseCdmaBroadcastConfig(Parcel p) {
        int[] response;
        int numServiceCategories = p.readInt();
        int i;
        if (numServiceCategories == 0) {
            response = new int[94];
            response[RESPONSE_SOLICITED] = CDMA_BROADCAST_SMS_NO_OF_SERVICE_CATEGORIES;
            for (i = RESPONSE_UNSOLICITED; i < 94; i += RESPONSE_SOLICITED_ACK_EXP) {
                response[i + RESPONSE_SOLICITED] = i / RESPONSE_SOLICITED_ACK_EXP;
                response[i + RESPONSE_UNSOLICITED] = RESPONSE_UNSOLICITED;
                response[i + RESPONSE_SOLICITED_ACK] = RESPONSE_SOLICITED;
            }
        } else {
            int numInts = (numServiceCategories * RESPONSE_SOLICITED_ACK_EXP) + RESPONSE_UNSOLICITED;
            response = new int[numInts];
            response[RESPONSE_SOLICITED] = numServiceCategories;
            for (i = RESPONSE_UNSOLICITED; i < numInts; i += RESPONSE_UNSOLICITED) {
                response[i] = p.readInt();
            }
        }
        return response;
    }

    private Object responseSignalStrength(Parcel p) {
        return SignalStrength.makeSignalStrengthFromRilParcel(p);
    }

    private ArrayList<CdmaInformationRecords> responseCdmaInformationRecord(Parcel p) {
        int numberOfInfoRecs = p.readInt();
        ArrayList<CdmaInformationRecords> response = new ArrayList(numberOfInfoRecs);
        for (int i = RESPONSE_SOLICITED; i < numberOfInfoRecs; i += RESPONSE_UNSOLICITED) {
            response.add(new CdmaInformationRecords(p));
        }
        return response;
    }

    private Object responseCdmaCallWaiting(Parcel p) {
        CdmaCallWaitingNotification notification = new CdmaCallWaitingNotification();
        notification.number = p.readString();
        notification.numberPresentation = CdmaCallWaitingNotification.presentationFromCLIP(p.readInt());
        notification.name = p.readString();
        notification.namePresentation = notification.numberPresentation;
        notification.isPresent = p.readInt();
        notification.signalType = p.readInt();
        notification.alertPitch = p.readInt();
        notification.signal = p.readInt();
        notification.numberType = p.readInt();
        notification.numberPlan = p.readInt();
        if (notification.numberType == RESPONSE_UNSOLICITED) {
            notification.number = PhoneNumberUtils.stringFromStringAndTOA(notification.number, PduPart.P_SEC);
        }
        return notification;
    }

    private Object responseCallRing(Parcel p) {
        char[] response = new char[RESPONSE_UNSOLICITED_ACK_EXP];
        response[RESPONSE_SOLICITED] = (char) p.readInt();
        response[RESPONSE_UNSOLICITED] = (char) p.readInt();
        response[RESPONSE_SOLICITED_ACK] = (char) p.readInt();
        response[RESPONSE_SOLICITED_ACK_EXP] = (char) p.readInt();
        this.mEventLog.writeRilCallRing(response);
        return response;
    }

    private void notifyRegistrantsCdmaInfoRec(CdmaInformationRecords infoRec) {
        if (infoRec.record instanceof CdmaDisplayInfoRec) {
            if (this.mDisplayInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mDisplayInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaSignalInfoRec) {
            if (this.mSignalInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mSignalInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaNumberInfoRec) {
            if (this.mNumberInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mNumberInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaRedirectingNumberInfoRec) {
            if (this.mRedirNumInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mRedirNumInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaLineControlInfoRec) {
            if (this.mLineControlInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mLineControlInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaT53ClirInfoRec) {
            if (this.mT53ClirInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mT53ClirInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if ((infoRec.record instanceof CdmaT53AudioControlInfoRec) && this.mT53AudCntrlInfoRegistrants != null) {
            unsljLogRet(1027, infoRec.record);
            this.mT53AudCntrlInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
        }
    }

    private ArrayList<CellInfo> responseCellInfoList(Parcel p) {
        int numberOfInfoRecs = p.readInt();
        ArrayList<CellInfo> response = new ArrayList(numberOfInfoRecs);
        for (int i = RESPONSE_SOLICITED; i < numberOfInfoRecs; i += RESPONSE_UNSOLICITED) {
            response.add((CellInfo) CellInfo.CREATOR.createFromParcel(p));
        }
        return response;
    }

    private Object responseHardwareConfig(Parcel p) {
        int num = p.readInt();
        ArrayList<HardwareConfig> response = new ArrayList(num);
        for (int i = RESPONSE_SOLICITED; i < num; i += RESPONSE_UNSOLICITED) {
            HardwareConfig hw;
            int type = p.readInt();
            switch (type) {
                case RESPONSE_SOLICITED /*0*/:
                    hw = new HardwareConfig(type);
                    hw.assignModem(p.readString(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt());
                    break;
                case RESPONSE_UNSOLICITED /*1*/:
                    hw = new HardwareConfig(type);
                    hw.assignSim(p.readString(), p.readInt(), p.readString());
                    break;
                default:
                    throw new RuntimeException("RIL_REQUEST_GET_HARDWARE_CONFIG invalid hardward type:" + type);
            }
            response.add(hw);
        }
        return response;
    }

    private Object responseRadioCapability(Parcel p) {
        int version = p.readInt();
        int session = p.readInt();
        int phase = p.readInt();
        int rat = p.readInt();
        String logicModemUuid = p.readString();
        int status = p.readInt();
        riljLog("responseRadioCapability: version= " + version + ", session=" + session + ", phase=" + phase + ", rat=" + rat + ", logicModemUuid=" + logicModemUuid + ", status=" + status);
        return new RadioCapability(this.mInstanceId.intValue(), session, phase, rat, logicModemUuid, status);
    }

    private Object responseLceData(Parcel p) {
        ArrayList<Integer> capacityResponse = new ArrayList();
        int capacityDownKbps = p.readInt();
        int confidenceLevel = p.readByte();
        int lceSuspended = p.readByte();
        riljLog("LCE capacity information received: capacity=" + capacityDownKbps + " confidence=" + confidenceLevel + " lceSuspended=" + lceSuspended);
        capacityResponse.add(Integer.valueOf(capacityDownKbps));
        capacityResponse.add(Integer.valueOf(confidenceLevel));
        capacityResponse.add(Integer.valueOf(lceSuspended));
        return capacityResponse;
    }

    private Object responseLceStatus(Parcel p) {
        ArrayList<Integer> statusResponse = new ArrayList();
        int lceStatus = p.readByte();
        int actualInterval = p.readInt();
        riljLog("LCE status information received: lceStatus=" + lceStatus + " actualInterval=" + actualInterval);
        statusResponse.add(Integer.valueOf(lceStatus));
        statusResponse.add(Integer.valueOf(actualInterval));
        return statusResponse;
    }

    private Object responseActivityData(Parcel p) {
        int sleepModeTimeMs = p.readInt();
        int idleModeTimeMs = p.readInt();
        int[] txModeTimeMs = new int[EVENT_BLOCKING_RESPONSE_TIMEOUT];
        for (int i = RESPONSE_SOLICITED; i < EVENT_BLOCKING_RESPONSE_TIMEOUT; i += RESPONSE_UNSOLICITED) {
            txModeTimeMs[i] = p.readInt();
        }
        int rxModeTimeMs = p.readInt();
        riljLog("Modem activity info received: sleepModeTimeMs=" + sleepModeTimeMs + " idleModeTimeMs=" + idleModeTimeMs + " txModeTimeMs[]=" + Arrays.toString(txModeTimeMs) + " rxModeTimeMs=" + rxModeTimeMs);
        return new ModemActivityInfo(SystemClock.elapsedRealtime(), sleepModeTimeMs, idleModeTimeMs, txModeTimeMs, rxModeTimeMs, RESPONSE_SOLICITED);
    }

    static String requestToString(int request) {
        switch (request) {
            case RESPONSE_UNSOLICITED /*1*/:
                return "GET_SIM_STATUS";
            case RESPONSE_SOLICITED_ACK /*2*/:
                return "ENTER_SIM_PIN";
            case RESPONSE_SOLICITED_ACK_EXP /*3*/:
                return "ENTER_SIM_PUK";
            case RESPONSE_UNSOLICITED_ACK_EXP /*4*/:
                return "ENTER_SIM_PIN2";
            case EVENT_BLOCKING_RESPONSE_TIMEOUT /*5*/:
                return "ENTER_SIM_PUK2";
            case CharacterSets.ISO_8859_3 /*6*/:
                return "CHANGE_SIM_PIN";
            case CharacterSets.ISO_8859_4 /*7*/:
                return "CHANGE_SIM_PIN2";
            case CharacterSets.ISO_8859_5 /*8*/:
                return "ENTER_NETWORK_DEPERSONALIZATION";
            case CharacterSets.ISO_8859_6 /*9*/:
                return "GET_CURRENT_CALLS";
            case CharacterSets.ISO_8859_7 /*10*/:
                return "DIAL";
            case CharacterSets.ISO_8859_8 /*11*/:
                return "GET_IMSI";
            case CharacterSets.ISO_8859_9 /*12*/:
                return "HANGUP";
            case UserData.ASCII_CR_INDEX /*13*/:
                return "HANGUP_WAITING_OR_BACKGROUND";
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                return "HANGUP_FOREGROUND_RESUME_BACKGROUND";
            case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                return "REQUEST_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE";
            case PduHeaders.MMS_VERSION_1_0 /*16*/:
                return "CONFERENCE";
            case PduHeaders.MMS_VERSION_1_1 /*17*/:
                return "UDUB";
            case PduHeaders.MMS_VERSION_1_2 /*18*/:
                return "LAST_CALL_FAIL_CAUSE";
            case PduHeaders.MMS_VERSION_1_3 /*19*/:
                return "SIGNAL_STRENGTH";
            case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                return "VOICE_REGISTRATION_STATE";
            case SmsHeader.ELT_ID_REUSED_EXTENDED_OBJECT /*21*/:
                return "DATA_REGISTRATION_STATE";
            case CallFailCause.NUMBER_CHANGED /*22*/:
                return "OPERATOR";
            case SmsHeader.ELT_ID_OBJECT_DISTR_INDICATOR /*23*/:
                return "RADIO_POWER";
            case SmsHeader.ELT_ID_STANDARD_WVG_OBJECT /*24*/:
                return "DTMF";
            case SmsHeader.ELT_ID_CHARACTER_SIZE_WVG_OBJECT /*25*/:
                return "SEND_SMS";
            case SmsHeader.ELT_ID_EXTENDED_OBJECT_DATA_REQUEST_CMD /*26*/:
                return "SEND_SMS_EXPECT_MORE";
            case CallFailCause.CALL_FAIL_DESTINATION_OUT_OF_ORDER /*27*/:
                return "SETUP_DATA_CALL";
            case CallFailCause.INVALID_NUMBER /*28*/:
                return "SIM_IO";
            case CallFailCause.FACILITY_REJECTED /*29*/:
                return "SEND_USSD";
            case CallFailCause.STATUS_ENQUIRY /*30*/:
                return "CANCEL_USSD";
            case CDMA_BROADCAST_SMS_NO_OF_SERVICE_CATEGORIES /*31*/:
                return "GET_CLIR";
            case UserData.PRINTABLE_ASCII_MIN_INDEX /*32*/:
                return "SET_CLIR";
            case SmsHeader.ELT_ID_HYPERLINK_FORMAT_ELEMENT /*33*/:
                return "QUERY_CALL_FORWARD_STATUS";
            case CallFailCause.NO_CIRCUIT_AVAIL /*34*/:
                return "SET_CALL_FORWARD";
            case SmsHeader.ELT_ID_ENHANCED_VOICE_MAIL_INFORMATION /*35*/:
                return "QUERY_CALL_WAITING";
            case CdmaSmsAddress.SMS_SUBADDRESS_MAX /*36*/:
                return "SET_CALL_WAITING";
            case SmsHeader.ELT_ID_NATIONAL_LANGUAGE_LOCKING_SHIFT /*37*/:
                return "SMS_ACKNOWLEDGE";
            case RadioNVItems.RIL_NV_MIP_PROFILE_HA_SPI /*38*/:
                return "GET_IMEI";
            case RadioNVItems.RIL_NV_MIP_PROFILE_AAA_SPI /*39*/:
                return "GET_IMEISV";
            case RadioNVItems.RIL_NV_MIP_PROFILE_MN_HA_SS /*40*/:
                return "ANSWER";
            case CallFailCause.TEMPORARY_FAILURE /*41*/:
                return "DEACTIVATE_DATA_CALL";
            case CallFailCause.SWITCHING_CONGESTION /*42*/:
                return "QUERY_FACILITY_LOCK";
            case CallFailCause.ACCESS_INFORMATION_DISCARDED /*43*/:
                return "SET_FACILITY_LOCK";
            case CallFailCause.CHANNEL_NOT_AVAIL /*44*/:
                return "CHANGE_BARRING_PASSWORD";
            case 45:
                return "QUERY_NETWORK_SELECTION_MODE";
            case 46:
                return "SET_NETWORK_SELECTION_AUTOMATIC";
            case WspTypeDecoder.PARAMETER_ID_X_WAP_APPLICATION_ID /*47*/:
                return "SET_NETWORK_SELECTION_MANUAL";
            case 48:
                return "QUERY_AVAILABLE_NETWORKS ";
            case CallFailCause.QOS_NOT_AVAIL /*49*/:
                return "DTMF_START";
            case SmsCbConstants.MESSAGE_ID_GSMA_ALLOCATED_CHANNEL_50 /*50*/:
                return "DTMF_STOP";
            case RadioNVItems.RIL_NV_CDMA_PRL_VERSION /*51*/:
                return "BASEBAND_VERSION";
            case RadioNVItems.RIL_NV_CDMA_BC10 /*52*/:
                return "SEPARATE_CONNECTION";
            case RadioNVItems.RIL_NV_CDMA_BC14 /*53*/:
                return "SET_MUTE";
            case RadioNVItems.RIL_NV_CDMA_SO68 /*54*/:
                return "GET_MUTE";
            case RadioNVItems.RIL_NV_CDMA_SO73_COP0 /*55*/:
                return "QUERY_CLIP";
            case RadioNVItems.RIL_NV_CDMA_SO73_COP1TO7 /*56*/:
                return "LAST_DATA_CALL_FAIL_CAUSE";
            case RadioNVItems.RIL_NV_CDMA_1X_ADVANCED_ENABLED /*57*/:
                return "DATA_CALL_LIST";
            case CallFailCause.BEARER_NOT_AVAIL /*58*/:
                return "RESET_RADIO";
            case RadioNVItems.RIL_NV_CDMA_EHRPD_FORCED /*59*/:
                return "OEM_HOOK_RAW";
            case 60:
                return "OEM_HOOK_STRINGS";
            case 61:
                return "SCREEN_STATE";
            case 62:
                return "SET_SUPP_SVC_NOTIFICATION";
            case SignalToneUtil.IS95_CONST_IR_SIG_TONE_NO_TONE /*63*/:
                return "WRITE_SMS_TO_SIM";
            case CommandsInterface.SERVICE_CLASS_PACKET /*64*/:
                return "DELETE_SMS_ON_SIM";
            case HwRadarUtils.RADAR_LEVEL_A /*65*/:
                return "SET_BAND_MODE";
            case HwRadarUtils.RADAR_LEVEL_B /*66*/:
                return "QUERY_AVAILABLE_BAND_MODE";
            case HwRadarUtils.RADAR_LEVEL_C /*67*/:
                return "REQUEST_STK_GET_PROFILE";
            case HwRadarUtils.RADAR_LEVEL_D /*68*/:
                return "REQUEST_STK_SET_PROFILE";
            case CallFailCause.REQUESTED_FACILITY_NOT_IMPLEMENTED /*69*/:
                return "REQUEST_STK_SEND_ENVELOPE_COMMAND";
            case CallFailCause.ONLY_DIGITAL_INFORMATION_BEARER_AVAILABLE /*70*/:
                return "REQUEST_STK_SEND_TERMINAL_RESPONSE";
            case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_25 /*71*/:
                return "REQUEST_STK_HANDLE_CALL_SETUP_REQUESTED_FROM_SIM";
            case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /*72*/:
                return "REQUEST_EXPLICIT_CALL_TRANSFER";
            case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_41 /*73*/:
                return "REQUEST_SET_PREFERRED_NETWORK_TYPE";
            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /*74*/:
                return "REQUEST_GET_PREFERRED_NETWORK_TYPE";
            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_26 /*75*/:
                return "REQUEST_GET_NEIGHBORING_CELL_IDS";
            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_41 /*76*/:
                return "REQUEST_SET_LOCATION_UPDATES";
            case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_25 /*77*/:
                return "RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE";
            case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_26 /*78*/:
                return "RIL_REQUEST_CDMA_SET_ROAMING_PREFERENCE";
            case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_41 /*79*/:
                return "RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE";
            case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                return "RIL_REQUEST_SET_TTY_MODE";
            case RadioNVItems.RIL_NV_LTE_BSR_TIMER /*81*/:
                return "RIL_REQUEST_QUERY_TTY_MODE";
            case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /*82*/:
                return "RIL_REQUEST_CDMA_SET_PREFERRED_VOICE_PRIVACY_MODE";
            case 83:
                return "RIL_REQUEST_CDMA_QUERY_PREFERRED_VOICE_PRIVACY_MODE";
            case 84:
                return "RIL_REQUEST_CDMA_FLASH";
            case 85:
                return "RIL_REQUEST_CDMA_BURST_DTMF";
            case 86:
                return "RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY";
            case CallFailCause.USER_NOT_MEMBER_OF_CUG /*87*/:
                return "RIL_REQUEST_CDMA_SEND_SMS";
            case CallFailCause.INCOMPATIBLE_DESTINATION /*88*/:
                return "RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE";
            case 89:
                return "RIL_REQUEST_GSM_GET_BROADCAST_CONFIG";
            case 90:
                return "RIL_REQUEST_GSM_SET_BROADCAST_CONFIG";
            case CallFailCause.INVALID_TRANSIT_NW_SELECTION /*91*/:
                return "RIL_REQUEST_GSM_BROADCAST_ACTIVATION";
            case 92:
                return "RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG";
            case 93:
                return "RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG";
            case 94:
                return "RIL_REQUEST_CDMA_BROADCAST_ACTIVATION";
            case CallFailCause.SEMANTICALLY_INCORRECT_MESSAGE /*95*/:
                return "RIL_REQUEST_CDMA_SUBSCRIPTION";
            case CallFailCause.INVALID_MANDATORY_INFORMATION /*96*/:
                return "RIL_REQUEST_CDMA_WRITE_SMS_TO_RUIM";
            case CallFailCause.MESSAGE_TYPE_NON_IMPLEMENTED /*97*/:
                return "RIL_REQUEST_CDMA_DELETE_SMS_ON_RUIM";
            case CallFailCause.MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE /*98*/:
                return "RIL_REQUEST_DEVICE_IDENTITY";
            case CallFailCause.INFORMATION_ELEMENT_NON_EXISTENT /*99*/:
                return "REQUEST_EXIT_EMERGENCY_CALLBACK_MODE";
            case IccRecords.EVENT_GET_ICC_RECORD_DONE /*100*/:
                return "RIL_REQUEST_GET_SMSC_ADDRESS";
            case CallFailCause.MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE /*101*/:
                return "RIL_REQUEST_SET_SMSC_ADDRESS";
            case CallFailCause.RECOVERY_ON_TIMER_EXPIRED /*102*/:
                return "RIL_REQUEST_REPORT_SMS_MEMORY_STATUS";
            case 103:
                return "RIL_REQUEST_REPORT_STK_SERVICE_IS_RUNNING";
            case AbstractPhoneBase.EVENT_ECC_NUM /*104*/:
                return "RIL_REQUEST_CDMA_GET_SUBSCRIPTION_SOURCE";
            case AbstractPhoneBase.EVENT_GET_IMSI_DONE /*105*/:
                return "RIL_REQUEST_ISIM_AUTHENTICATION";
            case CharacterSets.UTF_8 /*106*/:
                return "RIL_REQUEST_ACKNOWLEDGE_INCOMING_GSM_SMS_WITH_PDU";
            case 107:
                return "RIL_REQUEST_STK_SEND_ENVELOPE_WITH_STATUS";
            case AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE /*108*/:
                return "RIL_REQUEST_VOICE_RADIO_TECH";
            case 109:
                return "RIL_REQUEST_GET_CELL_INFO_LIST";
            case 110:
                return "RIL_REQUEST_SET_CELL_INFO_LIST_RATE";
            case CallFailCause.PROTOCOL_ERROR_UNSPECIFIED /*111*/:
                return "RIL_REQUEST_SET_INITIAL_ATTACH_APN";
            case 112:
                return "RIL_REQUEST_IMS_REGISTRATION_STATE";
            case 113:
                return "RIL_REQUEST_IMS_SEND_SMS";
            case 114:
                return "RIL_REQUEST_SIM_TRANSMIT_APDU_BASIC";
            case 115:
                return "RIL_REQUEST_SIM_OPEN_CHANNEL";
            case 116:
                return "RIL_REQUEST_SIM_CLOSE_CHANNEL";
            case 117:
                return "RIL_REQUEST_SIM_TRANSMIT_APDU_CHANNEL";
            case 118:
                return "RIL_REQUEST_NV_READ_ITEM";
            case 119:
                return "RIL_REQUEST_NV_WRITE_ITEM";
            case AbstractPhoneBase.BUFFER_SIZE /*120*/:
                return "RIL_REQUEST_NV_WRITE_CDMA_PRL";
            case 121:
                return "RIL_REQUEST_NV_RESET_CONFIG";
            case 122:
                return "RIL_REQUEST_SET_UICC_SUBSCRIPTION";
            case 123:
                return "RIL_REQUEST_ALLOW_DATA";
            case 124:
                return "GET_HARDWARE_CONFIG";
            case 125:
                return "RIL_REQUEST_SIM_AUTHENTICATION";
            case PduPart.P_Q /*128*/:
                return "RIL_REQUEST_SET_DATA_PROFILE";
            case PduPart.P_DISPOSITION_ATTACHMENT /*129*/:
                return "RIL_REQUEST_SHUTDOWN";
            case PduPart.P_LEVEL /*130*/:
                return "RIL_REQUEST_GET_RADIO_CAPABILITY";
            case PduPart.P_TYPE /*131*/:
                return "RIL_REQUEST_SET_RADIO_CAPABILITY";
            case PduHeaders.STATUS_UNRECOGNIZED /*132*/:
                return "RIL_REQUEST_START_LCE";
            case PduPart.P_DEP_NAME /*133*/:
                return "RIL_REQUEST_STOP_LCE";
            case PduPart.P_DEP_FILENAME /*134*/:
                return "RIL_REQUEST_PULL_LCEDATA";
            case PduPart.P_DIFFERENCES /*135*/:
                return "RIL_REQUEST_GET_ACTIVITY_INFO";
            case 800:
                return "RIL_RESPONSE_ACKNOWLEDGEMENT";
            default:
                return HwTelephonyFactory.getHwTelephonyBaseManager().requestToStringEx(request);
        }
    }

    static String responseToString(int request) {
        switch (request) {
            case CharacterSets.UCS2 /*1000*/:
                return "UNSOL_RESPONSE_RADIO_STATE_CHANGED";
            case TelephonyEventLog.TAG_RIL_REQUEST /*1001*/:
                return "UNSOL_RESPONSE_CALL_STATE_CHANGED";
            case TelephonyEventLog.TAG_RIL_RESPONSE /*1002*/:
                return "UNSOL_RESPONSE_VOICE_NETWORK_STATE_CHANGED";
            case TelephonyEventLog.TAG_RIL_UNSOL_RESPONSE /*1003*/:
                return "UNSOL_RESPONSE_NEW_SMS";
            case TelephonyEventLog.TAG_RIL_TIMEOUT_RESPONSE /*1004*/:
                return "UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT";
            case ServiceStateTracker.CS_NORMAL_ENABLED /*1005*/:
                return "UNSOL_RESPONSE_NEW_SMS_ON_SIM";
            case ServiceStateTracker.CS_EMERGENCY_ENABLED /*1006*/:
                return "UNSOL_ON_USSD";
            case CallFailCause.CDMA_PREEMPTED /*1007*/:
                return "UNSOL_ON_USSD_REQUEST";
            case CallFailCause.CDMA_NOT_EMERGENCY /*1008*/:
                return "UNSOL_NITZ_TIME_RECEIVED";
            case CallFailCause.CDMA_ACCESS_BLOCKED /*1009*/:
                return "UNSOL_SIGNAL_STRENGTH";
            case 1010:
                return "UNSOL_DATA_CALL_LIST_CHANGED";
            case 1011:
                return "UNSOL_SUPP_SVC_NOTIFICATION";
            case 1012:
                return "UNSOL_STK_SESSION_END";
            case 1013:
                return "UNSOL_STK_PROACTIVE_COMMAND";
            case 1014:
                return "UNSOL_STK_EVENT_NOTIFY";
            case CharacterSets.UTF_16 /*1015*/:
                return "UNSOL_STK_CALL_SETUP";
            case 1016:
                return "UNSOL_SIM_SMS_STORAGE_FULL";
            case 1017:
                return "UNSOL_SIM_REFRESH";
            case 1018:
                return "UNSOL_CALL_RING";
            case 1019:
                return "UNSOL_RESPONSE_SIM_STATUS_CHANGED";
            case 1020:
                return "UNSOL_RESPONSE_CDMA_NEW_SMS";
            case 1021:
                return "UNSOL_RESPONSE_NEW_BROADCAST_SMS";
            case 1022:
                return "UNSOL_CDMA_RUIM_SMS_STORAGE_FULL";
            case 1023:
                return "UNSOL_RESTRICTED_STATE_CHANGED";
            case 1024:
                return "UNSOL_ENTER_EMERGENCY_CALLBACK_MODE";
            case 1025:
                return "UNSOL_CDMA_CALL_WAITING";
            case 1026:
                return "UNSOL_CDMA_OTA_PROVISION_STATUS";
            case 1027:
                return "UNSOL_CDMA_INFO_REC";
            case 1028:
                return "UNSOL_OEM_HOOK_RAW";
            case 1029:
                return "UNSOL_RINGBACK_TONE";
            case 1030:
                return "UNSOL_RESEND_INCALL_MUTE";
            case 1031:
                return "CDMA_SUBSCRIPTION_SOURCE_CHANGED";
            case 1032:
                return "UNSOL_CDMA_PRL_CHANGED";
            case 1033:
                return "UNSOL_EXIT_EMERGENCY_CALLBACK_MODE";
            case 1034:
                return "UNSOL_RIL_CONNECTED";
            case 1035:
                return "UNSOL_VOICE_RADIO_TECH_CHANGED";
            case 1036:
                return "UNSOL_CELL_INFO_LIST";
            case 1037:
                return "UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED";
            case 1038:
                return "RIL_UNSOL_UICC_SUBSCRIPTION_STATUS_CHANGED";
            case 1039:
                return "UNSOL_SRVCC_STATE_NOTIFY";
            case 1040:
                return "RIL_UNSOL_HARDWARE_CONFIG_CHANGED";
            case 1042:
                return "RIL_UNSOL_RADIO_CAPABILITY";
            case 1043:
                return "UNSOL_ON_SS";
            case 1044:
                return "UNSOL_STK_CC_ALPHA_NOTIFY";
            case 1045:
                return "UNSOL_LCE_INFO_RECV";
            case 1046:
                return "RIL_UNSOL_RSRVCC_STATE_NOTIFY";
            default:
                return HwTelephonyFactory.getHwTelephonyBaseManager().responseToStringEx(request);
        }
    }

    private void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mInstanceId != null ? " [SUB" + this.mInstanceId + "]" : ""));
    }

    private void riljLogv(String msg) {
        Rlog.v(RILJ_LOG_TAG, msg + (this.mInstanceId != null ? " [SUB" + this.mInstanceId + "]" : ""));
    }

    private void unsljLog(int response) {
        riljLog("[UNSL]< " + responseToString(response));
    }

    private void unsljLogMore(int response, String more) {
        riljLog("[UNSL]< " + responseToString(response) + " " + more);
    }

    private void unsljLogRet(int response, Object ret) {
        riljLog("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }

    private void unsljLogvRet(int response, Object ret) {
        riljLogv("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }

    private Object responseSsData(Parcel p) {
        SsData ssData = new SsData();
        ssData.serviceType = ssData.ServiceTypeFromRILInt(p.readInt());
        ssData.requestType = ssData.RequestTypeFromRILInt(p.readInt());
        ssData.teleserviceType = ssData.TeleserviceTypeFromRILInt(p.readInt());
        ssData.serviceClass = p.readInt();
        ssData.result = p.readInt();
        int num = p.readInt();
        int i;
        if (ssData.serviceType.isTypeCF() && ssData.requestType.isTypeInterrogation()) {
            ssData.cfInfo = new CallForwardInfo[num];
            for (i = RESPONSE_SOLICITED; i < num; i += RESPONSE_UNSOLICITED) {
                ssData.cfInfo[i] = new CallForwardInfo();
                ssData.cfInfo[i].status = p.readInt();
                ssData.cfInfo[i].reason = p.readInt();
                ssData.cfInfo[i].serviceClass = p.readInt();
                ssData.cfInfo[i].toa = p.readInt();
                ssData.cfInfo[i].number = p.readString();
                ssData.cfInfo[i].timeSeconds = p.readInt();
                riljLog("[SS Data] CF Info " + i + " : " + ssData.cfInfo[i]);
            }
        } else {
            ssData.ssInfo = new int[num];
            for (i = RESPONSE_SOLICITED; i < num; i += RESPONSE_UNSOLICITED) {
                ssData.ssInfo[i] = p.readInt();
                riljLog("[SS Data] SS Info " + i + " : " + ssData.ssInfo[i]);
            }
        }
        return ssData;
    }

    public void getDeviceIdentity(Message response) {
        RILRequest rr = RILRequest.obtain(98, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getCDMASubscription(Message response) {
        RILRequest rr = RILRequest.obtain(95, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setPhoneType(int phoneType) {
        riljLog("setPhoneType=" + phoneType + " old value=" + this.mPhoneType);
        this.mPhoneType = phoneType;
    }

    public void queryCdmaRoamingPreference(Message response) {
        RILRequest rr = RILRequest.obtain(79, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
        RILRequest rr = RILRequest.obtain(78, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(cdmaRoamingType);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + cdmaRoamingType);
        send(rr);
    }

    public void setCdmaSubscriptionSource(int cdmaSubscription, Message response) {
        RILRequest rr = RILRequest.obtain(77, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(cdmaSubscription);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + cdmaSubscription);
        send(rr);
    }

    public void getCdmaSubscriptionSource(Message response) {
        RILRequest rr = RILRequest.obtain(AbstractPhoneBase.EVENT_ECC_NUM, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void queryTTYMode(Message response) {
        RILRequest rr = RILRequest.obtain(81, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setTTYMode(int ttyMode, Message response) {
        RILRequest rr = RILRequest.obtain(80, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(ttyMode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + ttyMode);
        send(rr);
    }

    public void sendCDMAFeatureCode(String FeatureCode, Message response) {
        RILRequest rr = RILRequest.obtain(84, response);
        rr.mParcel.writeString(FeatureCode);
        if (FeatureCode != null) {
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + FeatureCode.replaceAll("\\d{4}$", "****"));
        } else {
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        }
        send(rr);
    }

    public void getCdmaBroadcastConfig(Message response) {
        send(RILRequest.obtain(92, response));
    }

    public void setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs, Message response) {
        int i;
        int i2;
        RILRequest rr = RILRequest.obtain(93, response);
        ArrayList<CdmaSmsBroadcastConfigInfo> processedConfigs = new ArrayList();
        int length = configs.length;
        for (i = RESPONSE_SOLICITED; i < length; i += RESPONSE_UNSOLICITED) {
            CdmaSmsBroadcastConfigInfo config = configs[i];
            for (i2 = config.getFromServiceCategory(); i2 <= config.getToServiceCategory(); i2 += RESPONSE_UNSOLICITED) {
                processedConfigs.add(new CdmaSmsBroadcastConfigInfo(i2, i2, config.getLanguage(), config.isSelected()));
            }
        }
        CdmaSmsBroadcastConfigInfo[] rilConfigs = (CdmaSmsBroadcastConfigInfo[]) processedConfigs.toArray(configs);
        rr.mParcel.writeInt(rilConfigs.length);
        for (i2 = RESPONSE_SOLICITED; i2 < rilConfigs.length; i2 += RESPONSE_UNSOLICITED) {
            rr.mParcel.writeInt(rilConfigs[i2].getFromServiceCategory());
            rr.mParcel.writeInt(rilConfigs[i2].getLanguage());
            Parcel parcel = rr.mParcel;
            if (rilConfigs[i2].isSelected()) {
                i = RESPONSE_UNSOLICITED;
            } else {
                i = RESPONSE_SOLICITED;
            }
            parcel.writeInt(i);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + rilConfigs.length + " configs : ");
        for (i2 = RESPONSE_SOLICITED; i2 < rilConfigs.length; i2 += RESPONSE_UNSOLICITED) {
            riljLog(rilConfigs[i2].toString());
        }
        send(rr);
    }

    public void setCdmaBroadcastActivation(boolean activate, Message response) {
        int i = RESPONSE_UNSOLICITED;
        RILRequest rr = RILRequest.obtain(94, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        Parcel parcel = rr.mParcel;
        if (activate) {
            i = RESPONSE_SOLICITED;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void exitEmergencyCallbackMode(Message response) {
        RILRequest rr = RILRequest.obtain(99, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void requestIsimAuthentication(String nonce, Message response) {
        RILRequest rr = RILRequest.obtain(AbstractPhoneBase.EVENT_GET_IMSI_DONE, response);
        rr.mParcel.writeString(nonce);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void requestIccSimAuthentication(int authContext, String data, String aid, Message response) {
        RILRequest rr = RILRequest.obtain(125, response);
        rr.mParcel.writeInt(authContext);
        rr.mParcel.writeString(data);
        rr.mParcel.writeString(aid);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getCellInfoList(Message result) {
        RILRequest rr = RILRequest.obtain(109, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCellInfoListRate(int rateInMillis, Message response) {
        riljLog("setCellInfoListRate: " + rateInMillis);
        RILRequest rr = RILRequest.obtain(110, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(rateInMillis);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setInitialAttachApn(String apn, String protocol, int authType, String username, String password, Message result) {
        RILRequest rr = RILRequest.obtain(CallFailCause.PROTOCOL_ERROR_UNSPECIFIED, result);
        Map<String, String> map = correctApnAuth(username, authType, password);
        username = (String) map.get("userName");
        password = (String) map.get(Carriers.PASSWORD);
        try {
            authType = Integer.parseInt((String) map.get("authType"));
        } catch (Exception e) {
            riljLog(e + "The authType is not number");
        }
        rr.mParcel.writeString(apn);
        rr.mParcel.writeString(protocol);
        rr.mParcel.writeInt(authType);
        rr.mParcel.writeString(username);
        rr.mParcel.writeString(password);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", apn:" + apn + ", protocol:" + protocol + ", authType:" + authType + ", username:" + username);
        send(rr);
    }

    public void setDataProfile(DataProfile[] dps, Message result) {
        riljLog("Set RIL_REQUEST_SET_DATA_PROFILE");
        RILRequest rr = RILRequest.obtain(PduPart.P_Q, null);
        DataProfile.toParcel(rr.mParcel, dps);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + dps + " Data Profiles : ");
        for (int i = RESPONSE_SOLICITED; i < dps.length; i += RESPONSE_UNSOLICITED) {
            riljLog(dps[i].toString());
        }
        send(rr);
    }

    public void testingEmergencyCall() {
        riljLog("testingEmergencyCall");
        this.mTestingEmergencyCall.set(RILJ_LOGD);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("RIL: " + this);
        pw.println(" mSocket=" + this.mSocket);
        pw.println(" mSenderThread=" + this.mSenderThread);
        pw.println(" mSender=" + this.mSender);
        pw.println(" mReceiverThread=" + this.mReceiverThread);
        pw.println(" mReceiver=" + this.mReceiver);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" mWakeLockTimeout=" + this.mWakeLockTimeout);
        synchronized (this.mRequestList) {
            synchronized (this.mWakeLock) {
                pw.println(" mWakeLockCount=" + this.mWakeLockCount);
            }
            int count = this.mRequestList.size();
            pw.println(" mRequestList count=" + count);
            for (int i = RESPONSE_SOLICITED; i < count; i += RESPONSE_UNSOLICITED) {
                RILRequest rr = (RILRequest) this.mRequestList.valueAt(i);
                pw.println("  [" + rr.mSerial + "] " + requestToString(rr.mRequest));
            }
        }
        pw.println(" mLastNITZTimeInfo=" + Arrays.toString(this.mLastNITZTimeInfo));
        pw.println(" mTestingEmergencyCall=" + this.mTestingEmergencyCall.get());
    }

    public void iccOpenLogicalChannel(String AID, Message response) {
        RILRequest rr = RILRequest.obtain(115, response);
        rr.mParcel.writeString(AID);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void iccCloseLogicalChannel(int channel, Message response) {
        RILRequest rr = RILRequest.obtain(116, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(channel);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void iccTransmitApduLogicalChannel(int channel, int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
        if (channel <= 0) {
            throw new RuntimeException("Invalid channel in iccTransmitApduLogicalChannel: " + channel);
        }
        iccTransmitApduHelper(117, channel, cla, instruction, p1, p2, p3, data, response);
    }

    public void iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
        iccTransmitApduHelper(114, RESPONSE_SOLICITED, cla, instruction, p1, p2, p3, data, response);
    }

    private void iccTransmitApduHelper(int rilCommand, int channel, int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
        RILRequest rr = RILRequest.obtain(rilCommand, response);
        rr.mParcel.writeInt(channel);
        rr.mParcel.writeInt(cla);
        rr.mParcel.writeInt(instruction);
        rr.mParcel.writeInt(p1);
        rr.mParcel.writeInt(p2);
        rr.mParcel.writeInt(p3);
        rr.mParcel.writeString(data);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void nvReadItem(int itemID, Message response) {
        RILRequest rr = RILRequest.obtain(118, response);
        rr.mParcel.writeInt(itemID);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ' ' + itemID);
        send(rr);
    }

    public void nvWriteItem(int itemID, String itemValue, Message response) {
        RILRequest rr = RILRequest.obtain(119, response);
        rr.mParcel.writeInt(itemID);
        rr.mParcel.writeString(itemValue);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ' ' + itemID + ": " + itemValue);
        send(rr);
    }

    public void nvWriteCdmaPrl(byte[] preferredRoamingList, Message response) {
        RILRequest rr = RILRequest.obtain(AbstractPhoneBase.BUFFER_SIZE, response);
        rr.mParcel.writeByteArray(preferredRoamingList);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " (" + preferredRoamingList.length + " bytes)");
        send(rr);
    }

    public void nvResetConfig(int resetType, Message response) {
        RILRequest rr = RILRequest.obtain(121, response);
        rr.mParcel.writeInt(RESPONSE_UNSOLICITED);
        rr.mParcel.writeInt(resetType);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ' ' + resetType);
        send(rr);
    }

    public void setRadioCapability(RadioCapability rc, Message response) {
        RILRequest rr = RILRequest.obtain(PduPart.P_TYPE, response);
        rr.mParcel.writeInt(rc.getVersion());
        rr.mParcel.writeInt(rc.getSession());
        rr.mParcel.writeInt(rc.getPhase());
        rr.mParcel.writeInt(rc.getRadioAccessFamily());
        rr.mParcel.writeString(rc.getLogicalModemUuid());
        rr.mParcel.writeInt(rc.getStatus());
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + rc.toString());
        send(rr);
    }

    public void getRadioCapability(Message response) {
        RILRequest rr = RILRequest.obtain(PduPart.P_LEVEL, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void startLceService(int reportIntervalMs, boolean pullMode, Message response) {
        RILRequest rr = RILRequest.obtain(PduHeaders.STATUS_UNRECOGNIZED, response);
        rr.mParcel.writeInt(RESPONSE_SOLICITED_ACK);
        rr.mParcel.writeInt(reportIntervalMs);
        rr.mParcel.writeInt(pullMode ? RESPONSE_UNSOLICITED : RESPONSE_SOLICITED);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void stopLceService(Message response) {
        RILRequest rr = RILRequest.obtain(PduPart.P_DEP_NAME, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void pullLceData(Message response) {
        RILRequest rr = RILRequest.obtain(PduPart.P_DEP_FILENAME, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getModemActivityInfo(Message response) {
        RILRequest rr = RILRequest.obtain(PduPart.P_DIFFERENCES, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
        Message msg = this.mSender.obtainMessage(EVENT_BLOCKING_RESPONSE_TIMEOUT);
        msg.obj = null;
        msg.arg1 = rr.mSerial;
        this.mSender.sendMessageDelayed(msg, 2000);
    }

    private boolean isQcUnsolOemHookResp(ByteBuffer oemHookResponse) {
        if (oemHookResponse.capacity() < this.mHeaderSize) {
            riljLog("RIL_UNSOL_OEM_HOOK_RAW data size is " + oemHookResponse.capacity());
            return RILJ_LOGV;
        }
        byte[] oemIdBytes = new byte[OEM_IDENTIFIER.length()];
        oemHookResponse.get(oemIdBytes);
        String oemIdString = new String(oemIdBytes, Charset.forName("UTF-8"));
        riljLog("Oem ID in RIL_UNSOL_OEM_HOOK_RAW is " + oemIdString);
        if (OEM_IDENTIFIER.equals(oemIdString)) {
            return RILJ_LOGD;
        }
        return RILJ_LOGV;
    }

    private void processUnsolOemhookResponse(ByteBuffer oemHookResponse) {
        int responseId = oemHookResponse.getInt();
        riljLog("Response ID in RIL_UNSOL_OEM_HOOK_RAW is " + responseId);
        int responseSize = oemHookResponse.getInt();
        if (responseSize < 0) {
            riljLog("Response Size is Invalid " + responseSize);
            return;
        }
        byte[] responseData = new byte[responseSize];
        if (oemHookResponse.remaining() == responseSize) {
            oemHookResponse.get(responseData, RESPONSE_SOLICITED, responseSize);
            switch (responseId) {
                case 525308:
                    riljLog("QCRIL_EVT_HOOK_UNSOL_MODEM_CAPABILITY = mInstanceId" + this.mInstanceId);
                    notifyModemCap(responseData, this.mInstanceId);
                    break;
                case 525341:
                    riljLog("OEMHOOK_EVT_HOOK_UNSOL_RAT_RAC_CHANGED = mInstanceId" + this.mInstanceId);
                    sendRacChangeBroadcast(responseData);
                    break;
                case 598029:
                    notifyVpStatus(responseData);
                    break;
                case 598032:
                    riljLog("QCRIL_EVT_HOOK_UNSOL_HW_MODEM_GENERIC_IND = mInstanceId" + this.mInstanceId);
                    notifyAntOrMaxTxPowerInfo(responseData);
                    break;
                case 598035:
                    riljLog("QCRIL_EVT_HOOK_UNSOL_HW_RF_BAND_INFO = mInstanceId" + this.mInstanceId);
                    notifyBandClassInfo(responseData);
                    break;
                case 598044:
                    riljLog("received QCRILHOOK_UNSOL_HW_REPORT_BUFFER buffer is :" + IccUtils.bytesToHexString(responseData));
                    processHWBufferUnsolicited(responseData);
                    break;
            }
            return;
        }
        riljLog("Response Size(" + responseSize + ") doesnot match remaining bytes(" + oemHookResponse.remaining() + ") in the buffer. So, don't process further");
    }

    private void sendResultMessage() {
        if (this.mResultMessage != null) {
            Rlog.d(RILJ_LOG_TAG, "[2Cards]send Switch Slot Result!");
            AsyncResult.forMessage(this.mResultMessage);
            this.mResultMessage.sendToTarget();
            this.mResultMessage = null;
        }
    }

    private void breakSocketOnError(int msgId) {
        if (2094 == msgId) {
            Rlog.i(RILJ_LOG_TAG, "[2Cards]set shouldBreakRilSocket true on Error!");
            this.shouldBreakRilSocket = RILJ_LOGD;
        }
    }

    protected void notifyVpStatus(byte[] data) {
        int len = data.length;
        riljLog("notifyVpStatus: len = " + len);
        if (RESPONSE_UNSOLICITED == len) {
            this.mReportVpStatusRegistrants.notifyRegistrants(new AsyncResult(null, data, null));
        }
    }

    protected void notifyModemCap(byte[] data, Integer phoneId) {
        this.mModemCapRegistrants.notifyRegistrants(new AsyncResult(null, new UnsolOemHookBuffer(phoneId.intValue(), data), null));
        Rlog.d(RILJ_LOG_TAG, "MODEM_CAPABILITY on phone=" + phoneId + " notified to registrants");
    }

    private void notifyUnsolOemHookResponse(Object ret) {
        ByteBuffer oemHookResponse = ByteBuffer.wrap((byte[]) ret);
        oemHookResponse.order(ByteOrder.nativeOrder());
        if (isQcUnsolOemHookResp(oemHookResponse)) {
            Rlog.d(RILJ_LOG_TAG, "OEM ID check Passed");
            processUnsolOemhookResponse(oemHookResponse);
        } else if (this.mUnsolOemHookRawRegistrant != null) {
            Rlog.d(RILJ_LOG_TAG, "External OEM message, to be notified");
            this.mUnsolOemHookRawRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
        }
    }

    private void notifyUnsolRSrvccState(int response, Object ret) {
        unsljLogRet(response, ret);
        if (this.mRSrvccStateRegistrants != null) {
            this.mRSrvccStateRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
        }
    }

    private void updateScreenStateOnRadioAvailable() {
        int i;
        boolean z = RILJ_LOGD;
        this.mDefaultDisplayState = this.mDefaultDisplay.getState();
        if (this.mDefaultDisplayState == RESPONSE_SOLICITED_ACK || this.mIsDevicePlugged) {
            i = RESPONSE_UNSOLICITED;
        } else {
            i = RESPONSE_SOLICITED;
        }
        this.mRadioScreenState = i;
        riljLog("onRadioAvailable defaultDisplayState: " + this.mDefaultDisplayState + ", isDevicePlugged: " + this.mIsDevicePlugged);
        if (this.mRadioScreenState != RESPONSE_UNSOLICITED) {
            z = RILJ_LOGV;
        }
        sendScreenState(z);
    }
}
