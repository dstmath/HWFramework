package android.debug;

public abstract class HdbManagerInternal {
    public abstract boolean isHdbEnabled();

    public abstract void registerTransport(IHdbTransport iHdbTransport);

    public abstract void unregisterTransport(IHdbTransport iHdbTransport);
}
