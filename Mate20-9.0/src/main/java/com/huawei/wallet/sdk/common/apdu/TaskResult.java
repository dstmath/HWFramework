package com.huawei.wallet.sdk.common.apdu;

import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;

public class TaskResult<T> {
    private T data;
    private ApduCommand lastExcutedCommand;
    private String msg = "default success";
    private int resultCode = 0;

    public int getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(int resultCode2) {
        this.resultCode = resultCode2;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data2) {
        this.data = data2;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg2) {
        this.msg = msg2;
    }

    public ApduCommand getLastExcutedCommand() {
        return this.lastExcutedCommand;
    }

    public void setLastExcutedCommand(ApduCommand lastExcutedCommand2) {
        this.lastExcutedCommand = lastExcutedCommand2;
    }

    public String getPrintMsg() {
        return " resultCode : " + this.resultCode + " msg : " + this.msg;
    }
}
