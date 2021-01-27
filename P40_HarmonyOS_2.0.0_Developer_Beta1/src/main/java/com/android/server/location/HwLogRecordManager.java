package com.android.server.location;

import android.location.Location;
import android.os.SystemProperties;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.android.server.location.HwCryptoUtility;
import com.android.server.location.HwLbsLogger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HwLogRecordManager {
    public static final String COMMA_SEPARATE = ",";
    private static final int DEFAULT_SIZE = 16;
    public static final String EMPTY = "";
    private static final Object LOCK = new Object();
    private static final String LOG_VERSION_DOMESTIC = "3";
    private static final String MASTER_PASSWORD = HwLocalLocationManager.MASTER_PASSWORD;
    public static final String NA = "NA";
    private static final String SESSION_START = "start";
    private static final String SESSION_STOP = "stop";
    private static final String TAG = "HwLogRecordManager";
    public static final String VERTICAL_ESC_SEPARATE = "\\|";
    public static final String VERTICAL_SEPARATE = "|";
    private static volatile HwLogRecordManager sLogManager;

    private HwLogRecordManager() {
        if (isBetaUser()) {
            LBSLog.i(TAG, false, "begin start Log file", new Object[0]);
            startLogFile();
        }
    }

    public static HwLogRecordManager getInstance() {
        if (sLogManager == null) {
            synchronized (LOCK) {
                if (sLogManager == null) {
                    LBSLog.i(TAG, false, "sLogManager create.", new Object[0]);
                    sLogManager = new HwLogRecordManager();
                }
            }
        }
        return sLogManager;
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
            if (location == null) {
                return;
            }
            if (receivers != null) {
                String hashReceiver = ModelBaseService.UNKONW_IDENTIFY_RET;
                int hashNumber = 0;
                int receiverSize = receivers.size();
                if (receiverSize > 0) {
                    hashNumber = receiverSize;
                    StringBuilder builder = new StringBuilder(16);
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
                    LBSLog.i(TAG, false, "exception occured when encrypt longitude and latitude", new Object[0]);
                }
                StringBuffer dataBuffer = new StringBuffer(16);
                dataBuffer.append(formatTimestamp(timestamp));
                dataBuffer.append(VERTICAL_SEPARATE);
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
                    dataBuffer.append(VERTICAL_SEPARATE);
                } else {
                    dataBuffer.append("NA");
                    dataBuffer.append(VERTICAL_SEPARATE);
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
            LBSLog.i(TAG, false, "invalid evenet datas.", new Object[0]);
        }
    }

    public void writeSession(HwLbsLogger.SessionRecord sessionRecord) {
        if (sessionRecord == null) {
            LBSLog.i(TAG, false, "invalid session datas.", new Object[0]);
            return;
        }
        StringBuffer dataBuffer = new StringBuffer(16);
        String action = sessionRecord.getAction();
        String type = "";
        if (SESSION_START.equals(action)) {
            dataBuffer.append(formatTimestamp(sessionRecord.getTimestamp()));
            dataBuffer.append(VERTICAL_SEPARATE);
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
        } else if (SESSION_STOP.equals(action)) {
            dataBuffer.append(formatTimestamp(sessionRecord.getTimestamp()));
            dataBuffer.append(VERTICAL_SEPARATE);
            dataBuffer.append(sessionRecord.getAction());
            dataBuffer.append(",");
            dataBuffer.append(sessionRecord.getPackageName());
            dataBuffer.append(",");
            dataBuffer.append(sessionRecord.getReceiver());
            type = SESSION_STOP;
        }
        if (!"".equals(dataBuffer.toString())) {
            HwXmlLogParse.sessionTag(dataBuffer.toString(), type);
        }
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.US).format(new Date(timestamp));
    }
}
