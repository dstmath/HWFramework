package com.huawei.wallet.sdk.business.bankcard.response;

import com.huawei.wallet.sdk.business.bankcard.modle.PushCUPOperateMessage;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.JsonUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class PushMessageParser {
    private static final String NFC_PUSH_MSG_CONTENT_KEY = "content";
    private static final String NFC_PUSH_MSG_TYPE_KEY = "msg";

    public Object parsePushMessage(String msgContent) {
        LogC.d("parsePushMessage, content: " + msgContent, true);
        if (StringUtil.isEmpty(msgContent, true)) {
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(msgContent);
        } catch (JSONException e) {
            LogC.d("parsePushMessage, get json exception.", false);
        }
        if (jsonObject == null) {
            return null;
        }
        String pushMsgType = JsonUtil.getStringValue(jsonObject, NFC_PUSH_MSG_TYPE_KEY).trim();
        String pushMsgContent = JsonUtil.getStringValue(jsonObject, "content");
        if (!StringUtil.isEmpty(pushMsgContent, true)) {
            return getPushObject(pushMsgType, pushMsgContent, false);
        }
        LogC.d("parsePushMessage, content is empty.", false);
        return null;
    }

    private Object getPushObject(String pushMsgType, String pushMsgContent, boolean isRetryFlag) {
        if (PushCUPOperateMessage.CUP_PUSH_MSG_TYPE.equals(pushMsgType)) {
            return parsePushCUPOperateMsg(pushMsgContent);
        }
        LogC.d("the push msg type do not supported, now.", false);
        return null;
    }

    private Object parsePushCUPOperateMsg(String pushMsgContent) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(pushMsgContent);
        } catch (JSONException e) {
            LogC.d("parsPushConsumeMsg, get json exception.", false);
        }
        if (jsonObject == null) {
            LogC.d("parsPushConsumeMsg, jsonobject is null", false);
            return null;
        }
        PushCUPOperateMessage upMsg = new PushCUPOperateMessage();
        upMsg.setCplc(JsonUtil.getStringValue(jsonObject, "cplc"));
        upMsg.setVirtualCards(JsonUtil.getStringArrayValue(jsonObject, PushCUPOperateMessage.CUP_PUSH_MSG_KEY_VIRTUAL_CARD));
        JSONObject tsmLib = JsonUtil.getJsonObject(jsonObject, PushCUPOperateMessage.CUP_PUSH_MSG_KEY_TSMLIBDATA);
        if (tsmLib != null) {
            upMsg.setSsid(JsonUtil.getStringValue(tsmLib, "ssid"));
            upMsg.setSign(JsonUtil.getStringValue(tsmLib, "sign"));
            upMsg.setEvent(JsonUtil.getStringValue(tsmLib, "event"));
        }
        return upMsg;
    }
}
