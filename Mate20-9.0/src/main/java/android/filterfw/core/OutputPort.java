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

    public void open() {
        super.open();
        if (this.mTargetPort != null && !this.mTargetPort.isOpen()) {
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
        if (this.mTargetPort == null) {
            return null;
        }
        return this.mTargetPort.getFilter();
    }

    public void setBasePort(InputPort basePort) {
        this.mBasePort = basePort;
    }

    public InputPort getBasePort() {
        return this.mBasePort;
    }

    public boolean filterMustClose() {
        return !isOpen() && isBlocking();
    }

    public boolean isReady() {
        return (isOpen() && this.mTargetPort.acceptsFrame()) || !isBlocking();
    }

    public void clear() {
        if (this.mTargetPort != null) {
            this.mTargetPort.clear();
        }
    }

    public void pushFrame(Frame frame) {
        if (this.mTargetPort != null) {
            this.mTargetPort.pushFrame(frame);
            return;
        }
        throw new RuntimeException("Attempting to push frame on unconnected port: " + this + "!");
    }

    public void setFrame(Frame frame) {
        assertPortIsOpen();
        if (this.mTargetPort != null) {
            this.mTargetPort.setFrame(frame);
            return;
        }
        throw new RuntimeException("Attempting to set frame on unconnected port: " + this + "!");
    }

    public Frame pullFrame() {
        throw new RuntimeException("Cannot pull frame on " + this + "!");
    }

    public boolean hasFrame() {
        if (this.mTargetPort == null) {
            return false;
        }
        return this.mTargetPort.hasFrame();
    }

    public String toString() {
        return "output " + super.toString();
    }
}
