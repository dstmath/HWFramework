package com.leisen.wallet.sdk.apdu;

import android.content.Context;
import com.leisen.wallet.sdk.business.ApduBean;
import com.leisen.wallet.sdk.oma.SmartCard;
import com.leisen.wallet.sdk.oma.SmartCardBean;
import com.leisen.wallet.sdk.oma.SmartCardCallback;
import com.leisen.wallet.sdk.tsm.TSMOperator;
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
    private Context mContext;
    private ApduBean mCurrentExecuteApduBean;
    private int mCurrentExecuteIndex = 0;
    private int mFlag;
    private ApduResponseHandler mHandler;
    private boolean mIsGetLocalData = false;

    public ApduSmartCardRequest(Context context, ApduResponseHandler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    public synchronized void run() {
        sendApudToSmartCard();
    }

    private void sendApudToSmartCard() {
        if (this.mCapduList != null && this.mCurrentExecuteIndex != this.mCapduList.size()) {
            this.mCurrentExecuteApduBean = (ApduBean) this.mCapduList.get(this.mCurrentExecuteIndex);
            String apdu = this.mCurrentExecuteApduBean.getApdu();
            LogUtil.d(TAG, "==>start get apdu index:" + this.mCurrentExecuteIndex + "==apdu:" + apdu);
            if ("00A4".equals(apdu.substring(0, 4))) {
                LogUtil.d(TAG, "==>deal with select apdu :" + apdu);
                this.mApduAid = apdu.substring(apdu.length() - (Integer.parseInt(apdu.substring(8, 10), 16) * 2), apdu.length());
                this.mCurrentExecuteIndex++;
                SmartCard.getInstance().closeChannel();
                LogUtil.d(TAG, "==>has been get select aid:" + this.mApduAid);
                sendApudToSmartCard();
            } else if (this.mApduAid != null) {
                LogUtil.d(TAG, "==>start execute apdu：" + apdu);
                SmartCardBean bean = new SmartCardBean(1, this.mApduAid);
                bean.setCommand(apdu);
                SmartCard.getInstance().setSmartCardCallBack(this).execute(this.mContext, this.mFlag, bean);
            }
        }
    }

    public void onOperSuccess(int flag, String response) {
        LogUtil.d(TAG, "==>handle apdu response:" + response);
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
        if (Arrays.asList(this.mCurrentExecuteApduBean.getSw()).contains(res_sw)) {
            if (this.mCurrentExecuteIndex >= this.mCapduList.size() - 1) {
                sendMessage(0, this.mCurrentExecuteApduBean.getIndex(), r_apdu, res_sw);
            } else {
                this.mCurrentExecuteIndex++;
                sendApudToSmartCard();
            }
            return;
        }
        sendMessage(1, this.mCurrentExecuteApduBean.getIndex(), r_apdu, res_sw);
    }

    public void onOperFailure(int flag, Error e) {
        if (this.mIsGetLocalData) {
            sendFailureMessage(TSMOperator.RETURN_SMARTCARD_OPER_FAILURE, e);
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
        if (this.mCapduList != null) {
            this.mCapduList.clear();
        } else {
            this.mCapduList = new ArrayList();
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
