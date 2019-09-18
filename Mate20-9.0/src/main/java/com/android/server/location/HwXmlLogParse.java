package com.android.server.location;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import org.xmlpull.v1.XmlSerializer;

public class HwXmlLogParse {
    private static final String ACC_ATTR = "acc";
    private static final String ACTION_ATTR = "action";
    private static final String ALT_ATTR = "alt";
    private static final String COORDINATE_TAG = "coordinate";
    private static final String EVENT_ID_ATTR = "event_id";
    private static final String EVENT_TAG = "event";
    private static final int FILE_MAX_NUMBER = 5;
    private static final long FILE_MAX_SIZE = 5242880;
    private static final String HEADING_ATTR = "heading";
    private static final String INFORMATION_TAG = "infomation";
    private static final String INTERVAL_ATTR = "interval";
    private static final String LAT_ATTR = "lat";
    private static final String LBS_LOG_PATH = "/data/log/gps/hwlbslogger";
    private static final String LON_ATTR = "lon";
    private static final String NUMBER_ATTR = "number";
    private static final String PACKAGE_NAME_ATTR = "package_name";
    private static final int POSITION_ATTR_NUM = 1;
    private static final int POSITION_COORDINATE_ATTR_NUM = 8;
    private static final int POSITION_RECEIVERS_ATTR_NUM = 2;
    private static final String POSITION_TAG = "position";
    private static final String PROVIDER_ATTR = "provider";
    private static final String RECEIVER_ATTR = "receiver";
    private static final String RECEIVER_TAG = "receivers";
    private static final String ROOT_TAG = "hwlbslog";
    private static final int SESSION_ATTR_NUM = 1;
    private static final int SESSION_INFOMATION_ATTR_START = 5;
    private static final int SESSION_INFOMATION_ATTR_STOP = 3;
    private static final String SESSION_START = "start";
    private static final String SESSION_STOP = "stop";
    private static final String SESSION_TAG = "session";
    private static final String SPEED_ATTR = "speed";
    private static final String TAG = "HwXmlLogParse";
    private static final String TIMESTAMP_ATTR = "timestamp";
    private static final String TIME_ATTR = "loc_time";
    private static final String TYPE_ATTR = "type";
    private static final String VALUES_ATTR = "values";
    private static File mFile;
    private static FileOutputStream mFileOutputStream;
    private static XmlSerializer mXmlSerializer;

