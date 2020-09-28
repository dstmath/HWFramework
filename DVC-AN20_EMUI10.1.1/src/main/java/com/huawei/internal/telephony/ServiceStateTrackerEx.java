package com.huawei.internal.telephony;

import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.NitzStateMachine;
import com.android.internal.telephony.ServiceStateTracker;

public class ServiceStateTrackerEx extends Handler {
    public static final int CARRIER_NAME_DISPLAY_BITMASK_SHOW_PLMN = 2;
    public static final int CARRIER_NAME_DISPLAY_BITMASK_SHOW_SPN = 1;
    public static final boolean DBG = true;
    public static final int EVENT_ICC_CHANGED = 42;
    protected CommandsInterfaceEx mCi;
    protected PhoneExt mPhone;
    private ServiceStateTracker mServiceStateTracker;

    public void setServiceStateTracker(ServiceStateTracker serviceStateTracker) {
        this.mServiceStateTracker = serviceStateTracker;
    }

    public ServiceStateTracker getServiceStateTracker() {
        return this.mServiceStateTracker;
    }

    /* access modifiers changed from: protected */
    public void initServiceStateTracker(PhoneExt phoneExt, CommandsInterfaceEx commandsInterfaceEx) {
        this.mPhone = phoneExt;
        this.mCi = commandsInterfaceEx;
        this.mServiceStateTracker = new ServiceStateTracker((GsmCdmaPhone) this.mPhone.getPhone(), commandsInterfaceEx.getCommandsInterface());
    }

    public PhoneExt getPhone() {
        return this.mPhone;
    }

    public int getPhoneId() {
        return this.mPhone.getPhoneId();
    }

    public Handler getSSTHandler() {
        return this.mServiceStateTracker;
    }

    public ServiceState getSS() {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            return serviceStateTracker.mSS;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public CellLocation getCellLocationInfo() {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            return serviceStateTracker.getCellLocationInfo();
        }
        return null;
    }

    public void setDesiredPowerState(boolean desiredPowerState) {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            serviceStateTracker.setDesiredPowerState(desiredPowerState);
        }
    }

    public void registerForDataConnectionDetached(int transport, Handler h, int what, Object obj) {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            serviceStateTracker.registerForDataConnectionDetached(transport, h, what, obj);
        }
    }

    public void registerForDataConnectionAttached(int transport, Handler h, int what, Object obj) {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            serviceStateTracker.registerForDataConnectionAttached(transport, h, what, obj);
        }
    }

    public void registerForDataRegStateOrRatChanged(int transport, Handler h, int what, Object obj) {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            serviceStateTracker.registerForDataRegStateOrRatChanged(transport, h, what, obj);
        }
    }

    public void setCurrent3GPsCsAllowed(boolean isAllowed) {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            serviceStateTracker.setCurrent3GPsCsAllowed(isAllowed);
        }
    }

    public boolean isConcurrentVoiceAndDataAllowed() {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            return serviceStateTracker.isConcurrentVoiceAndDataAllowed();
        }
        return false;
    }

    public String getRplmn() {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            return serviceStateTracker.getRplmn();
        }
        return null;
    }

    public String getTimeZoneFromMcc(String mcc) {
        NitzStateMachine nitzStateMachine;
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker == null || (nitzStateMachine = serviceStateTracker.getNitzState()) == null) {
            return null;
        }
        return nitzStateMachine.getTimeZoneFromMcc(mcc);
    }

    public void handleAutoTimeZoneEnabledHw() {
        NitzStateMachine nitzStateMachine;
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null && (nitzStateMachine = serviceStateTracker.getNitzState()) != null) {
            nitzStateMachine.handleAutoTimeZoneEnabledHw();
        }
    }

    public long getSavedNitzTime() {
        NitzStateMachine nitzStateMachine;
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker == null || (nitzStateMachine = serviceStateTracker.getNitzState()) == null) {
            return 0;
        }
        return nitzStateMachine.getSavedNitzTime();
    }

    public int getCarrierNameDisplayBitmask(ServiceState ss) {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            return serviceStateTracker.getCarrierNameDisplayBitmask(ss);
        }
        return 0;
    }

    public int getNewNsaState() {
        ServiceStateTracker serviceStateTracker = this.mServiceStateTracker;
        if (serviceStateTracker != null) {
            return serviceStateTracker.getNewNsaState();
        }
        return 0;
    }
}
