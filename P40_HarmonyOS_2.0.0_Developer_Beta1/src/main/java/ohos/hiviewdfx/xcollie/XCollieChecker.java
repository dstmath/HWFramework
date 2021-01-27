package ohos.hiviewdfx.xcollie;

import ohos.hiviewdfx.FreezeDetectorUtils;
import ohos.hiviewdfx.HiLog;

public abstract class XCollieChecker {
    private String mCheckerName;
    private boolean mThreadBlockResult = false;

    public XCollieChecker(String str, String str2) {
        this.mCheckerName = "checker: " + str2 + " packageName: " + str;
    }

    public void checkLock() {
        HiLog.debug(FreezeDetectorUtils.LOG_TAG, "get check lock %s", this.mCheckerName);
    }

    public void checkThreadBlock() {
        HiLog.debug(FreezeDetectorUtils.LOG_TAG, "get checkThreadBlock %s", this.mCheckerName);
    }

    public void setThreadBlockResult(boolean z) {
        HiLog.debug(FreezeDetectorUtils.LOG_TAG, "setThreadBlockResult %s", this.mCheckerName);
        this.mThreadBlockResult = z;
    }

    public String getXCollieCheckerName() {
        return this.mCheckerName;
    }

    public boolean getXCollieThreadBlockResult() {
        return this.mThreadBlockResult;
    }
}
