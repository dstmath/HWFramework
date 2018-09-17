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

    OpenChannelParams(CommandDetails cmdDet, TextMessage confirmMsg, int bufSize, InterfaceTransportLevel itl, byte[] destinationAddress, BearerDescription bearerDescription, String networkAccessName, String userLogin, String userPassword) {
        super(cmdDet);
        this.confirmMsg = confirmMsg;
        this.bufSize = bufSize;
        this.itl = itl;
        this.destinationAddress = destinationAddress;
        this.bearerDescription = bearerDescription;
        this.networkAccessName = networkAccessName;
        this.userLogin = userLogin;
        this.userPassword = userPassword;
    }
}
