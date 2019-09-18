package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class HwGpsLogController {
    private static final String BCM_47531 = "47531";
    private static final String BCM_4774 = "4774";
    private static final long CHECK_GPS_LOG_INTERVEL = 30000;
    private static final String CHIP_TYPE = "ro.connectivity.chiptype";
    private static final boolean DBG = true;
    private static final String GPS_CONFIG_PATH = "data/gps/gpsconfig.xml";
    private static final String GPS_LOG_ENABLE = "gps_log_enable";
    private static final String GPS_LOG_STATUS_CLOSE = "com.android.huawei.log.GPS_LOG_STATUS_CLOSE";
    private static final String GPS_LOG_STATUS_OPEN = "com.android.huawei.log.GPS_LOG_STATUS_OPEN";
    private static final String GPS_PATH_IC_TYPE = "/proc/device-tree/gps_power/broadcom_config,ic_type";
    private static final int MSG_CHECK_GPS_LOG_STATUS = 1103;
    private static final int MSG_GPS_LOG_STATUS_CLOSE = 1102;
    private static final int MSG_GPS_LOG_STATUS_OPEN = 1101;
    private static final String TAG = "GpsLogController";
    private static HwGpsLogController mGpsLogController;
    private String mBcmChipType;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            Log.d(HwGpsLogController.TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case HwGpsLogController.MSG_GPS_LOG_STATUS_OPEN /*1101*/:
                    Settings.Global.putString(HwGpsLogController.this.mContext.getContentResolver(), HwGpsLogController.GPS_LOG_ENABLE, Boolean.toString(true));
                    HwGpsLogController.this.operatorLog(true);
                    return true;
                case HwGpsLogController.MSG_GPS_LOG_STATUS_CLOSE /*1102*/:
                    Settings.Global.putString(HwGpsLogController.this.mContext.getContentResolver(), HwGpsLogController.GPS_LOG_ENABLE, Boolean.toString(false));
                    HwGpsLogController.this.operatorLog(false);
                    return true;
                case HwGpsLogController.MSG_CHECK_GPS_LOG_STATUS /*1103*/:
                    HwGpsLogController.this.checkGpsLogStatus();
                    return true;
                default:
                    return false;
            }
        }
    });

    private HwGpsLogController(Context context) {
        Log.d(TAG, "HwGpsLogController");
        this.mContext = context;
        this.mHandler.sendEmptyMessageDelayed(MSG_CHECK_GPS_LOG_STATUS, 30000);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GPS_LOG_STATUS_OPEN);
        intentFilter.addAction(GPS_LOG_STATUS_CLOSE);
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (HwGpsLogController.GPS_LOG_STATUS_OPEN.equals(action)) {
                    HwGpsLogController.this.mHandler.sendEmptyMessage(HwGpsLogController.MSG_GPS_LOG_STATUS_OPEN);
                    HwGpsLogController.this.mHandler.removeMessages(HwGpsLogController.MSG_CHECK_GPS_LOG_STATUS);
                } else if (HwGpsLogController.GPS_LOG_STATUS_CLOSE.equals(action)) {
                    HwGpsLogController.this.mHandler.sendEmptyMessage(HwGpsLogController.MSG_GPS_LOG_STATUS_CLOSE);
                    HwGpsLogController.this.mHandler.removeMessages(HwGpsLogController.MSG_CHECK_GPS_LOG_STATUS);
                } else {
                    Log.d(HwGpsLogController.TAG, "no match broadcast");
                }
            }
        }, UserHandle.ALL, intentFilter, null, null);
        getBcmGpsChipType();
    }

    private String getBcmGpsChipType() {
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(GPS_PATH_IC_TYPE), "UTF-8"));
            int line = 1;
            while (true) {
                String readLine = reader2.readLine();
                String tempString = readLine;
                if (readLine == null) {
                    break;
                }
                Log.d(TAG, "line " + line + ": " + tempString);
                line++;
                if (tempString.contains(BCM_47531)) {
                    this.mBcmChipType = BCM_47531;
                } else if (tempString.contains(BCM_4774)) {
                    this.mBcmChipType = BCM_4774;
                } else {
                    Log.d(TAG, "tempString not contains BCM_47531 or BCM_4774");
                }
            }
            reader2.close();
            try {
                reader2.close();
            } catch (IOException e1) {
                Log.e(TAG, e1.getMessage());
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e12) {
                    Log.e(TAG, e12.getMessage());
                }
            }
            throw th;
        }
        return null;
    }

    public static synchronized HwGpsLogController create(Context context) {
        HwGpsLogController hwGpsLogController;
        synchronized (HwGpsLogController.class) {
            if (mGpsLogController == null) {
                mGpsLogController = new HwGpsLogController(context);
            }
            hwGpsLogController = mGpsLogController;
        }
        return hwGpsLogController;
    }

    /* access modifiers changed from: private */
    public void checkGpsLogStatus() {
        try {
            String result = Settings.Global.getString(this.mContext.getContentResolver(), GPS_LOG_ENABLE);
            if (result != null) {
                if (!"".equals(result)) {
                    operatorLog(Boolean.parseBoolean(result));
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void operatorLog(boolean enable) {
        Log.d(TAG, "operatorLog: " + enable);
        try {
            if (SystemProperties.get(CHIP_TYPE, "").contains("bcm") && SystemProperties.getInt("persist.sys.huawei.debug.on", 0) == 1 && this.mBcmChipType != null) {
                xmlModify(enable);
                killGpsProcess();
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void killGpsProcess() {
        BufferedReader bufferedReader = null;
        BufferedReader bufferedReader2 = bufferedReader;
        try {
            Process proc = Runtime.getRuntime().exec("ps");
            InputStream inputStream = proc.getInputStream();
            proc.waitFor();
            if (inputStream != null) {
                try {
                    BufferedReader bufferedReader3 = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while (true) {
                        String readLine = bufferedReader3.readLine();
                        String line = readLine;
                        if (readLine == null) {
                            try {
                                break;
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        } else if (line.contains("glgps")) {
                            int pid = getPid(line);
                            if (-1 != pid) {
                                Log.i(TAG, "kill " + pid);
                                Process.killProcessQuiet(pid);
                            }
                        }
                    }
                    inputStream.close();
                    try {
                        bufferedReader3.close();
                    } catch (IOException e2) {
                        Log.e(TAG, e2.getMessage());
                    }
                } catch (IOException ioe) {
                    Log.e(TAG, ioe.getMessage());
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        Log.e(TAG, e3.getMessage());
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (RuntimeException rte) {
                    Log.e(TAG, rte.getMessage());
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                        Log.e(TAG, e4.getMessage());
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, e5.getMessage());
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (Throwable th) {
                    try {
                        inputStream.close();
                    } catch (IOException e6) {
                        Log.e(TAG, e6.getMessage());
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e7) {
                            Log.e(TAG, e7.getMessage());
                        }
                    }
                    throw th;
                }
            }
        } catch (Exception e8) {
            Log.e(TAG, e8.getMessage());
        }
    }

    private int getPid(String ps) {
        String[] results = ps.split(" +");
        Log.i(TAG, "getPid " + results[1]);
        return Integer.parseInt(results[1]);
    }

    private void xmlModify(boolean enable) {
        try {
            File file = new File(GPS_CONFIG_PATH);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            if (doc != null) {
                Node gllNodes = doc.getElementsByTagName("gll").item(0);
                if (gllNodes != null && BCM_47531.equals(this.mBcmChipType)) {
                    Node logFacMaskNode = gllNodes.getAttributes().getNamedItem("LogFacMask");
                    if (logFacMaskNode != null) {
                        logFacMaskNode.setNodeValue(enable ? "LOG_DEFAULT" : "LOG_GLLAPI | LOG_NMEA");
                        Log.d(TAG, "modify:" + logFacMaskNode.getNodeName() + "=" + logFacMaskNode.getNodeValue());
                    }
                }
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty("indent", "yes");
                transformer.transform(new DOMSource(doc), new StreamResult(file));
            }
        } catch (ParserConfigurationException e1) {
            Log.e(TAG, e1.getMessage());
        } catch (SAXException e2) {
            Log.e(TAG, e2.getMessage());
        } catch (TransformerConfigurationException e3) {
            Log.e(TAG, e3.getMessage());
        } catch (TransformerException e4) {
            Log.e(TAG, e4.getMessage());
        } catch (IOException e5) {
            Log.e(TAG, e5.getMessage());
        } catch (RuntimeException e6) {
            Log.e(TAG, e6.getMessage());
        }
    }
}
