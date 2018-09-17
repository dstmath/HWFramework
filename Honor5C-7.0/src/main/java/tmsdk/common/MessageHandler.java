package tmsdk.common;

/* compiled from: Unknown */
public interface MessageHandler {
    boolean isMatch(int i);

    DataEntity onProcessing(DataEntity dataEntity);
}
