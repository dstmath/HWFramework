package com.android.internal.telephony;

import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.TimestampedValue;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.NewTimeServiceHelper;
import com.android.internal.telephony.NitzStateMachine;
import com.android.internal.telephony.TimeZoneLookupHelper;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.util.IndentingPrintWriter;
import com.huawei.internal.telephony.PhoneExt;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class NewNitzStateMachine implements NitzStateMachine {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "NitzStateMachine";
    private static final int NUMERIC_MIN_LEN = 5;
    private static final int VSIM_SUBID = 2;
    private static final String WAKELOCK_TAG = "NitzStateMachine";
    private static boolean mTimeZoneUpdateBlocked = false;
    private final NitzStateMachine.DeviceState mDeviceState;
    private boolean mGotCountryCode;
    private TimestampedValue<NitzData> mLatestNitzSignal;
    private boolean mNitzTimeZoneDetectionSuccessful;
    private final GsmCdmaPhone mPhone;
    private TimestampedValue<Long> mSavedNitzTime;
    private String mSavedTimeZoneId;
    private final LocalLog mTimeLog;
    private final NewTimeServiceHelper mTimeServiceHelper;
    private final LocalLog mTimeZoneLog;
    private final TimeZoneLookupHelper mTimeZoneLookupHelper;
    private final PowerManager.WakeLock mWakeLock;

    public NewNitzStateMachine(GsmCdmaPhone phone) {
        this(phone, new NewTimeServiceHelper(phone.getContext()), new NitzStateMachine.DeviceState(phone), new TimeZoneLookupHelper());
    }

    @VisibleForTesting
    public NewNitzStateMachine(GsmCdmaPhone phone, NewTimeServiceHelper timeServiceHelper, NitzStateMachine.DeviceState deviceState, TimeZoneLookupHelper timeZoneLookupHelper) {
        this.mGotCountryCode = false;
        this.mNitzTimeZoneDetectionSuccessful = false;
        this.mTimeLog = new LocalLog(10);
        this.mTimeZoneLog = new LocalLog(10);
        this.mPhone = phone;
        this.mWakeLock = ((PowerManager) phone.getContext().getSystemService("power")).newWakeLock(1, "NitzStateMachine");
        this.mDeviceState = deviceState;
        this.mTimeZoneLookupHelper = timeZoneLookupHelper;
        this.mTimeServiceHelper = timeServiceHelper;
        this.mTimeServiceHelper.setListener(new NewTimeServiceHelper.Listener() {
            /* class com.android.internal.telephony.NewNitzStateMachine.AnonymousClass1 */

            @Override // com.android.internal.telephony.NewTimeServiceHelper.Listener
            public void onTimeZoneDetectionChange(boolean enabled) {
                if (enabled) {
                    NewNitzStateMachine.this.handleAutoTimeZoneEnabled();
                }
            }
        });
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public void handleNetworkCountryCodeSet(boolean countryChanged) {
        boolean hadCountryCode = this.mGotCountryCode;
        this.mGotCountryCode = true;
        String isoCountryCode = this.mDeviceState.getNetworkCountryIsoForPhone();
        if (!TextUtils.isEmpty(isoCountryCode) && !this.mNitzTimeZoneDetectionSuccessful) {
            updateTimeZoneFromNetworkCountryCode(isoCountryCode);
        }
        if (this.mLatestNitzSignal == null) {
            return;
        }
        if (countryChanged || !hadCountryCode) {
            updateTimeZoneFromCountryAndNitz();
        }
    }

    private void updateTimeZoneFromCountryAndNitz() {
        String logMsg;
        String isoCountryCode = this.mDeviceState.getNetworkCountryIsoForPhone();
        TimestampedValue<NitzData> nitzSignal = this.mLatestNitzSignal;
        boolean isTimeZoneSettingInitialized = this.mTimeServiceHelper.isTimeZoneSettingInitialized();
        Rlog.i("NitzStateMachine", "updateTimeZoneFromCountryAndNitz: isTimeZoneSettingInitialized=" + isTimeZoneSettingInitialized + " nitzSignal=" + nitzSignal + " isoCountryCode=" + isoCountryCode);
        try {
            NitzData nitzData = (NitzData) nitzSignal.getValue();
            if (nitzData.getEmulatorHostTimeZone() != null) {
                logMsg = nitzData.getEmulatorHostTimeZone().getID();
            } else if (!this.mGotCountryCode) {
                logMsg = null;
            } else {
                String str = null;
                if (TextUtils.isEmpty(isoCountryCode)) {
                    TimeZoneLookupHelper.OffsetResult lookupResult = this.mTimeZoneLookupHelper.lookupByNitz(nitzData);
                    String logMsg2 = "updateTimeZoneFromCountryAndNitz: lookupByNitz returned lookupResult=" + lookupResult;
                    Rlog.i("NitzStateMachine", logMsg2);
                    this.mTimeZoneLog.log(logMsg2);
                    if (lookupResult != null) {
                        str = lookupResult.zoneId;
                    }
                    logMsg = str;
                } else if (this.mLatestNitzSignal == null) {
                    Rlog.i("NitzStateMachine", "updateTimeZoneFromCountryAndNitz: No cached NITZ data available, not setting zone");
                    logMsg = null;
                } else if (isNitzSignalOffsetInfoBogus(nitzSignal, isoCountryCode)) {
                    String logMsg3 = "updateTimeZoneFromCountryAndNitz: Received NITZ looks bogus,  isoCountryCode=" + isoCountryCode + " nitzSignal=" + nitzSignal;
                    Rlog.i("NitzStateMachine", logMsg3);
                    this.mTimeZoneLog.log(logMsg3);
                    logMsg = null;
                } else {
                    TimeZoneLookupHelper.OffsetResult lookupResult2 = this.mTimeZoneLookupHelper.lookupByNitzCountry(nitzData, isoCountryCode);
                    Rlog.i("NitzStateMachine", "updateTimeZoneFromCountryAndNitz: using lookupByNitzCountry(nitzData, isoCountryCode), nitzData=" + nitzData + " isoCountryCode=" + isoCountryCode + " lookupResult=" + lookupResult2);
                    if (lookupResult2 != null) {
                        str = lookupResult2.zoneId;
                    }
                    logMsg = str;
                }
            }
            this.mTimeZoneLog.log("updateTimeZoneFromCountryAndNitz: isTimeZoneSettingInitialized=" + isTimeZoneSettingInitialized + " isoCountryCode=" + isoCountryCode + " nitzSignal=" + nitzSignal + " zoneId=" + logMsg + " isTimeZoneDetectionEnabled()=" + this.mTimeServiceHelper.isTimeZoneDetectionEnabled());
            if (logMsg != null) {
                Rlog.i("NitzStateMachine", "updateTimeZoneFromCountryAndNitz: zoneId=" + logMsg);
                if (this.mTimeServiceHelper.isTimeZoneDetectionEnabled()) {
                    setAndBroadcastNetworkSetTimeZone(logMsg);
                    if (!mTimeZoneUpdateBlocked) {
                        if (this.mPhone.getServiceStateTracker() != null) {
                            this.mPhone.getServiceStateTracker().sendNitzTimeZoneUpdateMessage();
                        }
                        HwTelephonyFactory.getHwReportManager().reportSetTimeZoneByNitz(PhoneExt.getPhoneExt(this.mPhone), logMsg, -1, "NitzFix");
                    }
                } else {
                    Rlog.i("NitzStateMachine", "updateTimeZoneFromCountryAndNitz: skip changing zone as isTimeZoneDetectionEnabled() is false");
                }
                this.mSavedTimeZoneId = logMsg;
                this.mNitzTimeZoneDetectionSuccessful = true;
                return;
            }
            Rlog.i("NitzStateMachine", "updateTimeZoneFromCountryAndNitz: zoneId == null, do nothing");
        } catch (RuntimeException ex) {
            Rlog.e("NitzStateMachine", "updateTimeZoneFromCountryAndNitz: Processing NITZ data nitzSignal=" + nitzSignal + " isoCountryCode=" + isoCountryCode + " isTimeZoneSettingInitialized=" + isTimeZoneSettingInitialized + " ex=" + ex);
        }
    }

    private boolean isNitzSignalOffsetInfoBogus(TimestampedValue<NitzData> nitzSignal, String isoCountryCode) {
        if (TextUtils.isEmpty(isoCountryCode)) {
            return false;
        }
        NitzData newNitzData = (NitzData) nitzSignal.getValue();
        if (!(newNitzData.getLocalOffsetMillis() == 0 && !newNitzData.isDst()) || countryUsesUtc(isoCountryCode, nitzSignal)) {
            return false;
        }
        return true;
    }

    private boolean countryUsesUtc(String isoCountryCode, TimestampedValue<NitzData> nitzSignal) {
        return this.mTimeZoneLookupHelper.countryUsesUtc(isoCountryCode, ((NitzData) nitzSignal.getValue()).getCurrentTimeInMillis());
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public void handleNetworkAvailable() {
        Rlog.i("NitzStateMachine", "handleNetworkAvailable: mNitzTimeZoneDetectionSuccessful=" + this.mNitzTimeZoneDetectionSuccessful + ", Setting mNitzTimeZoneDetectionSuccessful=false");
        this.mNitzTimeZoneDetectionSuccessful = false;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public void handleNetworkCountryCodeUnavailable() {
        Rlog.i("NitzStateMachine", "handleNetworkCountryCodeUnavailable");
        this.mGotCountryCode = false;
        this.mNitzTimeZoneDetectionSuccessful = false;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public void handleNitzReceived(TimestampedValue<NitzData> nitzSignal) {
        this.mLatestNitzSignal = nitzSignal;
        updateTimeZoneFromCountryAndNitz();
        updateTimeFromNitz();
    }

    private void updateTimeFromNitz() {
        TimestampedValue<NitzData> nitzSignal = this.mLatestNitzSignal;
        try {
            if (this.mDeviceState.getIgnoreNitz()) {
                Rlog.i("NitzStateMachine", "updateTimeFromNitz: Not suggesting system clock because gsm.ignore-nitz is set");
                return;
            }
            try {
                this.mWakeLock.acquire();
                long elapsedRealtime = this.mTimeServiceHelper.elapsedRealtime();
                long millisSinceNitzReceived = elapsedRealtime - nitzSignal.getReferenceTimeMillis();
                if (millisSinceNitzReceived >= 0) {
                    if (millisSinceNitzReceived <= 2147483647L) {
                        this.mWakeLock.release();
                        long adjustedCurrentTimeMillis = ((NitzData) nitzSignal.getValue()).getCurrentTimeInMillis() + millisSinceNitzReceived;
                        TimestampedValue<Long> newNitzTime = new TimestampedValue<>(nitzSignal.getReferenceTimeMillis(), Long.valueOf(((NitzData) nitzSignal.getValue()).getCurrentTimeInMillis()));
                        if (this.mSavedNitzTime != null) {
                            int nitzUpdateSpacing = this.mDeviceState.getNitzUpdateSpacingMillis();
                            int nitzUpdateDiff = this.mDeviceState.getNitzUpdateDiffMillis();
                            long elapsedRealtimeSinceLastSaved = newNitzTime.getReferenceTimeMillis() - this.mSavedNitzTime.getReferenceTimeMillis();
                            long millisGained = (((Long) newNitzTime.getValue()).longValue() - ((Long) this.mSavedNitzTime.getValue()).longValue()) - elapsedRealtimeSinceLastSaved;
                            if (elapsedRealtimeSinceLastSaved <= ((long) nitzUpdateSpacing)) {
                                if (Math.abs(millisGained) <= ((long) nitzUpdateDiff)) {
                                    Rlog.i("NitzStateMachine", "updateTimeFromNitz: not setting time. NITZ signal is too similar to previous value received  mSavedNitzTime=" + this.mSavedNitzTime + ", nitzSignal=" + nitzSignal + ", nitzUpdateSpacing=" + nitzUpdateSpacing + ", nitzUpdateDiff=" + nitzUpdateDiff);
                                    return;
                                }
                            }
                        }
                        String logMsg = "updateTimeFromNitz: suggesting system clock update nitzSignal=" + nitzSignal + ", newNitzTime=" + newNitzTime + ", mSavedNitzTime= " + this.mSavedNitzTime;
                        Rlog.i("NitzStateMachine", logMsg);
                        this.mTimeLog.log(logMsg);
                        if (!this.mPhone.getServiceStateTracker().allowUpdateTimeFromNitz(((Long) newNitzTime.getValue()).longValue())) {
                            Rlog.i("NitzStateMachine", "not allow update time from nitz");
                            return;
                        }
                        this.mTimeServiceHelper.suggestDeviceTime(newNitzTime);
                        TelephonyMetrics.getInstance().writeNITZEvent(this.mPhone.getPhoneId(), ((Long) newNitzTime.getValue()).longValue());
                        this.mSavedNitzTime = newNitzTime;
                        SystemProperties.set("gsm.nitz.time", String.valueOf(adjustedCurrentTimeMillis));
                        SystemProperties.set("gsm.nitz.timereference", String.valueOf(SystemClock.elapsedRealtime()));
                        return;
                    }
                }
                Rlog.i("NitzStateMachine", "updateTimeFromNitz: not setting time, unexpected elapsedRealtime=" + elapsedRealtime + " nitzSignal=" + nitzSignal);
            } finally {
                this.mWakeLock.release();
            }
        } catch (RuntimeException ex) {
            Rlog.e("NitzStateMachine", "updateTimeFromNitz: Processing NITZ data nitzSignal=" + nitzSignal + " ex=" + ex);
        }
    }

    private void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        Rlog.i("NitzStateMachine", "setAndBroadcastNetworkSetTimeZone: zoneId=" + zoneId);
        if (this.mPhone.getServiceStateTracker().isNeedLocationTimeZoneUpdate(zoneId)) {
            Rlog.i("NitzStateMachine", "there is no need update time zone.");
            mTimeZoneUpdateBlocked = true;
            return;
        }
        mTimeZoneUpdateBlocked = false;
        this.mTimeServiceHelper.setDeviceTimeZone(zoneId);
        Rlog.i("NitzStateMachine", "setAndBroadcastNetworkSetTimeZone: called setDeviceTimeZone() zoneId=" + zoneId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAutoTimeZoneEnabled() {
        int airplaneMode = Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0);
        String tmpLog = "handleAutoTimeZoneEnabled: Reverting to NITZ TimeZone: mSavedTimeZoneId=" + this.mSavedTimeZoneId + " airplaneMode=" + airplaneMode;
        Rlog.i("NitzStateMachine", tmpLog);
        this.mTimeZoneLog.log(tmpLog);
        if (this.mSavedTimeZoneId != null || airplaneMode > 0) {
            setAndBroadcastNetworkSetTimeZone(this.mSavedTimeZoneId);
            HwTelephonyFactory.getHwReportManager().reportSetTimeZoneByNitz(PhoneExt.getPhoneExt(this.mPhone), this.mSavedTimeZoneId, -1, "AutoTZ");
        }
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public void dumpState(PrintWriter pw) {
        pw.println(" mSavedTime=" + this.mSavedNitzTime);
        pw.println(" mLatestNitzSignal=" + this.mLatestNitzSignal);
        pw.println(" mGotCountryCode=" + this.mGotCountryCode);
        pw.println(" mSavedTimeZoneId=" + this.mSavedTimeZoneId);
        pw.println(" mNitzTimeZoneDetectionSuccessful=" + this.mNitzTimeZoneDetectionSuccessful);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.flush();
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public void dumpLogs(FileDescriptor fd, IndentingPrintWriter ipw, String[] args) {
        ipw.println(" Time Logs:");
        ipw.increaseIndent();
        this.mTimeLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Time zone Logs:");
        ipw.increaseIndent();
        this.mTimeZoneLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
    }

    private void updateTimeZoneFromNetworkCountryCode(String iso) {
        String numeric;
        TimeZoneLookupHelper.CountryResult lookupResult = this.mTimeZoneLookupHelper.lookupByCountry(iso, this.mTimeServiceHelper.currentTimeMillis());
        if (this.mPhone.getPhoneId() == 2) {
            numeric = SystemProperties.get("gsm.operator.numeric.vsim", PhoneConfigurationManager.SSSS);
        } else {
            numeric = this.mDeviceState.getMTelephonyManager().getNetworkOperatorForPhone(this.mPhone.getPhoneId());
        }
        String mcc = null;
        if (!isInvalidOperatorNumeric(numeric)) {
            mcc = numeric.substring(0, 3);
        }
        boolean isNeedDefaultZone = isNeedDefaultZone(mcc);
        Rlog.i("NitzStateMachine", "updateTimeZoneByNetworkCountryCode: mcc: " + mcc + "  isNeedDefaultZone: " + isNeedDefaultZone + " lookupResult=" + lookupResult);
        if (lookupResult == null || (!lookupResult.allZonesHaveSameOffset && !isNeedDefaultZone)) {
            Rlog.i("NitzStateMachine", "updateTimeZoneFromNetworkCountryCode: no good zone for iso=" + iso + " lookupResult=" + lookupResult);
            return;
        }
        String logMsg = "updateTimeZoneFromNetworkCountryCode: tz result found iso=" + iso + " lookupResult=" + lookupResult;
        Rlog.i("NitzStateMachine", logMsg);
        this.mTimeZoneLog.log(logMsg);
        String zoneId = !isNeedDefaultZone ? lookupResult.zoneId : getTimeZoneFromMcc(mcc);
        if (this.mTimeServiceHelper.isTimeZoneDetectionEnabled() && !TextUtils.isEmpty(zoneId) && !zoneId.equals(this.mSavedTimeZoneId)) {
            setAndBroadcastNetworkSetTimeZone(zoneId);
            HwTelephonyFactory.getHwReportManager().reportSetTimeZoneByIso(PhoneExt.getPhoneExt(this.mPhone), zoneId, this.mNitzTimeZoneDetectionSuccessful, this.mPhone.getPhoneId() + " Iso");
        }
        this.mSavedTimeZoneId = zoneId;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedDefaultZone(String mcc) {
        if ("460".equals(mcc) || "255".equals(mcc)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isInvalidOperatorNumeric(String operatorNumeric) {
        return operatorNumeric == null || operatorNumeric.length() < 5;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public boolean getNitzTimeZoneDetectionSuccessful() {
        return this.mNitzTimeZoneDetectionSuccessful;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public NitzData getCachedNitzData() {
        TimestampedValue<NitzData> timestampedValue = this.mLatestNitzSignal;
        if (timestampedValue != null) {
            return (NitzData) timestampedValue.getValue();
        }
        return null;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public String getSavedTimeZoneId() {
        return this.mSavedTimeZoneId;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public void handleAutoTimeZoneEnabledHw() {
        handleAutoTimeZoneEnabled();
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public String getTimeZoneFromMcc(String mcc) {
        if ("460".equals(mcc)) {
            return "Asia/Shanghai";
        }
        if ("255".equals(mcc)) {
            return "Europe/Kiev";
        }
        return null;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public long getSavedNitzTime() {
        if (this.mSavedNitzTime == null) {
            return 0;
        }
        return ((Long) this.mSavedNitzTime.getValue()).longValue() + (this.mTimeServiceHelper.elapsedRealtime() - this.mSavedNitzTime.getReferenceTimeMillis());
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public long getNitzSpaceTime() {
        if (this.mSavedNitzTime != null) {
            return this.mTimeServiceHelper.elapsedRealtime() - this.mSavedNitzTime.getReferenceTimeMillis();
        }
        return this.mTimeServiceHelper.elapsedRealtime();
    }
}
