package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.CallQuality;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneCapability;
import android.telephony.PhysicalChannelConfig;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.telephony.ims.ImsReasonInfo;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import java.util.List;

public class DefaultPhoneNotifier implements PhoneNotifier {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "DefaultPhoneNotifier";
    @UnsupportedAppUsage
    protected ITelephonyRegistry mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyPhoneState(Phone sender) {
        Call ringingCall = sender.getRingingCall();
        int subId = sender.getSubId();
        int phoneId = sender.getPhoneId();
        String incomingNumber = PhoneConfigurationManager.SSSS;
        if (!(ringingCall == null || ringingCall.getEarliestConnection() == null)) {
            incomingNumber = ringingCall.getEarliestConnection().getAddress();
        }
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCallStateForPhoneId(phoneId, subId, PhoneConstantConversions.convertCallState(sender.getState()), incomingNumber);
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyServiceState(Phone sender) {
        ServiceState ss = sender.getServiceState();
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        Rlog.i(LOG_TAG, "nofityServiceState: mRegistry=" + this.mRegistry + " ss=" + ss + " sender=" + sender + " phondId=" + phoneId + " subId=" + subId);
        if (ss == null) {
            Rlog.i(LOG_TAG, "nofityServiceState: ss is null, and it will create new instance of ServiceState");
            ss = new ServiceState();
            ss.setStateOutOfService();
        }
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyServiceStateForPhoneId(phoneId, subId, ss);
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifySignalStrength(Phone sender) {
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifySignalStrengthForPhoneId(phoneId, subId, sender.getSignalStrength());
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyMessageWaitingChanged(Phone sender) {
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyMessageWaitingChangedForPhoneId(phoneId, subId, sender.getMessageWaitingIndicator());
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyCallForwardingChanged(Phone sender) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                Rlog.i(LOG_TAG, "notifyCallForwardingChanged: subId=" + subId + ", isCFActive=" + sender.getCallForwardingIndicator());
                this.mRegistry.notifyCallForwardingChangedForSubscriber(subId, sender.getCallForwardingIndicator());
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyDataActivity(Phone sender) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyDataActivityForSubscriber(subId, convertDataActivityState(sender.getDataActivityState()));
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyDataConnection(Phone sender, String apnType, PhoneConstants.DataState state) {
        doNotifyDataConnection(sender, apnType, state);
    }

    private void doNotifyDataConnection(Phone sender, String apnType, PhoneConstants.DataState state) {
        NetworkCapabilities networkCapabilities;
        LinkProperties linkProperties;
        PhoneConstants.DataState state2;
        int i;
        int subId = sender.getSubId();
        int phoneId = sender.getPhoneId();
        long defaultDataSubscriptionId = (long) SubscriptionManager.getDefaultDataSubscriptionId();
        if (!HwTelephonyFactory.getHwPhoneManager().checkApnShouldNotify(subId, apnType, state)) {
            log("doNotifyDataConnection, ignore redundant notify for " + apnType);
            return;
        }
        TelephonyManager telephony = TelephonyManager.getDefault();
        if (state == PhoneConstants.DataState.CONNECTED) {
            LinkProperties linkProperties2 = sender.getLinkProperties(apnType);
            NetworkCapabilities networkCapabilities2 = sender.getNetworkCapabilities(apnType);
            if (linkProperties2 == null || linkProperties2.getInterfaceName() == null || linkProperties2.getInterfaceName().equals(PhoneConfigurationManager.SSSS)) {
                state2 = PhoneConstants.DataState.DISCONNECTED;
                linkProperties = linkProperties2;
                networkCapabilities = networkCapabilities2;
            } else {
                state2 = state;
                linkProperties = linkProperties2;
                networkCapabilities = networkCapabilities2;
            }
        } else {
            state2 = state;
            linkProperties = null;
            networkCapabilities = null;
        }
        ServiceState ss = sender.getServiceState();
        boolean roaming = ss != null ? ss.getDataRoaming() : false;
        try {
            if (this.mRegistry != null) {
                ITelephonyRegistry iTelephonyRegistry = this.mRegistry;
                int convertDataState = PhoneConstantConversions.convertDataState(state2);
                boolean isDataAllowed = sender.isDataAllowed(ApnSetting.getApnTypesBitmaskFromString(apnType));
                String activeApnHost = sender.getActiveApnHost(apnType);
                if (telephony != null) {
                    try {
                        i = telephony.getDataNetworkType(subId);
                    } catch (RemoteException e) {
                        return;
                    }
                } else {
                    i = 0;
                }
                try {
                    iTelephonyRegistry.notifyDataConnectionForSubscriber(phoneId, subId, convertDataState, isDataAllowed, activeApnHost, apnType, linkProperties, networkCapabilities, i, roaming);
                } catch (RemoteException e2) {
                }
            }
        } catch (RemoteException e3) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyDataConnectionFailed(Phone sender, String apnType) {
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyDataConnectionFailedForSubscriber(sender.getPhoneId(), sender.getSubId(), apnType);
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyCellLocation(Phone sender, CellLocation cl) {
        int subId = sender.getSubId();
        Bundle data = new Bundle();
        cl.fillInNotifierBundle(data);
        if (sender.getCellLocation() != null) {
            sender.getCellLocation().fillInNotifierBundle(data);
        }
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCellLocationForSubscriber(subId, data);
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyCellInfo(Phone sender, List<CellInfo> cellInfo) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCellInfoForSubscriber(subId, cellInfo);
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyPhysicalChannelConfiguration(Phone sender, List<PhysicalChannelConfig> configs) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyPhysicalChannelConfigurationForSubscriber(subId, configs);
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyOtaspChanged(Phone sender, int otaspMode) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyOtaspChanged(subId, otaspMode);
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyPreciseCallState(Phone sender) {
        Call ringingCall = sender.getRingingCall();
        Call foregroundCall = sender.getForegroundCall();
        Call backgroundCall = sender.getBackgroundCall();
        if (ringingCall != null && foregroundCall != null && backgroundCall != null) {
            try {
                this.mRegistry.notifyPreciseCallState(sender.getPhoneId(), sender.getSubId(), convertPreciseCallState(ringingCall.getState()), convertPreciseCallState(foregroundCall.getState()), convertPreciseCallState(backgroundCall.getState()));
            } catch (RemoteException e) {
            }
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyDisconnectCause(Phone sender, int cause, int preciseCause) {
        try {
            this.mRegistry.notifyDisconnectCause(sender.getPhoneId(), sender.getSubId(), cause, preciseCause);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyImsDisconnectCause(Phone sender, ImsReasonInfo imsReasonInfo) {
        try {
            this.mRegistry.notifyImsDisconnectCause(sender.getSubId(), imsReasonInfo);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyPreciseDataConnectionFailed(Phone sender, String apnType, String apn, int failCause) {
        try {
            this.mRegistry.notifyPreciseDataConnectionFailed(sender.getPhoneId(), sender.getSubId(), apnType, apn, failCause);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifySrvccStateChanged(Phone sender, int state) {
        try {
            this.mRegistry.notifySrvccStateChanged(sender.getSubId(), state);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyDataActivationStateChanged(Phone sender, int activationState) {
        try {
            this.mRegistry.notifySimActivationStateChangedForPhoneId(sender.getPhoneId(), sender.getSubId(), 1, activationState);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyVoiceActivationStateChanged(Phone sender, int activationState) {
        try {
            this.mRegistry.notifySimActivationStateChangedForPhoneId(sender.getPhoneId(), sender.getSubId(), 0, activationState);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyUserMobileDataStateChanged(Phone sender, boolean state) {
        try {
            this.mRegistry.notifyUserMobileDataStateChangedForPhoneId(sender.getPhoneId(), sender.getSubId(), state);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyOemHookRawEventForSubscriber(Phone sender, byte[] rawData) {
        try {
            this.mRegistry.notifyOemHookRawEventForSubscriber(sender.getPhoneId(), sender.getSubId(), rawData);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyPhoneCapabilityChanged(PhoneCapability capability) {
        try {
            this.mRegistry.notifyPhoneCapabilityChanged(capability);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyRadioPowerStateChanged(Phone sender, int state) {
        try {
            this.mRegistry.notifyRadioPowerStateChanged(sender.getPhoneId(), sender.getSubId(), state);
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyEmergencyNumberList(Phone sender) {
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyEmergencyNumberList(sender.getPhoneId(), sender.getSubId());
            }
        } catch (RemoteException e) {
        }
    }

    @Override // com.android.internal.telephony.PhoneNotifier
    public void notifyCallQualityChanged(Phone sender, CallQuality callQuality, int callNetworkType) {
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCallQualityChanged(callQuality, sender.getPhoneId(), sender.getSubId(), callNetworkType);
            }
        } catch (RemoteException e) {
        }
    }

    public static int convertDataActivityState(PhoneInternalInterface.DataActivityState state) {
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$PhoneInternalInterface$DataActivityState[state.ordinal()];
        if (i == 1) {
            return 1;
        }
        if (i == 2) {
            return 2;
        }
        if (i == 3) {
            return 3;
        }
        if (i != 4) {
            return 0;
        }
        return 4;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.DefaultPhoneNotifier$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$PhoneInternalInterface$DataActivityState = new int[PhoneInternalInterface.DataActivityState.values().length];

        static {
            $SwitchMap$com$android$internal$telephony$Call$State = new int[Call.State.values().length];
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.ACTIVE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.HOLDING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.DIALING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.ALERTING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.INCOMING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.WAITING.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.DISCONNECTED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$Call$State[Call.State.DISCONNECTING.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneInternalInterface$DataActivityState[PhoneInternalInterface.DataActivityState.DATAIN.ordinal()] = 1;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneInternalInterface$DataActivityState[PhoneInternalInterface.DataActivityState.DATAOUT.ordinal()] = 2;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneInternalInterface$DataActivityState[PhoneInternalInterface.DataActivityState.DATAINANDOUT.ordinal()] = 3;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneInternalInterface$DataActivityState[PhoneInternalInterface.DataActivityState.DORMANT.ordinal()] = 4;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    public static int convertPreciseCallState(Call.State state) {
        switch (state) {
            case ACTIVE:
                return 1;
            case HOLDING:
                return 2;
            case DIALING:
                return 3;
            case ALERTING:
                return 4;
            case INCOMING:
                return 5;
            case WAITING:
                return 6;
            case DISCONNECTED:
                return 7;
            case DISCONNECTING:
                return 8;
            default:
                return 0;
        }
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
