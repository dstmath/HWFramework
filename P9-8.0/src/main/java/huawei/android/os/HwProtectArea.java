package huawei.android.os;

import android.os.FreezeScreenScene;

public class HwProtectArea {
    private int[] mErrno = new int[1];
    private String mOptItem;
    private String[] mReadBuf = new String[]{FreezeScreenScene.HUNG_CONFIG_ENABLE};

    public HwProtectArea(String optItem) {
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
