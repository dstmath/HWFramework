package com.android.server.security.tsmagent.logic.spi.tsm.request;

public class CommandRequest {
    private String cplc;
    private String funcallID;
    private String serverID;

    public static CommandRequest build(String cplc2, String funcallID2, String serverID2) {
        CommandRequest request = new CommandRequest();
        request.cplc = cplc2;
        request.funcallID = funcallID2;
        request.serverID = serverID2;
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
