package com.android.server;

import android.app.INonHardwareAcceleratedPackagesManager.Stub;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

final class NonHardwareAcceleratedPackagesManagerService extends SystemService {
    private static final int DATA_VERSION_INIT = 1;
    protected static boolean HWDBG = false;
    protected static final boolean HWLOGW_E = true;
    private static final int MSG_HINT = 2;
    private static final int MSG_INIT = 1;
    private static final int MSG_SAVE = 3;
    private static final String TAG = "NonHardAccelPkgs";
    private static final int WRITE_DELAY = 60000;
    private final AtomicFile mFile;
    private MyHandler mHandler;
    private boolean mInited;
    private final HashMap<String, Boolean> mPackages;
    private final IBinder mService;
    private int mVersion;

    private final class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NonHardwareAcceleratedPackagesManagerService.MSG_INIT /*1*/:
                    NonHardwareAcceleratedPackagesManagerService.this.initPackages(false);
                    NonHardwareAcceleratedPackagesManagerService.this.initPackages(NonHardwareAcceleratedPackagesManagerService.HWLOGW_E);
                    NonHardwareAcceleratedPackagesManagerService.this.mInited = NonHardwareAcceleratedPackagesManagerService.HWLOGW_E;
                case NonHardwareAcceleratedPackagesManagerService.MSG_HINT /*2*/:
                    NonHardwareAcceleratedPackagesManagerService.this.promptHint((String) msg.obj);
                case NonHardwareAcceleratedPackagesManagerService.MSG_SAVE /*3*/:
                    NonHardwareAcceleratedPackagesManagerService.this.savePackages();
                default:
            }
        }
    }

    private final class MyThread extends Thread {
        public MyThread() {
            super(NonHardwareAcceleratedPackagesManagerService.TAG);
        }

        public void run() {
            Looper.prepare();
            NonHardwareAcceleratedPackagesManagerService.this.mHandler = new MyHandler(null);
            Looper.loop();
        }
    }

    static {
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, MSG_SAVE) : false : HWLOGW_E;
        HWDBG = isLoggable;
    }

    public NonHardwareAcceleratedPackagesManagerService(Context context) {
        super(context);
        this.mInited = false;
        this.mHandler = null;
        this.mPackages = new HashMap();
        this.mVersion = MSG_INIT;
        this.mService = new Stub() {
            public void setForceEnabled(String pkgName, boolean force) {
                if (UserHandle.isApp(Binder.getCallingUid())) {
                    Slog.w(NonHardwareAcceleratedPackagesManagerService.TAG, "setForceEnabled: Permission Denial: calling from an user app is prohibited.");
                } else if (pkgName == null) {
                    Slog.w(NonHardwareAcceleratedPackagesManagerService.TAG, "Illegal null package name.");
                } else {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        if (NonHardwareAcceleratedPackagesManagerService.this.mInited) {
                            synchronized (NonHardwareAcceleratedPackagesManagerService.this.mPackages) {
                                if (((Boolean) NonHardwareAcceleratedPackagesManagerService.this.mPackages.put(pkgName, Boolean.valueOf(force))) == null) {
                                    NonHardwareAcceleratedPackagesManagerService.this.mHandler.sendMessage(NonHardwareAcceleratedPackagesManagerService.this.mHandler.obtainMessage(NonHardwareAcceleratedPackagesManagerService.MSG_HINT, pkgName));
                                }
                                NonHardwareAcceleratedPackagesManagerService.this.scheduleSave();
                                if (NonHardwareAcceleratedPackagesManagerService.HWDBG) {
                                    Slog.d(NonHardwareAcceleratedPackagesManagerService.TAG, "setForceEnabled: " + pkgName + " " + force);
                                }
                            }
                        }
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }

            public boolean getForceEnabled(String pkgName) {
                boolean z = false;
                if (UserHandle.isApp(Binder.getCallingUid())) {
                    Slog.w(NonHardwareAcceleratedPackagesManagerService.TAG, "getForceEnabled: Permission Denial: calling from an user app is prohibited.");
                    return false;
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    if (NonHardwareAcceleratedPackagesManagerService.this.mInited) {
                        synchronized (NonHardwareAcceleratedPackagesManagerService.this.mPackages) {
                            Boolean forced = (Boolean) NonHardwareAcceleratedPackagesManagerService.this.mPackages.get(pkgName);
                            if (NonHardwareAcceleratedPackagesManagerService.HWDBG) {
                                Slog.d(NonHardwareAcceleratedPackagesManagerService.TAG, "getForceEnabled: " + pkgName + " " + forced);
                            }
                            if (forced != null) {
                                z = forced.booleanValue();
                            }
                        }
                        return z;
                    }
                    Binder.restoreCallingIdentity(ident);
                    return false;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }

            public boolean hasPackage(String pkgName) {
                if (UserHandle.isApp(Binder.getCallingUid())) {
                    Slog.w(NonHardwareAcceleratedPackagesManagerService.TAG, "hasPackage: Permission Denial: calling from an user app is prohibited.");
                    return false;
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    if (NonHardwareAcceleratedPackagesManagerService.this.mInited) {
                        boolean existed;
                        synchronized (NonHardwareAcceleratedPackagesManagerService.this.mPackages) {
                            existed = ((Boolean) NonHardwareAcceleratedPackagesManagerService.this.mPackages.get(pkgName)) != null ? NonHardwareAcceleratedPackagesManagerService.HWLOGW_E : false;
                            if (NonHardwareAcceleratedPackagesManagerService.HWDBG) {
                                Slog.d(NonHardwareAcceleratedPackagesManagerService.TAG, "hasPackage: " + pkgName + " " + existed);
                            }
                        }
                        return existed;
                    }
                    Binder.restoreCallingIdentity(ident);
                    return false;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }

            public void removePackage(String pkgName) {
                if (UserHandle.isApp(Binder.getCallingUid())) {
                    Slog.w(NonHardwareAcceleratedPackagesManagerService.TAG, "removePackage: Permission Denial: calling from an user app is prohibited.");
                    return;
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    if (NonHardwareAcceleratedPackagesManagerService.this.mInited) {
                        synchronized (NonHardwareAcceleratedPackagesManagerService.this.mPackages) {
                            NonHardwareAcceleratedPackagesManagerService.this.mPackages.remove(pkgName);
                            NonHardwareAcceleratedPackagesManagerService.this.scheduleSave();
                            if (NonHardwareAcceleratedPackagesManagerService.HWDBG) {
                                Slog.d(NonHardwareAcceleratedPackagesManagerService.TAG, "removePackage: " + pkgName);
                            }
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }

            protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (NonHardwareAcceleratedPackagesManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                    pw.println("Permission Denial: can't dump nonhardaccelpkgs service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                } else {
                    NonHardwareAcceleratedPackagesManagerService.this.dumpImpl(pw);
                }
            }
        };
        File systemDir = new File(Environment.getDataDirectory(), "system");
        systemDir.mkdirs();
        this.mFile = new AtomicFile(new File(systemDir, "packages-nonhardaccel.xml"));
        new MyThread().start();
    }

    public void onStart() {
        publishBinderService("nonhardaccelpkgs", this.mService);
    }

    public void onBootPhase(int phase) {
        if (phase == 500 && this.mHandler != null) {
            this.mHandler.sendEmptyMessage(MSG_INIT);
        }
    }

    void dumpImpl(PrintWriter pw) {
        synchronized (this.mPackages) {
            HashMap<String, Boolean> pkgs = new HashMap(this.mPackages);
        }
        pw.println("FORCE\tPACKAGE");
        for (Entry<String, Boolean> entry : pkgs.entrySet()) {
            pw.println(entry.getValue() + "\t" + ((String) entry.getKey()));
        }
    }

    private void scheduleSave() {
        this.mHandler.removeMessages(MSG_SAVE);
        this.mHandler.sendEmptyMessageDelayed(MSG_SAVE, 60000);
    }

    private void initPackages(boolean bw) {
        FileInputStream fileInputStream = null;
        XmlPullParser xmlPullParser = null;
        if (bw) {
            xmlPullParser = getContext().getResources().getXml(34275330);
        } else {
            try {
                fileInputStream = this.mFile.openRead();
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(fileInputStream, null);
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Error reading nonhardaccel-packages", e);
                if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                    ((XmlResourceParser) xmlPullParser).close();
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (IOException e3) {
                if (fileInputStream != null) {
                    Slog.w(TAG, "Error reading hardaccel-packages", e3);
                }
                if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                    ((XmlResourceParser) xmlPullParser).close();
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th) {
                if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                    ((XmlResourceParser) xmlPullParser).close();
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e5) {
                    }
                }
            }
        }
        XmlUtils.beginDocument(xmlPullParser, "nonhardaccel-packages");
        String version = xmlPullParser.getAttributeValue(null, "version");
        if (HWDBG) {
            Slog.d(TAG, "initPackages: bw " + bw + ", version " + version);
        }
        if (version != null) {
            try {
                int versionInt = Integer.parseInt(version);
                if (bw) {
                    int i = this.mVersion;
                    if (r0 < versionInt) {
                        if (HWDBG) {
                            Slog.d(TAG, "initPackages: upgrade with bw list");
                        }
                        this.mVersion = versionInt;
                        scheduleSave();
                    } else {
                        if (HWDBG) {
                            Slog.d(TAG, "initPackages: ignore bw list");
                        }
                        if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                            ((XmlResourceParser) xmlPullParser).close();
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e6) {
                            }
                        }
                        return;
                    }
                }
                this.mVersion = versionInt;
            } catch (NumberFormatException e7) {
            }
        }
        while (true) {
            XmlUtils.nextElement(xmlPullParser);
            String element = xmlPullParser.getName();
            if (element == null) {
                break;
            }
            if (element.equals("pkg")) {
                String pkg = xmlPullParser.getAttributeValue(null, "name");
                if (pkg != null) {
                    String state = xmlPullParser.getAttributeValue(null, "state");
                    boolean stateBool = false;
                    if (state != null) {
                        stateBool = Boolean.parseBoolean(state);
                    }
                    Boolean prevState = (Boolean) this.mPackages.put(pkg, Boolean.valueOf(stateBool));
                    if (HWDBG) {
                        Slog.d(TAG, "initPackages: pkg " + pkg + ", " + prevState + " => " + stateBool);
                    }
                }
            }
        }
        if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
            ((XmlResourceParser) xmlPullParser).close();
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e8) {
            }
        }
    }

    private void promptHint(String pkgName) {
    }

    private void savePackages() {
        synchronized (this.mPackages) {
            HashMap<String, Boolean> pkgs = new HashMap(this.mPackages);
        }
        FileOutputStream fileOutputStream = null;
        if (HWDBG) {
            Slog.d(TAG, "savePackages: ");
        }
        try {
            fileOutputStream = this.mFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, "utf-8");
            out.startDocument(null, Boolean.valueOf(HWLOGW_E));
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", HWLOGW_E);
            out.startTag(null, "nonhardaccel-packages");
            out.attribute(null, "version", Integer.toString(this.mVersion));
            for (Entry<String, Boolean> entry : pkgs.entrySet()) {
                String pkg = (String) entry.getKey();
                Boolean state = (Boolean) entry.getValue();
                if (pkg == null) {
                    Slog.w(TAG, "hardaccel packages contains null name package, continue.");
                } else {
                    out.startTag(null, "pkg");
                    out.attribute(null, "name", pkg);
                    out.attribute(null, "state", Boolean.toString(state.booleanValue()));
                    out.endTag(null, "pkg");
                }
            }
            out.endTag(null, "nonhardaccel-packages");
            out.endDocument();
            this.mFile.finishWrite(fileOutputStream);
        } catch (IOException e1) {
            Slog.w(TAG, "Error writing non-hardware-accelerated packages", e1);
            if (fileOutputStream != null) {
                this.mFile.failWrite(fileOutputStream);
            }
        }
    }
}
