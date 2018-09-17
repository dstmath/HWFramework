package com.android.internal.midi;

import android.media.midi.MidiReceiver;
import android.media.midi.MidiSender;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MidiDispatcher extends MidiReceiver {
    private final CopyOnWriteArrayList<MidiReceiver> mReceivers;
    private final MidiSender mSender;

    public MidiDispatcher() {
        this.mReceivers = new CopyOnWriteArrayList();
        this.mSender = new MidiSender() {
            public void onConnect(MidiReceiver receiver) {
                MidiDispatcher.this.mReceivers.add(receiver);
            }

            public void onDisconnect(MidiReceiver receiver) {
                MidiDispatcher.this.mReceivers.remove(receiver);
            }
        };
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
            }
        }
    }

    public void onFlush() throws IOException {
        for (MidiReceiver receiver : this.mReceivers) {
            receiver.flush();
        }
    }
}
