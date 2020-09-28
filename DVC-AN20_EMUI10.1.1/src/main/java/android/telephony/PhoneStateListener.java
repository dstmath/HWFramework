package android.telephony;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.emergency.EmergencyNumber;
import android.telephony.ims.ImsReasonInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.IPhoneStateListener;
import com.android.internal.util.FunctionalUtils;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class PhoneStateListener {
    private static final boolean DBG = false;
    public static final int LISTEN_ACTIVE_DATA_SUBSCRIPTION_ID_CHANGE = 4194304;
    @SystemApi
    public static final int LISTEN_CALL_ATTRIBUTES_CHANGED = 67108864;
    @SystemApi
    public static final int LISTEN_CALL_DISCONNECT_CAUSES = 33554432;
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
    public static final int LISTEN_EMERGENCY_NUMBER_LIST = 16777216;
    @SystemApi
    public static final int LISTEN_IMS_CALL_DISCONNECT_CAUSES = 134217728;
    public static final int LISTEN_MESSAGE_WAITING_INDICATOR = 4;
    public static final int LISTEN_NONE = 0;
    @Deprecated
    public static final int LISTEN_OEM_HOOK_RAW_EVENT = 32768;
    public static final int LISTEN_OTASP_CHANGED = 512;
    public static final int LISTEN_PHONE_CAPABILITY_CHANGE = 2097152;
    public static final int LISTEN_PHYSICAL_CHANNEL_CONFIGURATION = 1048576;
    @SystemApi
    public static final int LISTEN_PRECISE_CALL_STATE = 2048;
    @SystemApi
    public static final int LISTEN_PRECISE_DATA_CONNECTION_STATE = 4096;
    @SystemApi
    public static final int LISTEN_RADIO_POWER_STATE_CHANGED = 8388608;
    public static final int LISTEN_SERVICE_STATE = 1;
    @Deprecated
    public static final int LISTEN_SIGNAL_STRENGTH = 2;
    public static final int LISTEN_SIGNAL_STRENGTHS = 256;
    @SystemApi
    public static final int LISTEN_SRVCC_STATE_CHANGED = 16384;
    public static final int LISTEN_USER_MOBILE_DATA_STATE = 524288;
    @SystemApi
    public static final int LISTEN_VOICE_ACTIVATION_STATE = 131072;
    private static final String LOG_TAG = "PhoneStateListener";
    @UnsupportedAppUsage
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public final IPhoneStateListener callback;
    @UnsupportedAppUsage
    protected Integer mSubId;

    public PhoneStateListener() {
        this((Integer) null, Looper.myLooper());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
    public PhoneStateListener(Looper looper) {
        this((Integer) null, looper);
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
    public PhoneStateListener(Integer subId) {
        this(subId, Looper.myLooper());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
    public PhoneStateListener(Integer subId, Looper looper) {
        this(subId, new HandlerExecutor(new Handler(looper)));
    }

    public PhoneStateListener(Executor executor) {
        this((Integer) null, executor);
    }

    private PhoneStateListener(Integer subId, Executor e) {
        if (e != null) {
            this.mSubId = subId;
            this.callback = new IPhoneStateListenerStub(this, e);
            return;
        }
        throw new IllegalArgumentException("PhoneStateListener Executor must be non-null");
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

    public void onCallStateChanged(int state, String phoneNumber) {
    }

    public void onDataConnectionStateChanged(int state) {
    }

    public void onDataConnectionStateChanged(int state, int networkType) {
    }

    public void onDataActivity(int direction) {
    }

    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
    }

    @UnsupportedAppUsage
    public void onOtaspChanged(int otaspMode) {
    }

    public void onCellInfoChanged(List<CellInfo> list) {
    }

    @SystemApi
    public void onPreciseCallStateChanged(PreciseCallState callState) {
    }

    @SystemApi
    public void onCallDisconnectCauseChanged(int disconnectCause, int preciseDisconnectCause) {
    }

    @SystemApi
    public void onImsCallDisconnectCauseChanged(ImsReasonInfo imsReasonInfo) {
    }

    @SystemApi
    public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState dataConnectionState) {
    }

    @UnsupportedAppUsage
    public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) {
    }

    @SystemApi
    public void onSrvccStateChanged(int srvccState) {
    }

    @SystemApi
    public void onVoiceActivationStateChanged(int state) {
    }

    public void onDataActivationStateChanged(int state) {
    }

    public void onUserMobileDataStateChanged(boolean enabled) {
    }

    public void onPhysicalChannelConfigurationChanged(List<PhysicalChannelConfig> list) {
    }

    public void onEmergencyNumberListChanged(Map<Integer, List<EmergencyNumber>> map) {
    }

    @UnsupportedAppUsage
    public void onOemHookRawEvent(byte[] rawData) {
    }

    public void onPhoneCapabilityChanged(PhoneCapability capability) {
    }

    public void onActiveDataSubscriptionIdChanged(int subId) {
    }

    @SystemApi
    public void onCallAttributesChanged(CallAttributes callAttributes) {
    }

    @SystemApi
    public void onRadioPowerStateChanged(int state) {
    }

    public void onCarrierNetworkChange(boolean active) {
    }

    /* access modifiers changed from: private */
    public static class IPhoneStateListenerStub extends IPhoneStateListener.Stub {
        private Executor mExecutor;
        private WeakReference<PhoneStateListener> mPhoneStateListenerWeakRef;

        IPhoneStateListenerStub(PhoneStateListener phoneStateListener, Executor executor) {
            this.mPhoneStateListenerWeakRef = new WeakReference<>(phoneStateListener);
            this.mExecutor = executor;
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, serviceState) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$uC5syhzl229gIpaK7Jfs__OCJxQ */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ ServiceState f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onServiceStateChanged$1$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onServiceStateChanged$1$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, ServiceState serviceState) throws Exception {
            this.mExecutor.execute(new Runnable(serviceState) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$nrGqSRBJrc3_EwotCDNwfKeizIo */
                private final /* synthetic */ ServiceState f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onServiceStateChanged$0(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onSignalStrengthChanged(int asu) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, asu) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$M39is_Zyt8D7Camw2NS4EGTDns */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onSignalStrengthChanged$3$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onSignalStrengthChanged$3$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int asu) throws Exception {
            this.mExecutor.execute(new Runnable(asu) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$5Jsdvem6pUpdVwRdm8IbDhvuv8 */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onSignalStrengthChanged$2(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, mwi) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$okPCYOx4UxYuvUHlM2iS425QGIg */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ boolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onMessageWaitingIndicatorChanged$5$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onMessageWaitingIndicatorChanged$5$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, boolean mwi) throws Exception {
            this.mExecutor.execute(new Runnable(mwi) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$TqrkuLPlaG_ucU7VbLS4tnf8hG8 */
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onMessageWaitingIndicatorChanged$4(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, cfi) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$1M3m0i6211i2YjWyTDT7l0bJm3I */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ boolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onCallForwardingIndicatorChanged$7$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onCallForwardingIndicatorChanged$7$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, boolean cfi) throws Exception {
            this.mExecutor.execute(new Runnable(cfi) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$WYWtWHdkZDxBd9anjoxyZozPWHc */
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onCallForwardingIndicatorChanged$6(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCellLocationChanged(Bundle bundle) {
            CellLocation location = CellLocation.newFromBundle(bundle);
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, location) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$Hbn6eZxY2p3rjOfStodI04A8E8 */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ CellLocation f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onCellLocationChanged$9$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onCellLocationChanged$9$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, CellLocation location) throws Exception {
            this.mExecutor.execute(new Runnable(location) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$2cMrwdqnKBpixpApeIX38rmRLak */
                private final /* synthetic */ CellLocation f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onCellLocationChanged$8(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, state, incomingNumber) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$oDAZqs8paeefe_3k_uRKV5plQW4 */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ String f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onCallStateChanged$11$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2, this.f$3);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onCallStateChanged$11$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int state, String incomingNumber) throws Exception {
            this.mExecutor.execute(new Runnable(state, incomingNumber) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$6czWSGzxct0CXPVO54T0aq05qls */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onCallStateChanged$10(PhoneStateListener.this, this.f$1, this.f$2);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onDataConnectionStateChanged(int state, int networkType) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, state, networkType) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$2VMO21pFQNJN3kpn6vQN1zPFEU */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onDataConnectionStateChanged$13$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2, this.f$3);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onDataConnectionStateChanged$13$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int state, int networkType) throws Exception {
            this.mExecutor.execute(new Runnable(state, networkType) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$dUc3j82sK9P9Zpaq91n9bk_Rpc */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onDataConnectionStateChanged$12(PhoneStateListener.this, this.f$1, this.f$2);
                }
            });
        }

        static /* synthetic */ void lambda$onDataConnectionStateChanged$12(PhoneStateListener psl, int state, int networkType) {
            psl.onDataConnectionStateChanged(state, networkType);
            psl.onDataConnectionStateChanged(state);
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onDataActivity(int direction) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, direction) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$XyayAGWQZC2dNjwr697SfSGBBOc */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onDataActivity$15$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onDataActivity$15$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int direction) throws Exception {
            this.mExecutor.execute(new Runnable(direction) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$JalixlMNdjktPsNntP_JT9pymhs */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onDataActivity$14(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, signalStrength) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$aysbwPqxcLV_5w6LP0TzZu2Dew */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ SignalStrength f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onSignalStrengthsChanged$17$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onSignalStrengthsChanged$17$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, SignalStrength signalStrength) throws Exception {
            this.mExecutor.execute(new Runnable(signalStrength) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$0s34qsuHFsa43jUHrTkD62ni6Ds */
                private final /* synthetic */ SignalStrength f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onSignalStrengthsChanged$16(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onOtaspChanged(int otaspMode) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, otaspMode) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$i4r8mBqOfCy4bnbF_JG7ujDXEOQ */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onOtaspChanged$19$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onOtaspChanged$19$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int otaspMode) throws Exception {
            this.mExecutor.execute(new Runnable(otaspMode) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$H1CbxYUcdxs1WggP_RRULTY01K8 */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onOtaspChanged$18(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, cellInfo) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$yvQnAlFGg5EWDG2vcA9X4xnalA */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ List f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onCellInfoChanged$21$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onCellInfoChanged$21$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, List cellInfo) throws Exception {
            this.mExecutor.execute(new Runnable(cellInfo) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$Q2A8FgYlU8_D6PD78tThGut_rTc */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onCellInfoChanged$20(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onPreciseCallStateChanged(PreciseCallState callState) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, callState) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$bELzxgwsPigyVKYkAXBO2BjcSm8 */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ PreciseCallState f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onPreciseCallStateChanged$23$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onPreciseCallStateChanged$23$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, PreciseCallState callState) throws Exception {
            this.mExecutor.execute(new Runnable(callState) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$4NHt5Shg_DHVT1IxfcQLHP5j0 */
                private final /* synthetic */ PreciseCallState f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onPreciseCallStateChanged$22(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCallDisconnectCauseChanged(int disconnectCause, int preciseDisconnectCause) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, disconnectCause, preciseDisconnectCause) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$icX71zgNszuMfnDaCmahcqWacFM */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onCallDisconnectCauseChanged$25$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2, this.f$3);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onCallDisconnectCauseChanged$25$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int disconnectCause, int preciseDisconnectCause) throws Exception {
            this.mExecutor.execute(new Runnable(disconnectCause, preciseDisconnectCause) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$hxq77a5O_MUfoptHg15ipzFvMkI */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onCallDisconnectCauseChanged$24(PhoneStateListener.this, this.f$1, this.f$2);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState dataConnectionState) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, dataConnectionState) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$RC2x2ijetApQrLa4QakzMBjh_k */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ PreciseDataConnectionState f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onPreciseDataConnectionStateChanged$27$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onPreciseDataConnectionStateChanged$27$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, PreciseDataConnectionState dataConnectionState) throws Exception {
            this.mExecutor.execute(new Runnable(dataConnectionState) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$HEcWnJ1WRb0wLERu2qoMIZDfjY */
                private final /* synthetic */ PreciseDataConnectionState f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onPreciseDataConnectionStateChanged$26(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, dcRtInfo) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$OfwFKKtcQHRmtv70FCopw6FDAAU */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ DataConnectionRealTimeInfo f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onDataConnectionRealTimeInfoChanged$29$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onDataConnectionRealTimeInfoChanged$29$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, DataConnectionRealTimeInfo dcRtInfo) throws Exception {
            this.mExecutor.execute(new Runnable(dcRtInfo) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$IU278K5QbmReFmbpcNVAvVlhFI */
                private final /* synthetic */ DataConnectionRealTimeInfo f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onDataConnectionRealTimeInfoChanged$28(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onSrvccStateChanged(int state) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, state) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$nR7W5ox6SCgPxtH9IRcENwKeFI4 */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onSrvccStateChanged$31$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onSrvccStateChanged$31$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int state) throws Exception {
            this.mExecutor.execute(new Runnable(state) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$ygzOWFRiY4sZQ4WYUPIefqgiGvM */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onSrvccStateChanged$30(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onVoiceActivationStateChanged(int activationState) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, activationState) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$5rF2IFj8mrb7uZc0HMKiuCodUn0 */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onVoiceActivationStateChanged$33$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onVoiceActivationStateChanged$33$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int activationState) throws Exception {
            this.mExecutor.execute(new Runnable(activationState) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$ytK7my_uXPo_oQ7AytfnekGEbU */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onVoiceActivationStateChanged$32(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onDataActivationStateChanged(int activationState) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, activationState) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$t2gWJ_jA36kAdNXSmlzw85aUtM */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onDataActivationStateChanged$35$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onDataActivationStateChanged$35$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int activationState) throws Exception {
            this.mExecutor.execute(new Runnable(activationState) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$W65ui1dCCcJnQa7gon1I7Bz7Sk */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onDataActivationStateChanged$34(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onUserMobileDataStateChanged(boolean enabled) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, enabled) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$5uu05j4ojTh9mEHkNynQqQRGM */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ boolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onUserMobileDataStateChanged$37$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onUserMobileDataStateChanged$37$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, boolean enabled) throws Exception {
            this.mExecutor.execute(new Runnable(enabled) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$5Uf5OZWCyPD0lZtySzbYw18FWhU */
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onUserMobileDataStateChanged$36(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onOemHookRawEvent(byte[] rawData) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, rawData) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$jNtyZYh5ZAuvyDZA_6f30zhW_dI */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ byte[] f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onOemHookRawEvent$39$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onOemHookRawEvent$39$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, byte[] rawData) throws Exception {
            this.mExecutor.execute(new Runnable(rawData) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$jclAV5yU3RtV94suRvvhafvGuhw */
                private final /* synthetic */ byte[] f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onOemHookRawEvent$38(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCarrierNetworkChange(boolean active) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, active) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$YY3srkIkMm8vTSFJZHoiKzUUrGs */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ boolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onCarrierNetworkChange$41$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onCarrierNetworkChange$41$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, boolean active) throws Exception {
            this.mExecutor.execute(new Runnable(active) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$jlNX9JiqGSNg9W49vDcKucKdeCI */
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onCarrierNetworkChange$40(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onPhysicalChannelConfigurationChanged(List<PhysicalChannelConfig> configs) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, configs) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$OIAjnTzp_YIf6Y7jPFABi9BXZvs */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ List f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onPhysicalChannelConfigurationChanged$43$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onPhysicalChannelConfigurationChanged$43$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, List configs) throws Exception {
            this.mExecutor.execute(new Runnable(configs) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$nMiL2eSbUjYsMAZ8joz_n4dLz0 */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onPhysicalChannelConfigurationChanged$42(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onEmergencyNumberListChanged(Map emergencyNumberList) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, emergencyNumberList) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$d9DVwzLraeX80tegF_wEzf_k2FI */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ Map f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onEmergencyNumberListChanged$45$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onEmergencyNumberListChanged$45$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, Map emergencyNumberList) throws Exception {
            this.mExecutor.execute(new Runnable(emergencyNumberList) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$jGjqFMdpjbsKaUErqJEeOALEGo */
                private final /* synthetic */ Map f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onEmergencyNumberListChanged$44(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onPhoneCapabilityChanged(PhoneCapability capability) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, capability) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$CiOzgf6ys4EwlCYOVUsuz9YQ5c */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ PhoneCapability f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onPhoneCapabilityChanged$47$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onPhoneCapabilityChanged$47$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, PhoneCapability capability) throws Exception {
            this.mExecutor.execute(new Runnable(capability) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$lHL69WZlO89JjNC1LLvFWp2OuKY */
                private final /* synthetic */ PhoneCapability f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onPhoneCapabilityChanged$46(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onRadioPowerStateChanged(int state) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, state) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$TYOBpOfoS3xjFssrzOJyHTelndw */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onRadioPowerStateChanged$49$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onRadioPowerStateChanged$49$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int state) throws Exception {
            this.mExecutor.execute(new Runnable(state) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$bI97h5HTIYvguXIcngwUrpGxrw */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onRadioPowerStateChanged$48(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onCallAttributesChanged(CallAttributes callAttributes) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, callAttributes) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$Q_Cpm8aB8qYt8lGxD5PXek_4bA */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ CallAttributes f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onCallAttributesChanged$51$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onCallAttributesChanged$51$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, CallAttributes callAttributes) throws Exception {
            this.mExecutor.execute(new Runnable(callAttributes) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$5t7yF_frkRH7MdItRlwmP00irsM */
                private final /* synthetic */ CallAttributes f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onCallAttributesChanged$50(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onActiveDataSubIdChanged(int subId) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, subId) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$ipH9N0fJiGE9EBJHahQeXcCZXzo */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onActiveDataSubIdChanged$53$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onActiveDataSubIdChanged$53$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, int subId) throws Exception {
            this.mExecutor.execute(new Runnable(subId) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$nnG75RvQ1_1KZGJk1ySeCH1JJRg */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onActiveDataSubIdChanged$52(PhoneStateListener.this, this.f$1);
                }
            });
        }

        @Override // com.android.internal.telephony.IPhoneStateListener
        public void onImsCallDisconnectCauseChanged(ImsReasonInfo disconnectCause) {
            PhoneStateListener psl = this.mPhoneStateListenerWeakRef.get();
            if (psl != null) {
                Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(psl, disconnectCause) {
                    /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$Bzok3Q_pjLC0O4ulkDfbWru0v6w */
                    private final /* synthetic */ PhoneStateListener f$1;
                    private final /* synthetic */ ImsReasonInfo f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
                    public final void runOrThrow() {
                        PhoneStateListener.IPhoneStateListenerStub.this.lambda$onImsCallDisconnectCauseChanged$55$PhoneStateListener$IPhoneStateListenerStub(this.f$1, this.f$2);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onImsCallDisconnectCauseChanged$55$PhoneStateListener$IPhoneStateListenerStub(PhoneStateListener psl, ImsReasonInfo disconnectCause) throws Exception {
            this.mExecutor.execute(new Runnable(disconnectCause) {
                /* class android.telephony.$$Lambda$PhoneStateListener$IPhoneStateListenerStub$eYTgM6ABgThWqEatVha4ZuIpI0A */
                private final /* synthetic */ ImsReasonInfo f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhoneStateListener.IPhoneStateListenerStub.lambda$onImsCallDisconnectCauseChanged$54(PhoneStateListener.this, this.f$1);
                }
            });
        }
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
