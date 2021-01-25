package ohos.data.dataability;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWindow;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import ohos.data.PredicatesUtils;
import ohos.data.rdb.ValuesBucket;
import ohos.data.rdb.impl.SharedResultSetWrapper;
import ohos.data.resultset.ResultSet;
import ohos.data.resultset.SharedBlock;
import ohos.data.resultset.SharedResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.fastjson.JSONException;
import ohos.utils.zson.ZSONObject;

public class ContentProviderConverter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "ContentProviderConverter");

    private ContentProviderConverter() {
    }

    public static ValuesBucket contentValuesToValuesBucket(ContentValues contentValues) {
        if (contentValues != null) {
            ValuesBucket valuesBucket = new ValuesBucket(contentValues.size());
            for (Map.Entry<String, Object> entry : contentValues.valueSet()) {
                fillValuesBucket(valuesBucket, entry.getValue(), entry.getKey());
            }
            return valuesBucket;
        }
        HiLog.info(LABEL, "contentValuesToValuesBucket: contentValues cannot be null.", new Object[0]);
        throw new IllegalArgumentException("contentValues cannot be null.");
    }

    private static void fillValuesBucket(ValuesBucket valuesBucket, Object obj, String str) {
        if (obj == null) {
            valuesBucket.putNull(str);
        } else if (obj instanceof String) {
            valuesBucket.putString(str, (String) obj);
        } else if (obj instanceof Byte) {
            valuesBucket.putByte(str, (Byte) obj);
        } else if (obj instanceof Short) {
            valuesBucket.putShort(str, (Short) obj);
        } else if (obj instanceof Integer) {
            valuesBucket.putInteger(str, (Integer) obj);
        } else if (obj instanceof Long) {
            valuesBucket.putLong(str, (Long) obj);
        } else if (obj instanceof Float) {
            valuesBucket.putFloat(str, (Float) obj);
        } else if (obj instanceof Double) {
            valuesBucket.putDouble(str, (Double) obj);
        } else if (obj instanceof Boolean) {
            valuesBucket.putBoolean(str, (Boolean) obj);
        } else if (obj instanceof byte[]) {
            valuesBucket.putByteArray(str, (byte[]) obj);
        } else {
            HiLog.info(LABEL, "contentValuesToValuesBucket: Function type conversion error.", new Object[0]);
            throw new IllegalArgumentException("Unrecognized conversion type: " + obj.getClass().getName());
        }
    }

    public static ContentValues valuesBucketToContentValues(ValuesBucket valuesBucket) {
        if (valuesBucket != null) {
            ContentValues contentValues = new ContentValues(valuesBucket.size());
            for (Map.Entry<String, Object> entry : valuesBucket.getAll()) {
                fillContentValues(contentValues, entry.getKey(), entry.getValue());
            }
            return contentValues;
        }
        HiLog.info(LABEL, "valuesBucketToContentValues: valuesBucket cannot be null.", new Object[0]);
        throw new IllegalArgumentException("valuesBucket cannot be null.");
    }

    private static void fillContentValues(ContentValues contentValues, String str, Object obj) {
        if (obj == null) {
            contentValues.putNull(str);
        } else if (obj instanceof String) {
            contentValues.put(str, (String) obj);
        } else if (obj instanceof Byte) {
            contentValues.put(str, (Byte) obj);
        } else if (obj instanceof Short) {
            contentValues.put(str, (Short) obj);
        } else if (obj instanceof Integer) {
            contentValues.put(str, (Integer) obj);
        } else if (obj instanceof Long) {
            contentValues.put(str, (Long) obj);
        } else if (obj instanceof Float) {
            contentValues.put(str, (Float) obj);
        } else if (obj instanceof Double) {
            contentValues.put(str, (Double) obj);
        } else if (obj instanceof Boolean) {
            contentValues.put(str, (Boolean) obj);
        } else if (obj instanceof byte[]) {
            contentValues.put(str, (byte[]) obj);
        } else {
            HiLog.info(LABEL, "valuesBucketToContentValues: Function type conversion error.", new Object[0]);
            throw new IllegalArgumentException("Unrecognized conversion type: " + obj.getClass().getName());
        }
    }

    public static ResultSet cursorToResultSet(Cursor cursor) {
        if (cursor != null) {
            return new CursorToResultSetAdapter(cursor);
        }
        HiLog.info(LABEL, "cursorToResultSet: cursor cannot be null.", new Object[0]);
        throw new IllegalArgumentException("cursor cannot be null.");
    }

    public static Cursor resultSetToCursor(ResultSet resultSet) {
        SharedResultSet sharedResultSet;
        if (resultSet != null) {
            if (resultSet instanceof SharedResultSet) {
                sharedResultSet = (SharedResultSet) resultSet;
            } else {
                sharedResultSet = new SharedResultSetWrapper(resultSet);
            }
            return new SharedResultSetToWindowedCursorAdapter(sharedResultSet);
        }
        HiLog.info(LABEL, "resultSetToCursor: input resultset can't be null.", new Object[0]);
        throw new IllegalArgumentException("input resultset can't be null.");
    }

    public static SharedBlock cursorWindowToSharedBlock(CursorWindow cursorWindow) throws IOException {
        if (cursorWindow != null) {
            Parcel obtain = Parcel.obtain();
            cursorWindow.writeToParcel(obtain, 0);
            obtain.setDataPosition(0);
            ohos.utils.Parcel create = ohos.utils.Parcel.create();
            create.writeInt(obtain.readInt());
            if (obtain.createByteArray().length % 4 == 0) {
                obtain.readInt();
            }
            create.writeString(cursorWindow.getName());
            ParcelFileDescriptor readFileDescriptor = obtain.readFileDescriptor();
            if (readFileDescriptor != null) {
                create.writeInt(readFileDescriptor.getFd());
            }
            SharedBlock sharedBlock = new SharedBlock(create);
            if (readFileDescriptor != null) {
                readFileDescriptor.close();
            }
            obtain.recycle();
            create.reclaim();
            return sharedBlock;
        }
        HiLog.info(LABEL, "cursorWindowToSharedBlock: CursorWindow cannot be null.", new Object[0]);
        throw new IllegalArgumentException("CursorWindow is null, couldn't convert to SharedBlock.");
    }

    public static CursorWindow sharedBlockToCursorWindow(SharedBlock sharedBlock) throws IOException {
        if (sharedBlock != null) {
            ohos.utils.Parcel create = ohos.utils.Parcel.create();
            sharedBlock.marshalling(create);
            Parcel obtain = Parcel.obtain();
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                obtain.writeInt(create.readInt());
                create.readString();
                obtain.writeString(null);
                parcelFileDescriptor = ParcelFileDescriptor.fromFd(create.readInt());
                obtain.writeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
                obtain.setDataPosition(0);
                CursorWindow newFromParcel = CursorWindow.newFromParcel(obtain);
                parcelFileDescriptor.close();
                create.reclaim();
                obtain.recycle();
                return newFromParcel;
            } catch (Throwable th) {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
                create.reclaim();
                obtain.recycle();
                throw th;
            }
        } else {
            HiLog.info(LABEL, "sharedBlockToCursorWindow: SharedBlock cannot be null.", new Object[0]);
            throw new IllegalArgumentException("SharedBlock is null, couldn't convert to CursorWindow.");
        }
    }

    public static DataAbilityPredicates selectionToDataAbilityPredicates(String str, String[] strArr) {
        if (str == null) {
            HiLog.info(LABEL, "selectionToDataAbilityPredicates: selection is null.", new Object[0]);
            return null;
        } else if (str.isEmpty()) {
            DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates(str);
            dataAbilityPredicates.setWhereArgs(new ArrayList(Arrays.asList(strArr)));
            return dataAbilityPredicates;
        } else {
            try {
                ZSONObject stringToZSON = ZSONObject.stringToZSON(str);
                if (stringToZSON != null) {
                    Boolean bool = stringToZSON.getBoolean(ConverterUtils.ISRAWSELECTION_IN_PREDICATES);
                    String string = stringToZSON.getString(ConverterUtils.WHERECLAUSE_IN_PREDICATES);
                    Boolean bool2 = stringToZSON.getBoolean(ConverterUtils.DISTINCT_IN_PREDICATES);
                    String string2 = stringToZSON.getString(ConverterUtils.GROUP_IN_PREDICATES);
                    if (bool == null || string == null || bool2 == null || string2 == null) {
                        throw new IllegalArgumentException("selection is in bad format, please check.");
                    } else if (bool.booleanValue()) {
                        DataAbilityPredicates dataAbilityPredicates2 = new DataAbilityPredicates(string);
                        dataAbilityPredicates2.setWhereArgs(new ArrayList(Arrays.asList(strArr)));
                        return dataAbilityPredicates2;
                    } else {
                        DataAbilityPredicates dataAbilityPredicates3 = new DataAbilityPredicates();
                        String string3 = stringToZSON.getString(ConverterUtils.INDEX_IN_PREDICATES);
                        Integer integer = stringToZSON.getInteger(ConverterUtils.LIMIT_IN_PREDICATES);
                        Integer integer2 = stringToZSON.getInteger(ConverterUtils.OFFSET_IN_PREDICATES);
                        PredicatesUtils.setWhereClauseAndArgs(dataAbilityPredicates3, string, new ArrayList(Arrays.asList(strArr)));
                        PredicatesUtils.setAttributes(dataAbilityPredicates3, bool2.booleanValue(), string3, string2, null, integer, integer2);
                        return dataAbilityPredicates3;
                    }
                } else {
                    throw new IllegalArgumentException("the conversion value from string to json is null.");
                }
            } catch (JSONException unused) {
                HiLog.info(LABEL, "selectionToDataAbilityPredicates: selection is in the raw non-json format.", new Object[0]);
                DataAbilityPredicates dataAbilityPredicates4 = new DataAbilityPredicates(str);
                dataAbilityPredicates4.setWhereArgs(new ArrayList(Arrays.asList(strArr)));
                return dataAbilityPredicates4;
            }
        }
    }

    public static String dataAbilityPredicatesToSelection(DataAbilityPredicates dataAbilityPredicates) {
        if (dataAbilityPredicates == null) {
            HiLog.info(LABEL, "dataAbilityPredicatesToSelection: DataAbilityPredicates is null.", new Object[0]);
            return null;
        }
        boolean isRawSelection = dataAbilityPredicates.isRawSelection();
        String whereClause = dataAbilityPredicates.getWhereClause();
        if (isRawSelection) {
            if (whereClause.isEmpty()) {
                return whereClause;
            }
            try {
                ZSONObject.stringToZSON(whereClause);
            } catch (JSONException unused) {
                HiLog.info(LABEL, "dataAbilityPredicatesToSelection: DataAbilityPredicates is created by rawSelectionof non-json format.", new Object[0]);
                return whereClause;
            }
        }
        HashMap hashMap = new HashMap(7);
        boolean isDistinct = dataAbilityPredicates.isDistinct();
        String group = dataAbilityPredicates.getGroup();
        hashMap.put(ConverterUtils.ISRAWSELECTION_IN_PREDICATES, Boolean.valueOf(isRawSelection));
        hashMap.put(ConverterUtils.WHERECLAUSE_IN_PREDICATES, whereClause);
        hashMap.put(ConverterUtils.DISTINCT_IN_PREDICATES, Boolean.valueOf(isDistinct));
        hashMap.put(ConverterUtils.GROUP_IN_PREDICATES, group);
        Integer limit = dataAbilityPredicates.getLimit();
        Integer offset = dataAbilityPredicates.getOffset();
        String index = dataAbilityPredicates.getIndex();
        if (limit != null) {
            hashMap.put(ConverterUtils.LIMIT_IN_PREDICATES, limit);
        }
        if (offset != null) {
            hashMap.put(ConverterUtils.OFFSET_IN_PREDICATES, offset);
        }
        if (index != null) {
            hashMap.put(ConverterUtils.INDEX_IN_PREDICATES, index);
        }
        return ZSONObject.toZSONString(new ZSONObject(hashMap));
    }

    public static String[] dataAbilityPredicatesToSelectionArgs(DataAbilityPredicates dataAbilityPredicates) {
        if (dataAbilityPredicates != null) {
            return (String[]) dataAbilityPredicates.getWhereArgs().toArray(new String[0]);
        }
        HiLog.info(LABEL, "dataAbilityPredicatesToSelectionArgs: dataAbilityPredicates is null.", new Object[0]);
        return null;
    }
}
