package com.android.internal.telephony.uicc;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class UiccCarrierPrivilegeRules extends Handler {
    private static final String AID = "A00000015141434C00";
    private static final int CLA = 128;
    private static final int COMMAND = 202;
    private static final String DATA = "";
    private static final boolean DBG = false;
    private static final int EVENT_CLOSE_LOGICAL_CHANNEL_DONE = 3;
    private static final int EVENT_OPEN_LOGICAL_CHANNEL_DONE = 1;
    private static final int EVENT_PKCS15_READ_DONE = 4;
    private static final int EVENT_TRANSMIT_LOGICAL_CHANNEL_DONE = 2;
    private static final String LOG_TAG = "UiccCarrierPrivilegeRules";
    private static final int MAX_RETRY = 1;
    private static final int P1 = 255;
    private static final int P2 = 64;
    private static final int P2_EXTENDED_DATA = 96;
    private static final int P3 = 0;
    private static final int RETRY_INTERVAL_MS = 10000;
    private static final int STATE_ERROR = 2;
    private static final int STATE_LOADED = 1;
    private static final int STATE_LOADING = 0;
    private static final String TAG_ALL_REF_AR_DO = "FF40";
    private static final String TAG_AR_DO = "E3";
    private static final String TAG_DEVICE_APP_ID_REF_DO = "C1";
    private static final String TAG_PERM_AR_DO = "DB";
    private static final String TAG_PKG_REF_DO = "CA";
    private static final String TAG_REF_AR_DO = "E2";
    private static final String TAG_REF_DO = "E1";
    private List<AccessRule> mAccessRules;
    private int mChannelId;
    private Message mLoadedCallback;
    private int mRetryCount;
    private final Runnable mRetryRunnable = new Runnable() {
        public void run() {
            UiccCarrierPrivilegeRules.this.openChannel();
        }
    };
    private String mRules;
    private AtomicInteger mState;
    private String mStatusMessage;
    private UiccCard mUiccCard;
    private UiccPkcs15 mUiccPkcs15;

    private static class AccessRule {
        public long accessType;
        public byte[] certificateHash;
        public String packageName;

        AccessRule(byte[] certificateHash, String packageName, long accessType) {
            this.certificateHash = certificateHash;
            this.packageName = packageName;
            this.accessType = accessType;
        }

        boolean matches(byte[] certHash, String packageName) {
            if (certHash == null || !Arrays.equals(this.certificateHash, certHash)) {
                return false;
            }
            return !TextUtils.isEmpty(this.packageName) ? this.packageName.equals(packageName) : true;
        }

        public String toString() {
            return "cert: " + IccUtils.bytesToHexString(this.certificateHash) + " pkg: " + this.packageName + " access: " + this.accessType;
        }
    }

    public static class TLV {
        private static final int SINGLE_BYTE_MAX_LENGTH = 128;
        private Integer length;
        private String lengthBytes;
        private String tag;
        private String value;

        public TLV(String tag) {
            this.tag = tag;
        }

        public String getValue() {
            if (this.value == null) {
                return UiccCarrierPrivilegeRules.DATA;
            }
            return this.value;
        }

        public String parseLength(String data) {
            int offset = this.tag.length();
            int firstByte = Integer.parseInt(data.substring(offset, offset + 2), 16);
            if (firstByte < 128) {
                this.length = Integer.valueOf(firstByte * 2);
                this.lengthBytes = data.substring(offset, offset + 2);
            } else {
                int numBytes = firstByte - 128;
                this.length = Integer.valueOf(Integer.parseInt(data.substring(offset + 2, (offset + 2) + (numBytes * 2)), 16) * 2);
                this.lengthBytes = data.substring(offset, (offset + 2) + (numBytes * 2));
            }
            UiccCarrierPrivilegeRules.log("TLV parseLength length=" + this.length + "lenghtBytes: " + this.lengthBytes);
            return this.lengthBytes;
        }

        public String parse(String data, boolean shouldConsumeAll) {
            UiccCarrierPrivilegeRules.log("Parse TLV: " + this.tag);
            if (data.startsWith(this.tag)) {
                int index = this.tag.length();
                if (index + 2 > data.length()) {
                    throw new IllegalArgumentException("No length.");
                }
                parseLength(data);
                index += this.lengthBytes.length();
                UiccCarrierPrivilegeRules.log("index=" + index + " length=" + this.length + "data.length=" + data.length());
                int remainingLength = data.length() - (this.length.intValue() + index);
                if (remainingLength < 0) {
                    throw new IllegalArgumentException("Not enough data.");
                } else if (!shouldConsumeAll || remainingLength == 0) {
                    this.value = data.substring(index, this.length.intValue() + index);
                    UiccCarrierPrivilegeRules.log("Got TLV: " + this.tag + "," + this.length + "," + this.value);
                    return data.substring(this.length.intValue() + index);
                } else {
                    throw new IllegalArgumentException("Did not consume all.");
                }
            }
            throw new IllegalArgumentException("Tags don't match.");
        }
    }

    private void openChannel() {
        this.mUiccCard.iccOpenLogicalChannel(AID, 0, obtainMessage(1, null));
    }

    public UiccCarrierPrivilegeRules(UiccCard uiccCard, Message loadedCallback) {
        log("Creating UiccCarrierPrivilegeRules");
        this.mUiccCard = uiccCard;
        this.mState = new AtomicInteger(0);
        this.mStatusMessage = "Not loaded.";
        this.mLoadedCallback = loadedCallback;
        this.mRules = DATA;
        this.mAccessRules = new ArrayList();
        openChannel();
    }

    public boolean areCarrierPriviligeRulesLoaded() {
        return this.mState.get() != 0;
    }

    public boolean hasCarrierPrivilegeRules() {
        return (this.mState.get() == 0 || this.mAccessRules == null || this.mAccessRules.size() <= 0) ? false : true;
    }

    public List<String> getPackageNames() {
        List<String> pkgNames = new ArrayList();
        if (this.mAccessRules != null) {
            for (AccessRule ar : this.mAccessRules) {
                if (!TextUtils.isEmpty(ar.packageName)) {
                    pkgNames.add(ar.packageName);
                }
            }
        }
        return pkgNames;
    }

    public int getCarrierPrivilegeStatus(Signature signature, String packageName) {
        int state = this.mState.get();
        if (state == 0) {
            return -1;
        }
        if (state == 2) {
            return -2;
        }
        byte[] certHash = getCertHash(signature, "SHA-1");
        byte[] certHash256 = getCertHash(signature, "SHA-256");
        for (AccessRule ar : this.mAccessRules) {
            if (!ar.matches(certHash, packageName)) {
                if (ar.matches(certHash256, packageName)) {
                }
            }
            return 1;
        }
        return 0;
    }

    public int getCarrierPrivilegeStatus(PackageManager packageManager, String packageName) {
        try {
            if (hasCarrierPrivilegeRules()) {
                return getCarrierPrivilegeStatus(packageManager.getPackageInfo(packageName, 32832));
            }
            int state = this.mState.get();
            if (state == 0) {
                return -1;
            }
            if (state == 2) {
                return -2;
            }
            return 0;
        } catch (NameNotFoundException ex) {
            Rlog.e(LOG_TAG, "NameNotFoundException", ex);
            return 0;
        }
    }

    public int getCarrierPrivilegeStatus(PackageInfo packageInfo) {
        for (Signature sig : packageInfo.signatures) {
            int accessStatus = getCarrierPrivilegeStatus(sig, packageInfo.packageName);
            if (accessStatus != 0) {
                return accessStatus;
            }
        }
        return 0;
    }

    public int getCarrierPrivilegeStatusForCurrentTransaction(PackageManager packageManager) {
        for (String pkg : packageManager.getPackagesForUid(Binder.getCallingUid())) {
            int accessStatus = getCarrierPrivilegeStatus(packageManager, pkg);
            if (accessStatus != 0) {
                return accessStatus;
            }
        }
        return 0;
    }

    public List<String> getCarrierPackageNamesForIntent(PackageManager packageManager, Intent intent) {
        List<String> packages = new ArrayList();
        List<ResolveInfo> receivers = new ArrayList();
        receivers.addAll(packageManager.queryBroadcastReceivers(intent, 0));
        receivers.addAll(packageManager.queryIntentContentProviders(intent, 0));
        receivers.addAll(packageManager.queryIntentActivities(intent, 0));
        receivers.addAll(packageManager.queryIntentServices(intent, 0));
        for (ResolveInfo resolveInfo : receivers) {
            String packageName = getPackageName(resolveInfo);
            if (packageName != null) {
                int status = getCarrierPrivilegeStatus(packageManager, packageName);
                if (status == 1) {
                    packages.add(packageName);
                } else if (status != 0) {
                    return null;
                }
            }
        }
        return packages;
    }

    private String getPackageName(ResolveInfo resolveInfo) {
        if (resolveInfo.activityInfo != null) {
            return resolveInfo.activityInfo.packageName;
        }
        if (resolveInfo.serviceInfo != null) {
            return resolveInfo.serviceInfo.packageName;
        }
        if (resolveInfo.providerInfo != null) {
            return resolveInfo.providerInfo.packageName;
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0192 A:{ExcHandler: java.lang.IllegalArgumentException (r16_0 'ex' java.lang.RuntimeException), Splitter: B:31:0x010f} */
    /* JADX WARNING: Missing block: B:38:0x0192, code:
            r16 = move-exception;
     */
    /* JADX WARNING: Missing block: B:39:0x0193, code:
            updateState(2, "Error parsing rules: " + r16);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
                log("EVENT_OPEN_LOGICAL_CHANNEL_DONE");
                ar = msg.obj;
                if (ar.exception == null && ar.result != null) {
                    this.mChannelId = ((int[]) ar.result)[0];
                    if (this.mChannelId <= 0) {
                        Rlog.e(LOG_TAG, "Error channelId!");
                        updateState(2, "Error channelId !");
                        return;
                    }
                    this.mUiccCard.iccTransmitApduLogicalChannel(this.mChannelId, 128, 202, 255, 64, 0, DATA, obtainMessage(2, Integer.valueOf(this.mChannelId)));
                    return;
                } else if ((ar.exception instanceof CommandException) && this.mRetryCount < 1 && ((CommandException) ar.exception).getCommandError() == Error.MISSING_RESOURCE) {
                    this.mRetryCount++;
                    removeCallbacks(this.mRetryRunnable);
                    postDelayed(this.mRetryRunnable, 10000);
                    return;
                } else {
                    log("No ARA, try ARF next.");
                    this.mUiccPkcs15 = new UiccPkcs15(this.mUiccCard, obtainMessage(4));
                    return;
                }
            case 2:
                log("EVENT_TRANSMIT_LOGICAL_CHANNEL_DONE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null || ar.result == null) {
                    updateState(2, "Error reading value from SIM.");
                } else {
                    IccIoResult response = ar.result;
                    if (response.sw1 != 144 || response.sw2 != 0 || response.payload == null || response.payload.length <= 0) {
                        updateState(2, "Invalid response: payload=" + response.payload + " sw1=" + response.sw1 + " sw2=" + response.sw2);
                    } else {
                        try {
                            this.mRules += IccUtils.bytesToHexString(response.payload).toUpperCase(Locale.US);
                            if (isDataComplete()) {
                                this.mAccessRules = parseRules(this.mRules);
                                updateState(1, "Success!");
                            } else {
                                this.mUiccCard.iccTransmitApduLogicalChannel(this.mChannelId, 128, 202, 255, 96, 0, DATA, obtainMessage(2, Integer.valueOf(this.mChannelId)));
                                return;
                            }
                        } catch (RuntimeException ex) {
                        }
                    }
                }
                this.mUiccCard.iccCloseLogicalChannel(this.mChannelId, obtainMessage(3));
                this.mChannelId = -1;
                return;
            case 3:
                log("EVENT_CLOSE_LOGICAL_CHANNEL_DONE");
                return;
            case 4:
                log("EVENT_PKCS15_READ_DONE");
                if (this.mUiccPkcs15 == null || this.mUiccPkcs15.getRules() == null) {
                    updateState(2, "No ARA or ARF.");
                    return;
                }
                for (String cert : this.mUiccPkcs15.getRules()) {
                    this.mAccessRules.add(new AccessRule(IccUtils.hexStringToBytes(cert), DATA, 0));
                }
                updateState(1, "Success!");
                return;
            default:
                Rlog.e(LOG_TAG, "Unknown event " + msg.what);
                return;
        }
    }

    private boolean isDataComplete() {
        log("isDataComplete mRules:" + this.mRules);
        if (this.mRules.startsWith(TAG_ALL_REF_AR_DO)) {
            TLV allRules = new TLV(TAG_ALL_REF_AR_DO);
            String lengthBytes = allRules.parseLength(this.mRules);
            log("isDataComplete lengthBytes: " + lengthBytes);
            if (this.mRules.length() == (TAG_ALL_REF_AR_DO.length() + lengthBytes.length()) + allRules.length.intValue()) {
                log("isDataComplete yes");
                return true;
            }
            log("isDataComplete no");
            return false;
        }
        throw new IllegalArgumentException("Tags don't match.");
    }

    private static List<AccessRule> parseRules(String rules) {
        log("Got rules: " + rules);
        TLV allRefArDo = new TLV(TAG_ALL_REF_AR_DO);
        allRefArDo.parse(rules, true);
        String arDos = allRefArDo.value;
        List<AccessRule> accessRules = new ArrayList();
        while (!arDos.isEmpty()) {
            TLV refArDo = new TLV(TAG_REF_AR_DO);
            arDos = refArDo.parse(arDos, false);
            AccessRule accessRule = parseRefArdo(refArDo.value);
            if (accessRule != null) {
                accessRules.add(accessRule);
            } else {
                Rlog.e(LOG_TAG, "Skip unrecognized rule." + refArDo.value);
            }
        }
        return accessRules;
    }

    private static AccessRule parseRefArdo(String rule) {
        log("Got rule: " + rule);
        String certificateHash = null;
        String packageName = null;
        while (!rule.isEmpty()) {
            if (rule.startsWith(TAG_REF_DO)) {
                TLV refDo = new TLV(TAG_REF_DO);
                rule = refDo.parse(rule, false);
                if (!refDo.value.startsWith(TAG_DEVICE_APP_ID_REF_DO)) {
                    return null;
                }
                TLV deviceDo = new TLV(TAG_DEVICE_APP_ID_REF_DO);
                String tmp = deviceDo.parse(refDo.value, false);
                certificateHash = deviceDo.value;
                if (tmp.isEmpty()) {
                    packageName = null;
                } else if (!tmp.startsWith(TAG_PKG_REF_DO)) {
                    return null;
                } else {
                    TLV pkgDo = new TLV(TAG_PKG_REF_DO);
                    pkgDo.parse(tmp, true);
                    packageName = new String(IccUtils.hexStringToBytes(pkgDo.value));
                }
            } else if (rule.startsWith(TAG_AR_DO)) {
                TLV arDo = new TLV(TAG_AR_DO);
                rule = arDo.parse(rule, false);
                if (!arDo.value.startsWith(TAG_PERM_AR_DO)) {
                    return null;
                }
                new TLV(TAG_PERM_AR_DO).parse(arDo.value, true);
            } else {
                throw new RuntimeException("Invalid Rule type");
            }
        }
        return new AccessRule(IccUtils.hexStringToBytes(certificateHash), packageName, 0);
    }

    private static byte[] getCertHash(Signature signature, String algo) {
        try {
            return MessageDigest.getInstance(algo).digest(signature.toByteArray());
        } catch (NoSuchAlgorithmException ex) {
            Rlog.e(LOG_TAG, "NoSuchAlgorithmException: " + ex);
            return null;
        }
    }

    private void updateState(int newState, String statusMessage) {
        this.mState.set(newState);
        if (this.mLoadedCallback != null) {
            this.mLoadedCallback.sendToTarget();
        }
        this.mStatusMessage = statusMessage;
    }

    private static void log(String msg) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("UiccCarrierPrivilegeRules: " + this);
        pw.println(" mState=" + getStateString(this.mState.get()));
        pw.println(" mStatusMessage='" + this.mStatusMessage + "'");
        if (this.mAccessRules != null) {
            pw.println(" mAccessRules: ");
            for (AccessRule ar : this.mAccessRules) {
                pw.println("  rule='" + ar + "'");
            }
        } else {
            pw.println(" mAccessRules: null");
        }
        if (this.mUiccPkcs15 != null) {
            pw.println(" mUiccPkcs15: " + this.mUiccPkcs15);
            this.mUiccPkcs15.dump(fd, pw, args);
        } else {
            pw.println(" mUiccPkcs15: null");
        }
        pw.flush();
    }

    private String getStateString(int state) {
        switch (state) {
            case 0:
                return "STATE_LOADING";
            case 1:
                return "STATE_LOADED";
            case 2:
                return "STATE_ERROR";
            default:
                return "UNKNOWN";
        }
    }
}
