package com.android.server.usb;

import android.content.Context;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceServer;
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
import com.android.internal.util.dump.DualDumpOutputStream;
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
    private final MidiDeviceServer.Callback mCallback = new MidiDeviceServer.Callback() {
        public void onDeviceStatusChanged(MidiDeviceServer server, MidiDeviceStatus status) {
            MidiDeviceInfo deviceInfo = status.getDeviceInfo();
            int inputPorts = deviceInfo.getInputPortCount();
            int outputPorts = deviceInfo.getOutputPortCount();
            boolean hasOpenPorts = false;
            int i = 0;
            int i2 = 0;
            while (true) {
                if (i2 >= inputPorts) {
                    break;
                } else if (status.isInputPortOpen(i2)) {
                    hasOpenPorts = true;
                    break;
                } else {
                    i2++;
                }
            }
            if (!hasOpenPorts) {
                while (true) {
                    if (i >= outputPorts) {
                        break;
                    } else if (status.getOutputPortOpenCount(i) > 0) {
                        hasOpenPorts = true;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            synchronized (UsbMidiDevice.this.mLock) {
                if (hasOpenPorts) {
                    try {
                        if (!UsbMidiDevice.this.mIsOpen) {
                            boolean unused = UsbMidiDevice.this.openLocked();
                        }
                    } catch (Throwable th) {
                        throw th;
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
    private MidiEventScheduler[] mEventSchedulers;
    private FileDescriptor[] mFileDescriptors;
    private final InputReceiverProxy[] mInputPortReceivers;
    /* access modifiers changed from: private */
    public FileInputStream[] mInputStreams;
    /* access modifiers changed from: private */
    public boolean mIsOpen;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private FileOutputStream[] mOutputStreams;
    private int mPipeFD = -1;
    /* access modifiers changed from: private */
    public StructPollfd[] mPollFDs;
    private MidiDeviceServer mServer;
    private final int mSubdeviceCount;

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
        this.mAlsaCard = card;
        this.mAlsaDevice = device;
        this.mSubdeviceCount = subdeviceCount;
        int inputPortCount = subdeviceCount;
        this.mInputPortReceivers = new InputReceiverProxy[inputPortCount];
        for (int port = 0; port < inputPortCount; port++) {
            this.mInputPortReceivers[port] = new InputReceiverProxy();
        }
    }

    /* access modifiers changed from: private */
    public boolean openLocked() {
        FileDescriptor[] fileDescriptors = nativeOpen(this.mAlsaCard, this.mAlsaDevice, this.mSubdeviceCount);
        if (fileDescriptors == null) {
            Log.e(TAG, "nativeOpen failed");
            return false;
        }
        this.mFileDescriptors = fileDescriptors;
        int inputStreamCount = fileDescriptors.length;
        int outputStreamCount = fileDescriptors.length - 1;
        this.mPollFDs = new StructPollfd[inputStreamCount];
        this.mInputStreams = new FileInputStream[inputStreamCount];
        for (int i = 0; i < inputStreamCount; i++) {
            FileDescriptor fd = fileDescriptors[i];
            StructPollfd pollfd = new StructPollfd();
            pollfd.fd = fd;
            pollfd.events = (short) OsConstants.POLLIN;
            this.mPollFDs[i] = pollfd;
            this.mInputStreams[i] = new FileInputStream(fd);
        }
        this.mOutputStreams = new FileOutputStream[outputStreamCount];
        this.mEventSchedulers = new MidiEventScheduler[outputStreamCount];
        for (int i2 = 0; i2 < outputStreamCount; i2++) {
            this.mOutputStreams[i2] = new FileOutputStream(fileDescriptors[i2]);
            MidiEventScheduler scheduler = new MidiEventScheduler();
            this.mEventSchedulers[i2] = scheduler;
            this.mInputPortReceivers[i2].setReceiver(scheduler.getReceiver());
        }
        final MidiReceiver[] outputReceivers = this.mServer.getOutputPortReceivers();
        new Thread("UsbMidiDevice input thread") {
            public void run() {
                byte[] buffer = new byte[512];
                while (true) {
                    try {
                        long timestamp = System.nanoTime();
                        synchronized (UsbMidiDevice.this.mLock) {
                            if (UsbMidiDevice.this.mIsOpen) {
                                int index = 0;
                                while (true) {
                                    int index2 = index;
                                    if (index2 >= UsbMidiDevice.this.mPollFDs.length) {
                                        break;
                                    }
                                    StructPollfd pfd = UsbMidiDevice.this.mPollFDs[index2];
                                    if ((pfd.revents & (OsConstants.POLLERR | OsConstants.POLLHUP)) != 0) {
                                        break;
                                    }
                                    if ((pfd.revents & OsConstants.POLLIN) != 0) {
                                        pfd.revents = 0;
                                        if (index2 == UsbMidiDevice.this.mInputStreams.length - 1) {
                                            break;
                                        }
                                        outputReceivers[index2].send(buffer, 0, UsbMidiDevice.this.mInputStreams[index2].read(buffer), timestamp);
                                    }
                                    index = index2 + 1;
                                }
                            }
                        }
                        Os.poll(UsbMidiDevice.this.mPollFDs, -1);
                    } catch (IOException e) {
                        Log.d(UsbMidiDevice.TAG, "reader thread exiting");
                    } catch (ErrnoException e2) {
                        Log.d(UsbMidiDevice.TAG, "reader thread exiting");
                    }
                }
                Log.d(UsbMidiDevice.TAG, "input thread exit");
            }
        }.start();
        for (int port = 0; port < outputStreamCount; port++) {
            MidiEventScheduler eventSchedulerF = this.mEventSchedulers[port];
            final int portF = port;
            final MidiEventScheduler midiEventScheduler = eventSchedulerF;
            final FileOutputStream fileOutputStream = this.mOutputStreams[port];
            AnonymousClass3 r6 = new Thread("UsbMidiDevice output thread " + port) {
                public void run() {
                    while (true) {
                        try {
                            MidiEventScheduler.MidiEvent event = midiEventScheduler.waitNextEvent();
                            if (event == null) {
                                Log.d(UsbMidiDevice.TAG, "output thread exit");
                                return;
                            }
                            try {
                                fileOutputStream.write(event.data, 0, event.count);
                            } catch (IOException e) {
                                Log.e(UsbMidiDevice.TAG, "write failed for port " + portF);
                            }
                            midiEventScheduler.addEventToPool(event);
                        } catch (InterruptedException e2) {
                        }
                    }
                }
            };
            r6.start();
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
        if (this.mServer == null) {
            return false;
        }
        return true;
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

    /* access modifiers changed from: private */
    public void closeLocked() {
        for (int i = 0; i < this.mEventSchedulers.length; i++) {
            this.mInputPortReceivers[i].setReceiver(null);
            this.mEventSchedulers[i].close();
        }
        this.mEventSchedulers = null;
        for (FileInputStream closeQuietly : this.mInputStreams) {
            IoUtils.closeQuietly(closeQuietly);
        }
        this.mInputStreams = null;
        for (FileOutputStream closeQuietly2 : this.mOutputStreams) {
            IoUtils.closeQuietly(closeQuietly2);
        }
        this.mOutputStreams = null;
        nativeClose(this.mFileDescriptors);
        this.mFileDescriptors = null;
        this.mIsOpen = false;
    }

    public void dump(String deviceAddr, DualDumpOutputStream dump, String idName, long id) {
        long token = dump.start(idName, id);
        dump.write("device_address", 1138166333443L, deviceAddr);
        dump.write("card", 1120986464257L, this.mAlsaCard);
        dump.write("device", 1120986464258L, this.mAlsaDevice);
        dump.end(token);
    }
}
