package android.scrollerboost;

public interface IScrollerBoostMgr {
    void finishListFling(float f);

    void init();

    void listFling(int i);

    void updateFrameJankInfo(long j);
}
