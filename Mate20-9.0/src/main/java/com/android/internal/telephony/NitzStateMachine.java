package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.TimeUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.TimeServiceHelper;
import com.android.internal.telephony.TimeZoneLookupHelper;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.util.TimeStampedValue;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.TimeZone;

public class NitzStateMachine {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "NitzStateMachine";
    private static final int NUMERIC_MIN_LEN = 5;
    private static final int VSIM_SUBID = 2;
    private static final String WAKELOCK_TAG = "NitzStateMachine";
    private static boolean mTimeZoneUpdateBlocked = false;
    private final DeviceState mDeviceState;
    private boolean mGotCountryCode;
    private TimeStampedValue<NitzData> mLatestNitzSignal;
    private boolean mNeedCountryCodeForNitz;
    private boolean mNitzTimeZoneDetectionSuccessful;
    private final GsmCdmaPhone mPhone;
    private TimeStampedValue<Long> mSavedNitzTime;
    private String mSavedTimeZoneId;
    private final LocalLog mTimeLog;
    private final TimeServiceHelper mTimeServiceHelper;
    private final LocalLog mTimeZoneLog;
    private final TimeZoneLookupHelper mTimeZoneLookupHelper;
    private final PowerManager.WakeLock mWakeLock;

    public static class DeviceState {
        private static final int NITZ_UPDATE_DIFF_DEFAULT = 2000;
        private static final int NITZ_UPDATE_SPACING_DEFAULT = 600000;
        private final ContentResolver mCr;
        private final int mNitzUpdateDiff = SystemProperties.getInt("ro.nitz_update_diff", 2000);
        private final int mNitzUpdateSpacing = SystemProperties.getInt("ro.nitz_update_spacing", NITZ_UPDATE_SPACING_DEFAULT);
        private final GsmCdmaPhone mPhone;
        /* access modifiers changed from: private */
        public final TelephonyManager mTelephonyManager;

        public DeviceState(GsmCdmaPhone phone) {
            this.mPhone = phone;
            Context context = phone.getContext();
            this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
            this.mCr = context.getContentResolver();
        }

        public int getNitzUpdateSpacingMillis() {
            return Settings.Global.getInt(this.mCr, "nitz_update_spacing", this.mNitzUpdateSpacing);
        }

        public int getNitzUpdateDiffMillis() {
            return Settings.Global.getInt(this.mCr, "nitz_update_diff", this.mNitzUpdateDiff);
        }

        public boolean getIgnoreNitz() {
            String ignoreNitz = SystemProperties.get("gsm.ignore-nitz");
            return ignoreNitz != null && ignoreNitz.equals("yes");
        }

        public String getNetworkCountryIsoForPhone() {
            if (this.mPhone.getPhoneId() == 2) {
                return SystemProperties.get("gsm.operator.iso-country.vsim", "");
            }
            return this.mTelephonyManager.getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
        }
    }

    public NitzStateMachine(GsmCdmaPhone phone) {
        this(phone, new TimeServiceHelper(phone.getContext()), new DeviceState(phone), new TimeZoneLookupHelper());
    }

    @VisibleForTesting
    public NitzStateMachine(GsmCdmaPhone phone, TimeServiceHelper timeServiceHelper, DeviceState deviceState, TimeZoneLookupHelper timeZoneLookupHelper) {
        this.mNeedCountryCodeForNitz = false;
        this.mGotCountryCode = false;
        this.mNitzTimeZoneDetectionSuccessful = false;
        this.mTimeLog = new LocalLog(15);
        this.mTimeZoneLog = new LocalLog(15);
        this.mPhone = phone;
        this.mWakeLock = ((PowerManager) phone.getContext().getSystemService("power")).newWakeLock(1, "NitzStateMachine");
        this.mDeviceState = deviceState;
        this.mTimeZoneLookupHelper = timeZoneLookupHelper;
        this.mTimeServiceHelper = timeServiceHelper;
        this.mTimeServiceHelper.setListener(new TimeServiceHelper.Listener() {
            public void onTimeDetectionChange(boolean enabled) {
                if (enabled) {
                    NitzStateMachine.this.handleAutoTimeEnabled();
                }
            }

            public void onTimeZoneDetectionChange(boolean enabled) {
                if (enabled) {
                    NitzStateMachine.this.handleAutoTimeZoneEnabled();
                }
            }
        });
    }

