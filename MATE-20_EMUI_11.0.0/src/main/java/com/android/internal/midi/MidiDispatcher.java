package com.android.internal.midi;

import android.media.midi.MidiReceiver;
import android.media.midi.MidiSender;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MidiDispatcher extends MidiReceiver {
    private final MidiReceiverFailureHandler mFailureHandler;
    private final CopyOnWriteArrayList<MidiReceiver> mReceivers;
    private final MidiSender mSender;

    public interface MidiReceiverFailureHandler {
        void onReceiverFailure(MidiReceiver midiReceiver, IOException iOException);
    }

    public MidiDispatcher() {
        this(null);
    }

    public MidiDispatcher(MidiReceiverFailureHandler failureHandler) {
        this.mReceivers = new CopyOnWriteArrayList<>();
        this.mSender = new MidiSender() {
            /* class com.android.internal.midi.MidiDispatcher.AnonymousClass1 */

            @Override // android.media.midi.MidiSender
            public void onConnect(MidiReceiver receiver) {
                MidiDispatcher.this.mReceivers.add(receiver);
            }

            @Override // android.media.midi.MidiSender
            public void onDisconnect(MidiReceiver receiver) {
                MidiDispatcher.this.mReceivers.remove(receiver);
            }
        };
        this.mFailureHandler = failureHandler;
    }

    public int getReceiverCount() {
        return this.mReceivers.size();
    }

    public MidiSender getSender() {
        return this.mSender;
    }

    @Override // android.media.midi.MidiReceiver
    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
        Iterator<MidiReceiver> it = this.mReceivers.iterator();
        while (it.hasNext()) {
            MidiReceiver receiver = it.next();
            try {
                receiver.send(msg, offset, count, timestamp);
            } catch (IOException e) {
                this.mReceivers.remove(receiver);
                MidiReceiverFailureHandler midiReceiverFailureHandler = this.mFailureHandler;
                if (midiReceiverFailureHandler != null) {
                    midiReceiverFailureHandler.onReceiverFailure(receiver, e);
                }
            }
        }
    }

    @Override // android.media.midi.MidiReceiver
    public void onFlush() throws IOException {
        Iterator<MidiReceiver> it = this.mReceivers.iterator();
        while (it.hasNext()) {
            MidiReceiver receiver = it.next();
            try {
                receiver.flush();
            } catch (IOException e) {
                this.mReceivers.remove(receiver);
                MidiReceiverFailureHandler midiReceiverFailureHandler = this.mFailureHandler;
                if (midiReceiverFailureHandler != null) {
                    midiReceiverFailureHandler.onReceiverFailure(receiver, e);
                }
            }
        }
    }
}
