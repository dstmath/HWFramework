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

public class CarrierKeyDownloadManager {
    private static final int[] CARRIER_KEY_TYPES = {1, 2};
    private static final int DAY_IN_MILLIS = 86400000;
    private static final int END_RENEWAL_WINDOW_DAYS = 7;
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
    private static final int UNINITIALIZED_KEY_TYPE = -1;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                int slotId = CarrierKeyDownloadManager.this.mPhone.getPhoneId();
                if (action.equals(CarrierKeyDownloadManager.INTENT_KEY_RENEWAL_ALARM_PREFIX + slotId)) {
                    Log.d(CarrierKeyDownloadManager.LOG_TAG, "Handling key renewal alarm: " + action);
                    CarrierKeyDownloadManager.this.handleAlarmOrConfigChange();
                } else if ("com.android.internal.telephony.ACTION_CARRIER_CERTIFICATE_DOWNLOAD".equals(action)) {
                    if (slotId == intent.getIntExtra("phone", -1)) {
                        Log.d(CarrierKeyDownloadManager.LOG_TAG, "Handling reset intent: " + action);
                        CarrierKeyDownloadManager.this.handleAlarmOrConfigChange();
                    }
                } else if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                    if (slotId == intent.getIntExtra("phone", -1)) {
                        Log.d(CarrierKeyDownloadManager.LOG_TAG, "Carrier Config changed: " + action);
                        CarrierKeyDownloadManager.this.handleAlarmOrConfigChange();
                    }
                } else if ("android.intent.action.DOWNLOAD_COMPLETE".equals(action)) {
                    Log.d(CarrierKeyDownloadManager.LOG_TAG, "Download Complete");
                    long carrierKeyDownloadIdentifier = intent.getLongExtra("extra_download_id", 0);
                    String mccMnc = CarrierKeyDownloadManager.this.getMccMncSetFromPref();
                    if (CarrierKeyDownloadManager.this.isValidDownload(mccMnc)) {
                        CarrierKeyDownloadManager.this.onDownloadComplete(carrierKeyDownloadIdentifier, mccMnc);
                        CarrierKeyDownloadManager.this.onPostDownloadProcessing(carrierKeyDownloadIdentifier);
                    }
                }
            }
        }
    };
    private final Context mContext;
    public final DownloadManager mDownloadManager;
    @VisibleForTesting
    public int mKeyAvailability = 0;
    /* access modifiers changed from: private */
    public final Phone mPhone;
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

    /* access modifiers changed from: private */
    public void onPostDownloadProcessing(long carrierKeyDownloadIdentifier) {
        resetRenewalAlarm();
        cleanupDownloadPreferences(carrierKeyDownloadIdentifier);
    }

    /* access modifiers changed from: private */
    public void handleAlarmOrConfigChange() {
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
        PendingIntent carrierKeyDownloadIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_KEY_RENEWAL_ALARM_PREFIX + slotId), 134217728);
        Context context = this.mContext;
        Context context2 = this.mContext;
        ((AlarmManager) context.getSystemService("alarm")).cancel(carrierKeyDownloadIntent);
    }

    @VisibleForTesting
    public long getExpirationDate() {
        long minExpirationDate = Long.MAX_VALUE;
        for (int key_type : CARRIER_KEY_TYPES) {
            if (isKeyEnabled(key_type)) {
                ImsiEncryptionInfo imsiEncryptionInfo = this.mPhone.getCarrierInfoForImsiEncryption(key_type);
                if (!(imsiEncryptionInfo == null || imsiEncryptionInfo.getExpirationTime() == null || minExpirationDate <= imsiEncryptionInfo.getExpirationTime().getTime())) {
                    minExpirationDate = imsiEncryptionInfo.getExpirationTime().getTime();
                }
            }
        }
        if (minExpirationDate == Long.MAX_VALUE || minExpirationDate < System.currentTimeMillis() + 604800000) {
            return System.currentTimeMillis() + 86400000;
        }
        return minExpirationDate - ((long) (new Random().nextInt(1814400000 - 604800000) + 604800000));
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

    /* access modifiers changed from: private */
    public String getMccMncSetFromPref() {
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

    /* access modifiers changed from: private */
    public void onDownloadComplete(long carrierKeyDownloadIdentifier, String mccMnc) {
        Log.d(LOG_TAG, "onDownloadComplete: " + carrierKeyDownloadIdentifier);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(new long[]{carrierKeyDownloadIdentifier});
        Cursor cursor = this.mDownloadManager.query(query);
        InputStream source = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (8 == cursor.getInt(cursor.getColumnIndex("status"))) {
                    try {
                        source = new FileInputStream(this.mDownloadManager.openDownloadedFile(carrierKeyDownloadIdentifier).getFileDescriptor());
                        parseJsonAndPersistKey(convertToString(source), mccMnc);
                        this.mDownloadManager.remove(new long[]{carrierKeyDownloadIdentifier});
                        try {
                            source.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e2) {
                        Log.e(LOG_TAG, "Error in download:" + carrierKeyDownloadIdentifier + ". " + e2);
                        this.mDownloadManager.remove(new long[]{carrierKeyDownloadIdentifier});
                        if (source != null) {
                            source.close();
                        }
                    } catch (Throwable th) {
                        this.mDownloadManager.remove(new long[]{carrierKeyDownloadIdentifier});
                        if (source != null) {
                            try {
                                source.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
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
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (carrierConfigManager == null) {
            return false;
        }
        PersistableBundle b = carrierConfigManager.getConfigForSubId(this.mPhone.getSubId());
        if (b == null) {
            return false;
        }
        this.mKeyAvailability = b.getInt("imsi_key_availability_int");
        this.mURL = b.getString("imsi_key_download_url_string");
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
                String readLine = reader.readLine();
                String line = readLine;
                if (readLine == null) {
                    return sb.toString();
                }
                sb.append(line);
                sb.append(10);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:0x0147 A[SYNTHETIC, Splitter:B:63:0x0147] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0176 A[SYNTHETIC, Splitter:B:71:0x0176] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x018a A[SYNTHETIC, Splitter:B:78:0x018a] */
    @VisibleForTesting
    public void parseJsonAndPersistKey(String jsonStr, String mccMnc) {
        PemReader reader;
        PemReader reader2;
        String str;
        StringBuilder sb;
        String cert;
        if (TextUtils.isEmpty(jsonStr) || TextUtils.isEmpty(mccMnc)) {
            String str2 = jsonStr;
            String str3 = mccMnc;
            Log.e(LOG_TAG, "jsonStr or mcc, mnc: is empty");
            return;
        }
        PemReader reader3 = null;
        try {
            String[] splitValue = mccMnc.split(SEPARATOR);
            int i = 0;
            String mcc = splitValue[0];
            String mnc = splitValue[1];
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONArray keys = jsonObj.getJSONArray(JSON_CARRIER_KEYS);
                while (i < keys.length()) {
                    JSONObject key = keys.getJSONObject(i);
                    if (key.has(JSON_CERTIFICATE)) {
                        cert = key.getString(JSON_CERTIFICATE);
                    } else {
                        cert = key.getString(JSON_CERTIFICATE_ALTERNATE);
                    }
                    String cert2 = cert;
                    String typeString = key.getString(JSON_TYPE);
                    int type = -1;
                    if (typeString.equals(JSON_TYPE_VALUE_WLAN)) {
                        type = 2;
                    } else if (typeString.equals(JSON_TYPE_VALUE_EPDG)) {
                        type = 1;
                    }
                    int type2 = type;
                    String identifier = key.getString(JSON_IDENTIFIER);
                    ByteArrayInputStream inStream = new ByteArrayInputStream(cert2.getBytes());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
                    reader3 = new PemReader(bufferedReader);
                    try {
                        Pair<PublicKey, Long> keyInfo = getKeyInformation(reader3.readPemObject().getContent());
                        reader3.close();
                        JSONObject jsonObj2 = jsonObj;
                        PemReader reader4 = reader3;
                        try {
                            Pair<PublicKey, Long> pair = keyInfo;
                            BufferedReader bufferedReader2 = bufferedReader;
                            PublicKey publicKey = (PublicKey) keyInfo.first;
                            ByteArrayInputStream byteArrayInputStream = inStream;
                            String str4 = cert2;
                            String str5 = typeString;
                            savePublicKey(publicKey, type2, identifier, ((Long) keyInfo.second).longValue(), mcc, mnc);
                            i++;
                            jsonObj = jsonObj2;
                            reader3 = reader4;
                        } catch (JSONException e) {
                            e = e;
                            reader3 = reader4;
                            Log.e(LOG_TAG, "Json parsing error: " + e.getMessage());
                            if (reader3 != null) {
                            }
                        } catch (Exception e2) {
                            e = e2;
                            reader3 = reader4;
                            try {
                                Log.e(LOG_TAG, "Exception getting certificate: " + e);
                                if (reader3 != null) {
                                }
                            } catch (Throwable th) {
                                th = th;
                                reader = reader3;
                                reader2 = th;
                                if (reader != null) {
                                }
                                throw reader2;
                            }
                        } catch (Throwable th2) {
                            reader2 = th2;
                            reader = reader4;
                            if (reader != null) {
                            }
                            throw reader2;
                        }
                    } catch (JSONException e3) {
                        e = e3;
                        PemReader pemReader = reader3;
                        Log.e(LOG_TAG, "Json parsing error: " + e.getMessage());
                        if (reader3 != null) {
                        }
                    } catch (Exception e4) {
                        e = e4;
                        PemReader pemReader2 = reader3;
                        Log.e(LOG_TAG, "Exception getting certificate: " + e);
                        if (reader3 != null) {
                        }
                    } catch (Throwable th3) {
                        PemReader pemReader3 = reader3;
                        reader2 = th3;
                        reader = pemReader3;
                        if (reader != null) {
                        }
                        throw reader2;
                    }
                }
                if (reader3 != null) {
                    try {
                        reader3.close();
                    } catch (Exception e5) {
                        e = e5;
                        Exception exc = e;
                        str = LOG_TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (JSONException e6) {
                e = e6;
                Log.e(LOG_TAG, "Json parsing error: " + e.getMessage());
                if (reader3 != null) {
                }
            } catch (Exception e7) {
                e = e7;
                Log.e(LOG_TAG, "Exception getting certificate: " + e);
                if (reader3 != null) {
                }
            }
        } catch (JSONException e8) {
            e = e8;
            String str6 = jsonStr;
            Log.e(LOG_TAG, "Json parsing error: " + e.getMessage());
            if (reader3 != null) {
                try {
                    reader3.close();
                } catch (Exception e9) {
                    e = e9;
                    Exception exc2 = e;
                    str = LOG_TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e10) {
            e = e10;
            String str7 = jsonStr;
            Log.e(LOG_TAG, "Exception getting certificate: " + e);
            if (reader3 != null) {
                try {
                    reader3.close();
                } catch (Exception e11) {
                    e = e11;
                    Exception exc3 = e;
                    str = LOG_TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th4) {
            th = th4;
            String str8 = jsonStr;
            reader = reader3;
            reader2 = th;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e12) {
                    Exception exc4 = e12;
                    Log.e(LOG_TAG, "Exception getting certificate: " + e12);
                }
            }
            throw reader2;
        }
        sb.append("Exception getting certificate: ");
        sb.append(e);
        Log.e(str, sb.toString());
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
        int length = iArr.length;
        int i = 0;
        while (i < length) {
            int key_type = iArr[i];
            if (!isKeyEnabled(key_type)) {
                i++;
            } else {
                ImsiEncryptionInfo imsiEncryptionInfo = this.mPhone.getCarrierInfoForImsiEncryption(key_type);
                boolean z = true;
                if (imsiEncryptionInfo == null) {
                    Log.d(LOG_TAG, "Key not found for: " + key_type);
                    return true;
                }
                if (imsiEncryptionInfo.getExpirationTime().getTime() - System.currentTimeMillis() >= 1814400000) {
                    z = false;
                }
                return z;
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
                request.setAllowedOverMetered(false);
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
        ImsiEncryptionInfo imsiEncryptionInfo = new ImsiEncryptionInfo(mcc, mnc, type, identifier, publicKey, new Date(expirationDate));
        this.mPhone.setCarrierInfoForImsiEncryption(imsiEncryptionInfo);
    }
}
