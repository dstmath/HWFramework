package com.android.internal.midi;

import android.media.midi.MidiReceiver;
import com.android.internal.midi.EventScheduler;
import java.io.IOException;

public class MidiEventScheduler extends EventScheduler {
    private static final int POOL_EVENT_SIZE = 16;
    private static final String TAG = "MidiEventScheduler";
    private MidiReceiver mReceiver = new SchedulingReceiver();

    private class SchedulingReceiver extends MidiReceiver {
        private SchedulingReceiver() {
        }

        @Override // android.media.midi.MidiReceiver
        public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
            MidiEvent event = MidiEventScheduler.this.createScheduledEvent(msg, offset, count, timestamp);
            if (event != null) {
                MidiEventScheduler.this.add(event);
            }
        }

        @Override // android.media.midi.MidiReceiver
        public void onFlush() {
            MidiEventScheduler.this.flush();
        }
    }

    public static class MidiEvent extends EventScheduler.SchedulableEvent {
        public int count;
        public byte[] data;

        private MidiEvent(int count2) {
            super(0);
            this.count = 0;
            this.data = new byte[count2];
        }

        private MidiEvent(byte[] msg, int offset, int count2, long timestamp) {
            super(timestamp);
            this.count = 0;
            this.data = new byte[count2];
            System.arraycopy(msg, offset, this.data, 0, count2);
            this.count = count2;
        }

        public String toString() {
            String text = "Event: ";
            for (int i = 0; i < this.count; i++) {
                text = text + ((int) this.data[i]) + ", ";
            }
            return text;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MidiEvent createScheduledEvent(byte[] msg, int offset, int count, long timestamp) {
        MidiEvent event;
        if (count > 16) {
            return new MidiEvent(msg, offset, count, timestamp);
        }
        MidiEvent event2 = (MidiEvent) removeEventfromPool();
        if (event2 == null) {
            event = new MidiEvent(16);
        } else {
            event = event2;
        }
        System.arraycopy(msg, offset, event.data, 0, count);
        event.count = count;
        event.setTimestamp(timestamp);
        return event;
    }

    @Override // com.android.internal.midi.EventScheduler
    public void addEventToPool(EventScheduler.SchedulableEvent event) {
        if ((event instanceof MidiEvent) && ((MidiEvent) event).data.length == 16) {
            super.addEventToPool(event);
        }
    }

    public MidiReceiver getReceiver() {
        return this.mReceiver;
    }
}
