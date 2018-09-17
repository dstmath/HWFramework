package com.google.android.maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.HashMap;

/* compiled from: MapActivity */
class NetworkConnectivityListener {
    private Context mContext;
    private HashMap<Handler, Integer> mHandlers = new HashMap();
    private boolean mIsFailover;
    private boolean mListening;
    private NetworkInfo mNetworkInfo;
    private NetworkInfo mOtherNetworkInfo;
    private String mReason;
    private ConnectivityBroadcastReceiver mReceiver = new ConnectivityBroadcastReceiver(this, null);
    private State mState = State.UNKNOWN;

    /* compiled from: MapActivity */
    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ ConnectivityBroadcastReceiver(NetworkConnectivityListener this$0, ConnectivityBroadcastReceiver -this1) {
            this();
        }

        private ConnectivityBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") && NetworkConnectivityListener.this.mListening) {
                if (intent.getBooleanExtra("noConnectivity", false)) {
                    NetworkConnectivityListener.this.mState = State.NOT_CONNECTED;
                } else {
                    NetworkConnectivityListener.this.mState = State.CONNECTED;
                }
                NetworkConnectivityListener.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                NetworkConnectivityListener.this.mOtherNetworkInfo = (NetworkInfo) intent.getParcelableExtra("otherNetwork");
                NetworkConnectivityListener.this.mReason = intent.getStringExtra("reason");
                NetworkConnectivityListener.this.mIsFailover = intent.getBooleanExtra("isFailover", false);
                for (Handler target : NetworkConnectivityListener.this.mHandlers.keySet()) {
                    target.sendMessage(Message.obtain(target, ((Integer) NetworkConnectivityListener.this.mHandlers.get(target)).intValue()));
                }
                return;
            }
            Log.w("NetworkConnectivityListener", "onReceived() called with " + NetworkConnectivityListener.this.mState.toString() + " and " + intent);
        }
    }

    /* compiled from: MapActivity */
    public enum State {
        UNKNOWN,
        CONNECTED,
        NOT_CONNECTED
    }

    public synchronized void startListening(Context context) {
        if (!this.mListening) {
            this.mContext = context;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            context.registerReceiver(this.mReceiver, filter);
            this.mListening = true;
        }
    }

    public synchronized void stopListening() {
        if (this.mListening) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mContext = null;
            this.mNetworkInfo = null;
            this.mOtherNetworkInfo = null;
            this.mIsFailover = false;
            this.mReason = null;
            this.mListening = false;
        }
    }

    public void registerHandler(Handler target, int what) {
        this.mHandlers.put(target, Integer.valueOf(what));
    }

    public void unregisterHandler(Handler target) {
        this.mHandlers.remove(target);
    }

    public State getState() {
        return this.mState;
    }
}
