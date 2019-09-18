package com.android.server;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import com.android.server.pm.auth.HwCertification;
import com.android.server.wifipro.WifiProCHRManager;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class BluetoothParaManager {
    private static final int BT_PARA_FILE_MAX_SIZE = 10485760;
    private static final String BT_PARA_FILE_WARNING = "BT_PARA_FILE_WARNING";
    private static final String BT_PARA_UPDATE_ACTION = "com.huawei.android.bluetooth.ACTION_BT_PARA_UPDATE";
    private static final String BT_PARA_UPDATE_PERMISSION = "com.huawei.android.bluetooth.permission.BT_PARA_UPDATE";
    private static final String EMCOM_PARA_READY_ACTION = "huawei.intent.action.ACTION_EMCOM_PARA_READY";
    private static final String EXTRA_EMCOM_PARA_READY_REC = "EXTRA_EMCOM_PARA_READY_REC";
    private static final int MASKBIT_PARATYPE_NONCELL_BT = 4;
    private static final int PARATYPE_NONCELL_BT = 16;
    private static final int PARA_PATHTYPE_COTA = 1;
    private static final int PARA_UPGRADE_FILE_NOTEXIST = 0;
    private static final int PARA_UPGRADE_RESPONSE_FILE_ERROR = 6;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_ALREADY = 4;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_FAILURE = 9;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_PENDING = 7;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_SUCCESS = 8;
    private static final int PARA_UPGRADE_RESPONSE_VERSION_MISMATCH = 5;
    private static final String RECEIVE_EMCOM_PARA_UPGRADE_PERMISSION = "huawei.permission.RECEIVE_EMCOM_PARA_UPGRADE";
    private static final String TAG = "BluetoothParaManager";
    /* access modifiers changed from: private */
    public static String mCotaFilePath = "";
    private static final String mCotaParaCfgDir = "emcom/noncell";
    /* access modifiers changed from: private */
    public static String mSysFilePath = "";
    private BluetoothAdapter mAdapter = null;
    private final ContentResolver mContentResolver;
    private Context mContext;
    /* access modifiers changed from: private */
    public int mCotaConfigVersion = 0;
    /* access modifiers changed from: private */
    public File mCotaFile;
    private String mCpSavedEmuiVersion = "";
    /* access modifiers changed from: private */
    public int mSavedConfigVersion = 0;
    /* access modifiers changed from: private */
    public boolean mSendBroadcastToApk = false;
    /* access modifiers changed from: private */
    public int mSysConfigVersion = 0;
    /* access modifiers changed from: private */
    public File mSysFile;
    private String mSystemEmuiVersion = "";
    /* access modifiers changed from: private */
    public int mToBeSavedBtInteropVersion = 0;
    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwLog.w(BluetoothParaManager.TAG, "BT_PARA onReceive: intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                HwLog.w(BluetoothParaManager.TAG, "BT_PARA onReceive: get null for intent.getAction()");
                return;
            }
            if (action.equals("huawei.intent.action.ACTION_EMCOM_PARA_READY")) {
                int cotaParaBitRec = intent.getIntExtra("EXTRA_EMCOM_PARA_READY_REC", 0);
                HwLog.d(BluetoothParaManager.TAG, "BT_PARA onReceive: cotaParaBitRec:" + cotaParaBitRec);
                if ((cotaParaBitRec & 16) == 0) {
                    HwLog.d(BluetoothParaManager.TAG, "BT_PARA onReceive: broadcast is not for bt");
                    return;
                }
                BluetoothParaManager.this.getCoteParaFilePath();
                HwLog.d(BluetoothParaManager.TAG, "BT_PARA broadcast mSysFilePath:" + BluetoothParaManager.mSysFilePath);
                HwLog.d(BluetoothParaManager.TAG, "BT_PARA broadcast mCotaFilePath:" + BluetoothParaManager.mCotaFilePath);
                File unused = BluetoothParaManager.this.mSysFile = new File(BluetoothParaManager.mSysFilePath);
                File unused2 = BluetoothParaManager.this.mCotaFile = new File(BluetoothParaManager.mCotaFilePath);
                new ParseBtInteropXmlThread(true).start();
            }
        }
    };

    class ParseBtInteropXmlThread extends Thread {
        boolean mIsCotaBroadcast;

        ParseBtInteropXmlThread(boolean cotaBroadcast) {
            this.mIsCotaBroadcast = cotaBroadcast;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:41:0x01d7, code lost:
            return;
         */
        public synchronized void run() {
            HwLog.d(BluetoothParaManager.TAG, "BT_PARA start parse BtInterop config file: is cota " + this.mIsCotaBroadcast);
            int unused = BluetoothParaManager.this.mSysConfigVersion = BluetoothParaManager.this.getConfigVersion(BluetoothParaManager.this.mSysFile);
            HwLog.i(BluetoothParaManager.TAG, "BT_PARA mSysConfigVersion:" + BluetoothParaManager.this.mSysConfigVersion);
            int unused2 = BluetoothParaManager.this.mCotaConfigVersion = BluetoothParaManager.this.getConfigVersion(BluetoothParaManager.this.mCotaFile);
            HwLog.i(BluetoothParaManager.TAG, "BT_PARA mCotaConfigVersion:" + BluetoothParaManager.this.mCotaConfigVersion);
            int unused3 = BluetoothParaManager.this.mSavedConfigVersion = BluetoothParaManager.this.getSavedConfigVersion();
            HwLog.i(BluetoothParaManager.TAG, "BT_PARA mSavedConfigVersion:" + BluetoothParaManager.this.mSavedConfigVersion);
            if (!this.mIsCotaBroadcast) {
                if (BluetoothParaManager.this.mSysConfigVersion <= BluetoothParaManager.this.mSavedConfigVersion) {
                    if (BluetoothParaManager.this.mCotaConfigVersion <= BluetoothParaManager.this.mSavedConfigVersion) {
                        HwLog.i(BluetoothParaManager.TAG, "BT_PARA not need to parse xml");
                    }
                }
                if (BluetoothParaManager.this.mSysConfigVersion >= BluetoothParaManager.this.mCotaConfigVersion) {
                    HwLog.i(BluetoothParaManager.TAG, "BT_PARA need update system file");
                    int unused4 = BluetoothParaManager.this.mToBeSavedBtInteropVersion = BluetoothParaManager.this.mSysConfigVersion;
                    BluetoothParaManager.this.updateBtInteropDataFromFile(BluetoothParaManager.this.mSysFile, this.mIsCotaBroadcast);
                } else {
                    HwLog.i(BluetoothParaManager.TAG, "BT_PARA need update cota file");
                    int unused5 = BluetoothParaManager.this.mToBeSavedBtInteropVersion = BluetoothParaManager.this.mCotaConfigVersion;
                    BluetoothParaManager.this.updateBtInteropDataFromFile(BluetoothParaManager.this.mCotaFile, this.mIsCotaBroadcast);
                }
            } else {
                boolean unused6 = BluetoothParaManager.this.mSendBroadcastToApk = false;
                int cotaFileLength = (int) BluetoothParaManager.this.mCotaFile.length();
                try {
                    if (!BluetoothParaManager.this.mCotaFile.exists()) {
                        HwLog.e(BluetoothParaManager.TAG, "BT_PARA cotafile does not exist: " + BluetoothParaManager.this.mCotaFile.exists());
                        BluetoothParaManager.this.responseForParaUpdate(0);
                    } else if (cotaFileLength >= BluetoothParaManager.BT_PARA_FILE_MAX_SIZE) {
                        BluetoothParaManager bluetoothParaManager = BluetoothParaManager.this;
                        bluetoothParaManager.btParaFileError("BT_PARA cotaFileLength is too large: " + cotaFileLength);
                        BluetoothParaManager.this.responseForParaUpdate(6);
                    } else if (BluetoothParaManager.this.getVersion(BluetoothParaManager.this.mCotaFile, "EMUI", "emui_version") == null) {
                        BluetoothParaManager.this.btParaFileError("BT_PARA get config file EMUI version failed.");
                        BluetoothParaManager.this.responseForParaUpdate(6);
                    } else if (BluetoothParaManager.this.mCotaConfigVersion > BluetoothParaManager.this.mSavedConfigVersion) {
                        HwLog.i(BluetoothParaManager.TAG, "BT_PARA cotaFile is newer than saved, need to parse");
                        int unused7 = BluetoothParaManager.this.mToBeSavedBtInteropVersion = BluetoothParaManager.this.mCotaConfigVersion;
                        BluetoothParaManager.this.updateBtInteropDataFromFile(BluetoothParaManager.this.mCotaFile, this.mIsCotaBroadcast);
                    } else {
                        HwLog.i(BluetoothParaManager.TAG, "BT_PARA cotaFile is not latest not need to parse ");
                        BluetoothParaManager.this.responseForParaUpdate(4);
                    }
                } catch (SecurityException e) {
                    HwLog.e(BluetoothParaManager.TAG, "BT_PARA mCotaFile exist or not exception");
                    BluetoothParaManager.this.responseForParaUpdate(0);
                }
            }
        }
    }

    public BluetoothParaManager(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        getCoteParaFilePath();
        HwLog.d(TAG, "BT_PARA mSysFilePath:" + mSysFilePath);
        HwLog.d(TAG, "BT_PARA mCotaFilePath:" + mCotaFilePath);
        this.mSysFile = new File(mSysFilePath);
        this.mCotaFile = new File(mCotaFilePath);
        try {
            this.mSystemEmuiVersion = getProperty("ro.build.version.emui", "").substring(getProperty("ro.build.version.emui", "").lastIndexOf("_") + 1);
            HwLog.i(TAG, "BT_PARA emuiVersionString:" + this.mSystemEmuiVersion);
        } catch (Exception e) {
            this.mSystemEmuiVersion = "";
            HwLog.e(TAG, "BT_PARA: FATAL ERROR - get system EMUI version failed");
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("huawei.intent.action.ACTION_EMCOM_PARA_READY");
        this.mContext.registerReceiver(this.myReceiver, intentFilter, "huawei.permission.RECEIVE_EMCOM_PARA_UPGRADE", null);
        compareEmuiVersion();
    }

    /* access modifiers changed from: private */
    public void getCoteParaFilePath() {
        HwLog.d(TAG, "BT_PARA getCoteParaFilePath() start");
        try {
            String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(mCotaParaCfgDir, "emcom/noncell/bt_interop.xml");
            if (cfgFileInfo == null) {
                HwLog.e(TAG, "BT_PARA Both default and cota config files not exist");
            } else if (cfgFileInfo[0].contains("/cota")) {
                HwLog.i(TAG, "BT_PARA cota config file path is: " + cfgFileInfo[0]);
                mCotaFilePath = cfgFileInfo[0];
            } else {
                HwLog.i(TAG, "BT_PARA system config file path is: " + cfgFileInfo[0]);
                mSysFilePath = cfgFileInfo[0];
            }
        } catch (NoClassDefFoundError e) {
            HwLog.e(TAG, "BT_PARA getCoteParaFilePath NoClassDefFoundError exception");
        } catch (Exception e2) {
            HwLog.e(TAG, "BT_PARA getCoteParaFilePath other exception");
        }
    }

    private void compareEmuiVersion() {
        this.mCpSavedEmuiVersion = Settings.Global.getString(this.mContentResolver, "hw_bluetooth_interop_emui_version");
        HwLog.d(TAG, "BT_PARA contentProvider saved Emui is:" + this.mCpSavedEmuiVersion);
        if (this.mCpSavedEmuiVersion == null) {
            HwLog.i(TAG, "BT_PARA contentProvider saved Emui is null");
            new ParseBtInteropXmlThread(false).start();
        } else if (this.mCpSavedEmuiVersion.equals(this.mSystemEmuiVersion)) {
            HwLog.i(TAG, "BT_PARA contentProvider saved Emui is equals system emui version");
            new ParseBtInteropXmlThread(false).start();
        } else {
            String systemFileEmuiVersion = getVersion(this.mSysFile, "EMUI", "emui_version");
            String cotaFileEmuiVersion = getVersion(this.mCotaFile, "EMUI", "emui_version");
            if (systemFileEmuiVersion == null && cotaFileEmuiVersion == null) {
                HwLog.i(TAG, "BT_PARA two files not exist");
                cleanContentProvider();
                return;
            }
            HwLog.i(TAG, "BT_PARA at least has one file exist");
            cleanContentProvider();
            new ParseBtInteropXmlThread(false).start();
        }
    }

    private void cleanContentProvider() {
        Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interop_version", null);
        Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interoperability_addr_list", null);
        Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interoperability_name_list", null);
        Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interoperability_manu_list", null);
        Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interop_emui_version", this.mSystemEmuiVersion);
        HwLog.i(TAG, "BT_PARA clean ContentProvider success");
    }

    public void responseForParaUpdate(int result) {
        HwLog.d(TAG, "BT_PARA response result: " + result);
        try {
            Class<?> class1 = Class.forName("android.emcom.EmcomManager");
            Object objInstance = class1.getDeclaredMethod(WifiProCHRManager.LOG_GET_INSTANCE_API_NAME, new Class[0]).invoke(class1, new Object[0]);
            if (objInstance != null) {
                class1.getDeclaredMethod("responseForParaUpgrade", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE}).invoke(objInstance, new Object[]{16, 1, Integer.valueOf(result)});
                HwLog.i(TAG, "BT_PARA response done with result: " + result);
                return;
            }
            HwLog.w(TAG, "BT_PARA objInstance() is null");
        } catch (NoSuchMethodException e) {
            HwLog.e(TAG, "BT_PARA response exception: NoSuchMethod");
        } catch (IllegalAccessException e2) {
            HwLog.e(TAG, "BT_PARA response exception: IllegalAccessException");
        } catch (ClassNotFoundException e3) {
            HwLog.e(TAG, "BT_PARA response exception: EmcomManager not found");
        } catch (Exception e4) {
            HwLog.e(TAG, "BT_PARA response exception");
        }
    }

    /* access modifiers changed from: private */
    public int getSavedConfigVersion() {
        int savedBtInteropVersion = 0;
        try {
            String savedConfigVersionString = Settings.Global.getString(this.mContentResolver, "hw_bluetooth_interop_version");
            HwLog.d(TAG, "BT_PARA contentProvider saved config version is:" + savedConfigVersionString);
            if (savedConfigVersionString != null) {
                savedBtInteropVersion = Integer.parseInt(savedConfigVersionString);
            } else {
                HwLog.d(TAG, "BT_PARA savedConfigVersionString is null");
            }
            return savedBtInteropVersion;
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "BT_PARA savedConfigVersionString :invalid string:");
            return 0;
        } catch (Exception e2) {
            HwLog.e(TAG, "BT_PARA getSavedConfigVersion:invalid string:");
            return 0;
        } catch (Throwable th) {
            return 0;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    /* access modifiers changed from: private */
    public int getConfigVersion(File file) {
        int btInteropVersion = 0;
        try {
            if (file.exists()) {
                if (getVersion(file, "EMUI", "emui_version") == null) {
                    btParaFileError("BT_PARA get file EMUI is null");
                } else if (getVersion(file, "EMUI", "config_version") == null) {
                    btParaFileError("BT_PARA get file BT is null");
                } else {
                    String btVersionString = getVersion(file, "EMUI", "config_version").trim();
                    if (!"".equals(btVersionString)) {
                        btInteropVersion = Integer.parseInt(btVersionString);
                        HwLog.d(TAG, "BT_PARA file btInteropVersion:" + btInteropVersion);
                    } else {
                        btParaFileError("BT_PARA btVersionString: empty string");
                    }
                }
                return 0;
            }
            HwLog.i(TAG, "BT_PARA file not exist");
            return btInteropVersion;
        } catch (NumberFormatException e) {
            btParaFileError("BT_PARA btVersionString: invalid string");
            return 0;
        } catch (Exception e2) {
            btParaFileError("BT_PARA getConfigVersion Exception:");
            return 0;
        } catch (Throwable th) {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public void updateBtInteropDataFromFile(File btInteropFile, boolean isCotaBroadcast) {
        InputStream btInteropIs = null;
        try {
            if (!btInteropFile.exists()) {
                if (isCotaBroadcast) {
                    HwLog.e(TAG, "BT_PARA ERROR - cota file does not exist");
                    responseForParaUpdate(0);
                }
                if (btInteropIs != null) {
                    try {
                        btInteropIs.close();
                    } catch (IOException e) {
                        HwLog.e(TAG, "BT_PARA btInteropFile close IOException:");
                    }
                }
                return;
            }
            btInteropIs = new FileInputStream(btInteropFile);
            if (btInteropIs.available() == 0) {
                if (isCotaBroadcast) {
                    responseForParaUpdate(6);
                }
                HwLog.e(TAG, "BT_PARA inputstream available() is 0");
                try {
                    btInteropIs.close();
                } catch (IOException e2) {
                    HwLog.e(TAG, "BT_PARA btInteropFile close IOException:");
                }
                return;
            }
            parserXMLPULL(btInteropIs, isCotaBroadcast);
            if (isCotaBroadcast) {
                if (this.mSendBroadcastToApk) {
                    this.mAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (this.mAdapter.getState() != 12) {
                        if (this.mAdapter.getState() != 11) {
                            HwLog.i(TAG, "BT_PARA bt state is not state on or state_turning_on not need to send broadcast");
                            responseForParaUpdate(7);
                            this.mSendBroadcastToApk = false;
                        }
                    }
                    Intent intent = new Intent();
                    intent.setAction(BT_PARA_UPDATE_ACTION);
                    this.mContext.sendBroadcast(intent, BT_PARA_UPDATE_PERMISSION);
                    HwLog.i(TAG, "BT_PARA send broadcast to app done");
                    this.mSendBroadcastToApk = false;
                } else {
                    btParaFileError("BT_PARA xml file format is incorrect");
                    responseForParaUpdate(6);
                }
            }
            try {
                btInteropIs.close();
            } catch (IOException e3) {
                HwLog.e(TAG, "BT_PARA btInteropFile close IOException:");
            }
        } catch (FileNotFoundException e4) {
            if (isCotaBroadcast) {
                responseForParaUpdate(0);
            }
            btParaFileError("BT_PARA updateBtInteropDataFromFile FileNotFoundException");
            if (btInteropIs != null) {
                btInteropIs.close();
            }
        } catch (IOException e5) {
            if (isCotaBroadcast) {
                responseForParaUpdate(6);
            }
            btParaFileError("BT_PARA updateBtInteropDataFromFile IOException");
            if (btInteropIs != null) {
                btInteropIs.close();
            }
        } catch (Exception e6) {
            if (isCotaBroadcast) {
                responseForParaUpdate(6);
            }
            btParaFileError("BT_PARA updateBtInteropDataFromFile Exception");
            if (btInteropIs != null) {
                btInteropIs.close();
            }
        } catch (Throwable th) {
            if (btInteropIs != null) {
                try {
                    btInteropIs.close();
                } catch (IOException e7) {
                    HwLog.e(TAG, "BT_PARA btInteropFile close IOException:");
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public String getVersion(File file, String versionTag, String attributeTag) {
        String result = null;
        InputStream is = null;
        if (versionTag == null) {
            HwLog.d(TAG, "BT_PARA versionTag is null");
            return null;
        }
        try {
            if (file.exists()) {
                XmlPullParser versionParser = XmlPullParserFactory.newInstance().newPullParser();
                is = new FileInputStream(file);
                if (is.available() != 0) {
                    versionParser.setInput(is, "UTF-8");
                    int type = versionParser.getEventType();
                    while (true) {
                        if (type != 1) {
                            if (type == 2 && versionTag.equals(versionParser.getName())) {
                                result = versionParser.getAttributeValue(null, attributeTag);
                                break;
                            }
                            type = versionParser.next();
                        } else {
                            break;
                        }
                    }
                } else {
                    HwLog.e(TAG, "BT_PARA getVersion: is.available() == 0");
                    try {
                        is.close();
                    } catch (IOException e) {
                        HwLog.e(TAG, "BT_PARA is close IOException:");
                    }
                    return null;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e2) {
                    HwLog.e(TAG, "BT_PARA is close IOException:");
                }
            }
        } catch (XmlPullParserException e3) {
            btParaFileError("BT_PARA getVersion Parser Exception");
            if (is != null) {
                is.close();
            }
        } catch (IOException e4) {
            btParaFileError("BT_PARA getVersion IOException");
            if (is != null) {
                is.close();
            }
        } catch (Exception e5) {
            btParaFileError("BT_PARA getVersion Exception");
            if (is != null) {
                is.close();
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e6) {
                    HwLog.e(TAG, "BT_PARA is close IOException:");
                }
            }
            throw th;
        }
        return result;
    }

    private void parserXMLPULL(InputStream is, boolean isCotaBroadcast) {
        String addr_str;
        XmlPullParserFactory xpf;
        String str;
        InputStream inputStream = is;
        boolean z = isCotaBroadcast;
        try {
            HwLog.d(TAG, "parserXMLPULL: isCota " + z);
            if (is.available() == 0) {
                if (z) {
                    responseForParaUpdate(6);
                }
                HwLog.e(TAG, "BT_PARA ERROR - file does not exist");
                if (inputStream != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        IOException iOException = e;
                        HwLog.e(TAG, "BT_PARA is close IOException in parserXMLPULL:");
                    }
                }
                return;
            }
            String addr_str2 = null;
            String name_str = null;
            String manufacture_str = null;
            String platformName = "";
            String databasesName = "";
            String releaseName = "";
            String configName = "";
            boolean firstHalfFormateCorrect = false;
            XmlPullParserFactory xpf2 = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xpf2.newPullParser();
            parser.setInput(inputStream, "UTF-8");
            int type = parser.getEventType();
            while (type != 1) {
                if (type != 0) {
                    switch (type) {
                        case 2:
                            xpf = xpf2;
                            String addr_str3 = addr_str2;
                            String tag_name = parser.getName();
                            if ("databases".equals(tag_name)) {
                                databasesName = "databases";
                            }
                            if ("release".equals(tag_name) && "databases".equals(databasesName)) {
                                releaseName = "release";
                            }
                            if ("config".equals(tag_name) && "release".equals(releaseName)) {
                                configName = "config";
                            }
                            if (HwCertification.SIGNATURE_PLATFORM.equals(tag_name) && "config".equals(configName)) {
                                platformName = HwCertification.SIGNATURE_PLATFORM;
                                firstHalfFormateCorrect = true;
                            }
                            if (HwCertification.SIGNATURE_PLATFORM.equals(platformName)) {
                                if ("interop_addr".equals(tag_name)) {
                                    addr_str3 = handleString(parser.nextText());
                                }
                                if ("interop_name".equals(tag_name)) {
                                    name_str = handleString(parser.nextText());
                                }
                                if ("interop_manufacture".equals(tag_name)) {
                                    manufacture_str = handleString(parser.nextText());
                                }
                            }
                            break;
                        case 3:
                            xpf = xpf2;
                            if (HwCertification.SIGNATURE_PLATFORM.equals(parser.getName()) && HwCertification.SIGNATURE_PLATFORM.equals(platformName)) {
                                platformName = "";
                            }
                            if ("config".equals(parser.getName()) && firstHalfFormateCorrect && "".equals(platformName)) {
                                configName = "";
                            }
                            if ("release".equals(parser.getName()) && firstHalfFormateCorrect && "".equals(configName)) {
                                releaseName = "";
                            }
                            if (!"databases".equals(parser.getName()) || !firstHalfFormateCorrect || !"".equals(releaseName)) {
                                addr_str = addr_str2;
                                str = platformName;
                            } else {
                                HwLog.d(TAG, "BT_PARA xml file formate is correct");
                                str = platformName;
                                Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interop_version", "0");
                                Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interoperability_addr_list", addr_str2);
                                Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interoperability_name_list", name_str);
                                Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interoperability_manu_list", manufacture_str);
                                Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interop_emui_version", this.mSystemEmuiVersion);
                                ContentResolver contentResolver = this.mContentResolver;
                                StringBuilder sb = new StringBuilder();
                                addr_str = addr_str2;
                                sb.append(this.mToBeSavedBtInteropVersion);
                                sb.append("");
                                Settings.Global.putString(contentResolver, "hw_bluetooth_interop_version", sb.toString());
                                HwLog.w(TAG, "BT_PARA contentprovider saved xml config version is:" + this.mToBeSavedBtInteropVersion);
                                if (z) {
                                    this.mSendBroadcastToApk = true;
                                }
                                databasesName = "";
                            }
                            platformName = str;
                            break;
                        case 4:
                            xpf = xpf2;
                            break;
                        default:
                            xpf = xpf2;
                            HwLog.w(TAG, "BT_PARA parse file - default clause, unexpected.");
                            break;
                    }
                    addr_str = addr_str2;
                } else {
                    xpf = xpf2;
                    addr_str = addr_str2;
                }
                addr_str2 = addr_str;
                type = parser.next();
                xpf2 = xpf;
            }
            if (inputStream != null) {
                try {
                    is.close();
                } catch (IOException e2) {
                    IOException iOException2 = e2;
                    HwLog.e(TAG, "BT_PARA is close IOException in parserXMLPULL:");
                }
            }
        } catch (XmlPullParserException e3) {
            HwLog.e(TAG, "BT_PARA parserXMLPULL exception: XmlPullParserException");
            if (inputStream != null) {
                is.close();
            }
        } catch (IOException e4) {
            HwLog.e(TAG, "BT_PARA parserXMLPULL exception: IOException");
            if (inputStream != null) {
                is.close();
            }
        } catch (Exception e5) {
            HwLog.e(TAG, "BT_PARA parserXMLPULL exception: Exception");
            if (inputStream != null) {
                is.close();
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (inputStream != null) {
                try {
                    is.close();
                } catch (IOException e6) {
                    IOException iOException3 = e6;
                    HwLog.e(TAG, "BT_PARA is close IOException in parserXMLPULL:");
                }
            }
            throw th2;
        }
    }

    private String handleString(String string) {
        String result = "";
        if (string != null) {
            try {
                for (String entry : string.split("\n")) {
                    String entry2 = entry.trim();
                    if ("".equals(entry2)) {
                        HwLog.d(TAG, "BT_PARA handleString entry is null ");
                    } else if (entry2.charAt(entry2.length() - 1) == ';') {
                        result = result + entry2;
                    }
                }
            } catch (Exception e) {
                btParaFileError("BT_PARA handleString Exception");
            } catch (Throwable th) {
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public void btParaFileError(String msg) {
        HwLog.e(TAG, "BT_PARA_FILE_WARNING " + msg);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            return (String) c.getMethod("get", new Class[]{String.class, String.class}).invoke(c, new Object[]{key, "unknown"});
        } catch (Exception e) {
            HwLog.e(TAG, "BT_PARA getProperty: e:");
        } catch (Throwable th) {
        }
        return value;
    }
}
