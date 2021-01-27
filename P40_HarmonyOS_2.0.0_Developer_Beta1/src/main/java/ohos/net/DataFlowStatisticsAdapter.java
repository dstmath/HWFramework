package ohos.net;

import android.net.TrafficStats;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

public class DataFlowStatisticsAdapter {
    public static void clearDataFlowTag() {
        TrafficStats.clearThreadStatsTag();
    }

    public static int getDataFlowTag() {
        return TrafficStats.getThreadStatsTag();
    }

    public static void addOperation(int i) {
        TrafficStats.incrementOperationCount(i);
    }

    public static void addOperation(int i, int i2) {
        TrafficStats.incrementOperationCount(i, i2);
    }

    public static void setDataFlowTag(int i) {
        TrafficStats.setThreadStatsTag(i);
    }

    public static void setDatagramSocketTag(DatagramSocket datagramSocket) throws SocketException {
        TrafficStats.tagDatagramSocket(datagramSocket);
    }

    public static void setSocketTag(Socket socket) throws SocketException {
        TrafficStats.tagSocket(socket);
    }

    public static void removeDatagramSocketTag(DatagramSocket datagramSocket) throws SocketException {
        TrafficStats.untagDatagramSocket(datagramSocket);
    }

    public static void removeSocketTag(Socket socket) throws SocketException {
        TrafficStats.untagSocket(socket);
    }
}
