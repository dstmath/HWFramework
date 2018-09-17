package com.android.internal.midi;

import android.media.midi.MidiReceiver;
import android.media.midi.MidiSender;
import java.io.IOException;
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
        this.mReceivers = new CopyOnWriteArrayList();
        this.mSender = new MidiSender() {
            public void onConnect(MidiReceiver receiver) {
                MidiDispatcher.this.mReceivers.add(receiver);
            }

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

    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
        for (MidiReceiver receiver : this.mReceivers) {
            try {
                receiver.send(msg, offset, count, timestamp);
            } catch (IOException e) {
                this.mReceivers.remove(receiver);
                if (this.mFailureHandler != null) {
                    this.mFailureHandler.onReceiverFailure(receiver, e);
                }
            }
        }
    }

    public void onFlush() throws IOException {
        for (MidiReceiver receiver : this.mReceivers) {
            try {
                receiver.flush();
            } catch (IOException e) {
                this.mReceivers.remove(receiver);
                if (this.mFailureHandler != null) {
                    this.mFailureHandler.onReceiverFailure(receiver, e);
                }
            }
        }
    }
}
