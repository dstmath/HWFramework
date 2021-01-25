package com.android.server;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.UEventObserver;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import com.android.server.ExtconUEventObserver;
import com.android.server.input.InputManagerService;
import com.android.server.pm.DumpState;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* access modifiers changed from: package-private */
public final class WiredAccessoryManager implements InputManagerService.WiredAccessoryCallbacks {
    private static final int BIT_HDMI_AUDIO = 16;
    private static final int BIT_HEADSET = 1;
    private static final int BIT_HEADSET_NO_MIC = 2;
    private static final int BIT_LINEOUT = 32;
    private static final int BIT_USB_HEADSET_ANLG = 4;
    private static final int BIT_USB_HEADSET_DGTL = 8;
    private static final boolean LOG = false;
    private static final int MSG_NEW_DEVICE_STATE = 1;
    private static final int MSG_SYSTEM_READY = 2;
    private static final String NAME_H2W = "h2w";
    private static final String NAME_HDMI = "hdmi";
    private static final String NAME_HDMI_AUDIO = "hdmi_audio";
    private static final String NAME_USB_AUDIO = "usb_audio";
    private static final int SUPPORTED_HEADSETS = 63;
    private static final String TAG = WiredAccessoryManager.class.getSimpleName();
    private final AudioManager mAudioManager;
    private final WiredAccessoryExtconObserver mExtconObserver;
    private final Handler mHandler = new Handler(Looper.myLooper(), null, true) {
        /* class com.android.server.WiredAccessoryManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                WiredAccessoryManager.this.setDevicesState(msg.arg1, msg.arg2, (String) msg.obj);
                WiredAccessoryManager.this.mWakeLock.release();
            } else if (i == 2) {
                WiredAccessoryManager.this.onSystemReady();
                WiredAccessoryManager.this.mWakeLock.release();
            }
        }
    };
    private int mHeadsetState;
    private final InputManagerService mInputManager;
    private final Object mLock = new Object();
    private final WiredAccessoryObserver mObserver;
    private int mSwitchValues;
    private final boolean mUseDevInputEventForAudioJack;
    private final PowerManager.WakeLock mWakeLock;

    public WiredAccessoryManager(Context context, InputManagerService inputManager) {
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "WiredAccessoryManager");
        this.mWakeLock.setReferenceCounted(false);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mInputManager = inputManager;
        this.mUseDevInputEventForAudioJack = context.getResources().getBoolean(17891560);
        this.mExtconObserver = new WiredAccessoryExtconObserver();
        this.mObserver = new WiredAccessoryObserver();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSystemReady() {
        if (this.mUseDevInputEventForAudioJack) {
            int switchValues = 0;
            if (this.mInputManager.getSwitchState(-1, -256, 2) == 1) {
                switchValues = 0 | 4;
            }
            if (this.mInputManager.getSwitchState(-1, -256, 4) == 1) {
                switchValues |= 16;
            }
            if (this.mInputManager.getSwitchState(-1, -256, 6) == 1) {
                switchValues |= 64;
            }
            notifyWiredAccessoryChanged(0, switchValues, 84);
        }
        if (ExtconUEventObserver.extconExists()) {
            if (this.mUseDevInputEventForAudioJack) {
                Log.w(TAG, "Both input event and extcon are used for audio jack, please just choose one.");
            }
            this.mExtconObserver.init();
            return;
        }
        this.mObserver.init();
    }

    @Override // com.android.server.input.InputManagerService.WiredAccessoryCallbacks
    public void notifyWiredAccessoryChanged(long whenNanos, int switchValues, int switchMask) {
        int headset;
        synchronized (this.mLock) {
            this.mSwitchValues = (this.mSwitchValues & (~switchMask)) | switchValues;
            int i = this.mSwitchValues & 84;
            if (i == 0) {
                headset = 0;
            } else if (i == 4) {
                headset = 2;
            } else if (i == 16) {
                headset = 1;
            } else if (i == 20) {
                headset = 1;
            } else if (i != 64) {
                headset = 0;
            } else {
                headset = 32;
            }
            updateLocked(NAME_H2W, (this.mHeadsetState & -36) | headset);
        }
    }

    @Override // com.android.server.input.InputManagerService.WiredAccessoryCallbacks
    public void systemReady() {
        synchronized (this.mLock) {
            this.mWakeLock.acquire();
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, 0, 0, null));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLocked(String newName, int newState) {
        int headsetState = newState & SUPPORTED_HEADSETS;
        int usb_headset_anlg = headsetState & 4;
        int usb_headset_dgtl = headsetState & 8;
        int h2w_headset = headsetState & 35;
        boolean h2wStateChange = true;
        boolean usbStateChange = true;
        if (this.mHeadsetState == headsetState) {
            Log.e(TAG, "No state change.");
            return;
        }
        if (h2w_headset == 35) {
            Log.e(TAG, "Invalid combination, unsetting h2w flag");
            h2wStateChange = false;
        }
        if (usb_headset_anlg == 4 && usb_headset_dgtl == 8) {
            Log.e(TAG, "Invalid combination, unsetting usb flag");
            usbStateChange = false;
        }
        if (h2wStateChange || usbStateChange) {
            this.mWakeLock.acquire();
            Log.i(TAG, "MSG_NEW_DEVICE_STATE");
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, headsetState, this.mHeadsetState, ""));
            this.mHeadsetState = headsetState;
            return;
        }
        Log.e(TAG, "invalid transition, returning ...");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDevicesState(int headsetState, int prevHeadsetState, String headsetName) {
        synchronized (this.mLock) {
            int allHeadsets = SUPPORTED_HEADSETS;
            int curHeadset = 1;
            while (allHeadsets != 0) {
                if ((curHeadset & allHeadsets) != 0) {
                    setDeviceStateLocked(curHeadset, headsetState, prevHeadsetState, headsetName);
                    allHeadsets &= ~curHeadset;
                }
                curHeadset <<= 1;
            }
        }
    }

    private void setDeviceStateLocked(int headset, int headsetState, int prevHeadsetState, String headsetName) {
        int state;
        int outDevice;
        if ((headsetState & headset) != (prevHeadsetState & headset)) {
            int inDevice = 0;
            if ((headsetState & headset) != 0) {
                state = 1;
            } else {
                state = 0;
            }
            if (1 != headsetState || 2 != headset) {
                if (2 != headsetState || 1 != headset) {
                    if (headset == 1) {
                        outDevice = 4;
                        inDevice = -2147483632;
                    } else if (headset == 2) {
                        outDevice = 8;
                    } else if (headset == 32) {
                        outDevice = DumpState.DUMP_INTENT_FILTER_VERIFIERS;
                    } else if (headset == 4) {
                        outDevice = 2048;
                    } else if (headset == 8) {
                        outDevice = 4096;
                    } else if (headset == 16) {
                        outDevice = 1024;
                    } else {
                        String str = TAG;
                        Slog.e(str, "setDeviceState() invalid headset type: " + headset);
                        return;
                    }
                    if (inDevice != 0) {
                        this.mAudioManager.setWiredDeviceConnectionState(inDevice, state, "", headsetName);
                    }
                    if (outDevice != 0) {
                        this.mAudioManager.setWiredDeviceConnectionState(outDevice, state, "", headsetName);
                    }
                }
            }
        }
    }

    private String switchCodeToString(int switchValues, int switchMask) {
        StringBuffer sb = new StringBuffer();
        if (!((switchMask & 4) == 0 || (switchValues & 4) == 0)) {
            sb.append("SW_HEADPHONE_INSERT ");
        }
        if (!((switchMask & 16) == 0 || (switchValues & 16) == 0)) {
            sb.append("SW_MICROPHONE_INSERT");
        }
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public class WiredAccessoryObserver extends UEventObserver {
        private final List<UEventInfo> mUEventInfo = makeObservedUEventList();

        public WiredAccessoryObserver() {
        }

        /* access modifiers changed from: package-private */
        public void init() {
            synchronized (WiredAccessoryManager.this.mLock) {
                char[] buffer = new char[1024];
                for (int i = 0; i < this.mUEventInfo.size(); i++) {
                    UEventInfo uei = this.mUEventInfo.get(i);
                    try {
                        FileReader file = new FileReader(uei.getSwitchStatePath());
                        int len = file.read(buffer, 0, 1024);
                        file.close();
                        int curState = Integer.parseInt(new String(buffer, 0, len).trim());
                        if (curState > 0) {
                            updateStateLocked(uei.getDevPath(), uei.getDevName(), curState);
                        }
                    } catch (FileNotFoundException e) {
                        Slog.w(WiredAccessoryManager.TAG, uei.getSwitchStatePath() + " not found while attempting to determine initial switch state");
                    } catch (Exception e2) {
                        Slog.e(WiredAccessoryManager.TAG, "Error while attempting to determine initial switch state for " + uei.getDevName(), e2);
                    }
                }
            }
            for (int i2 = 0; i2 < this.mUEventInfo.size(); i2++) {
                startObserving("DEVPATH=" + this.mUEventInfo.get(i2).getDevPath());
            }
        }

        private List<UEventInfo> makeObservedUEventList() {
            List<UEventInfo> retVal = new ArrayList<>();
            if (!WiredAccessoryManager.this.mUseDevInputEventForAudioJack) {
                UEventInfo uei = new UEventInfo(WiredAccessoryManager.NAME_H2W, 1, 2, 32);
                if (uei.checkSwitchExists()) {
                    retVal.add(uei);
                } else {
                    Slog.w(WiredAccessoryManager.TAG, "This kernel does not have wired headset support");
                }
            }
            UEventInfo uei2 = new UEventInfo(WiredAccessoryManager.NAME_USB_AUDIO, 4, 8, 0);
            if (uei2.checkSwitchExists()) {
                retVal.add(uei2);
            } else {
                Slog.w(WiredAccessoryManager.TAG, "This kernel does not have usb audio support");
            }
            UEventInfo uei3 = new UEventInfo(WiredAccessoryManager.NAME_HDMI_AUDIO, 16, 0, 0);
            if (uei3.checkSwitchExists()) {
                retVal.add(uei3);
            } else {
                UEventInfo uei4 = new UEventInfo(WiredAccessoryManager.NAME_HDMI, 16, 0, 0);
                if (uei4.checkSwitchExists()) {
                    retVal.add(uei4);
                } else {
                    Slog.w(WiredAccessoryManager.TAG, "This kernel does not have HDMI audio support");
                }
            }
            return retVal;
        }

        public void onUEvent(UEventObserver.UEvent event) {
            try {
                String devPath = event.get("DEVPATH");
                String name = event.get("SWITCH_NAME");
                int state = Integer.parseInt(event.get("SWITCH_STATE"));
                synchronized (WiredAccessoryManager.this.mLock) {
                    updateStateLocked(devPath, name, state);
                }
            } catch (NumberFormatException e) {
                String str = WiredAccessoryManager.TAG;
                Slog.e(str, "Could not parse switch state from event " + event);
            }
        }

        private void updateStateLocked(String devPath, String name, int state) {
            for (int i = 0; i < this.mUEventInfo.size(); i++) {
                UEventInfo uei = this.mUEventInfo.get(i);
                if (devPath.equals(uei.getDevPath())) {
                    WiredAccessoryManager wiredAccessoryManager = WiredAccessoryManager.this;
                    wiredAccessoryManager.updateLocked(name, uei.computeNewHeadsetState(wiredAccessoryManager.mHeadsetState, state));
                    return;
                }
            }
        }

        /* access modifiers changed from: private */
        public final class UEventInfo {
            private final String mDevName;
            private final int mState1Bits;
            private final int mState2Bits;
            private final int mStateNbits;

            public UEventInfo(String devName, int state1Bits, int state2Bits, int stateNbits) {
                this.mDevName = devName;
                this.mState1Bits = state1Bits;
                this.mState2Bits = state2Bits;
                this.mStateNbits = stateNbits;
            }

            public String getDevName() {
                return this.mDevName;
            }

            public String getDevPath() {
                return String.format(Locale.US, "/devices/virtual/switch/%s", this.mDevName);
            }

            public String getSwitchStatePath() {
                return String.format(Locale.US, "/sys/class/switch/%s/state", this.mDevName);
            }

            public boolean checkSwitchExists() {
                return new File(getSwitchStatePath()).exists();
            }

            public int computeNewHeadsetState(int headsetState, int switchState) {
                int setBits = this.mState1Bits;
                int preserveMask = ~(this.mState2Bits | setBits | this.mStateNbits);
                boolean z = true;
                if (switchState != 1) {
                    boolean z2 = switchState == 2;
                    if (switchState != 3) {
                        z = false;
                    }
                    if (z2 || z) {
                        setBits = this.mState2Bits;
                    } else {
                        setBits = this.mStateNbits;
                        if (switchState != setBits) {
                            setBits = 0;
                        }
                    }
                }
                return (headsetState & preserveMask) | setBits;
            }
        }
    }

