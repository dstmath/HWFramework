package android.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.IMediaScannerListener;
import android.media.IMediaScannerService;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class MediaScannerConnection implements ServiceConnection {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "MediaScannerConnection";
    private MediaScannerConnectionClient mClient;
    private boolean mConnected;
    private Context mContext;
    private final IMediaScannerListener.Stub mListener = new IMediaScannerListener.Stub() {
        /* class android.media.MediaScannerConnection.AnonymousClass1 */

        @Override // android.media.IMediaScannerListener
        public void scanCompleted(String path, Uri uri) {
            MediaScannerConnectionClient client = MediaScannerConnection.this.mClient;
            if (client != null) {
                client.onScanCompleted(path, uri);
            }
        }
    };
    private IMediaScannerService mService;

    public interface MediaScannerConnectionClient extends OnScanCompletedListener {
        void onMediaScannerConnected();

        @Override // android.media.MediaScannerConnection.OnScanCompletedListener
        void onScanCompleted(String str, Uri uri);
    }

    public interface OnScanCompletedListener {
        void onScanCompleted(String str, Uri uri);
    }

    public MediaScannerConnection(Context context, MediaScannerConnectionClient client) {
        this.mContext = context;
        this.mClient = client;
    }

    public void connect() {
        synchronized (this) {
            if (!this.mConnected) {
                Intent intent = new Intent(IMediaScannerService.class.getName());
                intent.setComponent(new ComponentName("com.android.providers.media", "com.android.providers.media.MediaScannerService"));
                this.mContext.bindService(intent, this, 1);
                this.mConnected = true;
            }
        }
    }

    public void disconnect() {
        synchronized (this) {
            if (this.mConnected) {
                try {
                    this.mContext.unbindService(this);
                    if (this.mClient instanceof ClientProxy) {
                        this.mClient = null;
                    }
                    this.mService = null;
                } catch (IllegalArgumentException e) {
                }
                this.mConnected = false;
            }
        }
    }

    public synchronized boolean isConnected() {
        return this.mService != null && this.mConnected;
    }

    public void scanFile(String path, String mimeType) {
        synchronized (this) {
            if (this.mService == null || !this.mConnected) {
                throw new IllegalStateException("not connected to MediaScannerService");
            }
            try {
                Log.d(TAG, "Scanning file");
                this.mService.requestScanFile(path, mimeType, this.mListener);
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.d(TAG, "Failed to scan file " + path);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class ClientProxy implements MediaScannerConnectionClient {
        final OnScanCompletedListener mClient;
        MediaScannerConnection mConnection;
        final String[] mMimeTypes;
        int mNextPath;
        final String[] mPaths;

        ClientProxy(String[] paths, String[] mimeTypes, OnScanCompletedListener client) {
            this.mPaths = paths;
            this.mMimeTypes = mimeTypes;
            this.mClient = client;
        }

        @Override // android.media.MediaScannerConnection.MediaScannerConnectionClient
        public void onMediaScannerConnected() {
            scanNextPath();
        }

        @Override // android.media.MediaScannerConnection.MediaScannerConnectionClient, android.media.MediaScannerConnection.OnScanCompletedListener
        public void onScanCompleted(String path, Uri uri) {
            OnScanCompletedListener onScanCompletedListener = this.mClient;
            if (onScanCompletedListener != null) {
                onScanCompletedListener.onScanCompleted(path, uri);
            }
            scanNextPath();
        }

        /* access modifiers changed from: package-private */
        public void scanNextPath() {
            int i = this.mNextPath;
            String mimeType = null;
            if (i >= this.mPaths.length) {
                this.mConnection.disconnect();
                this.mConnection = null;
                return;
            }
            String[] strArr = this.mMimeTypes;
            if (strArr != null) {
                mimeType = strArr[i];
            }
            this.mConnection.scanFile(this.mPaths[this.mNextPath], mimeType);
            this.mNextPath++;
        }
    }

    public static void scanFile(Context context, String[] paths, String[] mimeTypes, OnScanCompletedListener callback) {
        Log.d(TAG, "scanFile");
        ClientProxy client = new ClientProxy(paths, mimeTypes, callback);
        MediaScannerConnection connection = new MediaScannerConnection(context, client);
        client.mConnection = connection;
        connection.connect();
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName className, IBinder service) {
        Log.v(TAG, "Connected to Media Scanner");
        synchronized (this) {
            this.mService = IMediaScannerService.Stub.asInterface(service);
            if (!(this.mService == null || this.mClient == null)) {
                this.mClient.onMediaScannerConnected();
            }
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName className) {
        Log.v(TAG, "Disconnected from Media Scanner");
        synchronized (this) {
            this.mService = null;
        }
    }
}
