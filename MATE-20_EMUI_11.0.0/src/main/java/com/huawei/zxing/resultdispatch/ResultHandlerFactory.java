package com.huawei.zxing.resultdispatch;

import com.huawei.zxing.Result;
import com.huawei.zxing.client.result.ParsedResult;
import com.huawei.zxing.client.result.ResultParser;

public final class ResultHandlerFactory {
    private ResultHandlerFactory() {
    }

    public static ParsedResult parseResult(Result rawResult) {
        return ResultParser.parseResult(rawResult);
    }
}
