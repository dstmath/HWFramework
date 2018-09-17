package android.filterfw.core;

import java.util.Map.Entry;

public class FilterFunction {
    private Filter mFilter;
    private FilterContext mFilterContext;
    private boolean mFilterIsSetup;
    private FrameHolderPort[] mResultHolders;

    private class FrameHolderPort extends StreamPort {
        public FrameHolderPort() {
            super(null, "holder");
        }
    }

    public FilterFunction(FilterContext context, Filter filter) {
        this.mFilterIsSetup = false;
        this.mFilterContext = context;
        this.mFilter = filter;
    }

    public Frame execute(KeyValueMap inputMap) {
        int filterOutCount = this.mFilter.getNumberOfOutputs();
        if (filterOutCount > 1) {
            throw new RuntimeException("Calling execute on filter " + this.mFilter + " with multiple " + "outputs! Use executeMulti() instead!");
        }
        if (!this.mFilterIsSetup) {
            connectFilterOutputs();
            this.mFilterIsSetup = true;
        }
        boolean didActivateGLEnv = false;
        GLEnvironment glEnv = this.mFilterContext.getGLEnvironment();
        if (!(glEnv == null || glEnv.isActive())) {
            glEnv.activate();
            didActivateGLEnv = true;
        }
        for (Entry<String, Object> entry : inputMap.entrySet()) {
            if (entry.getValue() instanceof Frame) {
                this.mFilter.pushInputFrame((String) entry.getKey(), (Frame) entry.getValue());
            } else {
                this.mFilter.pushInputValue((String) entry.getKey(), entry.getValue());
            }
        }
        if (this.mFilter.getStatus() != 3) {
            this.mFilter.openOutputs();
        }
        this.mFilter.performProcess(this.mFilterContext);
        Frame result = null;
        if (filterOutCount == 1 && this.mResultHolders[0].hasFrame()) {
            result = this.mResultHolders[0].pullFrame();
        }
        if (didActivateGLEnv) {
            glEnv.deactivate();
        }
        return result;
    }

    public Frame executeWithArgList(Object... inputs) {
        return execute(KeyValueMap.fromKeyValues(inputs));
    }

    public void close() {
        this.mFilter.performClose(this.mFilterContext);
    }

    public FilterContext getContext() {
        return this.mFilterContext;
    }

    public Filter getFilter() {
        return this.mFilter;
    }

    public void setInputFrame(String input, Frame frame) {
        this.mFilter.setInputFrame(input, frame);
    }

    public void setInputValue(String input, Object value) {
        this.mFilter.setInputValue(input, value);
    }

    public void tearDown() {
        this.mFilter.performTearDown(this.mFilterContext);
        this.mFilter = null;
    }

    public String toString() {
        return this.mFilter.getName();
    }

    private void connectFilterOutputs() {
        int i = 0;
        this.mResultHolders = new FrameHolderPort[this.mFilter.getNumberOfOutputs()];
        for (OutputPort outputPort : this.mFilter.getOutputPorts()) {
            this.mResultHolders[i] = new FrameHolderPort();
            outputPort.connectTo(this.mResultHolders[i]);
            i++;
        }
    }
}
