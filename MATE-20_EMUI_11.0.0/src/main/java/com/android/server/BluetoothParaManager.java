package com.android.server;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.android.server.pm.auth.HwCertification;
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
    private static String mCotaFilePath = "";
    private static final String mCotaParaCfgDir = "emcom/noncell";
    private static String mSysFilePath = "";
    private BluetoothAdapter mAdapter = null;
    private final ContentResolver mContentResolver;
    private Context mContext;
    private int mCotaConfigVersion = 0;
    private File mCotaFile;
    private String mCpSavedEmuiVersion = "";
    private int mSavedConfigVersion = 0;
    private boolean mSendBroadcastToApk = false;
    private int mSysConfigVersion = 0;
    private File mSysFile;
    private String mSystemEmuiVersion = "";
    private int mToBeSavedBtInteropVersion = 0;
    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        /* class com.android.server.BluetoothParaManager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwLog.w(BluetoothParaManager.TAG, "BT_PARA onReceive: intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                HwLog.w(BluetoothParaManager.TAG, "BT_PARA onReceive: get null for intent.getAction()");
            } else if (action.equals("huawei.intent.action.ACTION_EMCOM_PARA_READY")) {
                int cotaParaBitRec = intent.getIntExtra("EXTRA_EMCOM_PARA_READY_REC", 0);
                HwLog.i(BluetoothParaManager.TAG, "BT_PARA onReceive: cotaParaBitRec:" + cotaParaBitRec);
                if ((cotaParaBitRec & 16) == 0) {
                    HwLog.i(BluetoothParaManager.TAG, "BT_PARA onReceive: broadcast is not for bt");
                    return;
                }
                BluetoothParaManager.this.mAdapter = BluetoothAdapter.getDefaultAdapter();
                if (BluetoothParaManager.this.mAdapter == null) {
                    HwLog.i(BluetoothParaManager.TAG, "onReceive mAdapter is null");
                } else if (BluetoothParaManager.this.mAdapter.getState() == 12 || BluetoothParaManager.this.mAdapter.getState() == 11) {
                    Intent updateIntent = new Intent();
                    intent.setPackage(HwBluetoothBigDataService.BIGDATA_RECEIVER_PACKAGENAME);
                    updateIntent.setAction(BluetoothParaManager.BT_PARA_UPDATE_ACTION);
                    BluetoothParaManager.this.mContext.sendBroadcast(updateIntent, BluetoothParaManager.BT_PARA_UPDATE_PERMISSION);
                    HwLog.i(BluetoothParaManager.TAG, "BT_PARA send broadcast to bluetooth app done");
                } else {
                    HwLog.i(BluetoothParaManager.TAG, "BT_PARA bt state is not state on or state_turning_on not need to send broadcast");
                    BluetoothParaManager.this.responseForParaUpdate(7);
                }
            }
        }
    };

    public BluetoothParaManager(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        getCoteParaFilePath();
        HwLog.d(TAG, "BT_PARA mSysFilePath:" + mSysFilePath);
        HwLog.d(TAG, "BT_PARA mCotaFilePath:" + mCotaFilePath);
        this.mSysFile = new File(mSysFilePath);
        this.mCotaFile = new File(mCotaFilePath);
        this.mSystemEmuiVersion = getProperty("ro.build.version.emui", "");
        int versionIndex = this.mSystemEmuiVersion.lastIndexOf("_");
        if (versionIndex == -1) {
            this.mSystemEmuiVersion = "";
            HwLog.e(TAG, "BT_PARA: FATAL ERROR - get system EMUI version failed");
        } else {
            this.mSystemEmuiVersion = this.mSystemEmuiVersion.substring(versionIndex + 1);
            HwLog.i(TAG, "BT_PARA emuiVersionString:" + this.mSystemEmuiVersion);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("huawei.intent.action.ACTION_EMCOM_PARA_READY");
        this.mContext.registerReceiver(this.myReceiver, intentFilter, "huawei.permission.RECEIVE_EMCOM_PARA_UPGRADE", null);
    }

    private void getCoteParaFilePath() {
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
            Object objInstance = class1.getDeclaredMethod("getInstance", new Class[0]).invoke(class1, new Object[0]);
            if (objInstance != null) {
                class1.getDeclaredMethod("responseForParaUpgrade", Integer.TYPE, Integer.TYPE, Integer.TYPE).invoke(objInstance, 16, 1, Integer.valueOf(result));
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

    class ParseBtInteropXmlThread extends Thread {
        boolean mIsCotaBroadcast;

        ParseBtInteropXmlThread(boolean cotaBroadcast) {
            this.mIsCotaBroadcast = cotaBroadcast;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public synchronized void run() {
            HwLog.d(BluetoothParaManager.TAG, "BT_PARA start parse BtInterop config file: is cota " + this.mIsCotaBroadcast);
            BluetoothParaManager.this.mSysConfigVersion = BluetoothParaManager.this.getConfigVersion(BluetoothParaManager.this.mSysFile);
            HwLog.i(BluetoothParaManager.TAG, "BT_PARA mSysConfigVersion:" + BluetoothParaManager.this.mSysConfigVersion);
            BluetoothParaManager.this.mCotaConfigVersion = BluetoothParaManager.this.getConfigVersion(BluetoothParaManager.this.mCotaFile);
            HwLog.i(BluetoothParaManager.TAG, "BT_PARA mCotaConfigVersion:" + BluetoothParaManager.this.mCotaConfigVersion);
            BluetoothParaManager.this.mSavedConfigVersion = BluetoothParaManager.this.getSavedConfigVersion();
            HwLog.i(BluetoothParaManager.TAG, "BT_PARA mSavedConfigVersion:" + BluetoothParaManager.this.mSavedConfigVersion);
            if (!this.mIsCotaBroadcast) {
                if (BluetoothParaManager.this.mSysConfigVersion <= BluetoothParaManager.this.mSavedConfigVersion) {
                    if (BluetoothParaManager.this.mCotaConfigVersion <= BluetoothParaManager.this.mSavedConfigVersion) {
                        HwLog.i(BluetoothParaManager.TAG, "BT_PARA not need to parse xml");
                    }
                }
                if (BluetoothParaManager.this.mSysConfigVersion >= BluetoothParaManager.this.mCotaConfigVersion) {
                    HwLog.i(BluetoothParaManager.TAG, "BT_PARA need update system file");
                    BluetoothParaManager.this.mToBeSavedBtInteropVersion = BluetoothParaManager.this.mSysConfigVersion;
                    BluetoothParaManager.this.updateBtInteropDataFromFile(BluetoothParaManager.this.mSysFile, this.mIsCotaBroadcast);
                } else {
                    HwLog.i(BluetoothParaManager.TAG, "BT_PARA need update cota file");
                    BluetoothParaManager.this.mToBeSavedBtInteropVersion = BluetoothParaManager.this.mCotaConfigVersion;
                    BluetoothParaManager.this.updateBtInteropDataFromFile(BluetoothParaManager.this.mCotaFile, this.mIsCotaBroadcast);
                }
            } else {
                BluetoothParaManager.this.mSendBroadcastToApk = false;
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
                        BluetoothParaManager.this.mToBeSavedBtInteropVersion = BluetoothParaManager.this.mCotaConfigVersion;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getSavedConfigVersion() {
        try {
            String savedConfigVersionString = Settings.Global.getString(this.mContentResolver, "hw_bluetooth_interop_version");
            HwLog.d(TAG, "BT_PARA contentProvider saved config version is:" + savedConfigVersionString);
            if (savedConfigVersionString != null) {
                return Integer.parseInt(savedConfigVersionString);
            }
            HwLog.d(TAG, "BT_PARA savedConfigVersionString is null");
            return 0;
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "BT_PARA savedConfigVersionString :invalid string:");
            return 0;
        } catch (Exception e2) {
            HwLog.e(TAG, "BT_PARA getSavedConfigVersion:invalid string:");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getConfigVersion(File file) {
        try {
            if (!file.exists()) {
                HwLog.i(TAG, "BT_PARA file not exist");
                return 0;
            } else if (getVersion(file, "EMUI", "emui_version") == null) {
                btParaFileError("BT_PARA get file EMUI is null");
                return 0;
            } else if (getVersion(file, "EMUI", "config_version") == null) {
                btParaFileError("BT_PARA get file BT is null");
                return 0;
            } else {
                String btVersionString = getVersion(file, "EMUI", "config_version").trim();
                if (!"".equals(btVersionString)) {
                    int btInteropVersion = Integer.parseInt(btVersionString);
                    HwLog.d(TAG, "BT_PARA file btInteropVersion:" + btInteropVersion);
                    return btInteropVersion;
                }
                btParaFileError("BT_PARA btVersionString: empty string");
                return 0;
            }
        } catch (NumberFormatException e) {
            btParaFileError("BT_PARA btVersionString: invalid string");
            return 0;
        } catch (Exception e2) {
            btParaFileError("BT_PARA getConfigVersion Exception:");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBtInteropDataFromFile(File btInteropFile, boolean isCotaBroadcast) {
        InputStream btInteropIs = null;
        try {
            if (!btInteropFile.exists()) {
                if (isCotaBroadcast) {
                    HwLog.e(TAG, "BT_PARA ERROR - cota file does not exist");
                    responseForParaUpdate(0);
                }
                if (0 != 0) {
                    try {
                        btInteropIs.close();
                    } catch (IOException e) {
                        HwLog.e(TAG, "BT_PARA btInteropFile close IOException:");
                    }
                }
            } else {
                InputStream btInteropIs2 = new FileInputStream(btInteropFile);
                if (btInteropIs2.available() == 0) {
                    if (isCotaBroadcast) {
                        responseForParaUpdate(6);
                    }
                    HwLog.e(TAG, "BT_PARA inputstream available() is 0");
                    try {
                        btInteropIs2.close();
                    } catch (IOException e2) {
                        HwLog.e(TAG, "BT_PARA btInteropFile close IOException:");
                    }
                } else {
                    parserXMLPULL(btInteropIs2, isCotaBroadcast);
                    if (isCotaBroadcast) {
                        if (this.mSendBroadcastToApk) {
                            this.mAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (this.mAdapter.getState() == 12 || this.mAdapter.getState() == 11) {
                                Intent intent = new Intent();
                                intent.setAction(BT_PARA_UPDATE_ACTION);
                                this.mContext.sendBroadcast(intent, BT_PARA_UPDATE_PERMISSION);
                                HwLog.i(TAG, "BT_PARA send broadcast to app done");
                            } else {
                                HwLog.i(TAG, "BT_PARA bt state is not state on or state_turning_on not need to send broadcast");
                                responseForParaUpdate(7);
                            }
                            this.mSendBroadcastToApk = false;
                        } else {
                            btParaFileError("BT_PARA xml file format is incorrect");
                            responseForParaUpdate(6);
                        }
                    }
                    try {
                        btInteropIs2.close();
                    } catch (IOException e3) {
                        HwLog.e(TAG, "BT_PARA btInteropFile close IOException:");
                    }
                }
            }
        } catch (FileNotFoundException e4) {
            if (isCotaBroadcast) {
                responseForParaUpdate(0);
            }
            btParaFileError("BT_PARA updateBtInteropDataFromFile FileNotFoundException");
            if (0 != 0) {
                btInteropIs.close();
            }
        } catch (IOException e5) {
            if (isCotaBroadcast) {
                responseForParaUpdate(6);
            }
            btParaFileError("BT_PARA updateBtInteropDataFromFile IOException");
            if (0 != 0) {
                btInteropIs.close();
            }
        } catch (Exception e6) {
            if (isCotaBroadcast) {
                responseForParaUpdate(6);
            }
            btParaFileError("BT_PARA updateBtInteropDataFromFile Exception");
            if (0 != 0) {
                btInteropIs.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
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
    /* access modifiers changed from: public */
    private String getVersion(File file, String versionTag, String attributeTag) {
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
            if (0 != 0) {
                is.close();
            }
        } catch (IOException e4) {
            btParaFileError("BT_PARA getVersion IOException");
            if (0 != 0) {
                is.close();
            }
        } catch (Exception e5) {
            btParaFileError("BT_PARA getVersion Exception");
            if (0 != 0) {
                is.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
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

    /* JADX WARNING: Removed duplicated region for block: B:118:0x0231 A[SYNTHETIC, Splitter:B:118:0x0231] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x023c  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0247  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x0258 A[SYNTHETIC, Splitter:B:131:0x0258] */
    /* JADX WARNING: Removed duplicated region for block: B:139:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:140:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:141:? A[RETURN, SYNTHETIC] */
    private void parserXMLPULL(InputStream is, boolean isCotaBroadcast) {
        IOException iOException;
        String str;
        String str2;
        String str3;
        String releaseName;
        String databasesName;
        String databasesName2;
        String configName;
        String configName2;
        String releaseName2;
        String str4 = "hw_bluetooth_interop_version";
        String str5 = "BT_PARA is close IOException in parserXMLPULL:";
        try {
            HwLog.d(TAG, "parserXMLPULL: isCota " + isCotaBroadcast);
            if (is.available() == 0) {
                if (isCotaBroadcast) {
                    responseForParaUpdate(6);
                }
                HwLog.e(TAG, "BT_PARA ERROR - file does not exist");
                try {
                    is.close();
                    return;
                } catch (IOException e) {
                    HwLog.e(TAG, str5);
                    return;
                }
            } else {
                String addr_str = null;
                String name_str = null;
                String manufacture_str = null;
                String platformName = "";
                String databasesName3 = "";
                String databasesName4 = "";
                String releaseName3 = "";
                String configName3 = "";
                boolean firstHalfFormateCorrect = false;
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(is, "UTF-8");
                int type = parser.getEventType();
                while (type != 1) {
                    if (type != 0) {
                        databasesName = databasesName4;
                        releaseName = releaseName3;
                        str2 = str5;
                        if (type == 2) {
                            String tag_name = parser.getName();
                            if ("databases".equals(tag_name)) {
                                str3 = str4;
                                databasesName2 = "databases";
                            } else {
                                str3 = str4;
                                databasesName2 = databasesName;
                            }
                            if (!"release".equals(tag_name) || !"databases".equals(databasesName2)) {
                                releaseName3 = releaseName;
                            } else {
                                releaseName3 = "release";
                            }
                            if (!"config".equals(tag_name) || !"release".equals(releaseName3)) {
                                configName = configName3;
                            } else {
                                configName = "config";
                            }
                            if (HwCertification.SIGNATURE_PLATFORM.equals(tag_name) && "config".equals(configName)) {
                                platformName = HwCertification.SIGNATURE_PLATFORM;
                                firstHalfFormateCorrect = true;
                            }
                            if (HwCertification.SIGNATURE_PLATFORM.equals(platformName)) {
                                if ("interop_addr".equals(tag_name)) {
                                    addr_str = handleString(parser.nextText());
                                }
                                if ("interop_name".equals(tag_name)) {
                                    name_str = handleString(parser.nextText());
                                }
                                if ("interop_manufacture".equals(tag_name)) {
                                    manufacture_str = handleString(parser.nextText());
                                    databasesName3 = tag_name;
                                    configName3 = configName;
                                    databasesName4 = databasesName2;
                                } else {
                                    databasesName3 = tag_name;
                                    configName3 = configName;
                                    databasesName4 = databasesName2;
                                }
                            } else {
                                databasesName3 = tag_name;
                                configName3 = configName;
                                databasesName4 = databasesName2;
                            }
                        } else if (type == 3) {
                            if (HwCertification.SIGNATURE_PLATFORM.equals(parser.getName()) && HwCertification.SIGNATURE_PLATFORM.equals(platformName)) {
                                platformName = "";
                            }
                            if (!"config".equals(parser.getName()) || !firstHalfFormateCorrect || !"".equals(platformName)) {
                                configName2 = configName3;
                            } else {
                                configName2 = "";
                            }
                            if (!"release".equals(parser.getName()) || !firstHalfFormateCorrect || !"".equals(configName2)) {
                                releaseName2 = releaseName;
                            } else {
                                releaseName2 = "";
                            }
                            if (!"databases".equals(parser.getName()) || !firstHalfFormateCorrect || !"".equals(releaseName2)) {
                                str3 = str4;
                                configName3 = configName2;
                                releaseName3 = releaseName2;
                                databasesName3 = databasesName3;
                                databasesName4 = databasesName;
                            } else {
                                HwLog.d(TAG, "BT_PARA xml file formate is correct");
                                Settings.Global.putString(this.mContentResolver, str4, "0");
                                Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interoperability_addr_list", addr_str);
                                Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interoperability_name_list", name_str);
                                Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interoperability_manu_list", manufacture_str);
                                Settings.Global.putString(this.mContentResolver, "hw_bluetooth_interop_emui_version", this.mSystemEmuiVersion);
                                ContentResolver contentResolver = this.mContentResolver;
                                Settings.Global.putString(contentResolver, str4, this.mToBeSavedBtInteropVersion + "");
                                HwLog.w(TAG, "BT_PARA contentprovider saved xml config version is:" + this.mToBeSavedBtInteropVersion);
                                if (isCotaBroadcast) {
                                    this.mSendBroadcastToApk = true;
                                }
                                str3 = str4;
                                configName3 = configName2;
                                releaseName3 = releaseName2;
                                databasesName4 = "";
                                databasesName3 = databasesName3;
                            }
                        } else if (type != 4) {
                            try {
                                HwLog.w(TAG, "BT_PARA parse file - default clause, unexpected.");
                                str3 = str4;
                            } catch (XmlPullParserException e2) {
                                HwLog.e(TAG, "BT_PARA parserXMLPULL exception: XmlPullParserException");
                                if (is == null) {
                                }
                            } catch (IOException e3) {
                                HwLog.e(TAG, "BT_PARA parserXMLPULL exception: IOException");
                                if (is == null) {
                                }
                            } catch (Exception e4) {
                                str = str2;
                                try {
                                    HwLog.e(TAG, "BT_PARA parserXMLPULL exception: Exception");
                                    if (is == null) {
                                    }
                                } catch (Throwable th) {
                                    e = th;
                                    iOException = e;
                                    if (is != null) {
                                    }
                                    throw iOException;
                                }
                            } catch (Throwable th2) {
                                iOException = th2;
                                str = str2;
                                if (is != null) {
                                }
                                throw iOException;
                            }
                        } else {
                            str3 = str4;
                        }
                        type = parser.next();
                        str4 = str3;
                        str5 = str2;
                    } else {
                        str3 = str4;
                        str2 = str5;
                        databasesName = databasesName4;
                        releaseName = releaseName3;
                    }
                    databasesName3 = databasesName3;
                    databasesName4 = databasesName;
                    releaseName3 = releaseName;
                    type = parser.next();
                    str4 = str3;
                    str5 = str2;
                }
                try {
                    is.close();
                    return;
                } catch (IOException e5) {
                    str = str5;
                }
            }
            HwLog.e(TAG, str);
        } catch (XmlPullParserException e6) {
            HwLog.e(TAG, "BT_PARA parserXMLPULL exception: XmlPullParserException");
            if (is == null) {
                is.close();
            }
        } catch (IOException e7) {
            HwLog.e(TAG, "BT_PARA parserXMLPULL exception: IOException");
            if (is == null) {
                is.close();
            }
        } catch (Exception e8) {
            str = str5;
            HwLog.e(TAG, "BT_PARA parserXMLPULL exception: Exception");
            if (is == null) {
                try {
                    is.close();
                } catch (IOException e9) {
                }
            }
        } catch (Throwable th3) {
            e = th3;
            str = str5;
            iOException = e;
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e10) {
                    HwLog.e(TAG, str);
                }
            }
            throw iOException;
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
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void btParaFileError(String msg) {
        HwLog.e(TAG, "BT_PARA_FILE_WARNING " + msg);
    }

    public static String getProperty(String key, String defaultValue) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            return (String) c.getMethod("get", String.class, String.class).invoke(c, key, ModelBaseService.UNKONW_IDENTIFY_RET);
        } catch (Exception e) {
            HwLog.e(TAG, "BT_PARA getProperty: e:");
            return defaultValue;
        }
    }
}
