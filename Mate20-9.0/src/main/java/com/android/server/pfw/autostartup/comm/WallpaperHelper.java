package com.android.server.pfw.autostartup.comm;

import android.os.Environment;
import android.util.Xml;
import com.android.server.pfw.log.HwPFWLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WallpaperHelper {
    private static final String TAG = "WallpaperHelper";
    private static final String WALLPAPER_INFO = "wallpaper_info.xml";

    private static File getWallpaperDir(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:84:0x019f A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01a0  */
    public static String getWallpaperPkgName(int userId) {
        String str;
        StringBuilder sb;
        int type;
        String componentName = null;
        File file = new File(getWallpaperDir(userId), WALLPAPER_INFO);
        if (!file.exists()) {
            return null;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, StandardCharsets.UTF_8.name());
            do {
                type = parser.next();
                if (type == 2 && "wp".equals(parser.getName())) {
                    componentName = parser.getAttributeValue(null, "component");
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e = e;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (FileNotFoundException e2) {
            HwPFWLogger.w(TAG, "no current wallpaper -- first boot?");
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (NullPointerException e4) {
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e4);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e6) {
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e6);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e8) {
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e8);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e9) {
                    e = e9;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e10) {
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e10);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    e = e11;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e12) {
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e12);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e13) {
                    e = e13;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (RuntimeException e14) {
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e14);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e15) {
                    e = e15;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e16) {
                    HwPFWLogger.e(TAG, "failed close stream " + e16);
                }
            }
            throw th;
        }
        if (componentName != null) {
            return null;
        }
        int sep = componentName.indexOf(47);
        if (sep < 0 || sep + 1 >= componentName.length()) {
            return null;
        }
        return componentName.substring(0, sep);
        sb.append("failed close stream ");
        sb.append(e);
        HwPFWLogger.e(str, sb.toString());
        if (componentName != null) {
        }
    }
}
