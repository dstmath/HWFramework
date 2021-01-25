package com.huawei.nearbysdk.DTCP;

public interface DTCPReceiveListener {
    void onErrorBeforeConfirm(DTCPReceiver dTCPReceiver, int i);

    void onPreviewReceive(FilesInfo filesInfo, DTCPReceiver dTCPReceiver);

    void onSendCancelBeforeConfirm(DTCPReceiver dTCPReceiver);

    void onStatusChanged(int i);
}
