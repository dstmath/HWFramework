package com.huawei.wallet.sdk.common.apdu.task;

import com.huawei.wallet.sdk.common.apdu.tsm.commom.TsmOperationConstant;
import java.util.HashMap;

public class SignCommand {
    static final String CITIC_SIGN_CONTENT_ACTIVATE_CARD = "ACTIVATEACTION";
    static final String CITIC_SIGN_CONTENT_APPLY_AID_ACTION = "APPLYIDCARDACTION";
    static final String CITIC_SIGN_CONTENT_APPLY_CARD_ACTION = "APPLYCARDACTION";
    static final String CITIC_SIGN_CONTENT_BILLLIST = "BILLLISTACTION";
    static final String CITIC_SIGN_CONTENT_NULLIFY = "NULLIFYACTION";
    static final String CITIC_SIGN_CONTENT_PERSONALIZE = "PERSONALIZEACTION";
    static final String CITIC_SIGN_CONTENT_QUERY_CASHLIMIT = "CASHLIMITSEARCHACTION";
    static final String CITIC_SIGN_CONTENT_SET_CASHLIMIT = "CASHLIMITACTION";
    static final String SIGN_CONTENT_CREATE_SSD = "CREATESSDACTION";
    static final String SIGN_CONTENT_DELETE_SSD = "DELETESSDACTION";
    static final String SIGN_CONTENT_ESE_INFO_SYNC = "ESEINFOSYNC";
    static final HashMap<String, String> sCommands = new HashMap<>();

    static {
        sCommands.put(TsmOperationConstant.TASK_COMMANDER_INSTALL_APP, CITIC_SIGN_CONTENT_APPLY_AID_ACTION);
        sCommands.put(TsmOperationConstant.TASK_COMMANDER_DEL_APP, CITIC_SIGN_CONTENT_NULLIFY);
        sCommands.put(TsmOperationConstant.TASK_COMMANDER_CREATE_SSD, SIGN_CONTENT_CREATE_SSD);
        sCommands.put(TsmOperationConstant.TASK_COMMANDER_DEL_SSD, SIGN_CONTENT_DELETE_SSD);
        sCommands.put(TsmOperationConstant.TASK_COMMANDER_SYNC_INFO, SIGN_CONTENT_ESE_INFO_SYNC);
    }

    public static String getSignCommand(String taskType) {
        return sCommands.get(taskType);
    }
}
