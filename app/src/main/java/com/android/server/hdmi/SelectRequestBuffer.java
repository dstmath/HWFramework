package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;
import android.os.RemoteException;
import android.util.Slog;

public class SelectRequestBuffer {
    public static final SelectRequestBuffer EMPTY_BUFFER = null;
    private static final String TAG = "SelectRequestBuffer";
    private SelectRequest mRequest;

    public static abstract class SelectRequest {
        protected final IHdmiControlCallback mCallback;
        protected final int mId;
        protected final HdmiControlService mService;

        public abstract void process();

        public SelectRequest(HdmiControlService service, int id, IHdmiControlCallback callback) {
            this.mService = service;
            this.mId = id;
            this.mCallback = callback;
        }

        protected HdmiCecLocalDeviceTv tv() {
            return this.mService.tv();
        }

        protected boolean isLocalDeviceReady() {
            if (tv() != null) {
                return true;
            }
            Slog.e(SelectRequestBuffer.TAG, "Local tv device not available");
            invokeCallback(2);
            return false;
        }

        private void invokeCallback(int reason) {
            try {
                if (this.mCallback != null) {
                    this.mCallback.onComplete(reason);
                }
            } catch (RemoteException e) {
                Slog.e(SelectRequestBuffer.TAG, "Invoking callback failed:" + e);
            }
        }
    }

    public static class DeviceSelectRequest extends SelectRequest {
        private DeviceSelectRequest(HdmiControlService srv, int id, IHdmiControlCallback callback) {
            super(srv, id, callback);
        }

        public void process() {
            if (isLocalDeviceReady()) {
                Slog.v(SelectRequestBuffer.TAG, "calling delayed deviceSelect id:" + this.mId);
                tv().deviceSelect(this.mId, this.mCallback);
            }
        }
    }

    public static class PortSelectRequest extends SelectRequest {
        private PortSelectRequest(HdmiControlService srv, int id, IHdmiControlCallback callback) {
            super(srv, id, callback);
        }

        public void process() {
            if (isLocalDeviceReady()) {
                Slog.v(SelectRequestBuffer.TAG, "calling delayed portSelect id:" + this.mId);
                tv().doManualPortSwitching(this.mId, this.mCallback);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.hdmi.SelectRequestBuffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.hdmi.SelectRequestBuffer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.hdmi.SelectRequestBuffer.<clinit>():void");
    }

    public static DeviceSelectRequest newDeviceSelect(HdmiControlService srv, int id, IHdmiControlCallback callback) {
        return new DeviceSelectRequest(id, callback, null);
    }

    public static PortSelectRequest newPortSelect(HdmiControlService srv, int id, IHdmiControlCallback callback) {
        return new PortSelectRequest(id, callback, null);
    }

    public void set(SelectRequest request) {
        this.mRequest = request;
    }

    public void process() {
        if (this.mRequest != null) {
            this.mRequest.process();
            clear();
        }
    }

    public void clear() {
        this.mRequest = null;
    }
}
