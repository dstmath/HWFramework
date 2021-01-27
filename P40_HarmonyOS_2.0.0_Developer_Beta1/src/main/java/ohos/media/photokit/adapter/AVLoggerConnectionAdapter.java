package ohos.media.photokit.adapter;

import android.content.ComponentName;
import android.media.MediaScannerConnection;
import android.os.IBinder;
import ohos.ai.cv.common.ConnectionCallback;
import ohos.app.Context;
import ohos.media.photokit.common.AVLogCompletedListener;
import ohos.media.photokit.common.AVLoggerConnectionClient;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.net.Uri;

public class AVLoggerConnectionAdapter extends MediaScannerConnection {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVLoggerConnectionAdapter.class);
    private ConnectionCallback aCallback;
    private MediaScannerConnection.MediaScannerConnectionClient aClient;
    private MediaScannerConnection aScanConn;

    /* access modifiers changed from: private */
    public static Uri uriDBShell(Uri uri) {
        return Uri.parse(uri.toString().replace("content://", "dataability:///"));
    }

    public AVLoggerConnectionAdapter(Context context, AVLoggerConnectionClient aVLoggerConnectionClient, ConnectionCallback connectionCallback) {
        super(initContext(context), initClient(aVLoggerConnectionClient));
        this.aCallback = connectionCallback;
    }

    private static MediaScannerConnection.MediaScannerConnectionClient initClient(final AVLoggerConnectionClient aVLoggerConnectionClient) {
        return new MediaScannerConnection.MediaScannerConnectionClient() {
            /* class ohos.media.photokit.adapter.AVLoggerConnectionAdapter.AnonymousClass1 */

            @Override // android.media.MediaScannerConnection.MediaScannerConnectionClient
            public void onMediaScannerConnected() {
                AVLoggerConnectionClient.this.onLoggerConnected();
            }

            @Override // android.media.MediaScannerConnection.OnScanCompletedListener
            public void onScanCompleted(String str, android.net.Uri uri) {
                AVLoggerConnectionClient.this.onLogCompleted(str, AVLoggerConnectionAdapter.uriDBShell(Uri.parse(uri.toString())));
            }
        };
    }

    private static android.content.Context initContext(Context context) {
        if (context.getHostContext() instanceof android.content.Context) {
            return (android.content.Context) context.getHostContext();
        }
        return null;
    }

    public void performLoggerFile(String str, String str2) {
        super.scanFile(str, str2);
    }

    public static void performLoggerFile(Context context, String[] strArr, String[] strArr2, AVLogCompletedListener aVLogCompletedListener) {
        AVClientProxy aVClientProxy = new AVClientProxy(strArr, strArr2, aVLogCompletedListener);
        AVLoggerConnectionAdapter aVLoggerConnectionAdapter = new AVLoggerConnectionAdapter(context, aVClientProxy, null);
        aVClientProxy.avConnection = aVLoggerConnectionAdapter;
        aVLoggerConnectionAdapter.connect();
    }

    static class AVClientProxy implements AVLoggerConnectionClient {
        AVLoggerConnectionAdapter avConnection;
        final String[] avTypes;
        final AVLogCompletedListener tClient;
        final String[] tPaths;
        int toLatterPath;

        AVClientProxy(String[] strArr, String[] strArr2, AVLogCompletedListener aVLogCompletedListener) {
            this.tPaths = strArr;
            this.avTypes = strArr2;
            this.tClient = aVLogCompletedListener;
        }

        @Override // ohos.media.photokit.common.AVLoggerConnectionClient
        public void onLoggerConnected() {
            scanPaths();
        }

        @Override // ohos.media.photokit.common.AVLoggerConnectionClient
        public void onLogCompleted(String str, Uri uri) {
            AVLogCompletedListener aVLogCompletedListener = this.tClient;
            if (aVLogCompletedListener != null) {
                aVLogCompletedListener.onLogCompleted(str, uri);
            }
            scanPaths();
        }

        /* access modifiers changed from: package-private */
        public void scanPaths() {
            AVLoggerConnectionAdapter aVLoggerConnectionAdapter;
            String str = null;
            if (this.toLatterPath >= this.tPaths.length && (aVLoggerConnectionAdapter = this.avConnection) != null) {
                aVLoggerConnectionAdapter.disconnect();
                this.avConnection = null;
            } else if (this.avConnection != null) {
                String[] strArr = this.avTypes;
                if (strArr != null) {
                    str = strArr[this.toLatterPath];
                }
                this.avConnection.performLoggerFile(this.tPaths[this.toLatterPath], str);
                this.toLatterPath++;
            }
        }
    }

    @Override // android.media.MediaScannerConnection
    public void connect() {
        super.connect();
    }

    @Override // android.media.MediaScannerConnection
    public void disconnect() {
        super.disconnect();
    }

    @Override // android.media.MediaScannerConnection
    public boolean isConnected() {
        return super.isConnected();
    }

    @Override // android.media.MediaScannerConnection, android.content.ServiceConnection
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        super.onServiceConnected(componentName, iBinder);
        ConnectionCallback connectionCallback = this.aCallback;
        if (connectionCallback != null) {
            connectionCallback.onServiceConnect();
        }
    }

    @Override // android.media.MediaScannerConnection, android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName componentName) {
        super.onServiceDisconnected(componentName);
        ConnectionCallback connectionCallback = this.aCallback;
        if (connectionCallback != null) {
            connectionCallback.onServiceDisconnect();
        }
    }
}
