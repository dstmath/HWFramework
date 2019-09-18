package com.android.internal.telephony;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.CellInfo;
import android.telephony.PhysicalChannelConfig;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import java.util.List;

public class DefaultPhoneNotifier implements PhoneNotifier {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "DefaultPhoneNotifier";
    protected ITelephonyRegistry mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));

    public void notifyPhoneState(Phone sender) {
        Call ringingCall = sender.getRingingCall();
        int subId = sender.getSubId();
        int phoneId = sender.getPhoneId();
        String incomingNumber = "";
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

    public void notifyServiceState(Phone sender) {
        ServiceState ss = sender.getServiceState();
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        Rlog.d(LOG_TAG, "nofityServiceState: mRegistry=" + this.mRegistry + " ss=" + ss + " sender=" + sender + " phondId=" + phoneId + " subId=" + subId);
        if (ss == null) {
            Rlog.d(LOG_TAG, "nofityServiceState: ss is null, and it will create new instance of ServiceState");
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

    public void notifyCallForwardingChanged(Phone sender) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                Rlog.d(LOG_TAG, "notifyCallForwardingChanged: subId=" + subId + ", isCFActive=" + sender.getCallForwardingIndicator());
                this.mRegistry.notifyCallForwardingChangedForSubscriber(subId, sender.getCallForwardingIndicator());
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyDataActivity(Phone sender) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyDataActivityForSubscriber(subId, convertDataActivityState(sender.getDataActivityState()));
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyDataConnection(Phone sender, String reason, String apnType, PhoneConstants.DataState state) {
        doNotifyDataConnection(sender, reason, apnType, state);
    }

    private void doNotifyDataConnection(Phone sender, String reason, String apnType, PhoneConstants.DataState state) {
        boolean roaming;
        int i;
        Phone phone = sender;
        String str = apnType;
        PhoneConstants.DataState dataState = state;
        int subId = sender.getSubId();
        long dds = (long) SubscriptionManager.getDefaultDataSubscriptionId();
        if (!HwTelephonyFactory.getHwPhoneManager().checkApnShouldNotify(subId, str, dataState)) {
            log("doNotifyDataConnection, ignore redundant notify for " + str);
            return;
        }
        TelephonyManager telephony = TelephonyManager.getDefault();
        LinkProperties linkProperties = null;
        NetworkCapabilities networkCapabilities = null;
        if (dataState == PhoneConstants.DataState.CONNECTED) {
            linkProperties = phone.getLinkProperties(str);
            networkCapabilities = phone.getNetworkCapabilities(str);
            if (linkProperties == null || linkProperties.getInterfaceName() == null || linkProperties.getInterfaceName().equals("")) {
                dataState = PhoneConstants.DataState.DISCONNECTED;
            }
        }
        PhoneConstants.DataState state2 = dataState;
        LinkProperties linkProperties2 = linkProperties;
        NetworkCapabilities networkCapabilities2 = networkCapabilities;
        ServiceState ss = sender.getServiceState();
        if (ss != null) {
            roaming = ss.getDataRoaming();
        } else {
            roaming = false;
        }
        try {
            if (this.mRegistry != null) {
                ITelephonyRegistry iTelephonyRegistry = this.mRegistry;
                int convertDataState = PhoneConstantConversions.convertDataState(state2);
                boolean isDataAllowed = sender.isDataAllowed();
                String activeApnHost = phone.getActiveApnHost(str);
                if (telephony != null) {
                    try {
                        i = telephony.getDataNetworkType(subId);
                    } catch (RemoteException e) {
                        ServiceState serviceState = ss;
                        PhoneConstants.DataState dataState2 = state2;
                        TelephonyManager telephonyManager = telephony;
                        long j = dds;
                    }
                } else {
                    i = 0;
                }
                ServiceState serviceState2 = ss;
                PhoneConstants.DataState dataState3 = state2;
                TelephonyManager telephonyManager2 = telephony;
                long j2 = dds;
                try {
                    iTelephonyRegistry.notifyDataConnectionForSubscriber(subId, convertDataState, isDataAllowed, reason, activeApnHost, str, linkProperties2, networkCapabilities2, i, roaming);
                } catch (RemoteException e2) {
                }
            } else {
                PhoneConstants.DataState dataState4 = state2;
                TelephonyManager telephonyManager3 = telephony;
                long j3 = dds;
            }
        } catch (RemoteException e3) {
            ServiceState serviceState3 = ss;
            PhoneConstants.DataState dataState5 = state2;
            TelephonyManager telephonyManager4 = telephony;
            long j4 = dds;
        }
    }

    public void notifyDataConnectionFailed(Phone sender, String reason, String apnType) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyDataConnectionFailedForSubscriber(subId, reason, apnType);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyCellLocation(Phone sender) {
        int subId = sender.getSubId();
        Bundle data = new Bundle();
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

    public void notifyCellInfo(Phone sender, List<CellInfo> cellInfo) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCellInfoForSubscriber(subId, cellInfo);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyPhysicalChannelConfiguration(Phone sender, List<PhysicalChannelConfig> configs) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyPhysicalChannelConfigurationForSubscriber(subId, configs);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyOtaspChanged(Phone sender, int otaspMode) {
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyOtaspChanged(otaspMode);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyPreciseCallState(Phone sender) {
        Call ringingCall = sender.getRingingCall();
        Call foregroundCall = sender.getForegroundCall();
        Call backgroundCall = sender.getBackgroundCall();
        if (ringingCall != null && foregroundCall != null && backgroundCall != null) {
            try {
                this.mRegistry.notifyPreciseCallState(convertPreciseCallState(ringingCall.getState()), convertPreciseCallState(foregroundCall.getState()), convertPreciseCallState(backgroundCall.getState()));
            } catch (RemoteException e) {
            }
        }
    }

    public void notifyDisconnectCause(int cause, int preciseCause) {
        try {
            this.mRegistry.notifyDisconnectCause(cause, preciseCause);
        } catch (RemoteException e) {
        }
    }

    public void notifyPreciseDataConnectionFailed(Phone sender, String reason, String apnType, String apn, String failCause) {
        try {
            this.mRegistry.notifyPreciseDataConnectionFailed(reason, apnType, apn, failCause);
        } catch (RemoteException e) {
        }
    }

    public void notifyVoLteServiceStateChanged(Phone sender, VoLteServiceState lteState) {
        try {
            this.mRegistry.notifyVoLteServiceStateChanged(lteState);
        } catch (RemoteException e) {
        }
    }

    public void notifyDataActivationStateChanged(Phone sender, int activationState) {
        try {
            this.mRegistry.notifySimActivationStateChangedForPhoneId(sender.getPhoneId(), sender.getSubId(), 1, activationState);
        } catch (RemoteException e) {
        }
    }

    public void notifyVoiceActivationStateChanged(Phone sender, int activationState) {
        try {
            this.mRegistry.notifySimActivationStateChangedForPhoneId(sender.getPhoneId(), sender.getSubId(), 0, activationState);
        } catch (RemoteException e) {
        }
    }

    public void notifyUserMobileDataStateChanged(Phone sender, boolean state) {
        try {
            this.mRegistry.notifyUserMobileDataStateChangedForPhoneId(sender.getPhoneId(), sender.getSubId(), state);
        } catch (RemoteException e) {
        }
    }

    public void notifyOemHookRawEventForSubscriber(int subId, byte[] rawData) {
        try {
            this.mRegistry.notifyOemHookRawEventForSubscriber(subId, rawData);
        } catch (RemoteException e) {
        }
    }

    public static int convertDataActivityState(PhoneInternalInterface.DataActivityState state) {
        switch (state) {
            case DATAIN:
                return 1;
            case DATAOUT:
                return 2;
            case DATAINANDOUT:
                return 3;
            case DORMANT:
                return 4;
            default:
                return 0;
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
