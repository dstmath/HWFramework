package ohos.aafwk.ability;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ohos.app.Context;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.global.resource.RawFileDescriptor;
import ohos.tools.Bytrace;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public class DataAbilityHelper {
    private static final String MAKE_PERMISSION_TRACE = "provider makePersistentUriPermission";
    private static final String SCHEME_HARMONY = "dataability";
    private final Object OPERATOR_LOCK = new Object();
    private Context context;
    private IDataAbility dataAbility;
    private Map<IDataAbilityObserver, IDataAbility> registerMap = new HashMap();
    private Uri uri;

    private DataAbilityHelper(Context context2) {
        this.context = context2;
    }

    private DataAbilityHelper(Context context2, Uri uri2, IDataAbility iDataAbility) {
        this.context = context2;
        this.uri = uri2;
        this.dataAbility = iDataAbility;
    }

    public Context getContext() {
        return this.context;
    }

    public static DataAbilityHelper creator(Context context2) {
        return new DataAbilityHelper(context2);
    }

    public static DataAbilityHelper creator(Context context2, Uri uri2) {
        return creator(context2, uri2, false);
    }

    public static DataAbilityHelper creator(Context context2, Uri uri2, boolean z) {
        if (uri2 == null || context2 == null || !SCHEME_HARMONY.equals(uri2.getScheme())) {
            return null;
        }
        Bytrace.startTrace(2147483648L, "DataAbility Create");
        IDataAbility dataAbility2 = context2.getDataAbility(uri2, z);
        if (dataAbility2 == null) {
            return null;
        }
        Bytrace.finishTrace(2147483648L, "DataAbility Create");
        return new DataAbilityHelper(context2, uri2, dataAbility2);
    }

    public boolean release() {
        if (this.uri == null) {
            return true;
        }
        if (!this.context.releaseDataAbility(this.dataAbility)) {
            return false;
        }
        this.dataAbility = null;
        this.uri = null;
        return true;
    }

    public void registerObserver(Uri uri2, IDataAbilityObserver iDataAbilityObserver) throws IllegalArgumentException {
        IDataAbility iDataAbility;
        checkUriParam(uri2);
        if (iDataAbilityObserver != null) {
            synchronized (this.OPERATOR_LOCK) {
                if (this.uri == null) {
                    iDataAbility = this.registerMap.get(iDataAbilityObserver);
                    if (iDataAbility == null) {
                        iDataAbility = this.context.getDataAbility(uri2);
                        if (iDataAbility != null) {
                            this.registerMap.put(iDataAbilityObserver, iDataAbility);
                        } else {
                            throw new IllegalArgumentException("DataAbility register failed, there is no corresponding dataAbility");
                        }
                    }
                } else {
                    iDataAbility = this.dataAbility;
                }
            }
            iDataAbility.registerObserver(uri2, iDataAbilityObserver);
            return;
        }
        throw new IllegalArgumentException("DataAbility register failed, dataObserver argument is null");
    }

    public void notifyChange(Uri uri2) throws IllegalArgumentException {
        checkUriParam(uri2);
        if (this.dataAbility == null) {
            this.dataAbility = this.context.getDataAbility(uri2);
            if (this.dataAbility == null) {
                throw new IllegalArgumentException("DataAbility notify failed, dataAbility is illegal");
            }
        }
        try {
            this.dataAbility.notifyChange(uri2);
        } finally {
            if (this.uri == null) {
                this.context.releaseDataAbility(this.dataAbility);
                this.dataAbility = null;
            }
        }
    }

    public void unregisterObserver(Uri uri2, IDataAbilityObserver iDataAbilityObserver) throws IllegalArgumentException {
        IDataAbility iDataAbility;
        checkUriParam(uri2);
        if (iDataAbilityObserver != null) {
            synchronized (this.OPERATOR_LOCK) {
                if (this.uri == null) {
                    iDataAbility = this.registerMap.get(iDataAbilityObserver);
                    this.registerMap.remove(iDataAbilityObserver);
                } else {
                    iDataAbility = this.dataAbility;
                }
                if (iDataAbility != null) {
                    iDataAbility.unregisterObserver(iDataAbilityObserver);
                    this.context.releaseDataAbility(iDataAbility);
                    return;
                }
                return;
            }
        }
        throw new IllegalArgumentException("DataAbility unregister failed, dataObserver argument is null");
    }

    public int insert(Uri uri2, ValuesBucket valuesBucket) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        Bytrace.startTrace(2147483648L, "providerInsert");
        if (this.uri == null) {
            this.dataAbility = this.context.getDataAbility(uri2, true);
        }
        IDataAbility iDataAbility = this.dataAbility;
        if (iDataAbility != null) {
            try {
                return iDataAbility.insert(uri2, valuesBucket);
            } finally {
                if (this.uri == null) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = null;
                }
                Bytrace.finishTrace(2147483648L, "providerInsert");
            }
        } else {
            Bytrace.finishTrace(2147483648L, "providerInsert");
            throw new IllegalStateException("No corresponding dataAbility, insert failed");
        }
    }

    public int batchInsert(Uri uri2, ValuesBucket[] valuesBucketArr) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        if (valuesBucketArr != null) {
            Bytrace.startTrace(2147483648L, "providerBatchInsert");
            if (this.uri == null) {
                this.dataAbility = this.context.getDataAbility(uri2, true);
            }
            IDataAbility iDataAbility = this.dataAbility;
            if (iDataAbility != null) {
                try {
                    return iDataAbility.batchInsert(uri2, valuesBucketArr);
                } finally {
                    if (this.uri == null) {
                        this.context.releaseDataAbility(this.dataAbility);
                        this.dataAbility = null;
                    }
                    Bytrace.finishTrace(2147483648L, "providerBatchInsert");
                }
            } else {
                Bytrace.finishTrace(2147483648L, "providerBatchInsert");
                throw new IllegalStateException("No corresponding dataAbility, batch insert failed");
            }
        } else {
            throw new IllegalArgumentException("Input uri and values can not be null.");
        }
    }

    public int delete(Uri uri2, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        Bytrace.startTrace(2147483648L, "providerDelete");
        if (this.uri == null) {
            this.dataAbility = this.context.getDataAbility(uri2, true);
        }
        IDataAbility iDataAbility = this.dataAbility;
        if (iDataAbility != null) {
            try {
                return iDataAbility.delete(uri2, dataAbilityPredicates);
            } finally {
                if (this.uri == null) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = null;
                }
                Bytrace.finishTrace(2147483648L, "providerDelete");
            }
        } else {
            Bytrace.finishTrace(2147483648L, "providerDelete");
            throw new IllegalStateException("No corresponding dataAbility, delete failed");
        }
    }

    public int update(Uri uri2, ValuesBucket valuesBucket, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        Bytrace.startTrace(2147483648L, "providerUpdate");
        if (this.uri == null) {
            this.dataAbility = this.context.getDataAbility(uri2, false);
        }
        IDataAbility iDataAbility = this.dataAbility;
        if (iDataAbility != null) {
            try {
                return iDataAbility.update(uri2, valuesBucket, dataAbilityPredicates);
            } finally {
                if (this.uri == null) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = null;
                }
                Bytrace.finishTrace(2147483648L, "providerUpdate");
            }
        } else {
            Bytrace.finishTrace(2147483648L, "providerUpdate");
            throw new IllegalStateException("No corresponding dataAbility, update failed");
        }
    }

    public ResultSet query(Uri uri2, String[] strArr, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        Bytrace.startTrace(2147483648L, "providerQuery");
        if (this.uri == null) {
            this.dataAbility = this.context.getDataAbility(uri2, false);
            IDataAbility iDataAbility = this.dataAbility;
            if (iDataAbility != null) {
                try {
                    return iDataAbility.query(uri2, strArr, dataAbilityPredicates);
                } catch (DataAbilityDeadException unused) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = this.context.getDataAbility(uri2, true);
                    return this.dataAbility.query(uri2, strArr, dataAbilityPredicates);
                } finally {
                    Bytrace.finishTrace(2147483648L, "providerQuery");
                }
            } else {
                Bytrace.finishTrace(2147483648L, "providerQuery");
                throw new IllegalStateException("No corresponding dataAbility, query failed");
            }
        } else {
            try {
                return this.dataAbility.query(uri2, strArr, dataAbilityPredicates);
            } finally {
                Bytrace.finishTrace(2147483648L, "providerQuery");
            }
        }
    }

    public DataAbilityResult[] executeBatch(Uri uri2, ArrayList<DataAbilityOperation> arrayList) throws DataAbilityRemoteException, OperationExecuteException {
        checkUriParam(uri2);
        if (arrayList != null) {
            Bytrace.startTrace(2147483648L, "providerExecuteBatch");
            if (this.uri == null) {
                this.dataAbility = this.context.getDataAbility(uri2, true);
            }
            IDataAbility iDataAbility = this.dataAbility;
            if (iDataAbility != null) {
                try {
                    return iDataAbility.executeBatch(arrayList);
                } finally {
                    if (this.uri == null) {
                        this.context.releaseDataAbility(this.dataAbility);
                        this.dataAbility = null;
                    }
                    Bytrace.finishTrace(2147483648L, "providerExecuteBatch");
                }
            } else {
                Bytrace.finishTrace(2147483648L, "providerExecuteBatch");
                throw new IllegalStateException("No corresponding dataAbility, execute batch failed");
            }
        } else {
            throw new IllegalArgumentException("operations is illegal, execute batch failed");
        }
    }

    public FileDescriptor openFile(Uri uri2, String str) throws FileNotFoundException, DataAbilityRemoteException {
        FileDescriptor openFile;
        checkUriParam(uri2);
        checkParamNotNull(str, "Parameter mode is null.");
        Bytrace.startTrace(2147483648L, "providerOpenFile");
        if (this.uri == null) {
            this.dataAbility = this.context.getDataAbility(uri2, false);
            IDataAbility iDataAbility = this.dataAbility;
            if (iDataAbility != null) {
                try {
                    openFile = iDataAbility.openFile(uri2, str);
                } catch (DataAbilityRemoteException unused) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = this.context.getDataAbility(uri2, true);
                    openFile = this.dataAbility.openFile(uri2, str);
                } catch (Throwable th) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = null;
                    Bytrace.finishTrace(2147483648L, "providerOpenFile");
                    throw th;
                }
                this.context.releaseDataAbility(this.dataAbility);
                this.dataAbility = null;
                Bytrace.finishTrace(2147483648L, "providerOpenFile");
                return openFile;
            }
            Bytrace.finishTrace(2147483648L, "providerOpenFile");
            throw new IllegalStateException("No corresponding dataAbility, open file failed");
        }
        try {
            return this.dataAbility.openFile(uri2, str);
        } finally {
            Bytrace.finishTrace(2147483648L, "providerOpenFile");
        }
    }

    public RawFileDescriptor openRawFile(Uri uri2, String str) throws FileNotFoundException, DataAbilityRemoteException {
        RawFileDescriptor openRawFile;
        checkUriParam(uri2);
        checkParamNotNull(str, "openRawFile parameter mode is null.");
        Bytrace.startTrace(2147483648L, "providerOpenRawFile");
        if (this.uri == null) {
            this.dataAbility = this.context.getDataAbility(uri2, false);
            IDataAbility iDataAbility = this.dataAbility;
            if (iDataAbility != null) {
                try {
                    openRawFile = iDataAbility.openRawFile(uri2, str);
                } catch (DataAbilityRemoteException unused) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = this.context.getDataAbility(uri2, true);
                    openRawFile = this.dataAbility.openRawFile(uri2, str);
                } catch (Throwable th) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = null;
                    Bytrace.finishTrace(2147483648L, "providerOpenRawFile");
                    throw th;
                }
                this.context.releaseDataAbility(this.dataAbility);
                this.dataAbility = null;
                Bytrace.finishTrace(2147483648L, "providerOpenRawFile");
                return openRawFile;
            }
            Bytrace.finishTrace(2147483648L, "providerOpenRawFile");
            throw new IllegalStateException("No corresponding dataAbility, open asset file failed");
        }
        try {
            return this.dataAbility.openRawFile(uri2, str);
        } finally {
            Bytrace.finishTrace(2147483648L, "providerOpenRawFile");
        }
    }

    public void makePersistentUriPermission(Uri uri2, int i) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        Bytrace.startTrace(2147483648L, MAKE_PERMISSION_TRACE);
        if (this.uri == null) {
            this.dataAbility = this.context.getDataAbility(uri2, false);
            IDataAbility iDataAbility = this.dataAbility;
            if (iDataAbility != null) {
                try {
                    iDataAbility.makePersistentUriPermission(uri2, i);
                } catch (DataAbilityRemoteException unused) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = this.context.getDataAbility(uri2, true);
                    this.dataAbility.makePersistentUriPermission(uri2, i);
                } catch (Throwable th) {
                    this.context.releaseDataAbility(this.dataAbility);
                    this.dataAbility = null;
                    Bytrace.finishTrace(2147483648L, MAKE_PERMISSION_TRACE);
                    throw th;
                }
                this.context.releaseDataAbility(this.dataAbility);
                this.dataAbility = null;
                Bytrace.finishTrace(2147483648L, MAKE_PERMISSION_TRACE);
                return;
            }
            Bytrace.finishTrace(2147483648L, MAKE_PERMISSION_TRACE);
            throw new IllegalStateException("No corresponding dataAbility, make Persistable UriPermission failed");
        }
        try {
            this.dataAbility.makePersistentUriPermission(uri2, i);
        } finally {
            Bytrace.finishTrace(2147483648L, MAKE_PERMISSION_TRACE);
        }
    }

    public String[] getFileTypes(Uri uri2, String str) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        checkParamNotNull(str, "Parameter mimeTypeFilter is null");
        this.dataAbility = getDataAbility(uri2);
        if (this.dataAbility == null) {
            return null;
        }
        Bytrace.startTrace(2147483648L, "providerGetFileTypes");
        try {
            return this.dataAbility.getFileTypes(uri2, str);
        } finally {
            if (this.uri == null) {
                this.context.releaseDataAbility(this.dataAbility);
                this.dataAbility = null;
            }
            Bytrace.finishTrace(2147483648L, "providerGetFileTypes");
        }
    }

    public PacMap call(Uri uri2, String str, String str2, PacMap pacMap) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        checkParamNotNull(str, "Parameter method is null");
        this.dataAbility = getDataAbility(uri2);
        if (this.dataAbility == null) {
            return null;
        }
        Bytrace.startTrace(2147483648L, "providerCall");
        try {
            return this.dataAbility.call(str, str2, pacMap);
        } finally {
            if (this.uri == null) {
                this.context.releaseDataAbility(this.dataAbility);
                this.dataAbility = null;
            }
            Bytrace.finishTrace(2147483648L, "providerCall");
        }
    }

    public String getType(Uri uri2) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        this.dataAbility = getDataAbility(uri2);
        if (this.dataAbility == null) {
            return null;
        }
        Bytrace.startTrace(2147483648L, "providerGetType");
        try {
            return this.dataAbility.getType(uri2);
        } finally {
            if (this.uri == null) {
                this.context.releaseDataAbility(this.dataAbility);
                this.dataAbility = null;
            }
            Bytrace.finishTrace(2147483648L, "providerGetType");
        }
    }

    public Uri normalizeUri(Uri uri2) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        this.dataAbility = getDataAbility(uri2);
        if (this.dataAbility == null) {
            return null;
        }
        Bytrace.startTrace(2147483648L, "providerNormalizeUri");
        try {
            return this.dataAbility.normalizeUri(uri2);
        } finally {
            if (this.uri == null) {
                this.context.releaseDataAbility(this.dataAbility);
                this.dataAbility = null;
            }
            Bytrace.finishTrace(2147483648L, "providerNormalizeUri");
        }
    }

    public Uri denormalizeUri(Uri uri2) throws DataAbilityRemoteException {
        checkUriParam(uri2);
        this.dataAbility = getDataAbility(uri2);
        if (this.dataAbility == null) {
            return null;
        }
        Bytrace.startTrace(2147483648L, "providerDenormalizeUri");
        try {
            return this.dataAbility.denormalizeUri(uri2);
        } finally {
            if (this.uri == null) {
                this.context.releaseDataAbility(this.dataAbility);
                this.dataAbility = null;
            }
            Bytrace.finishTrace(2147483648L, "providerDenormalizeUri");
        }
    }

    private IDataAbility getDataAbility(Uri uri2) {
        if (this.uri == null) {
            return this.context.getDataAbility(uri2, true);
        }
        return this.dataAbility;
    }

    private String checkParamNotNull(String str, String str2) {
        if (str != null) {
            return str;
        }
        throw new NullPointerException(String.valueOf(str2));
    }

    private void checkUriParam(Uri uri2) {
        if (uri2 != null) {
            checkZidaneUri(uri2);
            Uri uri3 = this.uri;
            if (uri3 != null) {
                checkZidaneUri(uri3);
                if (!((String) this.uri.getDecodedPathList().get(0)).equals(uri2.getDecodedPathList().get(0))) {
                    throw new IllegalArgumentException("this uri paths first segment is not equal to uri paths first segment.");
                }
                return;
            }
            return;
        }
        throw new NullPointerException("Parameter uri is null.");
    }

    private void checkZidaneUri(Uri uri2) {
        if (!SCHEME_HARMONY.equals(uri2.getScheme())) {
            throw new IllegalArgumentException("Scheme is illegal.");
        } else if (uri2.getDecodedPathList() == null || uri2.getDecodedPathList().isEmpty()) {
            throw new IllegalArgumentException("paths is illegal.");
        } else if (uri2.getDecodedPathList().get(0) == null || "".equals(uri2.getDecodedPathList().get(0))) {
            throw new IllegalArgumentException("paths first segment is illegal.");
        }
    }
}
