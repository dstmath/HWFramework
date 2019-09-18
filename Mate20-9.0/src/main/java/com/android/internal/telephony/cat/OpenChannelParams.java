package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class OpenChannelParams extends CommandParams {
    BearerDescription bearerDescription = null;
    int bufSize = 0;
    TextMessage confirmMsg = null;
    byte[] destinationAddress = null;
    InterfaceTransportLevel itl = null;
    String networkAccessName = null;
    String userLogin = null;
    String userPassword = null;

    OpenChannelParams(CommandDetails cmdDet, TextMessage confirmMsg2, int bufSize2, InterfaceTransportLevel itl2, byte[] destinationAddress2, BearerDescription bearerDescription2, String networkAccessName2, String userLogin2, String userPassword2) {
        super(cmdDet);
        this.confirmMsg = confirmMsg2;
        this.bufSize = bufSize2;
        this.itl = itl2;
        this.destinationAddress = destinationAddress2;
        this.bearerDescription = bearerDescription2;
        this.networkAccessName = networkAccessName2;
        this.userLogin = userLogin2;
        this.userPassword = userPassword2;
    }
}
