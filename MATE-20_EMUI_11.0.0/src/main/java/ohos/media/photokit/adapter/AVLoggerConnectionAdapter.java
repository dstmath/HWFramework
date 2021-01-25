package ohos.media.photokit.adapter;

import android.media.MediaScannerConnection;
import ohos.app.Context;
import ohos.media.photokit.common.AVLogCompletedListener;
import ohos.media.photokit.common.AVLoggerConnectionClient;
import ohos.utils.net.Uri;

public class AVLoggerConnectionAdapter {
    private MediaScannerConnection.MediaScannerConnectionClient aClient;
    private MediaScannerConnection aScanConn;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Uri uriDBShell(Uri uri) {
        return Uri.parse(uri.toString().replace("content://", "dataability:///"));
    }

    public AVLoggerConnectionAdapter(Context context, final AVLoggerConnectionClient aVLoggerConnectionClient) {
        this.aClient = new MediaScannerConnection.MediaScannerConnectionClient() {
            /* class ohos.media.photokit.adapter.AVLoggerConnectionAdapter.AnonymousClass1 */

            @Override // android.media.MediaScannerConnection.MediaScannerConnectionClient
            public void onMediaScannerConnected() {
                aVLoggerConnectionClient.onLoggerConnected();
            }

            @Override // android.media.MediaScannerConnection.OnScanCompletedListener
            public void onScanCompleted(String str, android.net.Uri uri) {
                aVLoggerConnectionClient.onLogCompleted(str, AVLoggerConnectionAdapter.this.uriDBShell(Uri.parse(uri.toString())));
            }
        };
        if (context.getHostContext() instanceof android.content.Context) {
            this.aScanConn = new MediaScannerConnection((android.content.Context) context.getHostContext(), this.aClient);
        }
    }

    public void performLoggerFile(String str, String str2) {
        this.aScanConn.scanFile(str, str2);
    }

    public static void performLoggerFile(Context context, String[] strArr, String[] strArr2, AVLogCompletedListener aVLogCompletedListener) {
        AVClientProxy aVClientProxy = new AVClientProxy(strArr, strArr2, aVLogCompletedListener);
        AVLoggerConnectionAdapter aVLoggerConnectionAdapter = new AVLoggerConnectionAdapter(context, aVClientProxy);
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

    public void connect() {
        this.aScanConn.connect();
    }

    public void disconnect() {
        this.aScanConn.disconnect();
    }

    public boolean isConnected() {
        return this.aScanConn.isConnected();
    }
}
