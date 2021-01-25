package android.filterfw.core;

public abstract class InputPort extends FilterPort {
    protected OutputPort mSourcePort;

    public abstract void transfer(FilterContext filterContext);

    public InputPort(Filter filter, String name) {
        super(filter, name);
    }

    public void setSourcePort(OutputPort source) {
        if (this.mSourcePort == null) {
            this.mSourcePort = source;
            return;
        }
        throw new RuntimeException(this + " already connected to " + this.mSourcePort + "!");
    }

    public boolean isConnected() {
        return this.mSourcePort != null;
    }

    @Override // android.filterfw.core.FilterPort
    public void open() {
        super.open();
        OutputPort outputPort = this.mSourcePort;
        if (outputPort != null && !outputPort.isOpen()) {
            this.mSourcePort.open();
        }
    }

    @Override // android.filterfw.core.FilterPort
    public void close() {
        OutputPort outputPort = this.mSourcePort;
        if (outputPort != null && outputPort.isOpen()) {
            this.mSourcePort.close();
        }
        super.close();
    }

    public OutputPort getSourcePort() {
        return this.mSourcePort;
    }

    public Filter getSourceFilter() {
        OutputPort outputPort = this.mSourcePort;
        if (outputPort == null) {
            return null;
        }
        return outputPort.getFilter();
    }

    public FrameFormat getSourceFormat() {
        OutputPort outputPort = this.mSourcePort;
        return outputPort != null ? outputPort.getPortFormat() : getPortFormat();
    }

    public Object getTarget() {
        return null;
    }

    @Override // android.filterfw.core.FilterPort
    public boolean filterMustClose() {
        return !isOpen() && isBlocking() && !hasFrame();
    }

    @Override // android.filterfw.core.FilterPort
    public boolean isReady() {
        return hasFrame() || !isBlocking();
    }

    public boolean acceptsFrame() {
        return !hasFrame();
    }
}
