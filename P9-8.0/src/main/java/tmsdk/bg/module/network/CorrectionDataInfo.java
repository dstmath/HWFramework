package tmsdk.bg.module.network;

public final class CorrectionDataInfo {
    private String mMessage = "";
    private String va = "";

    public CorrectionDataInfo(String str, String str2) {
        setAddress(str);
        setMessage(str2);
    }

    public String getAddress() {
        return this.va;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public void setAddress(String str) {
        if (str == null) {
            str = "";
        }
        this.va = str;
    }

    public void setMessage(String str) {
        if (str == null) {
            str = "";
        }
        this.mMessage = str;
    }
}
