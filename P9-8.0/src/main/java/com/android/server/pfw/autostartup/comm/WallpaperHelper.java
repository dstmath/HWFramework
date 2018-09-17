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

    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0297 A:{SYNTHETIC, Splitter: B:82:0x0297} */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0270 A:{SYNTHETIC, Splitter: B:76:0x0270} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0240 A:{SYNTHETIC, Splitter: B:68:0x0240} */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x01f0 A:{SYNTHETIC, Splitter: B:60:0x01f0} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x01a0 A:{SYNTHETIC, Splitter: B:52:0x01a0} */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0150 A:{SYNTHETIC, Splitter: B:44:0x0150} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0100 A:{SYNTHETIC, Splitter: B:36:0x0100} */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00b2 A:{SYNTHETIC, Splitter: B:28:0x00b2} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b9  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getWallpaperPkgName(int userId) {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        RuntimeException e6;
        Throwable th;
        String str = null;
        File file = new File(getWallpaperDir(userId), WALLPAPER_INFO);
        if (!file.exists()) {
            return null;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, StandardCharsets.UTF_8.name());
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        if ("wp".equals(parser.getName())) {
                            str = parser.getAttributeValue(null, "component");
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e7) {
                        HwPFWLogger.e(TAG, "failed close stream " + e7);
                    }
                }
                stream = stream2;
            } catch (FileNotFoundException e8) {
                stream = stream2;
                HwPFWLogger.w(TAG, "no current wallpaper -- first boot?");
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e72) {
                        HwPFWLogger.e(TAG, "failed close stream " + e72);
                    }
                }
                if (str == null) {
                }
            } catch (NullPointerException e9) {
                e2 = e9;
                stream = stream2;
                HwPFWLogger.w(TAG, "failed parsing " + file + " " + e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e722) {
                        HwPFWLogger.e(TAG, "failed close stream " + e722);
                    }
                }
                if (str == null) {
                }
            } catch (NumberFormatException e10) {
                e3 = e10;
                stream = stream2;
                HwPFWLogger.w(TAG, "failed parsing " + file + " " + e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e7222) {
                        HwPFWLogger.e(TAG, "failed close stream " + e7222);
                    }
                }
                if (str == null) {
                }
            } catch (XmlPullParserException e11) {
                e4 = e11;
                stream = stream2;
                HwPFWLogger.w(TAG, "failed parsing " + file + " " + e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e72222) {
                        HwPFWLogger.e(TAG, "failed close stream " + e72222);
                    }
                }
                if (str == null) {
                }
            } catch (IOException e12) {
                e72222 = e12;
                stream = stream2;
                HwPFWLogger.w(TAG, "failed parsing " + file + " " + e72222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e722222) {
                        HwPFWLogger.e(TAG, "failed close stream " + e722222);
                    }
                }
                if (str == null) {
                }
            } catch (IndexOutOfBoundsException e13) {
                e5 = e13;
                stream = stream2;
                HwPFWLogger.w(TAG, "failed parsing " + file + " " + e5);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e7222222) {
                        HwPFWLogger.e(TAG, "failed close stream " + e7222222);
                    }
                }
                if (str == null) {
                }
            } catch (RuntimeException e14) {
                e6 = e14;
                stream = stream2;
                try {
                    HwPFWLogger.w(TAG, "failed parsing " + file + " " + e6);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e72222222) {
                            HwPFWLogger.e(TAG, "failed close stream " + e72222222);
                        }
                    }
                    if (str == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e722222222) {
                            HwPFWLogger.e(TAG, "failed close stream " + e722222222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e15) {
            HwPFWLogger.w(TAG, "no current wallpaper -- first boot?");
            if (stream != null) {
            }
            if (str == null) {
            }
        } catch (NullPointerException e16) {
            e2 = e16;
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e2);
            if (stream != null) {
            }
            if (str == null) {
            }
        } catch (NumberFormatException e17) {
            e3 = e17;
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e3);
            if (stream != null) {
            }
            if (str == null) {
            }
        } catch (XmlPullParserException e18) {
            e4 = e18;
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e4);
            if (stream != null) {
            }
            if (str == null) {
            }
        } catch (IOException e19) {
            e722222222 = e19;
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e722222222);
            if (stream != null) {
            }
            if (str == null) {
            }
        } catch (IndexOutOfBoundsException e20) {
            e5 = e20;
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e5);
            if (stream != null) {
            }
            if (str == null) {
            }
        } catch (RuntimeException e21) {
            e6 = e21;
            HwPFWLogger.w(TAG, "failed parsing " + file + " " + e6);
            if (stream != null) {
            }
            if (str == null) {
            }
        }
        if (str == null) {
            return null;
        }
        int sep = str.indexOf(47);
        if (sep < 0 || sep + 1 >= str.length()) {
            return null;
        }
        return str.substring(0, sep);
    }
}
