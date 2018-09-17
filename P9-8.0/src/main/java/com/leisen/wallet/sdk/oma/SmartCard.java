package com.leisen.wallet.sdk.oma;

import android.content.Context;
import com.leisen.wallet.sdk.util.LogUtil;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.SEService.CallBack;

public class SmartCard implements CallBack {
    private static final String BOUNDARY = "==>";
    private static final String TAG = "SmartCard";
    private static SmartCard mInstance;
    private SmartCardCallback mCallback;
    private int mFlag = -1;
    private SEService mSEService;
    private boolean mServiceIsConnection = false;
    private SmartCardBean mSmartCardBean;
    private SmartCardRequest mSmartCardRequest;

    private SmartCard() {
    }

    public static synchronized SmartCard getInstance() {
        SmartCard smartCard;
        synchronized (SmartCard.class) {
            if (mInstance == null) {
                mInstance = new SmartCard();
            }
            smartCard = mInstance;
        }
        return smartCard;
    }

    public void execute(Context context, int flag, SmartCardBean bean) {
        this.mFlag = flag;
        if (bean == null) {
            operFailure(this.mFlag, "SmartCardBean must not allow to null");
            return;
        } else if (bean.getReaderName() == null) {
            operFailure(this.mFlag, "choose reader not exist");
            return;
        } else if (bean.getAid() != null) {
            this.mSmartCardBean = bean;
            if (this.mSEService == null) {
                SEService sEService = new SEService(context, this);
                LogUtil.d(TAG, "==>start bind SEService");
                if (!this.mServiceIsConnection) {
                    synchronized (this) {
                        try {
                            if (!this.mServiceIsConnection) {
                                LogUtil.d(TAG, "==>thread is waiting");
                                wait();
                            }
                        } catch (InterruptedException e) {
                            operFailure(this.mFlag, "thread error:" + e.getMessage());
                        }
                    }
                }
            }
            executeSmartCardRequest();
            return;
        } else {
            operFailure(this.mFlag, "the aid must not allow to null");
            return;
        }
    }

    private void executeSmartCardRequest() {
        if (this.mSEService != null) {
            if (this.mSmartCardRequest == null) {
                this.mSmartCardRequest = new SmartCardRequest(this.mSEService);
            }
            this.mSmartCardRequest.setSmartCartBean(this.mSmartCardBean);
            this.mSmartCardRequest.setSmartCardCallback(this.mCallback);
            this.mSmartCardRequest.setFlag(this.mFlag);
            this.mSmartCardRequest.run();
        }
    }

    public void serviceConnected(SEService service) {
        synchronized (this) {
            if (service.isConnected()) {
                LogUtil.d(TAG, "==>bind SEService success");
                this.mSEService = service;
            } else {
                operFailure(this.mFlag, "SEService connect failure");
            }
            LogUtil.d(TAG, "==>thread notifyAll");
            this.mServiceIsConnection = true;
            notifyAll();
        }
    }

    public SmartCard setSmartCardCallBack(SmartCardCallback callback) {
        this.mCallback = callback;
        return this;
    }

    public void closeService() {
        if (this.mSmartCardRequest != null) {
            this.mSmartCardRequest.closeChannelAndSession();
            this.mSmartCardRequest = null;
        }
        try {
            if (this.mSEService != null) {
                if (this.mSEService.isConnected()) {
                    this.mSEService.shutdown();
                    this.mSEService = null;
                    this.mServiceIsConnection = false;
                    LogUtil.i(TAG, "==>SEService正常关闭");
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "==>SEService关闭异常" + e.getMessage());
        }
        this.mCallback = null;
        this.mSmartCardBean = null;
        this.mSmartCardRequest = null;
    }

    public void closeChannel() {
        if (this.mSmartCardRequest != null) {
            this.mSmartCardRequest.closeChannelAndSession();
            this.mSmartCardRequest = null;
        }
    }

    private void operFailure(int flag, String detailMessage) {
        if (this.mCallback != null) {
            this.mCallback.onOperFailure(flag, new Error(detailMessage));
        }
    }
}
