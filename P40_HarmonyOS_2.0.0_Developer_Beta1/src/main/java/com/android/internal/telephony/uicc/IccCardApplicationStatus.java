package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.telephony.Rlog;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.IccCardStatus;

public class IccCardApplicationStatus {
    public String aid;
    public String app_label;
    public AppState app_state;
    @UnsupportedAppUsage
    public AppType app_type;
    public PersoSubState perso_substate;
    public IccCardStatus.PinState pin1;
    public int pin1_replaced;
    public IccCardStatus.PinState pin2;

    public enum AppType {
        APPTYPE_UNKNOWN,
        APPTYPE_SIM,
        APPTYPE_USIM,
        APPTYPE_RUIM,
        APPTYPE_CSIM,
        APPTYPE_ISIM
    }

    public enum AppState {
        APPSTATE_UNKNOWN,
        APPSTATE_DETECTED,
        APPSTATE_PIN,
        APPSTATE_PUK,
        APPSTATE_SUBSCRIPTION_PERSO,
        APPSTATE_READY;

        /* access modifiers changed from: package-private */
        public boolean isPinRequired() {
            return this == APPSTATE_PIN;
        }

        /* access modifiers changed from: package-private */
        public boolean isPukRequired() {
            return this == APPSTATE_PUK;
        }

        /* access modifiers changed from: package-private */
        public boolean isSubscriptionPersoEnabled() {
            return this == APPSTATE_SUBSCRIPTION_PERSO;
        }

        /* access modifiers changed from: package-private */
        public boolean isAppReady() {
            return this == APPSTATE_READY;
        }

        /* access modifiers changed from: package-private */
        public boolean isAppNotReady() {
            return this == APPSTATE_UNKNOWN || this == APPSTATE_DETECTED;
        }
    }

    public enum PersoSubState {
        PERSOSUBSTATE_UNKNOWN,
        PERSOSUBSTATE_IN_PROGRESS,
        PERSOSUBSTATE_READY,
        PERSOSUBSTATE_SIM_NETWORK,
        PERSOSUBSTATE_SIM_NETWORK_SUBSET,
        PERSOSUBSTATE_SIM_CORPORATE,
        PERSOSUBSTATE_SIM_SERVICE_PROVIDER,
        PERSOSUBSTATE_SIM_SIM,
        PERSOSUBSTATE_SIM_NETWORK_PUK,
        PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK,
        PERSOSUBSTATE_SIM_CORPORATE_PUK,
        PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK,
        PERSOSUBSTATE_SIM_SIM_PUK,
        PERSOSUBSTATE_RUIM_NETWORK1,
        PERSOSUBSTATE_RUIM_NETWORK2,
        PERSOSUBSTATE_RUIM_HRPD,
        PERSOSUBSTATE_RUIM_CORPORATE,
        PERSOSUBSTATE_RUIM_SERVICE_PROVIDER,
        PERSOSUBSTATE_RUIM_RUIM,
        PERSOSUBSTATE_RUIM_NETWORK1_PUK,
        PERSOSUBSTATE_RUIM_NETWORK2_PUK,
        PERSOSUBSTATE_RUIM_HRPD_PUK,
        PERSOSUBSTATE_RUIM_CORPORATE_PUK,
        PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK,
        PERSOSUBSTATE_RUIM_RUIM_PUK;

        /* access modifiers changed from: package-private */
        public boolean isPersoSubStateUnknown() {
            return this == PERSOSUBSTATE_UNKNOWN;
        }
    }

    @UnsupportedAppUsage
    public AppType AppTypeFromRILInt(int type) {
        if (type == 0) {
            return AppType.APPTYPE_UNKNOWN;
        }
        if (type == 1) {
            return AppType.APPTYPE_SIM;
        }
        if (type == 2) {
            return AppType.APPTYPE_USIM;
        }
        if (type == 3) {
            return AppType.APPTYPE_RUIM;
        }
        if (type == 4) {
            return AppType.APPTYPE_CSIM;
        }
        if (type == 5) {
            return AppType.APPTYPE_ISIM;
        }
        AppType newType = AppType.APPTYPE_UNKNOWN;
        loge("AppTypeFromRILInt: bad RIL_AppType: " + type + " use APPTYPE_UNKNOWN");
        return newType;
    }

