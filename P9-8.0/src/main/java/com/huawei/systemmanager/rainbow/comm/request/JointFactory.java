package com.huawei.systemmanager.rainbow.comm.request;

public class JointFactory {
    public static ICommonRequest getRainbowRequest() {
        return new HsmJoinRequest();
    }

    public static HsmInputStreamRequest getRainbowInputStreamRequest() {
        return new HsmInputStreamRequest();
    }

    public static ICommonRequest getGzipRequest() {
        return new HsmGzipRequest();
    }
}
