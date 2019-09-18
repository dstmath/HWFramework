package com.android.server.display;

public interface IHwDisplayPowerControllerEx {

    public interface Callbacks {
        void handleProximitySensorEventEx(long j, boolean z);

        void handlerSendTpKeepMsgEx();
    }

    boolean getTpKeep();

    void handleTpKeep();

    void initTpKeepParamters();

    void sendProximityBroadcast(boolean z);

    void setProxPositive(boolean z);

    void setTPDozeMode(boolean z);
}
