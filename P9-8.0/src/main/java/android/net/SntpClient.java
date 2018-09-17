package android.net;

import android.os.SystemClock;
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
            return requestTime(InetAddress.getByName(host), 123, timeout);
        } catch (Exception e) {
            EventLogTags.writeNtpFailure(host, e.toString());
            Log.d(TAG, "request time failed: " + e);
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x018b  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0195  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean requestTime(InetAddress address, int port, int timeout) {
        Exception e;
        Throwable th;
        Log.d(TAG, "ntp request time : " + address + "port : " + port + " timeout : " + timeout);
        if (address != null) {
            this.mNtpIpAddress = address.toString();
        }
        DatagramSocket socket = null;
        int oldTag = TrafficStats.getAndSetThreadStatsTag(TrafficStats.TAG_SYSTEM_NTP);
        try {
            DatagramSocket socket2 = new DatagramSocket();
            try {
                socket2.setSoTimeout(timeout);
                byte[] buffer = new byte[48];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
                buffer[0] = (byte) 27;
                long requestTime = System.currentTimeMillis();
                long requestTicks = SystemClock.elapsedRealtime();
                writeTimeStamp(buffer, 40, requestTime);
                socket2.send(request);
                socket2.receive(new DatagramPacket(buffer, buffer.length));
                long responseTicks = SystemClock.elapsedRealtime();
                long responseTime = requestTime + (responseTicks - requestTicks);
                byte leap = (byte) ((buffer[0] >> 6) & 3);
                byte mode = (byte) (buffer[0] & 7);
                int stratum = buffer[1] & 255;
                long originateTime = readTimeStamp(buffer, 24);
                long receiveTime = readTimeStamp(buffer, 32);
                long transmitTime = readTimeStamp(buffer, 40);
                checkValidServerReply(leap, mode, stratum, transmitTime);
                long roundTripTime = (responseTicks - requestTicks) - (transmitTime - receiveTime);
                long clockOffset = ((receiveTime - originateTime) + (transmitTime - responseTime)) / 2;
                EventLogTags.writeNtpSuccess(address.toString(), roundTripTime, clockOffset);
                Log.d(TAG, "round trip: " + roundTripTime + "ms, " + "clock offset: " + clockOffset + "ms");
                this.mNtpTime = responseTime + clockOffset;
                this.mNtpTimeReference = responseTicks;
                this.mRoundTripTime = roundTripTime;
                if (socket2 != null) {
                    socket2.close();
                }
                TrafficStats.setThreadStatsTag(oldTag);
                return true;
            } catch (Exception e2) {
                e = e2;
                socket = socket2;
                try {
                    EventLogTags.writeNtpFailure(address.toString(), e.toString());
                    Log.d(TAG, "request time failed: " + e);
                    if (socket != null) {
                    }
                    TrafficStats.setThreadStatsTag(oldTag);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (socket != null) {
                        socket.close();
                    }
                    TrafficStats.setThreadStatsTag(oldTag);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                socket = socket2;
                if (socket != null) {
                }
                TrafficStats.setThreadStatsTag(oldTag);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            EventLogTags.writeNtpFailure(address.toString(), e.toString());
            Log.d(TAG, "request time failed: " + e);
            if (socket != null) {
                socket.close();
            }
            TrafficStats.setThreadStatsTag(oldTag);
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
        if (leap == (byte) 3) {
            throw new InvalidServerReplyException("unsynchronized server");
        } else if (mode != (byte) 4 && mode != (byte) 5) {
            throw new InvalidServerReplyException("untrusted mode: " + mode);
        } else if (stratum == 0 || stratum > 15) {
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
        byte b3 = buffer[offset + 3];
        if ((b0 & 128) == 128) {
            i0 = (b0 & 127) + 128;
        } else {
            byte i02 = b0;
        }
        if ((b1 & 128) == 128) {
            i1 = (b1 & 127) + 128;
        } else {
            byte i12 = b1;
        }
        if ((b2 & 128) == 128) {
            i2 = (b2 & 127) + 128;
        } else {
            byte i22 = b2;
        }
        if ((b3 & 128) == 128) {
            i3 = (b3 & 127) + 128;
        } else {
            byte i32 = b3;
        }
        return (((((long) i02) << 24) + (((long) i12) << 16)) + (((long) i22) << 8)) + ((long) i32);
    }

    private long readTimeStamp(byte[] buffer, int offset) {
        long seconds = read32(buffer, offset);
        long fraction = read32(buffer, offset + 4);
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
        buffer[offset] = (byte) ((int) (seconds >> 24));
        offset = i + 1;
        buffer[i] = (byte) ((int) (seconds >> 16));
        i = offset + 1;
        buffer[offset] = (byte) ((int) (seconds >> 8));
        offset = i + 1;
        buffer[i] = (byte) ((int) (seconds >> null));
        long fraction = (4294967296L * milliseconds) / 1000;
        i = offset + 1;
        buffer[offset] = (byte) ((int) (fraction >> 24));
        offset = i + 1;
        buffer[i] = (byte) ((int) (fraction >> 16));
        i = offset + 1;
        buffer[offset] = (byte) ((int) (fraction >> 8));
        offset = i + 1;
        buffer[i] = (byte) ((int) (Math.random() * 255.0d));
    }
}
