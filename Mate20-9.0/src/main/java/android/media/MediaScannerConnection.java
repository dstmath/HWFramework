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
    /* access modifiers changed from: private */
    public MediaScannerConnectionClient mClient;
    private boolean mConnected;
    private Context mContext;
    private final IMediaScannerListener.Stub mListener = new IMediaScannerListener.Stub() {
        public void scanCompleted(String path, Uri uri) {
            MediaScannerConnectionClient client = MediaScannerConnection.this.mClient;
            if (client != null) {
                client.onScanCompleted(path, uri);
            }
        }
    };
    private IMediaScannerService mService;

    static class ClientProxy implements MediaScannerConnectionClient {
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

        public void onMediaScannerConnected() {
            scanNextPath();
        }

        public void onScanCompleted(String path, Uri uri) {
            if (this.mClient != null) {
                this.mClient.onScanCompleted(path, uri);
            }
            scanNextPath();
        }

        /* access modifiers changed from: package-private */
        public void scanNextPath() {
            String mimeType = null;
            if (this.mNextPath >= this.mPaths.length) {
                this.mConnection.disconnect();
                this.mConnection = null;
                return;
            }
            if (this.mMimeTypes != null) {
                mimeType = this.mMimeTypes[this.mNextPath];
            }
            this.mConnection.scanFile(this.mPaths[this.mNextPath], mimeType);
            this.mNextPath++;
        }
    }

    public interface MediaScannerConnectionClient extends OnScanCompletedListener {
        void onMediaScannerConnected();

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
                if (DEBUG) {
                    Log.v(TAG, "Scanning file " + path);
                }
                this.mService.requestScanFile(path, mimeType, this.mListener);
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.d(TAG, "Failed to scan file " + path);
                }
            }
        }
    }

    public static void scanFile(Context context, String[] paths, String[] mimeTypes, OnScanCompletedListener callback) {
        ClientProxy client = new ClientProxy(paths, mimeTypes, callback);
        MediaScannerConnection connection = new MediaScannerConnection(context, client);
        client.mConnection = connection;
        connection.connect();
    }

    public void onServiceConnected(ComponentName className, IBinder service) {
        synchronized (this) {
            this.mService = IMediaScannerService.Stub.asInterface(service);
            if (!(this.mService == null || this.mClient == null)) {
                this.mClient.onMediaScannerConnected();
            }
        }
    }

    public void onServiceDisconnected(ComponentName className) {
        synchronized (this) {
            this.mService = null;
        }
    }
}
