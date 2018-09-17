package com.android.internal.midi;

import android.media.midi.MidiReceiver;
import com.android.internal.midi.EventScheduler.SchedulableEvent;
import java.io.IOException;

public class MidiEventScheduler extends EventScheduler {
    private static final int POOL_EVENT_SIZE = 16;
    private static final String TAG = "MidiEventScheduler";
    private MidiReceiver mReceiver = new SchedulingReceiver(this, null);

    public static class MidiEvent extends SchedulableEvent {
        public int count;
        public byte[] data;

        /* synthetic */ MidiEvent(int count, MidiEvent -this1) {
            this(count);
        }

        /* synthetic */ MidiEvent(byte[] msg, int offset, int count, long timestamp, MidiEvent -this4) {
            this(msg, offset, count, timestamp);
        }

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
        /* synthetic */ SchedulingReceiver(MidiEventScheduler this$0, SchedulingReceiver -this1) {
            this();
        }

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

    private MidiEvent createScheduledEvent(byte[] msg, int offset, int count, long timestamp) {
        if (count > 16) {
            return new MidiEvent(msg, offset, count, timestamp, null);
        }
        MidiEvent event = (MidiEvent) removeEventfromPool();
        if (event == null) {
            event = new MidiEvent(16, null);
        }
        System.arraycopy(msg, offset, event.data, 0, count);
        event.count = count;
        event.setTimestamp(timestamp);
        return event;
    }

    public void addEventToPool(SchedulableEvent event) {
        if ((event instanceof MidiEvent) && ((MidiEvent) event).data.length == 16) {
            super.addEventToPool(event);
        }
    }

    public MidiReceiver getReceiver() {
        return this.mReceiver;
    }
}