    public void handleNetworkCountryCodeSet(boolean countryChanged) {
        String zoneId;
        String zoneId2;
        this.mGotCountryCode = true;
        String isoCountryCode = this.mDeviceState.getNetworkCountryIsoForPhone();
        if (!TextUtils.isEmpty(isoCountryCode) && !this.mNitzTimeZoneDetectionSuccessful && this.mTimeServiceHelper.isTimeZoneDetectionEnabled()) {
            updateTimeZoneByNetworkCountryCode(isoCountryCode);
        }
        if (countryChanged || this.mNeedCountryCodeForNitz) {
            boolean isTimeZoneSettingInitialized = this.mTimeServiceHelper.isTimeZoneSettingInitialized();
            Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet: isTimeZoneSettingInitialized=" + isTimeZoneSettingInitialized + " mLatestNitzSignal=" + this.mLatestNitzSignal + " isoCountryCode=" + isoCountryCode);
            boolean fixZoneByNitz = this.mNeedCountryCodeForNitz;
            String str = null;
            if (TextUtils.isEmpty(isoCountryCode) && this.mNeedCountryCodeForNitz) {
                TimeZoneLookupHelper.OffsetResult lookupResult = this.mTimeZoneLookupHelper.lookupByNitz((NitzData) this.mLatestNitzSignal.mValue);
                Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet: guessZoneIdByNitz() returned lookupResult=" + lookupResult);
                if (lookupResult != null) {
                    str = lookupResult.zoneId;
                }
                zoneId = str;
            } else if (this.mLatestNitzSignal == null) {
                zoneId = null;
                Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet: No cached NITZ data available, not setting zone");
            } else if (!nitzOffsetMightBeBogus((NitzData) this.mLatestNitzSignal.mValue) || !isTimeZoneSettingInitialized || countryUsesUtc(isoCountryCode, this.mLatestNitzSignal)) {
                NitzData nitzData = (NitzData) this.mLatestNitzSignal.mValue;
                TimeZoneLookupHelper.OffsetResult lookupResult2 = this.mTimeZoneLookupHelper.lookupByNitzCountry(nitzData, isoCountryCode);
                Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet: using guessZoneIdByNitzCountry(nitzData, isoCountryCode), nitzData=" + nitzData + " isoCountryCode=" + isoCountryCode + " lookupResult=" + lookupResult2);
                if (lookupResult2 != null) {
                    str = lookupResult2.zoneId;
                }
                zoneId = str;
            } else {
                TimeZone zone = TimeZone.getDefault();
                Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet: NITZ looks bogus, maybe using current default zone to adjust the system clock, mNeedCountryCodeForNitz=" + this.mNeedCountryCodeForNitz + " mLatestNitzSignal=" + this.mLatestNitzSignal + " zone=" + zone);
                String zoneId3 = zone.getID();
                if (this.mNeedCountryCodeForNitz) {
                    NitzData nitzData2 = (NitzData) this.mLatestNitzSignal.mValue;
                    try {
                        this.mWakeLock.acquire();
                        long delayAdjustedCtm = (this.mTimeServiceHelper.elapsedRealtime() - this.mLatestNitzSignal.mElapsedRealtime) + nitzData2.getCurrentTimeInMillis();
                        long tzOffset = (long) zone.getOffset(delayAdjustedCtm);
                        Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet: tzOffset=" + tzOffset + " delayAdjustedCtm=" + TimeUtils.logTimeOfDay(delayAdjustedCtm));
                        if (this.mTimeServiceHelper.isTimeDetectionEnabled()) {
                            TimeZone timeZone = zone;
                            zoneId2 = zoneId3;
                            long timeZoneAdjustedCtm = delayAdjustedCtm - tzOffset;
                            try {
                                setAndBroadcastNetworkSetTime("handleNetworkCountryCodeSet: setting time timeZoneAdjustedCtm=" + TimeUtils.logTimeOfDay(timeZoneAdjustedCtm), timeZoneAdjustedCtm);
                            } catch (Throwable th) {
                                th = th;
                                this.mWakeLock.release();
                                throw th;
                            }
                        } else {
                            zoneId2 = zoneId3;
                            this.mSavedNitzTime = new TimeStampedValue<>(Long.valueOf(((Long) this.mSavedNitzTime.mValue).longValue() - tzOffset), this.mSavedNitzTime.mElapsedRealtime);
                            Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet:adjusting time mSavedNitzTime=" + this.mSavedNitzTime);
                        }
                        this.mWakeLock.release();
                    } catch (Throwable th2) {
                        th = th2;
                        TimeZone timeZone2 = zone;
                        String str2 = zoneId3;
                        this.mWakeLock.release();
                        throw th;
                    }
                } else {
                    zoneId2 = zoneId3;
                }
                zoneId = zoneId2;
            }
            String zoneId4 = zoneId;
            this.mTimeZoneLog.log("handleNetworkCountryCodeSet: isTimeZoneSettingInitialized=" + isTimeZoneSettingInitialized + " mLatestNitzSignal=" + this.mLatestNitzSignal + " isoCountryCode=" + isoCountryCode + " mNeedCountryCodeForNitz=" + this.mNeedCountryCodeForNitz + " zoneId=" + zoneId4);
            if (zoneId4 != null) {
                Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet: zoneId != null, zoneId=" + zoneId4);
                if (this.mTimeServiceHelper.isTimeZoneDetectionEnabled()) {
                    setAndBroadcastNetworkSetTimeZone(zoneId4);
                    if (!mTimeZoneUpdateBlocked) {
                        if (fixZoneByNitz && this.mPhone.getServiceStateTracker() != null) {
                            HwTelephonyFactory.getHwNetworkManager().sendNitzTimeZoneUpdateMessage(this.mPhone.getServiceStateTracker().getCellLocationInfo());
                        }
                        HwTelephonyFactory.getHwReportManager().reportSetTimeZoneByNitz(this.mPhone, zoneId4, -1, "NitzFix");
                    }
                } else {
                    Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet: skip changing zone as isTimeZoneDetectionEnabled() is false");
                }
                if (this.mNeedCountryCodeForNitz) {
                    this.mSavedTimeZoneId = zoneId4;
                    this.mNitzTimeZoneDetectionSuccessful = true;
                }
            } else {
                Rlog.d("NitzStateMachine", "handleNetworkCountryCodeSet: lookupResult == null, do nothing");
            }
            this.mNeedCountryCodeForNitz = false;
        }
    }

