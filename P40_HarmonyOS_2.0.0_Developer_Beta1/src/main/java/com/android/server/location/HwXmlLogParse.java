package com.android.server.location;

import android.text.TextUtils;
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
    private static File sFile;
    private static FileOutputStream sFileOutputStream;
    private static XmlSerializer sXmlSerializer;

    private static String getLogNameTime() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(new Date());
    }

    public static void positionTag(String data) {
        if (data == null || "".equals(data)) {
            LBSLog.i(TAG, false, "positionTag, data is invalid.", new Object[0]);
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
                    createNode(RECEIVER_TAG, new String[]{NUMBER_ATTR, VALUES_ATTR}, values3);
                }
                createEndTag(POSITION_TAG);
                flush();
            }
        }
    }

    public static void sessionTag(String data, String type) {
        if (data == null || "".equals(data)) {
            LBSLog.i(TAG, false, "sessionTag, data is invalid.", new Object[0]);
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
                        createNode(INFORMATION_TAG, new String[]{"action", "package_name", "type", INTERVAL_ATTR, RECEIVER_ATTR}, values2);
                    }
                } else if (SESSION_STOP.equals(type)) {
                    String[] values3 = dataCheck(datas[1], 3, INFORMATION_TAG);
                    if (values3.length != 0) {
                        createNode(INFORMATION_TAG, new String[]{"action", "package_name", RECEIVER_ATTR}, values3);
                    }
                } else {
                    LBSLog.d(TAG, false, "sessionTag", new Object[0]);
                }
                createEndTag(SESSION_TAG);
                flush();
            }
        }
    }

    private static void checkFileSize() {
        File file = sFile;
        if (file == null) {
            LBSLog.e(TAG, false, "sfile is null.", new Object[0]);
        } else if (file.length() >= FILE_MAX_SIZE) {
            LBSLog.i(TAG, false, "file size 5M", new Object[0]);
            createEndTag(ROOT_TAG);
            endDocument();
            createXmlSerializer();
        }
    }

    private static void checkFileCount() {
        File file = sFile;
        if (file == null) {
            LBSLog.e(TAG, false, "sfile is null.", new Object[0]);
            return;
        }
        File dir = file.getParentFile();
        if (!dir.isDirectory()) {
            LBSLog.e(TAG, false, "mfile parent is not a directory.", new Object[0]);
            return;
        }
        File[] files = dir.listFiles();
        if (files != null && files.length >= 5) {
            Arrays.sort(files, new Comparator<File>() {
                /* class com.android.server.location.HwXmlLogParse.AnonymousClass1 */

                public int compare(File lhs, File rhs) {
                    return (int) (rhs.lastModified() - lhs.lastModified());
                }
            });
            for (int i = 4; i < files.length; i++) {
                File file2 = files[i];
                if (file2.delete()) {
                    LBSLog.i(TAG, false, "deleted old file %{private}s", file2);
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
        LBSLog.i(TAG, false, "file name: %{private}s", logName.toString());
        try {
            sFile = new File(logName.toString());
            if (!sFile.getParentFile().exists() && sFile.getParentFile().mkdirs() && sFile.createNewFile()) {
                LBSLog.i(TAG, false, "createNewFile ok.", new Object[0]);
            }
            checkFileCount();
            sFileOutputStream = new FileOutputStream(sFile);
        } catch (FileNotFoundException e) {
            LBSLog.e(TAG, false, "create xml log fail, file not found.", new Object[0]);
        } catch (Exception e2) {
            LBSLog.e(TAG, false, "create xml log fail.", new Object[0]);
        }
    }

    public static void createXmlSerializer() {
        createXmlFile();
        try {
            sXmlSerializer = Xml.newSerializer();
            sXmlSerializer.setOutput(sFileOutputStream, "utf-8");
            sXmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            sXmlSerializer.startDocument("utf-8", true);
            createStartTag(ROOT_TAG);
            LBSLog.i(TAG, false, "create xmlSerializer succeed.", new Object[0]);
        } catch (IOException e) {
            LBSLog.e(TAG, false, "create xmlSerializer fail.", new Object[0]);
        }
    }

    private static void endDocument() {
        try {
            if (sXmlSerializer == null) {
                FileOutputStream fileOutputStream = sFileOutputStream;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        LBSLog.e(TAG, false, "close sFileOutputStream error.", new Object[0]);
                    }
                }
            } else {
                sXmlSerializer.flush();
                sXmlSerializer.endDocument();
                FileOutputStream fileOutputStream2 = sFileOutputStream;
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e2) {
                        LBSLog.e(TAG, false, "close sFileOutputStream error.", new Object[0]);
                    }
                }
            }
        } catch (Exception e3) {
            LBSLog.e(TAG, false, "end document error.", new Object[0]);
            FileOutputStream fileOutputStream3 = sFileOutputStream;
            if (fileOutputStream3 != null) {
                fileOutputStream3.close();
            }
        } catch (Throwable th) {
            FileOutputStream fileOutputStream4 = sFileOutputStream;
            if (fileOutputStream4 != null) {
                try {
                    fileOutputStream4.close();
                } catch (IOException e4) {
                    LBSLog.e(TAG, false, "close sFileOutputStream error.", new Object[0]);
                }
            }
            throw th;
        }
    }

    private static String[] dataCheck(String data, int len, String tag) {
        if (TextUtils.isEmpty(data)) {
            LBSLog.e(TAG, false, "%{public}s tag: data is null.", tag);
            return new String[0];
        }
        String[] values = data.split(",");
        if (values.length != len) {
            LBSLog.e(TAG, false, "%{public}s tag: invalid data.", tag);
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
            LBSLog.e(TAG, false, "%{public}s tag: data is null.", tag);
            return new String[0];
        }
        String[] values = data.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
        if (values.length == len) {
            return values;
        }
        LBSLog.e(TAG, false, "%{public}s tag: invalid data.", tag);
        return new String[0];
    }

    private static void createStartTag(String tag) {
        try {
            if (sXmlSerializer == null) {
                return;
            }
            if (!TextUtils.isEmpty(tag)) {
                sXmlSerializer.startTag(null, tag);
            }
        } catch (Exception e) {
            LBSLog.e(TAG, false, "create start tag fail.", new Object[0]);
        }
    }

    private static void createStartTagHasAttr(String tag, String attr, String value) {
        try {
            if (sXmlSerializer != null && !TextUtils.isEmpty(attr)) {
                if (!TextUtils.isEmpty(value)) {
                    sXmlSerializer.startTag(null, tag);
                    sXmlSerializer.attribute(null, attr, value);
                }
            }
        } catch (Exception e) {
            LBSLog.e(TAG, false, "create start tag fail.", new Object[0]);
        }
    }

    private static void createEndTag(String tag) {
        try {
            if (sXmlSerializer == null) {
                return;
            }
            if (!TextUtils.isEmpty(tag)) {
                sXmlSerializer.endTag(null, tag);
            }
        } catch (Exception e) {
            LBSLog.e(TAG, false, "create end tag fail.", new Object[0]);
        }
    }

    private static void createNode(String tag, String[] attrs, String[] values) {
        try {
            if (!(sXmlSerializer == null || attrs == null)) {
                if (values != null) {
                    sXmlSerializer.startTag(null, tag);
                    int len = attrs.length;
                    for (int i = 0; i < len; i++) {
                        sXmlSerializer.attribute(null, attrs[i], values[i]);
                    }
                    sXmlSerializer.endTag(null, tag);
                }
            }
        } catch (IOException e) {
            LBSLog.e(TAG, false, "create node fail.", new Object[0]);
        }
    }

    private static void flush() {
        try {
            if (sXmlSerializer != null) {
                sXmlSerializer.flush();
            }
        } catch (Exception e) {
            LBSLog.e(TAG, false, "flush fail.", new Object[0]);
        }
    }
}
