package tmsdk.common.exception;

public class NotInitializedException extends Exception {
    public String getMessage() {
        return "没有完成初始化配置";
    }
}
