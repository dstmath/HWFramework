package com.leisen.wallet.sdk.oma;

import com.leisen.wallet.sdk.util.DataConvertUtil;
import com.leisen.wallet.sdk.util.LogUtil;
import java.io.IOException;
import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

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
                operFailure(this.mFlag, "execute apdu error：" + e.getMessage());
            }
        } else {
            return;
        }
        return;
    }

    private void executeApduCmd() throws IOException {
        String rapdu;
        String command = this.mSmartCardBean.getCommand();
        if (command == null || "".equals(command)) {
            rapdu = DataConvertUtil.bytesToHexString(this.mChannel.getSelectResponse());
            LogUtil.i(TAG, "==>选择AID的结果为：" + rapdu);
            operSuccess(this.mFlag, rapdu);
        }
        byte[] byteCommand = DataConvertUtil.hexStringToBytes(command);
        if (this.mChannel != null) {
            rapdu = DataConvertUtil.bytesToHexString(this.mChannel.transmit(byteCommand));
            LogUtil.i(TAG, "==>执行APDU:" + command + "，返回的RAPDU为：" + rapdu);
            operSuccess(this.mFlag, rapdu);
        }
    }

    private boolean openCurrentAvailableChannel() {
        if (this.mChannel == null) {
            Reader reader = getCurrentAvailableReader();
            if (reader == null) {
                operFailure(this.mFlag, "choose reader not exist");
                return false;
            } else if (reader.isSecureElementPresent()) {
                try {
                    this.mSession = reader.openSession();
                    byte[] byteAid = DataConvertUtil.hexStringToBytes(this.mSmartCardBean.getAid());
                    LogUtil.i(TAG, "==>打开通道的Aid：" + this.mSmartCardBean.getAid());
                    try {
                        if (this.mSession != null) {
                            this.mChannel = this.mSession.openLogicalChannel(byteAid);
                        }
                    } catch (IOException e) {
                        closeChannelAndSession();
                        operFailure(this.mFlag, "open channel error：" + e.getMessage());
                        return false;
                    } catch (SecurityException e2) {
                        closeChannelAndSession();
                        operFailure(this.mFlag, "open channel error：" + e2.getMessage());
                        return false;
                    } catch (Exception e3) {
                        closeChannelAndSession();
                        operFailure(this.mFlag, "open channel error：" + e3.getMessage());
                        return false;
                    }
                } catch (IOException e4) {
                    closeChannelAndSession();
                    operFailure(this.mFlag, "open session error：" + e4.getMessage());
                    return false;
                }
            } else {
                operFailure(this.mFlag, "choose reader can not use");
                return false;
            }
        }
        return true;
    }

    private Reader getCurrentAvailableReader() {
        Reader[] readers = this.mSEService.getReaders();
        if (readers != null && readers.length >= 1) {
            Reader[] readerArr = readers;
            for (Reader reader : readers) {
                LogUtil.e(TAG, "==>reader name:" + reader.getName());
                if (reader.getName().startsWith(this.mSmartCardBean.getReaderName())) {
                    return reader;
                }
            }
            return null;
        }
        operFailure(this.mFlag, "your devices not support any reader");
        return null;
    }

    public void closeChannelAndSession() {
        try {
            if (this.mChannel != null) {
                if (!this.mChannel.isClosed()) {
                    this.mChannel.close();
                    this.mChannel = null;
                    LogUtil.i(TAG, "==>Channel正常关闭");
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "==>Channel关闭异常" + e.getMessage());
        }
        try {
            if (this.mSession != null) {
                if (!this.mSession.isClosed()) {
                    this.mSession.close();
                    this.mSession = null;
                    LogUtil.i(TAG, "==>Session正常关闭");
                }
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
        LogUtil.e(TAG, new StringBuilder(BOUNDARY).append(detailMessage).toString());
        if (this.mCallback != null) {
            this.mCallback.onOperFailure(flag, new Error(detailMessage));
        }
    }
}
