package ohos.data.resultset;

import java.util.List;
import ohos.app.Context;
import ohos.data.rdb.DataObserver;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public class ResultSetWrapper implements ResultSet {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "ResultSetWrapper");
    protected ResultSet mResultSet;

    public ResultSetWrapper(ResultSet resultSet) {
        if (resultSet != null) {
            this.mResultSet = resultSet;
        } else {
            HiLog.info(LABEL, "ResultSetWrapper: inputResultSet can't be null.", new Object[0]);
            throw new IllegalArgumentException("inputResultSet can't be null for ResultSetWrapper construction.");
        }
    }

    public ResultSet getResultSet() {
        return this.mResultSet;
    }

    @Override // ohos.data.resultset.ResultSet
    public String[] getAllColumnNames() {
        return this.mResultSet.getAllColumnNames();
    }

    @Override // ohos.data.resultset.ResultSet
    public int getColumnCount() {
        return this.mResultSet.getColumnCount();
    }

    @Override // ohos.data.resultset.ResultSet
    public ResultSet.ColumnType getColumnTypeForIndex(int i) {
        return this.mResultSet.getColumnTypeForIndex(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public int getColumnIndexForName(String str) {
        return this.mResultSet.getColumnIndexForName(str);
    }

    @Override // ohos.data.resultset.ResultSet
    public String getColumnNameForIndex(int i) {
        return this.mResultSet.getColumnNameForIndex(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public int getRowCount() {
        return this.mResultSet.getRowCount();
    }

    @Override // ohos.data.resultset.ResultSet
    public int getRowIndex() {
        return this.mResultSet.getRowIndex();
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goTo(int i) {
        return this.mResultSet.goTo(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goToRow(int i) {
        return this.mResultSet.goToRow(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goToFirstRow() {
        return this.mResultSet.goToFirstRow();
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goToLastRow() {
        return this.mResultSet.goToLastRow();
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goToNextRow() {
        return this.mResultSet.goToNextRow();
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goToPreviousRow() {
        return this.mResultSet.goToPreviousRow();
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isEnded() {
        return this.mResultSet.isEnded();
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isStarted() {
        return this.mResultSet.isStarted();
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isAtFirstRow() {
        return this.mResultSet.isAtFirstRow();
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isAtLastRow() {
        return this.mResultSet.isAtLastRow();
    }

    @Override // ohos.data.resultset.ResultSet
    public byte[] getBlob(int i) {
        return this.mResultSet.getBlob(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public String getString(int i) {
        return this.mResultSet.getString(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public short getShort(int i) {
        return this.mResultSet.getShort(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public int getInt(int i) {
        return this.mResultSet.getInt(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public long getLong(int i) {
        return this.mResultSet.getLong(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public float getFloat(int i) {
        return this.mResultSet.getFloat(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public double getDouble(int i) {
        return this.mResultSet.getDouble(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isColumnNull(int i) {
        return this.mResultSet.isColumnNull(i);
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isClosed() {
        return this.mResultSet.isClosed();
    }

    @Override // ohos.data.resultset.ResultSet
    public void close() {
        this.mResultSet.close();
    }

    @Override // ohos.data.resultset.ResultSet
    public void setExtensions(PacMap pacMap) {
        this.mResultSet.setExtensions(pacMap);
    }

    @Override // ohos.data.resultset.ResultSet
    public PacMap getExtensions() {
        return this.mResultSet.getExtensions();
    }

    @Override // ohos.data.resultset.ResultSet
    public void setAffectedByUris(Object obj, List<Uri> list) {
        if (list == null) {
            throw new IllegalArgumentException("input parameter uris can not be null");
        } else if (obj instanceof Context) {
            this.mResultSet.setAffectedByUris(obj, list);
        } else {
            throw new IllegalArgumentException("input parameter context must instanceof Context");
        }
    }

    @Override // ohos.data.resultset.ResultSet
    public List<Uri> getAffectedByUris() {
        return this.mResultSet.getAffectedByUris();
    }

    @Override // ohos.data.resultset.ResultSet
    public void registerObserver(DataObserver dataObserver) {
        this.mResultSet.registerObserver(dataObserver);
    }

    @Override // ohos.data.resultset.ResultSet
    public void unregisterObserver(DataObserver dataObserver) {
        this.mResultSet.unregisterObserver(dataObserver);
    }
}
