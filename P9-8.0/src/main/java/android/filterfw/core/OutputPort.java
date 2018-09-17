package android.filterfw.core;

public class OutputPort extends FilterPort {
    protected InputPort mBasePort;
    protected InputPort mTargetPort;

    public OutputPort(Filter filter, String name) {
        super(filter, name);
    }

    public void connectTo(InputPort target) {
        if (this.mTargetPort != null) {
            throw new RuntimeException(this + " already connected to " + this.mTargetPort + "!");
        }
        this.mTargetPort = target;
        this.mTargetPort.setSourcePort(this);
    }

    public boolean isConnected() {
        return this.mTargetPort != null;
    }

    public void open() {
        super.open();
        if (this.mTargetPort != null && (this.mTargetPort.isOpen() ^ 1) != 0) {
            this.mTargetPort.open();
        }
    }

    public void close() {
        super.close();
        if (this.mTargetPort != null && this.mTargetPort.isOpen()) {
            this.mTargetPort.close();
        }
    }

    public InputPort getTargetPort() {
        return this.mTargetPort;
    }

    public Filter getTargetFilter() {
        return this.mTargetPort == null ? null : this.mTargetPort.getFilter();
    }

    public void setBasePort(InputPort basePort) {
        this.mBasePort = basePort;
    }

    public InputPort getBasePort() {
        return this.mBasePort;
    }

    public boolean filterMustClose() {
        return !isOpen() ? isBlocking() : false;
    }

    public boolean isReady() {
        return (isOpen() && this.mTargetPort.acceptsFrame()) ? true : isBlocking() ^ 1;
    }

    public void clear() {
        if (this.mTargetPort != null) {
            this.mTargetPort.clear();
        }
    }

    public void pushFrame(Frame frame) {
        if (this.mTargetPort == null) {
            throw new RuntimeException("Attempting to push frame on unconnected port: " + this + "!");
        }
        this.mTargetPort.pushFrame(frame);
    }

    public void setFrame(Frame frame) {
        assertPortIsOpen();
        if (this.mTargetPort == null) {
            throw new RuntimeException("Attempting to set frame on unconnected port: " + this + "!");
        }
        this.mTargetPort.setFrame(frame);
    }

    public Frame pullFrame() {
        throw new RuntimeException("Cannot pull frame on " + this + "!");
    }

    public boolean hasFrame() {
        return this.mTargetPort == null ? false : this.mTargetPort.hasFrame();
    }

    public String toString() {
        return "output " + super.toString();
    }
}
