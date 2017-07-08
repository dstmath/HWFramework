package android.filterfw.core;

import android.R;
import android.filterfw.format.ObjectFormat;
import android.filterfw.io.GraphIOException;
import android.filterfw.io.TextGraphReader;
import android.util.Log;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public abstract class Filter {
    static final int STATUS_ERROR = 6;
    static final int STATUS_FINISHED = 5;
    static final int STATUS_PREINIT = 0;
    static final int STATUS_PREPARED = 2;
    static final int STATUS_PROCESSING = 3;
    static final int STATUS_RELEASED = 7;
    static final int STATUS_SLEEPING = 4;
    static final int STATUS_UNPREPARED = 1;
    private static final String TAG = "Filter";
    private long mCurrentTimestamp;
    private HashSet<Frame> mFramesToRelease;
    private HashMap<String, Frame> mFramesToSet;
    private int mInputCount;
    private HashMap<String, InputPort> mInputPorts;
    private boolean mIsOpen;
    private boolean mLogVerbose;
    private String mName;
    private int mOutputCount;
    private HashMap<String, OutputPort> mOutputPorts;
    private int mSleepDelay;
    private int mStatus;

    public abstract void process(FilterContext filterContext);

    public abstract void setupPorts();

    final synchronized void unsetStatus(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.filterfw.core.Filter.unsetStatus(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterfw.core.Filter.unsetStatus(int):void");
    }

    public Filter(String name) {
        this.mInputCount = -1;
        this.mOutputCount = -1;
        this.mStatus = STATUS_PREINIT;
        this.mIsOpen = false;
        this.mName = name;
        this.mFramesToRelease = new HashSet();
        this.mFramesToSet = new HashMap();
        this.mStatus = STATUS_PREINIT;
        this.mLogVerbose = Log.isLoggable(TAG, STATUS_PREPARED);
    }

    public static final boolean isAvailable(String filterName) {
        try {
            try {
                Thread.currentThread().getContextClassLoader().loadClass(filterName).asSubclass(Filter.class);
                return true;
            } catch (ClassCastException e) {
                return false;
            }
        } catch (ClassNotFoundException e2) {
            return false;
        }
    }

    public final void initWithValueMap(KeyValueMap valueMap) {
        initFinalPorts(valueMap);
        initRemainingPorts(valueMap);
        this.mStatus = STATUS_UNPREPARED;
    }

    public final void initWithAssignmentString(String assignments) {
        try {
            initWithValueMap(new TextGraphReader().readKeyValueAssignments(assignments));
        } catch (GraphIOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public final void initWithAssignmentList(Object... keyValues) {
        KeyValueMap valueMap = new KeyValueMap();
        valueMap.setKeyValues(keyValues);
        initWithValueMap(valueMap);
    }

    public final void init() throws ProtocolException {
        initWithValueMap(new KeyValueMap());
    }

    public String getFilterClassName() {
        return getClass().getSimpleName();
    }

    public final String getName() {
        return this.mName;
    }

    public boolean isOpen() {
        return this.mIsOpen;
    }

    public void setInputFrame(String inputName, Frame frame) {
        FilterPort port = getInputPort(inputName);
        if (!port.isOpen()) {
            port.open();
        }
        port.setFrame(frame);
    }

    public final void setInputValue(String inputName, Object value) {
        setInputFrame(inputName, wrapInputValue(inputName, value));
    }

    protected void prepare(FilterContext context) {
    }

    protected void parametersUpdated(Set<String> set) {
    }

    protected void delayNextProcess(int millisecs) {
        this.mSleepDelay = millisecs;
        this.mStatus = STATUS_SLEEPING;
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return null;
    }

    public final FrameFormat getInputFormat(String portName) {
        return getInputPort(portName).getSourceFormat();
    }

    public void open(FilterContext context) {
    }

    public final int getSleepDelay() {
        return R.styleable.Theme_timePickerDialogTheme;
    }

    public void close(FilterContext context) {
    }

    public void tearDown(FilterContext context) {
    }

    public final int getNumberOfConnectedInputs() {
        int c = STATUS_PREINIT;
        for (InputPort inputPort : this.mInputPorts.values()) {
            if (inputPort.isConnected()) {
                c += STATUS_UNPREPARED;
            }
        }
        return c;
    }

    public final int getNumberOfConnectedOutputs() {
        int c = STATUS_PREINIT;
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            if (outputPort.isConnected()) {
                c += STATUS_UNPREPARED;
            }
        }
        return c;
    }

    public final int getNumberOfInputs() {
        return this.mOutputPorts == null ? STATUS_PREINIT : this.mInputPorts.size();
    }

    public final int getNumberOfOutputs() {
        return this.mInputPorts == null ? STATUS_PREINIT : this.mOutputPorts.size();
    }

    public final InputPort getInputPort(String portName) {
        if (this.mInputPorts == null) {
            throw new NullPointerException("Attempting to access input port '" + portName + "' of " + this + " before Filter has been initialized!");
        }
        InputPort result = (InputPort) this.mInputPorts.get(portName);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("Unknown input port '" + portName + "' on filter " + this + "!");
    }

    public final OutputPort getOutputPort(String portName) {
        if (this.mInputPorts == null) {
            throw new NullPointerException("Attempting to access output port '" + portName + "' of " + this + " before Filter has been initialized!");
        }
        OutputPort result = (OutputPort) this.mOutputPorts.get(portName);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("Unknown output port '" + portName + "' on filter " + this + "!");
    }

    protected final void pushOutput(String name, Frame frame) {
        if (frame.getTimestamp() == -2) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Default-setting output Frame timestamp on port " + name + " to " + this.mCurrentTimestamp);
            }
            frame.setTimestamp(this.mCurrentTimestamp);
        }
        getOutputPort(name).pushFrame(frame);
    }

    protected final Frame pullInput(String name) {
        Frame result = getInputPort(name).pullFrame();
        if (this.mCurrentTimestamp == -1) {
            this.mCurrentTimestamp = result.getTimestamp();
            if (this.mLogVerbose) {
                Log.v(TAG, "Default-setting current timestamp from input port " + name + " to " + this.mCurrentTimestamp);
            }
        }
        this.mFramesToRelease.add(result);
        return result;
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
    }

    protected void transferInputPortFrame(String name, FilterContext context) {
        getInputPort(name).transfer(context);
    }

    protected void initProgramInputs(Program program, FilterContext context) {
        if (program != null) {
            for (InputPort inputPort : this.mInputPorts.values()) {
                if (inputPort.getTarget() == program) {
                    inputPort.transfer(context);
                }
            }
        }
    }

    protected void addInputPort(String name) {
        addMaskedInputPort(name, null);
    }

    protected void addMaskedInputPort(String name, FrameFormat formatMask) {
        InputPort port = new StreamPort(this, name);
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + port);
        }
        this.mInputPorts.put(name, port);
        port.setPortFormat(formatMask);
    }

    protected void addOutputPort(String name, FrameFormat format) {
        OutputPort port = new OutputPort(this, name);
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + port);
        }
        port.setPortFormat(format);
        this.mOutputPorts.put(name, port);
    }

    protected void addOutputBasedOnInput(String outputName, String inputName) {
        OutputPort port = new OutputPort(this, outputName);
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + port);
        }
        port.setBasePort(getInputPort(inputName));
        this.mOutputPorts.put(outputName, port);
    }

    protected void addFieldPort(String name, Field field, boolean hasDefault, boolean isFinal) {
        InputPort fieldPort;
        field.setAccessible(true);
        if (isFinal) {
            fieldPort = new FinalPort(this, name, field, hasDefault);
        } else {
            fieldPort = new FieldPort(this, name, field, hasDefault);
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + fieldPort);
        }
        fieldPort.setPortFormat(ObjectFormat.fromClass(field.getType(), STATUS_UNPREPARED));
        this.mInputPorts.put(name, fieldPort);
    }

    protected void addProgramPort(String name, String varName, Field field, Class varType, boolean hasDefault) {
        field.setAccessible(true);
        InputPort programPort = new ProgramPort(this, name, varName, field, hasDefault);
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + programPort);
        }
        programPort.setPortFormat(ObjectFormat.fromClass(varType, STATUS_UNPREPARED));
        this.mInputPorts.put(name, programPort);
    }

    protected void closeOutputPort(String name) {
        getOutputPort(name).close();
    }

    protected void setWaitsOnInputPort(String portName, boolean waits) {
        getInputPort(portName).setBlocking(waits);
    }

    protected void setWaitsOnOutputPort(String portName, boolean waits) {
        getOutputPort(portName).setBlocking(waits);
    }

    public String toString() {
        return "'" + getName() + "' (" + getFilterClassName() + ")";
    }

    final Collection<InputPort> getInputPorts() {
        return this.mInputPorts.values();
    }

    final Collection<OutputPort> getOutputPorts() {
        return this.mOutputPorts.values();
    }

    final synchronized int getStatus() {
        return this.mStatus;
    }

    final synchronized void performOpen(FilterContext context) {
        if (!this.mIsOpen) {
            if (this.mStatus == STATUS_UNPREPARED) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Preparing " + this);
                }
                prepare(context);
                this.mStatus = STATUS_PREPARED;
            }
            if (this.mStatus == STATUS_PREPARED) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Opening " + this);
                }
                open(context);
                this.mStatus = STATUS_PROCESSING;
            }
            if (this.mStatus != STATUS_PROCESSING) {
                throw new RuntimeException("Filter " + this + " was brought into invalid state during " + "opening (state: " + this.mStatus + ")!");
            }
            this.mIsOpen = true;
        }
    }

    final synchronized void performProcess(FilterContext context) {
        if (this.mStatus == STATUS_RELEASED) {
            throw new RuntimeException("Filter " + this + " is already torn down!");
        }
        transferInputFrames(context);
        if (this.mStatus < STATUS_PROCESSING) {
            performOpen(context);
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "Processing " + this);
        }
        this.mCurrentTimestamp = -1;
        process(context);
        releasePulledFrames(context);
        if (filterMustClose()) {
            performClose(context);
        }
    }

    final synchronized void performClose(FilterContext context) {
        if (this.mIsOpen) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Closing " + this);
            }
            this.mIsOpen = false;
            this.mStatus = STATUS_PREPARED;
            close(context);
            closePorts();
        }
    }

    final synchronized void performTearDown(FilterContext context) {
        performClose(context);
        if (this.mStatus != STATUS_RELEASED) {
            tearDown(context);
            this.mStatus = STATUS_RELEASED;
        }
    }

    final synchronized boolean canProcess() {
        boolean z = false;
        synchronized (this) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Checking if can process: " + this + " (" + this.mStatus + ").");
            }
            if (this.mStatus <= STATUS_PROCESSING) {
                if (inputConditionsMet()) {
                    z = outputConditionsMet();
                }
                return z;
            }
            return false;
        }
    }

    final void openOutputs() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Opening all output ports on " + this + "!");
        }
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            if (!outputPort.isOpen()) {
                outputPort.open();
            }
        }
    }

    final void clearInputs() {
        for (InputPort inputPort : this.mInputPorts.values()) {
            inputPort.clear();
        }
    }

    final void clearOutputs() {
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            outputPort.clear();
        }
    }

    final void notifyFieldPortValueUpdated(String name, FilterContext context) {
        if (this.mStatus == STATUS_PROCESSING || this.mStatus == STATUS_PREPARED) {
            fieldPortValueUpdated(name, context);
        }
    }

    final synchronized void pushInputFrame(String inputName, Frame frame) {
        FilterPort port = getInputPort(inputName);
        if (!port.isOpen()) {
            port.open();
        }
        port.pushFrame(frame);
    }

    final synchronized void pushInputValue(String inputName, Object value) {
        pushInputFrame(inputName, wrapInputValue(inputName, value));
    }

    private final void initFinalPorts(KeyValueMap values) {
        this.mInputPorts = new HashMap();
        this.mOutputPorts = new HashMap();
        addAndSetFinalPorts(values);
    }

    private final void initRemainingPorts(KeyValueMap values) {
        addAnnotatedPorts();
        setupPorts();
        setInitialInputValues(values);
    }

    private final void addAndSetFinalPorts(KeyValueMap values) {
        Field[] declaredFields = getClass().getDeclaredFields();
        int length = declaredFields.length;
        for (int i = STATUS_PREINIT; i < length; i += STATUS_UNPREPARED) {
            Field field = declaredFields[i];
            Annotation annotation = field.getAnnotation(GenerateFinalPort.class);
            if (annotation != null) {
                GenerateFinalPort generator = (GenerateFinalPort) annotation;
                String name = generator.name().isEmpty() ? field.getName() : generator.name();
                addFieldPort(name, field, generator.hasDefault(), true);
                if (values.containsKey(name)) {
                    setImmediateInputValue(name, values.get(name));
                    values.remove(name);
                } else if (!generator.hasDefault()) {
                    throw new RuntimeException("No value specified for final input port '" + name + "' of filter " + this + "!");
                }
            }
        }
    }

    private final void addAnnotatedPorts() {
        Field[] declaredFields = getClass().getDeclaredFields();
        int length = declaredFields.length;
        for (int i = STATUS_PREINIT; i < length; i += STATUS_UNPREPARED) {
            Field field = declaredFields[i];
            Annotation annotation = field.getAnnotation(GenerateFieldPort.class);
            if (annotation != null) {
                addFieldGenerator((GenerateFieldPort) annotation, field);
            } else {
                annotation = field.getAnnotation(GenerateProgramPort.class);
                if (annotation != null) {
                    addProgramGenerator((GenerateProgramPort) annotation, field);
                } else {
                    annotation = field.getAnnotation(GenerateProgramPorts.class);
                    if (annotation != null) {
                        GenerateProgramPort[] value = ((GenerateProgramPorts) annotation).value();
                        int length2 = value.length;
                        for (int i2 = STATUS_PREINIT; i2 < length2; i2 += STATUS_UNPREPARED) {
                            addProgramGenerator(value[i2], field);
                        }
                    }
                }
            }
        }
    }

    private final void addFieldGenerator(GenerateFieldPort generator, Field field) {
        addFieldPort(generator.name().isEmpty() ? field.getName() : generator.name(), field, generator.hasDefault(), false);
    }

    private final void addProgramGenerator(GenerateProgramPort generator, Field field) {
        String varName;
        String name = generator.name();
        if (generator.variableName().isEmpty()) {
            varName = name;
        } else {
            varName = generator.variableName();
        }
        addProgramPort(name, varName, field, generator.type(), generator.hasDefault());
    }

    private final void setInitialInputValues(KeyValueMap values) {
        for (Entry<String, Object> entry : values.entrySet()) {
            setInputValue((String) entry.getKey(), entry.getValue());
        }
    }

    private final void setImmediateInputValue(String name, Object value) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Setting immediate value " + value + " for port " + name + "!");
        }
        FilterPort port = getInputPort(name);
        port.open();
        port.setFrame(SimpleFrame.wrapObject(value, null));
    }

    private final void transferInputFrames(FilterContext context) {
        for (InputPort inputPort : this.mInputPorts.values()) {
            inputPort.transfer(context);
        }
    }

    private final Frame wrapInputValue(String inputName, Object value) {
        boolean shouldSerialize;
        Frame frame;
        MutableFrameFormat inputFormat = ObjectFormat.fromObject(value, STATUS_UNPREPARED);
        if (value == null) {
            FrameFormat portFormat = getInputPort(inputName).getPortFormat();
            inputFormat.setObjectClass(portFormat == null ? null : portFormat.getObjectClass());
        }
        if ((value instanceof Number) || (value instanceof Boolean) || (value instanceof String)) {
            shouldSerialize = false;
        } else {
            shouldSerialize = value instanceof Serializable;
        }
        if (shouldSerialize) {
            frame = new SerializedFrame(inputFormat, null);
        } else {
            frame = new SimpleFrame(inputFormat, null);
        }
        frame.setObjectValue(value);
        return frame;
    }

    private final void releasePulledFrames(FilterContext context) {
        for (Frame frame : this.mFramesToRelease) {
            context.getFrameManager().releaseFrame(frame);
        }
        this.mFramesToRelease.clear();
    }

    private final boolean inputConditionsMet() {
        for (InputPort port : this.mInputPorts.values()) {
            if (!port.isReady()) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Input condition not met: " + port + "!");
                }
                return false;
            }
        }
        return true;
    }

    private final boolean outputConditionsMet() {
        for (OutputPort port : this.mOutputPorts.values()) {
            if (!port.isReady()) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Output condition not met: " + port + "!");
                }
                return false;
            }
        }
        return true;
    }

    private final void closePorts() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Closing all ports on " + this + "!");
        }
        for (InputPort inputPort : this.mInputPorts.values()) {
            inputPort.close();
        }
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            outputPort.close();
        }
    }

    private final boolean filterMustClose() {
        for (InputPort inputPort : this.mInputPorts.values()) {
            if (inputPort.filterMustClose()) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Filter " + this + " must close due to port " + inputPort);
                }
                return true;
            }
        }
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            if (outputPort.filterMustClose()) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Filter " + this + " must close due to port " + outputPort);
                }
                return true;
            }
        }
        return false;
    }
}
