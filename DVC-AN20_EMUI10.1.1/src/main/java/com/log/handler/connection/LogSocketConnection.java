package com.log.handler.connection;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.log.handler.LogHandlerUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LogSocketConnection extends AbstractLogConnection {
    private static final int BUFFER_SIZE = 1024;
    public static final int MSG_SOCKET_READ = 1;
    public static final int MSG_SOCKET_START_LISTEN = 1;
    private static final String TAG = "LogHandler/LogSocketConnection";
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private LocalSocket mSocket;
    private SocketListenHandler mSocketListenHandler;
    private SocketMessageHandler mSocketMessageHandler;

    public LogSocketConnection(String serverName) {
        super(serverName);
        HandlerThread listenHandlerThread = new HandlerThread("Socket_Listen_Handler");
        listenHandlerThread.start();
        this.mSocketListenHandler = new SocketListenHandler(listenHandlerThread.getLooper());
        HandlerThread socketMessageHandlerThread = new HandlerThread("Socket_Message_Handler");
        socketMessageHandlerThread.start();
        this.mSocketMessageHandler = new SocketMessageHandler(socketMessageHandlerThread.getLooper());
    }

    @Override // com.log.handler.connection.AbstractLogConnection, com.log.handler.connection.ILogConnection
    public boolean connect() {
        this.mSocket = new LocalSocket();
        try {
            this.mSocket.connect(new LocalSocketAddress(this.mServerName, LocalSocketAddress.Namespace.ABSTRACT));
            this.mOutputStream = this.mSocket.getOutputStream();
            this.mInputStream = this.mSocket.getInputStream();
            this.mSocketListenHandler.sendEmptyMessage(1);
            return true;
        } catch (IOException e) {
            LogHandlerUtils.logw(TAG, "Exception happens when connect to socket server : " + this.mServerName);
            disConnect();
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void listen() {
        byte[] buffer = new byte[BUFFER_SIZE];
        LogHandlerUtils.logi(TAG, "Socket listen start");
        while (true) {
            try {
                int count = this.mInputStream.read(buffer, 0, BUFFER_SIZE);
                if (count < 0) {
                    break;
                }
                LogHandlerUtils.logd(TAG, "Response from native byte size = " + count);
                byte[] resp = new byte[count];
                System.arraycopy(buffer, 0, resp, 0, count);
                this.mSocketMessageHandler.obtainMessage(1, new String(resp)).sendToTarget();
            } catch (IOException ex) {
                LogHandlerUtils.loge(TAG, "read failed", ex);
            }
        }
        LogHandlerUtils.logw(TAG, "Get a empty response from native layer, socket connection lost!");
        disConnect();
    }

    @Override // com.log.handler.connection.AbstractLogConnection, com.log.handler.connection.ILogConnection
    public boolean isConnection() {
        return this.mSocket != null;
    }

    /* access modifiers changed from: protected */
    @Override // com.log.handler.connection.AbstractLogConnection
    public boolean sendDataToServer(String data) {
        LogHandlerUtils.logd(TAG, "sendDataToServer() mServerName = " + this.mServerName + ", data = " + data);
        boolean sendSuccess = false;
        try {
            Thread.sleep(50);
            OutputStream outputStream = this.mOutputStream;
            outputStream.write((data + "\u0000").getBytes());
            this.mOutputStream.flush();
            sendSuccess = true;
        } catch (IOException e) {
            LogHandlerUtils.loge(TAG, "IOException while sending command to native.", e);
            disConnect();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        LogHandlerUtils.logd(TAG, "sendToServer done! sendSuccess = " + sendSuccess);
        return sendSuccess;
    }

    @Override // com.log.handler.connection.AbstractLogConnection, com.log.handler.connection.ILogConnection
    public synchronized void disConnect() {
        if (this.mSocket != null) {
            try {
                this.mSocket.shutdownInput();
                this.mSocket.shutdownOutput();
                this.mSocket.close();
            } catch (IOException e) {
                LogHandlerUtils.loge(TAG, "Exception happended while closing socket: " + e);
            }
        }
        this.mSocket = null;
        super.disConnect();
    }

    /* access modifiers changed from: package-private */
    public class SocketMessageHandler extends Handler {
        public SocketMessageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int what = msg.what;
            Object obj = msg.obj;
            LogHandlerUtils.logd(LogSocketConnection.TAG, "SocketMessageHandler receive message, what = " + what + ",obj = " + obj);
            if (what == 1 && obj != null && (obj instanceof String)) {
                LogSocketConnection.this.setResponseFromServer((String) obj);
            }
        }
    }

    class SocketListenHandler extends Handler {
        public SocketListenHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int what = msg.what;
            LogHandlerUtils.logi(LogSocketConnection.TAG, "SocketListenHandler receive message, what = " + what);
            if (what == 1) {
                LogSocketConnection.this.listen();
            }
        }
    }
}
