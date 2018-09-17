package com.android.server.wifi.wifipro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.HwWifiCHRConstImpl;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.WifiProCHRManager;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SampleCollectionManager {
    private static final String ACTION_REQUEST_AUTO_FILL_PHONE_NUM = "com.huawei.wifiplus.ACTION_REQUEST_AUTO_FILL_PHONE_NUM";
    private static final boolean CHR_PORTAL_HTML_ENABLE = false;
    public static final String CHR_TITLE_AUTH_MSG_COLLECTE = "WIFI_PORTAL_AUTH_MSG_COLLECTE";
    public static final String CHR_TITLE_SAMPLE_COLLECTE = "WIFI_PORTAL_SAMPLES_COLLECTE";
    public static final boolean DBG = true;
    private static final String FLAG_OVERRIDE_NEW_URL = "com.huawei.wifiplus.FLAG_OVERRIDE_NEW_URL";
    public static final int MSG_PARSE_PORTAL_HTML = 101;
    private static final int MSG_SMS_RECEIVED = 100;
    private static final String SECRET_SMS_RECEIVED_INTENT_ACTION = "android.provider.Telephony.INTERCEPTION_SMS_RECEIVED";
    private static final String SMS_RECEIVED_INTENT_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "WiFi_PRO_SampleCollectionManager";
    private static boolean mIsEnbleSampleCHR;
    private final String[] INVALID_PORTAL_URL;
    private String currentBssid;
    private String currentSsid;
    private BroadcastReceiver mBroadcastReceiver;
    private ConnectivityManager mConnManager;
    private Context mContext;
    private Handler mHandler;
    private IntentFilter mIntentFilter;
    private AtomicBoolean mIsPortalAuthenticating;
    private boolean mIsReceiverRegisted;
    private PortalDataBaseManager mPortalDataBaseManager;
    private WiFiProAuthCodeParser mWiFiProAuthCodeParser;
    private WifiProCHRManager mWiFiProCHRMgr;
    private WifiProStatisticsManager mWifiProStatisticsManager;

    /* renamed from: com.android.server.wifi.wifipro.SampleCollectionManager.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SampleCollectionManager.MSG_SMS_RECEIVED /*100*/:
                    SampleCollectionManager.this.parseSms(msg.obj);
                    break;
                case SampleCollectionManager.MSG_PARSE_PORTAL_HTML /*101*/:
                    SampleCollectionManager.this.parsePortalWebHtml((String) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.SampleCollectionManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.SampleCollectionManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.SampleCollectionManager.<clinit>():void");
    }

    public SampleCollectionManager(Context context, WifiProConfigurationManager config) {
        this.INVALID_PORTAL_URL = new String[]{"http://m.sohu.com/", "https://m.sohu.com/", "http://www.baidu.com/", "https://www.baidu.com/", "http://v.qq.com/", "http://love.163.com/", "http://m.news.baidu.com/news?", "http://so.m.sm.cn/", "https://so.m.sm.cn/", "http://m.huawei.com/", "http://i.youku.com", "http://3g.ganji.com/", "https://passport.tianya.cn", "http://m.yz.sm.cn/", "http://ent.163.com/", "http://film.qq.com/", "http://mysh.qq.com/", "http://gw.buaa.edu.cn/", "http://ecard.nankai.edu.cn/", "http://www.xinhuanet.com/", "http://g.pconline.com.cn/", "http://m.homeinns.com/", "http://auto.sina.cn/", "http://www.lib.ruc.edu.cn/", "https://wap.sogou.com/", "http://zhidao.baidu.com/", "http://finance.21cn.com/"};
        this.mContext = context;
        this.mConnManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mIsPortalAuthenticating = new AtomicBoolean(CHR_PORTAL_HTML_ENABLE);
        this.mWiFiProCHRMgr = WifiProCHRManager.getInstance();
        this.mPortalDataBaseManager = PortalDataBaseManager.getInstance(context);
        this.mWiFiProAuthCodeParser = new WiFiProAuthCodeParser(config);
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        init();
        LOGD("SampleCollectionManager init Complete! ");
    }

    private void init() {
        HandlerThread handlerThread = new HandlerThread("wifipro_sample_handler_thread");
        handlerThread.start();
        this.mHandler = new AnonymousClass1(handlerThread.getLooper());
    }

    public void registerBroadcastReceiver() {
        if (!this.mIsReceiverRegisted) {
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    SampleCollectionManager.this.LOGD("registerBroadcastReceiver, action = " + intent.getAction());
                    String action = intent.getAction();
                    if (SampleCollectionManager.SMS_RECEIVED_INTENT_ACTION.equals(action) || SampleCollectionManager.SECRET_SMS_RECEIVED_INTENT_ACTION.equals(action)) {
                        if (SampleCollectionManager.this.mIsPortalAuthenticating.get()) {
                            SampleCollectionManager.this.mHandler.sendMessage(SampleCollectionManager.this.mHandler.obtainMessage(SampleCollectionManager.MSG_SMS_RECEIVED, intent));
                        }
                    } else if (SampleCollectionManager.ACTION_REQUEST_AUTO_FILL_PHONE_NUM.equals(action)) {
                        synchronized (this) {
                            if (SampleCollectionManager.this.currentSsid != null) {
                                SampleCollectionManager.this.mHandler.sendMessage(Message.obtain(SampleCollectionManager.this.mHandler, SampleCollectionManager.MSG_PARSE_PORTAL_HTML, 0, 0, intent.getStringExtra(SampleCollectionManager.FLAG_OVERRIDE_NEW_URL)));
                            }
                        }
                    }
                }
            };
            this.mIntentFilter = new IntentFilter();
            this.mIntentFilter.addAction(SMS_RECEIVED_INTENT_ACTION);
            this.mIntentFilter.addAction(SECRET_SMS_RECEIVED_INTENT_ACTION);
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
            this.mIsReceiverRegisted = DBG;
        }
    }

    public void unRegisterBroadcastReceiver() {
        if (this.mIsReceiverRegisted && this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
            this.mIsReceiverRegisted = CHR_PORTAL_HTML_ENABLE;
        }
    }

    public void setPortalAuthenticating(boolean isPortalAuthenticating) {
        this.mIsPortalAuthenticating.set(isPortalAuthenticating);
    }

    private void parseSms(Intent intent) {
        if (mIsEnbleSampleCHR) {
            String[] authSms = this.mWiFiProAuthCodeParser.obtainAuthSms(intent);
            if (authSms.length == 2) {
                String sms_num = authSms[0];
                String sms_body = authSms[1];
                LOGD("isCommercialUser = " + WifiProCHRManager.isCommercialUser());
                if (this.mWiFiProAuthCodeParser.isPortalAuthSms(sms_num)) {
                    this.mWifiProStatisticsManager.increaseRcvSMS_Count();
                    if (this.mWiFiProAuthCodeParser.parsePortalAuthCode(sms_body) != null) {
                        this.mWifiProStatisticsManager.increasePortalCodeParseCount();
                        return;
                    } else if (WifiProCHRManager.isCommercialUser()) {
                        String commercialSms_num = this.mWiFiProAuthCodeParser.replaceCommercialUserSmsNum(sms_num);
                        String commercialSms_body = this.mWiFiProAuthCodeParser.replaceCommercialUserSmsBody(sms_body);
                        if (!(TextUtils.isEmpty(commercialSms_num) || TextUtils.isEmpty(commercialSms_body))) {
                            this.mWiFiProCHRMgr.updatePortalAutSms(commercialSms_num, commercialSms_body.getBytes(), commercialSms_body.length());
                            this.mWiFiProCHRMgr.updateWifiException(HwWifiCHRConstImpl.WIFI_PORTAL_AUTH_MSG_COLLECTE, CHR_TITLE_AUTH_MSG_COLLECTE);
                        }
                    }
                }
                if (!WifiProCHRManager.isCommercialUser()) {
                    this.mWiFiProCHRMgr.updatePortalAutSms(sms_num, sms_body.getBytes(), sms_body.length());
                    this.mWiFiProCHRMgr.updateWifiException(HwWifiCHRConstImpl.WIFI_PORTAL_AUTH_MSG_COLLECTE, CHR_TITLE_AUTH_MSG_COLLECTE);
                }
            }
        }
    }

    private void doUploadPortalWebPageChr(Map<String, String> webChrItems, List<String> keyLines) {
        if (this.mPortalDataBaseManager.needUploadChr(PortalDataBaseManager.createPortalInfo(webChrItems))) {
            this.mWiFiProCHRMgr.updatePortalAPInfo(((String) webChrItems.get("AP_SSID")).getBytes(StandardCharsets.UTF_8), "", "", ((String) webChrItems.get("AP_SSID")).length());
            String num = (String) webChrItems.get("AP_Btn_Number");
            WifiProCHRManager wifiProCHRManager = this.mWiFiProCHRMgr;
            String str = (String) webChrItems.get("AP_URL");
            String str2 = (String) webChrItems.get("AP_Phone_Number_ID");
            String str3 = (String) webChrItems.get("AP_Send_Button_ID");
            String str4 = (String) webChrItems.get("AP_Code_Input_ID");
            String str5 = (String) webChrItems.get("AP_Login_Button_ID");
            int parseInt = (num == null || num.length() <= 0) ? -1 : Integer.parseInt(num);
            wifiProCHRManager.updatePortalWebpageInfo(str, str2, str3, str4, str5, parseInt);
            this.mWiFiProCHRMgr.updatePortalKeyLines(Arrays.toString(keyLines.toArray()).getBytes(StandardCharsets.UTF_8));
            LOGD("doUploadPortalWebPageChr, start to upload it......");
            this.mWiFiProCHRMgr.updateWifiException(HwWifiCHRConstImpl.WIFI_PORTAL_SAMPLES_COLLECTE, CHR_TITLE_SAMPLE_COLLECTE);
        }
    }

    private void parsePortalWebHtml(String newUrl) {
        LOGD("parsePortalWebHtml(), newUrl = " + newUrl);
        if (!TextUtils.isEmpty(newUrl)) {
            Map<String, String> webChrItems = new HashMap();
            String downloadHtml = PortalHtmlParser.downloadPortalWebHtml(this.mConnManager, newUrl);
            if (downloadHtml != null) {
                List<String> keyLines = PortalHtmlParser.parsePortalWebHtml(downloadHtml, webChrItems);
                synchronized (this) {
                    int number = PortalHtmlParser.getInputNumber(keyLines);
                    if (number < 2 || this.currentSsid == null) {
                        LOGD("parsePortalWebHtml(), unmatched min input number, just ignore it.");
                    } else {
                        webChrItems.put("AP_Btn_Number", String.valueOf(number));
                        webChrItems.put("AP_URL", newUrl);
                        webChrItems.put("AP_SSID", this.currentSsid);
                        webChrItems.put("AP_BSSID", this.currentBssid);
                        doUploadPortalWebPageChr(webChrItems, keyLines);
                    }
                }
            } else {
                LOGD("parsePortalWebHtml(), downloadHtml = null, maybe IOException happened!");
            }
        }
    }

    private boolean isInvalidPortalUrl(String url) {
        if (url != null) {
            for (String startsWith : this.INVALID_PORTAL_URL) {
                if (url.startsWith(startsWith)) {
                    return DBG;
                }
            }
        }
        return CHR_PORTAL_HTML_ENABLE;
    }

    public synchronized void requestCollectPortalHtml(String url) {
        if (!(this.currentSsid == null || url == null || isInvalidPortalUrl(url))) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_PARSE_PORTAL_HTML, 0, 0, url));
        }
    }

    public synchronized void notifyPortalConnected(String ssid, String bssid) {
        this.currentSsid = ssid;
        this.currentBssid = bssid;
    }

    public synchronized void notifyWifiDisconnected(boolean portal) {
        this.currentSsid = null;
        this.currentBssid = null;
    }

    public static void setSampleChrEnable(boolean enble) {
        mIsEnbleSampleCHR = enble;
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
