package com.android.server.os;

import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class UDIDModelWhiteConfig {
    private static final String TAG = "UDIDModelWhiteConfig";
    private static final int UDID_MODEL_TYPE = 0;
    private static final String UDID_MODEL_WHITE_LIST = "udid_model_whitelist.xml";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_UDID_MODEL = "udid_model";
    private static UDIDModelWhiteConfig udidModelWhiteConfig;
    private List<String> whiteModelInfos = new ArrayList();

    public static UDIDModelWhiteConfig getInstance() {
        if (udidModelWhiteConfig == null) {
            udidModelWhiteConfig = new UDIDModelWhiteConfig();
        }
        return udidModelWhiteConfig;
    }

    private UDIDModelWhiteConfig() {
        loadWhiteModelWhiteList();
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0039 A:{Catch:{ FileNotFoundException -> 0x0088, XmlPullParserException -> 0x0118, IOException -> 0x00d3, all -> 0x0180 }} */
    /* JADX WARNING: Removed duplicated region for block: B:64:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00af A:{SYNTHETIC, Splitter: B:32:0x00af} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadWhiteModelWhiteList() {
        InputStream inputStream = null;
        File file = null;
        try {
            file = HwCfgFilePolicy.getCfgFile("xml/udid_model_whitelist.xml", 0);
            Slog.w(TAG, "udidModeFile exits? " + file.exists());
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        } catch (Exception e2) {
            Log.d(TAG, "HwCfgFilePolicy get udid_model_whitelist exception");
        }
        if (file != null) {
            try {
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                    if (inputStream != null) {
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(inputStream, null);
                        for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                            if (xmlEventType == 2 && XML_UDID_MODEL.equals(xmlParser.getName())) {
                                addUDIDModelInfo(xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME));
                            }
                        }
                    }
                    if (inputStream == null) {
                        try {
                            inputStream.close();
                            return;
                        } catch (IOException e3) {
                            Log.e(TAG, "load udid model config: IO Exception while closing stream: " + e3.getMessage());
                            return;
                        }
                    }
                    return;
                }
            } catch (FileNotFoundException e4) {
                Log.e(TAG, "load udid model config: " + e4.getMessage());
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e32) {
                        Log.e(TAG, "load udid model config: IO Exception while closing stream: " + e32.getMessage());
                        return;
                    }
                }
                return;
            } catch (XmlPullParserException e5) {
                Log.e(TAG, "load udid model config: " + e5.getMessage());
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e322) {
                        Log.e(TAG, "load udid model config: IO Exception while closing stream: " + e322.getMessage());
                        return;
                    }
                }
                return;
            } catch (IOException e3222) {
                Log.e(TAG, "load udid model config: " + e3222.getMessage());
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e32222) {
                        Log.e(TAG, "load udid model config: IO Exception while closing stream: " + e32222.getMessage());
                        return;
                    }
                }
                return;
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e322222) {
                        Log.e(TAG, "load udid model config: IO Exception while closing stream: " + e322222.getMessage());
                    }
                }
            }
        }
        Slog.w(TAG, "udid_model_whitelist.xml is not exist");
        if (inputStream != null) {
        }
        if (inputStream == null) {
        }
    }

    public boolean isWhiteModelForUDID(String model) {
        int size = this.whiteModelInfos.size();
        if (size <= 0) {
            loadWhiteModelWhiteList();
        }
        for (int i = 0; i < size; i++) {
            String modelInWhiteList = (String) this.whiteModelInfos.get(i);
            if (model != null && modelInWhiteList != null && model.indexOf(modelInWhiteList) == 0) {
                return true;
            }
        }
        return false;
    }

    private void addUDIDModelInfo(String model) {
        if (!this.whiteModelInfos.contains(model)) {
            this.whiteModelInfos.add(model);
        }
    }
}
