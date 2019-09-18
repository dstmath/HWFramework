package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;

public class MatchConditionOperation extends Operation {
    private Result defaultResult;
    private List<Result> resultList;

    static class Result {
        String result;
        String value;

        public Result(String value2, String result2) {
            this.value = value2;
            this.result = result2;
        }
    }

    public String handleData(String data) throws AppletCardException {
        if (this.resultList == null || this.resultList.isEmpty()) {
            throw new AppletCardException(2, " MatchConditionOperation no value-result pairs");
        } else if (this.defaultResult != null) {
            for (Result result : this.resultList) {
                if (data.matches(result.value)) {
                    return getResult(data, result.result);
                }
            }
            return getResult(data, this.defaultResult.result);
        } else {
            throw new AppletCardException(2, " MatchConditionOperation default value-result is null");
        }
    }

    private String getResult(String data, String result) {
        if (result.length() != 2 || !result.matches("r[0-9]")) {
            return result;
        }
        return data;
    }

    public void init(String dataLabel, String param) throws AppletCardException {
        super.init(dataLabel, param);
        if (StringUtil.isEmpty(param, true)) {
            this.resultList = null;
            this.defaultResult = null;
            return;
        }
        String[] results = param.split(":");
        if (results.length < 2) {
            this.resultList = null;
            this.defaultResult = null;
            return;
        }
        this.resultList = new ArrayList();
        for (String r : results) {
            String[] params = r.split("=");
            if (params.length != 2) {
                this.resultList = null;
                this.defaultResult = null;
                return;
            }
            String value = params[0];
            String result = params[1];
            if ("dft".equals(value)) {
                this.defaultResult = new Result(value, result);
            } else {
                this.resultList.add(new Result(value, result));
            }
        }
    }
}
