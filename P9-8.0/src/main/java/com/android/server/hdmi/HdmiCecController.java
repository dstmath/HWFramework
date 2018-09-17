package com.android.server.hdmi;

import android.hardware.hdmi.HdmiPortInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.hdmi.HdmiAnnotations.IoThreadOnly;
import com.android.server.hdmi.HdmiAnnotations.ServiceThreadOnly;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Predicate;
import libcore.util.EmptyArray;
import sun.util.locale.LanguageTag;

final class HdmiCecController {
    private static final byte[] EMPTY_BODY = EmptyArray.BYTE;
    private static final int MAX_CEC_MESSAGE_HISTORY = 20;
    private static final int NUM_LOGICAL_ADDRESS = 16;
    private static final String TAG = "HdmiCecController";
    private Handler mControlHandler;
    private Handler mIoHandler;
    private final SparseArray<HdmiCecLocalDevice> mLocalDevices = new SparseArray();
    private final ArrayBlockingQueue<MessageHistoryRecord> mMessageHistory = new ArrayBlockingQueue(20);
    private volatile long mNativePtr;
    private final Predicate<Integer> mRemoteDeviceAddressPredicate = new Predicate<Integer>() {
        public boolean test(Integer address) {
            return HdmiCecController.this.isAllocatedLocalDeviceAddress(address.intValue()) ^ 1;
        }
    };
    private final HdmiControlService mService;
    private final Predicate<Integer> mSystemAudioAddressPredicate = new Predicate<Integer>() {
        public boolean test(Integer address) {
            return HdmiUtils.getTypeFromAddress(address.intValue()) == 5;
        }
    };

    interface AllocateAddressCallback {
        void onAllocated(int i, int i2);
    }

    private final class MessageHistoryRecord {
        private final boolean mIsReceived;
        private final HdmiCecMessage mMessage;
        private final long mTime = System.currentTimeMillis();

        public MessageHistoryRecord(boolean isReceived, HdmiCecMessage message) {
            this.mIsReceived = isReceived;
            this.mMessage = message;
        }

        void dump(IndentingPrintWriter pw, SimpleDateFormat sdf) {
            pw.print(this.mIsReceived ? "[R]" : "[S]");
            pw.print(" time=");
            pw.print(sdf.format(new Date(this.mTime)));
            pw.print(" message=");
            pw.println(this.mMessage);
        }
    }

    private static native int nativeAddLogicalAddress(long j, int i);

    private static native void nativeClearLogicalAddress(long j);

    private static native void nativeEnableAudioReturnChannel(long j, int i, boolean z);

    private static native int nativeGetPhysicalAddress(long j);

    private static native HdmiPortInfo[] nativeGetPortInfos(long j);

    private static native int nativeGetVendorId(long j);

    private static native int nativeGetVersion(long j);

    private static native long nativeInit(HdmiCecController hdmiCecController, MessageQueue messageQueue);

    private static native boolean nativeIsConnected(long j, int i);

    private static native int nativeSendCecCommand(long j, int i, int i2, byte[] bArr);

    private static native void nativeSetLanguage(long j, String str);

    private static native void nativeSetOption(long j, int i, boolean z);

