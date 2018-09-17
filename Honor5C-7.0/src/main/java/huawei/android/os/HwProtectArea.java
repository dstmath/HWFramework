package huawei.android.os;

public class HwProtectArea {
    private int[] mErrno;
    private String mOptItem;
    private String[] mReadBuf;

    public HwProtectArea(String optItem) {
        this.mReadBuf = new String[]{"1"};
        this.mErrno = new int[1];
        this.mOptItem = optItem;
        this.mReadBuf[0] = "error";
        this.mErrno[0] = -1;
    }

    public String getOptItem() {
        return this.mOptItem;
    }

    public String getReadBuf() {
        return this.mReadBuf[0];
    }

    public int getErrno() {
        return this.mErrno[0];
    }

    public void setReadBuf(String tmpReadBuf) {
        this.mReadBuf[0] = tmpReadBuf;
    }

    public void setErrno(int tmpErrno) {
        this.mErrno[0] = tmpErrno;
    }
}
