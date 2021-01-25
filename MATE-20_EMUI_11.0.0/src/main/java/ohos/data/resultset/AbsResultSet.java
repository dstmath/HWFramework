package ohos.data.resultset;

import java.lang.ref.WeakReference;
import java.util.List;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.IDataAbilityObserver;
import ohos.app.Context;
import ohos.data.rdb.DataObservable;
import ohos.data.rdb.DataObserver;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public abstract class AbsResultSet implements ResultSet {
    protected static final int DEFAULT_POS = -1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "AbsResultSet");
    private DataAbilityHelper dataAbilityHelper;
    private DataObservable dataObservable = new DataObservable();
    private PacMap extensions = new PacMap();
    protected boolean isClosed = false;
    protected int pos = -1;
    private List<Uri> uris;
    private UrisObserver urisObserver;
    private final Object urisObserverLock = new Object();

    @Override // ohos.data.resultset.ResultSet
    public abstract String[] getAllColumnNames();

    @Override // ohos.data.resultset.ResultSet
    public abstract byte[] getBlob(int i);

    @Override // ohos.data.resultset.ResultSet
    public abstract double getDouble(int i);

    @Override // ohos.data.resultset.ResultSet
    public abstract float getFloat(int i);

    @Override // ohos.data.resultset.ResultSet
    public abstract int getInt(int i);

    @Override // ohos.data.resultset.ResultSet
    public abstract long getLong(int i);

    @Override // ohos.data.resultset.ResultSet
    public abstract int getRowCount();

    @Override // ohos.data.resultset.ResultSet
    public abstract short getShort(int i);

    @Override // ohos.data.resultset.ResultSet
    public abstract String getString(int i);

    @Override // ohos.data.resultset.ResultSet
    public abstract boolean goToRow(int i);

    @Override // ohos.data.resultset.ResultSet
    public abstract boolean isColumnNull(int i);

    @Override // ohos.data.resultset.ResultSet
    public int getRowIndex() {
        return this.pos;
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goTo(int i) {
        return goToRow(this.pos + i);
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goToFirstRow() {
        return goToRow(0);
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goToLastRow() {
        return goToRow(getRowCount() - 1);
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goToNextRow() {
        return goToRow(this.pos + 1);
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean goToPreviousRow() {
        return goToRow(this.pos - 1);
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isAtFirstRow() {
        return this.pos == 0 && getRowCount() != 0;
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isAtLastRow() {
        int rowCount = getRowCount();
        return this.pos == rowCount + -1 && rowCount != 0;
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isStarted() {
        if (getRowCount() == 0 || this.pos == -1) {
            return false;
        }
        return true;
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isEnded() {
        if (getRowCount() == 0 || this.pos == getRowCount()) {
            return true;
        }
        return false;
    }

    @Override // ohos.data.resultset.ResultSet
    public int getColumnCount() {
        return getAllColumnNames().length;
    }

    @Override // ohos.data.resultset.ResultSet
    public int getColumnIndexForName(String str) {
        int lastIndexOf = str.lastIndexOf(46);
        if (lastIndexOf != -1) {
            HiLog.debug(LABEL, "requesting column name with table name -- %{private}s", new Object[]{str});
            str = str.substring(lastIndexOf + 1);
        }
        String[] allColumnNames = getAllColumnNames();
        int length = allColumnNames.length;
        for (int i = 0; i < length; i++) {
            if (allColumnNames[i].equalsIgnoreCase(str)) {
                return i;
            }
        }
        return -1;
    }

    @Override // ohos.data.resultset.ResultSet
    public String getColumnNameForIndex(int i) {
        if (i < getColumnCount() && i >= 0) {
            return getAllColumnNames()[i];
        }
        throw new IllegalArgumentException("getColumnNameForIndex : column index is illegal.");
    }

    @Override // ohos.data.resultset.ResultSet
    public void setExtensions(PacMap pacMap) {
        if (pacMap == null) {
            pacMap = new PacMap();
        }
        this.extensions = pacMap;
    }

    @Override // ohos.data.resultset.ResultSet
    public PacMap getExtensions() {
        return this.extensions;
    }

    @Override // ohos.data.resultset.ResultSet
    public void setAffectedByUris(Object obj, List<Uri> list) {
        if (list == null) {
            throw new IllegalArgumentException("input parameter uris can not be null");
        } else if (obj instanceof Context) {
            synchronized (this.urisObserverLock) {
                if (!(this.dataAbilityHelper == null || this.urisObserver == null || this.uris == null)) {
                    for (Uri uri : this.uris) {
                        this.dataAbilityHelper.unregisterObserver(uri, this.urisObserver);
                    }
                }
                this.dataAbilityHelper = DataAbilityHelper.creator((Context) obj);
                this.uris = list;
                if (this.urisObserver == null) {
                    this.urisObserver = new UrisObserver(this);
                }
                for (Uri uri2 : list) {
                    this.dataAbilityHelper.registerObserver(uri2, this.urisObserver);
                }
            }
        } else {
            throw new IllegalArgumentException("input parameter context must instanceof Context");
        }
    }

    @Override // ohos.data.resultset.ResultSet
    public List<Uri> getAffectedByUris() {
        List<Uri> list;
        synchronized (this.urisObserverLock) {
            list = this.uris;
        }
        return list;
    }

    @Override // ohos.data.resultset.ResultSet
    public void registerObserver(DataObserver dataObserver) {
        this.dataObservable.add(dataObserver);
    }

    @Override // ohos.data.resultset.ResultSet
    public void unregisterObserver(DataObserver dataObserver) {
        this.dataObservable.remove(dataObserver);
    }

    @Override // ohos.data.resultset.ResultSet
    public void close() {
        this.isClosed = true;
        this.dataObservable.removeAll();
        synchronized (this.urisObserverLock) {
            if (!(this.dataAbilityHelper == null || this.urisObserver == null || this.uris == null)) {
                for (Uri uri : this.uris) {
                    this.dataAbilityHelper.unregisterObserver(uri, this.urisObserver);
                }
            }
        }
    }

    @Override // ohos.data.resultset.ResultSet
    public boolean isClosed() {
        return this.isClosed;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        if (!this.isClosed) {
            close();
        }
    }

    /* access modifiers changed from: protected */
    public void notifyChange() {
        HiLog.info(LABEL, "notify change", new Object[0]);
        this.dataObservable.notifyObservers();
    }

    /* access modifiers changed from: private */
    public class UrisObserver implements IDataAbilityObserver {
        WeakReference<AbsResultSet> resultSet;

        public UrisObserver(AbsResultSet absResultSet) {
            this.resultSet = new WeakReference<>(absResultSet);
        }

        @Override // ohos.aafwk.ability.IDataAbilityObserver
        public void onChange() {
            AbsResultSet absResultSet = this.resultSet.get();
            if (absResultSet != null) {
                absResultSet.notifyChange();
            }
        }
    }
}
