package ohos.data.resultset;

import java.util.List;
import ohos.data.rdb.DataObserver;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public interface ResultSet {
    void close();

    List<Uri> getAffectedByUris();

    String[] getAllColumnNames();

    byte[] getBlob(int i);

    int getColumnCount();

    int getColumnIndexForName(String str);

    String getColumnNameForIndex(int i);

    ColumnType getColumnTypeForIndex(int i);

    double getDouble(int i);

    PacMap getExtensions();

    float getFloat(int i);

    int getInt(int i);

    long getLong(int i);

    int getRowCount();

    int getRowIndex();

    short getShort(int i);

    String getString(int i);

    boolean goTo(int i);

    boolean goToFirstRow();

    boolean goToLastRow();

    boolean goToNextRow();

    boolean goToPreviousRow();

    boolean goToRow(int i);

    boolean isAtFirstRow();

    boolean isAtLastRow();

    boolean isClosed();

    boolean isColumnNull(int i);

    boolean isEnded();

    boolean isStarted();

    void registerObserver(DataObserver dataObserver);

    void setAffectedByUris(Object obj, List<Uri> list);

    void setExtensions(PacMap pacMap);

    void unregisterObserver(DataObserver dataObserver);

    public enum ColumnType {
        TYPE_NULL(0),
        TYPE_INTEGER(1),
        TYPE_FLOAT(2),
        TYPE_STRING(3),
        TYPE_BLOB(4);
        
        private int value;

        private ColumnType(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }

        public static ColumnType getByValue(int i) {
            ColumnType[] values = values();
            for (ColumnType columnType : values) {
                if (columnType.getValue() == i) {
                    return columnType;
                }
            }
            return null;
        }
    }
}
