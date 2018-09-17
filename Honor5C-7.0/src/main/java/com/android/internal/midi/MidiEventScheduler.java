package com.android.internal.midi;

import android.media.midi.MidiReceiver;
import com.android.internal.midi.EventScheduler.SchedulableEvent;
import java.io.IOException;

public class MidiEventScheduler extends EventScheduler {
    private static final int POOL_EVENT_SIZE = 16;
    private static final String TAG = "MidiEventScheduler";
    private MidiReceiver mReceiver;

    public static class MidiEvent extends SchedulableEvent {
        public int count;
        public byte[] data;

        private MidiEvent(int count) {
            super(0);
            this.count = 0;
            this.data = new byte[count];
        }

        private MidiEvent(byte[] msg, int offset, int count, long timestamp) {
            super(timestamp);
            this.count = 0;
            this.data = new byte[count];
            System.arraycopy(msg, offset, this.data, 0, count);
            this.count = count;
        }

        public String toString() {
            String text = "Event: ";
            for (int i = 0; i < this.count; i++) {
                text = text + this.data[i] + ", ";
            }
            return text;
        }
    }

    private class SchedulingReceiver extends MidiReceiver {
        private SchedulingReceiver() {
        }

        public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
            MidiEvent event = MidiEventScheduler.this.createScheduledEvent(msg, offset, count, timestamp);
            if (event != null) {
                MidiEventScheduler.this.add(event);
            }
        }

        public void onFlush() {
            MidiEventScheduler.this.flush();
        }
    }

    public MidiEventScheduler() {
        this.mReceiver = new SchedulingReceiver();
    }

    private MidiEvent createScheduledEvent(byte[] msg, int offset, int count, long timestamp) {
        if (count > POOL_EVENT_SIZE) {
            return new MidiEvent(offset, count, timestamp, null);
        }
        MidiEvent event = (MidiEvent) removeEventfromPool();
        if (event == null) {
            event = new MidiEvent(null);
        }
        System.arraycopy(msg, offset, event.data, 0, count);
        event.count = count;
        event.setTimestamp(timestamp);
        return event;
    }

    public void addEventToPool(SchedulableEvent event) {
        if ((event instanceof MidiEvent) && ((MidiEvent) event).data.length == POOL_EVENT_SIZE) {
            super.addEventToPool(event);
        }
    }

    public MidiReceiver getReceiver() {
        return this.mReceiver;
    }
}
