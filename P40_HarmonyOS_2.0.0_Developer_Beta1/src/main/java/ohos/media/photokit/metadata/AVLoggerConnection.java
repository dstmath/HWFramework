package ohos.media.photokit.metadata;

import ohos.ai.cv.common.ConnectionCallback;
import ohos.app.Context;
import ohos.media.photokit.adapter.AVLoggerConnectionAdapter;
import ohos.media.photokit.common.AVLogCompletedListener;
import ohos.media.photokit.common.AVLoggerConnectionClient;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVLoggerConnection {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVLoggerConnection.class);
    private AVLoggerConnection avLoggerConnection;
    private AVLoggerConnectionAdapter avLoggerConnectionAdapter;
    private ConnectionCallback connectionCallback;

    public void onServiceConnect() {
    }

    public void onServiceDisconnect() {
    }

    public AVLoggerConnection(Context context, AVLoggerConnectionClient aVLoggerConnectionClient) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        } else if (aVLoggerConnectionClient != null) {
            this.avLoggerConnection = this;
            this.connectionCallback = new ConnectionCallback() {
                /* class ohos.media.photokit.metadata.AVLoggerConnection.AnonymousClass1 */

                public void onServiceConnect() {
                    AVLoggerConnection.this.avLoggerConnection.onServiceConnect();
                }

                public void onServiceDisconnect() {
                    AVLoggerConnection.this.avLoggerConnection.onServiceDisconnect();
                }
            };
            this.avLoggerConnectionAdapter = new AVLoggerConnectionAdapter(context, aVLoggerConnectionClient, this.connectionCallback);
        } else {
            throw new IllegalArgumentException("client is null");
        }
    }

    public void performLoggerFile(String str, String str2) {
        if (str != null) {
            this.avLoggerConnectionAdapter.performLoggerFile(str, str2);
        } else {
            LOGGER.error("public setSource error: path is null", new Object[0]);
            throw new IllegalArgumentException("Set source, path cannot be null");
        }
    }

    public static void performLoggerFile(Context context, String[] strArr, String[] strArr2, AVLogCompletedListener aVLogCompletedListener) {
        if (context == null) {
            LOGGER.error("public setSource error: context is null", new Object[0]);
            throw new IllegalArgumentException("context is null");
        } else if (strArr == null) {
            LOGGER.error("public setSource error: path is null", new Object[0]);
            throw new IllegalArgumentException("Set source, path cannot be null");
        } else if (strArr2 == null || strArr2.length == strArr.length) {
            AVLoggerConnectionAdapter.performLoggerFile(context, strArr, strArr2, aVLogCompletedListener);
        } else {
            LOGGER.error("public setSource error: the input length is inconsistent", new Object[0]);
            throw new IllegalArgumentException("the input length is inconsistent");
        }
    }

    public void connect() {
        this.avLoggerConnectionAdapter.connect();
    }

    public void disconnect() {
        this.avLoggerConnectionAdapter.disconnect();
    }

    public boolean isConnected() {
        return this.avLoggerConnectionAdapter.isConnected();
    }
}
