package com.huawei.nb.coordinator.helper;

public interface RefreshDataRequestListener extends DataRequestListener {
    boolean onRefresh(RefreshResult refreshResult);
}
