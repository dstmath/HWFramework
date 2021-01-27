package com.huawei.distributedgw;

public interface DistributedGatewayStateCallback {
    void onBorrowingStateChanged(InternetBorrowingRequestEx internetBorrowingRequestEx, int i);

    void onSharingStateChanged(InternetSharingRequestEx internetSharingRequestEx, int i);
}
