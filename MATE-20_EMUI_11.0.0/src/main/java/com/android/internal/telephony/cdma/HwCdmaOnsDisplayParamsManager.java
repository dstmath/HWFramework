package com.android.internal.telephony.cdma;

import android.content.res.Resources;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwBaseOnsDisplayParamsManager;
import com.android.internal.telephony.HwPlmnActConcat;
import com.android.internal.telephony.HwServiceStateTrackerEx;
import com.android.internal.telephony.IServiceStateTrackerInner;
import com.android.internal.telephony.OnsDisplayParams;
import com.android.internal.telephony.PlmnConstants;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.utils.HwPartResourceUtils;
import huawei.cust.HwCustUtils;
import java.util.Locale;

public class HwCdmaOnsDisplayParamsManager extends HwBaseOnsDisplayParamsManager {
    private static final String CDMA_HOME_OPERATOR_NUMERIC = SystemPropertiesEx.get("ro.cdma.home.operator.numeric");
    public static final int CT_SID_FIRST_END = 14335;
    public static final int CT_SID_FIRST_START = 13568;
    public static final int CT_SID_SECOND_END = 26111;
    public static final int CT_SID_SECOND_START = 25600;
    private static final String DEFAULT_PLMN_LANG_EN = "en_us";
    private static final String INVAILD_PLMN = "1023127-123456-1023456-123127-99999-";
    private static final int INVALID_NUMBER = -1;
    private static final boolean IS_HISI_PLATFORM = HuaweiTelephonyConfigs.isHisiPlatform();
    private static final boolean IS_MTK_PLATFORM = HuaweiTelephonyConfigs.isMTKPlatform();
    private static final boolean IS_SET_UICC_BY_RADIO_POWER = SystemPropertiesEx.getBoolean("ro.hwpp.set_uicc_by_radiopower", false);
    private static final String LOG_TAG = "HwCdmaOnsDisplayParamsManager";
    private static final String SPRINT_OPERATOR = "310000";
    private static final String SPRINT_OPERATOR_ALPHA_LONG = "Sprint";
    private static final String[] USA_MCC_LISTS = {"332", "310", "311", "312", "313", "314", "315", "316", "544"};
    private HwCustCdmaServiceStateManager mHwCustCdmaServiceStateManager = ((HwCustCdmaServiceStateManager) HwCustUtils.createObj(HwCustCdmaServiceStateManager.class, new Object[0]));

    public HwCdmaOnsDisplayParamsManager(IServiceStateTrackerInner serviceStateTracker, PhoneExt phoneExt, HwServiceStateTrackerEx hwServiceStateTrackerEx) {
        super(serviceStateTracker, phoneExt, hwServiceStateTrackerEx);
        this.mTag = "HwCdmaOnsDisplayParamsManager[" + this.mPhoneId + "]";
    }

    public OnsDisplayParams getOnsDisplayParamsHw() {
        String plmn = getCdmaPlmn();
        OnsDisplayParams odp = new OnsDisplayParams(false, plmn != null, 0, plmn, (String) null);
        if (this.mPhone.isWifiCallingEnabled()) {
            odp = getOnsDisplayParamsForVoWifi(odp);
        }
        this.mHwServiceStateTrackerEx.setVowifi(odp.mShowWifi, odp.mWifi);
        return odp;
    }

    private String getCdmaPlmn() {
        String operatorNumeric = getSs().getOperatorNumeric();
        boolean isOutServiceOrDeactived = false;
        if (!TextUtils.isEmpty(operatorNumeric) && isInvalidPlmn(operatorNumeric)) {
            int systemId = getSs().getCdmaSystemId();
            if ((systemId >= 13568 && systemId <= 14335) || (systemId >= 25600 && systemId <= 26111)) {
                operatorNumeric = "46003";
            }
        }
        String data = null;
        try {
            data = Settings.System.getString(this.mContext.getContentResolver(), "plmn");
        } catch (Exception e) {
            loge("Exception when got data value");
        }
        PlmnConstants plmnConstants = new PlmnConstants(data);
        String plmnValue = plmnConstants.getPlmnValue(operatorNumeric, Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry());
        if (plmnValue == null) {
            plmnValue = plmnConstants.getPlmnValue(operatorNumeric, DEFAULT_PLMN_LANG_EN);
            logd("get default en_us plmn name:" + plmnValue);
        }
        if (plmnValue != null) {
            ServiceStateEx.setOperatorAlphaLong(getSs(), plmnValue);
        } else {
            plmnValue = getSs().getOperatorAlphaLong();
            int voiceRat = ServiceStateEx.getRilVoiceRadioTechnology(getSs());
            if (SPRINT_OPERATOR.equals(CDMA_HOME_OPERATOR_NUMERIC) && !TextUtils.isEmpty(operatorNumeric) && (voiceRat == 4 || voiceRat == 5 || voiceRat == 6)) {
                int count = USA_MCC_LISTS.length;
                int i = 0;
                while (true) {
                    if (i >= count) {
                        break;
                    } else if (operatorNumeric.startsWith(USA_MCC_LISTS[i])) {
                        plmnValue = SPRINT_OPERATOR_ALPHA_LONG;
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
        if (this.mHwServiceStateTrackerEx.getCombinedRegState(getSs()) == 1 || isCardDeactived(getSs())) {
            isOutServiceOrDeactived = true;
        }
        if (isOutServiceOrDeactived) {
            plmnValue = Resources.getSystem().getText(HwPartResourceUtils.getResourceId("lockscreen_carrier_default")).toString();
            logd("CDMA is out of service. plmnValue = " + plmnValue);
        } else if (HwPlmnActConcat.needPlmnActConcat()) {
            plmnValue = HwPlmnActConcat.getPlmnActConcat(plmnValue, getSs());
        }
        HwCustCdmaServiceStateManager hwCustCdmaServiceStateManager = this.mHwCustCdmaServiceStateManager;
        if (hwCustCdmaServiceStateManager != null) {
            return hwCustCdmaServiceStateManager.setEriBasedPlmn(this.mPhone, plmnValue);
        }
        return plmnValue;
    }

    private boolean isInvalidPlmn(String mccmnc) {
        StringBuilder sb = new StringBuilder();
        sb.append(mccmnc);
        sb.append("-");
        return INVAILD_PLMN.indexOf(sb.toString()) != -1;
    }

    private boolean isCardDeactived(ServiceState serviceState) {
        boolean isAirplaneModeOn = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
        boolean isDsDs = TelephonyManagerEx.getMultiSimConfiguration() == TelephonyManagerEx.MultiSimVariantsExt.DSDS;
        if (isAirplaneModeOn || ((!IS_HISI_PLATFORM && !IS_MTK_PLATFORM) || ((!isDsDs && !IS_SET_UICC_BY_RADIO_POWER) || ServiceStateEx.getVoiceRegState(serviceState) != 3))) {
            return false;
        }
        logd("CT card is deactived, should display out of service.");
        return true;
    }
}
