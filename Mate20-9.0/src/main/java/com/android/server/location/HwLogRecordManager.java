package com.android.server.location;

import android.location.Location;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.location.HwCryptoUtility;
import com.android.server.location.HwLbsLogger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HwLogRecordManager {
    public static final String COMMA_SEPARATE = ",";
    public static final String EMPTY = "";
    private static final String LOG_VERSION_DOMESTIC = "3";
    private static final String MASTER_PASSWORD = HwLocalLocationManager.MASTER_PASSWORD;
    public static final String NA = "NA";
    private static final String SESSION_START = "start";
    private static final String SESSION_STOP = "stop";
    private static final String TAG = "HwLogRecordManager";
    public static final String VERTICAL_ESC_SEPARATE = "\\|";
    public static final String VERTICAL_SEPARATE = "|";
    private static final Object mLock = new Object();
    private static volatile HwLogRecordManager mLogManager;

    private HwLogRecordManager() {
        if (isBetaUser()) {
            Log.d(TAG, "begin start Log file");
            startLogFile();
        }
    }

    public static HwLogRecordManager getInstance() {
        if (mLogManager == null) {
            synchronized (mLock) {
                if (mLogManager == null) {
                    Log.d(TAG, "mLogManager create.");
                    mLogManager = new HwLogRecordManager();
                }
            }
        }
        return mLogManager;
    }

    private void startLogFile() {
        HwXmlLogParse.createXmlSerializer();
    }

    private boolean isBetaUser() {
        if (SystemProperties.get("ro.logsystem.usertype", "0").equals("3")) {
            return true;
        }
        return false;
    }

    public void writePosition(HwLbsLogger.LocationResultRecord locationResult) {
        if (locationResult != null) {
            Location location = locationResult.getLocation();
            long timestamp = locationResult.getTimestamp();
            List<Integer> receivers = locationResult.getReceivers();
            if (location != null && receivers != null) {
                String hashReceiver = "unknown";
                int hashNumber = 0;
                int receiverSize = receivers.size();
                if (receiverSize > 0) {
                    hashNumber = receiverSize;
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < receiverSize; i++) {
                        builder.append(receivers.get(i));
                        if (i < receiverSize - 1) {
                            builder.append("; ");
                        }
                    }
                    hashReceiver = builder.toString();
                }
                String encryptedLong = String.valueOf(location.getLongitude());
                String encryptedLat = String.valueOf(location.getLatitude());
                try {
                    String str = MASTER_PASSWORD;
                    encryptedLong = HwCryptoUtility.AESLocalDbCrypto.encrypt(str, location.getLongitude() + "");
                    String str2 = MASTER_PASSWORD;
                    encryptedLat = HwCryptoUtility.AESLocalDbCrypto.encrypt(str2, location.getLatitude() + "");
                } catch (Exception e) {
                    Log.d(TAG, "exception occured when encrypt longitude and latitude");
                }
                StringBuffer dataBuffer = new StringBuffer();
                dataBuffer.append(formatTimestamp(timestamp));
                dataBuffer.append("|");
                dataBuffer.append(formatTimestamp(location.getTime()));
                dataBuffer.append(",");
                dataBuffer.append(encryptedLat);
                dataBuffer.append(",");
                dataBuffer.append(encryptedLong);
                dataBuffer.append(",");
                if (location.hasAltitude()) {
                    dataBuffer.append(location.getAltitude());
                    dataBuffer.append(",");
                } else {
                    dataBuffer.append("NA");
                    dataBuffer.append(",");
                }
                if (location.hasAccuracy()) {
                    dataBuffer.append(location.getAccuracy());
                    dataBuffer.append(",");
                } else {
                    dataBuffer.append("NA");
                    dataBuffer.append(",");
                }
                if (location.hasSpeed()) {
                    dataBuffer.append(location.getSpeed());
                    dataBuffer.append(",");
                } else {
                    dataBuffer.append("NA");
                    dataBuffer.append(",");
                }
                if (location.hasBearing()) {
                    dataBuffer.append(location.getBearing());
                    dataBuffer.append(",");
                } else {
                    dataBuffer.append("NA");
                    dataBuffer.append(",");
                }
                String provider = location.getProvider();
                if (provider != null) {
                    dataBuffer.append(provider);
                    dataBuffer.append("|");
                } else {
                    dataBuffer.append("NA");
                    dataBuffer.append("|");
                }
                dataBuffer.append(hashNumber);
                dataBuffer.append(",");
                dataBuffer.append(hashReceiver);
                HwXmlLogParse.positionTag(dataBuffer.toString());
            }
        }
    }

    public void writeEvent(HwLbsLogger.LocationEventRecord eventRecord) {
        if (eventRecord == null) {
            Log.d(TAG, "invalid evenet datas.");
        }
    }

    public void writeSession(HwLbsLogger.SessionRecord sessionRecord) {
        if (sessionRecord == null) {
            Log.d(TAG, "invalid session datas.");
            return;
        }
        StringBuffer dataBuffer = new StringBuffer();
        String action = sessionRecord.getAction();
        String type = "";
        if (action.equals(SESSION_START)) {
            dataBuffer.append(formatTimestamp(sessionRecord.getTimestamp()));
            dataBuffer.append("|");
            dataBuffer.append(sessionRecord.getAction());
            dataBuffer.append(",");
            dataBuffer.append(sessionRecord.getPackageName());
            dataBuffer.append(",");
            dataBuffer.append(sessionRecord.getProvider());
            dataBuffer.append(",");
            dataBuffer.append(sessionRecord.getInterval());
            dataBuffer.append(",");
            dataBuffer.append(sessionRecord.getReceiver());
            type = SESSION_START;
        } else if (action.equals(SESSION_STOP)) {
            dataBuffer.append(formatTimestamp(sessionRecord.getTimestamp()));
            dataBuffer.append("|");
            dataBuffer.append(sessionRecord.getAction());
            dataBuffer.append(",");
            dataBuffer.append(sessionRecord.getPackageName());
            dataBuffer.append(",");
            dataBuffer.append(sessionRecord.getReceiver());
            type = SESSION_STOP;
        }
        if (!dataBuffer.toString().equals("")) {
            HwXmlLogParse.sessionTag(dataBuffer.toString(), type);
        }
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.US).format(new Date(timestamp));
    }
}
