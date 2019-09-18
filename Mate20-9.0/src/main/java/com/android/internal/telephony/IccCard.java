package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;

public class IccCard {
    private IccCardConstants.State mIccCardState = IccCardConstants.State.UNKNOWN;

    public IccCard() {
    }

    public IccCard(IccCardConstants.State state) {
        this.mIccCardState = state;
    }

    public IccCardConstants.State getState() {
        return this.mIccCardState;
    }

    public IccRecords getIccRecords() {
        return null;
    }

    public void registerForNetworkLocked(Handler h, int what, Object obj) {
    }

    public void unregisterForNetworkLocked(Handler h) {
    }

    public void supplyPin(String pin, Message onComplete) {
        sendMessageWithCardAbsentException(onComplete);
    }

    public void supplyPuk(String puk, String newPin, Message onComplete) {
        sendMessageWithCardAbsentException(onComplete);
    }

    public void supplyPin2(String pin2, Message onComplete) {
        sendMessageWithCardAbsentException(onComplete);
    }

    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
        sendMessageWithCardAbsentException(onComplete);
    }

    public boolean getIccFdnAvailable() {
        return false;
    }

    public void supplyNetworkDepersonalization(String pin, Message onComplete) {
        sendMessageWithCardAbsentException(onComplete);
    }

    public boolean getIccLockEnabled() {
        return false;
    }

    public boolean getIccFdnEnabled() {
        return false;
    }

    public void setIccLockEnabled(boolean enabled, String password, Message onComplete) {
        sendMessageWithCardAbsentException(onComplete);
    }

    public void setIccFdnEnabled(boolean enabled, String password, Message onComplete) {
        sendMessageWithCardAbsentException(onComplete);
    }

    public void changeIccLockPassword(String oldPassword, String newPassword, Message onComplete) {
        sendMessageWithCardAbsentException(onComplete);
    }

    public void changeIccFdnPassword(String oldPassword, String newPassword, Message onComplete) {
        sendMessageWithCardAbsentException(onComplete);
    }

    public String getServiceProviderName() {
        return null;
    }

    public boolean isApplicationOnIcc(IccCardApplicationStatus.AppType type) {
        return false;
    }

    public boolean hasIccCard() {
        return false;
    }

    public boolean getIccPin2Blocked() {
        return false;
    }

    public boolean getIccPuk2Blocked() {
        return false;
    }

    private void sendMessageWithCardAbsentException(Message onComplete) {
        AsyncResult.forMessage(onComplete).exception = new RuntimeException("No valid IccCard");
        onComplete.sendToTarget();
    }

    public void supplyDepersonalization(String pin, int type, Message onComplete) {
    }
}
