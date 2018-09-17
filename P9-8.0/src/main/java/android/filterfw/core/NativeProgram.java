package android.filterfw.core;

public class NativeProgram extends Program {
    private boolean mHasGetValueFunction = false;
    private boolean mHasInitFunction = false;
    private boolean mHasResetFunction = false;
    private boolean mHasSetValueFunction = false;
    private boolean mHasTeardownFunction = false;
    private boolean mTornDown = false;
    private int nativeProgramId;

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
                if (this.mHasInitFunction && (callNativeInit() ^ 1) != 0) {
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
            if (!this.mHasTeardownFunction || (callNativeTeardown() ^ 1) == 0) {
                deallocate();
                this.mTornDown = true;
                return;
            }
            throw new RuntimeException("Could not tear down NativeProgram!");
        }
    }

    public void reset() {
        if (this.mHasResetFunction && (callNativeReset() ^ 1) != 0) {
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

    static {
        System.loadLibrary("filterfw");
    }
}
