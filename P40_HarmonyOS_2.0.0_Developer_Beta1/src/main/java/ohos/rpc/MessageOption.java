package ohos.rpc;

public class MessageOption {
    private static final int MAX_WAIT_TIME = 300;
    public static final int TF_ACCEPT_FDS = 16;
    public static final int TF_ASYNC = 1;
    public static final int TF_SYNC = 0;
    public static final int TF_WAIT_TIME = 4;
    private int mFlags;
    private int mWaitTime;

    public MessageOption() {
        this.mFlags = 0;
        this.mWaitTime = 4;
    }

    public MessageOption(int i) {
        this.mFlags = i;
        this.mWaitTime = 4;
    }

    public MessageOption(int i, int i2) {
        this.mFlags = i;
        this.mWaitTime = i2;
    }

    public void setFlags(int i) {
        this.mFlags = i;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void setWaitTime(int i) {
        if (i <= 4) {
            this.mWaitTime = 4;
        } else if (i >= 300) {
            this.mWaitTime = 300;
        } else {
            this.mWaitTime = i;
        }
    }

    public int getWaitTime() {
        return this.mWaitTime;
    }
}
