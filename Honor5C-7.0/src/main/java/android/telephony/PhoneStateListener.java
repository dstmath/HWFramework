package android.telephony;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.IPhoneStateListener;
import com.android.internal.telephony.IPhoneStateListener.Stub;
import java.lang.ref.WeakReference;
import java.util.List;

public class PhoneStateListener {
    private static final boolean DBG = false;
    public static final int LISTEN_CALL_FORWARDING_INDICATOR = 8;
    public static final int LISTEN_CALL_STATE = 32;
    public static final int LISTEN_CARRIER_NETWORK_CHANGE = 65536;
    public static final int LISTEN_CELL_INFO = 1024;
    public static final int LISTEN_CELL_LOCATION = 16;
    public static final int LISTEN_DATA_ACTIVITY = 128;
    @Deprecated
    public static final int LISTEN_DATA_CONNECTION_REAL_TIME_INFO = 8192;
    public static final int LISTEN_DATA_CONNECTION_STATE = 64;
    public static final int LISTEN_MESSAGE_WAITING_INDICATOR = 4;
    public static final int LISTEN_NONE = 0;
    public static final int LISTEN_OEM_HOOK_RAW_EVENT = 32768;
    public static final int LISTEN_OTASP_CHANGED = 512;
    public static final int LISTEN_PRECISE_CALL_STATE = 2048;
    public static final int LISTEN_PRECISE_DATA_CONNECTION_STATE = 4096;
    public static final int LISTEN_SERVICE_STATE = 1;
    @Deprecated
    public static final int LISTEN_SIGNAL_STRENGTH = 2;
    public static final int LISTEN_SIGNAL_STRENGTHS = 256;
    public static final int LISTEN_VOLTE_STATE = 16384;
    private static final String LOG_TAG = "PhoneStateListener";
    IPhoneStateListener callback;
    private final Handler mHandler;
    protected int mSubId;