    /* access modifiers changed from: private */
    public class WiredAccessoryExtconObserver extends ExtconStateObserver<Pair<Integer, Integer>> {
        private final List<ExtconUEventObserver.ExtconInfo> mExtconInfos = ExtconUEventObserver.ExtconInfo.getExtconInfos(".*audio.*");

        WiredAccessoryExtconObserver() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void init() {
            for (ExtconUEventObserver.ExtconInfo extconInfo : this.mExtconInfos) {
                Pair<Integer, Integer> state = null;
                try {
                    state = (Pair) parseStateFromFile(extconInfo);
                } catch (FileNotFoundException e) {
                    String str = WiredAccessoryManager.TAG;
                    Slog.w(str, extconInfo.getStatePath() + " not found while attempting to determine initial state", e);
                } catch (IOException e2) {
                    String str2 = WiredAccessoryManager.TAG;
                    Slog.e(str2, "Error reading " + extconInfo.getStatePath() + " while attempting to determine initial state", e2);
                }
                if (state != null) {
                    updateState(extconInfo, extconInfo.getName(), state);
                }
                startObserving(extconInfo);
            }
        }

        @Override // com.android.server.ExtconStateObserver
        public Pair<Integer, Integer> parseState(ExtconUEventObserver.ExtconInfo extconInfo, String status) {
            int[] maskAndState = {0, 0};
            WiredAccessoryManager.updateBit(maskAndState, 2, status, "HEADPHONE");
            WiredAccessoryManager.updateBit(maskAndState, 1, status, "MICROPHONE");
            WiredAccessoryManager.updateBit(maskAndState, 16, status, "HDMI");
            WiredAccessoryManager.updateBit(maskAndState, 32, status, "LINE-OUT");
            return Pair.create(Integer.valueOf(maskAndState[0]), Integer.valueOf(maskAndState[1]));
        }

        public void updateState(ExtconUEventObserver.ExtconInfo extconInfo, String name, Pair<Integer, Integer> maskAndState) {
            synchronized (WiredAccessoryManager.this.mLock) {
                int mask = ((Integer) maskAndState.first).intValue();
                int state = ((Integer) maskAndState.second).intValue();
                WiredAccessoryManager.this.updateLocked(name, WiredAccessoryManager.this.mHeadsetState | (mask & state & (~((~state) & mask))));
            }
        }
    }

    /* access modifiers changed from: private */
    public static void updateBit(int[] maskAndState, int position, String state, String name) {
        maskAndState[0] = maskAndState[0] | position;
        if (state.contains(name + "=1")) {
            maskAndState[0] = maskAndState[0] | position;
            maskAndState[1] = maskAndState[1] | position;
            return;
        }
        if (state.contains(name + "=0")) {
            maskAndState[0] = maskAndState[0] | position;
            maskAndState[1] = maskAndState[1] & (~position);
        }
    }
}
