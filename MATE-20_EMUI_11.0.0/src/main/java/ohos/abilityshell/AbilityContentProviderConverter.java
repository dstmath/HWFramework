package ohos.abilityshell;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.os.Parcel;
import java.util.HashMap;
import java.util.Map;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.aafwk.ability.DataAbilityResult;
import ohos.appexecfwk.utils.AppLog;
import ohos.data.dataability.ContentProviderConverter;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.UriConverter;
import ohos.utils.net.Uri;

public class AbilityContentProviderConverter {
    private static final int INVALID_OBJECT_FLAG = 0;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static final int VALID_OBJECT_FLAG = 1;

    private AbilityContentProviderConverter() {
    }

    public static DataAbilityResult contentProviderResultToDataAbilityResult(ContentProviderResult contentProviderResult) {
        if (contentProviderResult != null) {
            Parcel obtain = Parcel.obtain();
            contentProviderResult.writeToParcel(obtain, 0);
            obtain.setDataPosition(0);
            Uri uri = null;
            android.net.Uri uri2 = obtain.readInt() != 0 ? (android.net.Uri) android.net.Uri.CREATOR.createFromParcel(obtain) : null;
            Integer valueOf = obtain.readInt() != 0 ? Integer.valueOf(obtain.readInt()) : null;
            obtain.recycle();
            if (uri2 != null) {
                uri = UriConverter.convertToZidaneContentUri(uri2, "");
            }
            return new DataAbilityResult(uri, valueOf);
        }
        AppLog.e(SHELL_LABEL, "contentProviderToDataAbilityResult: contentProviderResult cannot be null.", new Object[0]);
        throw new IllegalArgumentException("contentProviderResult cannot be null.");
    }

    public static ContentProviderResult dataAbilityResultToContentProviderResult(DataAbilityResult dataAbilityResult) {
        if (dataAbilityResult != null) {
            Uri uri = dataAbilityResult.getUri();
            Integer count = dataAbilityResult.getCount();
            android.net.Uri convertToAndroidContentUri = uri != null ? UriConverter.convertToAndroidContentUri(uri) : null;
            Parcel obtain = Parcel.obtain();
            if (convertToAndroidContentUri != null) {
                obtain.writeInt(1);
                convertToAndroidContentUri.writeToParcel(obtain, 0);
            } else {
                obtain.writeInt(0);
            }
            if (count != null) {
                obtain.writeInt(1);
                obtain.writeInt(count.intValue());
            } else {
                obtain.writeInt(0);
            }
            obtain.setDataPosition(0);
            ContentProviderResult contentProviderResult = new ContentProviderResult(obtain);
            obtain.recycle();
            return contentProviderResult;
        }
        AppLog.e(SHELL_LABEL, "dataAbilityToContentProviderResult: dataAbilityResult cannot be null.", new Object[0]);
        throw new IllegalArgumentException("dataAbilityResult cannot be null.");
    }

    public static ContentProviderOperation dataAbilityOperationToContentProviderOperation(DataAbilityOperation dataAbilityOperation) {
        ContentProviderOperation.Builder builder;
        if (dataAbilityOperation != null) {
            ValuesBucket valuesBucket = dataAbilityOperation.getValuesBucket();
            DataAbilityPredicates dataAbilityPredicates = dataAbilityOperation.getDataAbilityPredicates();
            int type = dataAbilityOperation.getType();
            android.net.Uri convertToAndroidContentUri = UriConverter.convertToAndroidContentUri(dataAbilityOperation.getUri());
            if (type == 1) {
                builder = ContentProviderOperation.newInsert(convertToAndroidContentUri);
            } else if (type == 2) {
                builder = ContentProviderOperation.newUpdate(convertToAndroidContentUri);
            } else if (type == 3) {
                builder = ContentProviderOperation.newDelete(convertToAndroidContentUri);
            } else if (type == 4) {
                builder = ContentProviderOperation.newAssertQuery(convertToAndroidContentUri);
            } else {
                AppLog.e(SHELL_LABEL, "dataAbilityOperationToContentProviderOperation: contentType is invalid.", new Object[0]);
                throw new IllegalArgumentException("contentType is invalid: " + type);
            }
            if (valuesBucket != null) {
                builder.withValues(ContentProviderConverter.valuesBucketToContentValues(valuesBucket));
            }
            if (dataAbilityPredicates != null) {
                builder.withSelection(ContentProviderConverter.dataAbilityPredicatesToSelection(dataAbilityPredicates), ContentProviderConverter.dataAbilityPredicatesToSelectionArgs(dataAbilityPredicates));
            }
            Integer expectedCount = dataAbilityOperation.getExpectedCount();
            if (expectedCount != null) {
                builder.withExpectedCount(expectedCount.intValue());
            }
            ValuesBucket valuesBucketReferences = dataAbilityOperation.getValuesBucketReferences();
            if (valuesBucketReferences != null) {
                builder.withValueBackReferences(ContentProviderConverter.valuesBucketToContentValues(valuesBucketReferences));
            }
            Map<Integer, Integer> dataAbilityPredicatesBackReferences = dataAbilityOperation.getDataAbilityPredicatesBackReferences();
            if (dataAbilityPredicatesBackReferences != null) {
                for (Map.Entry<Integer, Integer> entry : dataAbilityPredicatesBackReferences.entrySet()) {
                    builder.withSelectionBackReference(entry.getKey().intValue(), entry.getValue().intValue());
                }
            }
            return builder.build();
        }
        AppLog.e(SHELL_LABEL, "dataAbilityToContentProviderOperation: dataAbilityOperation cannot be null.", new Object[0]);
        throw new IllegalArgumentException("dataAbilityOperation cannot be null.");
    }

