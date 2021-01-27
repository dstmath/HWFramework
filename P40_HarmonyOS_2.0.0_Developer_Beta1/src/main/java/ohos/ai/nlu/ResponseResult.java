package ohos.ai.nlu;

public class ResponseResult {
    private int code;
    private String message;
    private String responseResult;

    public ResponseResult() {
    }

    public ResponseResult(int i, String str) {
        this.code = i;
        this.message = str;
    }

    public void setCode(int i) {
        this.code = i;
    }

    public void setMessage(String str) {
        this.message = str;
    }

    public void setResponseResult(String str) {
        this.responseResult = str;
    }

    public String getResponseResult() {
        return this.responseResult;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
