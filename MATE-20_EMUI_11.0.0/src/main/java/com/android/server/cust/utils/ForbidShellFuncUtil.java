package com.android.server.cust.utils;

import android.hdm.HwDeviceManager;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ForbidShellFuncUtil {
    private static final String FORBID_SHELL_FUNC_XML = "xml/hw_forbid_app_func_blacklist.xml";
    private static final String FORBID_SHELL_FUNC_XML_ATTRIBUTE = "packageName";
    private static final String FORBID_SHELL_FUNC_XML_NODE_NAME = "forbid_app";
    private static final String TAG = "Utils";
    private static HashSet<String> sForbidShellFuncSet = new HashSet<>();
    private static volatile boolean sNeedInitForbidShellFuncSet = true;

    public static boolean isNeedForbidShellFunc(String packageName) {
        if (sNeedInitForbidShellFuncSet) {
            initForbidShellFuncSet();
        }
        if (!HwDeviceManager.disallowOp(3, packageName)) {
            return sForbidShellFuncSet.contains(packageName);
        }
        Slog.i(TAG, packageName + " is Persistent app, need to forbid shell func.");
        return true;
    }

    private static void initForbidShellFuncSet() {
        synchronized (sForbidShellFuncSet) {
            if (sNeedInitForbidShellFuncSet) {
                ArrayList<File> forbidShellFuncFileList = new ArrayList<>();
                try {
                    forbidShellFuncFileList = HwCfgFilePolicy.getCfgFileList(FORBID_SHELL_FUNC_XML, 0);
                } catch (NoClassDefFoundError e) {
                    Slog.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
                }
                if (forbidShellFuncFileList == null || forbidShellFuncFileList.size() == 0) {
                    Slog.i(TAG, "initHwForbidAppFuncBlacklist forbidShellFuncFileList is empty");
                    sNeedInitForbidShellFuncSet = false;
                    return;
                }
                Iterator<File> it = forbidShellFuncFileList.iterator();
                while (it.hasNext()) {
                    sForbidShellFuncSet.addAll(readXmlToSet(it.next()));
                }
                sNeedInitForbidShellFuncSet = false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0051, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0054, code lost:
        throw r4;
     */
    private static HashSet<String> readXmlToSet(File xmlFile) {
        HashSet<String> forbidFileSet = new HashSet<>();
        try {
            InputStream inputStream = new FileInputStream(xmlFile);
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream, "utf-8");
            int xmlEventType = xmlParser.next();
            while (xmlEventType != 1) {
                xmlEventType = xmlParser.next();
                if (xmlEventType == 2) {
                    if (FORBID_SHELL_FUNC_XML_NODE_NAME.equals(xmlParser.getName())) {
                        String xmlPkgName = xmlParser.getAttributeValue(null, "packageName");
                        if (!TextUtils.isEmpty(xmlPkgName)) {
                            forbidFileSet.add(xmlPkgName);
                        }
                    }
                }
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "readXmlToSet open file failed");
        } catch (XmlPullParserException e2) {
            Slog.e(TAG, "readXmlToSet XmlPullParserException");
        } catch (IOException e3) {
            Slog.e(TAG, "readXmlToSet IOException");
        } catch (Exception e4) {
            Slog.e(TAG, "readXmlToSet Exception");
        }
        return forbidFileSet;
    }
}
