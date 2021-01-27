package ohos.data.dataability.impl;

public class OldProxyNotSupportException extends InterfaceVersionMisMatchException {
    private static final long serialVersionUID = 2641673396348170958L;

    public OldProxyNotSupportException() {
    }

    public OldProxyNotSupportException(String str) {
        super(str);
    }
}
