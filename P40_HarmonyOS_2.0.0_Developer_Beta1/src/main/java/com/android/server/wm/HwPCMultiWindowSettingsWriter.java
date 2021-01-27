package com.android.server.wm;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AtomicFile;
import android.util.Pair;
import android.util.Xml;
import com.android.server.wm.HwPCMultiWindowManager;
import com.huawei.android.util.SlogEx;
import com.huawei.internal.util.FastXmlSerializerEx;
import com.huawei.server.pc.whiltestrategy.WhiteListAppStrategyManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwPCMultiWindowSettingsWriter {
    private static final int CURRENT_VERSION = 13;
    private static final long DELAY_MILLIS = 5000;
    private static final int MSG_WRITE = 1;
    private static final String TAG = "HwPCMultiWindowSettingsWriter";
    private final Handler mHandler;
    private int mLastVersion = -1;
    private final HwPCMultiWindowManager mSettings;
    private final AtomicFile mSettingsFile;

    public HwPCMultiWindowSettingsWriter(HwPCMultiWindowManager settings) {
        this.mSettings = settings;
        this.mHandler = new WorkerHandler(this.mSettings.mService.getLooper());
        this.mSettingsFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "app_window_settings.xml"));
        loadSettings();
        if (this.mLastVersion < CURRENT_VERSION) {
            this.mSettings.mEntriesToWrite.clear();
        }
        loadSpecialPackages();
    }

    public void scheduleWrite() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, DELAY_MILLIS);
    }

    private void loadSettings() {
        FileInputStream stream = null;
        try {
            stream = this.mSettingsFile.openRead();
            loadSettings(stream);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    SlogEx.e(TAG, "close stream failed");
                }
            }
        } catch (FileNotFoundException e2) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    SlogEx.e(TAG, "close stream failed");
                }
            }
        } catch (RuntimeException e4) {
            throw e4;
        } catch (Exception e5) {
            SlogEx.w(TAG, "on loadSettings");
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e6) {
                    SlogEx.e(TAG, "close stream failed");
                }
            }
            throw th;
        }
    }

    private void loadSettings(FileInputStream stream) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, "utf-8");
        HwPCMultiWindowManager.Entry entry = null;
        String entryKey = null;
        String deviceKey = null;
        for (int event = parser.getEventType(); event != 1; event = parser.next()) {
            if (event != 0) {
                if (event != 2) {
                    if (event == 3 && "pkg".equals(parser.getName())) {
                        this.mSettings.putEntry(deviceKey, entryKey, entry);
                        entryKey = null;
                        entry = null;
                    }
                } else if ("app_window_settings".equals(parser.getName())) {
                    this.mLastVersion = getIntAttribute(parser, "version");
                    deviceKey = parser.getAttributeValue(null, "device");
                } else if ("pkg".equals(parser.getName())) {
                    String name = parser.getAttributeValue(null, "name");
                    if (name != null) {
                        entryKey = name;
                        entry = new HwPCMultiWindowManager.Entry(name);
                    }
                } else if (!"state_bounds".equals(parser.getName())) {
                    SlogEx.w(TAG, "loadSettings do nothing");
                } else if (entry != null) {
                    entry.windowState = getIntAttribute(parser, "windowState");
                    entry.originalWindowState = getIntAttribute(parser, "originalWindowState");
                    entry.windowBounds.left = getIntAttribute(parser, "left");
                    entry.windowBounds.top = getIntAttribute(parser, "top");
                    entry.windowBounds.right = getIntAttribute(parser, "right");
                    entry.windowBounds.bottom = getIntAttribute(parser, "bottom");
                }
            }
        }
    }

    private int getIntAttribute(XmlPullParser parser, String name) {
        try {
            String str = parser.getAttributeValue(null, name);
            if (str != null) {
                return Integer.parseInt(str);
            }
            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeSettingsLocked() {
        try {
            FileOutputStream stream = this.mSettingsFile.startWrite();
            try {
                XmlSerializer out = FastXmlSerializerEx.getFastXmlSerializer();
                out.setOutput(stream, "utf-8");
                out.startDocument(null, true);
                for (String deviceKey : this.mSettings.mEntries.keySet()) {
                    out.startTag(null, "app_window_settings");
                    out.attribute(null, "version", Integer.toString(CURRENT_VERSION));
                    out.attribute(null, "device", deviceKey);
                    out.startTag(null, "system_app_window_mode");
                    out.endTag(null, "system_app_window_mode");
                    if (this.mSettings.mEntriesToWrite != null) {
                        if (this.mSettings.mEntriesToWrite.get(deviceKey) != null) {
                            for (HwPCMultiWindowManager.Entry entry : this.mSettings.mEntriesToWrite.get(deviceKey).values()) {
                                if (entry != null) {
                                    if (entry.windowBounds != null) {
                                        out.startTag(null, "pkg");
                                        out.attribute(null, "name", entry.pkgName);
                                        out.startTag(null, "state_bounds");
                                        out.attribute(null, "windowState", Integer.toString(entry.windowState));
                                        out.attribute(null, "originalWindowState", Integer.toString(entry.originalWindowState));
                                        out.attribute(null, "left", Integer.toString(entry.windowBounds.left));
                                        out.attribute(null, "top", Integer.toString(entry.windowBounds.top));
                                        out.attribute(null, "right", Integer.toString(entry.windowBounds.right));
                                        out.attribute(null, "bottom", Integer.toString(entry.windowBounds.bottom));
                                        out.endTag(null, "orientation");
                                        out.endTag(null, "pkg");
                                    }
                                }
                            }
                            out.endTag(null, "app_window_settings");
                        }
                    }
                }
                out.endDocument();
                this.mSettingsFile.finishWrite(stream);
            } catch (RuntimeException e) {
                this.mSettingsFile.failWrite(stream);
                SlogEx.e(TAG, "on RuntimeException");
            } catch (Exception e2) {
                this.mSettingsFile.failWrite(stream);
                SlogEx.e(TAG, "on writeSettingsLocked");
            }
        } catch (IOException e3) {
        }
    }

    private final class WorkerHandler extends Handler {
        WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                synchronized (HwPCMultiWindowSettingsWriter.this.mSettings.mService) {
                    HwPCMultiWindowSettingsWriter.this.writeSettingsLocked();
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v4, resolved type: java.util.List<java.lang.String> */
    /* JADX DEBUG: Multi-variable search result rejected for r3v6, resolved type: java.util.List<java.lang.String> */
    /* JADX DEBUG: Multi-variable search result rejected for r3v8, resolved type: java.util.List<java.lang.String> */
    /* JADX DEBUG: Multi-variable search result rejected for r3v10, resolved type: java.util.List<java.lang.String> */
    /* JADX DEBUG: Multi-variable search result rejected for r3v12, resolved type: java.util.List<java.lang.String> */
    /* JADX DEBUG: Multi-variable search result rejected for r3v14, resolved type: java.util.List<java.lang.String> */
    /* JADX DEBUG: Multi-variable search result rejected for r3v16, resolved type: java.util.List<java.lang.String> */
    /* JADX WARN: Multi-variable type inference failed */
    private void loadSpecialPackages() {
        this.mSettings.mSpecialVideosList.clear();
        this.mSettings.mNeedDelayList.clear();
        this.mSettings.mMaximizedOnlyList.clear();
        this.mSettings.mPortraitPkgList.clear();
        this.mSettings.mPortraitMaximizedPkgList.clear();
        this.mSettings.mPadFullscreenList.clear();
        this.mSettings.mFullscreenNoNavigationBar.clear();
        List<Pair<String, Integer>> specialPkgList = WhiteListAppStrategyManager.getInstance(this.mSettings.mService.getContext()).getSpecailWindowPolicyAppList();
        if (specialPkgList != null) {
            for (Pair<String, Integer> specialPkg : specialPkgList) {
                if (specialPkg != null) {
                    switch (((Integer) specialPkg.second).intValue()) {
                        case 1:
                            this.mSettings.mSpecialVideosList.add(specialPkg.first);
                            continue;
                        case 2:
                            this.mSettings.mNeedDelayList.add(specialPkg.first);
                            continue;
                        case 3:
                            this.mSettings.mMaximizedOnlyList.add(specialPkg.first);
                            continue;
                        case 4:
                            this.mSettings.mPortraitPkgList.add(specialPkg.first);
                            continue;
                        case 5:
                            this.mSettings.mPortraitMaximizedPkgList.add(specialPkg.first);
                            continue;
                        case 6:
                            this.mSettings.mPadFullscreenList.add(specialPkg.first);
                            continue;
                        case 7:
                            this.mSettings.mFullscreenNoNavigationBar.add(specialPkg.first);
                            continue;
                    }
                }
            }
        }
    }
}