    public AppState AppStateFromRILInt(int state) {
        if (state == 0) {
            return AppState.APPSTATE_UNKNOWN;
        }
        if (state == 1) {
            return AppState.APPSTATE_DETECTED;
        }
        if (state == 2) {
            return AppState.APPSTATE_PIN;
        }
        if (state == 3) {
            return AppState.APPSTATE_PUK;
        }
        if (state == 4) {
            return AppState.APPSTATE_SUBSCRIPTION_PERSO;
        }
        if (state == 5) {
            return AppState.APPSTATE_READY;
        }
        AppState newState = AppState.APPSTATE_UNKNOWN;
        loge("AppStateFromRILInt: bad state: " + state + " use APPSTATE_UNKNOWN");
        return newState;
    }

    public PersoSubState PersoSubstateFromRILInt(int substate) {
        switch (substate) {
            case 0:
                return PersoSubState.PERSOSUBSTATE_UNKNOWN;
            case 1:
                return PersoSubState.PERSOSUBSTATE_IN_PROGRESS;
            case 2:
                return PersoSubState.PERSOSUBSTATE_READY;
            case 3:
                return PersoSubState.PERSOSUBSTATE_SIM_NETWORK;
            case 4:
                return PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET;
            case 5:
                return PersoSubState.PERSOSUBSTATE_SIM_CORPORATE;
            case 6:
                return PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER;
            case 7:
                return PersoSubState.PERSOSUBSTATE_SIM_SIM;
            case 8:
                return PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK;
            case 9:
                return PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK;
            case 10:
                return PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK;
            case 11:
                return PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK;
            case 12:
                return PersoSubState.PERSOSUBSTATE_SIM_SIM_PUK;
            case 13:
                return PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1;
            case 14:
                return PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2;
            case 15:
                return PersoSubState.PERSOSUBSTATE_RUIM_HRPD;
            case 16:
                return PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE;
            case 17:
                return PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER;
            case 18:
                return PersoSubState.PERSOSUBSTATE_RUIM_RUIM;
            case 19:
                return PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1_PUK;
            case 20:
                return PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2_PUK;
            case 21:
                return PersoSubState.PERSOSUBSTATE_RUIM_HRPD_PUK;
            case 22:
                return PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE_PUK;
            case 23:
                return PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK;
            case TelephonyProto.RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                return PersoSubState.PERSOSUBSTATE_RUIM_RUIM_PUK;
            default:
                PersoSubState newSubState = PersoSubState.PERSOSUBSTATE_UNKNOWN;
                loge("PersoSubstateFromRILInt: bad substate: " + substate + " use PERSOSUBSTATE_UNKNOWN");
                return newSubState;
        }
    }

    public IccCardStatus.PinState PinStateFromRILInt(int state) {
        if (state == 0) {
            return IccCardStatus.PinState.PINSTATE_UNKNOWN;
        }
        if (state == 1) {
            return IccCardStatus.PinState.PINSTATE_ENABLED_NOT_VERIFIED;
        }
        if (state == 2) {
            return IccCardStatus.PinState.PINSTATE_ENABLED_VERIFIED;
        }
        if (state == 3) {
            return IccCardStatus.PinState.PINSTATE_DISABLED;
        }
        if (state == 4) {
            return IccCardStatus.PinState.PINSTATE_ENABLED_BLOCKED;
        }
        if (state == 5) {
            return IccCardStatus.PinState.PINSTATE_ENABLED_PERM_BLOCKED;
        }
        IccCardStatus.PinState newPinState = IccCardStatus.PinState.PINSTATE_UNKNOWN;
        loge("PinStateFromRILInt: bad pin state: " + state + " use PINSTATE_UNKNOWN");
        return newPinState;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(this.app_type);
        sb.append(",");
        sb.append(this.app_state);
        if (this.app_state == AppState.APPSTATE_SUBSCRIPTION_PERSO) {
            sb.append(",");
            sb.append(this.perso_substate);
        }
        if (this.app_type == AppType.APPTYPE_CSIM || this.app_type == AppType.APPTYPE_USIM || this.app_type == AppType.APPTYPE_ISIM) {
            sb.append(",pin1=");
            sb.append(this.pin1);
            sb.append(",pin2=");
            sb.append(this.pin2);
        }
        sb.append("}");
        return sb.toString();
    }

    private void loge(String s) {
        Rlog.e("IccCardApplicationStatus", s);
    }
}
