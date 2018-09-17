package dalvik.system.profiler;

import dalvik.system.profiler.BinaryHprof.Tag;
import dalvik.system.profiler.HprofData.StackTrace;
import dalvik.system.profiler.HprofData.ThreadEvent;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class BinaryHprofReader {
    private static final /* synthetic */ int[] -dalvik-system-profiler-BinaryHprof$TagSwitchesValues = null;
    private static final boolean TRACE = false;
    private final HprofData hprofData = new HprofData(this.stackTraces);
    private final Map<Integer, String> idToClassName = new HashMap();
    private final Map<Integer, StackTraceElement> idToStackFrame = new HashMap();
    private final Map<Integer, StackTrace> idToStackTrace = new HashMap();
    private final Map<Integer, String> idToString = new HashMap();
    private final DataInputStream in;
    private final Map<StackTrace, int[]> stackTraces = new HashMap();
    private boolean strict = true;
    private String version;

    private static /* synthetic */ int[] -getdalvik-system-profiler-BinaryHprof$TagSwitchesValues() {
        if (-dalvik-system-profiler-BinaryHprof$TagSwitchesValues != null) {
            return -dalvik-system-profiler-BinaryHprof$TagSwitchesValues;
        }
        int[] iArr = new int[Tag.values().length];
        try {
            iArr[Tag.ALLOC_SITES.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Tag.CONTROL_SETTINGS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Tag.CPU_SAMPLES.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Tag.END_THREAD.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Tag.HEAP_DUMP.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Tag.HEAP_DUMP_END.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Tag.HEAP_DUMP_SEGMENT.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Tag.HEAP_SUMMARY.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Tag.LOAD_CLASS.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Tag.STACK_FRAME.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Tag.STACK_TRACE.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Tag.START_THREAD.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Tag.STRING_IN_UTF8.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Tag.UNLOAD_CLASS.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        -dalvik-system-profiler-BinaryHprof$TagSwitchesValues = iArr;
        return iArr;
    }

    public BinaryHprofReader(InputStream inputStream) throws IOException {
        this.in = new DataInputStream(inputStream);
    }

    public boolean getStrict() {
        return this.strict;
    }

    public void setStrict(boolean strict) {
        if (this.version != null) {
            throw new IllegalStateException("cannot set strict after read()");
        }
        this.strict = strict;
    }

    private void checkRead() {
        if (this.version == null) {
            throw new IllegalStateException("data access before read()");
        }
    }

    public String getVersion() {
        checkRead();
        return this.version;
    }

    public HprofData getHprofData() {
        checkRead();
        return this.hprofData;
    }

    public void read() throws IOException {
        parseHeader();
        parseRecords();
    }

    private void parseHeader() throws IOException {
        parseVersion();
        parseIdSize();
        parseTime();
    }

    private void parseVersion() throws IOException {
        String version = BinaryHprof.readMagic(this.in);
        if (version == null) {
            throw new MalformedHprofException("Could not find HPROF version");
        }
        this.version = version;
    }

    private void parseIdSize() throws IOException {
        int idSize = this.in.readInt();
        if (idSize != 4) {
            throw new MalformedHprofException("Unsupported identifier size: " + idSize);
        }
    }

    private void parseTime() throws IOException {
        this.hprofData.setStartMillis(this.in.readLong());
    }

    private void parseRecords() throws IOException {
        do {
        } while (parseRecord());
    }

    private boolean parseRecord() throws IOException {
        int tagOrEOF = this.in.read();
        if (tagOrEOF == -1) {
            return false;
        }
        byte tag = (byte) tagOrEOF;
        int timeDeltaInMicroseconds = this.in.readInt();
        int recordLength = this.in.readInt();
        Tag hprofTag = Tag.get(tag);
        if (hprofTag == null) {
            skipRecord(hprofTag, (long) recordLength);
            return true;
        }
        String error = hprofTag.checkSize(recordLength);
        if (error != null) {
            throw new MalformedHprofException(error);
        }
        switch (-getdalvik-system-profiler-BinaryHprof$TagSwitchesValues()[hprofTag.ordinal()]) {
            case 2:
                parseControlSettings();
                return true;
            case 3:
                parseCpuSamples(recordLength);
                return true;
            case 4:
                parseEndThread();
                return true;
            case 9:
                parseLoadClass();
                return true;
            case 10:
                parseStackFrame();
                return true;
            case 11:
                parseStackTrace(recordLength);
                return true;
            case 12:
                parseStartThread();
                return true;
            case 13:
                parseStringInUtf8(recordLength);
                return true;
            default:
                skipRecord(hprofTag, (long) recordLength);
                return true;
        }
    }

    private void skipRecord(Tag hprofTag, long recordLength) throws IOException {
        long skipped = this.in.skip(recordLength);
        if (skipped != recordLength) {
            throw new EOFException("Expected to skip " + recordLength + " bytes but only skipped " + skipped + " bytes");
        }
    }

    private void parseControlSettings() throws IOException {
        int flags = this.in.readInt();
        short depth = this.in.readShort();
        this.hprofData.setFlags(flags);
        this.hprofData.setDepth(depth);
    }

    private void parseStringInUtf8(int recordLength) throws IOException {
        int stringId = this.in.readInt();
        byte[] bytes = new byte[(recordLength - 4)];
        readFully(this.in, bytes);
        if (((String) this.idToString.put(Integer.valueOf(stringId), new String(bytes, "UTF-8"))) != null) {
            throw new MalformedHprofException("Duplicate string id: " + stringId);
        }
    }

    private static void readFully(InputStream in, byte[] dst) throws IOException {
        int offset = 0;
        int byteCount = dst.length;
        while (byteCount > 0) {
            int bytesRead = in.read(dst, offset, byteCount);
            if (bytesRead < 0) {
                throw new EOFException();
            }
            offset += bytesRead;
            byteCount -= bytesRead;
        }
    }

    private void parseLoadClass() throws IOException {
        int classId = this.in.readInt();
        int classObjectId = readId();
        int stackTraceSerialNumber = this.in.readInt();
        if (((String) this.idToClassName.put(Integer.valueOf(classId), readString())) != null) {
            throw new MalformedHprofException("Duplicate class id: " + classId);
        }
    }

    private int readId() throws IOException {
        return this.in.readInt();
    }

    private String readString() throws IOException {
        int id = readId();
        if (id == 0) {
            return null;
        }
        String string = (String) this.idToString.get(Integer.valueOf(id));
        if (string != null) {
            return string;
        }
        throw new MalformedHprofException("Unknown string id " + id);
    }

    private String readClass() throws IOException {
        int id = readId();
        String string = (String) this.idToClassName.get(Integer.valueOf(id));
        if (string != null) {
            return string;
        }
        throw new MalformedHprofException("Unknown class id " + id);
    }

    private void parseStartThread() throws IOException {
        int threadId = this.in.readInt();
        int objectId = readId();
        int stackTraceSerialNumber = this.in.readInt();
        this.hprofData.addThreadEvent(ThreadEvent.start(objectId, threadId, readString(), readString(), readString()));
    }

    private void parseEndThread() throws IOException {
        this.hprofData.addThreadEvent(ThreadEvent.end(this.in.readInt()));
    }

    private void parseStackFrame() throws IOException {
        int stackFrameId = readId();
        String methodName = readString();
        String methodSignature = readString();
        if (((StackTraceElement) this.idToStackFrame.put(Integer.valueOf(stackFrameId), new StackTraceElement(readClass(), methodName, readString(), this.in.readInt()))) != null) {
            throw new MalformedHprofException("Duplicate stack frame id: " + stackFrameId);
        }
    }

    private void parseStackTrace(int recordLength) throws IOException {
        int stackTraceId = this.in.readInt();
        int threadId = this.in.readInt();
        int frames = this.in.readInt();
        int expectedLength = (frames * 4) + 12;
        if (recordLength != expectedLength) {
            throw new MalformedHprofException("Expected stack trace record of size " + expectedLength + " based on number of frames but header " + "specified a length of  " + recordLength);
        }
        StackTraceElement[] stackFrames = new StackTraceElement[frames];
        for (int i = 0; i < frames; i++) {
            int stackFrameId = readId();
            StackTraceElement stackFrame = (StackTraceElement) this.idToStackFrame.get(Integer.valueOf(stackFrameId));
            if (stackFrame == null) {
                throw new MalformedHprofException("Unknown stack frame id " + stackFrameId);
            }
            stackFrames[i] = stackFrame;
        }
        StackTrace stackTrace = new StackTrace(stackTraceId, threadId, stackFrames);
        if (this.strict) {
            this.hprofData.addStackTrace(stackTrace, new int[1]);
        } else if (((int[]) this.stackTraces.get(stackTrace)) == null) {
            this.hprofData.addStackTrace(stackTrace, new int[1]);
        }
        if (((StackTrace) this.idToStackTrace.put(Integer.valueOf(stackTraceId), stackTrace)) != null) {
            throw new MalformedHprofException("Duplicate stack trace id: " + stackTraceId);
        }
    }

    private void parseCpuSamples(int recordLength) throws IOException {
        int totalSamples = this.in.readInt();
        int samplesCount = this.in.readInt();
        int expectedLength = (samplesCount * 8) + 8;
        if (recordLength != expectedLength) {
            throw new MalformedHprofException("Expected CPU samples record of size " + expectedLength + " based on number of samples but header " + "specified a length of  " + recordLength);
        }
        int total = 0;
        int i = 0;
        while (i < samplesCount) {
            int count = this.in.readInt();
            int stackTraceId = this.in.readInt();
            StackTrace stackTrace = (StackTrace) this.idToStackTrace.get(Integer.valueOf(stackTraceId));
            if (stackTrace == null) {
                throw new MalformedHprofException("Unknown stack trace id " + stackTraceId);
            } else if (count == 0) {
                throw new MalformedHprofException("Zero sample count for stack trace " + stackTrace);
            } else {
                int[] countCell = (int[]) this.stackTraces.get(stackTrace);
                if (!this.strict) {
                    count += countCell[0];
                } else if (countCell[0] != 0) {
                    throw new MalformedHprofException("Setting sample count of stack trace " + stackTrace + " to " + count + " found it was already initialized to " + countCell[0]);
                }
                countCell[0] = count;
                total += count;
                i++;
            }
        }
        if (this.strict && totalSamples != total) {
            throw new MalformedHprofException("Expected a total of " + totalSamples + " samples but saw " + total);
        }
    }
}
