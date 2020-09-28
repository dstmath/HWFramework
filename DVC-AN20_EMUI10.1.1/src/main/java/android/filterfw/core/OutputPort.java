package android.filterfw.core;

public class OutputPort extends FilterPort {
    protected InputPort mBasePort;
    protected InputPort mTargetPort;

    public OutputPort(Filter filter, String name) {
        super(filter, name);
    }

    public void connectTo(InputPort target) {
        if (this.mTargetPort == null) {
            this.mTargetPort = target;
            this.mTargetPort.setSourcePort(this);
            return;
        }
        throw new RuntimeException(this + " already connected to " + this.mTargetPort + "!");
    }

    public boolean isConnected() {
        return this.mTargetPort != null;
    }

    @Override // android.filterfw.core.FilterPort
    public void open() {
        super.open();
        InputPort inputPort = this.mTargetPort;
        if (inputPort != null && !inputPort.isOpen()) {
            this.mTargetPort.open();
        }
    }

    @Override // android.filterfw.core.FilterPort
    public void close() {
        super.close();
        InputPort inputPort = this.mTargetPort;
        if (inputPort != null && inputPort.isOpen()) {
            this.mTargetPort.close();
        }
    }

    public InputPort getTargetPort() {
        return this.mTargetPort;
    }

    public Filter getTargetFilter() {
        InputPort inputPort = this.mTargetPort;
        if (inputPort == null) {
            return null;
        }
        return inputPort.getFilter();
    }

    public void setBasePort(InputPort basePort) {
        this.mBasePort = basePort;
    }

    public InputPort getBasePort() {
        return this.mBasePort;
    }

    @Override // android.filterfw.core.FilterPort
    public boolean filterMustClose() {
        return !isOpen() && isBlocking();
    }

    @Override // android.filterfw.core.FilterPort
    public boolean isReady() {
        return (isOpen() && this.mTargetPort.acceptsFrame()) || !isBlocking();
    }

    @Override // android.filterfw.core.FilterPort
    public void clear() {
        InputPort inputPort = this.mTargetPort;
        if (inputPort != null) {
            inputPort.clear();
        }
    }

    @Override // android.filterfw.core.FilterPort
    public void pushFrame(Frame frame) {
        InputPort inputPort = this.mTargetPort;
        if (inputPort != null) {
            inputPort.pushFrame(frame);
            return;
        }
        throw new RuntimeException("Attempting to push frame on unconnected port: " + this + "!");
    }

    @Override // android.filterfw.core.FilterPort
    public void setFrame(Frame frame) {
        assertPortIsOpen();
        InputPort inputPort = this.mTargetPort;
        if (inputPort != null) {
            inputPort.setFrame(frame);
            return;
        }
        throw new RuntimeException("Attempting to set frame on unconnected port: " + this + "!");
    }

    @Override // android.filterfw.core.FilterPort
    public Frame pullFrame() {
        throw new RuntimeException("Cannot pull frame on " + this + "!");
    }

    @Override // android.filterfw.core.FilterPort
    public boolean hasFrame() {
        InputPort inputPort = this.mTargetPort;
        if (inputPort == null) {
            return false;
        }
        return inputPort.hasFrame();
    }

    @Override // android.filterfw.core.FilterPort
    public String toString() {
        return "output " + super.toString();
    }
}
