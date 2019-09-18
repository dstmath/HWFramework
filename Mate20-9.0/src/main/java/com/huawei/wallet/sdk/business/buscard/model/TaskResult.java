package com.huawei.wallet.sdk.business.buscard.model;

import android.os.Bundle;

public class TaskResult<T> {
    private int arg;
    private T data;
    private Bundle extraDataBundle;
    private int resultCd;
    private String resultMsg;
    private int spiResultCd;

    public TaskResult() {
        this.extraDataBundle = new Bundle();
    }

    public TaskResult(T data2) {
        this.extraDataBundle = new Bundle();
        this.resultCd = 0;
        this.spiResultCd = 0;
        this.resultMsg = "SUCCESS";
        this.data = data2;
    }

    public TaskResult(T data2, String respCd) {
        this.extraDataBundle = new Bundle();
        this.resultCd = 0;
        this.spiResultCd = 0;
        this.resultMsg = "SUCCESS";
        this.data = data2;
    }

    public TaskResult(int resultCd2, String resultMsg2) {
        this.extraDataBundle = new Bundle();
        this.resultCd = resultCd2;
        this.spiResultCd = resultCd2;
        this.resultMsg = resultMsg2;
    }

    public TaskResult(int resultCd2, int spiResultCd2, String resultMsg2) {
        this.extraDataBundle = new Bundle();
        this.resultCd = resultCd2;
        this.spiResultCd = spiResultCd2;
        this.resultMsg = resultMsg2;
    }

    public TaskResult(int resultCd2, int spiResultCd2, String resultMsg2, String respCd) {
        this.extraDataBundle = new Bundle();
        this.resultCd = resultCd2;
        this.spiResultCd = spiResultCd2;
        this.resultMsg = resultMsg2;
    }

    public TaskResult(int resultCd2, int spiResultCd2, String resultMsg2, T data2) {
        this.extraDataBundle = new Bundle();
        this.resultCd = resultCd2;
        this.spiResultCd = spiResultCd2;
        this.resultMsg = resultMsg2;
        this.data = data2;
    }

    public int getSpiResultCd() {
        return this.spiResultCd;
    }

    public void setSpiResultCd(int spiResultCd2) {
        this.spiResultCd = spiResultCd2;
    }

    public int getArg() {
        return this.arg;
    }

    public void setArg(int arg2) {
        this.arg = arg2;
    }

    public int getResultCd() {
        return this.resultCd;
    }

    public void setResultCd(int resultCd2) {
        this.resultCd = resultCd2;
    }

    public String getResultMsg() {
        return this.resultMsg;
    }

    public void setResultMsg(String resultMsg2) {
        this.resultMsg = resultMsg2;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data2) {
        this.data = data2;
    }

    public Bundle getExtraDataBundle() {
        return this.extraDataBundle;
    }

    public void setExtraDataBundle(Bundle extraDataBundle2) {
        this.extraDataBundle = extraDataBundle2;
    }
}
