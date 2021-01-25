package com.android.server.hdmi;

import android.hardware.hdmi.HdmiPortInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.hdmi.HdmiAnnotations;
import com.android.server.hdmi.HdmiControlService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Predicate;
import libcore.util.EmptyArray;
import sun.util.locale.LanguageTag;

/* access modifiers changed from: package-private */
public final class HdmiCecController {
    private static final byte[] EMPTY_BODY = EmptyArray.BYTE;
    private static final int MAX_CEC_MESSAGE_HISTORY = 200;
    private static final int NUM_LOGICAL_ADDRESS = 16;
    private static final String TAG = "HdmiCecController";
    private Handler mControlHandler;
    private Handler mIoHandler;
    private final SparseArray<HdmiCecLocalDevice> mLocalDevices = new SparseArray<>();
    private final ArrayBlockingQueue<MessageHistoryRecord> mMessageHistory = new ArrayBlockingQueue<>(200);
    private volatile long mNativePtr;
    private final NativeWrapper mNativeWrapperImpl;
    private final List<Integer> mNeverAssignLogicalAddresses;
    private final Predicate<Integer> mRemoteDeviceAddressPredicate = new Predicate<Integer>() {
        /* class com.android.server.hdmi.HdmiCecController.AnonymousClass1 */

        public boolean test(Integer address) {
            return !HdmiCecController.this.isAllocatedLocalDeviceAddress(address.intValue());
        }
    };
    private final HdmiControlService mService;
    private final Predicate<Integer> mSystemAudioAddressPredicate = new Predicate<Integer>() {
        /* class com.android.server.hdmi.HdmiCecController.AnonymousClass2 */

        public boolean test(Integer address) {
            return HdmiUtils.getTypeFromAddress(address.intValue()) == 5;
        }
    };

    /* access modifiers changed from: package-private */
    public interface AllocateAddressCallback {
        void onAllocated(int i, int i2);
    }

    /* access modifiers changed from: protected */
    public interface NativeWrapper {
        int nativeAddLogicalAddress(long j, int i);

        void nativeClearLogicalAddress(long j);

        void nativeEnableAudioReturnChannel(long j, int i, boolean z);

        int nativeGetPhysicalAddress(long j);

        HdmiPortInfo[] nativeGetPortInfos(long j);

        int nativeGetVendorId(long j);

        int nativeGetVersion(long j);

        long nativeInit(HdmiCecController hdmiCecController, MessageQueue messageQueue);

        boolean nativeIsConnected(long j, int i);

        int nativeSendCecCommand(long j, int i, int i2, byte[] bArr);

        void nativeSetLanguage(long j, String str);

        void nativeSetOption(long j, int i, boolean z);
    }

    /* access modifiers changed from: private */
    public static native int nativeAddLogicalAddress(long j, int i);

    /* access modifiers changed from: private */
    public static native void nativeClearLogicalAddress(long j);

    /* access modifiers changed from: private */
    public static native void nativeEnableAudioReturnChannel(long j, int i, boolean z);

    /* access modifiers changed from: private */
    public static native int nativeGetPhysicalAddress(long j);

    /* access modifiers changed from: private */
    public static native HdmiPortInfo[] nativeGetPortInfos(long j);

    /* access modifiers changed from: private */
    public static native int nativeGetVendorId(long j);

    /* access modifiers changed from: private */
    public static native int nativeGetVersion(long j);

    /* access modifiers changed from: private */
    public static native long nativeInit(HdmiCecController hdmiCecController, MessageQueue messageQueue);

    /* access modifiers changed from: private */
    public static native boolean nativeIsConnected(long j, int i);

    /* access modifiers changed from: private */
    public static native int nativeSendCecCommand(long j, int i, int i2, byte[] bArr);

    /* access modifiers changed from: private */
    public static native void nativeSetLanguage(long j, String str);

    /* access modifiers changed from: private */
    public static native void nativeSetOption(long j, int i, boolean z);

