package com.android.server.usb;

import android.content.Context;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceServer;
import android.media.midi.MidiDeviceServer.Callback;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.util.Log;
import com.android.internal.midi.MidiEventScheduler;
import com.android.internal.midi.MidiEventScheduler.MidiEvent;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import libcore.io.IoUtils;

public final class UsbMidiDevice implements Closeable {
    private static final int BUFFER_SIZE = 512;
    private static final String TAG = "UsbMidiDevice";
    private final int mAlsaCard;
    private final int mAlsaDevice;
    private final Callback mCallback;
    private MidiEventScheduler[] mEventSchedulers;
    private FileDescriptor[] mFileDescriptors;
    private final InputReceiverProxy[] mInputPortReceivers;
    private FileInputStream[] mInputStreams;
    private boolean mIsOpen;
    private final Object mLock;
    private FileOutputStream[] mOutputStreams;
    private int mPipeFD;
    private StructPollfd[] mPollFDs;
    private MidiDeviceServer mServer;
    private final int mSubdeviceCount;

    /* renamed from: com.android.server.usb.UsbMidiDevice.2 */
    class AnonymousClass2 extends Thread {
        final /* synthetic */ MidiReceiver[] val$outputReceivers;

        AnonymousClass2(String $anonymous0, MidiReceiver[] val$outputReceivers) {
            this.val$outputReceivers = val$outputReceivers;
            super($anonymous0);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            byte[] buffer = new byte[UsbMidiDevice.BUFFER_SIZE];
            loop0:
            while (true) {
                try {
                    long timestamp = System.nanoTime();
                    synchronized (UsbMidiDevice.this.mLock) {
                        if (!UsbMidiDevice.this.mIsOpen) {
                            break;
                        }
                        int index = 0;
                        while (true) {
                            if (index < UsbMidiDevice.this.mPollFDs.length) {
                                StructPollfd pfd = UsbMidiDevice.this.mPollFDs[index];
                                if ((pfd.revents & (OsConstants.POLLERR | OsConstants.POLLHUP)) == 0) {
                                    if ((pfd.revents & OsConstants.POLLIN) != 0) {
                                        pfd.revents = (short) 0;
                                        if (index != UsbMidiDevice.this.mInputStreams.length - 1) {
                                            this.val$outputReceivers[index].send(buffer, 0, UsbMidiDevice.this.mInputStreams[index].read(buffer), timestamp);
                                        }
                                    }
                                    index++;
                                }
                            }
                            break;
                        }
                        Os.poll(UsbMidiDevice.this.mPollFDs, -1);
                    }
                } catch (IOException e) {
                    Log.d(UsbMidiDevice.TAG, "reader thread exiting");
                } catch (ErrnoException e2) {
                    Log.d(UsbMidiDevice.TAG, "reader thread exiting");
                }
            }
        }
    }

    /* renamed from: com.android.server.usb.UsbMidiDevice.3 */
    class AnonymousClass3 extends Thread {
        final /* synthetic */ MidiEventScheduler val$eventSchedulerF;
        final /* synthetic */ FileOutputStream val$outputStreamF;
        final /* synthetic */ int val$portF;

        AnonymousClass3(String $anonymous0, MidiEventScheduler val$eventSchedulerF, FileOutputStream val$outputStreamF, int val$portF) {
            this.val$eventSchedulerF = val$eventSchedulerF;
            this.val$outputStreamF = val$outputStreamF;
            this.val$portF = val$portF;
            super($anonymous0);
        }

        public void run() {
            while (true) {
                try {
                    MidiEvent event = (MidiEvent) this.val$eventSchedulerF.waitNextEvent();
                    if (event == null) {
                        Log.d(UsbMidiDevice.TAG, "output thread exit");
                        return;
                    }
                    try {
                        this.val$outputStreamF.write(event.data, 0, event.count);
                    } catch (IOException e) {
                        Log.e(UsbMidiDevice.TAG, "write failed for port " + this.val$portF);
                    }
                    this.val$eventSchedulerF.addEventToPool(event);
                } catch (InterruptedException e2) {
                }
            }
        }
    }

    private final class InputReceiverProxy extends MidiReceiver {
        private MidiReceiver mReceiver;

        private InputReceiverProxy() {
        }

        public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
            MidiReceiver receiver = this.mReceiver;
            if (receiver != null) {
                receiver.send(msg, offset, count, timestamp);
            }
        }

        public void setReceiver(MidiReceiver receiver) {
            this.mReceiver = receiver;
        }

