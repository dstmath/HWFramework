package com.leisen.wallet.sdk.apdu;

import android.content.Context;
import com.leisen.wallet.sdk.AppConfig;
import com.leisen.wallet.sdk.business.ApduBean;
import com.leisen.wallet.sdk.oma.SmartCard;
import com.leisen.wallet.sdk.oma.SmartCardBean;
import com.leisen.wallet.sdk.oma.SmartCardCallback;
import com.leisen.wallet.sdk.util.LogUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ApduSmartCardRequest implements Runnable, SmartCardCallback {
    private static final int RESULT_FAILURE = 1;
    private static final int RESULT_SUCCESS = 0;
    private static final String TAG = "ApduSmartCardRequest";
    private String mApduAid;
    private List<ApduBean> mCapduList;
    private AppConfig mConfig;
    private Context mContext;
    private ApduBean mCurrentExecuteApduBean;
    private int mCurrentExecuteIndex = 0;
    private int mFlag;
    private ApduResponseHandler mHandler;
    private boolean mIsGetLocalData = false;

    ApduSmartCardRequest(Context context, AppConfig config, ApduResponseHandler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mConfig = config;
    }

    public synchronized void run() {
        sendApudToSmartCard();
    }

    private void sendApudToSmartCard() {
        if (this.mCapduList != null && this.mCurrentExecuteIndex != this.mCapduList.size()) {
            this.mCurrentExecuteApduBean = this.mCapduList.get(this.mCurrentExecuteIndex);
            String apdu = this.mCurrentExecuteApduBean.getApdu();
            LogUtil.d(TAG, "==>start get apdu index:" + this.mCurrentExecuteIndex + "==apdu:" + apdu);
            if (apdu.length() >= 4 && "00A4".equals(apdu.substring(0, 4))) {
                LogUtil.d(TAG, "==>deal with select apdu :" + apdu);
                this.mApduAid = apdu.substring(apdu.length() - (Integer.parseInt(apdu.substring(8, 10), 16) * 2), apdu.length());
                this.mCurrentExecuteIndex = this.mCurrentExecuteIndex + 1;
                SmartCard.getInstance().closeChannel();
                LogUtil.d(TAG, "==>has been get select aid:" + this.mApduAid);
                sendApudToSmartCard();
            } else if (this.mApduAid != null) {
                LogUtil.d(TAG, "==>start execute apdu：" + apdu + "in eSE" + this.mConfig.CARDREADER);
                SmartCardBean bean = new SmartCardBean(this.mConfig.CARDREADER, this.mApduAid);
                bean.setCommand(apdu);
                SmartCard.getInstance().setSmartCardCallBack(this).execute(this.mContext, this.mFlag, bean);
            }
        }
    }

    public void onOperSuccess(int flag, String response) {
        String responseLog = response;
        if (responseLog != null && responseLog.toLowerCase().startsWith("9f7f")) {
            responseLog = "";
        }
        LogUtil.d(TAG, "==>handle apdu response:" + responseLog);
        if (this.mIsGetLocalData) {
            sendSuccessMessage(response);
            return;
        }
        String res_sw = response;
        String r_apdu = "";
        if (response != null && response.length() > 4) {
            res_sw = response.substring(response.length() - 4, response.length());
            r_apdu = response.substring(0, response.length() - 4).toUpperCase(Locale.getDefault());
        }
        if (res_sw != null) {
            res_sw = res_sw.toUpperCase(Locale.getDefault());
        }
        LogUtil.d(TAG, "==>get response res_sw:" + res_sw);
        if (!Arrays.asList(this.mCurrentExecuteApduBean.getSw()).contains(res_sw)) {
            sendMessage(1, this.mCurrentExecuteApduBean.getIndex(), r_apdu, res_sw);
            return;
        }
        if (this.mCurrentExecuteIndex < this.mCapduList.size() - 1) {
            this.mCurrentExecuteIndex++;
            sendApudToSmartCard();
        } else {
            sendMessage(0, this.mCurrentExecuteApduBean.getIndex(), r_apdu, res_sw);
        }
    }

    public void onOperFailure(int flag, Error e) {
        if (this.mIsGetLocalData) {
            sendFailureMessage(100009, e);
            return;
        }
        sendErrorMessage(1, this.mCurrentExecuteApduBean.getIndex(), "", "", e);
    }

    public void setCapduList(List<ApduBean> capduList) {
        this.mCapduList = capduList;
    }

    public void setFlag(int flag) {
        this.mFlag = flag;
    }

    public void isGetLocalData(boolean enable) {
        this.mIsGetLocalData = enable;
    }

    public void setGetLocalDataApdu(String apdu, String aid) {
        if (this.mCapduList == null) {
            this.mCapduList = new ArrayList();
        } else {
            this.mCapduList.clear();
        }
        this.mCapduList.add(new ApduBean(apdu));
        this.mApduAid = aid;
    }

    private void sendMessage(int result, int index, String rapdu, String sw) {
        clearData();
        if (this.mHandler != null) {
            this.mHandler.sendSendNextMessage(result, index, rapdu, sw);
        }
    }

    private void sendErrorMessage(int result, int index, String rapdu, String sw, Error e) {
        clearData();
        if (this.mHandler != null) {
            this.mHandler.sendSendNextErrorMessage(result, index, rapdu, sw, e);
        }
    }

    private void sendSuccessMessage(String response) {
        clearData();
        if (this.mHandler != null) {
            this.mHandler.sendSuccessMessage(response);
        }
    }

    private void sendFailureMessage(int result, Error e) {
        clearData();
        if (this.mHandler != null) {
            this.mHandler.sendFailureMessage(result, e);
        }
    }

    private void clearData() {
        this.mCurrentExecuteIndex = 0;
        this.mCurrentExecuteApduBean = null;
        this.mIsGetLocalData = false;
    }
}
