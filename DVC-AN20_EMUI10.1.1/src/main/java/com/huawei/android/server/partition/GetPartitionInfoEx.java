package com.huawei.android.server.partition;

import huawei.android.os.HwGeneralManager;

public class GetPartitionInfoEx {
    public static final int GETPARTION_TYPE_INODE = 4;
    public static final int GETPARTION_TYPE_META = 3;
    public static final int GETPARTION_TYPE_SIZE = 2;
    public static final int GETPARTION_TYPE_STARTPOS = 1;

    public static long getPartitionInfo(String partitionName, int infoType) {
        return HwGeneralManager.getInstance().getPartitionInfo(partitionName, infoType);
    }
}
