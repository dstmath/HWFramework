package ohos.data.rdb.impl;

public class PrecompiledStatement {
    private SqliteStatementInfo info;
    private boolean isInCache;
    private boolean isInUse;
    private boolean isReadOnly;
    private int numParameters;
    private String sql;
    private long statementPtr;
    private int type;

    public String getSql() {
        return this.sql;
    }

    public void setSql(String str) {
        this.sql = str;
    }

    public long getStatementPtr() {
        return this.statementPtr;
    }

    public void setStatementPtr(long j) {
        this.statementPtr = j;
    }

    public boolean isInUse() {
        return this.isInUse;
    }

    public void setInUse(boolean z) {
        this.isInUse = z;
    }

    public boolean isInCache() {
        return this.isInCache;
    }

    public void setInCache(boolean z) {
        this.isInCache = z;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int i) {
        this.type = i;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public void setReadOnly(boolean z) {
        this.isReadOnly = z;
    }

    public int getNumParameters() {
        return this.numParameters;
    }

    public void setNumParameters(int i) {
        this.numParameters = i;
    }

    public SqliteStatementInfo getInfo() {
        return this.info;
    }

    public void setInfo(SqliteStatementInfo sqliteStatementInfo) {
        this.info = sqliteStatementInfo;
        this.isReadOnly = sqliteStatementInfo.isReadOnly();
        this.numParameters = sqliteStatementInfo.getNumParameters();
    }
}
