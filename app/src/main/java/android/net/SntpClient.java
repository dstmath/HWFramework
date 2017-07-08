package android.net;

import android.net.wifi.ScanResult.InformationElement;
import android.os.Process;
import android.os.SystemClock;
import android.security.keymaster.KeymasterDefs;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class SntpClient {
    private static final boolean DBG = true;
    private static final int NTP_LEAP_NOSYNC = 3;
    private static final int NTP_MODE_BROADCAST = 5;
    private static final int NTP_MODE_CLIENT = 3;
    private static final int NTP_MODE_SERVER = 4;
    private static final int NTP_PACKET_SIZE = 48;
    private static final int NTP_PORT = 123;
    private static final int NTP_STRATUM_DEATH = 0;
    private static final int NTP_STRATUM_MAX = 15;
    private static final int NTP_VERSION = 3;
    private static final long OFFSET_1900_TO_1970 = 2208988800L;
    private static final int ORIGINATE_TIME_OFFSET = 24;
    private static final int RECEIVE_TIME_OFFSET = 32;
    private static final int REFERENCE_TIME_OFFSET = 16;
    private static final String TAG = "SntpClient";
    private static final int TRANSMIT_TIME_OFFSET = 40;
    private String mNtpIpAddress;
    private long mNtpTime;
    private long mNtpTimeReference;
    private long mRoundTripTime;

    private static class InvalidServerReplyException extends Exception {
        public InvalidServerReplyException(String message) {
            super(message);
        }
    }

    public boolean requestTime(String host, int timeout) {
        try {
            return requestTime(InetAddress.getByName(host), NTP_PORT, timeout);
        } catch (Exception e) {
            Log.d(TAG, "request time failed: " + e);
            return false;
        }
    }

    public boolean requestTime(InetAddress address, int port, int timeout) {
        Exception e;
        Throwable th;
        Log.d(TAG, "ntp request time : " + address + "port : " + port + " timeout : " + timeout);
        if (address != null) {
            this.mNtpIpAddress = address.toString();
        }
        DatagramSocket socket = null;
        try {
            DatagramSocket socket2 = new DatagramSocket();
            try {
                socket2.setSoTimeout(timeout);
                byte[] buffer = new byte[NTP_PACKET_SIZE];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
                buffer[NTP_STRATUM_DEATH] = (byte) 27;
                long requestTime = System.currentTimeMillis();
                long requestTicks = SystemClock.elapsedRealtime();
                writeTimeStamp(buffer, TRANSMIT_TIME_OFFSET, requestTime);
                socket2.send(request);
                socket2.receive(new DatagramPacket(buffer, buffer.length));
                long responseTicks = SystemClock.elapsedRealtime();
                long responseTime = requestTime + (responseTicks - requestTicks);
                byte leap = (byte) ((buffer[NTP_STRATUM_DEATH] >> 6) & NTP_VERSION);
                byte mode = (byte) (buffer[NTP_STRATUM_DEATH] & 7);
                int stratum = buffer[1] & Process.PROC_TERM_MASK;
                long originateTime = readTimeStamp(buffer, ORIGINATE_TIME_OFFSET);
                long receiveTime = readTimeStamp(buffer, RECEIVE_TIME_OFFSET);
                long transmitTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET);
                checkValidServerReply(leap, mode, stratum, transmitTime);
                long roundTripTime = (responseTicks - requestTicks) - (transmitTime - receiveTime);
                long clockOffset = ((receiveTime - originateTime) + (transmitTime - responseTime)) / 2;
                Log.d(TAG, "round trip: " + roundTripTime + "ms, " + "clock offset: " + clockOffset + "ms");
                this.mNtpTime = responseTime + clockOffset;
                this.mNtpTimeReference = responseTicks;
                this.mRoundTripTime = roundTripTime;
                if (socket2 != null) {
                    socket2.close();
                }
                return DBG;
            } catch (Exception e2) {
                e = e2;
                socket = socket2;
                try {
                    Log.d(TAG, "request time failed: " + e);
                    if (socket != null) {
                        socket.close();
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (socket != null) {
                        socket.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                socket = socket2;
                if (socket != null) {
                    socket.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.d(TAG, "request time failed: " + e);
            if (socket != null) {
                socket.close();
            }
            return false;
        }
    }

    public String getNtpIpAddress() {
        return this.mNtpIpAddress;
    }

    public long getNtpTime() {
        return this.mNtpTime;
    }

    public long getNtpTimeReference() {
        return this.mNtpTimeReference;
    }

    public long getRoundTripTime() {
        return this.mRoundTripTime;
    }

    private static void checkValidServerReply(byte leap, byte mode, int stratum, long transmitTime) throws InvalidServerReplyException {
        if (leap == NTP_VERSION) {
            throw new InvalidServerReplyException("unsynchronized server");
        } else if (mode != NTP_MODE_SERVER && mode != NTP_MODE_BROADCAST) {
            throw new InvalidServerReplyException("untrusted mode: " + mode);
        } else if (stratum == 0 || stratum > NTP_STRATUM_MAX) {
            throw new InvalidServerReplyException("untrusted stratum: " + stratum);
        } else if (transmitTime == 0) {
            throw new InvalidServerReplyException("zero transmitTime");
        }
    }

    private long read32(byte[] buffer, int offset) {
        int i0;
        int i1;
        int i2;
        int i3;
        byte b0 = buffer[offset];
        byte b1 = buffer[offset + 1];
        byte b2 = buffer[offset + 2];
        byte b3 = buffer[offset + NTP_VERSION];
        if ((b0 & KeymasterDefs.KM_ALGORITHM_HMAC) == KeymasterDefs.KM_ALGORITHM_HMAC) {
            i0 = (b0 & InformationElement.EID_EXTENDED_CAPS) + KeymasterDefs.KM_ALGORITHM_HMAC;
        } else {
            byte i02 = b0;
        }
        if ((b1 & KeymasterDefs.KM_ALGORITHM_HMAC) == KeymasterDefs.KM_ALGORITHM_HMAC) {
            i1 = (b1 & InformationElement.EID_EXTENDED_CAPS) + KeymasterDefs.KM_ALGORITHM_HMAC;
        } else {
            byte i12 = b1;
        }
        if ((b2 & KeymasterDefs.KM_ALGORITHM_HMAC) == KeymasterDefs.KM_ALGORITHM_HMAC) {
            i2 = (b2 & InformationElement.EID_EXTENDED_CAPS) + KeymasterDefs.KM_ALGORITHM_HMAC;
        } else {
            byte i22 = b2;
        }
        if ((b3 & KeymasterDefs.KM_ALGORITHM_HMAC) == KeymasterDefs.KM_ALGORITHM_HMAC) {
            i3 = (b3 & InformationElement.EID_EXTENDED_CAPS) + KeymasterDefs.KM_ALGORITHM_HMAC;
        } else {
            byte i32 = b3;
        }
        return (((((long) i0) << ORIGINATE_TIME_OFFSET) + (((long) i1) << REFERENCE_TIME_OFFSET)) + (((long) i2) << 8)) + ((long) i3);
    }

    private long readTimeStamp(byte[] buffer, int offset) {
        long seconds = read32(buffer, offset);
        long fraction = read32(buffer, offset + NTP_MODE_SERVER);
        if (seconds == 0 && fraction == 0) {
            return 0;
        }
        return ((seconds - OFFSET_1900_TO_1970) * 1000) + ((fraction * 1000) / 4294967296L);
    }

    private void writeTimeStamp(byte[] buffer, int offset, long time) {
        if (time == 0) {
            Arrays.fill(buffer, offset, offset + 8, (byte) 0);
            return;
        }
        long seconds = time / 1000;
        long milliseconds = time - (1000 * seconds);
        seconds += OFFSET_1900_TO_1970;
        int i = offset + 1;
        buffer[offset] = (byte) ((int) (seconds >> ORIGINATE_TIME_OFFSET));
        offset = i + 1;
        buffer[i] = (byte) ((int) (seconds >> REFERENCE_TIME_OFFSET));
        i = offset + 1;
        buffer[offset] = (byte) ((int) (seconds >> 8));
        offset = i + 1;
        buffer[i] = (byte) ((int) (seconds >> NTP_STRATUM_DEATH));
        long fraction = (4294967296L * milliseconds) / 1000;
        i = offset + 1;
        buffer[offset] = (byte) ((int) (fraction >> ORIGINATE_TIME_OFFSET));
        offset = i + 1;
        buffer[i] = (byte) ((int) (fraction >> REFERENCE_TIME_OFFSET));
        i = offset + 1;
        buffer[offset] = (byte) ((int) (fraction >> 8));
        offset = i + 1;
        buffer[i] = (byte) ((int) (Math.random() * 255.0d));
    }
}
