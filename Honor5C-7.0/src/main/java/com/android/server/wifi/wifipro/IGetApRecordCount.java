package com.android.server.wifi.wifipro;

public interface IGetApRecordCount {
    int getHomeApRecordCount();

    int getTotRecordCount();

    boolean statisticApInfoRecord();
}
