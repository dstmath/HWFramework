package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class OpenChannelParams extends CommandParams {
    BearerDescription bearerDescription;
    int bufSize;
    TextMessage confirmMsg;
    byte[] destinationAddress;
    InterfaceTransportLevel itl;
    String networkAccessName;
    String userLogin;
    String userPassword;

    OpenChannelParams(CommandDetails cmdDet, TextMessage confirmMsg, int bufSize, InterfaceTransportLevel itl, byte[] destinationAddress, BearerDescription bearerDescription, String networkAccessName, String userLogin, String userPassword) {
        super(cmdDet);
        this.confirmMsg = null;
        this.bufSize = 0;
        this.itl = null;
        this.destinationAddress = null;
        this.bearerDescription = null;
        this.networkAccessName = null;
        this.userLogin = null;
        this.userPassword = null;
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
