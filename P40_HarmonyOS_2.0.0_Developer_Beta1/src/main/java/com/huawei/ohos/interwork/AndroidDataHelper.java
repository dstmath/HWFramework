package com.huawei.ohos.interwork;

import java.util.ArrayList;
import java.util.Iterator;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.ability.DataAbilityResult;
import ohos.aafwk.ability.IDataAbilityObserver;
import ohos.aafwk.ability.OperationExecuteException;
import ohos.app.Context;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.net.UriConverter;
import ohos.utils.net.Uri;

public class AndroidDataHelper {
    private static final String SCHEME_HARMONY = "dataability";
    private DataAbilityHelper dataHelper;

    private AndroidDataHelper(Context context) {
        this.dataHelper = DataAbilityHelper.creator(context);
    }

    private AndroidDataHelper(Context context, Uri uri) {
        this.dataHelper = DataAbilityHelper.creator(context, uri);
    }

    private AndroidDataHelper(Context context, Uri uri, boolean z) {
        this.dataHelper = DataAbilityHelper.creator(context, uri, z);
    }

    private static Uri getRealHarmonyContentUir(Uri uri) {
        return (uri != null && !SCHEME_HARMONY.equals(uri.getScheme())) ? UriConverter.convertToZidaneContentUri(UriConverter.convertToAndroidUri(uri), "") : uri;
    }

    public static AndroidDataHelper creator(Context context) {
        return new AndroidDataHelper(context);
    }

    public static AndroidDataHelper creator(Context context, Uri uri) {
        return new AndroidDataHelper(context, getRealHarmonyContentUir(uri));
    }

    public static AndroidDataHelper creator(Context context, Uri uri, boolean z) {
        return new AndroidDataHelper(context, getRealHarmonyContentUir(uri), z);
    }

    public boolean release() {
        DataAbilityHelper dataAbilityHelper = this.dataHelper;
        if (dataAbilityHelper == null) {
            return true;
        }
        if (!dataAbilityHelper.release()) {
            return false;
        }
        this.dataHelper = null;
        return true;
    }

    public void registerObserver(Uri uri, IDataAbilityObserver iDataAbilityObserver) throws IllegalArgumentException {
        DataAbilityHelper dataAbilityHelper = this.dataHelper;
        if (dataAbilityHelper != null) {
            dataAbilityHelper.registerObserver(getRealHarmonyContentUir(uri), iDataAbilityObserver);
        }
    }

    public void notifyChange(Uri uri) throws IllegalArgumentException {
        DataAbilityHelper dataAbilityHelper = this.dataHelper;
        if (dataAbilityHelper != null) {
            dataAbilityHelper.notifyChange(getRealHarmonyContentUir(uri));
        }
    }

    public void unregisterObserver(Uri uri, IDataAbilityObserver iDataAbilityObserver) throws IllegalArgumentException {
        DataAbilityHelper dataAbilityHelper = this.dataHelper;
        if (dataAbilityHelper != null) {
            dataAbilityHelper.unregisterObserver(getRealHarmonyContentUir(uri), iDataAbilityObserver);
        }
    }

    public int insert(Uri uri, ValuesBucket valuesBucket) throws DataAbilityRemoteException {
        DataAbilityHelper dataAbilityHelper = this.dataHelper;
        if (dataAbilityHelper != null) {
            return dataAbilityHelper.insert(getRealHarmonyContentUir(uri), valuesBucket);
        }
        return 0;
    }

    public int batchInsert(Uri uri, ValuesBucket[] valuesBucketArr) throws DataAbilityRemoteException {
        DataAbilityHelper dataAbilityHelper = this.dataHelper;
        if (dataAbilityHelper != null) {
            return dataAbilityHelper.batchInsert(getRealHarmonyContentUir(uri), valuesBucketArr);
        }
        return 0;
    }

    public int delete(Uri uri, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        DataAbilityHelper dataAbilityHelper = this.dataHelper;
        if (dataAbilityHelper != null) {
            return dataAbilityHelper.delete(getRealHarmonyContentUir(uri), dataAbilityPredicates);
        }
        return 0;
    }

    public int update(Uri uri, ValuesBucket valuesBucket, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        DataAbilityHelper dataAbilityHelper = this.dataHelper;
        if (dataAbilityHelper != null) {
            return dataAbilityHelper.update(getRealHarmonyContentUir(uri), valuesBucket, dataAbilityPredicates);
        }
        return 0;
    }

    public ResultSet query(Uri uri, String[] strArr, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        if (this.dataHelper == null) {
            return null;
        }
        return this.dataHelper.query(getRealHarmonyContentUir(uri), strArr, dataAbilityPredicates);
    }

    public DataAbilityResult[] executeBatch(Uri uri, ArrayList<DataAbilityOperation> arrayList) throws DataAbilityRemoteException, OperationExecuteException {
        if (this.dataHelper == null) {
            return new DataAbilityResult[0];
        }
        if (arrayList != null) {
            ArrayList<DataAbilityOperation> arrayList2 = new ArrayList<>();
            Iterator<DataAbilityOperation> it = arrayList.iterator();
            while (it.hasNext()) {
                DataAbilityOperation next = it.next();
                if (next != null) {
                    arrayList2.add(new DataAbilityOperation(next, getRealHarmonyContentUir(next.getUri())));
                }
            }
            return this.dataHelper.executeBatch(getRealHarmonyContentUir(uri), arrayList2);
        }
        throw new IllegalArgumentException("operations is illegal, execute batch failed");
    }
}
