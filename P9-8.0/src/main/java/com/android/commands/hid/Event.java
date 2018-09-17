package com.android.commands.hid;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Event {
    public static final String COMMAND_DELAY = "delay";
    public static final String COMMAND_REGISTER = "register";
    public static final String COMMAND_REPORT = "report";
    private static final String TAG = "HidEvent";
    private String mCommand;
    private byte[] mDescriptor;
    private int mDuration;
    private int mId;
    private String mName;
    private int mPid;
    private byte[] mReport;
    private int mVid;

    private static class Builder {
        private Event mEvent = new Event();

        public void setId(int id) {
            this.mEvent.mId = id;
        }

        private void setCommand(String command) {
            this.mEvent.mCommand = command;
        }

        public void setName(String name) {
            this.mEvent.mName = name;
        }

        public void setDescriptor(byte[] descriptor) {
            this.mEvent.mDescriptor = descriptor;
        }

        public void setReport(byte[] report) {
            this.mEvent.mReport = report;
        }

        public void setVid(int vid) {
            this.mEvent.mVid = vid;
        }

        public void setPid(int pid) {
            this.mEvent.mPid = pid;
        }

        public void setDuration(int duration) {
            this.mEvent.mDuration = duration;
        }

        public Event build() {
            if (this.mEvent.mId == -1) {
                throw new IllegalStateException("No event id");
            } else if (this.mEvent.mCommand == null) {
                throw new IllegalStateException("Event does not contain a command");
            } else {
                if (Event.COMMAND_REGISTER.equals(this.mEvent.mCommand)) {
                    if (this.mEvent.mDescriptor == null) {
                        throw new IllegalStateException("Device registration is missing descriptor");
                    }
                } else if (Event.COMMAND_DELAY.equals(this.mEvent.mCommand)) {
                    if (this.mEvent.mDuration <= 0) {
                        throw new IllegalStateException("Delay has missing or invalid duration");
                    }
                } else if (Event.COMMAND_REPORT.equals(this.mEvent.mCommand) && this.mEvent.mReport == null) {
                    throw new IllegalStateException("Report command is missing report data");
                }
                return this.mEvent;
            }
        }
    }

    public static class Reader {
        private JsonReader mReader;

        public Reader(InputStreamReader in) {
            this.mReader = new JsonReader(in);
            this.mReader.setLenient(true);
        }

        public Event getNextEvent() throws IOException {
            Event e = null;
            while (e == null && this.mReader.peek() != JsonToken.END_DOCUMENT) {
                Builder eb = new Builder();
                try {
                    this.mReader.beginObject();
                    while (this.mReader.hasNext()) {
                        String name = this.mReader.nextName();
                        if (name.equals("id")) {
                            eb.setId(readInt());
                        } else if (name.equals("command")) {
                            eb.setCommand(this.mReader.nextString());
                        } else if (name.equals("descriptor")) {
                            eb.setDescriptor(readData());
                        } else if (name.equals("name")) {
                            eb.setName(this.mReader.nextString());
                        } else if (name.equals("vid")) {
                            eb.setVid(readInt());
                        } else if (name.equals("pid")) {
                            eb.setPid(readInt());
                        } else if (name.equals(Event.COMMAND_REPORT)) {
                            eb.setReport(readData());
                        } else if (name.equals("duration")) {
                            eb.setDuration(readInt());
                        } else {
                            this.mReader.skipValue();
                        }
                    }
                    this.mReader.endObject();
                    e = eb.build();
                } catch (IllegalStateException ex) {
                    Event.error("Error reading in object, ignoring.", ex);
                    consumeRemainingElements();
                    this.mReader.endObject();
                }
            }
            return e;
        }

        /* JADX WARNING: Removed duplicated region for block: B:7:0x0020 A:{Splitter: B:1:0x0005, ExcHandler: java.lang.IllegalStateException (r2_0 'e' java.lang.RuntimeException)} */
        /* JADX WARNING: Missing block: B:7:0x0020, code:
            r2 = move-exception;
     */
        /* JADX WARNING: Missing block: B:8:0x0021, code:
            consumeRemainingElements();
            r7.mReader.endArray();
     */
        /* JADX WARNING: Missing block: B:9:0x0031, code:
            throw new java.lang.IllegalStateException("Encountered malformed data.", r2);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private byte[] readData() throws IOException {
            ArrayList<Integer> data = new ArrayList();
            try {
                this.mReader.beginArray();
                while (this.mReader.hasNext()) {
                    data.add(Integer.decode(this.mReader.nextString()));
                }
                this.mReader.endArray();
                byte[] rawData = new byte[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    int d = ((Integer) data.get(i)).intValue();
                    if ((d & 255) != d) {
                        throw new IllegalStateException("Invalid data, all values must be byte-sized");
                    }
                    rawData[i] = (byte) d;
                }
                return rawData;
            } catch (RuntimeException e) {
            }
        }

        private int readInt() throws IOException {
            return Integer.decode(this.mReader.nextString()).intValue();
        }

        private void consumeRemainingElements() throws IOException {
            while (this.mReader.hasNext()) {
                this.mReader.skipValue();
            }
        }
    }

    public int getId() {
        return this.mId;
    }

    public String getCommand() {
        return this.mCommand;
    }

    public String getName() {
        return this.mName;
    }

    public byte[] getDescriptor() {
        return this.mDescriptor;
    }

    public int getVendorId() {
        return this.mVid;
    }

    public int getProductId() {
        return this.mPid;
    }

    public byte[] getReport() {
        return this.mReport;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public String toString() {
        return "Event{id=" + this.mId + ", command=" + String.valueOf(this.mCommand) + ", name=" + String.valueOf(this.mName) + ", descriptor=" + Arrays.toString(this.mDescriptor) + ", vid=" + this.mVid + ", pid=" + this.mPid + ", report=" + Arrays.toString(this.mReport) + ", duration=" + this.mDuration + "}";
    }

    private static void error(String msg) {
        error(msg, null);
    }

    private static void error(String msg, Exception e) {
        System.out.println(msg);
        Log.e(TAG, msg);
        if (e != null) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