    private HdmiCecController(HdmiControlService service, NativeWrapper nativeWrapper) {
        this.mService = service;
        this.mNativeWrapperImpl = nativeWrapper;
        HdmiControlService hdmiControlService = this.mService;
        this.mNeverAssignLogicalAddresses = HdmiControlService.getIntList(SystemProperties.get("ro.hdmi.property_hdmi_cec_never_assign_logical_addresses"));
    }

    static HdmiCecController create(HdmiControlService service) {
        return createWithNativeWrapper(service, new NativeWrapperImpl());
    }

    static HdmiCecController createWithNativeWrapper(HdmiControlService service, NativeWrapper nativeWrapper) {
        HdmiCecController controller = new HdmiCecController(service, nativeWrapper);
        long nativePtr = nativeWrapper.nativeInit(controller, service.getServiceLooper().getQueue());
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

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void addLocalDevice(int deviceType, HdmiCecLocalDevice device) {
        assertRunOnServiceThread();
        this.mLocalDevices.put(deviceType, device);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void allocateLogicalAddress(final int deviceType, final int preferredAddress, final AllocateAddressCallback callback) {
        assertRunOnServiceThread();
        runOnIoThread(new Runnable() {
            /* class com.android.server.hdmi.HdmiCecController.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                HdmiCecController.this.handleAllocateLogicalAddress(deviceType, preferredAddress, callback);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.IoThreadOnly
    private void handleAllocateLogicalAddress(final int deviceType, int preferredAddress, final AllocateAddressCallback callback) {
        assertRunOnIoThread();
        int startAddress = preferredAddress;
        if (preferredAddress == 15) {
            int i = 0;
            while (true) {
                if (i >= 16) {
                    break;
                } else if (deviceType == HdmiUtils.getTypeFromAddress(i)) {
                    startAddress = i;
                    break;
                } else {
                    i++;
                }
            }
        }
        final int logicalAddress = 15;
        int i2 = 0;
        while (true) {
            if (i2 >= 16) {
                break;
            }
            int curAddress = (startAddress + i2) % 16;
            if (curAddress != 15 && deviceType == HdmiUtils.getTypeFromAddress(curAddress) && !this.mNeverAssignLogicalAddresses.contains(Integer.valueOf(curAddress))) {
                boolean acked = false;
                int j = 0;
                while (true) {
                    if (j >= 3) {
                        break;
                    } else if (sendPollMessage(curAddress, curAddress, 1)) {
                        acked = true;
                        break;
                    } else {
                        j++;
                    }
                }
                if (!acked) {
                    logicalAddress = curAddress;
                    break;
                }
            }
            i2++;
        }
        HdmiLogger.debug("New logical address for device [%d]: [preferred:%d, assigned:%d]", Integer.valueOf(deviceType), Integer.valueOf(preferredAddress), Integer.valueOf(logicalAddress));
        if (callback != null) {
            runOnServiceThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiCecController.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    callback.onAllocated(deviceType, logicalAddress);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public static byte[] buildBody(int opcode, byte[] params) {
        byte[] body = new byte[(params.length + 1)];
        body[0] = (byte) opcode;
        System.arraycopy(params, 0, body, 1, params.length);
        return body;
    }

    /* access modifiers changed from: package-private */
    public HdmiPortInfo[] getPortInfos() {
        return this.mNativeWrapperImpl.nativeGetPortInfos(this.mNativePtr);
    }

    /* access modifiers changed from: package-private */
    public HdmiCecLocalDevice getLocalDevice(int deviceType) {
        return this.mLocalDevices.get(deviceType);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public int addLogicalAddress(int newLogicalAddress) {
        assertRunOnServiceThread();
        if (HdmiUtils.isValidAddress(newLogicalAddress)) {
            return this.mNativeWrapperImpl.nativeAddLogicalAddress(this.mNativePtr, newLogicalAddress);
        }
        return 2;
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void clearLogicalAddress() {
        assertRunOnServiceThread();
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            this.mLocalDevices.valueAt(i).clearAddress();
        }
        this.mNativeWrapperImpl.nativeClearLogicalAddress(this.mNativePtr);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void clearLocalDevices() {
        assertRunOnServiceThread();
        this.mLocalDevices.clear();
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public int getPhysicalAddress() {
        assertRunOnServiceThread();
        return this.mNativeWrapperImpl.nativeGetPhysicalAddress(this.mNativePtr);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public int getVersion() {
        assertRunOnServiceThread();
        return this.mNativeWrapperImpl.nativeGetVersion(this.mNativePtr);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public int getVendorId() {
        assertRunOnServiceThread();
        return this.mNativeWrapperImpl.nativeGetVendorId(this.mNativePtr);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setOption(int flag, boolean enabled) {
        assertRunOnServiceThread();
        HdmiLogger.debug("setOption: [flag:%d, enabled:%b]", Integer.valueOf(flag), Boolean.valueOf(enabled));
        this.mNativeWrapperImpl.nativeSetOption(this.mNativePtr, flag, enabled);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void setLanguage(String language) {
        assertRunOnServiceThread();
        if (LanguageTag.isLanguage(language)) {
            this.mNativeWrapperImpl.nativeSetLanguage(this.mNativePtr, language);
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void enableAudioReturnChannel(int port, boolean enabled) {
        assertRunOnServiceThread();
        this.mNativeWrapperImpl.nativeEnableAudioReturnChannel(this.mNativePtr, port, enabled);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public boolean isConnected(int port) {
        assertRunOnServiceThread();
        return this.mNativeWrapperImpl.nativeIsConnected(this.mNativePtr, port);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void pollDevices(HdmiControlService.DevicePollingCallback callback, int sourceAddress, int pickStrategy, int retryCount) {
        assertRunOnServiceThread();
        runDevicePolling(sourceAddress, pickPollCandidates(pickStrategy), retryCount, callback, new ArrayList<>());
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public List<HdmiCecLocalDevice> getLocalDeviceList() {
        assertRunOnServiceThread();
        return HdmiUtils.sparseArrayToList(this.mLocalDevices);
    }

    private List<Integer> pickPollCandidates(int pickStrategy) {
        Predicate<Integer> pickPredicate;
        if ((pickStrategy & 3) != 2) {
            pickPredicate = this.mRemoteDeviceAddressPredicate;
        } else {
            pickPredicate = this.mSystemAudioAddressPredicate;
        }
        int iterationStrategy = 196608 & pickStrategy;
        LinkedList<Integer> pollingCandidates = new LinkedList<>();
        if (iterationStrategy != 65536) {
            for (int i = 14; i >= 0; i--) {
                if (pickPredicate.test(Integer.valueOf(i))) {
                    pollingCandidates.add(Integer.valueOf(i));
                }
            }
        } else {
            for (int i2 = 0; i2 <= 14; i2++) {
                if (pickPredicate.test(Integer.valueOf(i2))) {
                    pollingCandidates.add(Integer.valueOf(i2));
                }
            }
        }
        return pollingCandidates;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
    private boolean isAllocatedLocalDeviceAddress(int address) {
        assertRunOnServiceThread();
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            if (this.mLocalDevices.valueAt(i).isAddressOf(address)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.ServiceThreadOnly
    private void runDevicePolling(final int sourceAddress, final List<Integer> candidates, final int retryCount, final HdmiControlService.DevicePollingCallback callback, final List<Integer> allocated) {
        assertRunOnServiceThread();
        if (!candidates.isEmpty()) {
            final Integer candidate = candidates.remove(0);
            runOnIoThread(new Runnable() {
                /* class com.android.server.hdmi.HdmiCecController.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    if (HdmiCecController.this.sendPollMessage(sourceAddress, candidate.intValue(), retryCount)) {
                        allocated.add(candidate);
                    }
                    HdmiCecController.this.runOnServiceThread(new Runnable() {
                        /* class com.android.server.hdmi.HdmiCecController.AnonymousClass5.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            HdmiCecController.this.runDevicePolling(sourceAddress, candidates, retryCount, callback, allocated);
                        }
                    });
                }
            });
        } else if (callback != null) {
            HdmiLogger.debug("[P]:AllocatedAddress=%s", allocated.toString());
            callback.onPollingFinished(allocated);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @HdmiAnnotations.IoThreadOnly
    private boolean sendPollMessage(int sourceAddress, int destinationAddress, int retryCount) {
        assertRunOnIoThread();
        for (int i = 0; i < retryCount; i++) {
            int ret = this.mNativeWrapperImpl.nativeSendCecCommand(this.mNativePtr, sourceAddress, destinationAddress, EMPTY_BODY);
            if (ret == 0) {
                return true;
            }
            if (ret != 1) {
                HdmiLogger.warning("Failed to send a polling message(%d->%d) with return code %d", Integer.valueOf(sourceAddress), Integer.valueOf(destinationAddress), Integer.valueOf(ret));
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void runOnServiceThread(Runnable runnable) {
        this.mControlHandler.post(runnable);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void flush(final Runnable runnable) {
        assertRunOnServiceThread();
        runOnIoThread(new Runnable() {
            /* class com.android.server.hdmi.HdmiCecController.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                HdmiCecController.this.runOnServiceThread(runnable);
            }
        });
    }

    private boolean isAcceptableAddress(int address) {
        if (address == 15) {
            return true;
        }
        return isAllocatedLocalDeviceAddress(address);
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void onReceiveCommand(HdmiCecMessage message) {
        assertRunOnServiceThread();
        if (!isAcceptableAddress(message.getDestination()) || !this.mService.handleCecCommand(message)) {
            maySendFeatureAbortCommand(message, 0);
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void maySendFeatureAbortCommand(HdmiCecMessage message, int reason) {
        int originalOpcode;
        assertRunOnServiceThread();
        int src = message.getDestination();
        int dest = message.getSource();
        if (src != 15 && dest != 15 && (originalOpcode = message.getOpcode()) != 0) {
            sendCommand(HdmiCecMessageBuilder.buildFeatureAbortCommand(src, dest, originalOpcode, reason));
        }
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void sendCommand(HdmiCecMessage cecMessage) {
        assertRunOnServiceThread();
        sendCommand(cecMessage, null);
    }

    /* access modifiers changed from: package-private */
    @HdmiAnnotations.ServiceThreadOnly
    public void sendCommand(final HdmiCecMessage cecMessage, final HdmiControlService.SendMessageCallback callback) {
        assertRunOnServiceThread();
        addMessageToHistory(false, cecMessage);
        runOnIoThread(new Runnable() {
            /* class com.android.server.hdmi.HdmiCecController.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                final int errorCode;
                HdmiLogger.debug("[S]:" + cecMessage, new Object[0]);
                byte[] body = HdmiCecController.buildBody(cecMessage.getOpcode(), cecMessage.getParams());
                int i = 0;
                while (true) {
                    errorCode = HdmiCecController.this.mNativeWrapperImpl.nativeSendCecCommand(HdmiCecController.this.mNativePtr, cecMessage.getSource(), cecMessage.getDestination(), body);
                    if (errorCode == 0) {
                        break;
                    }
                    int i2 = i + 1;
                    if (i >= 1) {
                        break;
                    }
                    i = i2;
                }
                if (errorCode != 0) {
                    Slog.w(HdmiCecController.TAG, "Failed to send " + cecMessage + " with errorCode=" + errorCode);
                }
                if (callback != null) {
                    HdmiCecController.this.runOnServiceThread(new Runnable() {
                        /* class com.android.server.hdmi.HdmiCecController.AnonymousClass7.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            callback.onSendCompleted(errorCode);
                        }
                    });
                }
            }
        });
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void handleIncomingCecCommand(int srcAddress, int dstAddress, byte[] body) {
        assertRunOnServiceThread();
        HdmiCecMessage command = HdmiCecMessageBuilder.of(srcAddress, dstAddress, body);
        HdmiLogger.debug("[R]:" + command, new Object[0]);
        addMessageToHistory(true, command);
        onReceiveCommand(command);
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void handleHotplug(int port, boolean connected) {
        assertRunOnServiceThread();
        HdmiLogger.debug("Hotplug event:[port:%d, connected:%b]", Integer.valueOf(port), Boolean.valueOf(connected));
        this.mService.onHotplug(port, connected);
    }

    @HdmiAnnotations.ServiceThreadOnly
    private void addMessageToHistory(boolean isReceived, HdmiCecMessage message) {
        assertRunOnServiceThread();
        MessageHistoryRecord record = new MessageHistoryRecord(isReceived, message);
        if (!this.mMessageHistory.offer(record)) {
            this.mMessageHistory.poll();
            this.mMessageHistory.offer(record);
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(IndentingPrintWriter pw) {
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            pw.println("HdmiCecLocalDevice #" + this.mLocalDevices.keyAt(i) + ":");
            pw.increaseIndent();
            this.mLocalDevices.valueAt(i).dump(pw);
            pw.decreaseIndent();
        }
        pw.println("CEC message history:");
        pw.increaseIndent();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Iterator<MessageHistoryRecord> it = this.mMessageHistory.iterator();
        while (it.hasNext()) {
            it.next().dump(pw, sdf);
        }
        pw.decreaseIndent();
    }

    /* access modifiers changed from: private */
    public static final class NativeWrapperImpl implements NativeWrapper {
        private NativeWrapperImpl() {
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public long nativeInit(HdmiCecController handler, MessageQueue messageQueue) {
            return HdmiCecController.nativeInit(handler, messageQueue);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public int nativeSendCecCommand(long controllerPtr, int srcAddress, int dstAddress, byte[] body) {
            return HdmiCecController.nativeSendCecCommand(controllerPtr, srcAddress, dstAddress, body);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public int nativeAddLogicalAddress(long controllerPtr, int logicalAddress) {
            return HdmiCecController.nativeAddLogicalAddress(controllerPtr, logicalAddress);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public void nativeClearLogicalAddress(long controllerPtr) {
            HdmiCecController.nativeClearLogicalAddress(controllerPtr);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public int nativeGetPhysicalAddress(long controllerPtr) {
            return HdmiCecController.nativeGetPhysicalAddress(controllerPtr);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public int nativeGetVersion(long controllerPtr) {
            return HdmiCecController.nativeGetVersion(controllerPtr);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public int nativeGetVendorId(long controllerPtr) {
            return HdmiCecController.nativeGetVendorId(controllerPtr);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public HdmiPortInfo[] nativeGetPortInfos(long controllerPtr) {
            return HdmiCecController.nativeGetPortInfos(controllerPtr);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public void nativeSetOption(long controllerPtr, int flag, boolean enabled) {
            HdmiCecController.nativeSetOption(controllerPtr, flag, enabled);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public void nativeSetLanguage(long controllerPtr, String language) {
            HdmiCecController.nativeSetLanguage(controllerPtr, language);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public void nativeEnableAudioReturnChannel(long controllerPtr, int port, boolean flag) {
            HdmiCecController.nativeEnableAudioReturnChannel(controllerPtr, port, flag);
        }

        @Override // com.android.server.hdmi.HdmiCecController.NativeWrapper
        public boolean nativeIsConnected(long controllerPtr, int port) {
            return HdmiCecController.nativeIsConnected(controllerPtr, port);
        }
    }

    /* access modifiers changed from: private */
    public final class MessageHistoryRecord {
        private final boolean mIsReceived;
        private final HdmiCecMessage mMessage;
        private final long mTime = System.currentTimeMillis();

        public MessageHistoryRecord(boolean isReceived, HdmiCecMessage message) {
            this.mIsReceived = isReceived;
            this.mMessage = message;
        }

        /* access modifiers changed from: package-private */
        public void dump(IndentingPrintWriter pw, SimpleDateFormat sdf) {
            pw.print(this.mIsReceived ? "[R]" : "[S]");
            pw.print(" time=");
            pw.print(sdf.format(new Date(this.mTime)));
            pw.print(" message=");
            pw.println(this.mMessage);
        }
    }
}
