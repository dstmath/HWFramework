package com.android.server.security.tsmagent.logic.spi.tsm.request;

public class CommandRequest {
    private String cplc;
    private String funcallID;
    private String serverID;

    public static CommandRequest build(String cplc, String funcallID, String serverID) {
        CommandRequest request = new CommandRequest();
        request.cplc = cplc;
        request.funcallID = funcallID;
        request.serverID = serverID;
        return request;
    }

    public String getCplc() {
        return this.cplc;
    }

    public String getFuncCall() {
        return this.funcallID;
    }

    public String getServerID() {
        return this.serverID;
    }
}
