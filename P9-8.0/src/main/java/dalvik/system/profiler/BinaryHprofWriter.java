package dalvik.system.profiler;

import dalvik.system.profiler.BinaryHprof.Tag;
import dalvik.system.profiler.HprofData.Sample;
import dalvik.system.profiler.HprofData.StackTrace;
import dalvik.system.profiler.HprofData.ThreadEvent;
import dalvik.system.profiler.HprofData.ThreadEventType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class BinaryHprofWriter {
    private static final /* synthetic */ int[] -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues = null;
    private final Map<String, Integer> classNameToId = new HashMap();
    private final HprofData data;
    private int nextClassId = 1;
    private int nextStackFrameId = 1;
    private int nextStringId = 1;
    private final DataOutputStream out;
    private final Map<StackTraceElement, Integer> stackFrameToId = new HashMap();
    private final Map<String, Integer> stringToId = new HashMap();

    private static /* synthetic */ int[] -getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues() {
        if (-dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues != null) {
            return -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues;
        }
        int[] iArr = new int[ThreadEventType.values().length];
        try {
            iArr[ThreadEventType.END.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ThreadEventType.START.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -dalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues = iArr;
        return iArr;
    }

    public static void write(HprofData data, OutputStream outputStream) throws IOException {
        new BinaryHprofWriter(data, outputStream).write();
    }

    private BinaryHprofWriter(HprofData data, OutputStream outputStream) {
        this.data = data;
        this.out = new DataOutputStream(outputStream);
    }

    private void write() throws IOException {
        try {
            writeHeader(this.data.getStartMillis());
            writeControlSettings(this.data.getFlags(), this.data.getDepth());
            for (ThreadEvent event : this.data.getThreadHistory()) {
                writeThreadEvent(event);
            }
            Set<Sample> samples = this.data.getSamples();
            int total = 0;
            for (Sample sample : samples) {
                total += sample.count;
                writeStackTrace(sample.stackTrace);
            }
            writeCpuSamples(total, samples);
        } finally {
            this.out.flush();
        }
    }

    private void writeHeader(long dumpTimeInMilliseconds) throws IOException {
        this.out.writeBytes(BinaryHprof.MAGIC + "1.0.2");
        this.out.writeByte(0);
        this.out.writeInt(4);
        this.out.writeLong(dumpTimeInMilliseconds);
    }

    private void writeControlSettings(int flags, int depth) throws IOException {
        if (depth > 32767) {
            throw new IllegalArgumentException("depth too large for binary hprof: " + depth + " > " + 32767);
        }
        writeRecordHeader(Tag.CONTROL_SETTINGS, 0, Tag.CONTROL_SETTINGS.maximumSize);
        this.out.writeInt(flags);
        this.out.writeShort((short) depth);
    }

    private void writeThreadEvent(ThreadEvent e) throws IOException {
        switch (-getdalvik-system-profiler-HprofData$ThreadEventTypeSwitchesValues()[e.type.ordinal()]) {
            case 1:
                writeStopThread(e);
                return;
            case 2:
                writeStartThread(e);
                return;
            default:
                throw new IllegalStateException(e.type.toString());
        }
    }

    private void writeStartThread(ThreadEvent e) throws IOException {
        int threadNameId = writeString(e.threadName);
        int groupNameId = writeString(e.groupName);
        int parentGroupNameId = writeString(e.parentGroupName);
        writeRecordHeader(Tag.START_THREAD, 0, Tag.START_THREAD.maximumSize);
        this.out.writeInt(e.threadId);
        writeId(e.objectId);
        this.out.writeInt(0);
        writeId(threadNameId);
        writeId(groupNameId);
        writeId(parentGroupNameId);
    }

    private void writeStopThread(ThreadEvent e) throws IOException {
        writeRecordHeader(Tag.END_THREAD, 0, Tag.END_THREAD.maximumSize);
        this.out.writeInt(e.threadId);
    }

    private void writeRecordHeader(Tag hprofTag, int timeDeltaInMicroseconds, int recordLength) throws IOException {
        String error = hprofTag.checkSize(recordLength);
        if (error != null) {
            throw new AssertionError(error);
        }
        this.out.writeByte(hprofTag.tag);
        this.out.writeInt(timeDeltaInMicroseconds);
        this.out.writeInt(recordLength);
    }

    private void writeId(int id) throws IOException {
        this.out.writeInt(id);
    }

    private int writeString(String string) throws IOException {
        if (string == null) {
            return 0;
        }
        Integer identifier = (Integer) this.stringToId.get(string);
        if (identifier != null) {
            return identifier.intValue();
        }
        int id = this.nextStringId;
        this.nextStringId = id + 1;
        this.stringToId.put(string, Integer.valueOf(id));
        byte[] bytes = string.getBytes("UTF-8");
        writeRecordHeader(Tag.STRING_IN_UTF8, 0, bytes.length + 4);
        this.out.writeInt(id);
        this.out.write(bytes, 0, bytes.length);
        return id;
    }

    private void writeCpuSamples(int totalSamples, Set<Sample> samples) throws IOException {
        int samplesCount = samples.size();
        if (samplesCount != 0) {
            writeRecordHeader(Tag.CPU_SAMPLES, 0, (samplesCount * 8) + 8);
            this.out.writeInt(totalSamples);
            this.out.writeInt(samplesCount);
            for (Sample sample : samples) {
                this.out.writeInt(sample.count);
                this.out.writeInt(sample.stackTrace.stackTraceId);
            }
        }
    }

    private void writeStackTrace(StackTrace stackTrace) throws IOException {
        int i = 0;
        int frames = stackTrace.stackFrames.length;
        int[] stackFrameIds = new int[frames];
        for (int i2 = 0; i2 < frames; i2++) {
            stackFrameIds[i2] = writeStackFrame(stackTrace.stackFrames[i2]);
        }
        writeRecordHeader(Tag.STACK_TRACE, 0, (frames * 4) + 12);
        this.out.writeInt(stackTrace.stackTraceId);
        this.out.writeInt(stackTrace.threadId);
        this.out.writeInt(frames);
        int length = stackFrameIds.length;
        while (i < length) {
            writeId(stackFrameIds[i]);
            i++;
        }
    }

    private int writeLoadClass(String className) throws IOException {
        Integer identifier = (Integer) this.classNameToId.get(className);
        if (identifier != null) {
            return identifier.intValue();
        }
        int id = this.nextClassId;
        this.nextClassId = id + 1;
        this.classNameToId.put(className, Integer.valueOf(id));
        int classNameId = writeString(className);
        writeRecordHeader(Tag.LOAD_CLASS, 0, Tag.LOAD_CLASS.maximumSize);
        this.out.writeInt(id);
        writeId(0);
        this.out.writeInt(0);
        writeId(classNameId);
        return id;
    }

    private int writeStackFrame(StackTraceElement stackFrame) throws IOException {
        Integer identifier = (Integer) this.stackFrameToId.get(stackFrame);
        if (identifier != null) {
            return identifier.intValue();
        }
        int id = this.nextStackFrameId;
        this.nextStackFrameId = id + 1;
        this.stackFrameToId.put(stackFrame, Integer.valueOf(id));
        int classId = writeLoadClass(stackFrame.getClassName());
        int methodNameId = writeString(stackFrame.getMethodName());
        int sourceId = writeString(stackFrame.getFileName());
        writeRecordHeader(Tag.STACK_FRAME, 0, Tag.STACK_FRAME.maximumSize);
        writeId(id);
        writeId(methodNameId);
        writeId(0);
        writeId(sourceId);
        this.out.writeInt(classId);
        this.out.writeInt(stackFrame.getLineNumber());
        return id;
    }
}