    private static String getLogNameTime() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(new Date());
    }

    public static void positionTag(String data) {
        if (data == null || data.equals("")) {
            Log.d(TAG, "positionTag, data is invalid.");
            return;
        }
        checkFileSize();
        String[] datas = moreDataCheck(data, 3, POSITION_TAG);
        if (datas.length != 0) {
            String[] values = dataCheck(datas[0], 1, POSITION_TAG);
            if (values.length != 0) {
                createStartTagHasAttr(POSITION_TAG, TIMESTAMP_ATTR, values[0]);
                String[] values2 = dataCheck(datas[1], 8, COORDINATE_TAG);
                if (values2.length != 0) {
                    createNode(COORDINATE_TAG, new String[]{TIMESTAMP_ATTR, LAT_ATTR, LON_ATTR, ALT_ATTR, ACC_ATTR, SPEED_ATTR, HEADING_ATTR, PROVIDER_ATTR}, values2);
                }
                String[] values3 = dataCheck(datas[2], 2, RECEIVER_TAG);
                if (values3.length != 0) {
                    createNode(RECEIVER_TAG, new String[]{"number", VALUES_ATTR}, values3);
                }
                createEndTag(POSITION_TAG);
                flush();
            }
        }
    }

    public static void sessionTag(String data, String type) {
        if (data == null || data.equals("")) {
            Log.d(TAG, "sessionTag, data is invalid.");
            return;
        }
        checkFileSize();
        String[] datas = moreDataCheck(data, 2, SESSION_TAG);
        if (datas.length != 0) {
            String[] values = dataCheck(datas[0], 1, SESSION_TAG);
            if (values.length != 0) {
                createStartTagHasAttr(SESSION_TAG, TIMESTAMP_ATTR, values[0]);
                if (SESSION_START.equals(type)) {
                    String[] values2 = dataCheck(datas[1], 5, INFORMATION_TAG);
                    if (values2.length != 0) {
                        createNode(INFORMATION_TAG, new String[]{"action", "package_name", "type", "interval", "receiver"}, values2);
                    }
                } else if (SESSION_STOP.equals(type)) {
                    String[] values3 = dataCheck(datas[1], 3, INFORMATION_TAG);
                    if (values3.length != 0) {
                        createNode(INFORMATION_TAG, new String[]{"action", "package_name", "receiver"}, values3);
                    }
                }
                createEndTag(SESSION_TAG);
                flush();
            }
        }
    }

    private static void checkFileSize() {
        if (mFile == null) {
            Log.e(TAG, "mfile is null.");
            return;
        }
        if (mFile.length() >= 5242880) {
            Log.d(TAG, "file size 5M");
            createEndTag(ROOT_TAG);
            endDocument();
            createXmlSerializer();
        }
    }

    private static void checkFileCount() {
        if (mFile == null) {
            Log.e(TAG, "mfile is null.");
            return;
        }
        File dir = mFile.getParentFile();
        if (!dir.isDirectory()) {
            Log.e(TAG, "mfile parent is not a directory.");
            return;
        }
        File[] files = dir.listFiles();
        if (files != null && files.length >= 5) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File lhs, File rhs) {
                    return (int) (rhs.lastModified() - lhs.lastModified());
                }
            });
            for (int i = 4; i < files.length; i++) {
                File file = files[i];
                if (file.delete()) {
                    Log.d(TAG, "deleted old file " + file);
                }
            }
        }
    }

    private static void createXmlFile() {
        StringBuffer logName = new StringBuffer();
        logName.append(LBS_LOG_PATH);
        logName.append(File.separator);
        logName.append("hwlbslog_");
        logName.append(getLogNameTime());
        logName.append(".xml");
        Log.d(TAG, "file name:" + logName.toString());
        try {
            mFile = new File(logName.toString());
            if (!mFile.getParentFile().exists() && mFile.getParentFile().mkdirs() && mFile.createNewFile()) {
                Log.d(TAG, "createNewFile ok.");
            }
            checkFileCount();
            mFileOutputStream = new FileOutputStream(mFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "create xml log fail, file not found.");
        } catch (Exception e2) {
            Log.e(TAG, "create xml log fail.");
        }
    }

    public static void createXmlSerializer() {
        createXmlFile();
        try {
            mXmlSerializer = Xml.newSerializer();
            mXmlSerializer.setOutput(mFileOutputStream, "utf-8");
            mXmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            mXmlSerializer.startDocument("utf-8", true);
            createStartTag(ROOT_TAG);
            Log.d(TAG, "create xmlSerializer succeed.");
        } catch (IOException e) {
            Log.e(TAG, "create xmlSerializer fail.");
        }
    }

    private static void endDocument() {
        try {
            if (mXmlSerializer == null) {
                if (mFileOutputStream != null) {
                    try {
                        mFileOutputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close mFileOutputStream error.");
                    }
                }
                return;
            }
            mXmlSerializer.flush();
            mXmlSerializer.endDocument();
            if (mFileOutputStream != null) {
                try {
                    mFileOutputStream.close();
                } catch (IOException e2) {
                    Log.e(TAG, "close mFileOutputStream error.");
                }
            }
        } catch (Exception e3) {
            Log.e(TAG, "end document error.");
            if (mFileOutputStream != null) {
                mFileOutputStream.close();
            }
        } catch (Throwable th) {
            if (mFileOutputStream != null) {
                try {
                    mFileOutputStream.close();
                } catch (IOException e4) {
                    Log.e(TAG, "close mFileOutputStream error.");
                }
            }
            throw th;
        }
    }

    private static String[] dataCheck(String data, int len, String tag) {
        if (TextUtils.isEmpty(data)) {
            Log.e(TAG, tag + " tag: data is null.");
            return new String[0];
        }
        String[] values = data.split(",");
        if (values.length != len) {
            Log.e(TAG, tag + " tag: invalid data.");
            return new String[0];
        }
        for (int i = 0; i < values.length; i++) {
            if ("NA".equals(values[i])) {
                values[i] = "";
            }
        }
        return values;
    }

    private static String[] moreDataCheck(String data, int len, String tag) {
        if (TextUtils.isEmpty(data)) {
            Log.e(TAG, tag + " tag: data is null.");
            return new String[0];
        }
        String[] values = data.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
        if (values.length == len) {
            return values;
        }
        Log.e(TAG, tag + " tag: invalid data.");
        return new String[0];
    }

    private static void createStartTag(String tag) {
        try {
            if (mXmlSerializer != null) {
                if (!TextUtils.isEmpty(tag)) {
                    mXmlSerializer.startTag(null, tag);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "create start tag fail.");
        }
    }

    private static void createStartTagHasAttr(String tag, String attr, String value) {
        try {
            if (mXmlSerializer != null && !TextUtils.isEmpty(attr)) {
                if (!TextUtils.isEmpty(value)) {
                    mXmlSerializer.startTag(null, tag);
                    mXmlSerializer.attribute(null, attr, value);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "create start tag fail.");
        }
    }

    private static void createEndTag(String tag) {
        try {
            if (mXmlSerializer != null) {
                if (!TextUtils.isEmpty(tag)) {
                    mXmlSerializer.endTag(null, tag);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "create end tag fail.");
        }
    }

    private static void createNode(String tag, String[] attrs, String[] values) {
        try {
            if (!(mXmlSerializer == null || attrs == null)) {
                if (values != null) {
                    mXmlSerializer.startTag(null, tag);
                    int len = attrs.length;
                    for (int i = 0; i < len; i++) {
                        mXmlSerializer.attribute(null, attrs[i], values[i]);
                    }
                    mXmlSerializer.endTag(null, tag);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "create node fail.");
        }
    }

    private static void flush() {
        try {
            if (mXmlSerializer != null) {
                mXmlSerializer.flush();
            }
        } catch (Exception e) {
            Log.e(TAG, "flush fail.");
        }
    }
}
