package com.leisen.wallet.sdk.oma;

import android.se.omapi.Channel;
import android.se.omapi.Reader;
import android.se.omapi.SEService;
import android.se.omapi.Session;
import com.leisen.wallet.sdk.util.DataConvertUtil;
import com.leisen.wallet.sdk.util.LogUtil;
import java.io.IOException;

class SmartCardRequest implements Runnable {
    private static final String BOUNDARY = "==>";
    private static final String TAG = "SmartCardRequest";
    private SmartCardCallback mCallback;
    private Channel mChannel;
    private int mFlag = -1;
    private SEService mSEService;
    private Session mSession;
    private SmartCardBean mSmartCardBean;

    protected SmartCardRequest(SEService seService) {
        this.mSEService = seService;
    }

    public void setSmartCartBean(SmartCardBean bean) {
        this.mSmartCardBean = bean;
    }

    public void setSmartCardCallback(SmartCardCallback callback) {
        this.mCallback = callback;
    }

    public void setFlag(int flag) {
        this.mFlag = flag;
    }

    public synchronized void run() {
        if (openCurrentAvailableChannel()) {
            try {
                executeApduCmd();
            } catch (IOException e) {
                closeChannelAndSession();
                int i = this.mFlag;
                operFailure(i, "execute apdu error：" + e.getMessage());
            }
        } else {
            return;
        }
        return;
    }

    private void executeApduCmd() throws IOException {
        String command = this.mSmartCardBean.getCommand();
        if (command == null || "".equals(command)) {
            String rapdu = DataConvertUtil.bytesToHexString(this.mChannel.getSelectResponse());
            LogUtil.i(TAG, "==>选择AID的结果为：" + rapdu);
            operSuccess(this.mFlag, rapdu);
        }
        byte[] byteCommand = DataConvertUtil.hexStringToBytes(command);
        if (this.mChannel != null) {
            String rapdu2 = DataConvertUtil.bytesToHexString(this.mChannel.transmit(byteCommand));
            String responseLog = rapdu2;
            if (responseLog != null && responseLog.toLowerCase().startsWith("9f7f")) {
                responseLog = "";
            }
            LogUtil.i(TAG, "==>执行APDU:" + command + "，返回的RAPDU为：" + responseLog);
            operSuccess(this.mFlag, rapdu2);
        }
    }

    private boolean openCurrentAvailableChannel() {
        if (this.mChannel == null) {
            Reader reader = getCurrentAvailableReader();
            if (reader == null) {
                operFailure(this.mFlag, "choose reader not exist");
                return false;
            } else if (!reader.isSecureElementPresent()) {
                operFailure(this.mFlag, "choose reader can not use-> isSecureElementPresent false");
                return false;
            } else {
                try {
                    this.mSession = reader.openSession();
                    byte[] byteAid = DataConvertUtil.hexStringToBytes(this.mSmartCardBean.getAid());
                    LogUtil.i(TAG, "==>打开通道的Aid：" + this.mSmartCardBean.getAid());
                    try {
                        if (this.mSession != null) {
                            this.mChannel = this.mSession.openBasicChannel(byteAid);
                        }
                    } catch (IOException e) {
                        closeChannelAndSession();
                        int i = this.mFlag;
                        operFailure(i, "IOException open channel error：" + e.getMessage());
                        return false;
                    } catch (SecurityException e2) {
                        closeChannelAndSession();
                        int i2 = this.mFlag;
                        operFailure(i2, "SecurityException open channel error：" + e2.getMessage());
                        return false;
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        closeChannelAndSession();
                        int i3 = this.mFlag;
                        operFailure(i3, "Exception open channel error：" + e3.getMessage());
                        return false;
                    }
                } catch (IOException e4) {
                    closeChannelAndSession();
                    int i4 = this.mFlag;
                    operFailure(i4, "open session error：" + e4.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    private Reader getCurrentAvailableReader() {
        Reader[] readers = this.mSEService.getReaders();
        if (readers == null || readers.length < 1) {
            operFailure(this.mFlag, "your devices not support any reader");
            return null;
        }
        LogUtil.e(TAG, "==>find use reader name:" + this.mSmartCardBean.getReaderName());
        if (this.mSmartCardBean.getReaderName() != null) {
            for (Reader reader : readers) {
                LogUtil.e(TAG, "==>reader name:" + reader.getName());
                if (reader.getName().equals(this.mSmartCardBean.getReaderName())) {
                    return reader;
                }
            }
        }
        for (Reader reader2 : readers) {
            if (reader2.getName().equals("eSE2")) {
                LogUtil.e(TAG, "==> second choice use reader name:" + reader2.getName());
                return reader2;
            }
        }
        for (Reader reader3 : readers) {
            if (reader3.getName().startsWith("eSE")) {
                LogUtil.e(TAG, "==> third choice use reader name:" + reader3.getName());
                return reader3;
            }
        }
        return null;
    }

    public void closeChannelAndSession() {
        try {
            if (this.mChannel != null) {
                this.mChannel.close();
                this.mChannel = null;
                LogUtil.i(TAG, "==>Channel正常关闭");
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "==>Channel关闭异常" + e.getMessage());
        }
        try {
            if (this.mSession != null && !this.mSession.isClosed()) {
                this.mSession.close();
                this.mSession = null;
                LogUtil.i(TAG, "==>Session正常关闭");
            }
        } catch (Exception e2) {
            LogUtil.e(TAG, "==>Session关闭异常" + e2.getMessage());
        }
    }

    private void operSuccess(int flag, String response) {
        if (this.mCallback != null) {
            this.mCallback.onOperSuccess(flag, response);
        }
    }

    private void operFailure(int flag, String detailMessage) {
        LogUtil.e(TAG, BOUNDARY + detailMessage);
        if (this.mCallback != null) {
            this.mCallback.onOperFailure(flag, new Error(detailMessage));
        }
    }
}
