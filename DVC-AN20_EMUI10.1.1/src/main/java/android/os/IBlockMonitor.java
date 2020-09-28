package android.os;

public interface IBlockMonitor {
    void checkBinderTime(long j);

    void checkInputReceiveTime(int i, long j);

    void checkInputTime(long j);

    void initialize();
}
