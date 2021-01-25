package com.huawei.networkit.grs;

import java.util.Map;

public interface IQueryUrlsCallBack {
    void onCallBackFail(int i);

    void onCallBackSuccess(Map<String, String> map);
}