    /* renamed from: android.telephony.PhoneStateListener.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            PhoneStateListener phoneStateListener;
            switch (msg.what) {
                case PhoneStateListener.LISTEN_SERVICE_STATE /*1*/:
                    PhoneStateListener.this.onServiceStateChanged((ServiceState) msg.obj);
                case PhoneStateListener.LISTEN_SIGNAL_STRENGTH /*2*/:
                    PhoneStateListener.this.onSignalStrengthChanged(msg.arg1);
                case PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR /*4*/:
                    phoneStateListener = PhoneStateListener.this;
                    if (msg.arg1 == 0) {
                        z = PhoneStateListener.DBG;
                    }
                    phoneStateListener.onMessageWaitingIndicatorChanged(z);
                case PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR /*8*/:
                    phoneStateListener = PhoneStateListener.this;
                    if (msg.arg1 == 0) {
                        z = PhoneStateListener.DBG;
                    }
                    phoneStateListener.onCallForwardingIndicatorChanged(z);
                case PhoneStateListener.LISTEN_CELL_LOCATION /*16*/:
                    PhoneStateListener.this.onCellLocationChanged((CellLocation) msg.obj);
                case PhoneStateListener.LISTEN_CALL_STATE /*32*/:
                    PhoneStateListener.this.onCallStateChanged(msg.arg1, (String) msg.obj);
                case PhoneStateListener.LISTEN_DATA_CONNECTION_STATE /*64*/:
                    PhoneStateListener.this.onDataConnectionStateChanged(msg.arg1, msg.arg2);
                    PhoneStateListener.this.onDataConnectionStateChanged(msg.arg1);
                case PhoneStateListener.LISTEN_DATA_ACTIVITY /*128*/:
                    PhoneStateListener.this.onDataActivity(msg.arg1);
                case PhoneStateListener.LISTEN_SIGNAL_STRENGTHS /*256*/:
                    PhoneStateListener.this.onSignalStrengthsChanged((SignalStrength) msg.obj);
                case PhoneStateListener.LISTEN_OTASP_CHANGED /*512*/:
                    PhoneStateListener.this.onOtaspChanged(msg.arg1);
                case PhoneStateListener.LISTEN_CELL_INFO /*1024*/:
                    PhoneStateListener.this.onCellInfoChanged((List) msg.obj);
                case PhoneStateListener.LISTEN_PRECISE_CALL_STATE /*2048*/:
                    PhoneStateListener.this.onPreciseCallStateChanged((PreciseCallState) msg.obj);
                case PhoneStateListener.LISTEN_PRECISE_DATA_CONNECTION_STATE /*4096*/:
                    PhoneStateListener.this.onPreciseDataConnectionStateChanged((PreciseDataConnectionState) msg.obj);
                case PhoneStateListener.LISTEN_DATA_CONNECTION_REAL_TIME_INFO /*8192*/:
                    PhoneStateListener.this.onDataConnectionRealTimeInfoChanged((DataConnectionRealTimeInfo) msg.obj);
                case PhoneStateListener.LISTEN_VOLTE_STATE /*16384*/:
                    PhoneStateListener.this.onVoLteServiceStateChanged((VoLteServiceState) msg.obj);
                case PhoneStateListener.LISTEN_OEM_HOOK_RAW_EVENT /*32768*/:
                    PhoneStateListener.this.onOemHookRawEvent((byte[]) msg.obj);
                case PhoneStateListener.LISTEN_CARRIER_NETWORK_CHANGE /*65536*/:
                    PhoneStateListener.this.onCarrierNetworkChange(((Boolean) msg.obj).booleanValue());
                default:
            }
        }
    }

    private static class IPhoneStateListenerStub extends Stub {
        private WeakReference<PhoneStateListener> mPhoneStateListenerWeakRef;

        public IPhoneStateListenerStub(PhoneStateListener phoneStateListener) {
            this.mPhoneStateListenerWeakRef = new WeakReference(phoneStateListener);
        }

        private void send(int what, int arg1, int arg2, Object obj) {
            PhoneStateListener listener = (PhoneStateListener) this.mPhoneStateListenerWeakRef.get();
            if (listener != null) {
                Message.obtain(listener.mHandler, what, arg1, arg2, obj).sendToTarget();
            }
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            send(PhoneStateListener.LISTEN_SERVICE_STATE, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, serviceState);
        }

        public void onSignalStrengthChanged(int asu) {
            send(PhoneStateListener.LISTEN_SIGNAL_STRENGTH, asu, PhoneStateListener.LISTEN_NONE, null);
        }

        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            int i;
            if (mwi) {
                i = PhoneStateListener.LISTEN_SERVICE_STATE;
            } else {
                i = PhoneStateListener.LISTEN_NONE;
            }
            send(PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR, i, PhoneStateListener.LISTEN_NONE, null);
        }

        public void onCallForwardingIndicatorChanged(boolean cfi) {
            int i;
            if (cfi) {
                i = PhoneStateListener.LISTEN_SERVICE_STATE;
            } else {
                i = PhoneStateListener.LISTEN_NONE;
            }
            send(PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR, i, PhoneStateListener.LISTEN_NONE, null);
        }

        public void onCellLocationChanged(Bundle bundle) {
            send(PhoneStateListener.LISTEN_CELL_LOCATION, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, CellLocation.newFromBundle(bundle));
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            send(PhoneStateListener.LISTEN_CALL_STATE, state, PhoneStateListener.LISTEN_NONE, incomingNumber);
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            send(PhoneStateListener.LISTEN_DATA_CONNECTION_STATE, state, networkType, null);
        }

        public void onDataActivity(int direction) {
            send(PhoneStateListener.LISTEN_DATA_ACTIVITY, direction, PhoneStateListener.LISTEN_NONE, null);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            send(PhoneStateListener.LISTEN_SIGNAL_STRENGTHS, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, signalStrength);
        }

        public void onOtaspChanged(int otaspMode) {
            send(PhoneStateListener.LISTEN_OTASP_CHANGED, otaspMode, PhoneStateListener.LISTEN_NONE, null);
        }

        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            send(PhoneStateListener.LISTEN_CELL_INFO, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, cellInfo);
        }

        public void onPreciseCallStateChanged(PreciseCallState callState) {
            send(PhoneStateListener.LISTEN_PRECISE_CALL_STATE, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, callState);
        }

        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState dataConnectionState) {
            send(PhoneStateListener.LISTEN_PRECISE_DATA_CONNECTION_STATE, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, dataConnectionState);
        }

        public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) {
            send(PhoneStateListener.LISTEN_DATA_CONNECTION_REAL_TIME_INFO, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, dcRtInfo);
        }

        public void onVoLteServiceStateChanged(VoLteServiceState lteState) {
            send(PhoneStateListener.LISTEN_VOLTE_STATE, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, lteState);
        }

        public void onOemHookRawEvent(byte[] rawData) {
            send(PhoneStateListener.LISTEN_OEM_HOOK_RAW_EVENT, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, rawData);
        }

        public void onCarrierNetworkChange(boolean active) {
            send(PhoneStateListener.LISTEN_CARRIER_NETWORK_CHANGE, PhoneStateListener.LISTEN_NONE, PhoneStateListener.LISTEN_NONE, Boolean.valueOf(active));
        }
    }

    public PhoneStateListener() {
        this(HwBootFail.STAGE_BOOT_SUCCESS, Looper.myLooper());
    }

    public PhoneStateListener(Looper looper) {
        this(HwBootFail.STAGE_BOOT_SUCCESS, looper);
    }

    public PhoneStateListener(int subId) {
        this(subId, Looper.myLooper());
    }

    public PhoneStateListener(int subId, Looper looper) {
        this.mSubId = -1;
        this.callback = new IPhoneStateListenerStub(this);
        this.mSubId = subId;
        this.mHandler = new AnonymousClass1(looper);
    }

    public void onServiceStateChanged(ServiceState serviceState) {
    }

    @Deprecated
    public void onSignalStrengthChanged(int asu) {
    }

    public void onMessageWaitingIndicatorChanged(boolean mwi) {
    }

    public void onCallForwardingIndicatorChanged(boolean cfi) {
    }

    public void onCellLocationChanged(CellLocation location) {
    }

    public void onCallStateChanged(int state, String incomingNumber) {
    }

    public void onDataConnectionStateChanged(int state) {
    }

    public void onDataConnectionStateChanged(int state, int networkType) {
    }

    public void onDataActivity(int direction) {
    }

    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
    }

    public void onOtaspChanged(int otaspMode) {
    }

    public void onCellInfoChanged(List<CellInfo> list) {
    }

    public void onPreciseCallStateChanged(PreciseCallState callState) {
    }

    public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState dataConnectionState) {
    }

    public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) {
    }

    public void onVoLteServiceStateChanged(VoLteServiceState stateInfo) {
    }

    public void onOemHookRawEvent(byte[] rawData) {
    }

    public void onCarrierNetworkChange(boolean active) {
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
