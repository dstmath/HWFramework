package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class UiccCard implements IUiccCardInner {
    protected static final boolean DBG = true;
    private static final int EVENT_CARD_UIM_LOCK = 30;
    public static final String EXTRA_ICC_CARD_ADDED = "com.android.internal.telephony.uicc.ICC_CARD_ADDED";
    protected static final String LOG_TAG = "UiccCard";
    protected String mCardId;
    @UnsupportedAppUsage
    private IccCardStatus.CardState mCardState;
    @UnsupportedAppUsage
    private CommandsInterface mCi;
    @UnsupportedAppUsage
    private Context mContext;
    protected Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.uicc.UiccCard.AnonymousClass1 */

        public void handleMessage(Message msg) {
            if (msg.what != 30) {
                super.handleMessage(msg);
                return;
            }
            UiccCard.this.log("EVENT_CARD_UIM_LOCK");
            UiccCard.this.mIsCardUimLocked = true;
            UiccCard.this.mHwUiccCardEx.displayUimTipDialog(UiccCard.this.mContext, 33685795);
        }
    };
    private IHwUiccCardEx mHwUiccCardEx;
    private String mIccid;
    private boolean mIsCardUimLocked = false;
    @UnsupportedAppUsage
    protected final Object mLock;
    @UnsupportedAppUsage
    private final int mPhoneId;
    private UiccProfile mUiccProfile;

    public UiccCard(Context c, CommandsInterface ci, IccCardStatus ics, int phoneId, Object lock) {
        log("Creating");
        this.mCardState = ics.mCardState;
        this.mPhoneId = phoneId;
        this.mLock = lock;
        update(c, ci, ics);
        this.mHwUiccCardEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwUiccCardEx(this);
        if (this.mPhoneId == 0) {
            ci.registerForUimLockcard(this.mHandler, 30, 0);
            ci.getIccCardStatus(null);
        }
    }

    public void dispose() {
        synchronized (this.mLock) {
            log("Disposing card");
            if (this.mUiccProfile != null) {
                this.mUiccProfile.dispose();
            }
            this.mUiccProfile = null;
            if (this.mPhoneId == 0) {
                this.mCi.unregisterForUimLockcard(this.mHandler);
            }
        }
    }

    public void update(Context c, CommandsInterface ci, IccCardStatus ics) {
        synchronized (this.mLock) {
            this.mCardState = ics.mCardState;
            this.mContext = c;
            this.mCi = ci;
            this.mIccid = ics.iccid;
            updateCardId();
            if (this.mUiccProfile == null) {
                this.mUiccProfile = TelephonyComponentFactory.getInstance().makeUiccProfile(this.mContext, this.mCi, ics, this.mPhoneId, this, this.mLock);
            } else {
                this.mUiccProfile.update(this.mContext, this.mCi, ics);
            }
        }
    }

    public CatService getCatService() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCatService();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        log("UiccCard finalized");
    }

    /* access modifiers changed from: protected */
    public void updateCardId() {
        this.mCardId = this.mIccid;
    }

    @Deprecated
    public void registerForCarrierPrivilegeRulesLoaded(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            if (this.mUiccProfile != null) {
                this.mUiccProfile.registerForCarrierPrivilegeRulesLoaded(h, what, obj);
            } else {
                loge("registerForCarrierPrivilegeRulesLoaded Failed!");
            }
        }
    }

    @Deprecated
    public void unregisterForCarrierPrivilegeRulesLoaded(Handler h) {
        synchronized (this.mLock) {
            if (this.mUiccProfile != null) {
                this.mUiccProfile.unregisterForCarrierPrivilegeRulesLoaded(h);
            } else {
                loge("unregisterForCarrierPrivilegeRulesLoaded Failed!");
            }
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public boolean isApplicationOnIcc(IccCardApplicationStatus.AppType type) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return false;
            }
            return this.mUiccProfile.isApplicationOnIcc(type);
        }
    }

    @UnsupportedAppUsage
    public IccCardStatus.CardState getCardState() {
        return this.mCardState;
    }

    @Deprecated
    public IccCardStatus.PinState getUniversalPinState() {
        synchronized (this.mLock) {
            if (this.mUiccProfile != null) {
                return this.mUiccProfile.getUniversalPinState();
            }
            return IccCardStatus.PinState.PINSTATE_UNKNOWN;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public UiccCardApplication getApplication(int family) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return null;
            }
            return this.mUiccProfile.getApplication(family);
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public UiccCardApplication getApplicationIndex(int index) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return null;
            }
            return this.mUiccProfile.getApplicationIndex(index);
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public UiccCardApplication getApplicationByType(int type) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return null;
            }
            return this.mUiccProfile.getApplicationByType(type);
        }
    }

    @Deprecated
    public boolean resetAppWithAid(String aid, boolean reset) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return false;
            }
            return this.mUiccProfile.resetAppWithAid(aid, reset);
        }
    }

    @Deprecated
    public void iccOpenLogicalChannel(String AID, int p2, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccOpenLogicalChannel(AID, p2, response);
        } else {
            loge("iccOpenLogicalChannel Failed!");
        }
    }

    @Deprecated
    public void iccCloseLogicalChannel(int channel, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccCloseLogicalChannel(channel, response);
        } else {
            loge("iccCloseLogicalChannel Failed!");
        }
    }

    @Deprecated
    public void iccTransmitApduLogicalChannel(int channel, int cla, int command, int p1, int p2, int p3, String data, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccTransmitApduLogicalChannel(channel, cla, command, p1, p2, p3, data, response);
        } else {
            loge("iccTransmitApduLogicalChannel Failed!");
        }
    }

    @Deprecated
    public void iccTransmitApduBasicChannel(int cla, int command, int p1, int p2, int p3, String data, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccTransmitApduBasicChannel(cla, command, p1, p2, p3, data, response);
        } else {
            loge("iccTransmitApduBasicChannel Failed!");
        }
    }

    @Deprecated
    public void iccExchangeSimIO(int fileID, int command, int p1, int p2, int p3, String pathID, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccExchangeSimIO(fileID, command, p1, p2, p3, pathID, response);
        } else {
            loge("iccExchangeSimIO Failed!");
        }
    }

    @Deprecated
    public void sendEnvelopeWithStatus(String contents, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.sendEnvelopeWithStatus(contents, response);
        } else {
            loge("sendEnvelopeWithStatus Failed!");
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public int getNumApplications() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getNumApplications();
        }
        return 0;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public UiccProfile getUiccProfile() {
        return this.mUiccProfile;
    }

    @Deprecated
    public boolean areCarrierPriviligeRulesLoaded() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.areCarrierPriviligeRulesLoaded();
        }
        return false;
    }

    @Deprecated
    public boolean hasCarrierPrivilegeRules() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.hasCarrierPrivilegeRules();
        }
        return false;
    }

    @Deprecated
    public int getCarrierPrivilegeStatus(Signature signature, String packageName) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPrivilegeStatus(signature, packageName);
        }
        return -1;
    }

    @Deprecated
    public int getCarrierPrivilegeStatus(PackageManager packageManager, String packageName) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPrivilegeStatus(packageManager, packageName);
        }
        return -1;
    }

    @Deprecated
    public int getCarrierPrivilegeStatus(PackageInfo packageInfo) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPrivilegeStatus(packageInfo);
        }
        return -1;
    }

    @Deprecated
    public int getCarrierPrivilegeStatusForCurrentTransaction(PackageManager packageManager) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPrivilegeStatusForCurrentTransaction(packageManager);
        }
        return -1;
    }

    @UnsupportedAppUsage
    @Deprecated
    public List<String> getCarrierPackageNamesForIntent(PackageManager packageManager, Intent intent) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPackageNamesForIntent(packageManager, intent);
        }
        return null;
    }

    @Deprecated
    public boolean setOperatorBrandOverride(String brand) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.setOperatorBrandOverride(brand);
        }
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public String getOperatorBrandOverride() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getOperatorBrandOverride();
        }
        return null;
    }

    @UnsupportedAppUsage
    public String getIccId() {
        String str = this.mIccid;
        if (str != null) {
            return str;
        }
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getIccId();
        }
        return null;
    }

    public String getCardId() {
        if (!TextUtils.isEmpty(this.mCardId)) {
            return this.mCardId;
        }
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getIccId();
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void log(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    @UnsupportedAppUsage
    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("UiccCard:");
        pw.println(" mCi=" + this.mCi);
        pw.println(" mCardState=" + this.mCardState);
        pw.println(" mCardId=" + this.mCardId);
        pw.println(" mPhoneId=" + this.mPhoneId);
        pw.println();
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.dump(fd, pw, args);
        }
    }

    @Override // com.android.internal.telephony.uicc.IUiccCardInner
    public int getGsmUmtsSubscriptionAppIndex() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getGsmUmtsSubscriptionAppIndex();
        }
        return -1;
    }

    @Override // com.android.internal.telephony.uicc.IUiccCardInner
    public int getCdmaSubscriptionAppIndex() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCdmaSubscriptionAppIndex();
        }
        return -1;
    }

    @Override // com.android.internal.telephony.uicc.IUiccCardInner
    public boolean isCardUimLocked() {
        return this.mIsCardUimLocked;
    }

    @Override // com.android.internal.telephony.uicc.IUiccCardInner
    public void iccGetATR(Message onComplete) {
        this.mHwUiccCardEx.iccGetATR(onComplete);
    }

    @Override // com.android.internal.telephony.uicc.IUiccCardInner
    public UiccCardExt getUiccCard() {
        UiccCardExt uiccCardExt = new UiccCardExt();
        uiccCardExt.setUiccCard(this);
        return uiccCardExt;
    }
}
