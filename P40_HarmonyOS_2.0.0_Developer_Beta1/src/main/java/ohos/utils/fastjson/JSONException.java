package ohos.utils.fastjson;

public class JSONException extends RuntimeException {
    public JSONException(String str) {
        super(str);
    }

    public JSONException(String str, Throwable th) {
        super(str, th);
    }
}
