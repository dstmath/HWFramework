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

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0068 A[Catch:{ FileNotFoundException -> 0x005d, XmlPullParserException -> 0x005a, IOException -> 0x0058, all -> 0x0055 }] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x009a A[SYNTHETIC, Splitter:B:38:0x009a] */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private void loadWhiteModelWhiteList() {
        StringBuilder sb;
        IOException e;
        InputStream inputStream = null;
        File udidModeFile = null;
        try {
            udidModeFile = HwCfgFilePolicy.getCfgFile("xml/udid_model_whitelist.xml", 0);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("udidModeFile exits? ");
            sb2.append(udidModeFile != null ? Boolean.valueOf(udidModeFile.exists()) : "udidModeFile is null");
            Slog.w(TAG, sb2.toString());
        } catch (NoClassDefFoundError e2) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        } catch (Exception e3) {
            Log.d(TAG, "HwCfgFilePolicy get udid_model_whitelist exception");
        }
        if (udidModeFile != null) {
            try {
                if (udidModeFile.exists()) {
                    inputStream = new FileInputStream(udidModeFile);
                    if (inputStream != null) {
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(inputStream, null);
                        for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                            if (xmlEventType == 2 && XML_UDID_MODEL.equals(xmlParser.getName())) {
                                addUDIDModelInfo(xmlParser.getAttributeValue(null, "name"));
                            }
                        }
                    }
                    if (inputStream == null) {
                        try {
                            inputStream.close();
                            return;
                        } catch (IOException e4) {
                            e = e4;
                            sb = new StringBuilder();
                        }
                    } else {
                        return;
                    }
                }
            } catch (FileNotFoundException e5) {
                Log.e(TAG, "load udid model config: " + e5.getMessage());
                if (0 != 0) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e6) {
                        e = e6;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (XmlPullParserException e7) {
                Log.e(TAG, "load udid model config: " + e7.getMessage());
                if (0 != 0) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e8) {
                        e = e8;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e9) {
                Log.e(TAG, "load udid model config: " + e9.getMessage());
                if (0 != 0) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e10) {
                        e = e10;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e11) {
                        Log.e(TAG, "load udid model config: IO Exception while closing stream: " + e11.getMessage());
                    }
                }
                throw th;
            }
        }
        Slog.w(TAG, "udid_model_whitelist.xml is not exist");
        if (inputStream != null) {
        }
        if (inputStream == null) {
        }
        sb.append("load udid model config: IO Exception while closing stream: ");
        sb.append(e.getMessage());
        Log.e(TAG, sb.toString());
    }

    public boolean isWhiteModelForUDID(String model) {
        int size = this.whiteModelInfos.size();
        if (size <= 0) {
            loadWhiteModelWhiteList();
            size = this.whiteModelInfos.size();
        }
        for (int i = 0; i < size; i++) {
            String modelInWhiteList = this.whiteModelInfos.get(i);
            if (!(model == null || modelInWhiteList == null || model.indexOf(modelInWhiteList) != 0)) {
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
