package com.android.server.pfw.autostartup.xmlparser;

import android.content.Context;
import com.android.server.pfw.autostartup.comm.DefaultXmlParsedResult;
import com.android.server.pfw.log.HwPFWLogger;
import com.android.server.pfw.xml.HwPFWXmlException;
import com.android.server.pfw.xml.HwPFWXmlParsers;

public final class StartupParsers {
    private static final String ASSET_FILE_PATH = "pfw/auto_startup_default_configuration.xml";
    private static final String DISK_FILE_PATH = "/system/etc/pfw/auto_startup_default_configuration.xml";
    private static final String TAG = "StartupParsers";

    public static DefaultXmlParsedResult parseAssetFile(Context ctx) {
        try {
            return new AssetFileRootElementParser().parseDOMElement(HwPFWXmlParsers.assetXmlRootElement(ctx, ASSET_FILE_PATH));
        } catch (HwPFWXmlException ex) {
            HwPFWLogger.e(TAG, "parseAssetFile catch exception:" + ex.getMessage());
            return null;
        }
    }

    public static DefaultXmlParsedResult parseCustFile(Context ctx) {
        try {
            return new CustFileRootElementParser().parseDOMElement(HwPFWXmlParsers.diskXmlRootElement(DISK_FILE_PATH));
        } catch (HwPFWXmlException ex) {
            HwPFWLogger.e(TAG, "parseCustFile catch exception:" + ex.getMessage());
            return null;
        }
    }
}
