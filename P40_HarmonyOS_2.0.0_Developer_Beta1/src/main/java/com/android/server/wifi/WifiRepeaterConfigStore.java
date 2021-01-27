package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.wifi.HwHiLog;
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
    private State mActiveState = new ActiveState();
    private State mDefaultState = new DefaultState();
    private State mInactiveState = new InactiveState();
    private AsyncChannel mReplyChannel = new AsyncChannel();
    private WifiConfiguration mWifiRepeaterConfig = null;

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

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifiP2pService.CMD_SET_REPEATER_CONFIG /* 143461 */:
                case HwWifiP2pService.CMD_SET_REPEATER_CONFIG_COMPLETED /* 143462 */:
                    HwHiLog.e(WifiRepeaterConfigStore.TAG, false, "%{public}s", new Object[]{"Unexpected message: " + message});
                    break;
                case HwWifiP2pService.CMD_REQUEST_REPEATER_CONFIG /* 143463 */:
                    WifiRepeaterConfigStore.this.mReplyChannel.replyToMessage(message, (int) HwWifiP2pService.CMD_RESPONSE_REPEATER_CONFIG, WifiRepeaterConfigStore.this.mWifiRepeaterConfig);
                    break;
                default:
                    HwHiLog.e(WifiRepeaterConfigStore.TAG, false, "%{public}s", new Object[]{"Failed to handle " + message});
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
                WifiRepeaterConfigStore.this.mWifiRepeaterConfig = config;
                WifiRepeaterConfigStore wifiRepeaterConfigStore = WifiRepeaterConfigStore.this;
                wifiRepeaterConfigStore.transitionTo(wifiRepeaterConfigStore.mActiveState);
            } else {
                HwHiLog.e(WifiRepeaterConfigStore.TAG, false, "%{public}s", new Object[]{"Try to setup AP config without SSID: " + message});
            }
            return true;
        }
    }

    class ActiveState extends State {
        ActiveState() {
        }

        public void enter() {
            new Thread(new Runnable() {
                /* class com.android.server.wifi.WifiRepeaterConfigStore.ActiveState.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    WifiRepeaterConfigStore.this.writeApConfiguration(WifiRepeaterConfigStore.this.mWifiRepeaterConfig);
                    WifiRepeaterConfigStore.this.sendMessage(HwWifiP2pService.CMD_SET_REPEATER_CONFIG_COMPLETED);
                }
            }).start();
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifiP2pService.CMD_SET_REPEATER_CONFIG /* 143461 */:
                    WifiRepeaterConfigStore.this.deferMessage(message);
                    return true;
                case HwWifiP2pService.CMD_SET_REPEATER_CONFIG_COMPLETED /* 143462 */:
                    WifiRepeaterConfigStore wifiRepeaterConfigStore = WifiRepeaterConfigStore.this;
                    wifiRepeaterConfigStore.transitionTo(wifiRepeaterConfigStore.mInactiveState);
                    return true;
                default:
                    return false;
            }
        }
    }

    public void loadRepeaterConfiguration() {
        DataInputStream in = null;
        try {
            WifiConfiguration config = new WifiConfiguration();
            DataInputStream in2 = new DataInputStream(new BufferedInputStream(new FileInputStream(REPEATER_CONFIG_FILE)));
            int version = in2.readInt();
            if (version == 1 || version == 2) {
                config.SSID = in2.readUTF();
                if (version >= 2) {
                    config.apBand = in2.readInt();
                    config.apChannel = in2.readInt();
                }
                int authType = in2.readInt();
                config.allowedKeyManagement.set(authType);
                if (authType != 0) {
                    config.preSharedKey = in2.readUTF();
                }
                this.mWifiRepeaterConfig = config;
                try {
                    in2.close();
                } catch (IOException e) {
                }
            } else {
                HwHiLog.e(TAG, false, "Bad version on repeater configuration file, set defaults", new Object[0]);
                setDefaultApConfiguration();
                try {
                    in2.close();
                } catch (IOException e2) {
                }
            }
        } catch (IOException e3) {
            setDefaultApConfiguration();
            if (0 != 0) {
                in.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
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
    /* access modifiers changed from: public */
    private void writeApConfiguration(WifiConfiguration config) {
        DataOutputStream out = null;
        try {
            DataOutputStream out2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(REPEATER_CONFIG_FILE)));
            out2.writeInt(2);
            out2.writeUTF(config.SSID);
            out2.writeInt(config.apBand);
            out2.writeInt(config.apChannel);
            int authType = config.getAuthType();
            out2.writeInt(authType);
            if (!(authType == 0 || config.preSharedKey == null)) {
                out2.writeUTF(config.preSharedKey);
            }
            try {
                out2.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            HwHiLog.e(TAG, false, "Error writing hotspot configuration %{public}s", new Object[]{e2.getMessage()});
            if (0 != 0) {
                out.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e3) {
                }
            }
            throw th;
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
