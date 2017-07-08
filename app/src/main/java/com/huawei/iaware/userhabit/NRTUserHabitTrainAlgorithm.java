package com.huawei.iaware.userhabit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteException;
import android.rms.iaware.AwareConstant.Database;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NRTUserHabitTrainAlgorithm {
    private static final int APPLY_BATCH_COUNT = 1000;
    public static final String TAG = "NRTUserHabitTrainAlgorithm";
    private static final long TIME_LIMIT = 3600000;
    private static final long TRANS_TIME_LIMIT = 600000;
    private static final String[] mFilterApp = null;
    private ContentResolver mContentResolver;
    private Context mContext;
    private ArraySet<String> mFilterAppSet;
    private LinkedHashMap<Integer, String> mIdToPkgNameMap;
    private LinkedHashMap<String, Integer> mPkgNameToIdMap;
    private final ArrayMap<String, Integer> mTodayUsageCount;
    private ArrayList<ArrayList<Integer>> mTransProMatrix;
    private ArrayList<ArrayList<Integer>> mTransProMatrixBackup;
    private ArrayMap<String, Integer> mUsageCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.iaware.userhabit.NRTUserHabitTrainAlgorithm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.iaware.userhabit.NRTUserHabitTrainAlgorithm.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.iaware.userhabit.NRTUserHabitTrainAlgorithm.<clinit>():void");
    }

    public NRTUserHabitTrainAlgorithm(Context context) {
        this.mTransProMatrix = new ArrayList();
        this.mTransProMatrixBackup = new ArrayList();
        this.mPkgNameToIdMap = new LinkedHashMap();
        this.mIdToPkgNameMap = new LinkedHashMap();
        this.mUsageCount = new ArrayMap();
        this.mTodayUsageCount = new ArrayMap();
        this.mFilterAppSet = new ArraySet();
        this.mContext = null;
        this.mContentResolver = null;
        if (context != null) {
            this.mContext = context;
            this.mContentResolver = context.getContentResolver();
        }
    }

    private void initFilterPkg() {
        addDefaultApp();
        addHomeApp();
    }

    private void addFilterPkg(String str) {
        synchronized (this.mFilterAppSet) {
            this.mFilterAppSet.add(str);
        }
    }

    private void addDefaultApp() {
        synchronized (this.mFilterAppSet) {
            for (Object add : mFilterApp) {
                this.mFilterAppSet.add(add);
            }
        }
    }

    private void addHomeApp() {
        if (this.mContext != null) {
            PackageManager packageManager = this.mContext.getPackageManager();
            if (packageManager != null) {
                List queryIntentActivities = packageManager.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), 0);
                if (queryIntentActivities != null) {
                    for (int i = 0; i < queryIntentActivities.size(); i++) {
                        String str = ((ResolveInfo) queryIntentActivities.get(i)).activityInfo.packageName;
                        if (str != null) {
                            addFilterPkg(str);
                        }
                    }
                }
            }
        }
    }

    private void clearFilterPkg() {
        synchronized (this.mFilterAppSet) {
            this.mFilterAppSet.clear();
        }
    }

    public boolean containsFilterPkg(String str) {
        synchronized (this.mFilterAppSet) {
            if (this.mFilterAppSet.contains(str)) {
                return true;
            }
            return false;
        }
    }

    private void init(int i) {
        IAwareHabitUtils.loadPkgInfo(this.mContentResolver, this.mPkgNameToIdMap, this.mIdToPkgNameMap, this.mUsageCount, i);
        IAwareHabitUtils.loadAppAssociateInfo(this.mContentResolver, this.mPkgNameToIdMap, this.mTransProMatrix, i);
    }

    private void clearData() {
        this.mPkgNameToIdMap.clear();
        this.mIdToPkgNameMap.clear();
        this.mTransProMatrix.clear();
        this.mUsageCount.clear();
    }

    private void checkModelData() {
        if (this.mTransProMatrix.size() != this.mPkgNameToIdMap.size() || this.mTransProMatrix.size() != this.mUsageCount.size()) {
            AwareLog.e(TAG, "the model data is wrong, clear the table");
            IAwareHabitUtils.deleteTable(this.mContentResolver);
            clearData();
        }
    }

    private void initTransProMatrix(int i, int i2) {
        if (i - i2 > 0) {
            for (int i3 = 0; i3 < i2; i3++) {
                for (int i4 = i2; i4 < i; i4++) {
                    ((ArrayList) this.mTransProMatrix.get(i3)).add(Integer.valueOf(0));
                }
            }
            while (i2 < i) {
                ArrayList arrayList = new ArrayList();
                for (int i5 = 0; i5 < i; i5++) {
                    arrayList.add(Integer.valueOf(0));
                }
                this.mTransProMatrix.add(arrayList);
                i2++;
            }
        }
    }

    private void updateTransProMatrix(List<Entry<Integer, Long>> list) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            int intValue = ((Integer) ((Entry) list.get(i)).getKey()).intValue();
            if (this.mIdToPkgNameMap.containsKey(Integer.valueOf(intValue)) && !containsFilterPkg((String) this.mIdToPkgNameMap.get(Integer.valueOf(intValue)))) {
                arrayList.add(list.get(i));
            }
        }
        for (int i2 = 0; i2 < arrayList.size() - 1; i2++) {
            Object obj;
            long longValue = ((Long) ((Entry) arrayList.get(i2 + 1)).getValue()).longValue() - ((Long) ((Entry) arrayList.get(i2)).getValue()).longValue();
            if (longValue > TRANS_TIME_LIMIT) {
                obj = 1;
            } else {
                obj = null;
            }
            if (obj == null) {
                if ((longValue <= 0 ? 1 : null) == null) {
                    ArrayList arrayList2 = (ArrayList) this.mTransProMatrix.get(((Integer) ((Entry) arrayList.get(i2)).getKey()).intValue());
                    arrayList2.set(((Integer) ((Entry) arrayList.get(i2 + 1)).getKey()).intValue(), Integer.valueOf(((Integer) arrayList2.get(((Integer) ((Entry) arrayList.get(i2 + 1)).getKey()).intValue())).intValue() + 1));
                    this.mTransProMatrix.set(((Integer) ((Entry) arrayList.get(i2)).getKey()).intValue(), arrayList2);
                }
            }
        }
    }

    private void updateUsageCount(List<Entry<Integer, Long>> list, Set<Integer> set) {
        for (int i = 0; i < list.size(); i++) {
            int intValue = ((Integer) ((Entry) list.get(i)).getKey()).intValue();
            set.add(Integer.valueOf(intValue));
            String str = (String) this.mIdToPkgNameMap.get(Integer.valueOf(intValue));
            if (this.mUsageCount.containsKey(str)) {
                this.mUsageCount.put(str, Integer.valueOf(((Integer) this.mUsageCount.get(str)).intValue() + 1));
            } else {
                this.mUsageCount.put(str, Integer.valueOf(1));
            }
            if (!containsFilterPkg(str)) {
                if (this.mTodayUsageCount.containsKey(str)) {
                    this.mTodayUsageCount.put(str, Integer.valueOf(((Integer) this.mTodayUsageCount.get(str)).intValue() + 1));
                } else {
                    this.mTodayUsageCount.put(str, Integer.valueOf(1));
                }
            }
        }
    }

    void prepare(boolean z, List<Integer> list) {
        if (z) {
            IAwareHabitUtils.decreaseAppCount(this.mContentResolver);
        }
        IAwareHabitUtils.updateReInstallPkgNameInfo(this.mContentResolver, list);
        IAwareHabitUtils.updatePkgNameTable(this.mContext);
        initFilterPkg();
    }

    boolean train(int i) {
        init(i);
        checkModelData();
        int size = this.mPkgNameToIdMap.size();
        List arrayList = new ArrayList();
        IAwareHabitUtils.loadUserdataInfo(this.mContentResolver, this.mPkgNameToIdMap, this.mIdToPkgNameMap, arrayList, i);
        if (arrayList.isEmpty()) {
            clearData();
            AwareLog.i(TAG, "no need to train,habit train end");
            return false;
        }
        backupData();
        int size2 = this.mPkgNameToIdMap.size();
        initTransProMatrix(size2, size);
        updateTransProMatrix(arrayList);
        Set arraySet = new ArraySet();
        updateUsageCount(arrayList, arraySet);
        storeData(size2, size, arraySet, i);
        clearData();
        return true;
    }

    private void backupData() {
        for (int i = 0; i < this.mTransProMatrix.size(); i++) {
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < ((ArrayList) this.mTransProMatrix.get(i)).size(); i2++) {
                arrayList.add(((ArrayList) this.mTransProMatrix.get(i)).get(i2));
            }
            this.mTransProMatrixBackup.add(arrayList);
        }
    }

    Map<String, Integer> getTodayUsageInfo() {
        return this.mTodayUsageCount;
    }

    void deleteRawData() {
        if (this.mContentResolver != null) {
            try {
                this.mContentResolver.delete(Database.PKGRECORD_URI, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: PkgRecord deleteRawData " + e);
            }
            try {
                this.mContentResolver.delete(Database.USERDATA_URI, null, null);
            } catch (SQLiteException e2) {
                AwareLog.e(TAG, "Error: UserData deleteRawData " + e2);
            }
        }
        clearFilterPkg();
    }

    void clearTodayUsageCount() {
        this.mTodayUsageCount.clear();
    }

    private void insertPkgNameData(int i, int i2, int i3) {
        int i4 = i - i2;
        for (int i5 = 0; i5 < i4; i5++) {
            String str = (String) this.mIdToPkgNameMap.get(Integer.valueOf(i2 + i5));
            ContentValues contentValues = new ContentValues();
            contentValues.put("appPkgName", str);
            contentValues.put("userID", Integer.valueOf(i3));
            try {
                this.mContentResolver.insert(Database.PKGNAME_URI, contentValues);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertPkgNameData " + e);
            }
        }
    }

    private void insertTransProMatrixData(int i) {
        int i2;
        int i3;
        String str;
        String str2;
        int i4 = 0;
        int size = this.mTransProMatrixBackup.size();
        List arrayList = new ArrayList();
        for (i2 = 0; i2 < size; i2++) {
            for (i3 = 0; i3 < size; i3++) {
                ContentValues contentValues;
                int intValue = ((Integer) ((ArrayList) this.mTransProMatrix.get(i2)).get(i3)).intValue();
                int intValue2 = ((Integer) ((ArrayList) this.mTransProMatrixBackup.get(i2)).get(i3)).intValue();
                if (intValue != 0 && intValue - intValue2 > 0) {
                    str = (String) this.mIdToPkgNameMap.get(Integer.valueOf(i2));
                    str2 = (String) this.mIdToPkgNameMap.get(Integer.valueOf(i3));
                    if (intValue2 <= 0) {
                        contentValues = new ContentValues();
                        contentValues.put("srcPkgName", str);
                        contentValues.put("dstPkgName", str2);
                        contentValues.put("transitionTimes", Integer.valueOf(intValue));
                        contentValues.put("userID", Integer.valueOf(i));
                        arrayList.add(contentValues);
                    } else {
                        contentValues = new ContentValues();
                        contentValues.put("transitionTimes", Integer.valueOf(intValue));
                        String str3 = "srcPkgName=? AND dstPkgName=? AND userID=?";
                        try {
                            this.mContentResolver.update(Database.ASSOCIATE_URI, contentValues, str3, new String[]{str, str2, String.valueOf(i)});
                        } catch (SQLiteException e) {
                            AwareLog.e(TAG, "Error: insert Associate " + e);
                        }
                    }
                }
            }
        }
        for (i3 = size; i3 < this.mTransProMatrix.size(); i3++) {
            for (i2 = 0; i2 < ((ArrayList) this.mTransProMatrix.get(0)).size(); i2++) {
                intValue = ((Integer) ((ArrayList) this.mTransProMatrix.get(i3)).get(i2)).intValue();
                if (intValue != 0) {
                    str = (String) this.mIdToPkgNameMap.get(Integer.valueOf(i3));
                    str2 = (String) this.mIdToPkgNameMap.get(Integer.valueOf(i2));
                    contentValues = new ContentValues();
                    contentValues.put("srcPkgName", str);
                    contentValues.put("dstPkgName", str2);
                    contentValues.put("transitionTimes", Integer.valueOf(intValue));
                    contentValues.put("userID", Integer.valueOf(i));
                    arrayList.add(contentValues);
                }
            }
        }
        while (i4 < size) {
            for (i3 = size; i3 < this.mTransProMatrix.size(); i3++) {
                i2 = ((Integer) ((ArrayList) this.mTransProMatrix.get(i4)).get(i3)).intValue();
                if (i2 != 0) {
                    str = (String) this.mIdToPkgNameMap.get(Integer.valueOf(i4));
                    str2 = (String) this.mIdToPkgNameMap.get(Integer.valueOf(i3));
                    ContentValues contentValues2 = new ContentValues();
                    contentValues2.put("srcPkgName", str);
                    contentValues2.put("dstPkgName", str2);
                    contentValues2.put("transitionTimes", Integer.valueOf(i2));
                    contentValues2.put("userID", Integer.valueOf(i));
                    arrayList.add(contentValues2);
                }
            }
            i4++;
        }
        bulkInsert(arrayList);
    }

    private void bulkInsert(List<ContentValues> list) {
        int i;
        int i2 = 0;
        int size = list.size() / APPLY_BATCH_COUNT;
        for (int i3 = 0; i3 < size; i3++) {
            ContentValues[] contentValuesArr = new ContentValues[APPLY_BATCH_COUNT];
            for (i = 0; i < APPLY_BATCH_COUNT; i++) {
                contentValuesArr[i] = (ContentValues) list.get((i3 * APPLY_BATCH_COUNT) + i);
            }
            try {
                this.mContentResolver.bulkInsert(Database.ASSOCIATE_URI, contentValuesArr);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: bulkInsert Associate " + e);
            }
        }
        i = list.size() % APPLY_BATCH_COUNT;
        ContentValues[] contentValuesArr2 = new ContentValues[i];
        while (i2 < i) {
            contentValuesArr2[i2] = (ContentValues) list.get((size * APPLY_BATCH_COUNT) + i2);
            i2++;
        }
        try {
            this.mContentResolver.bulkInsert(Database.ASSOCIATE_URI, contentValuesArr2);
        } catch (SQLiteException e2) {
            AwareLog.e(TAG, "Error: bulkInsert Associate " + e2);
        }
        this.mTransProMatrixBackup.clear();
    }

    private void insertUsageCountData(int i, int i2, Set<Integer> set, int i3) {
        for (int i4 = 0; i4 < i; i4++) {
            if (set.contains(Integer.valueOf(i4))) {
                int intValue = ((Integer) this.mUsageCount.get((String) this.mIdToPkgNameMap.get(Integer.valueOf(i4)))).intValue();
                ContentValues contentValues = new ContentValues();
                contentValues.put("totalUseTimes", Integer.valueOf(intValue));
                String str = "appPkgName=? AND userID=?";
                try {
                    this.mContentResolver.update(Database.PKGNAME_URI, contentValues, str, new String[]{r0, String.valueOf(i3)});
                } catch (SQLiteException e) {
                    AwareLog.e(TAG, "Error: insertUsageCountData " + e);
                }
            }
        }
    }

    private void storeData(int i, int i2, Set<Integer> set, int i3) {
        insertPkgNameData(i, i2, i3);
        insertTransProMatrixData(i3);
        insertUsageCountData(i, i2, set, i3);
    }
}
