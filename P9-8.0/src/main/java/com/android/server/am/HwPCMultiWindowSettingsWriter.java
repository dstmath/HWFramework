package com.android.server.am;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AtomicFile;
import android.util.Pair;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.am.HwPCMultiWindowManager.Entry;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.pc.whiltestrategy.WhiteListAppStrategyManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class HwPCMultiWindowSettingsWriter {
    private static final int MSG_WRITE = 1;
    private static final int mCurrentVersion = 11;
    private final Handler mHandler;
    private int mLastVersion = -1;
    private HwPCMultiWindowManager mSettings;
    private final AtomicFile mSettingsFile;

    private final class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (HwPCMultiWindowSettingsWriter.this.mSettings.mService) {
                        HwPCMultiWindowSettingsWriter.this.writeSettingsLocked();
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public HwPCMultiWindowSettingsWriter(HwPCMultiWindowManager settings) {
        this.mSettings = settings;
        this.mHandler = new WorkerHandler(this.mSettings.mService.mHandler.getLooper());
        this.mSettingsFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "app_window_settings.xml"));
        loadSettings();
        if (this.mLastVersion < 11) {
            this.mSettings.mEntries.clear();
        }
        loadSpecialPackages();
    }

    public void scheduleWrite() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    private void loadSettings() {
        try {
            FileInputStream stream = this.mSettingsFile.openRead();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, "utf-8");
                Entry entry = null;
                String entryKey = null;
                String deviceKey = null;
                for (int event = parser.getEventType(); event != 1; event = parser.next()) {
                    switch (event) {
                        case 2:
                            if (!"app_window_settings".equals(parser.getName())) {
                                if (!HwGpsPowerTracker.DEL_PKG.equals(parser.getName())) {
                                    if ("state_bounds".equals(parser.getName()) && entry != null) {
                                        entry.windowState = getIntAttribute(parser, "windowState");
                                        entry.originalWindowState = getIntAttribute(parser, "originalWindowState");
                                        entry.windowBounds.left = getIntAttribute(parser, "left");
                                        entry.windowBounds.top = getIntAttribute(parser, "top");
                                        entry.windowBounds.right = getIntAttribute(parser, "right");
                                        entry.windowBounds.bottom = getIntAttribute(parser, "bottom");
                                        break;
                                    }
                                }
                                String name = parser.getAttributeValue(null, "name");
                                if (name == null) {
                                    break;
                                }
                                entryKey = name;
                                entry = new Entry(name);
                                break;
                            }
                            this.mLastVersion = getIntAttribute(parser, "version");
                            deviceKey = parser.getAttributeValue(null, "device");
                            break;
                        case 3:
                            if (!HwGpsPowerTracker.DEL_PKG.equals(parser.getName())) {
                                break;
                            }
                            this.mSettings.putEntry(deviceKey, entryKey, entry);
                            entryKey = null;
                            entry = null;
                            break;
                        default:
                            break;
                    }
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e2) {
                Slog.w("HwPCMultiWindowSettingsWriter", "on loadSettings", e2);
            }
        } catch (FileNotFoundException e3) {
        }
    }

    private int getIntAttribute(XmlPullParser parser, String name) {
        int i = 0;
        try {
            String str = parser.getAttributeValue(null, name);
            if (str != null) {
                i = Integer.parseInt(str);
            }
            return i;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void writeSettingsLocked() {
        try {
            FileOutputStream stream = this.mSettingsFile.startWrite();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                for (String deviceKey : this.mSettings.mEntries.keySet()) {
                    out.startTag(null, "app_window_settings");
                    out.attribute(null, "version", Integer.toString(11));
                    out.attribute(null, "device", deviceKey);
                    out.startTag(null, "system_app_window_mode");
                    out.endTag(null, "system_app_window_mode");
                    for (Entry entry : ((HashMap) this.mSettings.mEntries.get(deviceKey)).values()) {
                        if (!(entry == null || entry.windowBounds == null)) {
                            out.startTag(null, HwGpsPowerTracker.DEL_PKG);
                            out.attribute(null, "name", entry.pkgName);
                            out.startTag(null, "state_bounds");
                            out.attribute(null, "windowState", Integer.toString(entry.windowState));
                            out.attribute(null, "originalWindowState", Integer.toString(entry.originalWindowState));
                            out.attribute(null, "left", Integer.toString(entry.windowBounds.left));
                            out.attribute(null, "top", Integer.toString(entry.windowBounds.top));
                            out.attribute(null, "right", Integer.toString(entry.windowBounds.right));
                            out.attribute(null, "bottom", Integer.toString(entry.windowBounds.bottom));
                            out.endTag(null, "orientation");
                            out.endTag(null, HwGpsPowerTracker.DEL_PKG);
                        }
                    }
                    out.endTag(null, "app_window_settings");
                }
                out.endDocument();
                this.mSettingsFile.finishWrite(stream);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e2) {
                this.mSettingsFile.failWrite(stream);
                Slog.w("HwPCMultiWindowSettingsWriter", "on writeSettingsLocked", e2);
            }
        } catch (IOException e3) {
        }
    }

    private void loadSpecialPackages() {
        this.mSettings.mSpecialVideosList.clear();
        this.mSettings.mNeedDelayList.clear();
        this.mSettings.mMaximizedOnlyList.clear();
        this.mSettings.mPortraitPkgList.clear();
        this.mSettings.mPortraitMaximizedPkgList.clear();
        this.mSettings.mPadFullscreenList.clear();
        this.mSettings.mFullscreenNoNavigationBar.clear();
        List<Pair<String, Integer>> specialPkgList = WhiteListAppStrategyManager.getInstance(this.mSettings.mService.mContext).getSpecailWindowPolicyAppList();
        if (specialPkgList != null) {
            for (Pair<String, Integer> specialPkg : specialPkgList) {
                if (specialPkg != null) {
                    switch (((Integer) specialPkg.second).intValue()) {
                        case 1:
                            this.mSettings.mSpecialVideosList.add((String) specialPkg.first);
                            break;
                        case 2:
                            this.mSettings.mNeedDelayList.add((String) specialPkg.first);
                            break;
                        case 3:
                            this.mSettings.mMaximizedOnlyList.add((String) specialPkg.first);
                            break;
                        case 4:
                            this.mSettings.mPortraitPkgList.add((String) specialPkg.first);
                            break;
                        case 5:
                            this.mSettings.mPortraitMaximizedPkgList.add((String) specialPkg.first);
                            break;
                        case 6:
                            this.mSettings.mPadFullscreenList.add((String) specialPkg.first);
                            break;
                        case 7:
                            this.mSettings.mFullscreenNoNavigationBar.add((String) specialPkg.first);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
}
