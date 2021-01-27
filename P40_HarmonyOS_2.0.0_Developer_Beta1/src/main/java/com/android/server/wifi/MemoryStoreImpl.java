package com.android.server.wifi;

import android.content.Context;
import android.net.IpMemoryStore;
import android.net.ipmemorystore.Blob;
import android.net.ipmemorystore.OnBlobRetrievedListener;
import android.net.ipmemorystore.OnStatusListener;
import android.net.ipmemorystore.Status;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.android.server.wifi.WifiScoreCard;
import java.util.Objects;

final class MemoryStoreImpl implements WifiScoreCard.MemoryStore {
    private static final boolean DBG = true;
    private static final String TAG = "WifiMemoryStoreImpl";
    public static final String WIFI_FRAMEWORK_IP_MEMORY_STORE_CLIENT_ID = "com.android.server.wifi";
    public static final String WIFI_FRAMEWORK_IP_MEMORY_STORE_DATA_NAME = "scorecard.proto";
    private boolean mBroken = false;
    private final Context mContext;
    private IpMemoryStore mIpMemoryStore;
    private final WifiInjector mWifiInjector;
    private final WifiScoreCard mWifiScoreCard;

    MemoryStoreImpl(Context context, WifiInjector wifiInjector, WifiScoreCard wifiScoreCard) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mWifiScoreCard = (WifiScoreCard) Preconditions.checkNotNull(wifiScoreCard);
        this.mWifiInjector = (WifiInjector) Preconditions.checkNotNull(wifiInjector);
        this.mIpMemoryStore = null;
    }

    private void handleException(Exception e) {
        Log.wtf(TAG, "Exception using IpMemoryStore - disabling WifiScoreReport persistence", e);
        this.mBroken = true;
    }

    @Override // com.android.server.wifi.WifiScoreCard.MemoryStore
    public void read(String key, WifiScoreCard.BlobListener blobListener) {
        if (!this.mBroken) {
            try {
                this.mIpMemoryStore.retrieveBlob(key, WIFI_FRAMEWORK_IP_MEMORY_STORE_CLIENT_ID, WIFI_FRAMEWORK_IP_MEMORY_STORE_DATA_NAME, new CatchAFallingBlob(key, blobListener));
            } catch (RuntimeException e) {
                handleException(e);
            }
        }
    }

    private static class CatchAFallingBlob implements OnBlobRetrievedListener {
        private final WifiScoreCard.BlobListener mBlobListener;
        private final String mL2Key;

        CatchAFallingBlob(String l2Key, WifiScoreCard.BlobListener blobListener) {
            this.mL2Key = l2Key;
            this.mBlobListener = blobListener;
        }

        public void onBlobRetrieved(Status status, String l2Key, String name, Blob data) {
            if (!Objects.equals(this.mL2Key, l2Key)) {
                throw new IllegalArgumentException("l2Key does not match request");
            } else if (!status.isSuccess()) {
                Log.e(MemoryStoreImpl.TAG, "android.net.ipmemorystore.Status " + status);
            } else if (data == null) {
                Log.i(MemoryStoreImpl.TAG, "Blob is null");
                this.mBlobListener.onBlobRetrieved(null);
            } else {
                this.mBlobListener.onBlobRetrieved(data.data);
            }
        }
    }

    @Override // com.android.server.wifi.WifiScoreCard.MemoryStore
    public void write(String key, byte[] value) {
        if (!this.mBroken) {
            Blob blob = new Blob();
            blob.data = value;
            try {
                this.mIpMemoryStore.storeBlob(key, WIFI_FRAMEWORK_IP_MEMORY_STORE_CLIENT_ID, WIFI_FRAMEWORK_IP_MEMORY_STORE_DATA_NAME, blob, (OnStatusListener) null);
            } catch (RuntimeException e) {
                handleException(e);
            }
        }
    }

    public void start() {
        if (this.mIpMemoryStore != null) {
            Log.w(TAG, "Reconnecting to IpMemoryStore service");
        }
        this.mIpMemoryStore = this.mWifiInjector.getIpMemoryStore();
        if (this.mIpMemoryStore == null) {
            Log.e(TAG, "No IpMemoryStore service!");
        } else {
            this.mWifiScoreCard.installMemoryStore(this);
        }
    }

    public void stop() {
        if (this.mIpMemoryStore != null) {
            this.mWifiScoreCard.doWrites();
            Log.i(TAG, "Disconnecting from IpMemoryStore service");
            this.mIpMemoryStore = null;
        }
    }
}
