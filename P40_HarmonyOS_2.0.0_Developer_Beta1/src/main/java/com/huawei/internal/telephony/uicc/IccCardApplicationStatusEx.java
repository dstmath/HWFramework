package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccCardApplicationStatus;

public class IccCardApplicationStatusEx {

    public enum AppStateEx {
        APPSTATE_UNKNOWN(IccCardApplicationStatus.AppState.APPSTATE_UNKNOWN),
        APPSTATE_DETECTED(IccCardApplicationStatus.AppState.APPSTATE_DETECTED),
        APPSTATE_PIN(IccCardApplicationStatus.AppState.APPSTATE_PIN),
        APPSTATE_PUK(IccCardApplicationStatus.AppState.APPSTATE_PUK),
        APPSTATE_SUBSCRIPTION_PERSO(IccCardApplicationStatus.AppState.APPSTATE_SUBSCRIPTION_PERSO),
        APPSTATE_READY(IccCardApplicationStatus.AppState.APPSTATE_READY);
        
        private final IccCardApplicationStatus.AppState value;

        private AppStateEx(IccCardApplicationStatus.AppState value2) {
            this.value = value2;
        }

        public static AppStateEx getAppStateExByAppState(IccCardApplicationStatus.AppState appState) {
            switch (appState) {
                case APPSTATE_UNKNOWN:
                    return APPSTATE_UNKNOWN;
                case APPSTATE_DETECTED:
                    return APPSTATE_DETECTED;
                case APPSTATE_PIN:
                    return APPSTATE_PIN;
                case APPSTATE_PUK:
                    return APPSTATE_PUK;
                case APPSTATE_SUBSCRIPTION_PERSO:
                    return APPSTATE_SUBSCRIPTION_PERSO;
                case APPSTATE_READY:
                    return APPSTATE_READY;
                default:
                    return null;
            }
        }

        public IccCardApplicationStatus.AppState getValue() {
            return this.value;
        }
    }

    public enum PersoSubStateEx {
        PERSOSUBSTATE_UNKNOWN(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_UNKNOWN),
        PERSOSUBSTATE_IN_PROGRESS(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_IN_PROGRESS),
        PERSOSUBSTATE_READY(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_READY),
        PERSOSUBSTATE_SIM_NETWORK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK),
        PERSOSUBSTATE_SIM_NETWORK_SUBSET(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET),
        PERSOSUBSTATE_SIM_CORPORATE(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_CORPORATE),
        PERSOSUBSTATE_SIM_SERVICE_PROVIDER(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER),
        PERSOSUBSTATE_SIM_SIM(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SIM),
        PERSOSUBSTATE_SIM_NETWORK_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK),
        PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK),
        PERSOSUBSTATE_SIM_CORPORATE_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK),
        PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK),
        PERSOSUBSTATE_SIM_SIM_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SIM_PUK),
        PERSOSUBSTATE_RUIM_NETWORK1(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1),
        PERSOSUBSTATE_RUIM_NETWORK2(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2),
        PERSOSUBSTATE_RUIM_HRPD(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_HRPD),
        PERSOSUBSTATE_RUIM_CORPORATE(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE),
        PERSOSUBSTATE_RUIM_SERVICE_PROVIDER(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER),
        PERSOSUBSTATE_RUIM_RUIM(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_RUIM),
        PERSOSUBSTATE_RUIM_NETWORK1_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1_PUK),
        PERSOSUBSTATE_RUIM_NETWORK2_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2_PUK),
        PERSOSUBSTATE_RUIM_HRPD_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_HRPD_PUK),
        PERSOSUBSTATE_RUIM_CORPORATE_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE_PUK),
        PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK),
        PERSOSUBSTATE_RUIM_RUIM_PUK(IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_RUIM_RUIM_PUK);
        
        private final IccCardApplicationStatus.PersoSubState persoSubState;

        private PersoSubStateEx(IccCardApplicationStatus.PersoSubState state) {
            this.persoSubState = state;
        }

        public static PersoSubStateEx getPersoSubStateEx(IccCardApplicationStatus.PersoSubState persoSubState2) {
            return valueOf(persoSubState2.toString());
        }
    }

    public enum AppTypeEx {
        APPTYPE_UNKNOWN(IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN),
        APPTYPE_SIM(IccCardApplicationStatus.AppType.APPTYPE_SIM),
        APPTYPE_USIM(IccCardApplicationStatus.AppType.APPTYPE_USIM),
        APPTYPE_RUIM(IccCardApplicationStatus.AppType.APPTYPE_RUIM),
        APPTYPE_CSIM(IccCardApplicationStatus.AppType.APPTYPE_CSIM),
        APPTYPE_ISIM(IccCardApplicationStatus.AppType.APPTYPE_ISIM);
        
        private final IccCardApplicationStatus.AppType appType;

        private AppTypeEx(IccCardApplicationStatus.AppType type) {
            this.appType = type;
        }

        public static AppTypeEx getAppTypeEx(IccCardApplicationStatus.AppType appType2) {
            return valueOf(appType2.toString());
        }

        public static IccCardApplicationStatus.AppType getAppTypeByEx(AppTypeEx appTypeEx) {
            return IccCardApplicationStatus.AppType.valueOf(appTypeEx.toString());
        }
    }
}
