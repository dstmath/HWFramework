package huawei.cust;

import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwCarrierConfigXmlParse {
    private static final Map EMPTY = new HashMap();
    private static final String LOG_TAG = "HwCarrierConfigXmlParse";

    public static Map parse(String fileName, int slotId) {
        log("begin parse " + fileName);
        if (TextUtils.isEmpty(fileName)) {
            return EMPTY;
        }
        return parseFile(getHighestPriorityConfigFile(fileName, slotId));
    }

    public static Map parseFile(File file) {
        XmlPullParser input = null;
        FileInputStream fileInputStream = null;
        if (file != null) {
            try {
                if (file.exists()) {
                    log("find config xml:" + file);
                    input = Xml.newPullParser();
                    fileInputStream = new FileInputStream(file);
                    try {
                        input.setInput(fileInputStream, null);
                        Map read = HwCarrierConfigXmlUtils.read(input);
                        try {
                            input.setInput(null);
                        } catch (XmlPullParserException e) {
                            loge("Loading Config failed because exception in Xml Pull Parser");
                        }
                        try {
                            fileInputStream.close();
                        } catch (IOException e2) {
                            loge("IOException in closing inputStream");
                        }
                        return read;
                    } catch (Exception e3) {
                        loge("parseFile " + file + " catch Exception");
                    }
                }
            } catch (FileNotFoundException e4) {
                loge("Exception in Loading Config because the file is not found");
                if (0 != 0) {
                    try {
                        input.setInput(null);
                    } catch (XmlPullParserException e5) {
                        loge("Loading Config failed because exception in Xml Pull Parser");
                    }
                }
                if (0 != 0) {
                    fileInputStream.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        input.setInput(null);
                    } catch (XmlPullParserException e6) {
                        loge("Loading Config failed because exception in Xml Pull Parser");
                    }
                }
                if (0 != 0) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e7) {
                        loge("IOException in closing inputStream");
                    }
                }
                throw th;
            }
        }
        log("File not found");
        if (input != null) {
            try {
                input.setInput(null);
            } catch (XmlPullParserException e8) {
                loge("Loading Config failed because exception in Xml Pull Parser");
            }
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e9) {
                loge("IOException in closing inputStream");
            }
        }
        return EMPTY;
    }

    private static File getHighestPriorityConfigFile(String fileName, int slotId) {
        try {
            return HwCfgFilePolicy.getCfgFile(fileName, 0, slotId);
        } catch (NoClassDefFoundError e) {
            loge("NoClassDefFoundError!");
            return null;
        }
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
