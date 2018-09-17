package com.android.server.hdmi;

import android.hardware.hdmi.HdmiPortInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Predicate;
import com.android.server.hdmi.HdmiAnnotations.IoThreadOnly;
import com.android.server.hdmi.HdmiAnnotations.ServiceThreadOnly;
import com.android.server.wm.WindowState;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final class HdmiCecController {
    private static final byte[] EMPTY_BODY = null;
    private static final int NUM_LOGICAL_ADDRESS = 16;
    private static final String TAG = "HdmiCecController";
    private Handler mControlHandler;
    private Handler mIoHandler;
    private final SparseArray<HdmiCecLocalDevice> mLocalDevices;
    private volatile long mNativePtr;
    private final Predicate<Integer> mRemoteDeviceAddressPredicate;
    private final HdmiControlService mService;
    private final Predicate<Integer> mSystemAudioAddressPredicate;

    /* renamed from: com.android.server.hdmi.HdmiCecController.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ AllocateAddressCallback val$callback;
        final /* synthetic */ int val$deviceType;
        final /* synthetic */ int val$preferredAddress;

        AnonymousClass3(int val$deviceType, int val$preferredAddress, AllocateAddressCallback val$callback) {
            this.val$deviceType = val$deviceType;
            this.val$preferredAddress = val$preferredAddress;
            this.val$callback = val$callback;
        }

        public void run() {
            HdmiCecController.this.handleAllocateLogicalAddress(this.val$deviceType, this.val$preferredAddress, this.val$callback);
        }
    }

    /* renamed from: com.android.server.hdmi.HdmiCecController.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ int val$assignedAddress;
        final /* synthetic */ AllocateAddressCallback val$callback;
        final /* synthetic */ int val$deviceType;

        AnonymousClass4(AllocateAddressCallback val$callback, int val$deviceType, int val$assignedAddress) {
            this.val$callback = val$callback;
            this.val$deviceType = val$deviceType;
            this.val$assignedAddress = val$assignedAddress;
        }

        public void run() {
            this.val$callback.onAllocated(this.val$deviceType, this.val$assignedAddress);
        }
    }

    /* renamed from: com.android.server.hdmi.HdmiCecController.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ List val$allocated;
        final /* synthetic */ DevicePollingCallback val$callback;
        final /* synthetic */ Integer val$candidate;
        final /* synthetic */ List val$candidates;
        final /* synthetic */ int val$retryCount;
        final /* synthetic */ int val$sourceAddress;

        /* renamed from: com.android.server.hdmi.HdmiCecController.5.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ List val$allocated;
            final /* synthetic */ DevicePollingCallback val$callback;
            final /* synthetic */ List val$candidates;
            final /* synthetic */ int val$retryCount;
            final /* synthetic */ int val$sourceAddress;

            AnonymousClass1(int val$sourceAddress, List val$candidates, int val$retryCount, DevicePollingCallback val$callback, List val$allocated) {
                this.val$sourceAddress = val$sourceAddress;
                this.val$candidates = val$candidates;
                this.val$retryCount = val$retryCount;
                this.val$callback = val$callback;
                this.val$allocated = val$allocated;
            }

            public void run() {
                HdmiCecController.this.runDevicePolling(this.val$sourceAddress, this.val$candidates, this.val$retryCount, this.val$callback, this.val$allocated);
            }
        }

        AnonymousClass5(int val$sourceAddress, Integer val$candidate, int val$retryCount, List val$allocated, List val$candidates, DevicePollingCallback val$callback) {
            this.val$sourceAddress = val$sourceAddress;
            this.val$candidate = val$candidate;
            this.val$retryCount = val$retryCount;
            this.val$allocated = val$allocated;
            this.val$candidates = val$candidates;
            this.val$callback = val$callback;
        }

        public void run() {
            if (HdmiCecController.this.sendPollMessage(this.val$sourceAddress, this.val$candidate.intValue(), this.val$retryCount)) {
                this.val$allocated.add(this.val$candidate);
            }
            HdmiCecController.this.runOnServiceThread(new AnonymousClass1(this.val$sourceAddress, this.val$candidates, this.val$retryCount, this.val$callback, this.val$allocated));
        }
    }

    /* renamed from: com.android.server.hdmi.HdmiCecController.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ Runnable val$runnable;

        AnonymousClass6(Runnable val$runnable) {
            this.val$runnable = val$runnable;
        }

        public void run() {
            HdmiCecController.this.runOnServiceThread(this.val$runnable);
        }
    }

    /* renamed from: com.android.server.hdmi.HdmiCecController.7 */
    class AnonymousClass7 implements Runnable {
        final /* synthetic */ SendMessageCallback val$callback;
        final /* synthetic */ HdmiCecMessage val$cecMessage;

        /* renamed from: com.android.server.hdmi.HdmiCecController.7.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ SendMessageCallback val$callback;
            final /* synthetic */ int val$finalError;

            AnonymousClass1(SendMessageCallback val$callback, int val$finalError) {
                this.val$callback = val$callback;
                this.val$finalError = val$finalError;
            }

            public void run() {
                this.val$callback.onSendCompleted(this.val$finalError);
            }
        }

        AnonymousClass7(HdmiCecMessage val$cecMessage, SendMessageCallback val$callback) {
            this.val$cecMessage = val$cecMessage;
            this.val$callback = val$callback;
        }

        public void run() {
            int errorCode;
            HdmiLogger.debug("[S]:" + this.val$cecMessage, new Object[0]);
            byte[] body = HdmiCecController.buildBody(this.val$cecMessage.getOpcode(), this.val$cecMessage.getParams());
            int i = 0;
            while (true) {
                errorCode = HdmiCecController.nativeSendCecCommand(HdmiCecController.this.mNativePtr, this.val$cecMessage.getSource(), this.val$cecMessage.getDestination(), body);
                if (errorCode != 0) {
                    int i2 = i + 1;
                    if (i >= 1) {
                        break;
                    }
                    i = i2;
                } else {
                    break;
                }
            }
            int finalError = errorCode;
            if (errorCode != 0) {
                Slog.w(HdmiCecController.TAG, "Failed to send " + this.val$cecMessage);
            }
            if (this.val$callback != null) {
                HdmiCecController.this.runOnServiceThread(new AnonymousClass1(this.val$callback, errorCode));
            }
        }
    }

    interface AllocateAddressCallback {
        void onAllocated(int i, int i2);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.hdmi.HdmiCecController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.hdmi.HdmiCecController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.hdmi.HdmiCecController.<clinit>():void");
    }

    private static native int nativeAddLogicalAddress(long j, int i);

    private static native void nativeClearLogicalAddress(long j);

    private static native int nativeGetPhysicalAddress(long j);

    private static native HdmiPortInfo[] nativeGetPortInfos(long j);

    private static native int nativeGetVendorId(long j);

    private static native int nativeGetVersion(long j);

    private static native long nativeInit(HdmiCecController hdmiCecController, MessageQueue messageQueue);

    private static native boolean nativeIsConnected(long j, int i);

    private static native int nativeSendCecCommand(long j, int i, int i2, byte[] bArr);

    private static native void nativeSetAudioReturnChannel(long j, int i, boolean z);

    private static native void nativeSetOption(long j, int i, int i2);

    private HdmiCecController(HdmiControlService service) {
        this.mRemoteDeviceAddressPredicate = new Predicate<Integer>() {
            public boolean apply(Integer address) {
                return !HdmiCecController.this.isAllocatedLocalDeviceAddress(address.intValue());
            }
        };
        this.mSystemAudioAddressPredicate = new Predicate<Integer>() {
            public boolean apply(Integer address) {
                return HdmiUtils.getTypeFromAddress(address.intValue()) == 5;
            }
        };
        this.mLocalDevices = new SparseArray();
        this.mService = service;
    }

    static HdmiCecController create(HdmiControlService service) {
        HdmiCecController controller = new HdmiCecController(service);
        long nativePtr = nativeInit(controller, service.getServiceLooper().getQueue());
        if (nativePtr == 0) {
            return null;
        }
        controller.init(nativePtr);
        return controller;
    }

    private void init(long nativePtr) {
        this.mIoHandler = new Handler(this.mService.getIoLooper());
        this.mControlHandler = new Handler(this.mService.getServiceLooper());
        this.mNativePtr = nativePtr;
    }

    @ServiceThreadOnly
    void addLocalDevice(int deviceType, HdmiCecLocalDevice device) {
        assertRunOnServiceThread();
        this.mLocalDevices.put(deviceType, device);
    }

    @ServiceThreadOnly
    void allocateLogicalAddress(int deviceType, int preferredAddress, AllocateAddressCallback callback) {
        assertRunOnServiceThread();
        runOnIoThread(new AnonymousClass3(deviceType, preferredAddress, callback));
    }

    @IoThreadOnly
    private void handleAllocateLogicalAddress(int deviceType, int preferredAddress, AllocateAddressCallback callback) {
        int i;
        assertRunOnIoThread();
        int startAddress = preferredAddress;
        if (preferredAddress == 15) {
            for (i = 0; i < NUM_LOGICAL_ADDRESS; i++) {
                if (deviceType == HdmiUtils.getTypeFromAddress(i)) {
                    startAddress = i;
                    break;
                }
            }
        }
        int logicalAddress = 15;
        for (i = 0; i < NUM_LOGICAL_ADDRESS; i++) {
            int curAddress = (startAddress + i) % NUM_LOGICAL_ADDRESS;
            if (curAddress != 15 && deviceType == HdmiUtils.getTypeFromAddress(curAddress)) {
                int failedPollingCount = 0;
                for (int j = 0; j < 3; j++) {
                    if (!sendPollMessage(curAddress, curAddress, 1)) {
                        failedPollingCount++;
                    }
                }
                if (failedPollingCount * 2 > 3) {
                    logicalAddress = curAddress;
                    break;
                }
            }
        }
        HdmiLogger.debug("New logical address for device [%d]: [preferred:%d, assigned:%d]", Integer.valueOf(deviceType), Integer.valueOf(preferredAddress), Integer.valueOf(logicalAddress));
        if (callback != null) {
            runOnServiceThread(new AnonymousClass4(callback, deviceType, assignedAddress));
        }
    }

    private static byte[] buildBody(int opcode, byte[] params) {
        byte[] body = new byte[(params.length + 1)];
        body[0] = (byte) opcode;
        System.arraycopy(params, 0, body, 1, params.length);
        return body;
    }

    HdmiPortInfo[] getPortInfos() {
        return nativeGetPortInfos(this.mNativePtr);
    }

    HdmiCecLocalDevice getLocalDevice(int deviceType) {
        return (HdmiCecLocalDevice) this.mLocalDevices.get(deviceType);
    }

    @ServiceThreadOnly
    int addLogicalAddress(int newLogicalAddress) {
        assertRunOnServiceThread();
        if (HdmiUtils.isValidAddress(newLogicalAddress)) {
            return nativeAddLogicalAddress(this.mNativePtr, newLogicalAddress);
        }
        return -1;
    }

    @ServiceThreadOnly
    void clearLogicalAddress() {
        assertRunOnServiceThread();
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            ((HdmiCecLocalDevice) this.mLocalDevices.valueAt(i)).clearAddress();
        }
        nativeClearLogicalAddress(this.mNativePtr);
    }

    @ServiceThreadOnly
    void clearLocalDevices() {
        assertRunOnServiceThread();
        this.mLocalDevices.clear();
    }

    @ServiceThreadOnly
    int getPhysicalAddress() {
        assertRunOnServiceThread();
        return nativeGetPhysicalAddress(this.mNativePtr);
    }

    @ServiceThreadOnly
    int getVersion() {
        assertRunOnServiceThread();
        return nativeGetVersion(this.mNativePtr);
    }

    @ServiceThreadOnly
    int getVendorId() {
        assertRunOnServiceThread();
        return nativeGetVendorId(this.mNativePtr);
    }

    @ServiceThreadOnly
    void setOption(int flag, int value) {
        assertRunOnServiceThread();
        HdmiLogger.debug("setOption: [flag:%d, value:%d]", Integer.valueOf(flag), Integer.valueOf(value));
        nativeSetOption(this.mNativePtr, flag, value);
    }

    @ServiceThreadOnly
    void setAudioReturnChannel(int port, boolean enabled) {
        assertRunOnServiceThread();
        nativeSetAudioReturnChannel(this.mNativePtr, port, enabled);
    }

    @ServiceThreadOnly
    boolean isConnected(int port) {
        assertRunOnServiceThread();
        return nativeIsConnected(this.mNativePtr, port);
    }

    @ServiceThreadOnly
    void pollDevices(DevicePollingCallback callback, int sourceAddress, int pickStrategy, int retryCount) {
        assertRunOnServiceThread();
        runDevicePolling(sourceAddress, pickPollCandidates(pickStrategy), retryCount, callback, new ArrayList());
    }

    @ServiceThreadOnly
    List<HdmiCecLocalDevice> getLocalDeviceList() {
        assertRunOnServiceThread();
        return HdmiUtils.sparseArrayToList(this.mLocalDevices);
    }

    private List<Integer> pickPollCandidates(int pickStrategy) {
        Predicate<Integer> pickPredicate;
        switch (pickStrategy & 3) {
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                pickPredicate = this.mSystemAudioAddressPredicate;
                break;
            default:
                pickPredicate = this.mRemoteDeviceAddressPredicate;
                break;
        }
        int iterationStrategy = pickStrategy & 196608;
        LinkedList<Integer> pollingCandidates = new LinkedList();
        int i;
        switch (iterationStrategy) {
            case DumpState.DUMP_INSTALLS /*65536*/:
                for (i = 0; i <= 14; i++) {
                    if (pickPredicate.apply(Integer.valueOf(i))) {
                        pollingCandidates.add(Integer.valueOf(i));
                    }
                }
                break;
            default:
                for (i = 14; i >= 0; i--) {
                    if (pickPredicate.apply(Integer.valueOf(i))) {
                        pollingCandidates.add(Integer.valueOf(i));
                    }
                }
                break;
        }
        return pollingCandidates;
    }

    @ServiceThreadOnly
    private boolean isAllocatedLocalDeviceAddress(int address) {
        assertRunOnServiceThread();
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            if (((HdmiCecLocalDevice) this.mLocalDevices.valueAt(i)).isAddressOf(address)) {
                return true;
            }
        }
        return false;
    }

    @ServiceThreadOnly
    private void runDevicePolling(int sourceAddress, List<Integer> candidates, int retryCount, DevicePollingCallback callback, List<Integer> allocated) {
        assertRunOnServiceThread();
        if (candidates.isEmpty()) {
            if (callback != null) {
                HdmiLogger.debug("[P]:AllocatedAddress=%s", allocated.toString());
                callback.onPollingFinished(allocated);
            }
            return;
        }
        runOnIoThread(new AnonymousClass5(sourceAddress, (Integer) candidates.remove(0), retryCount, allocated, candidates, callback));
    }

    @IoThreadOnly
    private boolean sendPollMessage(int sourceAddress, int destinationAddress, int retryCount) {
        assertRunOnIoThread();
        for (int i = 0; i < retryCount; i++) {
            if (nativeSendCecCommand(this.mNativePtr, sourceAddress, destinationAddress, EMPTY_BODY) == 0) {
                return true;
            }
        }
        return false;
    }

    private void assertRunOnIoThread() {
        if (Looper.myLooper() != this.mIoHandler.getLooper()) {
            throw new IllegalStateException("Should run on io thread.");
        }
    }

    private void assertRunOnServiceThread() {
        if (Looper.myLooper() != this.mControlHandler.getLooper()) {
            throw new IllegalStateException("Should run on service thread.");
        }
    }

    private void runOnIoThread(Runnable runnable) {
        this.mIoHandler.post(runnable);
    }

    private void runOnServiceThread(Runnable runnable) {
        this.mControlHandler.post(runnable);
    }

    @ServiceThreadOnly
    void flush(Runnable runnable) {
        assertRunOnServiceThread();
        runOnIoThread(new AnonymousClass6(runnable));
    }

    private boolean isAcceptableAddress(int address) {
        if (address == 15) {
            return true;
        }
        return isAllocatedLocalDeviceAddress(address);
    }

    @ServiceThreadOnly
    private void onReceiveCommand(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!isAcceptableAddress(message.getDestination()) || !this.mService.handleCecCommand(message)) {
            maySendFeatureAbortCommand(message, 0);
        }
    }

    @ServiceThreadOnly
    void maySendFeatureAbortCommand(HdmiCecMessage message, int reason) {
        assertRunOnServiceThread();
        int src = message.getDestination();
        int dest = message.getSource();
        if (src != 15 && dest != 15) {
            int originalOpcode = message.getOpcode();
            if (originalOpcode != 0) {
                sendCommand(HdmiCecMessageBuilder.buildFeatureAbortCommand(src, dest, originalOpcode, reason));
            }
        }
    }

    @ServiceThreadOnly
    void sendCommand(HdmiCecMessage cecMessage) {
        assertRunOnServiceThread();
        sendCommand(cecMessage, null);
    }

    @ServiceThreadOnly
    void sendCommand(HdmiCecMessage cecMessage, SendMessageCallback callback) {
        assertRunOnServiceThread();
        runOnIoThread(new AnonymousClass7(cecMessage, callback));
    }

    @ServiceThreadOnly
    private void handleIncomingCecCommand(int srcAddress, int dstAddress, byte[] body) {
        assertRunOnServiceThread();
        HdmiCecMessage command = HdmiCecMessageBuilder.of(srcAddress, dstAddress, body);
        HdmiLogger.debug("[R]:" + command, new Object[0]);
        onReceiveCommand(command);
    }

    @ServiceThreadOnly
    private void handleHotplug(int port, boolean connected) {
        assertRunOnServiceThread();
        HdmiLogger.debug("Hotplug event:[port:%d, connected:%b]", Integer.valueOf(port), Boolean.valueOf(connected));
        this.mService.onHotplug(port, connected);
    }

    void dump(IndentingPrintWriter pw) {
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            pw.println("HdmiCecLocalDevice #" + i + ":");
            pw.increaseIndent();
            ((HdmiCecLocalDevice) this.mLocalDevices.valueAt(i)).dump(pw);
            pw.decreaseIndent();
        }
    }
}
