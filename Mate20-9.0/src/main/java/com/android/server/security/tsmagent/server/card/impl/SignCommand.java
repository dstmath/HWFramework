package com.android.server.security.tsmagent.server.card.impl;

import java.util.HashMap;

public class SignCommand {
    static final String CITIC_SIGN_CONTENT_APPLY_AID_ACTION = "APPLYIDCARDACTION";
    static final String CITIC_SIGN_CONTENT_NULLIFY = "NULLIFYACTION";
    static final String SIGN_CONTENT_CREATE_SSD = "CREATESSDACTION";
    static final String SIGN_CONTENT_CREATE_SSD_EX = "EX_CREATESSDACTION";
    static final String SIGN_CONTENT_DELETE_SSD = "DELETESSDACTION";
    static final String SIGN_CONTENT_DELETE_SSD_EX = "EX_DELETESSDACTION";
    static final String SIGN_CONTENT_SYNC_SSD = "ESEINFOSYNC";
    private static final HashMap<String, String> sCommands = new HashMap<>();
    private static final HashMap<String, String> sCommandsEX = new HashMap<>();

    static {
        sCommands.put("nfc.get.install.APP", CITIC_SIGN_CONTENT_APPLY_AID_ACTION);
        sCommands.put("nfc.get.del.APP", CITIC_SIGN_CONTENT_NULLIFY);
        sCommands.put("nfc.get.create.SSD", SIGN_CONTENT_CREATE_SSD);
        sCommands.put("nfc.get.del.SSD", SIGN_CONTENT_DELETE_SSD);
        sCommands.put("nfc.get.NotifyEseInfoSync", SIGN_CONTENT_SYNC_SSD);
        sCommandsEX.put("nfc.get.create.SSD", SIGN_CONTENT_CREATE_SSD_EX);
        sCommandsEX.put("nfc.get.del.SSD", SIGN_CONTENT_DELETE_SSD_EX);
    }

    public static String getSignCommand(String taskType, String SPID, String ssdAID, String timeStamp, boolean isExSign) {
        if (isExSign) {
            return sCommandsEX.get(taskType) + "|" + SPID + "|" + ssdAID + "|" + timeStamp;
        }
        return sCommands.get(taskType) + "|" + timeStamp;
    }
}
