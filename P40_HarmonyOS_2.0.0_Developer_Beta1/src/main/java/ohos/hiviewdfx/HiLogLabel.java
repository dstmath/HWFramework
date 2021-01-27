package ohos.hiviewdfx;

public final class HiLogLabel {
    public int domain;
    public com.huawei.android.app.HiLogLabel label = new com.huawei.android.app.HiLogLabel(this.type, this.domain, this.tag);
    public String tag;
    public int type;

    public HiLogLabel(int i, int i2, String str) {
        this.type = i;
        this.domain = i2;
        this.tag = str;
    }
}
