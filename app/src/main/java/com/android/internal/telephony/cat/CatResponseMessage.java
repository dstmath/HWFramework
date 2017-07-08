package com.android.internal.telephony.cat;

public class CatResponseMessage {
    String envelopeCmd;
    byte[] mAddedInfo;
    int mAdditionalInfo;
    CommandDetails mCmdDet;
    int mEventValue;
    boolean mIncludeAdditionalInfo;
    ResultCode mResCode;
    boolean mUsersConfirm;
    String mUsersInput;
    int mUsersMenuSelection;
    boolean mUsersYesNoSelection;

    public CatResponseMessage(String envCmd) {
        this.mCmdDet = null;
        this.mResCode = ResultCode.OK;
        this.mUsersMenuSelection = 0;
        this.mUsersInput = null;
        this.mUsersYesNoSelection = false;
        this.mUsersConfirm = false;
        this.mIncludeAdditionalInfo = false;
        this.mAdditionalInfo = 0;
        this.mEventValue = -1;
        this.mAddedInfo = null;
        this.envelopeCmd = null;
        this.envelopeCmd = envCmd;
    }

    public CatResponseMessage(CatCmdMessage cmdMsg) {
        this.mCmdDet = null;
        this.mResCode = ResultCode.OK;
        this.mUsersMenuSelection = 0;
        this.mUsersInput = null;
        this.mUsersYesNoSelection = false;
        this.mUsersConfirm = false;
        this.mIncludeAdditionalInfo = false;
        this.mAdditionalInfo = 0;
        this.mEventValue = -1;
        this.mAddedInfo = null;
        this.envelopeCmd = null;
        this.mCmdDet = cmdMsg.mCmdDet;
    }

    public void setResultCode(ResultCode resCode) {
        this.mResCode = resCode;
    }

    public void setMenuSelection(int selection) {
        this.mUsersMenuSelection = selection;
    }

    public void setInput(String input) {
        this.mUsersInput = input;
    }

    public void setEventDownload(int event, byte[] addedInfo) {
        this.mEventValue = event;
        this.mAddedInfo = addedInfo;
    }

    public void setYesNo(boolean yesNo) {
        this.mUsersYesNoSelection = yesNo;
    }

    public void setConfirmation(boolean confirm) {
        this.mUsersConfirm = confirm;
    }

    public void setAdditionalInfo(boolean addInfoReq, int addInfoCode) {
        this.mIncludeAdditionalInfo = addInfoReq;
        this.mAdditionalInfo = addInfoCode;
    }

    public void setAdditionalInfo(int info) {
        this.mIncludeAdditionalInfo = true;
        this.mAdditionalInfo = info;
    }

    CommandDetails getCmdDetails() {
        return this.mCmdDet;
    }
}
