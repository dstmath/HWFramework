package com.android.commands.hid;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.util.SparseArray;
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
    private SparseArray<byte[]> mFeatureReports;
    private int mId;
    private String mName;
    private int mPid;
    private byte[] mReport;
    private int mVid;

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

    public SparseArray<byte[]> getFeatureReports() {
        return this.mFeatureReports;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public String toString() {
        return "Event{id=" + this.mId + ", command=" + String.valueOf(this.mCommand) + ", name=" + String.valueOf(this.mName) + ", descriptor=" + Arrays.toString(this.mDescriptor) + ", vid=" + this.mVid + ", pid=" + this.mPid + ", report=" + Arrays.toString(this.mReport) + ", feature_reports=" + this.mFeatureReports.toString() + ", duration=" + this.mDuration + "}";
    }

    private static class Builder {
        private Event mEvent = new Event();

        public void setId(int id) {
            this.mEvent.mId = id;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
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

        public void setFeatureReports(SparseArray<byte[]> reports) {
            this.mEvent.mFeatureReports = reports;
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

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
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
                                    c = '\b';
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
                            case -401007242:
                                if (name.equals("feature_reports")) {
                                    c = 7;
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
                                eb.setFeatureReports(readFeatureReports());
                                break;
                            case '\b':
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
                for (int i = 0; i < data.size(); i++) {
                    int d = data.get(i).intValue();
                    if ((d & 255) == d) {
                        rawData[i] = (byte) d;
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

        /* JADX WARNING: Removed duplicated region for block: B:20:0x004c A[Catch:{ IllegalStateException | NumberFormatException -> 0x008d, all -> 0x008b }] */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0073 A[Catch:{ IllegalStateException | NumberFormatException -> 0x008d, all -> 0x008b }] */
        private SparseArray<byte[]> readFeatureReports() throws IllegalStateException, IOException {
            SparseArray<byte[]> featureReports = new SparseArray<>();
            try {
                this.mReader.beginArray();
                while (this.mReader.hasNext()) {
                    int id = 0;
                    byte[] data = null;
                    this.mReader.beginObject();
                    while (this.mReader.hasNext()) {
                        String name = this.mReader.nextName();
                        char c = 65535;
                        int hashCode = name.hashCode();
                        if (hashCode != 3355) {
                            if (hashCode == 3076010 && name.equals("data")) {
                                c = 1;
                                if (c == 0) {
                                    id = readInt();
                                } else if (c == 1) {
                                    data = readData();
                                } else {
                                    consumeRemainingElements();
                                    this.mReader.endObject();
                                    throw new IllegalStateException("Invalid key in feature report: " + name);
                                }
                            }
                        } else if (name.equals("id")) {
                            c = 0;
                            if (c == 0) {
                            }
                        }
                        if (c == 0) {
                        }
                    }
                    this.mReader.endObject();
                    if (data != null) {
                        featureReports.put(id, data);
                    }
                }
                this.mReader.endArray();
                return featureReports;
            } catch (IllegalStateException | NumberFormatException e) {
                consumeRemainingElements();
                this.mReader.endArray();
                throw new IllegalStateException("Encountered malformed data.", e);
            } catch (Throwable th) {
                return featureReports;
            }
        }

        private void consumeRemainingElements() throws IOException {
            while (this.mReader.hasNext()) {
                this.mReader.skipValue();
            }
        }
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
