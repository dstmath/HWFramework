package ohos.data.distributed.common;

public interface KvStoreResultSet {
    Entry getEntry();

    int getRowCount();

    int getRowIndex();

    boolean goToFirstRow();

    boolean goToLastRow();

    boolean goToNextRow();

    boolean goToPreviousRow();

    boolean goToRow(int i);

    boolean isAtFirstRow();

    boolean isAtLastRow();

    boolean isEnded();

    boolean isStarted();

    boolean skipRow(int i);
}