    public static DataAbilityOperation contentProviderOperationToDataAbilityOperation(ContentProviderOperation contentProviderOperation) {
        DataAbilityOperation.Builder builder;
        if (contentProviderOperation != null) {
            Parcel obtain = Parcel.obtain();
            contentProviderOperation.writeToParcel(obtain, 0);
            obtain.setDataPosition(0);
            int readInt = obtain.readInt();
            android.net.Uri uri = (android.net.Uri) android.net.Uri.CREATOR.createFromParcel(obtain);
            HashMap hashMap = null;
            ContentValues contentValues = obtain.readInt() != 0 ? (ContentValues) ContentValues.CREATOR.createFromParcel(obtain) : null;
            String readString = obtain.readInt() != 0 ? obtain.readString() : null;
            String[] readStringArray = obtain.readInt() != 0 ? obtain.readStringArray() : null;
            Integer valueOf = obtain.readInt() != 0 ? Integer.valueOf(obtain.readInt()) : null;
            ContentValues contentValues2 = obtain.readInt() != 0 ? (ContentValues) ContentValues.CREATOR.createFromParcel(obtain) : null;
            if (obtain.readInt() != 0) {
                hashMap = new HashMap();
            }
            if (hashMap != null) {
                int readInt2 = obtain.readInt();
                for (int i = 0; i < readInt2; i++) {
                    hashMap.put(Integer.valueOf(obtain.readInt()), Integer.valueOf(obtain.readInt()));
                }
            }
            obtain.recycle();
            if (readInt == 1) {
                builder = DataAbilityOperation.newInsertBuilder(UriConverter.convertToZidaneContentUri(uri, ""));
            } else if (readInt == 2) {
                builder = DataAbilityOperation.newUpdateBuilder(UriConverter.convertToZidaneContentUri(uri, ""));
            } else if (readInt == 3) {
                builder = DataAbilityOperation.newDeleteBuilder(UriConverter.convertToZidaneContentUri(uri, ""));
            } else if (readInt == 4) {
                builder = DataAbilityOperation.newAssertBuilder(UriConverter.convertToZidaneContentUri(uri, ""));
            } else {
                AppLog.e(SHELL_LABEL, "contentProviderOperationToDataAbilityOperation: type is invalid.", new Object[0]);
                throw new IllegalArgumentException("type is invalid: " + readInt);
            }
            if (contentValues != null) {
                builder.withValuesBucket(ContentProviderConverter.contentValuesToValuesBucket(contentValues));
            }
            if (valueOf != null) {
                builder.withExpectedCount(valueOf.intValue());
            }
            if (readString != null) {
                builder.withPredicates(ContentProviderConverter.selectionToDataAbilityPredicates(readString, readStringArray));
            }
            if (contentValues2 != null) {
                builder.withValueBackReferences(ContentProviderConverter.contentValuesToValuesBucket(contentValues2));
            }
            if (hashMap != null) {
                for (Map.Entry entry : hashMap.entrySet()) {
                    builder.withPredicatesBackReference(((Integer) entry.getKey()).intValue(), ((Integer) entry.getValue()).intValue());
                }
            }
            return builder.build();
        }
        AppLog.e(SHELL_LABEL, "contentProviderOperationToDataAbilityOperation: contentProviderOperation cannot be null.", new Object[0]);
        throw new IllegalArgumentException("contentProviderOperation cannot be null.");
    }
}
