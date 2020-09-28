package huawei.android.app;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.SystemProperties;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class HwCustEmergDataManagerImpl extends HwCustEmergDataManager {
    private static final String CONTACTS_PROVIDER_PATH = "/data/data/com.android.providers.contacts";
    private static final String CONTACTS_PROVIDER_PATH_BK = "/data_bk/data/com.android.providers.contacts";
    private static final String EMERGENCY_CRASH_STATE = "2";
    private static final String EMERGENCY_MOUNT_STATE = "1";
    private static final String EMERGENCY_PKG_NAME = "EmergencyData.apk";
    private static final String EMERGENCY_STATE_PROP_NAME = "sys.emergency.mountdata";
    private static final boolean HWFLOW;
    private static final boolean HWLOGW_E = true;
    private static final String PKGXML_PATH = "/data/system/packages.xml";
    private static final String PKGXML_PATH_BK = "/data_bk/system/packages.xml";
    private static final String TAG = "HwCustEmergDataManager";
    private static final String TAG_FLOW = "HwCustEmergDataManager_FLOW";
    private static final String TAG_INIT = "HwCustEmergDataManager_INIT";
    private static final String TELEPHONY_PROVIDER_PATH = "/data/data/com.android.providers.telephony";
    private static final String TELEPHONY_PROVIDER_PATH_BK = "/data_bk/data/com.android.providers.telephony";
    private static boolean localLOGV;

    static {
        boolean z = Log.HWINFO;
        boolean z2 = HWFLOW;
        HWFLOW = z || (Log.HWModuleLog && Log.isLoggable(TAG, 4));
        if (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) {
            z2 = true;
        }
        localLOGV = z2;
    }

    public boolean isEmergencyState() {
        String emergencyState = SystemProperties.get(EMERGENCY_STATE_PROP_NAME);
        if (EMERGENCY_MOUNT_STATE.equals(emergencyState) || EMERGENCY_CRASH_STATE.equals(emergencyState)) {
            return HWLOGW_E;
        }
        return HWFLOW;
    }

    public boolean isEmergencyMountState() {
        if (EMERGENCY_MOUNT_STATE.equals(SystemProperties.get(EMERGENCY_STATE_PROP_NAME))) {
            return HWLOGW_E;
        }
        return HWFLOW;
    }

    public void backupEmergencyDataFile() {
        if (HWFLOW) {
            Log.i(TAG_FLOW, "begin to copy some files");
        }
        FileBackup fileBackup = new FileBackup();
        if (fileBackup.init()) {
            if (HWFLOW) {
                Log.i(TAG_FLOW, "FileBackup init success");
            }
            fileBackup.copy(CONTACTS_PROVIDER_PATH_BK, CONTACTS_PROVIDER_PATH);
            fileBackup.copy(TELEPHONY_PROVIDER_PATH_BK, TELEPHONY_PROVIDER_PATH);
            fileBackup.copy(PKGXML_PATH_BK, PKGXML_PATH);
            fileBackup.disconnectSocket();
        }
    }

    public String getEmergencyPkgName() {
        return EMERGENCY_PKG_NAME;
    }

    private static class FileBackup {
        private static final String COPY = "copy ";
        private static final boolean HWFLOW;
        private static final boolean HWLOGW_E = true;
        private static final int MAX_BUF_LENGTH = 8448;
        private static final int MAX_LENGTH = 8192;
        private static final int ONE_INT_LENGTH = 4;
        private static final int STRING_LENGTH = 256;
        private static final String TAG = "FileBackup";
        private static final String TAG_FLOW = "FileBackup_FLOW";
        private static final String TAG_INIT = "FileBackup_INIT";
        private static boolean localLOGV;
        private InputStream mIn;
        private OutputStream mOut;
        private LocalSocket mSocket;

        static {
            boolean z = Log.HWINFO;
            boolean z2 = HWFLOW;
            HWFLOW = z || (Log.HWModuleLog && Log.isLoggable(TAG, ONE_INT_LENGTH));
            if (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) {
                z2 = true;
            }
            localLOGV = z2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean init() {
            if (!connectSocket()) {
                Log.e(TAG, "connectSocket failed");
                return HWFLOW;
            } else if (!HWFLOW) {
                return HWLOGW_E;
            } else {
                Log.i(TAG_FLOW, "connectSocket success");
                return HWLOGW_E;
            }
        }

        private boolean connectSocket() {
            if (this.mSocket != null) {
                return HWLOGW_E;
            }
            if (HWFLOW) {
                Log.i(TAG_FLOW, "connecting...");
            }
            try {
                this.mSocket = new LocalSocket();
                this.mSocket.connect(new LocalSocketAddress("filebackup", LocalSocketAddress.Namespace.RESERVED));
                this.mIn = this.mSocket.getInputStream();
                this.mOut = this.mSocket.getOutputStream();
                return HWLOGW_E;
            } catch (IOException ex) {
                Log.e(TAG, "connecting error : ", ex);
                disconnectSocket();
                return HWFLOW;
            } catch (Exception ex2) {
                Log.e(TAG, "connecting error : ", ex2);
                disconnectSocket();
                return HWFLOW;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void disconnectSocket() {
            if (HWFLOW) {
                Log.i(TAG_FLOW, "disconnecting...");
            }
            try {
                if (this.mSocket != null) {
                    this.mSocket.close();
                    this.mSocket = null;
                }
            } catch (IOException ex) {
                Log.e(TAG, "disconnectSocket error : ", ex);
            } catch (Exception ex2) {
                Log.e(TAG, "disconnectSocket error : ", ex2);
            }
            try {
                if (this.mIn != null) {
                    this.mIn.close();
                    this.mIn = null;
                }
            } catch (IOException ex3) {
                Log.e(TAG, "disconnectSocket error : ", ex3);
            } catch (Exception ex4) {
                Log.e(TAG, "disconnectSocket error : ", ex4);
            }
            try {
                if (this.mOut != null) {
                    this.mOut.close();
                    this.mOut = null;
                }
            } catch (IOException ex5) {
                Log.e(TAG, "disconnectSocket error : ", ex5);
            } catch (Exception ex6) {
                Log.e(TAG, "disconnectSocket error : ", ex6);
            }
        }

        private int writeCommand(String command, byte[] buf) {
            try {
                byte[] cmd = command.getBytes("UTF-8");
                int len = cmd.length;
                if (len < 1 || len > MAX_BUF_LENGTH) {
                    return 0;
                }
                buf[0] = (byte) (len & 255);
                buf[1] = (byte) ((len >> 8) & 255);
                try {
                    this.mOut.write(buf, 0, 2);
                    this.mOut.write(cmd, 0, len);
                    this.mOut.flush();
                    return readReply(buf);
                } catch (IOException ex) {
                    Log.e(TAG, "write error! command : " + command, ex);
                    disconnectSocket();
                    return 0;
                } catch (Exception ex2) {
                    Log.e(TAG, "write error! command : " + command, ex2);
                    disconnectSocket();
                    return 0;
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "writeCommand err : ", e);
                return 0;
            }
        }

        private int readReply(byte[] buf) {
            if (!readBytes(buf, 2)) {
                Log.e(TAG, "!readBytes(buf, 2) error");
                return 0;
            }
            int buflen = (buf[0] & 255) | ((buf[1] & 255) << 8);
            if (buflen < 1 || buflen > MAX_BUF_LENGTH) {
                Log.e(TAG, "invalid reply length (" + buflen + ")");
                disconnectSocket();
                return 0;
            } else if (readBytes(buf, buflen)) {
                return buflen;
            } else {
                Log.e(TAG, "!readBytes(buf," + buflen + ") error");
                return 0;
            }
        }

        private boolean readBytes(byte[] buffer, int len) {
            int off = 0;
            if (len < 0) {
                return HWFLOW;
            }
            while (true) {
                if (off == len) {
                    break;
                }
                try {
                    int count = this.mIn.read(buffer, off, len - off);
                    if (count <= 0) {
                        Log.e(TAG, "read error " + count);
                        break;
                    }
                    off += count;
                } catch (IOException ex) {
                    Log.e(TAG, "read exception", ex);
                }
            }
            if (off == len) {
                return HWLOGW_E;
            }
            disconnectSocket();
            Log.e(TAG, "read bytes error, off != len");
            return HWFLOW;
        }

        private int bytesToInt(byte[] intByte) {
            int fromByte = 0;
            for (int i = 0; i < ONE_INT_LENGTH; i++) {
                fromByte += (intByte[i] & 255) << (i * 8);
            }
            return fromByte;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean copy(String srcPath, String dstPath) {
            byte[] copyBuf = new byte[MAX_BUF_LENGTH];
            writeCommand(COPY + srcPath + " " + dstPath, copyBuf);
            byte[] returnValue = new byte[ONE_INT_LENGTH];
            System.arraycopy(copyBuf, 0, returnValue, 0, ONE_INT_LENGTH);
            if (bytesToInt(returnValue) != 0) {
                Log.e(TAG, "copy from " + srcPath + " to " + dstPath + " failed");
                return HWFLOW;
            } else if (!HWFLOW) {
                return HWLOGW_E;
            } else {
                Log.i(TAG_FLOW, "copy from " + srcPath + " to " + dstPath + " success");
                return HWLOGW_E;
            }
        }
    }
}
