package ohos.ai.nlu;

import ohos.ai.engine.utils.HiAILog;
import ohos.ai.nlu.sdk.NluApiLocalService;
import ohos.ai.nlu.util.NluError;
import ohos.ai.nlu.utils.NluResultParser;
import ohos.app.Context;

public class NluClient {
    private static final String TAG = NluClient.class.getSimpleName();
    private static volatile NluClient nluClient;

    private NluClient() {
    }

    public static NluClient getInstance() {
        if (nluClient == null) {
            synchronized (NluClient.class) {
                if (nluClient == null) {
                    nluClient = new NluClient();
                }
            }
        }
        return nluClient;
    }

    public void init(Context context, OnResultListener<Integer> onResultListener, boolean z) {
        NluApiLocalService.getInstance().bindService(context, onResultListener, z);
    }

    public ResponseResult getWordSegment(String str, int i) {
        return getWordSegment(str, i, null);
    }

    public ResponseResult getWordSegment(String str, int i, OnResultListener<ResponseResult> onResultListener) {
        if (str == null) {
            return new ResponseResult(2, NluError.REQUEST_JSON_INVALID);
        }
        if (i == 0) {
            return NluResultParser.parserResult(NluApiLocalService.getInstance().getWordSegment(str, onResultListener).orElse(null));
        }
        HiAILog.warn(TAG, "getWordSegment now only support local invoke type!");
        return new ResponseResult(2, NluError.REQUEST_TYPE_INVALID);
    }

    public ResponseResult getEntity(String str, int i) {
        return getEntity(str, i, null);
    }

    public ResponseResult getEntity(String str, int i, OnResultListener<ResponseResult> onResultListener) {
        if (str == null) {
            return new ResponseResult(2, NluError.REQUEST_JSON_INVALID);
        }
        if (i == 0) {
            return NluResultParser.parserResult(NluApiLocalService.getInstance().getEntity(str, onResultListener).orElse(null));
        }
        HiAILog.warn(TAG, "getEntity now only support local invoke type!");
        return new ResponseResult(2, NluError.REQUEST_TYPE_INVALID);
    }

    public ResponseResult getWordPos(String str, int i) {
        return getWordPos(str, i, null);
    }

    public ResponseResult getWordPos(String str, int i, OnResultListener<ResponseResult> onResultListener) {
        if (str == null) {
            return new ResponseResult(2, NluError.REQUEST_JSON_INVALID);
        }
        if (i == 0) {
            return NluResultParser.parserResult(NluApiLocalService.getInstance().getWordPos(str, onResultListener).orElse(null));
        }
        HiAILog.warn(TAG, "getWordPos now only support local invoke type!");
        return new ResponseResult(2, NluError.REQUEST_TYPE_INVALID);
    }

    public ResponseResult getChatIntention(String str, int i) {
        return getChatIntention(str, i, null);
    }

    public ResponseResult getChatIntention(String str, int i, OnResultListener<ResponseResult> onResultListener) {
        if (str == null) {
            return new ResponseResult(2, NluError.REQUEST_JSON_INVALID);
        }
        if (i == 0) {
            return NluResultParser.parserResult(NluApiLocalService.getInstance().getChatIntention(str, onResultListener).orElse(null));
        }
        HiAILog.warn(TAG, "getChatIntention now only support local invoke type!");
        return new ResponseResult(2, NluError.REQUEST_TYPE_INVALID);
    }

    public ResponseResult getAssistantIntention(String str, int i) {
        return getAssistantIntention(str, i, null);
    }

    public ResponseResult getAssistantIntention(String str, int i, OnResultListener<ResponseResult> onResultListener) {
        if (str == null) {
            return new ResponseResult(2, NluError.REQUEST_JSON_INVALID);
        }
        if (i == 0) {
            return NluResultParser.parserResult(NluApiLocalService.getInstance().getAssistantIntention(str, onResultListener).orElse(null));
        }
        HiAILog.warn(TAG, "getAssistantIntention now only support local invoke type!");
        return new ResponseResult(2, NluError.REQUEST_TYPE_INVALID);
    }

    public ResponseResult getKeywords(String str, int i) {
        return getKeywords(str, i, null);
    }

    public ResponseResult getKeywords(String str, int i, OnResultListener<ResponseResult> onResultListener) {
        if (str == null) {
            return new ResponseResult(2, NluError.REQUEST_JSON_INVALID);
        }
        if (i == 0) {
            return NluResultParser.parserResult(NluApiLocalService.getInstance().getKeywords(str, onResultListener).orElse(null));
        }
        HiAILog.warn(TAG, "getKeywords now only support local invoke type!");
        return new ResponseResult(2, NluError.REQUEST_TYPE_INVALID);
    }

    public void destroy(Context context) {
        NluApiLocalService.getInstance().unBindService(context);
    }
}
