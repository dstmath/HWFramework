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
    /* access modifiers changed from: private */
    public String mCommand;
    /* access modifiers changed from: private */
    public byte[] mDescriptor;
    /* access modifiers changed from: private */
    public int mDuration;
    /* access modifiers changed from: private */
    public int mId;
    /* access modifiers changed from: private */
    public String mName;
    /* access modifiers changed from: private */
    public int mPid;
    /* access modifiers changed from: private */
    public byte[] mReport;
    /* access modifiers changed from: private */
    public int mVid;

    private static class Builder {
        private Event mEvent = new Event();

        public void setId(int id) {
            int unused = this.mEvent.mId = id;
        }

        /* access modifiers changed from: private */
        public void setCommand(String command) {
            String unused = this.mEvent.mCommand = command;
        }

        public void setName(String name) {
            String unused = this.mEvent.mName = name;
        }

        public void setDescriptor(byte[] descriptor) {
            byte[] unused = this.mEvent.mDescriptor = descriptor;
        }

        public void setReport(byte[] report) {
            byte[] unused = this.mEvent.mReport = report;
        }

        public void setVid(int vid) {
            int unused = this.mEvent.mVid = vid;
        }

        public void setPid(int pid) {
            int unused = this.mEvent.mPid = pid;
        }

        public void setDuration(int duration) {
            int unused = this.mEvent.mDuration = duration;
        }

        public Event build() {
            if (this.mEvent.mId == -1) {
                throw new IllegalStateException("No event id");
            } else if (this.mEvent.mCommand != null) {
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
            } else {
                throw new IllegalStateException("Event does not contain a command");
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
                        char c = 65535;
                        switch (name.hashCode()) {
                            case -1992012396:
                                if (name.equals("duration")) {
                                    c = 7;
                                    break;
                                }
                                break;
                            case -934521548:
                                if (name.equals(Event.COMMAND_REPORT)) {
                                    c = 6;
                                    break;
                                }
                                break;
                            case -748366993:
                                if (name.equals("descriptor")) {
                                    c = 2;
                                    break;
                                }
                                break;
                            case 3355:
                                if (name.equals("id")) {
                                    c = 0;
                                    break;
                                }
                                break;
                            case 110987:
                                if (name.equals("pid")) {
                                    c = 5;
                                    break;
                                }
                                break;
                            case 116753:
                                if (name.equals("vid")) {
                                    c = 4;
                                    break;
                                }
                                break;
                            case 3373707:
                                if (name.equals("name")) {
                                    c = 3;
                                    break;
                                }
                                break;
                            case 950394699:
                                if (name.equals("command")) {
                                    c = 1;
                                    break;
                                }
                                break;
                        }
                        switch (c) {
                            case 0:
                                eb.setId(readInt());
                                break;
                            case 1:
                                eb.setCommand(this.mReader.nextString());
                                break;
                            case 2:
                                eb.setDescriptor(readData());
                                break;
                            case 3:
                                eb.setName(this.mReader.nextString());
                                break;
                            case 4:
                                eb.setVid(readInt());
                                break;
                            case 5:
                                eb.setPid(readInt());
                                break;
                            case 6:
                                eb.setReport(readData());
                                break;
                            case 7:
                                eb.setDuration(readInt());
                                break;
                            default:
                                this.mReader.skipValue();
                                break;
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

        private byte[] readData() throws IOException {
            ArrayList<Integer> data = new ArrayList<>();
            try {
                this.mReader.beginArray();
                while (this.mReader.hasNext()) {
                    data.add(Integer.decode(this.mReader.nextString()));
                }
                this.mReader.endArray();
                byte[] rawData = new byte[data.size()];
                int i = 0;
                while (i < data.size()) {
                    int d = data.get(i).intValue();
                    if ((d & 255) == d) {
                        rawData[i] = (byte) d;
                        i++;
                    } else {
                        throw new IllegalStateException("Invalid data, all values must be byte-sized");
                    }
                }
                return rawData;
            } catch (IllegalStateException | NumberFormatException e) {
                consumeRemainingElements();
                this.mReader.endArray();
                throw new IllegalStateException("Encountered malformed data.", e);
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

    /* access modifiers changed from: private */
    public static void error(String msg, Exception e) {
        System.out.println(msg);
        Log.e(TAG, msg);
        if (e != null) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
