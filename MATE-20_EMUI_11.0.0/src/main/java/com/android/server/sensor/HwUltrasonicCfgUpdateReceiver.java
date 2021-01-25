package com.android.server.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.SystemSensorManager;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwUltrasonicCfgUpdateReceiver extends BroadcastReceiver {
    private static final String OUC_LOCATION = "/data/cota/para/xml/sensor/ultrasonic/whitelist.xml";
    private static final String PRE_LOCATION = "xml/sensor/ultrasonic/whitelist.xml";
    private static final String TAG = "UltrasonicCfg_Update";
    private List<String> mPackageWhiteList = new ArrayList();

    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, Intent intent) {
        if (context != null && intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Slog.i(TAG, "action:" + action);
            new Thread(new Runnable() {
                /* class com.android.server.sensor.HwUltrasonicCfgUpdateReceiver.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwUltrasonicCfgUpdateReceiver.this.loadConfig(context);
                }
            }).start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadConfig(Context context) {
        File oucConfigfile = new File(OUC_LOCATION);
        if (oucConfigfile.exists()) {
            Slog.i(TAG, "LoadConfig OUC Configfile success");
            loadConfig(oucConfigfile, context);
            return;
        }
        Slog.i(TAG, "Load OUC configfile failure, Try to load preset configfile");
        File preConfigfile = HwCfgFilePolicy.getCfgFile(PRE_LOCATION, 0);
        if (preConfigfile != null) {
            Slog.i(TAG, "LoadConfig preset configfile success");
            loadConfig(preConfigfile, context);
            return;
        }
        Slog.i(TAG, "load Config Fail");
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0029 A[Catch:{ FileNotFoundException -> 0x001e, XmlPullParserException -> 0x001b, IOException -> 0x0018, all -> 0x0015 }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0091 A[SYNTHETIC, Splitter:B:32:0x0091] */
    /* JADX WARNING: Removed duplicated region for block: B:58:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private void loadConfig(File configfile, Context context) {
        InputStream inputStream = null;
        if (configfile != null) {
            try {
                if (configfile.exists()) {
                    inputStream = new FileInputStream(configfile);
                    if (inputStream != null) {
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(inputStream, null);
                        int xmlEventType = xmlParser.next();
                        while (true) {
                            if (xmlEventType == 1) {
                                break;
                            }
                            if (xmlEventType != 2 || !"whitelisted-app".equals(xmlParser.getName())) {
                                if (xmlEventType == 3 && "config".equals(xmlParser.getName())) {
                                    break;
                                }
                            } else {
                                addPackageToWhiteList(xmlParser.getAttributeValue(null, "package"));
                            }
                            xmlEventType = xmlParser.next();
                        }
                    }
                    int result = ((SystemSensorManager) context.getSystemService("sensor")).hwSetPackageWhiteListImpl(this.mPackageWhiteList);
                    Slog.i(TAG, "send the whitelist result : " + result);
                    if (inputStream == null) {
                        try {
                            inputStream.close();
                            return;
                        } catch (IOException e) {
                            Log.e(TAG, "load config: IO Exception while closing stream");
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "loadConfig FileNotFoundException.");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (XmlPullParserException e3) {
                Log.e(TAG, "loadConfig XmlPullParserException");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (IOException e4) {
                Log.e(TAG, "loadConfig IOException");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "load config: IO Exception while closing stream");
                    }
                }
                throw th;
            }
        }
        Slog.i(TAG, "whitelist is not exist");
        if (inputStream != null) {
        }
        int result2 = ((SystemSensorManager) context.getSystemService("sensor")).hwSetPackageWhiteListImpl(this.mPackageWhiteList);
        Slog.i(TAG, "send the whitelist result : " + result2);
        if (inputStream == null) {
        }
    }

    private void addPackageToWhiteList(String packageName) {
        if ("".equals(packageName) || packageName == null) {
            Slog.i(TAG, "packageName is null or empty");
        } else if (!this.mPackageWhiteList.contains(packageName)) {
            Slog.i(TAG, "add packageName to whitelist : " + packageName);
            this.mPackageWhiteList.add(packageName);
        }
    }
}
