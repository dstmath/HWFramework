package com.android.ims;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.telecom.ConferenceParticipant;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsCallProfile;
import android.util.Log;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.SubscriptionController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwImsCallEx implements IHwImsCallEx {
    private static final boolean DBG = Log.isLoggable(LOG_TAG, 3);
    private static final boolean FORCE_DEBUG = false;
    private static final String LOCAL_CEP_MCC_MNC = "hw_local_cep_mccmnc";
    private static final String LOG_TAG = "HwImsCallEx";
    private List<ConferenceParticipant> mConferenceParticipants = null;
    private Context mContext;
    private IHwImsCallManagerInner mImsCallMgrInner;
    private boolean mIsCEPPresent = false;
    private final Object mLockObj = new Object();

    public HwImsCallEx(IHwImsCallManagerInner iImsCall, Context context) {
        this.mImsCallMgrInner = iImsCall;
        this.mContext = context;
    }

    public void updateQcomConferenceParticipantsList(IHwImsCallManagerInner bgCall) {
        logi("updateQcomConferenceParticipantsList :: bgImsCall=" + bgCall);
        if (bgCall != null) {
            IHwImsCallManagerInner parentCall = this.mImsCallMgrInner;
            IHwImsCallManagerInner childCall = bgCall;
            if (bgCall.getImsCall().isMultiparty()) {
                logi("updateQcomConferenceParticipantsList: BG call is conference");
                parentCall = bgCall;
                childCall = this.mImsCallMgrInner;
            } else if (!this.mImsCallMgrInner.getImsCall().isMultiparty()) {
                logi("updateQcomConferenceParticipantsList: Make this call as conference and add child");
                IHwImsCallManagerInner iHwImsCallManagerInner = this.mImsCallMgrInner;
                addToQcomConferenceParticipantList(iHwImsCallManagerInner, iHwImsCallManagerInner);
            }
            addToQcomConferenceParticipantList(parentCall, childCall);
        }
    }

    private void addToQcomConferenceParticipantList(IHwImsCallManagerInner parentCall, IHwImsCallManagerInner childCall) {
        if (parentCall != null && childCall != null) {
            ImsCallProfile profile = childCall.getImsCall().getCallProfile();
            if (profile == null) {
                loge("addToQcomConferenceParticipantList: null profile for childcall");
                return;
            }
            String handle = profile.getCallExtra("oi", (String) null);
            String name = profile.getCallExtra("cna", "");
            if (handle == null) {
                loge("addToQcomConferenceParticipantList: Invalid number for childcall");
                return;
            }
            Uri userUri = Uri.parse(handle);
            ConferenceParticipant participant = new ConferenceParticipant(userUri, name, userUri, 4, -1);
            IHwImsCallEx hwImsCallEx = parentCall.getHwImsCallEx();
            if (hwImsCallEx == null) {
                loge("addToQcomConferenceParticipantList: IHwImsCall of parentCall is null");
                return;
            }
            synchronized (hwImsCallEx.getQcomLockObj()) {
                List<ConferenceParticipant> conferenceParticipants = parentCall.getConferenceParticipantsForExt();
                if (conferenceParticipants == null) {
                    loge("Error:conferenceParticipants is null");
                    return;
                }
                logi("Adding participant: " + participant + " to list:" + conferenceParticipants);
                conferenceParticipants.add(participant);
                parentCall.setConferenceParticipants(conferenceParticipants);
            }
        }
    }

    public boolean shouldSaveQcomParticipantList() {
        String localCEPMccMnc;
        int[] subIds;
        if (this.mContext == null) {
            return false;
        }
        int slotId = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
        int subId = -1;
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        if (!(subCtrlr == null || (subIds = subCtrlr.getSubId(slotId)) == null || subIds[0] < 0)) {
            subId = subIds[0];
        }
        logi("isQcomPlatform: " + HuaweiTelephonyConfigs.isQcomPlatform());
        if (HuaweiTelephonyConfigs.isQcomPlatform() && (localCEPMccMnc = Settings.System.getString(this.mContext.getContentResolver(), LOCAL_CEP_MCC_MNC)) != null) {
            String[] localCEPMccMncList = localCEPMccMnc.split("[,]");
            String currentOperator = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimOperatorNumeric(subId);
            for (String s : localCEPMccMncList) {
                if (s.equals(currentOperator)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeQcomParticipantsFromList(String[] participants) {
        IHwImsCallManagerInner iHwImsCallManagerInner = this.mImsCallMgrInner;
        if (iHwImsCallManagerInner != null) {
            List<ConferenceParticipant> conferenceParticipants = iHwImsCallManagerInner.getConferenceParticipantsForExt();
            if (!(this.mIsCEPPresent || participants == null || conferenceParticipants == null)) {
                for (String participant : participants) {
                    Iterator<ConferenceParticipant> it = conferenceParticipants.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ConferenceParticipant cp = it.next();
                        if (participant != null && Uri.parse(participant).equals(cp.getHandle())) {
                            conferenceParticipants.remove(cp);
                            break;
                        }
                    }
                }
            }
            synchronized (this.mLockObj) {
                this.mImsCallMgrInner.setConferenceParticipants(conferenceParticipants);
                if (this.mImsCallMgrInner.getListener() != null) {
                    try {
                        this.mImsCallMgrInner.getListener().onConferenceParticipantsStateChanged(this.mImsCallMgrInner.getImsCall(), conferenceParticipants);
                    } catch (Throwable th) {
                        loge("removeparticipants failed.");
                    }
                }
            }
        }
    }

    public void setIsQcomCEPPresent(boolean isCEPPresent) {
        this.mIsCEPPresent = isCEPPresent;
    }

    public void addQcomConferenceParticipant(ConferenceParticipant conferenceParticipant) {
        if (conferenceParticipant != null && getConferenceParticipants() != null) {
            this.mConferenceParticipants.add(conferenceParticipant);
        }
    }

    public void replaceQcomConferenceParticipantsList() {
        if (shouldSaveQcomParticipantList()) {
            this.mIsCEPPresent = true;
        }
        synchronized (this.mLockObj) {
            List<ConferenceParticipant> curConferenceParticipants = this.mImsCallMgrInner.getConferenceParticipantsForExt();
            curConferenceParticipants.clear();
            if (getConferenceParticipants() != null) {
                if (this.mConferenceParticipants.size() != 0) {
                    curConferenceParticipants.addAll(this.mConferenceParticipants);
                    this.mImsCallMgrInner.setConferenceParticipants(curConferenceParticipants);
                    this.mConferenceParticipants.clear();
                    return;
                }
            }
            this.mImsCallMgrInner.setConferenceParticipants(curConferenceParticipants);
        }
    }

    public Object getQcomLockObj() {
        return this.mLockObj;
    }

    private List<ConferenceParticipant> getConferenceParticipants() {
        if (this.mConferenceParticipants == null) {
            this.mConferenceParticipants = new ArrayList();
        }
        return this.mConferenceParticipants;
    }

    private void logd(String s) {
        Log.d(LOG_TAG, s);
    }

    private void loge(String s) {
        Log.e(LOG_TAG, s);
    }

    private void logi(String s) {
        Log.i(LOG_TAG, s);
    }

    public void hangupForegroundResumeBackground(int reason, IHwImsCallManagerInner imsCallInner) throws ImsException {
        logi("hangupForegroundResumeBackground :: reason=" + reason);
        imsCallInner.setImsCallState(false, false, true);
        if (imsCallInner.getImsCall().getSession() != null) {
            imsCallInner.getImsCall().getSession().hangupForegroundResumeBackground(reason);
        }
    }

    public void hangupWaitingOrBackground(int reason, IHwImsCallManagerInner imsCallInner) throws ImsException {
        logi("hangupWaitingOrBackground :: reason=" + reason);
        imsCallInner.setImsCallState(false, false, true);
        if (imsCallInner.getImsCall().getSession() != null) {
            imsCallInner.getImsCall().getSession().hangupWaitingOrBackground(reason);
        }
    }
}
