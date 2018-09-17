package tmsdk.common;

public interface MessageHandler {
    boolean isMatch(int i);

    DataEntity onProcessing(DataEntity dataEntity);
}
