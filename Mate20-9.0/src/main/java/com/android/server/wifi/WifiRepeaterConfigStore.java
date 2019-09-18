package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.p2p.HwWifiP2pService;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class WifiRepeaterConfigStore extends StateMachine {
    private static final String REPEATER_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/wifirepeater.conf");
    private static final int REPEATER_CONFIG_FILE_VERSION = 2;
    private static final String TAG = "WifiRepeaterConfigStore";
    /* access modifiers changed from: private */
    public State mActiveState = new ActiveState();
    private State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public State mInactiveState = new InactiveState();
    /* access modifiers changed from: private */
    public AsyncChannel mReplyChannel = new AsyncChannel();
    /* access modifiers changed from: private */
    public WifiConfiguration mWifiRepeaterConfig = null;

    class ActiveState extends State {
        ActiveState() {
        }

        public void enter() {
            new Thread(new Runnable() {
                public void run() {
                    WifiRepeaterConfigStore.this.writeApConfiguration(WifiRepeaterConfigStore.this.mWifiRepeaterConfig);
                    WifiRepeaterConfigStore.this.sendMessage(HwWifiP2pService.CMD_SET_REPEATER_CONFIG_COMPLETED);
                }
            }).start();
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifiP2pService.CMD_SET_REPEATER_CONFIG /*143461*/:
                    WifiRepeaterConfigStore.this.deferMessage(message);
                    break;
                case HwWifiP2pService.CMD_SET_REPEATER_CONFIG_COMPLETED /*143462*/:
                    WifiRepeaterConfigStore.this.transitionTo(WifiRepeaterConfigStore.this.mInactiveState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifiP2pService.CMD_SET_REPEATER_CONFIG /*143461*/:
                case HwWifiP2pService.CMD_SET_REPEATER_CONFIG_COMPLETED /*143462*/:
                    Log.e(WifiRepeaterConfigStore.TAG, "Unexpected message: " + message);
                    break;
                case HwWifiP2pService.CMD_REQUEST_REPEATER_CONFIG /*143463*/:
                    WifiRepeaterConfigStore.this.mReplyChannel.replyToMessage(message, HwWifiP2pService.CMD_RESPONSE_REPEATER_CONFIG, WifiRepeaterConfigStore.this.mWifiRepeaterConfig);
                    break;
                default:
                    Log.e(WifiRepeaterConfigStore.TAG, "Failed to handle " + message);
                    break;
            }
            return true;
        }
    }

    class InactiveState extends State {
        InactiveState() {
        }

        public boolean processMessage(Message message) {
            if (message.what != 143461) {
                return false;
            }
            WifiConfiguration config = (WifiConfiguration) message.obj;
            if (config.SSID != null) {
                WifiConfiguration unused = WifiRepeaterConfigStore.this.mWifiRepeaterConfig = config;
                WifiRepeaterConfigStore.this.transitionTo(WifiRepeaterConfigStore.this.mActiveState);
            } else {
                Log.e(WifiRepeaterConfigStore.TAG, "Try to setup AP config without SSID: " + message);
            }
            return true;
        }
    }

    WifiRepeaterConfigStore(Handler target) {
        super(TAG, target.getLooper());
        addState(this.mDefaultState);
        addState(this.mInactiveState, this.mDefaultState);
        addState(this.mActiveState, this.mDefaultState);
        setInitialState(this.mInactiveState);
    }

    public static WifiRepeaterConfigStore makeWifiRepeaterConfigStore(Handler target) {
        WifiRepeaterConfigStore s = new WifiRepeaterConfigStore(target);
        s.start();
        return s;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0067, code lost:
        if (r0 == null) goto L_0x006a;
     */
    public void loadRepeaterConfiguration() {
        DataInputStream in = null;
        try {
            WifiConfiguration config = new WifiConfiguration();
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(REPEATER_CONFIG_FILE)));
            int version = in.readInt();
            if (version == 1 || version == 2) {
                config.SSID = in.readUTF();
                if (version >= 2) {
                    config.apBand = in.readInt();
                    config.apChannel = in.readInt();
                }
                int authType = in.readInt();
                config.allowedKeyManagement.set(authType);
                if (authType != 0) {
                    config.preSharedKey = in.readUTF();
                }
                this.mWifiRepeaterConfig = config;
                try {
                    in.close();
                } catch (IOException e) {
                }
                return;
            }
            Log.e(TAG, "Bad version on repeater configuration file, set defaults");
            setDefaultApConfiguration();
            try {
                in.close();
            } catch (IOException e2) {
            }
        } catch (IOException e3) {
            setDefaultApConfiguration();
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    public Messenger getMessenger() {
        return new Messenger(getHandler());
    }

    /* access modifiers changed from: private */
    public void writeApConfiguration(WifiConfiguration config) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(REPEATER_CONFIG_FILE)));
            out.writeInt(2);
            out.writeUTF(config.SSID);
            out.writeInt(config.apBand);
            out.writeInt(config.apChannel);
            int authType = config.getAuthType();
            out.writeInt(authType);
            if (!(authType == 0 || config.preSharedKey == null)) {
                out.writeUTF(config.preSharedKey);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error writing hotspot configuration" + e);
            if (out == null) {
                return;
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
        try {
            out.close();
        } catch (IOException e3) {
        }
    }

    private void setDefaultApConfiguration() {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedKeyManagement.set(4);
        String randomUUID = UUID.randomUUID().toString();
        config.preSharedKey = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
        config.SSID = HwWifiServiceFactory.getHwWifiServiceManager().getCustWifiApDefaultName(config);
        sendMessage(HwWifiP2pService.CMD_SET_REPEATER_CONFIG, config);
    }
}
