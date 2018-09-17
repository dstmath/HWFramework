package android.telephony;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
    public static final int LISTEN_DATA_ACTIVATION_STATE = 262144;
    public static final int LISTEN_DATA_ACTIVITY = 128;
    @Deprecated
    public static final int LISTEN_DATA_CONNECTION_REAL_TIME_INFO = 8192;
    public static final int LISTEN_DATA_CONNECTION_STATE = 64;
    public static final int LISTEN_MESSAGE_WAITING_INDICATOR = 4;
    public static final int LISTEN_NONE = 0;
    @Deprecated
    public static final int LISTEN_OEM_HOOK_RAW_EVENT = 32768;
    public static final int LISTEN_OTASP_CHANGED = 512;
    public static final int LISTEN_PRECISE_CALL_STATE = 2048;
    public static final int LISTEN_PRECISE_DATA_CONNECTION_STATE = 4096;
    public static final int LISTEN_SERVICE_STATE = 1;
    @Deprecated
    public static final int LISTEN_SIGNAL_STRENGTH = 2;
    public static final int LISTEN_SIGNAL_STRENGTHS = 256;
    public static final int LISTEN_VOICE_ACTIVATION_STATE = 131072;
    public static final int LISTEN_VOLTE_STATE = 16384;
    private static final String LOG_TAG = "PhoneStateListener";
    IPhoneStateListener callback;
    private final Handler mHandler;
    protected Integer mSubId;

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
            send(1, 0, 0, serviceState);
        }

        public void onSignalStrengthChanged(int asu) {
            send(2, asu, 0, null);
        }

        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            int i;
            if (mwi) {
                i = 1;
            } else {
                i = 0;
            }
            send(4, i, 0, null);
        }

        public void onCallForwardingIndicatorChanged(boolean cfi) {
            int i;
            if (cfi) {
                i = 1;
            } else {
                i = 0;
            }
            send(8, i, 0, null);
        }

        public void onCellLocationChanged(Bundle bundle) {
            CellLocation location;
            int slotid = bundle.getInt("SubId");
            if (Integer.MAX_VALUE == slotid) {
                location = CellLocation.newFromBundle(bundle);
            } else {
                location = CellLocation.newFromBundle(bundle, slotid);
            }
            send(16, 0, 0, location);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            send(32, state, 0, incomingNumber);
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            send(64, state, networkType, null);
        }

        public void onDataActivity(int direction) {
            send(128, direction, 0, null);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            send(256, 0, 0, signalStrength);
        }

        public void onOtaspChanged(int otaspMode) {
            send(512, otaspMode, 0, null);
        }

        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            send(1024, 0, 0, cellInfo);
        }

        public void onPreciseCallStateChanged(PreciseCallState callState) {
            send(2048, 0, 0, callState);
        }

        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState dataConnectionState) {
            send(4096, 0, 0, dataConnectionState);
        }

        public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) {
            send(8192, 0, 0, dcRtInfo);
        }

        public void onVoLteServiceStateChanged(VoLteServiceState lteState) {
            send(16384, 0, 0, lteState);
        }

        public void onVoiceActivationStateChanged(int activationState) {
            send(131072, 0, 0, Integer.valueOf(activationState));
        }

        public void onDataActivationStateChanged(int activationState) {
            send(262144, 0, 0, Integer.valueOf(activationState));
        }

        public void onOemHookRawEvent(byte[] rawData) {
            send(32768, 0, 0, rawData);
        }

        public void onCarrierNetworkChange(boolean active) {
            send(65536, 0, 0, Boolean.valueOf(active));
        }
    }

    public PhoneStateListener() {
        this(null, Looper.myLooper());
    }

    public PhoneStateListener(Looper looper) {
        this(null, looper);
    }

    public PhoneStateListener(Integer subId) {
        this(subId, Looper.myLooper());
    }

    public PhoneStateListener(Integer subId, Looper looper) {
        this.callback = new IPhoneStateListenerStub(this);
        this.mSubId = subId;
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                boolean z = true;
                PhoneStateListener phoneStateListener;
                switch (msg.what) {
                    case 1:
                        PhoneStateListener.this.onServiceStateChanged((ServiceState) msg.obj);
                        return;
                    case 2:
                        PhoneStateListener.this.onSignalStrengthChanged(msg.arg1);
                        return;
                    case 4:
                        phoneStateListener = PhoneStateListener.this;
                        if (msg.arg1 == 0) {
                            z = false;
                        }
                        phoneStateListener.onMessageWaitingIndicatorChanged(z);
                        return;
                    case 8:
                        phoneStateListener = PhoneStateListener.this;
                        if (msg.arg1 == 0) {
                            z = false;
                        }
                        phoneStateListener.onCallForwardingIndicatorChanged(z);
                        return;
                    case 16:
                        PhoneStateListener.this.onCellLocationChanged((CellLocation) msg.obj);
                        return;
                    case 32:
                        PhoneStateListener.this.onCallStateChanged(msg.arg1, (String) msg.obj);
                        return;
                    case 64:
                        PhoneStateListener.this.onDataConnectionStateChanged(msg.arg1, msg.arg2);
                        PhoneStateListener.this.onDataConnectionStateChanged(msg.arg1);
                        return;
                    case 128:
                        PhoneStateListener.this.onDataActivity(msg.arg1);
                        return;
                    case 256:
                        PhoneStateListener.this.onSignalStrengthsChanged((SignalStrength) msg.obj);
                        return;
                    case 512:
                        PhoneStateListener.this.onOtaspChanged(msg.arg1);
                        return;
                    case 1024:
                        PhoneStateListener.this.onCellInfoChanged((List) msg.obj);
                        return;
                    case 2048:
                        PhoneStateListener.this.onPreciseCallStateChanged((PreciseCallState) msg.obj);
                        return;
                    case 4096:
                        PhoneStateListener.this.onPreciseDataConnectionStateChanged((PreciseDataConnectionState) msg.obj);
                        return;
                    case 8192:
                        PhoneStateListener.this.onDataConnectionRealTimeInfoChanged((DataConnectionRealTimeInfo) msg.obj);
                        return;
                    case 16384:
                        PhoneStateListener.this.onVoLteServiceStateChanged((VoLteServiceState) msg.obj);
                        return;
                    case 32768:
                        PhoneStateListener.this.onOemHookRawEvent((byte[]) msg.obj);
                        return;
                    case 65536:
                        PhoneStateListener.this.onCarrierNetworkChange(((Boolean) msg.obj).booleanValue());
                        return;
                    case 131072:
                        PhoneStateListener.this.onVoiceActivationStateChanged(((Integer) msg.obj).intValue());
                        return;
                    case 262144:
                        PhoneStateListener.this.onDataActivationStateChanged(((Integer) msg.obj).intValue());
                        return;
                    default:
                        return;
                }
            }
        };
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

    public void onVoiceActivationStateChanged(int state) {
    }

    public void onDataActivationStateChanged(int state) {
    }

    public void onOemHookRawEvent(byte[] rawData) {
    }

    public void onCarrierNetworkChange(boolean active) {
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
