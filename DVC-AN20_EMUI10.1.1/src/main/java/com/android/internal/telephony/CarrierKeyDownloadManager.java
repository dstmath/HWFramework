package com.android.internal.telephony;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.telephony.CarrierConfigManager;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.org.bouncycastle.util.io.pem.PemReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CarrierKeyDownloadManager extends Handler {
    private static final int[] CARRIER_KEY_TYPES = {1, 2};
    private static final int DAY_IN_MILLIS = 86400000;
    private static final int END_RENEWAL_WINDOW_DAYS = 7;
    private static final int EVENT_ALARM_OR_CONFIG_CHANGE = 0;
    private static final int EVENT_DOWNLOAD_COMPLETE = 1;
    private static final String INTENT_KEY_RENEWAL_ALARM_PREFIX = "com.android.internal.telephony.carrier_key_download_alarm";
    private static final String JSON_CARRIER_KEYS = "carrier-keys";
    private static final String JSON_CERTIFICATE = "certificate";
    private static final String JSON_CERTIFICATE_ALTERNATE = "public-key";
    private static final String JSON_IDENTIFIER = "key-identifier";
    private static final String JSON_TYPE = "key-type";
    private static final String JSON_TYPE_VALUE_EPDG = "EPDG";
    private static final String JSON_TYPE_VALUE_WLAN = "WLAN";
    private static final String LOG_TAG = "CarrierKeyDownloadManager";
    public static final String MCC = "MCC";
    private static final String MCC_MNC_PREF_TAG = "CARRIER_KEY_DM_MCC_MNC";
    public static final String MNC = "MNC";
    private static final String SEPARATOR = ":";
    private static final int START_RENEWAL_WINDOW_DAYS = 21;
    private boolean mAllowedOverMeteredNetwork = false;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.CarrierKeyDownloadManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                int slotId = CarrierKeyDownloadManager.this.mPhone.getPhoneId();
                if (action.equals(CarrierKeyDownloadManager.INTENT_KEY_RENEWAL_ALARM_PREFIX + slotId)) {
                    Log.d(CarrierKeyDownloadManager.LOG_TAG, "Handling key renewal alarm: " + action);
                    CarrierKeyDownloadManager.this.sendEmptyMessage(0);
                } else if ("com.android.internal.telephony.ACTION_CARRIER_CERTIFICATE_DOWNLOAD".equals(action)) {
                    if (slotId == intent.getIntExtra("phone", -1)) {
                        Log.d(CarrierKeyDownloadManager.LOG_TAG, "Handling reset intent: " + action);
                        CarrierKeyDownloadManager.this.sendEmptyMessage(0);
                    }
                } else if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                    if (slotId == intent.getIntExtra("phone", -1)) {
                        Log.d(CarrierKeyDownloadManager.LOG_TAG, "Carrier Config changed: " + action);
                        CarrierKeyDownloadManager.this.sendEmptyMessage(0);
                    }
                } else if ("android.intent.action.DOWNLOAD_COMPLETE".equals(action)) {
                    Log.d(CarrierKeyDownloadManager.LOG_TAG, "Download Complete");
                    CarrierKeyDownloadManager carrierKeyDownloadManager = CarrierKeyDownloadManager.this;
                    carrierKeyDownloadManager.sendMessage(carrierKeyDownloadManager.obtainMessage(1, Long.valueOf(intent.getLongExtra("extra_download_id", 0))));
                }
            }
        }
    };
    private final Context mContext;
    public final DownloadManager mDownloadManager;
    @VisibleForTesting
    public int mKeyAvailability = 0;
    private final Phone mPhone;
    private String mURL;

    public CarrierKeyDownloadManager(Phone phone) {
        this.mPhone = phone;
        this.mContext = phone.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("android.intent.action.DOWNLOAD_COMPLETE");
        filter.addAction(INTENT_KEY_RENEWAL_ALARM_PREFIX + this.mPhone.getPhoneId());
        filter.addAction("com.android.internal.telephony.ACTION_CARRIER_CERTIFICATE_DOWNLOAD");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, phone);
        this.mDownloadManager = (DownloadManager) this.mContext.getSystemService("download");
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 0) {
            handleAlarmOrConfigChange();
        } else if (i == 1) {
            long carrierKeyDownloadIdentifier = ((Long) msg.obj).longValue();
            String mccMnc = getMccMncSetFromPref();
            if (isValidDownload(mccMnc)) {
                onDownloadComplete(carrierKeyDownloadIdentifier, mccMnc);
                onPostDownloadProcessing(carrierKeyDownloadIdentifier);
            }
        }
    }

    private void onPostDownloadProcessing(long carrierKeyDownloadIdentifier) {
        resetRenewalAlarm();
        cleanupDownloadPreferences(carrierKeyDownloadIdentifier);
    }

    private void handleAlarmOrConfigChange() {
        if (!carrierUsesKeys()) {
            cleanupRenewalAlarms();
        } else if (areCarrierKeysAbsentOrExpiring() && !downloadKey()) {
            resetRenewalAlarm();
        }
    }

    private void cleanupDownloadPreferences(long carrierKeyDownloadIdentifier) {
        Log.d(LOG_TAG, "Cleaning up download preferences: " + carrierKeyDownloadIdentifier);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.remove(String.valueOf(carrierKeyDownloadIdentifier));
        editor.commit();
    }

    private void cleanupRenewalAlarms() {
        Log.d(LOG_TAG, "Cleaning up existing renewal alarms");
        int slotId = this.mPhone.getPhoneId();
        ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_KEY_RENEWAL_ALARM_PREFIX + slotId), 134217728));
    }

    @VisibleForTesting
    public long getExpirationDate() {
        ImsiEncryptionInfo imsiEncryptionInfo;
        long minExpirationDate = Long.MAX_VALUE;
        int[] iArr = CARRIER_KEY_TYPES;
        for (int key_type : iArr) {
            if (isKeyEnabled(key_type) && (imsiEncryptionInfo = this.mPhone.getCarrierInfoForImsiEncryption(key_type)) != null && imsiEncryptionInfo.getExpirationTime() != null && minExpirationDate > imsiEncryptionInfo.getExpirationTime().getTime()) {
                minExpirationDate = imsiEncryptionInfo.getExpirationTime().getTime();
            }
        }
        return (minExpirationDate == Long.MAX_VALUE || minExpirationDate < System.currentTimeMillis() + 604800000) ? System.currentTimeMillis() + 86400000 : minExpirationDate - ((long) (new Random().nextInt(1814400000 - 604800000) + 604800000));
    }

    @VisibleForTesting
    public void resetRenewalAlarm() {
        cleanupRenewalAlarms();
        int slotId = this.mPhone.getPhoneId();
        long minExpirationDate = getExpirationDate();
        Log.d(LOG_TAG, "minExpirationDate: " + new Date(minExpirationDate));
        Intent intent = new Intent(INTENT_KEY_RENEWAL_ALARM_PREFIX + slotId);
        ((AlarmManager) this.mContext.getSystemService("alarm")).set(2, minExpirationDate, PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728));
        Log.d(LOG_TAG, "setRenewelAlarm: action=" + intent.getAction() + " time=" + new Date(minExpirationDate));
    }

    private String getMccMncSetFromPref() {
        int slotId = this.mPhone.getPhoneId();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        return preferences.getString(MCC_MNC_PREF_TAG + slotId, null);
    }

    @VisibleForTesting
    public String getSimOperator() {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).getSimOperator(this.mPhone.getSubId());
    }

    @VisibleForTesting
    public boolean isValidDownload(String mccMnc) {
        String simOperator = getSimOperator();
        if (TextUtils.isEmpty(simOperator) || TextUtils.isEmpty(mccMnc)) {
            Log.e(LOG_TAG, "simOperator or mcc/mnc is empty");
            return false;
        }
        String[] splitValue = mccMnc.split(SEPARATOR);
        String mccSource = splitValue[0];
        String mncSource = splitValue[1];
        Log.d(LOG_TAG, "values from sharedPrefs mcc, mnc: " + mccSource + "," + mncSource);
        String mccCurrent = simOperator.substring(0, 3);
        String mncCurrent = simOperator.substring(3);
        Log.d(LOG_TAG, "using values for mcc, mnc: " + mccCurrent + "," + mncCurrent);
        if (!TextUtils.equals(mncSource, mncCurrent) || !TextUtils.equals(mccSource, mccCurrent)) {
            return false;
        }
        return true;
    }

    private void onDownloadComplete(long carrierKeyDownloadIdentifier, String mccMnc) {
        Log.d(LOG_TAG, "onDownloadComplete: " + carrierKeyDownloadIdentifier);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(carrierKeyDownloadIdentifier);
        Cursor cursor = this.mDownloadManager.query(query);
        InputStream source = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (8 == cursor.getInt(cursor.getColumnIndex("status"))) {
                    try {
                        source = new FileInputStream(this.mDownloadManager.openDownloadedFile(carrierKeyDownloadIdentifier).getFileDescriptor());
                        parseJsonAndPersistKey(convertToString(source), mccMnc);
                        this.mDownloadManager.remove(carrierKeyDownloadIdentifier);
                        try {
                            source.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e2) {
                        Log.e(LOG_TAG, "Error in download:" + carrierKeyDownloadIdentifier + ". " + e2);
                        this.mDownloadManager.remove(carrierKeyDownloadIdentifier);
                        if (source != null) {
                            try {
                                source.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                    } catch (Throwable th) {
                        this.mDownloadManager.remove(carrierKeyDownloadIdentifier);
                        if (source != null) {
                            try {
                                source.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
                Log.d(LOG_TAG, "Completed downloading keys");
            }
            cursor.close();
        }
    }

    private boolean carrierUsesKeys() {
        PersistableBundle b;
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (carrierConfigManager == null || (b = carrierConfigManager.getConfigForSubId(this.mPhone.getSubId())) == null) {
            return false;
        }
        this.mKeyAvailability = b.getInt("imsi_key_availability_int");
        this.mURL = b.getString("imsi_key_download_url_string");
        this.mAllowedOverMeteredNetwork = b.getBoolean("allow_metered_network_for_cert_download_bool");
        if (TextUtils.isEmpty(this.mURL) || this.mKeyAvailability == 0) {
            Log.d(LOG_TAG, "Carrier not enabled or invalid values");
            return false;
        }
        for (int key_type : CARRIER_KEY_TYPES) {
            if (isKeyEnabled(key_type)) {
                return true;
            }
        }
        return false;
    }

    private static String convertToString(InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(is), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    return sb.toString();
                }
                sb.append(line);
                sb.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x0149 A[SYNTHETIC, Splitter:B:47:0x0149] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0170 A[SYNTHETIC, Splitter:B:53:0x0170] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0182 A[SYNTHETIC, Splitter:B:59:0x0182] */
    /* JADX WARNING: Removed duplicated region for block: B:70:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:71:? A[RETURN, SYNTHETIC] */
    @VisibleForTesting
    public void parseJsonAndPersistKey(String jsonStr, String mccMnc) {
        Exception e;
        StringBuilder sb;
        String cert;
        int type;
        String str = JSON_TYPE;
        String str2 = JSON_CERTIFICATE;
        if (!TextUtils.isEmpty(jsonStr)) {
            if (!TextUtils.isEmpty(mccMnc)) {
                PemReader reader = null;
                try {
                    String[] splitValue = mccMnc.split(SEPARATOR);
                    int i = 0;
                    String mcc = splitValue[0];
                    String mnc = splitValue[1];
                    try {
                        for (JSONArray keys = new JSONObject(jsonStr).getJSONArray(JSON_CARRIER_KEYS); i < keys.length(); keys = keys) {
                            JSONObject key = keys.getJSONObject(i);
                            if (key.has(str2)) {
                                cert = key.getString(str2);
                            } else {
                                cert = key.getString(JSON_CERTIFICATE_ALTERNATE);
                            }
                            if (key.has(str)) {
                                String typeString = key.getString(str);
                                if (typeString.equals(JSON_TYPE_VALUE_EPDG)) {
                                    type = 1;
                                    String identifier = key.getString(JSON_IDENTIFIER);
                                    reader = new PemReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cert.getBytes()))));
                                    Pair<PublicKey, Long> keyInfo = getKeyInformation(reader.readPemObject().getContent());
                                    reader.close();
                                    savePublicKey((PublicKey) keyInfo.first, type, identifier, ((Long) keyInfo.second).longValue(), mcc, mnc);
                                    i++;
                                    str = str;
                                    str2 = str2;
                                } else if (!typeString.equals(JSON_TYPE_VALUE_WLAN)) {
                                    Log.e(LOG_TAG, "Invalid key-type specified: " + typeString);
                                }
                            }
                            type = 2;
                            String identifier2 = key.getString(JSON_IDENTIFIER);
                            reader = new PemReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cert.getBytes()))));
                            Pair<PublicKey, Long> keyInfo2 = getKeyInformation(reader.readPemObject().getContent());
                            reader.close();
                            savePublicKey((PublicKey) keyInfo2.first, type, identifier2, ((Long) keyInfo2.second).longValue(), mcc, mnc);
                            i++;
                            str = str;
                            str2 = str2;
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                                return;
                            } catch (Exception e2) {
                                e = e2;
                                sb = new StringBuilder();
                            }
                        } else {
                            return;
                        }
                    } catch (JSONException e3) {
                        e = e3;
                        Log.e(LOG_TAG, "Json parsing error: " + e.getMessage());
                        if (0 == 0) {
                        }
                    } catch (Exception e4) {
                        e = e4;
                        try {
                            Log.e(LOG_TAG, "Exception getting certificate: " + e);
                            if (0 == 0) {
                            }
                        } catch (Throwable th) {
                            th = th;
                            if (0 != 0) {
                            }
                            throw th;
                        }
                    }
                } catch (JSONException e5) {
                    e = e5;
                    Log.e(LOG_TAG, "Json parsing error: " + e.getMessage());
                    if (0 == 0) {
                        try {
                            reader.close();
                            return;
                        } catch (Exception e6) {
                            e = e6;
                            sb = new StringBuilder();
                        }
                    } else {
                        return;
                    }
                } catch (Exception e7) {
                    e = e7;
                    Log.e(LOG_TAG, "Exception getting certificate: " + e);
                    if (0 == 0) {
                        try {
                            reader.close();
                            return;
                        } catch (Exception e8) {
                            e = e8;
                            sb = new StringBuilder();
                        }
                    } else {
                        return;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (0 != 0) {
                        try {
                            reader.close();
                        } catch (Exception e9) {
                            Log.e(LOG_TAG, "Exception getting certificate: " + e9);
                        }
                    }
                    throw th;
                }
            }
        }
        Log.e(LOG_TAG, "jsonStr or mcc, mnc: is empty");
        return;
        sb.append("Exception getting certificate: ");
        sb.append(e);
        Log.e(LOG_TAG, sb.toString());
    }

    @VisibleForTesting
    public boolean isKeyEnabled(int keyType) {
        if (((this.mKeyAvailability >> (keyType - 1)) & 1) == 1) {
            return true;
        }
        return false;
    }

    @VisibleForTesting
    public boolean areCarrierKeysAbsentOrExpiring() {
        int[] iArr = CARRIER_KEY_TYPES;
        for (int key_type : iArr) {
            if (isKeyEnabled(key_type)) {
                ImsiEncryptionInfo imsiEncryptionInfo = this.mPhone.getCarrierInfoForImsiEncryption(key_type);
                if (imsiEncryptionInfo == null) {
                    Log.d(LOG_TAG, "Key not found for: " + key_type);
                    return true;
                } else if (imsiEncryptionInfo.getExpirationTime().getTime() - System.currentTimeMillis() < 1814400000) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean downloadKey() {
        Log.d(LOG_TAG, "starting download from: " + this.mURL);
        String simOperator = getSimOperator();
        if (!TextUtils.isEmpty(simOperator)) {
            String mcc = simOperator.substring(0, 3);
            String mnc = simOperator.substring(3);
            Log.d(LOG_TAG, "using values for mcc, mnc: " + mcc + "," + mnc);
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(this.mURL));
                request.setAllowedOverMetered(this.mAllowedOverMeteredNetwork);
                request.setVisibleInDownloadsUi(false);
                request.setNotificationVisibility(2);
                Long carrierKeyDownloadRequestId = Long.valueOf(this.mDownloadManager.enqueue(request));
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
                int slotId = this.mPhone.getPhoneId();
                Log.d(LOG_TAG, "storing values in sharedpref mcc, mnc, days: " + mcc + "," + mnc + "," + carrierKeyDownloadRequestId);
                StringBuilder sb = new StringBuilder();
                sb.append(MCC_MNC_PREF_TAG);
                sb.append(slotId);
                editor.putString(sb.toString(), mcc + SEPARATOR + mnc);
                editor.commit();
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "exception trying to dowload key from url: " + this.mURL);
                return false;
            }
        } else {
            Log.e(LOG_TAG, "mcc, mnc: is empty");
            return false;
        }
    }

    @VisibleForTesting
    public static Pair<PublicKey, Long> getKeyInformation(byte[] certificate) throws Exception {
        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certificate));
        return new Pair<>(cert.getPublicKey(), Long.valueOf(cert.getNotAfter().getTime()));
    }

    @VisibleForTesting
    public void savePublicKey(PublicKey publicKey, int type, String identifier, long expirationDate, String mcc, String mnc) {
        this.mPhone.setCarrierInfoForImsiEncryption(new ImsiEncryptionInfo(mcc, mnc, type, identifier, publicKey, new Date(expirationDate)));
    }
}
