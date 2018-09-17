package tmsdk.common.module.optimus;

public class SMSCheckerResult {
    public boolean isCloudCheck;
    public BsFakeType mType;

    public SMSCheckerResult() {
        this.mType = BsFakeType.UNKNOW;
        this.isCloudCheck = false;
    }

    public SMSCheckerResult(BsFakeType bsFakeType, boolean z) {
        this.mType = bsFakeType;
        this.isCloudCheck = z;
    }
}
