package ohos.rpc;

public class MessageOption {
    public static final int TF_ACCEPT_FDS = 16;
    public static final int TF_ASYNC = 1;
    public static final int TF_SYNC = 0;
    private int mFlags;

    public MessageOption() {
        this.mFlags = 0;
    }

    public MessageOption(int i) {
        this.mFlags = i;
    }

    public void setFlags(int i) {
        this.mFlags = i;
    }

    public int getFlags() {
        return this.mFlags;
    }
}
