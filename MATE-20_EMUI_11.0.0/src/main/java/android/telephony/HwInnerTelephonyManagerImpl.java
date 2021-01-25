package android.telephony;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.service.euicc.HwEuiccServiceEx;
import android.service.euicc.IHwEuiccServiceEx;
import android.service.euicc.IHwEuiccServiceInner;
import com.android.internal.telephony.HwCallerInfo;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyProperties;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.CellSignalStrengthCdmaEx;
import com.huawei.android.telephony.CellSignalStrengthGsmEx;
import com.huawei.android.telephony.CellSignalStrengthLteEx;
import com.huawei.android.telephony.CellSignalStrengthNrEx;
import com.huawei.android.telephony.CellSignalStrengthWcdmaEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CallerInfoExt;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.telephony.CallerInfoHwUtils;
import huawei.cust.HwCfgFilePolicy;
import java.util.Arrays;

public class HwInnerTelephonyManagerImpl extends DefaultHwInnerTelephonyManager {
    private static final int DBM_INVAILD = -1;
    private static final int DEFAULT_SIGNAL_STRENGTH_LEVEL_RULE = 0;
    public static final int INVALID_ECIO = 255;
    public static final int INVALID_RSSI = -1;
    private static final int INVALID_SIGNAL_STRENGTH_LEVEL = 0;
    private static boolean IS_USE_RSRQ = SystemPropertiesEx.getBoolean("ro.config.lte_use_rsrq", false);
    private static final String KEY_SIGNAL_STRENGTH_LEVEL_RULE = "key_signal_strength_level_rule";
    private static final int MAX_ASU = 31;
    private static final int NSA_STATE2 = 2;
    private static final int NSA_STATE5 = 5;
    private static final int PARMS_CDMA = 5;
    private static final int PARMS_GSM = 3;
    private static final int PARMS_LTE = 6;
    private static final int PARMS_NR = 6;
    private static final int PARMS_WCDMA = 5;
    public static final int PREFERRED_NETWORK_MODE_CFG_NUM = 1;
    private static final int SIGNAL_STRENGTH_LEVEL_RULE_ONE = 1;
    private static final int SIGNAL_STRENGTH_LEVEL_RULE_TWO = 2;
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    private static final String TAG = "HwInnerTelephonyManagerImpl";
    private static final int WCDMA_RSSI_MAX = -51;
    private static final int WCDMA_RSSI_MIN = -113;
    private static HwInnerTelephonyManager sInstance = new HwInnerTelephonyManagerImpl();

    public static HwInnerTelephonyManager getDefault() {
        return sInstance;
    }

    public boolean setDualCardMode(int nMode) {
        SystemPropertiesEx.set("persist.radio.hw.ctmode", Integer.toString(nMode));
        return true;
    }

    public int getDualCardMode() {
        return SystemPropertiesEx.getInt("persist.radio.hw.ctmode", 0);
    }

    public String getPesn() {
        return HwTelephonyManagerInnerUtils.getDefault().getPesn();
    }

