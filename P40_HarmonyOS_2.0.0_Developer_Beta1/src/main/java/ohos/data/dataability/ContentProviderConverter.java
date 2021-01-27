package ohos.data.dataability;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWindow;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import ohos.data.PredicatesUtils;
import ohos.data.dataability.impl.CursorToResultSetAdapter;
import ohos.data.rdb.ValuesBucket;
import ohos.data.rdb.impl.SharedResultSetWrapper;
import ohos.data.resultset.ResultSet;
import ohos.data.resultset.SharedBlock;
import ohos.data.resultset.SharedResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ContentProviderConverter {
    private static final int INVALID_OBJECT_FLAG = 0;
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
        DataAbilityPredicates dataAbilityPredicates;
        if (str == null) {
            HiLog.info(LABEL, "selectionToDataAbilityPredicates: selection is null.", new Object[0]);
            return null;
        } else if (str.isEmpty() || !isParcelString(str)) {
            DataAbilityPredicates dataAbilityPredicates2 = new DataAbilityPredicates(str);
            dataAbilityPredicates2.setWhereArgs(new ArrayList(Arrays.asList(strArr)));
            return dataAbilityPredicates2;
        } else {
            ohos.utils.Parcel create = ohos.utils.Parcel.create();
            create.writeBytes(Base64.getDecoder().decode(str));
            if (create.readInt() == 0) {
                boolean readBoolean = create.readBoolean();
                String readString = create.readString();
                if (readBoolean) {
                    dataAbilityPredicates = new DataAbilityPredicates(readString);
                    dataAbilityPredicates.setWhereArgs(new ArrayList(Arrays.asList(strArr)));
                } else {
                    dataAbilityPredicates = new DataAbilityPredicates();
                    boolean readBoolean2 = create.readBoolean();
                    String readString2 = create.readString();
                    Integer num = (Integer) create.readValue();
                    Integer num2 = (Integer) create.readValue();
                    String readString3 = create.readString();
                    PredicatesUtils.setWhereClauseAndArgs(dataAbilityPredicates, readString, new ArrayList(Arrays.asList(strArr)));
                    PredicatesUtils.setAttributes(dataAbilityPredicates, readBoolean2, readString3, readString2, null, num, num2);
                }
                create.reclaim();
                return dataAbilityPredicates;
            }
            create.reclaim();
            throw new IllegalArgumentException("the conversion value from string to parcel is error format.");
        }
    }

    public static String dataAbilityPredicatesToSelection(DataAbilityPredicates dataAbilityPredicates) {
        if (dataAbilityPredicates == null) {
            HiLog.info(LABEL, "dataAbilityPredicatesToSelection: DataAbilityPredicates is null.", new Object[0]);
            return null;
        }
        boolean isRawSelection = dataAbilityPredicates.isRawSelection();
        String whereClause = dataAbilityPredicates.getWhereClause();
        if (isRawSelection && !isParcelString(whereClause)) {
            return whereClause;
        }
        ohos.utils.Parcel create = ohos.utils.Parcel.create();
        create.writeInt(0);
        create.writeBoolean(isRawSelection);
        create.writeString(whereClause);
        if (!isRawSelection) {
            create.writeBoolean(dataAbilityPredicates.isDistinct());
            create.writeString(dataAbilityPredicates.getGroup());
            create.writeValue(dataAbilityPredicates.getLimit());
            create.writeValue(dataAbilityPredicates.getOffset());
            create.writeString(dataAbilityPredicates.getIndex());
        }
        byte[] bytes = create.getBytes();
        create.reclaim();
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String[] dataAbilityPredicatesToSelectionArgs(DataAbilityPredicates dataAbilityPredicates) {
        if (dataAbilityPredicates != null) {
            return (String[]) dataAbilityPredicates.getWhereArgs().toArray(new String[0]);
        }
        HiLog.info(LABEL, "dataAbilityPredicatesToSelectionArgs: dataAbilityPredicates is null.", new Object[0]);
        return null;
    }

    private static boolean isParcelString(String str) {
        boolean z = false;
        if (str != null && !str.isEmpty()) {
            try {
                byte[] decode = Base64.getDecoder().decode(str);
                ohos.utils.Parcel create = ohos.utils.Parcel.create();
                create.writeBytes(decode);
                if (create.readInt() == 0) {
                    z = true;
                }
                create.reclaim();
            } catch (IllegalArgumentException unused) {
            }
        }
        return z;
    }
}
