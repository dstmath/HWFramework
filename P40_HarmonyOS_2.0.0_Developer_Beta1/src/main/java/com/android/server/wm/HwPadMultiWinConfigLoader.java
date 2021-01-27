package com.android.server.wm;

import android.content.Context;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.huawei.hiai.awareness.client.AwarenessRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwPadMultiWinConfigLoader {
    private static final Object M_LOCK = new Object();
    private static final String TAG = HwPadMultiWinConfigLoader.class.getSimpleName();
    private static volatile HwPadMultiWinConfigLoader instance = null;
    private Set<String> mBlackPackageNameList = new HashSet();
    private Context mContext = null;

    private HwPadMultiWinConfigLoader(Context context) {
        this.mContext = context;
    }

    public static HwPadMultiWinConfigLoader getInstance(Context context) {
        if (instance == null) {
            synchronized (M_LOCK) {
                if (instance == null) {
                    instance = new HwPadMultiWinConfigLoader(context);
                }
            }
        }
        return instance;
    }

    public Set<String> getBlackPackageNameList() {
        Set<String> set;
        synchronized (M_LOCK) {
            set = this.mBlackPackageNameList;
        }
        return set;
    }

    public void loadBlackPackageNameList(String filePath) {
        String str;
        String str2;
        Slog.i(TAG, "loadBlackPackageNameList start");
        synchronized (M_LOCK) {
            this.mBlackPackageNameList = new HashSet();
            InputStream inputStream = null;
            if (filePath == null) {
                try {
                    Slog.w(TAG, "load padcast blacklist fail, filePath is null. ");
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Slog.e(TAG, "load padcast blacklist: IO Exception while closing stream.");
                        }
                    }
                    return;
                } catch (FileNotFoundException e2) {
                    Slog.e(TAG, "load padcast blacklist fail, FileNotFound ");
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e3) {
                            str2 = TAG;
                            str = "load padcast blacklist: IO Exception while closing stream.";
                        }
                    }
                } catch (XmlPullParserException e4) {
                    Slog.e(TAG, "load padcast blacklist fail, ParserException ");
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e5) {
                            str2 = TAG;
                            str = "load padcast blacklist: IO Exception while closing stream.";
                        }
                    }
                } catch (IOException e6) {
                    Slog.e(TAG, "load padcast blacklist fail, IOException ");
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e7) {
                            str2 = TAG;
                            str = "load padcast blacklist: IO Exception while closing stream.";
                        }
                    }
                } catch (Throwable e8) {
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e9) {
                            Slog.e(TAG, "load padcast blacklist: IO Exception while closing stream.");
                        }
                    }
                    throw e8;
                }
            } else {
                File blackListFile = new File(filePath);
                if (!blackListFile.exists()) {
                    Slog.w(TAG, "load padcast blacklist fail, blackListFile is not exist.");
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (IOException e10) {
                            Slog.e(TAG, "load padcast blacklist: IO Exception while closing stream.");
                        }
                    }
                    return;
                }
                InputStream inputStream2 = new FileInputStream(blackListFile);
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream2, null);
                for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                    if (xmlEventType == 2 && AwarenessRequest.Field.PACKAGE_NAME.equals(xmlParser.getName())) {
                        String packageName = xmlParser.nextText();
                        if (!TextUtils.isEmpty(packageName)) {
                            this.mBlackPackageNameList.add(packageName);
                        }
                    }
                }
                try {
                    inputStream2.close();
                } catch (IOException e11) {
                    str2 = TAG;
                    str = "load padcast blacklist: IO Exception while closing stream.";
                }
            }
        }
        Slog.e(str2, str);
    }
}
