package com.android.server.wifi.wifipro;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.PortalWebPageInfo;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class PortalAutoFillManager {
    public static final String ACTION_AUTO_FILL_PHONE_NUM_STATUS = "com.huawei.wifiplus.ACTION_AUTO_FILL_PHONE_NUM_STATUS";
    public static final String ACTION_AUTO_FILL_RANDOM_CODE_STATUS = "com.huawei.wifi.ACTION_AUTO_FILL_RANDOM_CODE_STATUS";
    public static final String ACTION_REQUEST_AUTO_FILL_PHONE_NUM = "com.huawei.wifiplus.ACTION_REQUEST_AUTO_FILL_PHONE_NUM";
    public static final String ACTION_REQUEST_AUTO_FILL_RANDOM_CODE = "com.huawei.wifiplus.ACTION_REQUEST_AUTO_FILL_RANDOM_CODE";
    public static final String ACTION_REQUEST_AUTO_LOGIN = "com.huawei.wifi.ACTION_REQUEST_AUTO_LOGIN";
    public static final String ACTION_RESEND_CHOSEN_PHONE_NUM = "com.huawei.wifi.ACTION_RESEND_CHOSEN_PHONE_NUM";
    public static final String ACTION_RESP_AUTO_FILL_PHONE_NUM = "com.huawei.wifiplus.ACTION_RESP_AUTO_FILL_PHONE_NUM";
    public static final long AUTO_FILL_PHONE_NUM_DELAY_MS = 1000;
    public static final long AUTO_FILL_PW_DELAY_MS = 3000;
    public static final long AUTO_LOGIN_DELAY_MS = 1300;
    public static final String BROWSER_CUSTOM_DATA = "BROWSER_CUSTOM_DATA";
    public static final String BROWSER_PACKET_NAME = "com.android.browser";
    private static final int CARD_TYPE_CHINA_MOBILE = 0;
    private static final int CARD_TYPE_CHINA_TELECOM = 2;
    private static final int CARD_TYPE_CHINA_UNICOM = 1;
    private static final int CARD_TYPE_UNKNOWN = -1;
    public static final String CLOUD_OTA_PERMISSION = "huawei.permission.RECEIVE_CLOUD_OTA_UPDATA";
    public static final String CLOUD_OTA_WIFI_PLUS_UPDATE = "cloud.ota.wifi.plus.UPDATE";
    public static final String FLAG_AUTO_FILL_STATUS = "com.huawei.wifiplus.FLAG_AUTO_FILL_STATUS";
    public static final String FLAG_LOGIN_BTN_ID = "com.huawei.wifiplus.FLAG_LOGIN_BTN_ID";
    public static final String FLAG_LOGIN_BTN_NAME = "com.huawei.wifiplus.FLAG_LOGIN_BTN_NAME";
    public static final String FLAG_LOGIN_BTN_NODE_TYPE = "com.huawei.wifiplus.FLAG_LOGIN_BTN_NODE_TYPE";
    public static final String FLAG_LOGIN_BTN_VALUE = "com.huawei.wifiplus.FLAG_LOGIN_BTN_VALUE";
    public static final String FLAG_OVERRIDE_NEW_URL = "com.huawei.wifiplus.FLAG_OVERRIDE_NEW_URL";
    public static final String FLAG_PHONE_NUMBER = "com.huawei.wifiplus.FLAG_PHONE_NUMBER";
    public static final String FLAG_PHONE_NUM_INPUT_ID = "com.huawei.wifiplus.FLAG_PHONE_NUM_INPUT_ID";
    public static final String FLAG_PHONE_NUM_INPUT_NAME = "com.huawei.wifiplus.FLAG_PHONE_NUM_INPUT_NAME";
    public static final String FLAG_RANDOM_CODE = "com.huawei.wifiplus.FLAG_RANDOM_CODE";
    public static final String FLAG_RANDOM_CODE_INPUT_ID = "com.huawei.wifiplus.FLAG_RANDOM_CODE_INPUT_ID";
    public static final String FLAG_RANDOM_CODE_INPUT_NAME = "com.huawei.wifiplus.FLAG_RANDOM_CODE_INPUT_NAME";
    public static final String FLAG_RANDOM_CODE_INPUT_VALUE = "com.huawei.wifiplus.FLAG_RANDOM_CODE_INPUT_VALUE";
    public static final String FLAG_SUPPORT_AUTO_FILL = "com.huawei.wifiplus.FLAG_SUPPORT_AUTO_FILL";
    public static final String ITEM_LOGIN_ID = "login_btn_id";
    public static final String ITEM_LOGIN_NAME = "login_btn_name";
    public static final String ITEM_LOGIN_NODE_TYPE = "login_node_type";
    public static final String ITEM_LOGIN_VALUE = "login_btn_value";
    public static final String ITEM_PHONE_NUM_ID = "first_step_id";
    public static final String ITEM_PHONE_NUM_NAME = "first_step_name";
    public static final String ITEM_PW_ID = "second_step_id";
    public static final String ITEM_PW_NAME = "second_step_name";
    public static final String ITEM_PW_VALUE = "second_step_value";
    public static final String ITEM_SSID = "ssid";
    public static final String ITEM_UPDATE_ONLY = "update_only";
    public static final String ITEM_URL = "page_location";
    public static final String LAST_CHR_UPDATE_VERSION_KEY = "WIFI_PRO_LAST_CHR_UPDATE_VERSION_KEY";
    public static final String LAST_INPUT_PHONE_NUMBER = "WIFI_PRO_LAST_INPUT_PHONE_NUMBER";
    public static final String LAST_LOCAL_UPDATE_VERSION_KEY = "WIFI_PRO_LAST_LOCAL_UPDATE_VERSION_KEY";
    private static final String MOBILE_CODE_CN = "+86";
    private static final int MSG_INIT_COLLECTION_DATABASE = 100;
    private static final int MSG_QUERY_AUTO_FILL_SUPPORTED = 102;
    private static final int MSG_REQUEST_AUTO_FILL_PW = 105;
    private static final int MSG_REQUEST_AUTO_LOGIN = 106;
    private static final int MSG_REQ_AUTO_FILL_PHONE_NUM = 101;
    private static final int MSG_SET_PORTAL_PROPERTY_FLAG = 103;
    private static final int MSG_UPDATE_CHOSEN_PHONE_NUMBER = 104;
    private static final int MSG_UPDATE_COTA_DATABASE = 107;
    public static final String NODE_A_STRING = "a";
    public static final String NODE_INPUT_STRING = "input";
    public static final String NODE_SPAN_STRING = "span";
    public static final int NODE_TYPE_A_INT = 3;
    public static final int NODE_TYPE_INPUT_INT = 2;
    public static final int NODE_TYPE_SPAN_INT = 1;
    public static final int NODE_TYPE_UNKNOWN = 0;
    private static final String[] OPERATOR_CODE_CHINA_MOBILE = null;
    private static final String[] OPERATOR_CODE_CHINA_TELECOM = null;
    private static final String[] OPERATOR_CODE_CHINA_UNICOM = null;
    private static final String OPERATOR_SSID_CHINA_MOBILE = "CMCC";
    private static final String OPERATOR_SSID_CHINA_TELECOM = "ChinaNet";
    private static final String OPERATOR_SSID_CHINA_UNICOM = "ChinaUnicom";
    public static final String PAGE_ITEM = "page_item";
    private static final int PHONE_NUM_LEN_CN = 11;
    public static final String PORTAL_CHR_DATABASE_COTA = "wifipro_portal_page_items_cota_update.xml";
    public static final String PORTAL_CHR_DATABASE_LOCAL = "wifipro_portal_page_items.xml";
    private static final String TAG = "WiFi_PRO_PortalAutoFillManager";
    public static final String VERSION_NUMBER = "version_number";
    private boolean autoFillPasswordSuccess;
    private String autoFillPhoneNumber;
    private String card1Number;
    private int card1Type;
    private String card2Number;
    private int card2Type;
    private String currentRequestUrl;
    private String currentSsid;
    private boolean dualSimCard;
    private BroadcastReceiver mBroadcastReceiver;
    private ContentResolver mContentResolver;
    private Context mContext;
    private BroadcastReceiver mCotaUpdateReceiver;
    private Handler mHandler;
    IWifiProPortalCallBack mPortalCtrlCallback;
    private PortalDataBaseManager mPortalDataBaseManager;
    private SampleCollectionManager mSampleCollectionManager;
    private TelephonyManager mTelephonyManager;

    /* renamed from: com.android.server.wifi.wifipro.PortalAutoFillManager.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PortalAutoFillManager.MSG_INIT_COLLECTION_DATABASE /*100*/:
                    PortalAutoFillManager.this.initDatabase();
                    break;
                case PortalAutoFillManager.MSG_REQ_AUTO_FILL_PHONE_NUM /*101*/:
                    if (PortalAutoFillManager.this.mHandler.hasMessages(PortalAutoFillManager.MSG_QUERY_AUTO_FILL_SUPPORTED)) {
                        PortalAutoFillManager.this.LOGW("MSG_QUERY_AUTO_FILL_SUPPORTED msg removed");
                        PortalAutoFillManager.this.mHandler.removeMessages(PortalAutoFillManager.MSG_QUERY_AUTO_FILL_SUPPORTED);
                    }
                    PortalAutoFillManager.this.mHandler.sendMessageDelayed(Message.obtain(PortalAutoFillManager.this.mHandler, PortalAutoFillManager.MSG_QUERY_AUTO_FILL_SUPPORTED, (Bundle) msg.obj), PortalAutoFillManager.AUTO_FILL_PHONE_NUM_DELAY_MS);
                    break;
                case PortalAutoFillManager.MSG_QUERY_AUTO_FILL_SUPPORTED /*102*/:
                    PortalAutoFillManager.this.queryAutoFillSupported((Bundle) msg.obj);
                    break;
                case PortalAutoFillManager.MSG_SET_PORTAL_PROPERTY_FLAG /*103*/:
                    PortalAutoFillManager.this.LOGD("MSG_SET_PORTAL_PROPERTY_FLAG, flag = " + msg.arg1);
                    Secure.putInt(PortalAutoFillManager.this.mContentResolver, PortalHtmlParser.PORTAL_NETWORK_FLAG, msg.arg1);
                    break;
                case PortalAutoFillManager.MSG_UPDATE_CHOSEN_PHONE_NUMBER /*104*/:
                    PortalAutoFillManager.this.updateChosenPhoneNumber((String) msg.obj);
                    break;
                case PortalAutoFillManager.MSG_REQUEST_AUTO_FILL_PW /*105*/:
                    PortalAutoFillManager.this.requestAutoFillPassword((String) msg.obj);
                    break;
                case PortalAutoFillManager.MSG_REQUEST_AUTO_LOGIN /*106*/:
                    PortalAutoFillManager.this.requestAutoLogin();
                    break;
                case PortalAutoFillManager.MSG_UPDATE_COTA_DATABASE /*107*/:
                    PortalAutoFillManager.this.updateCotaDatabase();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.PortalAutoFillManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.PortalAutoFillManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.PortalAutoFillManager.<clinit>():void");
    }

    public PortalAutoFillManager(Context context, TelephonyManager tel, IWifiProPortalCallBack cb, SampleCollectionManager sample) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mTelephonyManager = tel;
        this.mPortalDataBaseManager = PortalDataBaseManager.getInstance(context);
        this.mSampleCollectionManager = sample;
        this.mPortalCtrlCallback = cb;
        this.currentSsid = null;
        this.currentRequestUrl = null;
        this.card1Number = null;
        this.card1Type = CARD_TYPE_UNKNOWN;
        this.card2Number = null;
        this.card2Type = CARD_TYPE_UNKNOWN;
        this.dualSimCard = false;
        this.autoFillPasswordSuccess = false;
        init();
    }

    private void init() {
        synchronized (this) {
            HandlerThread handlerThread = new HandlerThread("wifipro_portal_auto_fill_handler_thread");
            handlerThread.start();
            this.mHandler = new AnonymousClass1(handlerThread.getLooper());
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_REQUEST_AUTO_FILL_PHONE_NUM);
        intentFilter.addAction(ACTION_AUTO_FILL_PHONE_NUM_STATUS);
        intentFilter.addAction(ACTION_AUTO_FILL_RANDOM_CODE_STATUS);
        intentFilter.addAction(ACTION_RESEND_CHOSEN_PHONE_NUM);
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                PortalAutoFillManager.this.LOGD("receive broadcast from browser, action = " + intent.getAction());
                if (PortalAutoFillManager.ACTION_REQUEST_AUTO_FILL_PHONE_NUM.equals(intent.getAction())) {
                    Bundle localBundle = new Bundle();
                    String newUrl = intent.getStringExtra(PortalAutoFillManager.FLAG_OVERRIDE_NEW_URL);
                    Bundle custData = intent.getBundleExtra(PortalAutoFillManager.BROWSER_CUSTOM_DATA);
                    localBundle.putString(PortalAutoFillManager.FLAG_OVERRIDE_NEW_URL, newUrl);
                    localBundle.putBundle(PortalAutoFillManager.BROWSER_CUSTOM_DATA, custData);
                    PortalAutoFillManager.this.mHandler.sendMessage(Message.obtain(PortalAutoFillManager.this.mHandler, PortalAutoFillManager.MSG_REQ_AUTO_FILL_PHONE_NUM, localBundle));
                } else if (PortalAutoFillManager.ACTION_AUTO_FILL_PHONE_NUM_STATUS.equals(intent.getAction())) {
                    PortalAutoFillManager.this.handleAutoFillPhoneNumStatus(intent.getBooleanExtra(PortalAutoFillManager.FLAG_AUTO_FILL_STATUS, false));
                } else if (PortalAutoFillManager.ACTION_AUTO_FILL_RANDOM_CODE_STATUS.equals(intent.getAction())) {
                    PortalAutoFillManager.this.handleAutoFillPasswordStatus(intent.getBooleanExtra(PortalAutoFillManager.FLAG_AUTO_FILL_STATUS, false));
                } else if (PortalAutoFillManager.ACTION_RESEND_CHOSEN_PHONE_NUM.equals(intent.getAction())) {
                    PortalAutoFillManager.this.mHandler.sendMessage(Message.obtain(PortalAutoFillManager.this.mHandler, PortalAutoFillManager.MSG_UPDATE_CHOSEN_PHONE_NUMBER, intent.getStringExtra(PortalAutoFillManager.FLAG_PHONE_NUMBER)));
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        IntentFilter cotaUpdateFilter = new IntentFilter(CLOUD_OTA_WIFI_PLUS_UPDATE);
        this.mCotaUpdateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                PortalAutoFillManager.this.LOGD("receive broadcast from cota client, action = " + intent.getAction());
                if (PortalAutoFillManager.CLOUD_OTA_WIFI_PLUS_UPDATE.equals(intent.getAction())) {
                    PortalAutoFillManager.this.mHandler.sendMessage(Message.obtain(PortalAutoFillManager.this.mHandler, PortalAutoFillManager.MSG_UPDATE_COTA_DATABASE));
                }
            }
        };
        this.mContext.registerReceiver(this.mCotaUpdateReceiver, cotaUpdateFilter, CLOUD_OTA_PERMISSION, null);
        this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_INIT_COLLECTION_DATABASE));
    }

    private synchronized void initDatabase() {
        String currVersion;
        if (this.mPortalDataBaseManager.isDatabaseEmpty("COLLECTED_PORTAL_WEB_PAGE_INFO")) {
            LOGD("initDatabase, local db is empty, to update all records from xml.");
            currVersion = parseVersionNumber(PORTAL_CHR_DATABASE_LOCAL, true);
            if (currVersion != null) {
                Secure.putString(this.mContentResolver, LAST_LOCAL_UPDATE_VERSION_KEY, currVersion);
            }
            parseXmlAndInsertDatabase(PORTAL_CHR_DATABASE_LOCAL, true);
        } else {
            String lastVersion = Secure.getString(this.mContentResolver, LAST_LOCAL_UPDATE_VERSION_KEY);
            currVersion = parseVersionNumber(PORTAL_CHR_DATABASE_LOCAL, true);
            if (lastVersion == null || (currVersion != null && Float.valueOf(currVersion).floatValue() > Float.valueOf(lastVersion).floatValue())) {
                LOGD("initDatabase, lastVersion = " + lastVersion + ", currVersion = " + currVersion);
                Secure.putString(this.mContentResolver, LAST_LOCAL_UPDATE_VERSION_KEY, currVersion);
                if (this.mPortalDataBaseManager.removeDbRecords("COLLECTED_PORTAL_WEB_PAGE_INFO")) {
                    this.mHandler.sendEmptyMessageDelayed(MSG_INIT_COLLECTION_DATABASE, AUTO_FILL_PHONE_NUM_DELAY_MS);
                }
            }
        }
    }

    private synchronized void updateCotaDatabase() {
        try {
            String[] ret = HwCfgFilePolicy.getDownloadCfgFile("cloud/wifi_plus", "cloud/wifi_plus");
            if (ret != null && NODE_TYPE_INPUT_INT == ret.length) {
                LOGD("updateCotaDatabase, getDownloadCfgFile version" + ret[NODE_TYPE_SPAN_INT] + ", path = " + ret[NODE_TYPE_UNKNOWN]);
                String path = ret[NODE_TYPE_UNKNOWN] + "/" + PORTAL_CHR_DATABASE_COTA;
                String lastVersion = Secure.getString(this.mContentResolver, LAST_CHR_UPDATE_VERSION_KEY);
                String currVersion = parseVersionNumber(path, false);
                if (lastVersion == null || (currVersion != null && Float.valueOf(currVersion).floatValue() > Float.valueOf(lastVersion).floatValue())) {
                    LOGD("updateCotaDatabase, lastVersion = " + lastVersion + ", currVersion = " + currVersion);
                    Secure.putString(this.mContentResolver, LAST_CHR_UPDATE_VERSION_KEY, currVersion);
                    parseXmlAndInsertDatabase(path, false);
                }
            }
        } catch (NoClassDefFoundError e) {
            LOGD("updateCotaDatabase, NoClassDefFoundError, msg = " + e.getMessage());
        } catch (Exception e2) {
            LOGD("updateCotaDatabase, Exception, msg = " + e2.getMessage());
        }
    }

    private String parseVersionNumber(String fileName, boolean fromAssets) {
        String str = null;
        InputStream inputStream = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            if (fromAssets) {
                inputStream = this.mContext.getAssets().open(fileName);
            } else {
                inputStream = new FileInputStream(new File(fileName));
            }
            xpp.setInput(inputStream, "UTF-8");
            for (int eventType = xpp.getEventType(); eventType != NODE_TYPE_SPAN_INT; eventType = xpp.next()) {
                if (eventType != 0) {
                    if (eventType == NODE_TYPE_INPUT_INT && xpp.getName() != null && xpp.getName().equals(VERSION_NUMBER)) {
                        str = xpp.nextText();
                        LOGD("parseVersionNumber, version = " + str + ", fromAssets = " + fromAssets);
                        break;
                    }
                }
                LOGD("parseVersionNumber, eventType = START_DOCUMENT");
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (XmlPullParserException e2) {
            LOGW("parseVersionNumber, XmlPullParserException msg = " + e2.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                }
            }
        } catch (IOException e4) {
            LOGW("parseVersionNumber, IOException msg = " + e4.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                }
            }
        }
        return str;
    }

    private void parseXmlAndInsertDatabase(String fileName, boolean fromAssets) {
        InputStream inputStream = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            if (fromAssets) {
                inputStream = this.mContext.getAssets().open(fileName);
            } else {
                inputStream = new FileInputStream(new File(fileName));
            }
            xpp.setInput(inputStream, "UTF-8");
            PortalWebPageInfo portalWebPageInfo = null;
            for (int eventType = xpp.getEventType(); eventType != NODE_TYPE_SPAN_INT; eventType = xpp.next()) {
                if (eventType == 0) {
                    LOGD("eventType = START_DOCUMENT");
                } else if (eventType == NODE_TYPE_INPUT_INT) {
                    if (xpp.getName() == null || !xpp.getName().equals(PAGE_ITEM)) {
                        if (!(xpp.getName() == null || portalWebPageInfo == null)) {
                            if (xpp.getName().equals(ITEM_SSID)) {
                                portalWebPageInfo.ssid = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_URL)) {
                                portalWebPageInfo.url = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_PHONE_NUM_ID)) {
                                portalWebPageInfo.phoneNumInputId = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_PHONE_NUM_NAME)) {
                                portalWebPageInfo.phoneNumInputName = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_PW_ID)) {
                                portalWebPageInfo.smsPwInputId = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_PW_NAME)) {
                                portalWebPageInfo.smsPwInputName = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_PW_VALUE)) {
                                portalWebPageInfo.smsPwInputValue = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_LOGIN_ID)) {
                                portalWebPageInfo.loginBtnId = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_LOGIN_NAME)) {
                                portalWebPageInfo.loginBtnName = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_LOGIN_VALUE)) {
                                portalWebPageInfo.loginBtnValue = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_LOGIN_NODE_TYPE)) {
                                portalWebPageInfo.loginNodeType = xpp.nextText();
                            } else if (xpp.getName().equals(ITEM_UPDATE_ONLY)) {
                                String updateOnly = xpp.nextText();
                                portalWebPageInfo.updateOnly = updateOnly != null ? updateOnly.equals("true") : false;
                            }
                        }
                    } else if (portalWebPageInfo == null) {
                        portalWebPageInfo = new PortalWebPageInfo();
                    } else {
                        portalWebPageInfo.ssid = "";
                        portalWebPageInfo.url = "";
                        portalWebPageInfo.phoneNumInputId = "";
                        portalWebPageInfo.phoneNumInputName = "";
                        portalWebPageInfo.smsPwInputId = "";
                        portalWebPageInfo.smsPwInputName = "";
                        portalWebPageInfo.smsPwInputValue = "";
                        portalWebPageInfo.htmlBtnNumber = "-1";
                        portalWebPageInfo.bssid = "";
                        portalWebPageInfo.cellid = "-1";
                        portalWebPageInfo.sndBtnId = "";
                        portalWebPageInfo.loginBtnId = "";
                        portalWebPageInfo.loginBtnName = "";
                        portalWebPageInfo.loginBtnValue = "";
                        portalWebPageInfo.loginNodeType = "";
                        portalWebPageInfo.updateOnly = false;
                    }
                } else if (eventType == NODE_TYPE_A_INT && xpp.getName() != null && portalWebPageInfo != null && xpp.getName().equals(PAGE_ITEM)) {
                    if (portalWebPageInfo.updateOnly) {
                        this.mPortalDataBaseManager.updateTable(portalWebPageInfo, "COLLECTED_PORTAL_WEB_PAGE_INFO");
                    } else {
                        this.mPortalDataBaseManager.insertTable(portalWebPageInfo, "COLLECTED_PORTAL_WEB_PAGE_INFO");
                    }
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (XmlPullParserException e2) {
            LOGW("initDatabase, XmlPullParserException msg = " + e2.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                }
            }
        } catch (IOException e4) {
            LOGW("initDatabase, IOException msg = " + e4.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                }
            }
        }
    }

    private void initPhoneNumber() {
        String operator;
        boolean z;
        boolean z2 = false;
        this.card1Number = this.mTelephonyManager.getLine1Number();
        if (this.card1Number != null) {
            if (this.card1Number.startsWith(MOBILE_CODE_CN)) {
                this.card1Number = this.card1Number.replace(MOBILE_CODE_CN, "");
            }
            operator = this.mTelephonyManager.getNetworkOperator();
            if (operator != null) {
                if (matchOperator(operator, OPERATOR_CODE_CHINA_MOBILE)) {
                    this.card1Type = NODE_TYPE_UNKNOWN;
                } else if (matchOperator(operator, OPERATOR_CODE_CHINA_UNICOM)) {
                    this.card1Type = NODE_TYPE_SPAN_INT;
                } else if (matchOperator(operator, OPERATOR_CODE_CHINA_TELECOM)) {
                    this.card1Type = NODE_TYPE_INPUT_INT;
                }
            }
        }
        int defaultSubid = SubscriptionManager.getDefaultSubId();
        int[] subId = SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList();
        if (subId.length == NODE_TYPE_INPUT_INT) {
            this.dualSimCard = true;
            int card2SubId = subId[NODE_TYPE_UNKNOWN] == defaultSubid ? subId[NODE_TYPE_SPAN_INT] : subId[NODE_TYPE_UNKNOWN];
            this.card2Number = this.mTelephonyManager.getLine1Number(card2SubId);
            if (this.card2Number != null) {
                if (this.card2Number.startsWith(MOBILE_CODE_CN)) {
                    this.card2Number = this.card2Number.replace(MOBILE_CODE_CN, "");
                }
                operator = this.mTelephonyManager.getNetworkOperatorForPhone(SubscriptionManager.getPhoneId(card2SubId));
                if (operator != null) {
                    if (matchOperator(operator, OPERATOR_CODE_CHINA_MOBILE)) {
                        this.card2Type = NODE_TYPE_UNKNOWN;
                    } else if (matchOperator(operator, OPERATOR_CODE_CHINA_UNICOM)) {
                        this.card2Type = NODE_TYPE_SPAN_INT;
                    } else if (matchOperator(operator, OPERATOR_CODE_CHINA_TELECOM)) {
                        this.card2Type = NODE_TYPE_INPUT_INT;
                    }
                }
            }
        }
        StringBuilder append = new StringBuilder().append("init phone status, num1 = ");
        if (TextUtils.isEmpty(this.card1Number)) {
            z = false;
        } else {
            z = true;
        }
        StringBuilder append2 = append.append(z).append(", num2 = ");
        if (!TextUtils.isEmpty(this.card2Number)) {
            z2 = true;
        }
        LOGD(append2.append(z2).append(", type1 = ").append(this.card1Type).append(", type2 = ").append(this.card2Type).append(", dual = ").append(this.dualSimCard).toString());
    }

    private synchronized void updateChosenPhoneNumber(String newPhoneNumber) {
        if (newPhoneNumber != null) {
            if (newPhoneNumber.length() >= PHONE_NUM_LEN_CN && this.currentSsid != null) {
                Secure.putString(this.mContext.getContentResolver(), LAST_INPUT_PHONE_NUMBER, newPhoneNumber);
                this.mPortalDataBaseManager.updateLastInputPhoneNumBySsid(this.currentSsid, newPhoneNumber);
            }
        }
    }

    private boolean matchOperator(String operator, String[] codes) {
        for (int i = NODE_TYPE_UNKNOWN; i < codes.length; i += NODE_TYPE_SPAN_INT) {
            if (operator.equals(codes[i])) {
                return true;
            }
        }
        return false;
    }

    protected synchronized void handleAutoFillPhoneNumStatus(boolean success) {
        boolean z = false;
        synchronized (this) {
            StringBuilder append = new StringBuilder().append("handleAutoFillPhoneNumStatus, success = ").append(success).append(", currentSsid = ").append(this.currentSsid).append(", pn = ");
            if (!TextUtils.isEmpty(this.autoFillPhoneNumber)) {
                z = true;
            }
            LOGD(append.append(z).toString());
            if (this.currentSsid != null) {
                if (success && !TextUtils.isEmpty(this.autoFillPhoneNumber)) {
                    WifiProStatisticsManager.getInstance().uploadPortalAutoFillStatus(success, NODE_TYPE_UNKNOWN);
                } else if (!(success || this.currentRequestUrl == null)) {
                    this.mSampleCollectionManager.requestCollectPortalHtml(this.currentRequestUrl);
                }
            }
        }
    }

    protected synchronized void handleAutoFillPasswordStatus(boolean success) {
        LOGD("handleAutoFillPasswordStatus, success = " + success + ", currentSsid = " + this.currentSsid);
        if (this.currentSsid != null) {
            if (success) {
                this.autoFillPasswordSuccess = true;
                this.mHandler.sendEmptyMessageDelayed(MSG_REQUEST_AUTO_LOGIN, AUTO_LOGIN_DELAY_MS);
                WifiProStatisticsManager.getInstance().uploadPortalAutoFillStatus(success, NODE_TYPE_SPAN_INT);
            } else if (this.currentRequestUrl != null) {
                this.mSampleCollectionManager.requestCollectPortalHtml(this.currentRequestUrl);
            }
        }
    }

    private synchronized void requestAutoLogin() {
        if (this.currentSsid != null) {
            Intent intent = new Intent(ACTION_REQUEST_AUTO_LOGIN);
            LOGD("requestAutoLogin, currentSsid = " + this.currentSsid + ", loginBtnId = " + this.mPortalDataBaseManager.getLoginBtnId());
            if (!TextUtils.isEmpty(this.mPortalDataBaseManager.getLoginBtnId())) {
                intent.putExtra(FLAG_LOGIN_BTN_ID, this.mPortalDataBaseManager.getLoginBtnId());
            } else if (!TextUtils.isEmpty(this.mPortalDataBaseManager.getLoginBtnName())) {
                intent.putExtra(FLAG_LOGIN_BTN_NAME, this.mPortalDataBaseManager.getLoginBtnName());
                LOGD("requestAutoFillPassword, FLAG_LOGIN_BTN_NAME, btn name = " + this.mPortalDataBaseManager.getLoginBtnName());
            } else if (!TextUtils.isEmpty(this.mPortalDataBaseManager.getLoginBtnValue())) {
                int nodeType;
                if (NODE_INPUT_STRING.equals(this.mPortalDataBaseManager.getLoginBtnNodeType())) {
                    nodeType = NODE_TYPE_INPUT_INT;
                } else if (NODE_SPAN_STRING.equals(this.mPortalDataBaseManager.getLoginBtnNodeType())) {
                    nodeType = NODE_TYPE_SPAN_INT;
                } else if (NODE_A_STRING.equals(this.mPortalDataBaseManager.getLoginBtnNodeType())) {
                    nodeType = NODE_TYPE_A_INT;
                } else {
                    return;
                }
                intent.putExtra(FLAG_LOGIN_BTN_VALUE, this.mPortalDataBaseManager.getLoginBtnValue());
                intent.putExtra(FLAG_LOGIN_BTN_NODE_TYPE, nodeType);
                LOGD("requestAutoFillPassword, FLAG_LOGIN_BTN_VALUE, btn value = " + this.mPortalDataBaseManager.getLoginBtnValue());
                LOGD("requestAutoFillPassword, FLAG_LOGIN_BTN_NODE_TYPE, btn node type = " + nodeType);
            } else {
                return;
            }
            intent.setPackage(BROWSER_PACKET_NAME);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private String getUserLastInputPhoneNumber(String currentSsid) {
        String phoneNum = this.mPortalDataBaseManager.queryLastInputPhoneNumBySsid(currentSsid);
        if (phoneNum == null) {
            return Secure.getString(this.mContext.getContentResolver(), LAST_INPUT_PHONE_NUMBER);
        }
        return phoneNum;
    }

    private String getBestPhoneNumberBySsid(String currentSsid) {
        if (this.card1Number == null && this.card2Number == null) {
            initPhoneNumber();
        }
        String phoneNum = getUserLastInputPhoneNumber(currentSsid);
        if (phoneNum == null) {
            if (currentSsid.startsWith(OPERATOR_SSID_CHINA_MOBILE)) {
                if (this.card1Number != null && this.card1Type == 0) {
                    return this.card1Number;
                }
                if (this.card2Number != null && this.card2Type == 0) {
                    return this.card2Number;
                }
            } else if (currentSsid.startsWith(OPERATOR_SSID_CHINA_UNICOM)) {
                if (this.card1Number != null && this.card1Type == NODE_TYPE_SPAN_INT) {
                    return this.card1Number;
                }
                if (this.card2Number != null && this.card2Type == NODE_TYPE_SPAN_INT) {
                    return this.card2Number;
                }
            } else if (!currentSsid.startsWith(OPERATOR_SSID_CHINA_TELECOM)) {
                LOGD("getBestPhoneNumberBySsid, not a operator portal network, currentSsid = " + currentSsid);
                if (this.card1Number != null) {
                    return this.card1Number;
                }
                if (this.card2Number != null) {
                    return this.card2Number;
                }
            } else if (this.card1Number != null && this.card1Type == NODE_TYPE_INPUT_INT) {
                return this.card1Number;
            } else {
                if (this.card2Number != null && this.card2Type == NODE_TYPE_INPUT_INT) {
                    return this.card2Number;
                }
            }
        }
        return phoneNum;
    }

    public synchronized void queryAutoFillSupported(Bundle bundle) {
        boolean z = false;
        Intent intent = new Intent(ACTION_RESP_AUTO_FILL_PHONE_NUM);
        if (this.currentSsid != null) {
            String newUrl = bundle.getString(FLAG_OVERRIDE_NEW_URL);
            if (urlValid(newUrl)) {
                this.currentRequestUrl = newUrl;
                z = this.mPortalDataBaseManager.isMatchedByCollectionTable(this.currentSsid, newUrl);
                if (z) {
                    this.autoFillPhoneNumber = getBestPhoneNumberBySsid(this.currentSsid);
                    if (!TextUtils.isEmpty(this.mPortalDataBaseManager.getPhoneNumInputId())) {
                        intent.putExtra(FLAG_PHONE_NUM_INPUT_ID, this.mPortalDataBaseManager.getPhoneNumInputId());
                    } else if (!TextUtils.isEmpty(this.mPortalDataBaseManager.getPhoneNumInputName())) {
                        intent.putExtra(FLAG_PHONE_NUM_INPUT_NAME, this.mPortalDataBaseManager.getPhoneNumInputName());
                    }
                    if (TextUtils.isEmpty(this.autoFillPhoneNumber)) {
                        this.autoFillPhoneNumber = null;
                    }
                    intent.putExtra(FLAG_PHONE_NUMBER, this.autoFillPhoneNumber);
                    LOGD("queryAutoFillSupported, number = " + (!TextUtils.isEmpty(this.autoFillPhoneNumber)) + ", id = " + this.mPortalDataBaseManager.getPhoneNumInputId());
                    this.mPortalCtrlCallback.onStartReceiveSms();
                } else {
                    this.mSampleCollectionManager.requestCollectPortalHtml(this.currentRequestUrl);
                }
            }
        }
        intent.putExtra(FLAG_SUPPORT_AUTO_FILL, z);
        intent.putExtra(BROWSER_CUSTOM_DATA, bundle.getBundle(BROWSER_CUSTOM_DATA));
        LOGD("queryAutoFillSupported, supported = " + z + ", currentSsid = " + this.currentSsid);
        intent.setPackage(BROWSER_PACKET_NAME);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean urlValid(String newUrl) {
        if (newUrl == null || (!newUrl.startsWith("http://") && !newUrl.startsWith("https://"))) {
            return false;
        }
        return true;
    }

    public synchronized void notifyParsedPassword(String code) {
        LOGD("notifyParsedPassword, currentSsid = " + this.currentSsid + ", supported = " + this.mPortalDataBaseManager.isAutoFillSupported());
        if (!(TextUtils.isEmpty(code) || this.currentSsid == null || !this.mPortalDataBaseManager.isAutoFillSupported())) {
            if (this.mHandler.hasMessages(MSG_REQUEST_AUTO_FILL_PW)) {
                LOGW("notifyParsedPassword, receive more than one SMS, replace with the new one.");
                this.mHandler.removeMessages(MSG_REQUEST_AUTO_FILL_PW);
                this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_REQUEST_AUTO_FILL_PW, code));
            } else {
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_REQUEST_AUTO_FILL_PW, code), AUTO_FILL_PW_DELAY_MS);
            }
        }
    }

    private synchronized void requestAutoFillPassword(String code) {
        LOGD("requestAutoFillPassword, supported = " + this.mPortalDataBaseManager.isAutoFillSupported() + ", passwordInputId = " + this.mPortalDataBaseManager.getPasswordInputId() + ", passwordInputName = " + this.mPortalDataBaseManager.getPasswordInputName() + ", value = " + this.mPortalDataBaseManager.getPasswordInputValue());
        if (this.currentSsid != null && this.mPortalDataBaseManager.isAutoFillSupported()) {
            Intent intent = new Intent(ACTION_REQUEST_AUTO_FILL_RANDOM_CODE);
            if (!TextUtils.isEmpty(this.mPortalDataBaseManager.getPasswordInputId())) {
                intent.putExtra(FLAG_RANDOM_CODE_INPUT_ID, this.mPortalDataBaseManager.getPasswordInputId());
            } else if (!TextUtils.isEmpty(this.mPortalDataBaseManager.getPasswordInputName())) {
                intent.putExtra(FLAG_RANDOM_CODE_INPUT_NAME, this.mPortalDataBaseManager.getPasswordInputName());
            } else if (!TextUtils.isEmpty(this.mPortalDataBaseManager.getPasswordInputValue())) {
                intent.putExtra(FLAG_RANDOM_CODE_INPUT_VALUE, this.mPortalDataBaseManager.getPasswordInputValue());
                LOGD("requestAutoFillPassword, FLAG_RANDOM_CODE_INPUT_VALUE");
            }
            intent.putExtra(FLAG_RANDOM_CODE, code);
            intent.setPackage(BROWSER_PACKET_NAME);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public synchronized void notifyPortalAuthenStatus(boolean isPortal, boolean success) {
        if (isPortal) {
            if (success) {
                WifiProStatisticsManager.getInstance().increasePortalConnectedAndAuthenCnt();
                if (this.autoFillPasswordSuccess && !TextUtils.isEmpty(this.mPortalDataBaseManager.getLoginBtnId())) {
                    WifiProStatisticsManager.getInstance().uploadPortalAutoFillStatus(true, NODE_TYPE_INPUT_INT);
                }
            }
            this.mPortalCtrlCallback.onStopReceiveSms();
            this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_SET_PORTAL_PROPERTY_FLAG, NODE_TYPE_UNKNOWN, NODE_TYPE_UNKNOWN, null));
        }
    }

    public synchronized void notifyPortalConnected(String ssid, String bssid) {
        WifiProStatisticsManager.getInstance().increasePortalConnectedCnt();
        this.currentSsid = ssid;
        LOGD("notifyPortalConnected(), ssid = " + this.currentSsid);
        this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_SET_PORTAL_PROPERTY_FLAG, NODE_TYPE_SPAN_INT, NODE_TYPE_UNKNOWN, null));
    }

    public synchronized void handleWifiProStatusChanged(boolean enabled, boolean portal) {
        LOGD("Enter: handleWifiProStatusChanged(), enabled = " + enabled + ", portal = " + portal + ", currentSsid = " + this.currentSsid);
        if (portal && this.currentSsid != null) {
            if (enabled) {
                this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_SET_PORTAL_PROPERTY_FLAG, NODE_TYPE_SPAN_INT, NODE_TYPE_UNKNOWN, null));
            } else {
                this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_SET_PORTAL_PROPERTY_FLAG, NODE_TYPE_UNKNOWN, NODE_TYPE_UNKNOWN, null));
            }
        }
    }

    public synchronized void handleWifiDisconnected(boolean portal) {
        LOGD("Enter: handleWifiDisconnected(), portal = " + portal + ", currentSsid = " + this.currentSsid);
        this.currentSsid = null;
        this.currentRequestUrl = null;
        this.autoFillPhoneNumber = null;
        this.autoFillPasswordSuccess = false;
        this.mPortalCtrlCallback.onStopReceiveSms();
        this.mPortalDataBaseManager.handlePortalDisconnected();
        if (portal) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_SET_PORTAL_PROPERTY_FLAG, NODE_TYPE_UNKNOWN, NODE_TYPE_UNKNOWN, null));
        }
    }

    public synchronized String getCurrentSsid() {
        return this.currentSsid;
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
