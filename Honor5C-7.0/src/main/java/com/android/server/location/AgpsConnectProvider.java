package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.am.ProcessList;
import com.android.server.power.AbsPowerManagerService;
import com.android.server.wm.WindowManagerService.H;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class AgpsConnectProvider {
    private static final String ACTION = "action";
    private static final String ADDRESS = "address";
    private static final int CLOSE_SOCKET = 0;
    private static final int CREATE_SOCKET = 1;
    private static final String DATA = "data";
    private static final String DATASTATUS = "dataStatus";
    private static final boolean DEBUG = false;
    private static final boolean DEBUGE_OVER = false;
    private static final int DEFAULT_DATASTATUS = 1;
    private static final int FINISH_DATA_CONNECT = 0;
    private static final int GPSRPC_SERVER_MPC = 1;
    private static final int GPSRPC_SERVER_NULL = 0;
    private static final int GPSRPC_SERVER_PDE = 2;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    protected static final boolean HWFLOW = false;
    private static final boolean IS_CDMA_GSM = false;
    private static final boolean IS_HISI_CDMA_SUPPORTED = false;
    private static final String LEN = "len";
    private static final int MPC_SERVICE_ID = 1;
    private static final String PORT = "port";
    private static final String SERVER = "server";
    private static final int STATUS_CLOSE = 0;
    private static final int STATUS_CONNECTED = 1;
    private static final String TAG = "AgpsConnectProvider";
    private static final String TRIGGERMODE = "triggerMode";
    private static final int TRIGGERMODE_NI = 1;
    private static final long delaytime = 5000;
    private Context mContext;
    private AgpsHandler mHandler;
    private SocketThread mSocketThread;
    private HandlerThread mThread;

    /* renamed from: com.android.server.location.AgpsConnectProvider.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            if (Secure.isLocationProviderEnabled(AgpsConnectProvider.this.mContext.getContentResolver(), "gps")) {
                SystemProperties.set("sys.gps_provider_enabled", "true");
            } else {
                SystemProperties.set("sys.gps_provider_enabled", "false");
            }
        }
    }

    private class AgpsHandler extends Handler {
        private static final long CTWAP_INTERVAL = 7200000;
        private static final String FORBIDEN_AGPS_IN_2_HOURS_SWITCH = "forbiden_agps_in_2_hours";
        private static final int FORBIDEN_OPEN = 1;
        private static final int FORCE_START_DOING = 1;
        private static final int FORCE_START_ERROR = 2;
        private static final int FORCE_START_READY = 0;
        private static final long FORE_OPEN = 0;
        private static final long FORE_OPEN_DATA_TIMEOUT = 180000;
        private static final int MSG_CHECK_CONNECT = 100;
        private static final int MSG_CONTROL_SOCKET_CONNECT = 101;
        private static final int MSG_FORE_OPEN_DATA_SUCCESS = 105;
        private static final int MSG_FORE_OPEN_DATA_TIMEOUT = 104;
        private static final int MSG_HANDLE_SOCKET = 102;
        private static final int MSG_INIT = 103;
        private static final int RESULT_CONNECTED = 1;
        private static final int RESULT_IDLE = 0;
        private static final int SOCKET_MODE = 1;
        private long mLastMsgTime;
        private long mLastSwitchTime;
        private ConnectivityBroadcastReceiver mReceiver;
        private Socket mSocketMPC;
        private Socket mSocketPDE;

        private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
            private ConnectivityBroadcastReceiver() {
            }

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (AgpsConnectProvider.DEBUG) {
                    Log.w(AgpsConnectProvider.TAG, "ConnectivityBroadcastReceiver.onReceive() action: " + action);
                }
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action) || "android.net.conn.CONNECTIVITY_CHANGE_SUPL".equals(action)) {
                    ConnectivityManager cm = (ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity");
                    if (cm != null) {
                        NetworkInfo suplNetworkInfo = cm.getNetworkInfo(3);
                        if (suplNetworkInfo == null) {
                            if (AgpsConnectProvider.DEBUG) {
                                Log.v(AgpsConnectProvider.TAG, "suplNetworkInfo is null");
                            }
                            return;
                        }
                        if (AgpsConnectProvider.DEBUG) {
                            Log.v(AgpsConnectProvider.TAG, " ConnectivityBroadcastReceiver.onReceive()" + suplNetworkInfo);
                        }
                        if (!suplNetworkInfo.isAvailable()) {
                            Log.d(AgpsConnectProvider.TAG, "net not available");
                            return;
                        } else if (suplNetworkInfo.getType() != 3) {
                            if (AgpsConnectProvider.DEBUG) {
                                Log.v(AgpsConnectProvider.TAG, "   type is not TYPE_MOBILE_SUPL");
                            }
                            if ("2GVoiceCallEnded".equals(suplNetworkInfo.getReason())) {
                                if (AgpsConnectProvider.DEBUG) {
                                    Log.v(AgpsConnectProvider.TAG, "   reason is 2GVoiceCallEnded, retrying SUPL connectivity");
                                }
                                AgpsHandler.this.renewSuplConnectivity();
                            }
                            return;
                        } else if (suplNetworkInfo.isConnected()) {
                            Message msg = new Message();
                            msg.what = AgpsHandler.MSG_FORE_OPEN_DATA_SUCCESS;
                            AgpsConnectProvider.this.mHandler.sendMessage(msg);
                            return;
                        } else {
                            if (AgpsConnectProvider.DEBUG) {
                                Log.v(AgpsConnectProvider.TAG, "   TYPE_MOBILE_SUPL not connected");
                            }
                            if (suplNetworkInfo.isAvailable()) {
                                if (AgpsConnectProvider.DEBUG) {
                                    Log.v(AgpsConnectProvider.TAG, "   retrying  connectivity for it's available");
                                }
                                AgpsHandler.this.renewSuplConnectivity();
                            }
                            return;
                        }
                    }
                    if (AgpsConnectProvider.DEBUG) {
                        Log.v(AgpsConnectProvider.TAG, "ConnectivityManager is null");
                    }
                    return;
                }
                Log.w(AgpsConnectProvider.TAG, "ConnectivityBroadcastReceiver.onReceive() NULL action ");
            }
        }

        AgpsHandler(Looper looper) {
            super(looper);
            this.mLastSwitchTime = FORE_OPEN;
            this.mLastMsgTime = System.currentTimeMillis();
        }

        public void handleMessage(Message msg) {
            if (AgpsConnectProvider.DEBUG) {
                Log.d(AgpsConnectProvider.TAG, "handleMessage msg is : " + msg.what);
            }
            if (AgpsConnectProvider.IS_HISI_CDMA_SUPPORTED) {
                long intervalTime = System.currentTimeMillis() - this.mLastMsgTime;
                if (AgpsConnectProvider.DEBUG) {
                    Log.d(AgpsConnectProvider.TAG, "interval between msg is " + intervalTime);
                }
                if (intervalTime < 50) {
                    long delayedTime = 50 - intervalTime;
                    if (delayedTime > 50) {
                        delayedTime = 50;
                    }
                    try {
                        Thread.sleep(delayedTime);
                    } catch (Exception e) {
                    }
                }
                this.mLastMsgTime = System.currentTimeMillis();
            }
            switch (msg.what) {
                case MSG_CHECK_CONNECT /*100*/:
                    handleCheckConnect(msg.getData());
                case MSG_CONTROL_SOCKET_CONNECT /*101*/:
                    Log.d(AgpsConnectProvider.TAG, "enter case MSG_CONTROL_SOCKET_CONNECT:");
                    handleControlSocketConnet(msg.getData());
                case MSG_HANDLE_SOCKET /*102*/:
                    handleSocketCommunication(msg.getData());
                case MSG_INIT /*103*/:
                    new Thread(new Runnable() {
                        public void run() {
                            AgpsHandler.this.handleInit();
                        }
                    }).start();
                case MSG_FORE_OPEN_DATA_TIMEOUT /*104*/:
                    handleForeOpenDataTimeout();
                case MSG_FORE_OPEN_DATA_SUCCESS /*105*/:
                    handleForeOpenDataSuccess();
                default:
            }
        }

        private void handleInit() {
            if (AgpsConnectProvider.DEBUG) {
                Log.d(AgpsConnectProvider.TAG, "handle init obj");
            }
            AgpsConnectProvider.start_socket();
        }

        private void handleSocketCommunication(Bundle data) {
            int server = data.getInt(AgpsConnectProvider.SERVER);
            if (server == SOCKET_MODE) {
                if (this.mSocketMPC == null) {
                    Log.w(AgpsConnectProvider.TAG, "mSocketMPC == null");
                    AgpsConnectProvider.this.native_agps_sock_status(server, RESULT_IDLE);
                    return;
                }
                Log.d(AgpsConnectProvider.TAG, "Create MPC_SERVICE SocketThread");
                AgpsConnectProvider.this.mSocketThread = new SocketThread(this.mSocketMPC, data);
            } else if (this.mSocketPDE == null) {
                Log.w(AgpsConnectProvider.TAG, "mSocketPDE == null");
                AgpsConnectProvider.this.native_agps_sock_status(server, RESULT_IDLE);
                return;
            } else {
                Log.d(AgpsConnectProvider.TAG, "Create PDE_SERVICE SocketThread");
                AgpsConnectProvider.this.mSocketThread = new SocketThread(this.mSocketPDE, data);
            }
            AgpsConnectProvider.this.mSocketThread.start();
        }

        private void handleControlSocketConnet(Bundle data) {
            Log.d(AgpsConnectProvider.TAG, "enter handleControlSocketConnet");
            int server = data.getInt(AgpsConnectProvider.SERVER);
            int action = data.getInt(AgpsConnectProvider.ACTION);
            String address = data.getString(AgpsConnectProvider.ADDRESS);
            int port = data.getInt(AgpsConnectProvider.PORT);
            if (action == SOCKET_MODE) {
                if (startConnection(address, port, server)) {
                    if (AgpsConnectProvider.DEBUG) {
                        Log.d(AgpsConnectProvider.TAG, "Connect success, address :" + address + " ,port: " + port);
                    }
                    AgpsConnectProvider.this.native_agps_sock_status(server, SOCKET_MODE);
                    return;
                }
                AgpsConnectProvider.this.native_agps_sock_status(server, RESULT_IDLE);
                if (AgpsConnectProvider.DEBUG) {
                    Log.d(AgpsConnectProvider.TAG, "Connect fail, address :" + address + " ,port: " + port);
                }
            } else if (action == 0) {
                if (server == SOCKET_MODE) {
                    try {
                        Log.d(AgpsConnectProvider.TAG, "Close MPC_SERVICE");
                        if (this.mSocketMPC != null) {
                            this.mSocketMPC.close();
                            this.mSocketMPC = null;
                        }
                    } catch (IOException e) {
                        Log.e(AgpsConnectProvider.TAG, "close socket IOException");
                    }
                } else {
                    Log.d(AgpsConnectProvider.TAG, "Close PDE_SERVICE");
                    if (this.mSocketPDE != null) {
                        this.mSocketPDE.close();
                        this.mSocketPDE = null;
                    }
                }
                if (AgpsConnectProvider.DEBUG) {
                    Log.d(AgpsConnectProvider.TAG, "native_agps_sock_status, server " + server + " ,status  " + RESULT_IDLE);
                }
                AgpsConnectProvider.this.native_agps_sock_status(server, RESULT_IDLE);
            } else if (AgpsConnectProvider.DEBUG) {
                Log.e(AgpsConnectProvider.TAG, "error action:" + action);
            }
        }

        private boolean startConnection(String address, int port, int server) {
            Socket socket;
            UnknownHostException e;
            IOException e2;
            Exception e3;
            int retryNum = SOCKET_MODE;
            addRoute(address);
            Socket socket2 = null;
            while (socket2 == null) {
                if (retryNum > FORCE_START_ERROR) {
                    Log.d(AgpsConnectProvider.TAG, "socket is null!");
                    return AgpsConnectProvider.IS_HISI_CDMA_SUPPORTED;
                }
                try {
                    Log.d(AgpsConnectProvider.TAG, "create new Socket");
                    socket = new Socket();
                    try {
                        socket.connect(new InetSocketAddress(address, port), ProcessList.PSS_MIN_TIME_FROM_STATE_CHANGE);
                    } catch (UnknownHostException e4) {
                        e = e4;
                        e.printStackTrace();
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e5) {
                                Log.e(AgpsConnectProvider.TAG, "close socket IOException");
                            }
                            socket = null;
                            if (!AgpsConnectProvider.DEBUG) {
                                Log.d(AgpsConnectProvider.TAG, "Connect fail " + retryNum);
                            }
                        }
                        retryNum += SOCKET_MODE;
                        socket2 = socket;
                    } catch (IOException e6) {
                        e2 = e6;
                        e2.printStackTrace();
                        if (socket.isConnected()) {
                            socket.close();
                            socket = null;
                            if (!AgpsConnectProvider.DEBUG) {
                                Log.d(AgpsConnectProvider.TAG, "Connect fail " + retryNum);
                            }
                        }
                        retryNum += SOCKET_MODE;
                        socket2 = socket;
                    } catch (Exception e7) {
                        e3 = e7;
                        e3.printStackTrace();
                        if (socket.isConnected()) {
                            socket.close();
                            socket = null;
                            if (!AgpsConnectProvider.DEBUG) {
                                Log.d(AgpsConnectProvider.TAG, "Connect fail " + retryNum);
                            }
                        }
                        retryNum += SOCKET_MODE;
                        socket2 = socket;
                    }
                } catch (UnknownHostException e8) {
                    e = e8;
                    socket = socket2;
                    e.printStackTrace();
                    if (socket.isConnected()) {
                        socket.close();
                        socket = null;
                        if (!AgpsConnectProvider.DEBUG) {
                            Log.d(AgpsConnectProvider.TAG, "Connect fail " + retryNum);
                        }
                    }
                    retryNum += SOCKET_MODE;
                    socket2 = socket;
                } catch (IOException e9) {
                    e2 = e9;
                    socket = socket2;
                    e2.printStackTrace();
                    if (socket.isConnected()) {
                        socket.close();
                        socket = null;
                        if (!AgpsConnectProvider.DEBUG) {
                            Log.d(AgpsConnectProvider.TAG, "Connect fail " + retryNum);
                        }
                    }
                    retryNum += SOCKET_MODE;
                    socket2 = socket;
                } catch (Exception e10) {
                    e3 = e10;
                    socket = socket2;
                    e3.printStackTrace();
                    if (socket.isConnected()) {
                        socket.close();
                        socket = null;
                        if (!AgpsConnectProvider.DEBUG) {
                            Log.d(AgpsConnectProvider.TAG, "Connect fail " + retryNum);
                        }
                    }
                    retryNum += SOCKET_MODE;
                    socket2 = socket;
                }
                if (socket.isConnected()) {
                    socket.close();
                    socket = null;
                    if (!AgpsConnectProvider.DEBUG) {
                        Log.d(AgpsConnectProvider.TAG, "Connect fail " + retryNum);
                    }
                }
                retryNum += SOCKET_MODE;
                socket2 = socket;
            }
            if (server == SOCKET_MODE) {
                Log.d(AgpsConnectProvider.TAG, "Create MPC_SERVICE");
                this.mSocketMPC = socket2;
            } else {
                Log.d(AgpsConnectProvider.TAG, "Create PDE_SERVICE");
                this.mSocketPDE = socket2;
            }
            return true;
        }

        private void addRoute(String address) {
            try {
                if (!((ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity")).requestRouteToHostAddress(3, InetAddress.getByName(address))) {
                    Log.d(AgpsConnectProvider.TAG, "add roater, fail");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleForeOpenDataTimeout() {
            AgpsConnectProvider.this.native_agps_data_conn_status(RESULT_IDLE);
            if (this.mReceiver != null) {
                AgpsConnectProvider.this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
        }

        private void handleForeOpenDataSuccess() {
            if (this.mReceiver != null) {
                AgpsConnectProvider.this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
            AgpsConnectProvider.this.native_agps_data_conn_status(SOCKET_MODE);
            AgpsConnectProvider.this.mHandler.removeMessages(MSG_FORE_OPEN_DATA_TIMEOUT);
        }

        private int forceOpenData(ConnectivityManager cm) {
            int result = cm.startUsingNetworkFeature(RESULT_IDLE, "enableSUPL");
            if (result == 0) {
                if (AgpsConnectProvider.DEBUG) {
                    Log.d(AgpsConnectProvider.TAG, "PhoneConstants.APN_ALREADY_ACTIVE");
                }
                return RESULT_IDLE;
            } else if (result == SOCKET_MODE) {
                if (AgpsConnectProvider.DEBUG) {
                    Log.d(AgpsConnectProvider.TAG, "PhoneConstants.APN_REQUEST_STARTED");
                }
                return SOCKET_MODE;
            } else {
                if (AgpsConnectProvider.DEBUG) {
                    Log.d(AgpsConnectProvider.TAG, "startUsingNetworkFeature failed, value is " + result);
                }
                return FORCE_START_ERROR;
            }
        }

        private void handleCheckConnect(Bundle data) {
            int triggerMode = data.getInt(AgpsConnectProvider.TRIGGERMODE);
            int dataStatus = data.getInt(AgpsConnectProvider.DATASTATUS);
            ConnectivityManager cm = (ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity");
            if (cm != null) {
                if (dataStatus == 0) {
                    cm.stopUsingNetworkFeature(RESULT_IDLE, "enableSUPL");
                    if (SOCKET_MODE == System.getInt(AgpsConnectProvider.this.mContext.getContentResolver(), FORBIDEN_AGPS_IN_2_HOURS_SWITCH, SOCKET_MODE)) {
                        this.mLastSwitchTime = System.currentTimeMillis();
                    }
                    Log.d(AgpsConnectProvider.TAG, "Close data call ,call stopUsingNetworkFeature");
                    return;
                }
                boolean isConnect = AgpsConnectProvider.IS_HISI_CDMA_SUPPORTED;
                NetworkInfo networkInfo = cm.getNetworkInfo(RESULT_IDLE);
                if (networkInfo != null) {
                    isConnect = networkInfo.isConnected();
                }
                if (AgpsConnectProvider.DEBUG) {
                    Log.d(AgpsConnectProvider.TAG, "isConnect = " + isConnect);
                }
                if (isConnect || triggerMode == SOCKET_MODE) {
                    boolean isCdmaType = AgpsConnectProvider.IS_HISI_CDMA_SUPPORTED;
                    try {
                        isCdmaType = FORCE_START_ERROR == TelephonyManager.getDefault().getCurrentPhoneType(Global.getInt(AgpsConnectProvider.this.mContext.getContentResolver(), "multi_sim_data_call", RESULT_IDLE)) ? true : AgpsConnectProvider.IS_HISI_CDMA_SUPPORTED;
                        if (AgpsConnectProvider.DEBUG) {
                            Log.d(AgpsConnectProvider.TAG, "isCdmaType = " + isCdmaType);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (triggerMode != SOCKET_MODE && isConnect && !isCdmaType) {
                        AgpsConnectProvider.this.native_agps_data_conn_status(RESULT_IDLE);
                        return;
                    } else if (SOCKET_MODE != System.getInt(AgpsConnectProvider.this.mContext.getContentResolver(), FORBIDEN_AGPS_IN_2_HOURS_SWITCH, SOCKET_MODE) || triggerMode == SOCKET_MODE || this.mLastSwitchTime == FORE_OPEN || System.currentTimeMillis() - this.mLastSwitchTime >= CTWAP_INTERVAL) {
                        int feature = forceOpenData((ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity"));
                        if (feature == 0) {
                            AgpsConnectProvider.this.native_agps_data_conn_status(SOCKET_MODE);
                        } else if (feature == SOCKET_MODE) {
                            startReceiver();
                            Message msg = new Message();
                            msg.what = MSG_FORE_OPEN_DATA_TIMEOUT;
                            AgpsConnectProvider.this.mHandler.sendMessageDelayed(msg, FORE_OPEN_DATA_TIMEOUT);
                        } else {
                            AgpsConnectProvider.this.native_agps_data_conn_status(RESULT_IDLE);
                        }
                        return;
                    } else {
                        Log.d(AgpsConnectProvider.TAG, "forbidden");
                        AgpsConnectProvider.this.native_agps_data_conn_status(RESULT_IDLE);
                        return;
                    }
                }
                AgpsConnectProvider.this.native_agps_data_conn_status(RESULT_IDLE);
            }
        }

        private void startReceiver() {
            if (this.mReceiver != null) {
                AgpsConnectProvider.this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
            this.mReceiver = new ConnectivityBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE_SUPL");
            AgpsConnectProvider.this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }

        private void renewSuplConnectivity() {
            if (AgpsConnectProvider.DEBUG) {
                Log.v(AgpsConnectProvider.TAG, "renewSuplConnectivity");
            }
            int feature = forceOpenData((ConnectivityManager) AgpsConnectProvider.this.mContext.getSystemService("connectivity"));
            if (feature == 0) {
                AgpsConnectProvider.this.native_agps_data_conn_status(SOCKET_MODE);
            } else if (feature == SOCKET_MODE) {
                if (AgpsConnectProvider.DEBUG) {
                    Log.v(AgpsConnectProvider.TAG, "   FORCE_START_DOING");
                }
                return;
            } else {
                AgpsConnectProvider.this.native_agps_data_conn_status(RESULT_IDLE);
            }
            AgpsConnectProvider.this.mHandler.removeMessages(MSG_FORE_OPEN_DATA_TIMEOUT);
            if (this.mReceiver != null) {
                AgpsConnectProvider.this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
        }
    }

    private class SocketThread extends Thread {
        byte[] mBytes;
        int mServer;
        private Socket mSocket;

        public SocketThread(Socket socket, Bundle data) {
            this.mSocket = socket;
            this.mServer = data.getInt(AgpsConnectProvider.SERVER);
            this.mBytes = data.getByteArray(AgpsConnectProvider.DATA);
        }

        public void run() {
            try {
                this.mSocket.setSoTimeout(AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT);
                OutputStream ops = this.mSocket.getOutputStream();
                ops.write(this.mBytes);
                ops.flush();
                InputStream ips = this.mSocket.getInputStream();
                byte[] oBytes = new byte[1460];
                int readlen = AgpsConnectProvider.TRIGGERMODE_NI;
                while (readlen > 0) {
                    readlen = ips.read(oBytes);
                    if (AgpsConnectProvider.DEBUG) {
                        Log.d(AgpsConnectProvider.TAG, "readlen is = " + readlen + " ,oBytes.length is " + oBytes.length);
                    }
                    if (readlen > 0) {
                        AgpsConnectProvider.this.native_agps_sock_data(this.mServer, oBytes, readlen);
                    }
                }
            } catch (IOException e) {
                Log.d(AgpsConnectProvider.TAG, "read/write time out,force to close socket,not exception");
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.AgpsConnectProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.AgpsConnectProvider.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.AgpsConnectProvider.<clinit>():void");
    }

    private static native void class_init_native();

    private native void init_native_gpsrpc_object();

    private native int native_agps_data_conn_status(int i);

    private native int native_agps_sock_data(int i, byte[] bArr, int i2);

    private native int native_agps_sock_status(int i, int i2);

    private static native void start_socket();

    public AgpsConnectProvider(Context context) {
        if (DEBUG) {
            Log.d(TAG, "AgpsConnectProvider init");
        }
        this.mContext = context;
        init_native_gpsrpc_object();
        this.mThread = new HandlerThread(TAG);
        this.mThread.start();
        this.mHandler = new AgpsHandler(this.mThread.getLooper());
        Message msg = new Message();
        msg.what = HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION;
        this.mHandler.sendMessage(msg);
        if (DEBUG) {
            Log.d(TAG, "send msg to handle init");
        }
        checkGpsEnable();
    }

    private void checkDataConnect(int triggerMode, int dataStatus) {
        Message msg = new Message();
        msg.what = 100;
        Bundle bundle = new Bundle();
        bundle.putInt(TRIGGERMODE, triggerMode);
        bundle.putInt(DATASTATUS, dataStatus);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        if (DEBUG) {
            Log.d(TAG, "enter checkDataConnect,triggerMode = " + triggerMode + " ,dataStatus = " + dataStatus);
        }
    }

    private void jnitest() {
        Log.e(TAG, "jni test callback success!");
    }

    private void controlSocketConnect(int server, int action, String address, int port) {
        Message msg = new Message();
        msg.what = H.KEYGUARD_DISMISS_DONE;
        Bundle bundle = new Bundle();
        bundle.putInt(SERVER, server);
        bundle.putInt(ACTION, action);
        bundle.putString(ADDRESS, address);
        bundle.putInt(PORT, port);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        if (DEBUG) {
            Log.d(TAG, "controlSocketConnect,server = " + server + " ,action = " + action + " ,address = " + address + " ,port = " + port);
        }
    }

    private void handleSocket(int server, byte[] data, int len) {
        Message msg = new Message();
        msg.what = HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION;
        Bundle bundle = new Bundle();
        bundle.putInt(SERVER, server);
        bundle.putByteArray(DATA, data);
        bundle.putInt(LEN, len);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        if (DEBUG) {
            Log.d(TAG, "handleSocket,server = " + server + " ,len = " + len);
        }
    }

    public static AgpsConnectProvider createAgpsConnectProvider(Context context) {
        if (IS_CDMA_GSM || IS_HISI_CDMA_SUPPORTED) {
            return new AgpsConnectProvider(context);
        }
        return null;
    }

    private void checkGpsEnable() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("location_providers_allowed"), true, new AnonymousClass1(this.mHandler), -1);
        if (Secure.isLocationProviderEnabled(this.mContext.getContentResolver(), "gps")) {
            SystemProperties.set("sys.gps_provider_enabled", "true");
        } else {
            SystemProperties.set("sys.gps_provider_enabled", "false");
        }
    }

    public static int setPostionMode(int oldPositionMode, Context context) {
        if (!IS_CDMA_GSM || Secure.isLocationProviderEnabled(context.getContentResolver(), "network")) {
            return oldPositionMode;
        }
        Log.d(TAG, "set GPS_POSITION_MODE_STANDALONE");
        return STATUS_CLOSE;
    }
}