    private boolean countryUsesUtc(String isoCountryCode, TimeStampedValue<NitzData> nitzSignal) {
        return this.mTimeZoneLookupHelper.countryUsesUtc(isoCountryCode, ((NitzData) nitzSignal.mValue).getCurrentTimeInMillis());
    }

    public void handleNetworkAvailable() {
        Rlog.d("NitzStateMachine", "handleNetworkAvailable: mNitzTimeZoneDetectionSuccessful=" + this.mNitzTimeZoneDetectionSuccessful + ", Setting mNitzTimeZoneDetectionSuccessful=false");
        this.mNitzTimeZoneDetectionSuccessful = false;
    }

    public void handleNetworkUnavailable() {
        Rlog.d("NitzStateMachine", "handleNetworkUnavailable");
        this.mGotCountryCode = false;
        this.mNitzTimeZoneDetectionSuccessful = false;
    }

    private static boolean nitzOffsetMightBeBogus(NitzData nitzData) {
        return nitzData.getLocalOffsetMillis() == 0 && !nitzData.isDst();
    }

    public void handleNitzReceived(TimeStampedValue<NitzData> nitzSignal) {
        handleTimeZoneFromNitz(nitzSignal);
        handleTimeFromNitz(nitzSignal);
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00bb A[Catch:{ RuntimeException -> 0x00f7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:40:? A[RETURN, SYNTHETIC] */
    private void handleTimeZoneFromNitz(TimeStampedValue<NitzData> nitzSignal) {
        String zoneId;
        try {
            NitzData newNitzData = (NitzData) nitzSignal.mValue;
            String iso = this.mDeviceState.getNetworkCountryIsoForPhone();
            if (newNitzData.getEmulatorHostTimeZone() != null) {
                zoneId = newNitzData.getEmulatorHostTimeZone().getID();
            } else if (!this.mGotCountryCode) {
                zoneId = null;
            } else {
                String str = null;
                if (!TextUtils.isEmpty(iso)) {
                    TimeZoneLookupHelper.OffsetResult lookupResult = this.mTimeZoneLookupHelper.lookupByNitzCountry(newNitzData, iso);
                    if (lookupResult != null) {
                        str = lookupResult.zoneId;
                    }
                    zoneId = str;
                } else {
                    TimeZoneLookupHelper.OffsetResult lookupResult2 = this.mTimeZoneLookupHelper.lookupByNitz(newNitzData);
                    Rlog.d("NitzStateMachine", "handleTimeZoneFromNitz: guessZoneIdByNitz returned lookupResult=" + lookupResult2);
                    if (lookupResult2 != null) {
                        str = lookupResult2.zoneId;
                    }
                    zoneId = str;
                }
                if (zoneId == null || this.mLatestNitzSignal == null || offsetInfoDiffers(newNitzData, (NitzData) this.mLatestNitzSignal.mValue)) {
                    this.mNeedCountryCodeForNitz = true;
                    this.mLatestNitzSignal = nitzSignal;
                }
                String tmpLog = "handleTimeZoneFromNitz: nitzSignal=" + nitzSignal + " zoneId=" + zoneId + " iso=" + iso + " mGotCountryCode=" + this.mGotCountryCode + " mNeedCountryCodeForNitz=" + this.mNeedCountryCodeForNitz + " isTimeZoneDetectionEnabled()=" + this.mTimeServiceHelper.isTimeZoneDetectionEnabled();
                Rlog.d("NitzStateMachine", tmpLog);
                this.mTimeZoneLog.log(tmpLog);
                if (zoneId == null) {
                    if (this.mTimeServiceHelper.isTimeZoneDetectionEnabled()) {
                        setAndBroadcastNetworkSetTimeZone(zoneId);
                        if (!mTimeZoneUpdateBlocked) {
                            if (this.mPhone.getServiceStateTracker() != null) {
                                HwTelephonyFactory.getHwNetworkManager().sendNitzTimeZoneUpdateMessage(this.mPhone.getServiceStateTracker().getCellLocationInfo());
                            }
                            HwTelephonyFactory.getHwReportManager().reportSetTimeZoneByNitz(this.mPhone, zoneId, newNitzData.getLocalOffsetMillis(), "Nitz");
                        }
                    }
                    this.mNitzTimeZoneDetectionSuccessful = true;
                    this.mSavedTimeZoneId = zoneId;
                    return;
                }
                return;
            }
            this.mNeedCountryCodeForNitz = true;
            this.mLatestNitzSignal = nitzSignal;
            String tmpLog2 = "handleTimeZoneFromNitz: nitzSignal=" + nitzSignal + " zoneId=" + zoneId + " iso=" + iso + " mGotCountryCode=" + this.mGotCountryCode + " mNeedCountryCodeForNitz=" + this.mNeedCountryCodeForNitz + " isTimeZoneDetectionEnabled()=" + this.mTimeServiceHelper.isTimeZoneDetectionEnabled();
            Rlog.d("NitzStateMachine", tmpLog2);
            this.mTimeZoneLog.log(tmpLog2);
            if (zoneId == null) {
            }
        } catch (RuntimeException ex) {
            Rlog.e("NitzStateMachine", "handleTimeZoneFromNitz: Processing NITZ data nitzSignal=" + nitzSignal + " ex=" + ex);
        }
    }

    private static boolean offsetInfoDiffers(NitzData one, NitzData two) {
        return (one.getLocalOffsetMillis() == two.getLocalOffsetMillis() && one.isDst() == two.isDst()) ? false : true;
    }

    private void handleTimeFromNitz(TimeStampedValue<NitzData> nitzSignal) {
        TimeStampedValue<NitzData> timeStampedValue = nitzSignal;
        try {
            if (this.mDeviceState.getIgnoreNitz()) {
                Rlog.d("NitzStateMachine", "handleTimeFromNitz: Not setting clock because gsm.ignore-nitz is set");
                return;
            }
            this.mWakeLock.acquire();
            long millisSinceNitzReceived = this.mTimeServiceHelper.elapsedRealtime() - timeStampedValue.mElapsedRealtime;
            if (millisSinceNitzReceived < 0) {
                long j = millisSinceNitzReceived;
            } else if (millisSinceNitzReceived > 2147483647L) {
                long j2 = millisSinceNitzReceived;
            } else {
                long adjustedCurrentTimeMillis = ((NitzData) timeStampedValue.mValue).getCurrentTimeInMillis() + millisSinceNitzReceived;
                long gained = adjustedCurrentTimeMillis - this.mTimeServiceHelper.currentTimeMillis();
                if (this.mTimeServiceHelper.isTimeDetectionEnabled()) {
                    String logMsg = "handleTimeFromNitz: nitzSignal=" + timeStampedValue + " adjustedCurrentTimeMillis=" + adjustedCurrentTimeMillis + " millisSinceNitzReceived= " + millisSinceNitzReceived + " gained=" + gained;
                    if (this.mSavedNitzTime == null) {
                        setAndBroadcastNetworkSetTime(logMsg + ": First update received.", adjustedCurrentTimeMillis);
                        long j3 = millisSinceNitzReceived;
                        long j4 = gained;
                    } else {
                        long elapsedRealtimeSinceLastSaved = this.mTimeServiceHelper.elapsedRealtime() - this.mSavedNitzTime.mElapsedRealtime;
                        int nitzUpdateSpacing = this.mDeviceState.getNitzUpdateSpacingMillis();
                        int nitzUpdateDiff = this.mDeviceState.getNitzUpdateDiffMillis();
                        long j5 = millisSinceNitzReceived;
                        if (elapsedRealtimeSinceLastSaved <= ((long) nitzUpdateSpacing)) {
                            long j6 = gained;
                            if (Math.abs(gained) <= ((long) nitzUpdateDiff)) {
                                Rlog.d("NitzStateMachine", logMsg + ": Update throttled.");
                                this.mWakeLock.release();
                                return;
                            }
                        }
                        setAndBroadcastNetworkSetTime(logMsg + ": New update received.", adjustedCurrentTimeMillis);
                    }
                } else {
                    long j7 = gained;
                }
                this.mSavedNitzTime = new TimeStampedValue<>(Long.valueOf(adjustedCurrentTimeMillis), timeStampedValue.mElapsedRealtime);
                SystemProperties.set("gsm.nitz.time", String.valueOf(adjustedCurrentTimeMillis));
                SystemProperties.set("gsm.nitz.timereference", String.valueOf(SystemClock.elapsedRealtime()));
                this.mWakeLock.release();
                return;
            }
            Rlog.d("NitzStateMachine", "handleTimeFromNitz: not setting time, unexpected elapsedRealtime=" + elapsedRealtime + " nitzSignal=" + timeStampedValue);
            this.mWakeLock.release();
        } catch (RuntimeException ex) {
            Rlog.e("NitzStateMachine", "handleTimeFromNitz: Processing NITZ data nitzSignal=" + timeStampedValue + " ex=" + ex);
        } catch (Throwable th) {
            this.mWakeLock.release();
            throw th;
        }
    }

    private void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        Rlog.d("NitzStateMachine", "setAndBroadcastNetworkSetTimeZone: zoneId=" + zoneId);
        if (HwTelephonyFactory.getHwNetworkManager().isNeedLocationTimeZoneUpdate(this.mPhone, zoneId)) {
            Rlog.d("NitzStateMachine", "there is no need update time zone.");
            mTimeZoneUpdateBlocked = true;
            return;
        }
        mTimeZoneUpdateBlocked = false;
        this.mTimeServiceHelper.setDeviceTimeZone(zoneId);
        Rlog.d("NitzStateMachine", "setAndBroadcastNetworkSetTimeZone: called setDeviceTimeZone() zoneId=" + zoneId);
    }

    private void setAndBroadcastNetworkSetTime(String msg, long time) {
        if (!this.mWakeLock.isHeld()) {
            Rlog.w("NitzStateMachine", "setAndBroadcastNetworkSetTime: Wake lock not held while setting device time (msg=" + msg + ")");
        }
        String msg2 = "setAndBroadcastNetworkSetTime: [Setting time to time=" + time + "]:" + msg;
        Rlog.d("NitzStateMachine", msg2);
        this.mTimeLog.log(msg2);
        this.mTimeServiceHelper.setDeviceTime(time);
        TelephonyMetrics.getInstance().writeNITZEvent(this.mPhone.getPhoneId(), time);
    }

    /* access modifiers changed from: private */
    public void handleAutoTimeEnabled() {
        Rlog.d("NitzStateMachine", "handleAutoTimeEnabled: Reverting to NITZ Time: mSavedNitzTime=" + this.mSavedNitzTime);
        if (this.mSavedNitzTime != null) {
            try {
                this.mWakeLock.acquire();
                long elapsedRealtime = this.mTimeServiceHelper.elapsedRealtime();
                setAndBroadcastNetworkSetTime("mSavedNitzTime: Reverting to NITZ time elapsedRealtime=" + elapsedRealtime + " mSavedNitzTime=" + this.mSavedNitzTime, ((Long) this.mSavedNitzTime.mValue).longValue() + (elapsedRealtime - this.mSavedNitzTime.mElapsedRealtime));
            } finally {
                this.mWakeLock.release();
            }
        }
    }

    public void handleAutoTimeZoneEnabled() {
        int airplaneMode;
        String tmpLog = "handleAutoTimeZoneEnabled: Reverting to NITZ TimeZone: mSavedTimeZoneId=" + this.mSavedTimeZoneId + " airplaneMode=" + airplaneMode;
        Rlog.d("NitzStateMachine", tmpLog);
        this.mTimeZoneLog.log(tmpLog);
        if (this.mSavedTimeZoneId == null || (!this.mNitzTimeZoneDetectionSuccessful && airplaneMode <= 0)) {
            String iso = this.mDeviceState.getNetworkCountryIsoForPhone();
            if (!TextUtils.isEmpty(iso)) {
                updateTimeZoneByNetworkCountryCode(iso);
                return;
            }
            return;
        }
        setAndBroadcastNetworkSetTimeZone(this.mSavedTimeZoneId);
        HwTelephonyFactory.getHwReportManager().reportSetTimeZoneByNitz(this.mPhone, this.mSavedTimeZoneId, -1, "AutoTZ");
    }

    public void dumpState(PrintWriter pw) {
        pw.println(" mSavedTime=" + this.mSavedNitzTime);
        pw.println(" mNeedCountryCodeForNitz=" + this.mNeedCountryCodeForNitz);
        pw.println(" mLatestNitzSignal=" + this.mLatestNitzSignal);
        pw.println(" mGotCountryCode=" + this.mGotCountryCode);
        pw.println(" mSavedTimeZoneId=" + this.mSavedTimeZoneId);
        pw.println(" mNitzTimeZoneDetectionSuccessful=" + this.mNitzTimeZoneDetectionSuccessful);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.flush();
    }

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

    private void updateTimeZoneByNetworkCountryCode(String iso) {
        String numeric;
        TimeZoneLookupHelper.CountryResult lookupResult = this.mTimeZoneLookupHelper.lookupByCountry(iso, this.mTimeServiceHelper.currentTimeMillis());
        if (this.mPhone.getPhoneId() == 2) {
            numeric = SystemProperties.get("gsm.operator.numeric.vsim", "");
        } else {
            numeric = this.mDeviceState.mTelephonyManager.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
        }
        String mcc = null;
        if (!isInvalidOperatorNumeric(numeric)) {
            mcc = numeric.substring(0, 3);
        }
        boolean isNeedDefaultZone = isNeedDefaultZone(mcc);
        Rlog.d("NitzStateMachine", "updateTimeZoneByNetworkCountryCode: mcc: " + mcc + "  isNeedDefaultZone: " + isNeedDefaultZone + " lookupResult=" + lookupResult);
        if (lookupResult == null || (!lookupResult.allZonesHaveSameOffset && !isNeedDefaultZone)) {
            Rlog.d("NitzStateMachine", "updateTimeZoneByNetworkCountryCode: no good zone for iso=" + iso + " lookupResult=" + lookupResult);
            return;
        }
        String logMsg = "updateTimeZoneByNetworkCountryCode: set time lookupResult=" + lookupResult + " iso=" + iso;
        Rlog.d("NitzStateMachine", logMsg);
        this.mTimeZoneLog.log(logMsg);
        String zoneId = !isNeedDefaultZone ? lookupResult.zoneId : getTimeZoneFromMcc(mcc);
        setAndBroadcastNetworkSetTimeZone(zoneId);
        HwTelephonyFactory.getHwReportManager().reportSetTimeZoneByIso(this.mPhone, zoneId, this.mNitzTimeZoneDetectionSuccessful, this.mPhone.getPhoneId() + " Iso");
    }

    /* access modifiers changed from: protected */
    public boolean isNeedDefaultZone(String mcc) {
        if ("460".equals(mcc) || "255".equals(mcc)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String getTimeZoneFromMcc(String mcc) {
        if ("460".equals(mcc)) {
            return "Asia/Shanghai";
        }
        if ("255".equals(mcc)) {
            return "Europe/Kiev";
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isInvalidOperatorNumeric(String operatorNumeric) {
        return operatorNumeric == null || operatorNumeric.length() < 5;
    }

    public boolean getNitzTimeZoneDetectionSuccessful() {
        return this.mNitzTimeZoneDetectionSuccessful;
    }

    public NitzData getCachedNitzData() {
        if (this.mLatestNitzSignal != null) {
            return (NitzData) this.mLatestNitzSignal.mValue;
        }
        return null;
    }

    public String getSavedTimeZoneId() {
        return this.mSavedTimeZoneId;
    }

    public long getNitzSpaceTime() {
        if (this.mSavedNitzTime != null) {
            return this.mTimeServiceHelper.elapsedRealtime() - this.mSavedNitzTime.mElapsedRealtime;
        }
        return this.mTimeServiceHelper.elapsedRealtime();
    }
}
