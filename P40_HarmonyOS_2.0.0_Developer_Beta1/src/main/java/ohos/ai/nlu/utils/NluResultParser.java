package ohos.ai.nlu.utils;

import ohos.ai.engine.utils.HiAILog;
import ohos.ai.nlu.ResponseResult;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;

public class NluResultParser {
    private static final String TAG = NluResultParser.class.getSimpleName();

    private NluResultParser() {
    }

    public static ResponseResult parserResult(String str) {
        ResponseResult responseResult = new ResponseResult();
        if (str != null) {
            try {
                responseResult.setResponseResult(str);
                JSONObject parseObject = JSON.parseObject(str);
                if (parseObject != null) {
                    responseResult.setCode(parseObject.getIntValue("code"));
                    responseResult.setMessage(parseObject.getString("message"));
                }
            } catch (JSONException unused) {
                HiAILog.error(TAG, "ResponseResult parserResult.");
            }
        } else {
            responseResult.setCode(4);
        }
        return responseResult;
    }
}