        public void onFlush() throws IOException {
            MidiReceiver receiver = this.mReceiver;
            if (receiver != null) {
                receiver.flush();
            }
        }
    }

    private native void nativeClose(FileDescriptor[] fileDescriptorArr);

    private static native int nativeGetSubdeviceCount(int i, int i2);

    private native FileDescriptor[] nativeOpen(int i, int i2, int i3);

    public static UsbMidiDevice create(Context context, Bundle properties, int card, int device) {
        int subDeviceCount = nativeGetSubdeviceCount(card, device);
        if (subDeviceCount <= 0) {
            Log.e(TAG, "nativeGetSubdeviceCount failed");
            return null;
        }
        UsbMidiDevice midiDevice = new UsbMidiDevice(card, device, subDeviceCount);
        if (midiDevice.register(context, properties)) {
            return midiDevice;
        }
        IoUtils.closeQuietly(midiDevice);
        Log.e(TAG, "createDeviceServer failed");
        return null;
    }

    private UsbMidiDevice(int card, int device, int subdeviceCount) {
        this.mLock = new Object();
        this.mPipeFD = -1;
        this.mCallback = new Callback() {
            public void onDeviceStatusChanged(MidiDeviceServer server, MidiDeviceStatus status) {
                int i;
                MidiDeviceInfo deviceInfo = status.getDeviceInfo();
                int inputPorts = deviceInfo.getInputPortCount();
                int outputPorts = deviceInfo.getOutputPortCount();
                boolean hasOpenPorts = false;
                for (i = 0; i < inputPorts; i++) {
                    if (status.isInputPortOpen(i)) {
                        hasOpenPorts = true;
                        break;
                    }
                }
                if (!hasOpenPorts) {
                    for (i = 0; i < outputPorts; i++) {
                        if (status.getOutputPortOpenCount(i) > 0) {
                            hasOpenPorts = true;
                            break;
                        }
                    }
                }
                synchronized (UsbMidiDevice.this.mLock) {
                    if (hasOpenPorts) {
                        if (!UsbMidiDevice.this.mIsOpen) {
                            UsbMidiDevice.this.openLocked();
                        }
                    }
                    if (!hasOpenPorts && UsbMidiDevice.this.mIsOpen) {
                        UsbMidiDevice.this.closeLocked();
                    }
                }
            }

            public void onClose() {
            }
        };
        this.mAlsaCard = card;
        this.mAlsaDevice = device;
        this.mSubdeviceCount = subdeviceCount;
        int inputCount = subdeviceCount;
        this.mInputPortReceivers = new InputReceiverProxy[subdeviceCount];
        for (int port = 0; port < subdeviceCount; port++) {
            this.mInputPortReceivers[port] = new InputReceiverProxy();
        }
    }

    private boolean openLocked() {
        FileDescriptor[] fileDescriptors = nativeOpen(this.mAlsaCard, this.mAlsaDevice, this.mSubdeviceCount);
        if (fileDescriptors == null) {
            Log.e(TAG, "nativeOpen failed");
            return false;
        }
        int i;
        this.mFileDescriptors = fileDescriptors;
        int inputCount = fileDescriptors.length;
        int outputCount = fileDescriptors.length - 1;
        this.mPollFDs = new StructPollfd[inputCount];
        this.mInputStreams = new FileInputStream[inputCount];
        for (i = 0; i < inputCount; i++) {
            FileDescriptor fd = fileDescriptors[i];
            StructPollfd pollfd = new StructPollfd();
            pollfd.fd = fd;
            pollfd.events = (short) OsConstants.POLLIN;
            this.mPollFDs[i] = pollfd;
            this.mInputStreams[i] = new FileInputStream(fd);
        }
        this.mOutputStreams = new FileOutputStream[outputCount];
        this.mEventSchedulers = new MidiEventScheduler[outputCount];
        for (i = 0; i < outputCount; i++) {
            this.mOutputStreams[i] = new FileOutputStream(fileDescriptors[i]);
            MidiEventScheduler scheduler = new MidiEventScheduler();
            this.mEventSchedulers[i] = scheduler;
            this.mInputPortReceivers[i].setReceiver(scheduler.getReceiver());
        }
        new AnonymousClass2("UsbMidiDevice input thread", this.mServer.getOutputPortReceivers()).start();
        for (int port = 0; port < outputCount; port++) {
            MidiEventScheduler eventSchedulerF = this.mEventSchedulers[port];
            FileOutputStream outputStreamF = this.mOutputStreams[port];
            int portF = port;
            new AnonymousClass3("UsbMidiDevice output thread " + port, eventSchedulerF, outputStreamF, portF).start();
        }
        this.mIsOpen = true;
        return true;
    }

    private boolean register(Context context, Bundle properties) {
        MidiManager midiManager = (MidiManager) context.getSystemService("midi");
        if (midiManager == null) {
            Log.e(TAG, "No MidiManager in UsbMidiDevice.create()");
            return false;
        }
        this.mServer = midiManager.createDeviceServer(this.mInputPortReceivers, this.mSubdeviceCount, null, null, properties, 1, this.mCallback);
        return this.mServer != null;
    }

    public void close() throws IOException {
        synchronized (this.mLock) {
            if (this.mIsOpen) {
                closeLocked();
            }
        }
        if (this.mServer != null) {
            IoUtils.closeQuietly(this.mServer);
        }
    }

    private void closeLocked() {
        for (int i = 0; i < this.mEventSchedulers.length; i++) {
            this.mInputPortReceivers[i].setReceiver(null);
            this.mEventSchedulers[i].close();
        }
        this.mEventSchedulers = null;
        for (AutoCloseable closeQuietly : this.mInputStreams) {
            IoUtils.closeQuietly(closeQuietly);
        }
        this.mInputStreams = null;
        for (AutoCloseable closeQuietly2 : this.mOutputStreams) {
            IoUtils.closeQuietly(closeQuietly2);
        }
        this.mOutputStreams = null;
        nativeClose(this.mFileDescriptors);
        this.mFileDescriptors = null;
        this.mIsOpen = false;
    }
}
