package com.android.server;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.util.Log;
import android.util.Slog;
import com.android.server.input.InputManagerService;
import com.android.server.input.InputManagerService.WiredAccessoryCallbacks;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class WiredAccessoryManager implements WiredAccessoryCallbacks {
    private static final int BIT_HDMI_AUDIO = 16;
    private static final int BIT_HEADSET = 1;
    private static final int BIT_HEADSET_NO_MIC = 2;
    private static final int BIT_LINEOUT = 32;
    private static final int BIT_USB_HEADSET_ANLG = 4;
    private static final int BIT_USB_HEADSET_DGTL = 8;
    private static final int HEADSET_REVERT_SEQUENCE = 3;
    private static final boolean LOG = true;
    private static final int MSG_NEW_DEVICE_STATE = 1;
    private static final int MSG_SYSTEM_READY = 2;
    private static final String NAME_H2W = "h2w";
    private static final String NAME_HDMI = "hdmi";
    private static final String NAME_HDMI_AUDIO = "hdmi_audio";
    private static final String NAME_USB_AUDIO = "usb_audio";
    private static final int SUPPORTED_HEADSETS = 63;
    private static final String TAG = null;
    private final AudioManager mAudioManager;
    private final Handler mHandler;
    private int mHeadsetState;
    private final InputManagerService mInputManager;
    private final Object mLock;
    private final WiredAccessoryObserver mObserver;
    private int mSwitchValues;
    private final boolean mUseDevInputEventForAudioJack;
    private final WakeLock mWakeLock;

    /* renamed from: com.android.server.WiredAccessoryManager.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0, Callback $anonymous1, boolean $anonymous2) {
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WiredAccessoryManager.MSG_NEW_DEVICE_STATE /*1*/:
                    WiredAccessoryManager.this.setDevicesState(msg.arg1, msg.arg2, (String) msg.obj);
                    WiredAccessoryManager.this.mWakeLock.release();
                case WiredAccessoryManager.MSG_SYSTEM_READY /*2*/:
                    WiredAccessoryManager.this.onSystemReady();
                    WiredAccessoryManager.this.mWakeLock.release();
                default:
            }
        }
    }

    class WiredAccessoryObserver extends UEventObserver {
        private final List<UEventInfo> mUEventInfo;

        private final class UEventInfo {
            private final String mDevName;
            private final int mState1Bits;
            private final int mState2Bits;
            private final int mStateNbits;

            public int computeNewHeadsetState(int r1, int r2) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.WiredAccessoryManager.WiredAccessoryObserver.UEventInfo.computeNewHeadsetState(int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.WiredAccessoryManager.WiredAccessoryObserver.UEventInfo.computeNewHeadsetState(int, int):int");
            }

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
                Object[] objArr = new Object[WiredAccessoryManager.MSG_NEW_DEVICE_STATE];
                objArr[0] = this.mDevName;
                return String.format(Locale.US, "/devices/virtual/switch/%s", objArr);
            }

            public String getSwitchStatePath() {
                Object[] objArr = new Object[WiredAccessoryManager.MSG_NEW_DEVICE_STATE];
                objArr[0] = this.mDevName;
                return String.format(Locale.US, "/sys/class/switch/%s/state", objArr);
            }

            public boolean checkSwitchExists() {
                return new File(getSwitchStatePath()).exists();
            }
        }

        public WiredAccessoryObserver() {
            this.mUEventInfo = makeObservedUEventList();
        }

        private IBinder getAudioService() {
            return ServiceManager.getService("audio");
        }

        private void setHeadsetRevertSequenceState(boolean isRevertSeq) {
            Slog.v(WiredAccessoryManager.TAG, "setHeadsetRevertSequenceState isRevertSeq: " + isRevertSeq);
            IBinder b = getAudioService();
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            if (isRevertSeq) {
                data.writeInt(WiredAccessoryManager.MSG_NEW_DEVICE_STATE);
            } else {
                data.writeInt(0);
            }
            try {
                b.transact(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, data, reply, 0);
            } catch (RemoteException e) {
                Slog.e(WiredAccessoryManager.TAG, "setHeadsetRevertSequenceState transact e: " + e);
            }
        }

        void init() {
            int i;
            synchronized (WiredAccessoryManager.this.mLock) {
                Slog.v(WiredAccessoryManager.TAG, "init()");
                char[] buffer = new char[DumpState.DUMP_PROVIDERS];
                for (i = 0; i < this.mUEventInfo.size(); i += WiredAccessoryManager.MSG_NEW_DEVICE_STATE) {
                    UEventInfo uei = (UEventInfo) this.mUEventInfo.get(i);
                    try {
                        FileReader file = new FileReader(uei.getSwitchStatePath());
                        int len = file.read(buffer, 0, DumpState.DUMP_PROVIDERS);
                        file.close();
                        int curState = Integer.parseInt(new String(buffer, 0, len).trim());
                        if (curState > 0) {
                            updateStateLocked(uei.getDevPath(), uei.getDevName(), curState);
                        }
                    } catch (FileNotFoundException e) {
                        Slog.w(WiredAccessoryManager.TAG, uei.getSwitchStatePath() + " not found while attempting to determine initial switch state");
                    } catch (Exception e2) {
                        Slog.e(WiredAccessoryManager.TAG, "", e2);
                    }
                }
            }
            for (i = 0; i < this.mUEventInfo.size(); i += WiredAccessoryManager.MSG_NEW_DEVICE_STATE) {
                startObserving("DEVPATH=" + ((UEventInfo) this.mUEventInfo.get(i)).getDevPath());
            }
        }

        private List<UEventInfo> makeObservedUEventList() {
            UEventInfo uei;
            List<UEventInfo> retVal = new ArrayList();
            if (!WiredAccessoryManager.this.mUseDevInputEventForAudioJack) {
                uei = new UEventInfo(WiredAccessoryManager.NAME_H2W, WiredAccessoryManager.MSG_NEW_DEVICE_STATE, WiredAccessoryManager.MSG_SYSTEM_READY, WiredAccessoryManager.BIT_LINEOUT);
                if (uei.checkSwitchExists()) {
                    retVal.add(uei);
                } else {
                    Slog.w(WiredAccessoryManager.TAG, "This kernel does not have wired headset support");
                }
            }
            uei = new UEventInfo(WiredAccessoryManager.NAME_USB_AUDIO, WiredAccessoryManager.BIT_USB_HEADSET_ANLG, WiredAccessoryManager.BIT_USB_HEADSET_DGTL, 0);
            if (uei.checkSwitchExists()) {
                retVal.add(uei);
            } else {
                Slog.w(WiredAccessoryManager.TAG, "This kernel does not have usb audio support");
            }
            uei = new UEventInfo(WiredAccessoryManager.NAME_HDMI_AUDIO, WiredAccessoryManager.BIT_HDMI_AUDIO, 0, 0);
            if (uei.checkSwitchExists()) {
                retVal.add(uei);
            } else {
                uei = new UEventInfo(WiredAccessoryManager.NAME_HDMI, WiredAccessoryManager.BIT_HDMI_AUDIO, 0, 0);
                if (uei.checkSwitchExists()) {
                    retVal.add(uei);
                } else {
                    Slog.w(WiredAccessoryManager.TAG, "This kernel does not have HDMI audio support");
                }
            }
            return retVal;
        }

        public void onUEvent(UEvent event) {
            Slog.v(WiredAccessoryManager.TAG, "Headset UEVENT: " + event.toString());
            try {
                String devPath = event.get("DEVPATH");
                String name = event.get("SWITCH_NAME");
                int state = Integer.parseInt(event.get("SWITCH_STATE"));
                synchronized (WiredAccessoryManager.this.mLock) {
                    updateStateLocked(devPath, name, state);
                }
            } catch (NumberFormatException e) {
                Slog.e(WiredAccessoryManager.TAG, "Could not parse switch state from event " + event);
            }
        }

        private void updateStateLocked(String devPath, String name, int state) {
            if (WiredAccessoryManager.NAME_H2W.equals(name)) {
                if (state == WiredAccessoryManager.HEADSET_REVERT_SEQUENCE) {
                    setHeadsetRevertSequenceState(WiredAccessoryManager.LOG);
                } else {
                    setHeadsetRevertSequenceState(false);
                }
            }
            for (int i = 0; i < this.mUEventInfo.size(); i += WiredAccessoryManager.MSG_NEW_DEVICE_STATE) {
                UEventInfo uei = (UEventInfo) this.mUEventInfo.get(i);
                if (devPath.equals(uei.getDevPath())) {
                    WiredAccessoryManager.this.updateLocked(name, uei.computeNewHeadsetState(WiredAccessoryManager.this.mHeadsetState, state));
                    return;
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.WiredAccessoryManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.WiredAccessoryManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.WiredAccessoryManager.<clinit>():void");
    }

    private void setDevicesState(int r1, int r2, java.lang.String r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.WiredAccessoryManager.setDevicesState(int, int, java.lang.String):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.WiredAccessoryManager.setDevicesState(int, int, java.lang.String):void");
    }

    public void notifyWiredAccessoryChanged(long r1, int r3, int r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.WiredAccessoryManager.notifyWiredAccessoryChanged(long, int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.WiredAccessoryManager.notifyWiredAccessoryChanged(long, int, int):void");
    }

    public WiredAccessoryManager(Context context, InputManagerService inputManager) {
        this.mLock = new Object();
        this.mHandler = new AnonymousClass1(Looper.myLooper(), null, LOG);
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(MSG_NEW_DEVICE_STATE, "WiredAccessoryManager");
        this.mWakeLock.setReferenceCounted(false);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mInputManager = inputManager;
        this.mUseDevInputEventForAudioJack = context.getResources().getBoolean(17956987);
        this.mObserver = new WiredAccessoryObserver();
    }

    private void onSystemReady() {
        if (this.mUseDevInputEventForAudioJack) {
            int switchValues = 0;
            if (this.mInputManager.getSwitchState(-1, -256, MSG_SYSTEM_READY) == MSG_NEW_DEVICE_STATE) {
                switchValues = BIT_USB_HEADSET_ANLG;
            }
            if (this.mInputManager.getSwitchState(-1, -256, BIT_USB_HEADSET_ANLG) == MSG_NEW_DEVICE_STATE) {
                switchValues |= BIT_HDMI_AUDIO;
            }
            if (this.mInputManager.getSwitchState(-1, -256, 6) == MSG_NEW_DEVICE_STATE) {
                switchValues |= 64;
            }
            notifyWiredAccessoryChanged(0, switchValues, 84);
        }
        this.mObserver.init();
    }

    public void systemReady() {
        synchronized (this.mLock) {
            this.mWakeLock.acquire();
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SYSTEM_READY, 0, 0, null));
        }
    }

    private void updateLocked(String newName, int newState) {
        int headsetState = newState & SUPPORTED_HEADSETS;
        int usb_headset_anlg = headsetState & BIT_USB_HEADSET_ANLG;
        int usb_headset_dgtl = headsetState & BIT_USB_HEADSET_DGTL;
        int h2w_headset = headsetState & 35;
        boolean h2wStateChange = LOG;
        boolean usbStateChange = LOG;
        Slog.v(TAG, "newName=" + newName + " newState=" + newState + " headsetState=" + headsetState + " prev headsetState=" + this.mHeadsetState);
        if (this.mHeadsetState == headsetState) {
            Log.e(TAG, "No state change.");
            return;
        }
        if (h2w_headset == 35) {
            Log.e(TAG, "Invalid combination, unsetting h2w flag");
            h2wStateChange = false;
        }
        if (usb_headset_anlg == BIT_USB_HEADSET_ANLG && usb_headset_dgtl == BIT_USB_HEADSET_DGTL) {
            Log.e(TAG, "Invalid combination, unsetting usb flag");
            usbStateChange = false;
        }
        if (h2wStateChange || usbStateChange) {
            this.mWakeLock.acquire();
            Log.i(TAG, "MSG_NEW_DEVICE_STATE");
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_NEW_DEVICE_STATE, headsetState, this.mHeadsetState, ""));
            this.mHeadsetState = headsetState;
            return;
        }
        Log.e(TAG, "invalid transition, returning ...");
    }

    private void setDeviceStateLocked(int headset, int headsetState, int prevHeadsetState, String headsetName) {
        if ((headsetState & headset) != (prevHeadsetState & headset)) {
            int state;
            int inDevice = 0;
            if ((headsetState & headset) != 0) {
                state = MSG_NEW_DEVICE_STATE;
            } else {
                state = 0;
            }
            if (MSG_NEW_DEVICE_STATE != headsetState || MSG_SYSTEM_READY != headset) {
                if (MSG_SYSTEM_READY != headsetState || MSG_NEW_DEVICE_STATE != headset) {
                    int outDevice;
                    if (headset == MSG_NEW_DEVICE_STATE) {
                        outDevice = BIT_USB_HEADSET_ANLG;
                        inDevice = -2147483632;
                    } else if (headset == MSG_SYSTEM_READY) {
                        outDevice = BIT_USB_HEADSET_DGTL;
                    } else if (headset == BIT_LINEOUT) {
                        outDevice = DumpState.DUMP_INTENT_FILTER_VERIFIERS;
                    } else if (headset == BIT_USB_HEADSET_ANLG) {
                        outDevice = DumpState.DUMP_VERIFIERS;
                    } else if (headset == BIT_USB_HEADSET_DGTL) {
                        outDevice = DumpState.DUMP_PREFERRED;
                    } else if (headset == BIT_HDMI_AUDIO) {
                        outDevice = DumpState.DUMP_PROVIDERS;
                    } else {
                        Slog.e(TAG, "setDeviceState() invalid headset type: " + headset);
                        return;
                    }
                    Slog.v(TAG, "headsetName: " + headsetName + (state == MSG_NEW_DEVICE_STATE ? " connected" : " disconnected"));
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
        if (!((switchMask & BIT_USB_HEADSET_ANLG) == 0 || (switchValues & BIT_USB_HEADSET_ANLG) == 0)) {
            sb.append("SW_HEADPHONE_INSERT ");
        }
        if (!((switchMask & BIT_HDMI_AUDIO) == 0 || (switchValues & BIT_HDMI_AUDIO) == 0)) {
            sb.append("SW_MICROPHONE_INSERT");
        }
        return sb.toString();
    }
}
