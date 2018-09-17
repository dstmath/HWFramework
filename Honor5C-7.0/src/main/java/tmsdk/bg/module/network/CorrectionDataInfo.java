package tmsdk.bg.module.network;

/* compiled from: Unknown */
public final class CorrectionDataInfo {
    private String mMessage;
    private String xW;

    public CorrectionDataInfo() {
        this.xW = "";
        this.mMessage = "";
    }

    public CorrectionDataInfo(String str, String str2) {
        this.xW = "";
        this.mMessage = "";
        setAddress(str);
        setMessage(str2);
    }

    public String getAddress() {
        return this.xW;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public void setAddress(String str) {
        if (str == null) {
            str = "";
        }
        this.xW = str;
    }

    public void setMessage(String str) {
        if (str == null) {
            str = "";
        }
        this.mMessage = str;
    }
}
