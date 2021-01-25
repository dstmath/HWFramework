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
        /* class com.android.server.usb.UsbMidiDevice.AnonymousClass1 */

        public void onDeviceStatusChanged(MidiDeviceServer server, MidiDeviceStatus status) {
            MidiDeviceInfo deviceInfo = status.getDeviceInfo();
            int inputPorts = deviceInfo.getInputPortCount();
            int outputPorts = deviceInfo.getOutputPortCount();
            boolean hasOpenPorts = false;
            int i = 0;
            while (true) {
                if (i >= inputPorts) {
                    break;
                } else if (status.isInputPortOpen(i)) {
                    hasOpenPorts = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!hasOpenPorts) {
                int i2 = 0;
                while (true) {
                    if (i2 >= outputPorts) {
                        break;
                    } else if (status.getOutputPortOpenCount(i2) > 0) {
                        hasOpenPorts = true;
                        break;
                    } else {
                        i2++;
                    }
                }
            }
            synchronized (UsbMidiDevice.this.mLock) {
                if (hasOpenPorts) {
                    try {
                        if (!UsbMidiDevice.this.mIsOpen) {
                            UsbMidiDevice.this.openLocked();
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
    private FileInputStream[] mInputStreams;
    private boolean mIsOpen;
    private final Object mLock = new Object();
    private FileOutputStream[] mOutputStreams;
    private int mPipeFD = -1;
    private StructPollfd[] mPollFDs;
    private MidiDeviceServer mServer;
    private final int mSubdeviceCount;

    private native void nativeClose(FileDescriptor[] fileDescriptorArr);

    private static native int nativeGetSubdeviceCount(int i, int i2);

    private native FileDescriptor[] nativeOpen(int i, int i2, int i3);

    /* access modifiers changed from: private */
    public final class InputReceiverProxy extends MidiReceiver {
        private MidiReceiver mReceiver;

        private InputReceiverProxy() {
        }

        @Override // android.media.midi.MidiReceiver
        public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
            MidiReceiver receiver = this.mReceiver;
            if (receiver != null) {
                receiver.send(msg, offset, count, timestamp);
            }
        }

        public void setReceiver(MidiReceiver receiver) {
            this.mReceiver = receiver;
        }

        @Override // android.media.midi.MidiReceiver
        public void onFlush() throws IOException {
            MidiReceiver receiver = this.mReceiver;
            if (receiver != null) {
                receiver.flush();
            }
        }
    }

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
        this.mInputPortReceivers = new InputReceiverProxy[subdeviceCount];
        for (int port = 0; port < subdeviceCount; port++) {
            this.mInputPortReceivers[port] = new InputReceiverProxy();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean openLocked() {
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
            /* class com.android.server.usb.UsbMidiDevice.AnonymousClass2 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                byte[] buffer = new byte[512];
                while (true) {
                    try {
                        long timestamp = System.nanoTime();
                        synchronized (UsbMidiDevice.this.mLock) {
                            if (UsbMidiDevice.this.mIsOpen) {
                                int index = 0;
                                while (true) {
                                    if (index >= UsbMidiDevice.this.mPollFDs.length) {
                                        break;
                                    }
                                    StructPollfd pfd = UsbMidiDevice.this.mPollFDs[index];
                                    if ((pfd.revents & (OsConstants.POLLERR | OsConstants.POLLHUP)) != 0) {
                                        break;
                                    }
                                    if ((pfd.revents & OsConstants.POLLIN) != 0) {
                                        pfd.revents = 0;
                                        if (index == UsbMidiDevice.this.mInputStreams.length - 1) {
                                            break;
                                        }
                                        outputReceivers[index].send(buffer, 0, UsbMidiDevice.this.mInputStreams[index].read(buffer), timestamp);
                                    }
                                    index++;
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
        for (final int port = 0; port < outputStreamCount; port++) {
            final MidiEventScheduler eventSchedulerF = this.mEventSchedulers[port];
            final FileOutputStream outputStreamF = this.mOutputStreams[port];
            new Thread("UsbMidiDevice output thread " + port) {
                /* class com.android.server.usb.UsbMidiDevice.AnonymousClass3 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    while (true) {
                        try {
                            MidiEventScheduler.MidiEvent event = eventSchedulerF.waitNextEvent();
                            if (event == null) {
                                Log.d(UsbMidiDevice.TAG, "output thread exit");
                                return;
                            }
                            try {
                                outputStreamF.write(event.data, 0, event.count);
                            } catch (IOException e) {
                                Log.e(UsbMidiDevice.TAG, "write failed for port " + port);
                            }
                            eventSchedulerF.addEventToPool(event);
                        } catch (InterruptedException e2) {
                        }
                    }
                }
            }.start();
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

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        synchronized (this.mLock) {
            if (this.mIsOpen) {
                closeLocked();
            }
        }
        MidiDeviceServer midiDeviceServer = this.mServer;
        if (midiDeviceServer != null) {
            IoUtils.closeQuietly(midiDeviceServer);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void closeLocked() {
        for (int i = 0; i < this.mEventSchedulers.length; i++) {
            this.mInputPortReceivers[i].setReceiver(null);
            this.mEventSchedulers[i].close();
        }
        this.mEventSchedulers = null;
        int i2 = 0;
        while (true) {
            FileInputStream[] fileInputStreamArr = this.mInputStreams;
            if (i2 >= fileInputStreamArr.length) {
                break;
            }
            IoUtils.closeQuietly(fileInputStreamArr[i2]);
            i2++;
        }
        this.mInputStreams = null;
        int i3 = 0;
        while (true) {
            FileOutputStream[] fileOutputStreamArr = this.mOutputStreams;
            if (i3 < fileOutputStreamArr.length) {
                IoUtils.closeQuietly(fileOutputStreamArr[i3]);
                i3++;
            } else {
                this.mOutputStreams = null;
                nativeClose(this.mFileDescriptors);
                this.mFileDescriptors = null;
                this.mIsOpen = false;
                return;
            }
        }
    }

    public void dump(String deviceAddr, DualDumpOutputStream dump, String idName, long id) {
        long token = dump.start(idName, id);
        dump.write("device_address", 1138166333443L, deviceAddr);
        dump.write("card", 1120986464257L, this.mAlsaCard);
        dump.write("device", 1120986464258L, this.mAlsaDevice);
        dump.end(token);
    }
}