    private HdmiCecController(HdmiControlService service) {
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
    void allocateLogicalAddress(final int deviceType, final int preferredAddress, final AllocateAddressCallback callback) {
        assertRunOnServiceThread();
        runOnIoThread(new Runnable() {
            public void run() {
                HdmiCecController.this.handleAllocateLogicalAddress(deviceType, preferredAddress, callback);
            }
        });
    }

    @IoThreadOnly
    private void handleAllocateLogicalAddress(final int deviceType, int preferredAddress, final AllocateAddressCallback callback) {
        int i;
        assertRunOnIoThread();
        int startAddress = preferredAddress;
        if (preferredAddress == 15) {
            for (i = 0; i < 16; i++) {
                if (deviceType == HdmiUtils.getTypeFromAddress(i)) {
                    startAddress = i;
                    break;
                }
            }
        }
        int logicalAddress = 15;
        for (i = 0; i < 16; i++) {
            int curAddress = (startAddress + i) % 16;
            if (curAddress != 15 && deviceType == HdmiUtils.getTypeFromAddress(curAddress)) {
                boolean acked = false;
                for (int j = 0; j < 3; j++) {
                    if (sendPollMessage(curAddress, curAddress, 1)) {
                        acked = true;
                        break;
                    }
                }
                if (!acked) {
                    logicalAddress = curAddress;
                    break;
                }
            }
        }
        final int assignedAddress = logicalAddress;
        HdmiLogger.debug("New logical address for device [%d]: [preferred:%d, assigned:%d]", Integer.valueOf(deviceType), Integer.valueOf(preferredAddress), Integer.valueOf(assignedAddress));
        if (callback != null) {
            runOnServiceThread(new Runnable() {
                public void run() {
                    callback.onAllocated(deviceType, assignedAddress);
                }
            });
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
        return 2;
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
    void setOption(int flag, boolean enabled) {
        assertRunOnServiceThread();
        HdmiLogger.debug("setOption: [flag:%d, enabled:%b]", Integer.valueOf(flag), Boolean.valueOf(enabled));
        nativeSetOption(this.mNativePtr, flag, enabled);
    }

    @ServiceThreadOnly
    void setLanguage(String language) {
        assertRunOnServiceThread();
        if (LanguageTag.isLanguage(language)) {
            nativeSetLanguage(this.mNativePtr, language);
        }
    }

    @ServiceThreadOnly
    void enableAudioReturnChannel(int port, boolean enabled) {
        assertRunOnServiceThread();
        nativeEnableAudioReturnChannel(this.mNativePtr, port, enabled);
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
            case 2:
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
            case 65536:
                for (i = 0; i <= 14; i++) {
                    if (pickPredicate.test(Integer.valueOf(i))) {
                        pollingCandidates.add(Integer.valueOf(i));
                    }
                }
                break;
            default:
                for (i = 14; i >= 0; i--) {
                    if (pickPredicate.test(Integer.valueOf(i))) {
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
        final Integer candidate = (Integer) candidates.remove(0);
        final int i = sourceAddress;
        final int i2 = retryCount;
        final List<Integer> list = allocated;
        final List<Integer> list2 = candidates;
        final DevicePollingCallback devicePollingCallback = callback;
        runOnIoThread(new Runnable() {
            public void run() {
                if (HdmiCecController.this.sendPollMessage(i, candidate.intValue(), i2)) {
                    list.add(candidate);
                }
                HdmiCecController hdmiCecController = HdmiCecController.this;
                final int i = i;
                final List list = list2;
                final int i2 = i2;
                final DevicePollingCallback devicePollingCallback = devicePollingCallback;
                final List list2 = list;
                hdmiCecController.runOnServiceThread(new Runnable() {
                    public void run() {
                        HdmiCecController.this.runDevicePolling(i, list, i2, devicePollingCallback, list2);
                    }
                });
            }
        });
    }

    @IoThreadOnly
    private boolean sendPollMessage(int sourceAddress, int destinationAddress, int retryCount) {
        assertRunOnIoThread();
        for (int i = 0; i < retryCount; i++) {
            int ret = nativeSendCecCommand(this.mNativePtr, sourceAddress, destinationAddress, EMPTY_BODY);
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

    private void runOnServiceThread(Runnable runnable) {
        this.mControlHandler.post(runnable);
    }

    @ServiceThreadOnly
    void flush(final Runnable runnable) {
        assertRunOnServiceThread();
        runOnIoThread(new Runnable() {
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
    void sendCommand(final HdmiCecMessage cecMessage, final SendMessageCallback callback) {
        assertRunOnServiceThread();
        addMessageToHistory(false, cecMessage);
        runOnIoThread(new Runnable() {
            public void run() {
                final int errorCode;
                HdmiLogger.debug("[S]:" + cecMessage, new Object[0]);
                byte[] body = HdmiCecController.buildBody(cecMessage.getOpcode(), cecMessage.getParams());
                int i = 0;
                while (true) {
                    errorCode = HdmiCecController.nativeSendCecCommand(HdmiCecController.this.mNativePtr, cecMessage.getSource(), cecMessage.getDestination(), body);
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
                    Slog.w(HdmiCecController.TAG, "Failed to send " + cecMessage + " with errorCode=" + errorCode);
                }
                if (callback != null) {
                    HdmiCecController hdmiCecController = HdmiCecController.this;
                    final SendMessageCallback sendMessageCallback = callback;
                    hdmiCecController.runOnServiceThread(new Runnable() {
                        public void run() {
                            sendMessageCallback.onSendCompleted(errorCode);
                        }
                    });
                }
            }
        });
    }

    @ServiceThreadOnly
    private void handleIncomingCecCommand(int srcAddress, int dstAddress, byte[] body) {
        assertRunOnServiceThread();
        HdmiCecMessage command = HdmiCecMessageBuilder.of(srcAddress, dstAddress, body);
        HdmiLogger.debug("[R]:" + command, new Object[0]);
        addMessageToHistory(true, command);
        onReceiveCommand(command);
    }

    @ServiceThreadOnly
    private void handleHotplug(int port, boolean connected) {
        assertRunOnServiceThread();
        HdmiLogger.debug("Hotplug event:[port:%d, connected:%b]", Integer.valueOf(port), Boolean.valueOf(connected));
        this.mService.onHotplug(port, connected);
    }

    @ServiceThreadOnly
    private void addMessageToHistory(boolean isReceived, HdmiCecMessage message) {
        assertRunOnServiceThread();
        MessageHistoryRecord record = new MessageHistoryRecord(isReceived, message);
        if (!this.mMessageHistory.offer(record)) {
            this.mMessageHistory.poll();
            this.mMessageHistory.offer(record);
        }
    }

    void dump(IndentingPrintWriter pw) {
        for (int i = 0; i < this.mLocalDevices.size(); i++) {
            pw.println("HdmiCecLocalDevice #" + i + ":");
            pw.increaseIndent();
            ((HdmiCecLocalDevice) this.mLocalDevices.valueAt(i)).dump(pw);
            pw.decreaseIndent();
        }
        pw.println("CEC message history:");
        pw.increaseIndent();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (MessageHistoryRecord record : this.mMessageHistory) {
            record.dump(pw, sdf);
        }
        pw.decreaseIndent();
    }
}
