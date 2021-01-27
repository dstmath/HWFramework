package com.android.internal.telephony;

import android.os.PowerManager;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.TimestampedValue;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.NitzStateMachine;
import com.android.internal.telephony.OldTimeServiceHelper;
import com.android.internal.telephony.TimeZoneLookupHelper;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class OldNitzStateMachine implements NitzStateMachine {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "OldNitzStateMachine";
    private static final String WAKELOCK_TAG = "NitzStateMachine";
    private final NitzStateMachine.DeviceState mDeviceState;
    private boolean mGotCountryCode;
    private TimestampedValue<NitzData> mLatestNitzSignal;
    private boolean mNitzTimeZoneDetectionSuccessful;
    private final GsmCdmaPhone mPhone;
    private TimestampedValue<Long> mSavedNitzTime;
    private String mSavedTimeZoneId;
    private final LocalLog mTimeLog;
    private final OldTimeServiceHelper mTimeServiceHelper;
    private final LocalLog mTimeZoneLog;
    private final TimeZoneLookupHelper mTimeZoneLookupHelper;
    private final PowerManager.WakeLock mWakeLock;

    public OldNitzStateMachine(GsmCdmaPhone phone) {
        this(phone, new OldTimeServiceHelper(phone.getContext()), new NitzStateMachine.DeviceState(phone), new TimeZoneLookupHelper());
    }

    @VisibleForTesting
    public OldNitzStateMachine(GsmCdmaPhone phone, OldTimeServiceHelper timeServiceHelper, NitzStateMachine.DeviceState deviceState, TimeZoneLookupHelper timeZoneLookupHelper) {
        this.mGotCountryCode = false;
        this.mNitzTimeZoneDetectionSuccessful = false;
        this.mTimeLog = new LocalLog(15);
        this.mTimeZoneLog = new LocalLog(15);
        this.mPhone = phone;
        this.mWakeLock = ((PowerManager) phone.getContext().getSystemService("power")).newWakeLock(1, WAKELOCK_TAG);
        this.mDeviceState = deviceState;
        this.mTimeZoneLookupHelper = timeZoneLookupHelper;
        this.mTimeServiceHelper = timeServiceHelper;
        this.mTimeServiceHelper.setListener(new OldTimeServiceHelper.Listener() {
            /* class com.android.internal.telephony.OldNitzStateMachine.AnonymousClass1 */

            @Override // com.android.internal.telephony.OldTimeServiceHelper.Listener
            public void onTimeDetectionChange(boolean enabled) {
                if (enabled) {
                    OldNitzStateMachine.this.handleAutoTimeEnabled();
                }
            }

            @Override // com.android.internal.telephony.OldTimeServiceHelper.Listener
            public void onTimeZoneDetectionChange(boolean enabled) {
                if (enabled) {
                    OldNitzStateMachine.this.handleAutoTimeZoneEnabled();
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
        Rlog.d(LOG_TAG, "updateTimeZoneFromCountryAndNitz: isTimeZoneSettingInitialized=" + isTimeZoneSettingInitialized + " nitzSignal=" + nitzSignal + " isoCountryCode=" + isoCountryCode);
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
                    Rlog.d(LOG_TAG, logMsg2);
                    this.mTimeZoneLog.log(logMsg2);
                    if (lookupResult != null) {
                        str = lookupResult.zoneId;
                    }
                    logMsg = str;
                } else if (this.mLatestNitzSignal == null) {
                    Rlog.d(LOG_TAG, "updateTimeZoneFromCountryAndNitz: No cached NITZ data available, not setting zone");
                    logMsg = null;
                } else if (isNitzSignalOffsetInfoBogus(nitzSignal, isoCountryCode)) {
                    String logMsg3 = "updateTimeZoneFromCountryAndNitz: Received NITZ looks bogus,  isoCountryCode=" + isoCountryCode + " nitzSignal=" + nitzSignal;
                    Rlog.d(LOG_TAG, logMsg3);
                    this.mTimeZoneLog.log(logMsg3);
                    logMsg = null;
                } else {
                    TimeZoneLookupHelper.OffsetResult lookupResult2 = this.mTimeZoneLookupHelper.lookupByNitzCountry(nitzData, isoCountryCode);
                    Rlog.d(LOG_TAG, "updateTimeZoneFromCountryAndNitz: using lookupByNitzCountry(nitzData, isoCountryCode), nitzData=" + nitzData + " isoCountryCode=" + isoCountryCode + " lookupResult=" + lookupResult2);
                    if (lookupResult2 != null) {
                        str = lookupResult2.zoneId;
                    }
                    logMsg = str;
                }
            }
            this.mTimeZoneLog.log("updateTimeZoneFromCountryAndNitz: isTimeZoneSettingInitialized=" + isTimeZoneSettingInitialized + " isoCountryCode=" + isoCountryCode + " nitzSignal=" + nitzSignal + " zoneId=" + logMsg + " isTimeZoneDetectionEnabled()=" + this.mTimeServiceHelper.isTimeZoneDetectionEnabled());
            if (logMsg != null) {
                Rlog.d(LOG_TAG, "updateTimeZoneFromCountryAndNitz: zoneId=" + logMsg);
                if (this.mTimeServiceHelper.isTimeZoneDetectionEnabled()) {
                    setAndBroadcastNetworkSetTimeZone(logMsg);
                } else {
                    Rlog.d(LOG_TAG, "updateTimeZoneFromCountryAndNitz: skip changing zone as isTimeZoneDetectionEnabled() is false");
                }
                this.mSavedTimeZoneId = logMsg;
                this.mNitzTimeZoneDetectionSuccessful = true;
                return;
            }
            Rlog.d(LOG_TAG, "updateTimeZoneFromCountryAndNitz: zoneId == null, do nothing");
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "updateTimeZoneFromCountryAndNitz: Processing NITZ data nitzSignal=" + nitzSignal + " isoCountryCode=" + isoCountryCode + " isTimeZoneSettingInitialized=" + isTimeZoneSettingInitialized + " ex=" + ex);
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
        Rlog.d(LOG_TAG, "handleNetworkAvailable: mNitzTimeZoneDetectionSuccessful=" + this.mNitzTimeZoneDetectionSuccessful + ", Setting mNitzTimeZoneDetectionSuccessful=false");
        this.mNitzTimeZoneDetectionSuccessful = false;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public void handleNetworkCountryCodeUnavailable() {
        Rlog.d(LOG_TAG, "handleNetworkCountryCodeUnavailable");
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
        Throwable th;
        TimestampedValue<NitzData> nitzSignal = this.mLatestNitzSignal;
        try {
            if (this.mDeviceState.getIgnoreNitz()) {
                Rlog.d(LOG_TAG, "updateTimeFromNitz: Not setting clock because gsm.ignore-nitz is set");
                return;
            }
            try {
                this.mWakeLock.acquire();
                long elapsedRealtime = this.mTimeServiceHelper.elapsedRealtime();
                long millisSinceNitzReceived = elapsedRealtime - nitzSignal.getReferenceTimeMillis();
                if (millisSinceNitzReceived >= 0) {
                    if (millisSinceNitzReceived <= 2147483647L) {
                        long adjustedCurrentTimeMillis = ((NitzData) nitzSignal.getValue()).getCurrentTimeInMillis() + millisSinceNitzReceived;
                        long gained = adjustedCurrentTimeMillis - this.mTimeServiceHelper.currentTimeMillis();
                        if (this.mTimeServiceHelper.isTimeDetectionEnabled()) {
                            String logMsg = "updateTimeFromNitz: nitzSignal=" + nitzSignal + " adjustedCurrentTimeMillis=" + adjustedCurrentTimeMillis + " millisSinceNitzReceived= " + millisSinceNitzReceived + " gained=" + gained;
                            if (this.mSavedNitzTime == null) {
                                try {
                                    setAndBroadcastNetworkSetTime(logMsg + ": First update received.", adjustedCurrentTimeMillis);
                                } catch (Throwable th2) {
                                    th = th2;
                                    this.mWakeLock.release();
                                    throw th;
                                }
                            } else {
                                long elapsedRealtimeSinceLastSaved = this.mTimeServiceHelper.elapsedRealtime() - this.mSavedNitzTime.getReferenceTimeMillis();
                                int nitzUpdateSpacing = this.mDeviceState.getNitzUpdateSpacingMillis();
                                try {
                                    int nitzUpdateDiff = this.mDeviceState.getNitzUpdateDiffMillis();
                                    if (elapsedRealtimeSinceLastSaved <= ((long) nitzUpdateSpacing)) {
                                        if (Math.abs(gained) <= ((long) nitzUpdateDiff)) {
                                            Rlog.d(LOG_TAG, logMsg + ": Update throttled.");
                                            this.mWakeLock.release();
                                            return;
                                        }
                                    }
                                    setAndBroadcastNetworkSetTime(logMsg + ": New update received.", adjustedCurrentTimeMillis);
                                } catch (Throwable th3) {
                                    th = th3;
                                    this.mWakeLock.release();
                                    throw th;
                                }
                            }
                        }
                        this.mSavedNitzTime = new TimestampedValue<>(nitzSignal.getReferenceTimeMillis(), Long.valueOf(((NitzData) nitzSignal.getValue()).getCurrentTimeInMillis()));
                        this.mWakeLock.release();
                        return;
                    }
                }
                Rlog.d(LOG_TAG, "updateTimeFromNitz: not setting time, unexpected elapsedRealtime=" + elapsedRealtime + " nitzSignal=" + nitzSignal);
                this.mWakeLock.release();
            } catch (Throwable th4) {
                th = th4;
                this.mWakeLock.release();
                throw th;
            }
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "updateTimeFromNitz: Processing NITZ data nitzSignal=" + nitzSignal + " ex=" + ex);
        }
    }

    private void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        Rlog.d(LOG_TAG, "setAndBroadcastNetworkSetTimeZone: zoneId=" + zoneId);
        this.mTimeServiceHelper.setDeviceTimeZone(zoneId);
        Rlog.d(LOG_TAG, "setAndBroadcastNetworkSetTimeZone: called setDeviceTimeZone() zoneId=" + zoneId);
    }

    private void setAndBroadcastNetworkSetTime(String msg, long time) {
        if (!this.mWakeLock.isHeld()) {
            Rlog.w(LOG_TAG, "setAndBroadcastNetworkSetTime: Wake lock not held while setting device time (msg=" + msg + ")");
        }
        String msg2 = "setAndBroadcastNetworkSetTime: [Setting time to time=" + time + "]:" + msg;
        Rlog.d(LOG_TAG, msg2);
        this.mTimeLog.log(msg2);
        this.mTimeServiceHelper.setDeviceTime(time);
        TelephonyMetrics.getInstance().writeNITZEvent(this.mPhone.getPhoneId(), time);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAutoTimeEnabled() {
        Rlog.d(LOG_TAG, "handleAutoTimeEnabled: Reverting to NITZ Time: mSavedNitzTime=" + this.mSavedNitzTime);
        if (this.mSavedNitzTime != null) {
            try {
                this.mWakeLock.acquire();
                long elapsedRealtime = this.mTimeServiceHelper.elapsedRealtime();
                setAndBroadcastNetworkSetTime("mSavedNitzTime: Reverting to NITZ time elapsedRealtime=" + elapsedRealtime + " mSavedNitzTime=" + this.mSavedNitzTime, ((Long) this.mSavedNitzTime.getValue()).longValue() + (elapsedRealtime - this.mSavedNitzTime.getReferenceTimeMillis()));
            } finally {
                this.mWakeLock.release();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAutoTimeZoneEnabled() {
        String tmpLog = "handleAutoTimeZoneEnabled: Reverting to NITZ TimeZone: mSavedTimeZoneId=" + this.mSavedTimeZoneId;
        Rlog.d(LOG_TAG, tmpLog);
        this.mTimeZoneLog.log(tmpLog);
        String str = this.mSavedTimeZoneId;
        if (str != null) {
            setAndBroadcastNetworkSetTimeZone(str);
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
        TimeZoneLookupHelper.CountryResult lookupResult = this.mTimeZoneLookupHelper.lookupByCountry(iso, this.mTimeServiceHelper.currentTimeMillis());
        if (lookupResult == null || !lookupResult.allZonesHaveSameOffset) {
            Rlog.d(LOG_TAG, "updateTimeZoneFromNetworkCountryCode: no good zone for iso=" + iso + " lookupResult=" + lookupResult);
            return;
        }
        String logMsg = "updateTimeZoneFromNetworkCountryCode: tz result found iso=" + iso + " lookupResult=" + lookupResult;
        Rlog.d(LOG_TAG, logMsg);
        this.mTimeZoneLog.log(logMsg);
        String zoneId = lookupResult.zoneId;
        if (this.mTimeServiceHelper.isTimeZoneDetectionEnabled()) {
            setAndBroadcastNetworkSetTimeZone(zoneId);
        }
        this.mSavedTimeZoneId = zoneId;
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
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public String getTimeZoneFromMcc(String mcc) {
        return null;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public long getNitzSpaceTime() {
        return 0;
    }

    @Override // com.android.internal.telephony.NitzStateMachine
    public long getSavedNitzTime() {
        return 0;
    }
}
