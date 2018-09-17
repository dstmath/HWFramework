package com.android.server.location;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.server.display.Utils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HwBcmGnssManager {
    private static final int BCM_NUM_PWR_MODE_STATUS = 4;
    private static final int BCM_PM_DISABLED = 0;
    private static final int BCM_PM_FULL = 1;
    private static final int BCM_PM_HOST_OFFLOAD = 3;
    private static final int BCM_PM_SAVE = 2;
    private static final String CALL_SENTRY = "CallSentry";
    private static final boolean DEBUG = false;
    private static final int LBS_AGC_DATA = 260;
    private static final int LBS_AIDING_EPH = 4;
    private static final int LBS_AIDING_FREQ = 8;
    private static final int LBS_AIDING_NONE = 0;
    private static final int LBS_AIDING_POS = 2;
    private static final int LBS_AIDING_STATUS = 259;
    private static final int LBS_AIDING_TIME = 1;
    private static final int LBS_ASSERT_MSG = 512;
    private static final int LBS_DOP_DATA = 261;
    private static final int LBS_DYN_CITY = 1;
    private static final int LBS_DYN_DRIVING = 2;
    private static final int LBS_DYN_INVALID = 3;
    private static final int LBS_DYN_WALKING = 0;
    private static final int LBS_POS_AUTONOMOUS = 3;
    private static final int LBS_POS_CELL_ID = 4;
    private static final int LBS_POS_HULA = 7;
    private static final int LBS_POS_LAST_KNOWN = 5;
    private static final int LBS_POS_SOURCE = 256;
    private static final int LBS_POS_UE_ASSISTED_AGPS = 1;
    private static final int LBS_POS_UE_BASED_AGPS = 2;
    private static final int LBS_POS_UE_E_ASSISTED_AGPS = 6;
    private static final int LBS_POS_UNKNOWN = 0;
    private static final int LBS_TCXO_OFFSET = 258;
    private static final int LBS_TIME_FROM_ASSIST = 2;
    private static final int LBS_TIME_FROM_POSITION = 3;
    private static final int LBS_TIME_FROM_STANDBY = 1;
    private static final int LBS_TIME_FROM_TOW = 4;
    private static final int LBS_TIME_FROM_TOW_CONFIRMED = 5;
    private static final int LBS_TIME_ROURCE = 257;
    private static final int LBS_TIME_UNKNOWN = 0;
    private static final String TAG = "HwGnssLog_BcmGnss";
    private static final boolean VERBOSE = false;
    private GpsPosErrorEvent mBcmGnssErr;
    private Handler mBcmGnssHander;
    private GpsSessionEvent mBcmGnssSession;
    private LocalServerSocket serverSocket;

    public class ServerThread extends Thread {
        private LocalSocket localSoc;

        private ServerThread(LocalSocket localSocket) throws IOException {
            this.localSoc = localSocket;
        }

        public void run() {
            if (HwBcmGnssManager.DEBUG) {
                Log.d(HwBcmGnssManager.TAG, "enter handle_bcm_process !localSocket is : " + this.localSoc);
            }
            InputStream inputStream = null;
            try {
                inputStream = this.localSoc.getInputStream();
                byte[] buf = new byte[264];
                while (inputStream.read(buf, HwBcmGnssManager.LBS_TIME_UNKNOWN, 264) != -1) {
                    bcmGnssMsgDecoder(buf);
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            }
        }

        private byte[] toLH(int n) {
            byte[] b = new byte[HwBcmGnssManager.LBS_TIME_FROM_TOW];
            b[HwBcmGnssManager.LBS_TIME_UNKNOWN] = (byte) (n & Utils.MAXINUM_TEMPERATURE);
            b[HwBcmGnssManager.LBS_TIME_FROM_STANDBY] = (byte) ((n >> HwBcmGnssManager.LBS_AIDING_FREQ) & Utils.MAXINUM_TEMPERATURE);
            b[HwBcmGnssManager.LBS_TIME_FROM_ASSIST] = (byte) ((n >> 16) & Utils.MAXINUM_TEMPERATURE);
            b[HwBcmGnssManager.LBS_TIME_FROM_POSITION] = (byte) ((n >> 24) & Utils.MAXINUM_TEMPERATURE);
            return b;
        }

        private String toStr(byte[] valArr, int startpoint, int maxLen) {
            int index = HwBcmGnssManager.LBS_TIME_UNKNOWN;
            while (index + startpoint < valArr.length && index < maxLen && valArr[index + startpoint] != null) {
                index += HwBcmGnssManager.LBS_TIME_FROM_STANDBY;
            }
            byte[] temp = new byte[index];
            System.arraycopy(valArr, startpoint, temp, HwBcmGnssManager.LBS_TIME_UNKNOWN, index);
            return new String(temp);
        }

        private int vtolh(byte[] bArr, int offset) {
            int n = HwBcmGnssManager.LBS_TIME_UNKNOWN;
            int i = HwBcmGnssManager.LBS_TIME_UNKNOWN;
            while (i < bArr.length && i < HwBcmGnssManager.LBS_TIME_FROM_TOW) {
                n += (bArr[offset + i] & Utils.MAXINUM_TEMPERATURE) << (i * HwBcmGnssManager.LBS_AIDING_FREQ);
                i += HwBcmGnssManager.LBS_TIME_FROM_STANDBY;
            }
            return n;
        }

        private int byteArrayToInt(byte[] b, int offset) {
            int value = HwBcmGnssManager.LBS_TIME_UNKNOWN;
            for (int i = HwBcmGnssManager.LBS_TIME_UNKNOWN; i < HwBcmGnssManager.LBS_TIME_FROM_TOW; i += HwBcmGnssManager.LBS_TIME_FROM_STANDBY) {
                value += (b[i + offset] & Utils.MAXINUM_TEMPERATURE) << ((3 - i) * HwBcmGnssManager.LBS_AIDING_FREQ);
            }
            return value;
        }

        private void bcmGnssMsgDecoder(byte[] bArr) {
            if (HwBcmGnssManager.DEBUG) {
                Log.d(HwBcmGnssManager.TAG, "Server Enter ProcessMessage !!!!!!");
            }
            byte[] msg = new byte[HwBcmGnssManager.LBS_POS_SOURCE];
            byte[] temp = new byte[HwBcmGnssManager.LBS_TIME_FROM_TOW];
            System.arraycopy(bArr, HwBcmGnssManager.LBS_TIME_UNKNOWN, temp, HwBcmGnssManager.LBS_TIME_UNKNOWN, HwBcmGnssManager.LBS_TIME_FROM_TOW);
            int cmdID = vtolh(temp, HwBcmGnssManager.LBS_TIME_UNKNOWN);
            System.arraycopy(bArr, HwBcmGnssManager.LBS_TIME_FROM_TOW, temp, HwBcmGnssManager.LBS_TIME_UNKNOWN, HwBcmGnssManager.LBS_TIME_FROM_TOW);
            int len = vtolh(temp, HwBcmGnssManager.LBS_TIME_UNKNOWN);
            if (HwBcmGnssManager.DEBUG) {
                Log.d(HwBcmGnssManager.TAG, "cmdID= " + cmdID + ", len= " + len);
            }
            if (len >= 0 && len <= 248) {
                System.arraycopy(bArr, HwBcmGnssManager.LBS_AIDING_FREQ, msg, HwBcmGnssManager.LBS_TIME_UNKNOWN, len);
                switch (cmdID) {
                    case HwBcmGnssManager.LBS_POS_SOURCE /*256*/:
                        if (len == HwBcmGnssManager.LBS_TIME_FROM_TOW) {
                            int lbsPosFixSource = vtolh(msg, HwBcmGnssManager.LBS_TIME_UNKNOWN);
                            HwBcmGnssManager.this.handlerBcmPosSource(lbsPosFixSource);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: lbsPosFixSource is :" + lbsPosFixSource);
                                break;
                            }
                        }
                        break;
                    case HwBcmGnssManager.LBS_TIME_ROURCE /*257*/:
                        if (len == HwBcmGnssManager.LBS_TIME_FROM_TOW) {
                            int lbsTimeValidity = vtolh(msg, HwBcmGnssManager.LBS_TIME_UNKNOWN);
                            HwBcmGnssManager.this.handlerBcmTimeSource(lbsTimeValidity);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: lbsTimeValidity is :" + lbsTimeValidity);
                                break;
                            }
                        }
                        break;
                    case HwBcmGnssManager.LBS_TCXO_OFFSET /*258*/:
                        if (len == HwBcmGnssManager.LBS_TIME_FROM_TOW) {
                            int tcxo = vtolh(msg, HwBcmGnssManager.LBS_TIME_UNKNOWN);
                            HwBcmGnssManager.this.handlerBcmTcxoOffset(tcxo);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: tcxo is :" + tcxo);
                                break;
                            }
                        }
                        break;
                    case HwBcmGnssManager.LBS_AIDING_STATUS /*259*/:
                        if (len == HwBcmGnssManager.LBS_TIME_FROM_TOW) {
                            int aidingStatus = vtolh(msg, HwBcmGnssManager.LBS_TIME_UNKNOWN);
                            HwBcmGnssManager.this.handlerBcmAidingStatus(aidingStatus);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: aidingStatus is :" + aidingStatus);
                                break;
                            }
                        }
                        break;
                    case HwBcmGnssManager.LBS_AGC_DATA /*260*/:
                        if (len == 12) {
                            float agcGPS = Float.intBitsToFloat(vtolh(msg, HwBcmGnssManager.LBS_TIME_UNKNOWN));
                            float agcGLO = Float.intBitsToFloat(vtolh(msg, HwBcmGnssManager.LBS_TIME_FROM_TOW));
                            float agcBDS = Float.intBitsToFloat(vtolh(msg, HwBcmGnssManager.LBS_AIDING_FREQ));
                            HwBcmGnssManager.this.handlerBcmAgcData(agcGPS, agcGLO, agcBDS);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: agcGPS is :" + agcGPS + "agcGPS is :" + agcGLO + "agcBDS is :" + agcBDS);
                                break;
                            }
                        }
                        break;
                    case HwBcmGnssManager.LBS_DOP_DATA /*261*/:
                        if (len == 12 && HwBcmGnssManager.DEBUG) {
                            Log.d(HwBcmGnssManager.TAG, "Server: received: LBS_DOP_DATA");
                            break;
                        }
                    case HwBcmGnssManager.LBS_ASSERT_MSG /*512*/:
                        if (len != 0) {
                            String assertmsg = toStr(msg, HwBcmGnssManager.LBS_TIME_UNKNOWN, len);
                            if (HwBcmGnssManager.DEBUG) {
                                Log.d(HwBcmGnssManager.TAG, "Server: received: assertmsg is :" + assertmsg);
                            }
                            HwBcmGnssManager.this.handlerBcmAssert(assertmsg);
                            break;
                        }
                        break;
                    default:
                        Log.d(HwBcmGnssManager.TAG, "cmdID id is not defiend !");
                        break;
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwBcmGnssManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwBcmGnssManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwBcmGnssManager.<clinit>():void");
    }

    HwBcmGnssManager(Handler handler, GpsSessionEvent gpsSessionEvent, GpsPosErrorEvent gpsPosErrorEvent) {
        this.mBcmGnssHander = handler;
        this.mBcmGnssSession = gpsSessionEvent;
        this.mBcmGnssErr = gpsPosErrorEvent;
    }

    public void bcmGnssSocketinit() {
        for (int i = LBS_TIME_FROM_STANDBY; i <= 9; i += LBS_TIME_FROM_STANDBY) {
            if (!bcmGnssSocketServer()) {
                Log.d(TAG, "bcmGnssSocketinit: CONNECT SERVER FAILD, retry count = " + i);
            }
        }
    }

    private boolean bcmGnssSocketServer() {
        try {
            this.serverSocket = new LocalServerSocket("gpssock:huawei");
            FileDescriptor bcmGnssSocket_fd = this.serverSocket.getFileDescriptor();
            if (DEBUG) {
                Log.d(TAG, "serverSocket.getFileDescriptor() IS : " + bcmGnssSocket_fd);
            }
            if (bcmGnssSocket_fd != null && handle_bcm_process() == 0) {
                Log.e(TAG, "bcmGnssSocketServer error!");
            }
            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return DEBUG;
                }
            }
            return DEBUG;
        } catch (IOException e2) {
            e2.printStackTrace();
            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                    return DEBUG;
                }
            }
            return DEBUG;
        } catch (Throwable th) {
            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                    return DEBUG;
                }
            }
        }
    }

    private int handle_bcm_process() {
        while (true) {
            try {
                new ServerThread(this.serverSocket.accept(), null).start();
            } catch (IOException e) {
                e.printStackTrace();
                return LBS_TIME_FROM_STANDBY;
            } catch (Throwable th) {
                return LBS_TIME_FROM_STANDBY;
            }
        }
    }

    private void handlerBcmPosSource(int posSource) {
        this.mBcmGnssSession.setBrcmPosSource(posSource);
    }

    private void handlerBcmTimeSource(int timeSource) {
        this.mBcmGnssSession.setBrcmTimeSource(timeSource);
    }

    private void handlerBcmAidingStatus(int status) {
        this.mBcmGnssSession.setBrcmAidingStatus(status);
    }

    private void handlerBcmTcxoOffset(int offset) {
        this.mBcmGnssSession.setBrcmTcxoOffset(offset);
    }

    private void handlerBcmAgcData(float gps, float glo, float dbs) {
        this.mBcmGnssSession.setBrcmAgcData(gps, glo, dbs);
    }

    private void handlerBcmAssert(String assertInfo) {
        boolean triggerNeeded = true;
        if (assertInfo.contains(CALL_SENTRY)) {
            triggerNeeded = DEBUG;
        }
        this.mBcmGnssErr.setBrcmAssertInfo(assertInfo);
        sendMsgToTriggerErr(30, triggerNeeded);
    }

    private void sendMsgToTriggerErr(int errorcode, boolean trigger) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(errorcode));
        list.add(Boolean.valueOf(trigger));
        msg.what = 22;
        msg.obj = list;
        this.mBcmGnssHander.sendMessage(msg);
    }
}