    public boolean isSms7BitEnabled() {
        return 1 == SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_SMS_7BIT, 0);
    }

    public boolean isCallerInfofixedIndexValid(String cookie, Cursor cursor) {
        return CallerInfoHwUtils.isFixedIndexValid(cookie, cursor);
    }

    public String custExtraEmergencyNumbers(long subId, String numbers) {
        return HwPhoneNumberUtils.custExtraNumbers(subId, numbers);
    }

    public boolean skipHardcodeEmergencyNumbers() {
        return HwPhoneNumberUtils.skipHardcodeNumbers();
    }

    public boolean isVoiceMailNumber(String number) {
        return HwPhoneNumberUtils.isVoiceMailNumber(number);
    }

    public boolean useVoiceMailNumberFeature() {
        return HwPhoneNumberUtils.useVoiceMailNumberFeature();
    }

    public CallerInfoExt getCallerInfo(Context context, Uri contactRef, Cursor cursor, String compNum) {
        return HwCallerInfo.getCallerInfo(context, contactRef, cursor, compNum);
    }

    public boolean isMultiSimEnabled() {
        return TelephonyManagerEx.isMultiSimEnabled();
    }

    public boolean isCustomProcess() {
        return HwPhoneNumberUtils.isCustomProcess();
    }

    public String stripBrackets(String number) {
        return HwPhoneNumberUtils.stripBrackets(number);
    }

    public int[] getSingleShiftTable(Resources r) {
        int[] temp = new int[1];
        if (SystemPropertiesEx.getInt("ro.config.smsCoding_National", 0) != 0) {
            temp[0] = SystemPropertiesEx.getInt("ro.config.smsCoding_National", 0);
        } else if (SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_SMS_CODING, 0) != 0) {
            temp[0] = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_SMS_CODING, 0);
        } else if (r != null) {
            return r.getIntArray(HwPartResourceUtils.getResourceId("config_sms_enabled_single_shift_tables"));
        }
        return temp;
    }

    public boolean isHwCustNotEmergencyNumber(Context context, String number) {
        return HwPhoneNumberUtils.isHwCustNotEmergencyNumber(context, number);
    }

    public String getOperatorNumeric() {
        int type = TelephonyManagerEx.getCurrentPhoneType();
        if (type == 1) {
            return SystemPropertiesEx.get("gsm.sim.operator.numeric");
        }
        if (type != 2) {
            return null;
        }
        return SystemPropertiesEx.get("ro.cdma.home.operator.numeric");
    }

    public boolean useHwSignalStrength() {
        return true;
    }

    @Deprecated
    public int getGsmAsuLevel(SignalStrength strength) {
        int dbm = SignalStrengthEx.getGsmDbm(strength);
        if (dbm == -1 || dbm == 0 || dbm == Integer.MAX_VALUE) {
            return -1;
        }
        int asu = (dbm + 113) / 2;
        if (asu < 0) {
            return 0;
        }
        if (asu > MAX_ASU) {
            return MAX_ASU;
        }
        return asu;
    }

    public int getNrLevel(CellSignalStrengthNr strength) {
        return HwTelephonyManagerInnerUtils.getDefault().getNrLevel(strength);
    }

    public boolean isRemoveSeparateOnSK() {
        return HwPhoneNumberUtils.isRemoveSeparateOnSK();
    }

    public String removeAllSeparate(String number) {
        return HwPhoneNumberUtils.removeAllSeparate(number);
    }

    public int getNewRememberedPos(int rememberedPos, String formatted) {
        return HwPhoneNumberUtils.getNewRememberedPos(rememberedPos, formatted);
    }

    public boolean isCustRemoveSep() {
        return HwPhoneNumberUtils.isCustRemoveSep();
    }

    public int getDefault4GSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public int getCardType(int slotId) {
        return HwTelephonyManagerInner.getDefault().getCardType(slotId);
    }

    public void updateCrurrentPhone(int lteSlot) {
        HwTelephonyManagerInner.getDefault().updateCrurrentPhone(lteSlot);
    }

    public void setDefaultDataSlotId(int slotId) {
        HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(slotId);
    }

    public int getLteServiceAbility() {
        return HwTelephonyManagerInner.getDefault().getLteServiceAbility();
    }

    public void setLteServiceAbility(int ability) {
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(ability);
    }

    public boolean isFullNetworkSupported() {
        return HwTelephonyManagerInner.getDefault().isFullNetworkSupported();
    }

    public void printCallingAppNameInfo(boolean enable, Context context) {
        HwTelephonyManagerInnerUtils.getDefault().printCallingAppNameInfo(enable, context);
    }

    public String getUniqueDeviceId(int scope, String callingPackageName) {
        return HwTelephonyManagerInnerUtils.getDefault().getUniqueDeviceId(scope, callingPackageName);
    }

    public String getCallingAppName(Context context) {
        return HwTelephonyManagerInnerUtils.getDefault().getCallingAppName(context);
    }

    public boolean isLongVoiceMailNumber(int subId, String number) {
        return HwPhoneNumberUtils.isLongVoiceMailNumber(subId, number);
    }

    public boolean isVSimEnabled() {
        return HwVSimManager.getDefault().isVSimEnabled();
    }

    @Deprecated
    public void validateInput(SignalStrength newSignalStrength) {
        RlogEx.d(TAG, "Signal before HW validate=" + newSignalStrength);
        if (newSignalStrength != null) {
            SignalStrengthEx.setGsmSignalStrength(newSignalStrength, newSignalStrength.getGsmSignalStrength() > 0 ? newSignalStrength.getGsmSignalStrength() * -1 : -1);
            SignalStrengthEx.setWcdmaRscp(newSignalStrength, SignalStrengthEx.getWcdmaRscp(newSignalStrength) > 0 ? SignalStrengthEx.getWcdmaRscp(newSignalStrength) * -1 : -1);
            int i = 255;
            SignalStrengthEx.setWcdmaEcio(newSignalStrength, SignalStrengthEx.getWcdmaEcio(newSignalStrength) >= 0 ? SignalStrengthEx.getWcdmaEcio(newSignalStrength) * -1 : 255);
            SignalStrengthEx.setCdmaDbm(newSignalStrength, newSignalStrength.getCdmaDbm() > 0 ? newSignalStrength.getCdmaDbm() * -1 : -1);
            SignalStrengthEx.setCdmaEcio(newSignalStrength, newSignalStrength.getCdmaEcio() > 0 ? newSignalStrength.getCdmaEcio() * -1 : 255);
            SignalStrengthEx.setEvdoDbm(newSignalStrength, (newSignalStrength.getEvdoDbm() <= 0 || newSignalStrength.getEvdoDbm() >= 125) ? -1 : newSignalStrength.getEvdoDbm() * -1);
            SignalStrengthEx.setEvdoEcio(newSignalStrength, newSignalStrength.getEvdoEcio() >= 0 ? newSignalStrength.getEvdoEcio() * -1 : 255);
            if (newSignalStrength.getEvdoSnr() > 0 && newSignalStrength.getEvdoSnr() <= 8) {
                i = newSignalStrength.getEvdoSnr();
            }
            SignalStrengthEx.setEvdoSnr(newSignalStrength, i);
            SignalStrengthEx.setLteSignalStrength(newSignalStrength, SignalStrengthEx.getLteSignalStrength(newSignalStrength) >= 0 ? SignalStrengthEx.getLteSignalStrength(newSignalStrength) : 99);
            int i2 = Integer.MAX_VALUE;
            SignalStrengthEx.setLteRsrp(newSignalStrength, (SignalStrengthEx.getLteRsrp(newSignalStrength) < 44 || SignalStrengthEx.getLteRsrp(newSignalStrength) > 140) ? Integer.MAX_VALUE : SignalStrengthEx.getLteRsrp(newSignalStrength) * -1);
            SignalStrengthEx.setLteRsrq(newSignalStrength, (SignalStrengthEx.getLteRsrq(newSignalStrength) < 3 || SignalStrengthEx.getLteRsrq(newSignalStrength) > 20) ? Integer.MAX_VALUE : SignalStrengthEx.getLteRsrq(newSignalStrength) * -1);
            if (SignalStrengthEx.getLteRssnr(newSignalStrength) >= -200 && SignalStrengthEx.getLteRssnr(newSignalStrength) <= 300) {
                i2 = SignalStrengthEx.getLteRssnr(newSignalStrength);
            }
            SignalStrengthEx.setLteRssnr(newSignalStrength, i2);
            RlogEx.d(TAG, "Signal after HW validate=" + newSignalStrength);
        }
    }

    public String convertPlusByMcc(String number, int mcc) {
        return HwPhoneNumberUtils.convertPlusByMcc(number, mcc);
    }

    public String updatePreferNetworkModeValArray(String networkMode, String name) {
        String vNew = networkMode;
        if (!isConfigValid(networkMode, name)) {
            return vNew;
        }
        String[] valArray = networkMode.split(",");
        if (1 != valArray.length) {
            return vNew;
        }
        int subId = getDefault4GSlotId();
        if (subId == 0) {
            vNew = valArray[0] + ",1";
        } else if (1 == subId) {
            vNew = "1," + valArray[0];
        }
        RlogEx.d(TAG, "updatePreferNetworkModeValArray: vNew = " + vNew + ", sub = " + subId);
        return vNew;
    }

    private boolean isConfigValid(String networkMode, String name) {
        if (!"preferred_network_mode".equals(name) || !TelephonyManagerEx.isMultiSimEnabled() || networkMode == null) {
            return false;
        }
        RlogEx.d(TAG, "updatePreferNetworkModeValArray: networkMode = " + networkMode + ", name = " + name);
        return true;
    }

    public int getLevelHw(CellSignalStrength signalStrength) {
        return HwTelephonyManagerInnerUtils.getDefault().getLevelHw(signalStrength);
    }

    public int getGsmAsuLevel(CellSignalStrengthGsm strength) {
        int dbm = strength != null ? strength.getDbm() : -1;
        if (dbm == -1 || dbm == 0 || dbm == Integer.MAX_VALUE) {
            return -1;
        }
        int asu = (dbm + 113) / 2;
        if (asu < 0) {
            return 0;
        }
        if (asu > MAX_ASU) {
            return MAX_ASU;
        }
        return asu;
    }

    public int getEvdoLevel(CellSignalStrengthCdma strength) {
        return HwTelephonyManagerInnerUtils.getDefault().getEvdoLevel(strength);
    }

    public int getCdmaLevel(CellSignalStrengthCdma strength) {
        return HwTelephonyManagerInnerUtils.getDefault().getCdmaLevel(strength);
    }

    public void validateInput(CellSignalStrength strength, int... parms) {
        RlogEx.d(TAG, "CellSignalStrength parms=" + Arrays.toString(parms));
        if ((strength instanceof CellSignalStrengthNr) && parms.length >= 6) {
            validateInputNr((CellSignalStrengthNr) strength, parms);
        } else if ((strength instanceof CellSignalStrengthLte) && parms.length >= 6) {
            validateInputLte((CellSignalStrengthLte) strength, parms);
        } else if ((strength instanceof CellSignalStrengthCdma) && parms.length >= 5) {
            validateInputCdma((CellSignalStrengthCdma) strength, parms);
        } else if ((strength instanceof CellSignalStrengthWcdma) && parms.length >= 5) {
            validateInputWcdma((CellSignalStrengthWcdma) strength, parms);
        } else if (!(strength instanceof CellSignalStrengthGsm) || parms.length < 3) {
            RlogEx.d(TAG, "Unsupported CellSignalStrength=" + strength);
        } else {
            validateInputGsm((CellSignalStrengthGsm) strength, parms);
        }
        RlogEx.d(TAG, "Signal after HW validate=" + strength);
    }

    private void validateInputNr(CellSignalStrengthNr strength, int... parms) {
        int csiRsrp = parms[0];
        int csiRsrq = parms[1];
        int csiSinr = parms[2];
        int ssRsrp = parms[3];
        int ssRsrq = parms[4];
        int ssSinr = parms[5];
        CellSignalStrengthNrEx.setNrSignalStrength(strength, 0, (csiRsrp < 44 || csiRsrp > 140) ? Integer.MAX_VALUE : csiRsrp * -1);
        CellSignalStrengthNrEx.setNrSignalStrength(strength, 1, (csiRsrq < 3 || csiRsrq > 20) ? Integer.MAX_VALUE : csiRsrq * -1);
        CellSignalStrengthNrEx.setNrSignalStrength(strength, 2, (csiSinr < -23 || csiSinr > 23) ? Integer.MAX_VALUE : csiSinr * -1);
        CellSignalStrengthNrEx.setNrSignalStrength(strength, 3, (ssRsrp < 44 || ssRsrp > 140) ? Integer.MAX_VALUE : ssRsrp * -1);
        CellSignalStrengthNrEx.setNrSignalStrength(strength, 4, (ssRsrq < 3 || ssRsrq > 20) ? Integer.MAX_VALUE : ssRsrq * -1);
        CellSignalStrengthNrEx.setNrSignalStrength(strength, 5, (ssSinr < -40 || ssSinr > 23) ? Integer.MAX_VALUE : ssSinr * -1);
    }

    private void validateInputLte(CellSignalStrengthLte strength, int... parms) {
        int rssi = parms[0];
        int rsrp = parms[1];
        int rsrq = parms[2];
        int rssnr = parms[3];
        int cqi = parms[4];
        int timingAdvance = parms[5];
        int i = Integer.MAX_VALUE;
        CellSignalStrengthLteEx.setRssi(strength, (rssi < 51 || rssi > 113) ? Integer.MAX_VALUE : rssi * -1);
        CellSignalStrengthLteEx.setRsrp(strength, (rsrp < 44 || rsrp > 140) ? Integer.MAX_VALUE : rsrp * -1);
        CellSignalStrengthLteEx.setRsrq(strength, (rsrq < 3 || rsrq > 20) ? Integer.MAX_VALUE : rsrq * -1);
        if (rssnr >= -200 && rssnr <= 300) {
            i = rssnr;
        }
        CellSignalStrengthLteEx.setRssnr(strength, i);
        CellSignalStrengthLteEx.setCqi(strength, HwInnerTelephonyManagerImplUtils.inRangeOrUnavailable(cqi, 0, 15));
        CellSignalStrengthLteEx.setTimingAdvance(strength, HwInnerTelephonyManagerImplUtils.inRangeOrUnavailable(timingAdvance, 0, 1282));
    }

    private void validateInputCdma(CellSignalStrengthCdma strength, int... parms) {
        int cdmaDbm = parms[0];
        int cdmaEcio = parms[1];
        int evdoDbm = parms[2];
        int evdoEcio = parms[3];
        int evdoSnr = parms[4];
        int i = Integer.MAX_VALUE;
        CellSignalStrengthCdmaEx.setCdmaDbm(strength, (cdmaDbm <= 0 || cdmaDbm == Integer.MAX_VALUE) ? Integer.MAX_VALUE : cdmaDbm * -1);
        CellSignalStrengthCdmaEx.setCdmaEcio(strength, (cdmaEcio <= 0 || cdmaEcio == Integer.MAX_VALUE) ? Integer.MAX_VALUE : cdmaEcio * -1);
        CellSignalStrengthCdmaEx.setEvdoDbm(strength, (evdoDbm <= 0 || evdoDbm >= 125 || evdoDbm == Integer.MAX_VALUE) ? Integer.MAX_VALUE : evdoDbm * -1);
        CellSignalStrengthCdmaEx.setEvdoEcio(strength, (evdoEcio < 0 || evdoEcio == Integer.MAX_VALUE) ? Integer.MAX_VALUE : evdoEcio * -1);
        if (evdoSnr > 0 && evdoSnr <= 8 && evdoSnr != Integer.MAX_VALUE) {
            i = evdoSnr;
        }
        CellSignalStrengthCdmaEx.setEvdoSnr(strength, i);
    }

    private void validateInputWcdma(CellSignalStrengthWcdma strength, int... parms) {
        int rssi = parms[0];
        int ber = parms[1];
        int rscp = parms[2];
        int ecno = parms[3];
        int ecio = parms[4];
        int i = Integer.MAX_VALUE;
        CellSignalStrengthWcdmaEx.setRscp(strength, (rscp <= 0 || rscp == Integer.MAX_VALUE) ? Integer.MAX_VALUE : rscp * -1);
        if (ecio >= 0 && ecio != Integer.MAX_VALUE) {
            i = ecio * -1;
        }
        CellSignalStrengthWcdmaEx.setEcio(strength, i);
        CellSignalStrengthWcdmaEx.setRssi(strength, HwInnerTelephonyManagerImplUtils.inRangeOrUnavailable(rssi, (int) WCDMA_RSSI_MIN, (int) WCDMA_RSSI_MAX));
        CellSignalStrengthWcdmaEx.setBitErrorRate(strength, HwInnerTelephonyManagerImplUtils.inRangeOrUnavailable(ber, 0, 7, 99));
        CellSignalStrengthWcdmaEx.setEcNo(strength, HwInnerTelephonyManagerImplUtils.inRangeOrUnavailable(ecno, -24, 1));
    }

    private void validateInputGsm(CellSignalStrengthGsm strength, int... parms) {
        int rssi = parms[0];
        int ber = parms[1];
        int ta = parms[2];
        int i = Integer.MAX_VALUE;
        if (rssi > 0 && rssi != Integer.MAX_VALUE) {
            i = rssi * -1;
        }
        CellSignalStrengthGsmEx.setRssi(strength, i);
        CellSignalStrengthGsmEx.setBitErrorRate(strength, HwInnerTelephonyManagerImplUtils.inRangeOrUnavailable(ber, 0, 7, 99));
        CellSignalStrengthGsmEx.setTimingAdvance(strength, HwInnerTelephonyManagerImplUtils.inRangeOrUnavailable(ta, 0, 219));
    }

    public IHwEuiccServiceEx getHwEuiccServiceEx(IHwEuiccServiceInner euiccService) {
        return new HwEuiccServiceEx(euiccService);
    }

    private int getLevelByRule(SignalStrength signalStrength) {
        Integer rule = (Integer) HwCfgFilePolicy.getValue(KEY_SIGNAL_STRENGTH_LEVEL_RULE, SignalStrengthEx.getPhoneId(signalStrength), Integer.class);
        int levelRule = rule != null ? rule.intValue() : 0;
        if (levelRule == 0) {
            int lteLevel = CellSignalStrengthLteEx.getLevelHw(signalStrength);
            int nrLevel = CellSignalStrengthNrEx.getLevelHw(signalStrength);
            if (nrLevel == 0) {
                return SignalStrengthEx.getPrimaryLevelHw(signalStrength);
            }
            return lteLevel > nrLevel ? lteLevel : nrLevel;
        } else if (levelRule == 1) {
            return SignalStrengthEx.getPrimaryLevelHw(signalStrength);
        } else {
            if (levelRule == 2) {
                int nrLevel2 = CellSignalStrengthNrEx.getLevelHw(signalStrength);
                if (nrLevel2 == 0) {
                    return SignalStrengthEx.getPrimaryLevelHw(signalStrength);
                }
                return nrLevel2;
            }
            RlogEx.d(TAG, "unknown level rule.");
            return 0;
        }
    }

    public boolean isNrSlicesSupported() {
        return HwTelephonyManagerInnerUtils.getDefault().isNrSlicesSupported();
    }

    public String getCTOperator(int slotId, String operator) {
        return HwTelephonyManagerInnerUtils.getDefault().getCtOperator(slotId, operator);
    }

    public int getLevelHw(SignalStrength signalStrength) {
        int phoneId = SignalStrengthEx.getPhoneId(signalStrength);
        if (HwModemCapability.isCapabilitySupport(29)) {
            int state = HwTelephonyManager.getDefault().getNetworkMode(phoneId);
            if (state == 1) {
                return getLevelByRule(signalStrength);
            }
            if (state == 2) {
                return SignalStrengthEx.getNrLevel(signalStrength);
            }
            RlogEx.d(TAG, "unknown network mode.");
        }
        return SignalStrengthEx.getPrimaryLevelHw(signalStrength);
    }
}
