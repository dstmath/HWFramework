package ohos.ai.nlu;

import ohos.ai.engine.utils.HiAILog;
import ohos.ai.nlu.sdk.NluApiLocalService;
import ohos.ai.nlu.util.NluError;
import ohos.ai.nlu.utils.NluResultParser;

public class NluClientInner {
    private static final String TAG = NluClientInner.class.getSimpleName();
    private static volatile NluClientInner nluClientInner;

    private NluClientInner() {
    }

    public static NluClientInner getInstance() {
        if (nluClientInner == null) {
            synchronized (NluClientInner.class) {
                if (nluClientInner == null) {
                    nluClientInner = new NluClientInner();
                }
            }
        }
        return nluClientInner;
    }

    public ResponseResult analyzeAssistant(String str, int i) {
        HiAILog.info(TAG, "begin to analyzeAssistant");
        if (str == null) {
            return new ResponseResult(2, NluError.REQUEST_JSON_INVALID);
        }
        if (i == 0) {
            return NluResultParser.parserResult(NluApiLocalService.getInstance().analyzeAssistant(str).orElse(null));
        }
        HiAILog.warn(TAG, "analyzeAssistant now only support local invoke type!");
        return new ResponseResult(2, NluError.REQUEST_TYPE_INVALID);
    }

    public ResponseResult analyzeShortText(String str, int i) {
        HiAILog.info(TAG, "begin to analyzeShortText");
        if (str == null) {
            return new ResponseResult(2, NluError.REQUEST_JSON_INVALID);
        }
        if (i == 0) {
            return NluResultParser.parserResult(NluApiLocalService.getInstance().analyzeShortText(str).orElse(null));
        }
        HiAILog.warn(TAG, "analyzeShortText now only support local invoke type!");
        return new ResponseResult(2, NluError.REQUEST_TYPE_INVALID);
    }

    public ResponseResult analyzeLongText(String str, int i) {
        HiAILog.info(TAG, "begin to analyzeLongText");
        if (str == null) {
            return new ResponseResult(2, NluError.REQUEST_JSON_INVALID);
        }
        if (i == 0) {
            return NluResultParser.parserResult(NluApiLocalService.getInstance().analyzeLongText(str).orElse(null));
        }
        HiAILog.warn(TAG, "analyzeLongText now only support local invoke type!");
        return new ResponseResult(2, NluError.REQUEST_TYPE_INVALID);
    }
}
