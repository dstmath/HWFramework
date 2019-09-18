package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.List;

public abstract class Operation {
    private int idx;
    protected String param = "";
    private int paramIdx = -1;

    /* access modifiers changed from: protected */
    public abstract String handleData(String str) throws AppletCardException;

    public void init(String dataLabel, String param2) throws AppletCardException {
        int idx2 = getResultIdx(dataLabel);
        if (idx2 >= 0) {
            this.idx = idx2;
            this.param = param2;
            this.paramIdx = getResultIdx(param2);
            return;
        }
        throw new AppletCardException(2, "operation config error. config data : " + dataLabel + "," + param2);
    }

    public String checkAndHandleData(List<String> datas) throws AppletCardException {
        if (this.idx < datas.size()) {
            String data = datas.get(this.idx);
            if (!StringUtil.isEmpty(data, true)) {
                return handleData(data);
            }
            throw new AppletCardException(1, getClass().getSimpleName() + " checkAndHandleData but data is null");
        }
        throw new AppletCardException(2, "handlerRespData idx out of bound. idx : " + this.idx + " size : " + datas.size());
    }

    public boolean isNeedChangeParamWithData() {
        return this.paramIdx >= 0;
    }

    public void changeParamWithData(List<String> datas) throws AppletCardException {
        if (this.paramIdx >= datas.size() || this.paramIdx < 0) {
            throw new AppletCardException(2, "handlerRespData idx out of bound. paramIdx : " + this.paramIdx + " size : " + datas.size());
        }
        this.param = datas.get(this.paramIdx);
    }

    private boolean isResultPattern(String pattern) {
        return pattern != null && pattern.matches("^r[0-9]");
    }

    private int getResultIdx(String pattern) {
        if (!isResultPattern(pattern)) {
            return -1;
        }
        return Integer.parseInt(pattern.replace("r", ""));
    }
}
