package android.filterfw.core;

public class NativeProgram extends Program {
    private boolean mHasGetValueFunction;
    private boolean mHasInitFunction;
    private boolean mHasResetFunction;
    private boolean mHasSetValueFunction;
    private boolean mHasTeardownFunction;
    private boolean mTornDown;
    private int nativeProgramId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.filterfw.core.NativeProgram.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.filterfw.core.NativeProgram.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterfw.core.NativeProgram.<clinit>():void");
    }

    private native boolean allocate();

    private native boolean bindGetValueFunction(String str);

    private native boolean bindInitFunction(String str);

    private native boolean bindProcessFunction(String str);

    private native boolean bindResetFunction(String str);

    private native boolean bindSetValueFunction(String str);

    private native boolean bindTeardownFunction(String str);

    private native String callNativeGetValue(String str);

    private native boolean callNativeInit();

    private native boolean callNativeProcess(NativeFrame[] nativeFrameArr, NativeFrame nativeFrame);

    private native boolean callNativeReset();

    private native boolean callNativeSetValue(String str, String str2);

    private native boolean callNativeTeardown();

    private native boolean deallocate();

    private native boolean nativeInit();

    private native boolean openNativeLibrary(String str);

    public NativeProgram(String nativeLibName, String nativeFunctionPrefix) {
        this.mHasInitFunction = false;
        this.mHasTeardownFunction = false;
        this.mHasSetValueFunction = false;
        this.mHasGetValueFunction = false;
        this.mHasResetFunction = false;
        this.mTornDown = false;
        allocate();
        String fullLibName = "lib" + nativeLibName + ".so";
        if (openNativeLibrary(fullLibName)) {
            String processFuncName = nativeFunctionPrefix + "_process";
            if (bindProcessFunction(processFuncName)) {
                this.mHasInitFunction = bindInitFunction(nativeFunctionPrefix + "_init");
                this.mHasTeardownFunction = bindTeardownFunction(nativeFunctionPrefix + "_teardown");
                this.mHasSetValueFunction = bindSetValueFunction(nativeFunctionPrefix + "_setvalue");
                this.mHasGetValueFunction = bindGetValueFunction(nativeFunctionPrefix + "_getvalue");
                this.mHasResetFunction = bindResetFunction(nativeFunctionPrefix + "_reset");
                if (this.mHasInitFunction && !callNativeInit()) {
                    throw new RuntimeException("Could not initialize NativeProgram!");
                }
                return;
            }
            throw new RuntimeException("Could not find native program function name " + processFuncName + " in library " + fullLibName + "! " + "This function is required!");
        }
        throw new RuntimeException("Could not find native library named '" + fullLibName + "' " + "required for native program!");
    }

    public void tearDown() {
        if (!this.mTornDown) {
            if (!this.mHasTeardownFunction || callNativeTeardown()) {
                deallocate();
                this.mTornDown = true;
                return;
            }
            throw new RuntimeException("Could not tear down NativeProgram!");
        }
    }

    public void reset() {
        if (this.mHasResetFunction && !callNativeReset()) {
            throw new RuntimeException("Could not reset NativeProgram!");
        }
    }

    protected void finalize() throws Throwable {
        tearDown();
    }

    public void process(Frame[] inputs, Frame output) {
        if (this.mTornDown) {
            throw new RuntimeException("NativeProgram already torn down!");
        }
        NativeFrame[] nativeInputs = new NativeFrame[inputs.length];
        int i = 0;
        while (i < inputs.length) {
            if (inputs[i] == null || (inputs[i] instanceof NativeFrame)) {
                nativeInputs[i] = (NativeFrame) inputs[i];
                i++;
            } else {
                throw new RuntimeException("NativeProgram got non-native frame as input " + i + "!");
            }
        }
        if (output != null && !(output instanceof NativeFrame)) {
            throw new RuntimeException("NativeProgram got non-native output frame!");
        } else if (!callNativeProcess(nativeInputs, (NativeFrame) output)) {
            throw new RuntimeException("Calling native process() caused error!");
        }
    }

    public void setHostValue(String variableName, Object value) {
        if (this.mTornDown) {
            throw new RuntimeException("NativeProgram already torn down!");
        } else if (!this.mHasSetValueFunction) {
            throw new RuntimeException("Attempting to set native variable, but native code does not define native setvalue function!");
        } else if (!callNativeSetValue(variableName, value.toString())) {
            throw new RuntimeException("Error setting native value for variable '" + variableName + "'!");
        }
    }

    public Object getHostValue(String variableName) {
        if (this.mTornDown) {
            throw new RuntimeException("NativeProgram already torn down!");
        } else if (this.mHasGetValueFunction) {
            return callNativeGetValue(variableName);
        } else {
            throw new RuntimeException("Attempting to get native variable, but native code does not define native getvalue function!");
        }
    }
}
