package ohos.msg;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class MessengerException extends Exception {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "MessengerException");
    private static final long serialVersionUID = 1;
    private String mDescription;
    private int mValue;

    public MessengerException(String str, int i) {
        super(str);
        this.mValue = i;
        this.mDescription = str;
        HiLog.error(TAG, "have a messenger exception %{public}s", str);
    }

    public int getValue() {
        return this.mValue;
    }

    public String getInfo() {
        return this.mDescription;
    }
}
