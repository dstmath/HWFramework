package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
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
    private static boolean DBG = true;
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
    private Context mContext;
    private Handler mHandler;

    private HwGpsLogController(Context context) {
        if (DBG) {
            Log.d(TAG, "HwGpsLogController");
        }
        this.mContext = context;
        this.mHandler = new Handler(new Callback() {
            public boolean handleMessage(Message msg) {
                if (HwGpsLogController.DBG) {
                    Log.d(HwGpsLogController.TAG, "handleMessage: " + msg.what);
                }
                switch (msg.what) {
                    case HwGpsLogController.MSG_GPS_LOG_STATUS_OPEN /*1101*/:
                        Global.putString(HwGpsLogController.this.mContext.getContentResolver(), HwGpsLogController.GPS_LOG_ENABLE, Boolean.toString(true));
                        HwGpsLogController.this.operatorLog(true);
                        return true;
                    case HwGpsLogController.MSG_GPS_LOG_STATUS_CLOSE /*1102*/:
                        Global.putString(HwGpsLogController.this.mContext.getContentResolver(), HwGpsLogController.GPS_LOG_ENABLE, Boolean.toString(false));
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
        this.mHandler.sendEmptyMessageDelayed(MSG_CHECK_GPS_LOG_STATUS, CHECK_GPS_LOG_INTERVEL);
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
                }
            }
        }, UserHandle.ALL, intentFilter, null, null);
        getBcmGpsChipType();
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0060 A:{SYNTHETIC, Splitter: B:19:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0077 A:{SYNTHETIC, Splitter: B:30:0x0077} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getBcmGpsChipType() {
        IOException e;
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(GPS_PATH_IC_TYPE), "UTF-8"));
            int line = 1;
            while (true) {
                try {
                    String tempString = reader.readLine();
                    if (tempString == null) {
                        break;
                    }
                    if (DBG) {
                        Log.d(TAG, "line " + line + ": " + tempString);
                    }
                    line++;
                    if (tempString.contains(BCM_47531)) {
                        this.mBcmChipType = BCM_47531;
                    } else if (tempString.contains(BCM_4774)) {
                        this.mBcmChipType = BCM_4774;
                    }
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = reader;
                    try {
                        e.printStackTrace();
                        if (bufferedReader != null) {
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = reader;
                    if (bufferedReader != null) {
                    }
                    throw th;
                }
            }
            reader.close();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
        } catch (IOException e5) {
            e = e5;
            e.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e6) {
                }
            }
            return null;
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

    private void checkGpsLogStatus() {
        try {
            String result = Global.getString(this.mContext.getContentResolver(), GPS_LOG_ENABLE);
            if (result != null && !"".equals(result)) {
                operatorLog(Boolean.parseBoolean(result));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void operatorLog(boolean enable) {
        if (DBG) {
            Log.d(TAG, "operatorLog: " + enable);
        }
        try {
            if (SystemProperties.get(CHIP_TYPE, "").contains("bcm") && SystemProperties.getInt("persist.sys.huawei.debug.on", 0) == 1 && this.mBcmChipType != null) {
                xmlModify(enable);
                killGpsProcess();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x006d A:{SYNTHETIC, Splitter: B:30:0x006d} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00a5 A:{SYNTHETIC, Splitter: B:61:0x00a5} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x008e A:{SYNTHETIC, Splitter: B:49:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x00c3 A:{SYNTHETIC, Splitter: B:75:0x00c3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void killGpsProcess() {
        IOException ioe;
        Throwable th;
        RuntimeException rte;
        Exception ex;
        try {
            Process proc = Runtime.getRuntime().exec("ps");
            InputStream inputStream = proc.getInputStream();
            proc.waitFor();
            if (inputStream != null) {
                BufferedReader bufferedReader = null;
                try {
                    BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while (true) {
                        try {
                            String line = bufferedReader2.readLine();
                            if (line == null) {
                                try {
                                    break;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else if (line.contains("glgps")) {
                                int pid = getPid(line);
                                if (-1 != pid) {
                                    if (DBG) {
                                        Log.i(TAG, "kill " + pid);
                                    }
                                    Process.killProcessQuiet(pid);
                                }
                            }
                        } catch (IOException e2) {
                            ioe = e2;
                            bufferedReader = bufferedReader2;
                            try {
                                ioe.printStackTrace();
                                try {
                                    inputStream.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                }
                                if (bufferedReader != null) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                try {
                                    inputStream.close();
                                } catch (IOException e32) {
                                    e32.printStackTrace();
                                }
                                if (bufferedReader != null) {
                                }
                                throw th;
                            }
                        } catch (RuntimeException e4) {
                            rte = e4;
                            bufferedReader = bufferedReader2;
                            rte.printStackTrace();
                            try {
                                inputStream.close();
                            } catch (IOException e322) {
                                e322.printStackTrace();
                            }
                            if (bufferedReader != null) {
                            }
                        } catch (Exception e5) {
                            ex = e5;
                            bufferedReader = bufferedReader2;
                            ex.printStackTrace();
                            try {
                                inputStream.close();
                            } catch (IOException e3222) {
                                e3222.printStackTrace();
                            }
                            if (bufferedReader != null) {
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            bufferedReader = bufferedReader2;
                            inputStream.close();
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e32222) {
                                    e32222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    }
                    inputStream.close();
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e322222) {
                            e322222.printStackTrace();
                        }
                    }
                } catch (IOException e6) {
                    ioe = e6;
                    ioe.printStackTrace();
                    inputStream.close();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e3222222) {
                            e3222222.printStackTrace();
                        }
                    }
                } catch (RuntimeException e7) {
                    rte = e7;
                    rte.printStackTrace();
                    inputStream.close();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e32222222) {
                            e32222222.printStackTrace();
                        }
                    }
                } catch (Exception e8) {
                    ex = e8;
                    ex.printStackTrace();
                    inputStream.close();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e322222222) {
                            e322222222.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e9) {
            e9.printStackTrace();
        }
    }

    private int getPid(String ps) {
        String[] results = ps.split(" +");
        if (DBG) {
            Log.i(TAG, "getPid " + results[1]);
        }
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
                        if (DBG) {
                            Log.d(TAG, "modify:" + logFacMaskNode.getNodeName() + "=" + logFacMaskNode.getNodeValue());
                        }
                    }
                }
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty("indent", "yes");
                transformer.transform(new DOMSource(doc), new StreamResult(file));
            }
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        } catch (SAXException e2) {
            e2.printStackTrace();
        } catch (TransformerConfigurationException e3) {
            e3.printStackTrace();
        } catch (TransformerException e4) {
            e4.printStackTrace();
        } catch (IOException e5) {
            e5.printStackTrace();
        } catch (RuntimeException e52) {
            e52.printStackTrace();
        }
    }
}
