package ohos.data.resultset;

public interface SharedResultSet extends ResultSet {
    void fillBlock(int i, SharedBlock sharedBlock);

    SharedBlock getBlock();

    boolean onGo(int i, int i2);
}
