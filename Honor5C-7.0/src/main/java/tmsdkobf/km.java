package tmsdkobf;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class km extends ContentProvider {
    private Context mContext;

    public km(Context context) {
        this.mContext = context;
    }

    private String a(Uri uri) {
        return uri.getPathSegments().size() <= 0 ? null : (String) uri.getPathSegments().get(0);
    }

    private String b(Object obj) {
        if (obj instanceof String) {
            return "String";
        }
        if (obj instanceof Integer) {
            return "Int";
        }
        if (obj instanceof Boolean) {
            return "Boolean";
        }
        if (obj instanceof Float) {
            return "Float";
        }
        if (obj instanceof Long) {
            return "Long";
        }
        throw new RuntimeException("cannot parse type def!");
    }

    public int delete(Uri uri, String str, String[] strArr) {
        String a = a(uri);
        if (a != null) {
            Editor edit = this.mContext.getSharedPreferences(a, 0).edit();
            if (str != null) {
                edit.remove(str);
            } else {
                edit.clear();
            }
            return !edit.commit() ? 0 : 1;
        } else {
            throw new RuntimeException("[delete] sharedPreferences failed:file name should not be null(uri=" + uri + ")");
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        String a = a(uri);
        if (a == null || contentValues == null) {
            throw new RuntimeException(new StringBuilder().append("[insert] sharedPreferences failed:").append(a).toString() != null ? "valuesshould not be null(uri=" + uri + ")" : "file name ");
        }
        Editor edit = this.mContext.getSharedPreferences(a, 0).edit();
        for (Entry entry : contentValues.valueSet()) {
            String str = (String) entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                edit.remove(str);
            } else if (value instanceof String) {
                edit.putString(str, value.toString());
            } else if (value instanceof Integer) {
                edit.putInt(str, Integer.parseInt(value.toString()));
            } else if (value instanceof Boolean) {
                edit.putBoolean(str, Boolean.parseBoolean(value.toString()));
            } else if (value instanceof Float) {
                edit.putFloat(str, Float.parseFloat(value.toString()));
            } else if (value instanceof Long) {
                edit.putLong(str, Long.parseLong(value.toString()));
            } else {
                throw new IllegalArgumentException("not supported type.");
            }
        }
        return !edit.commit() ? null : uri;
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        int i = 1;
        String a = a(uri);
        if (a != null) {
            SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(a, 0);
            if (str2 != null) {
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{"value"}, 1);
                a = (String) uri.getPathSegments().get(1);
                if (sharedPreferences.contains(str2)) {
                    if ("String".equals(a)) {
                        matrixCursor.addRow(new String[]{sharedPreferences.getString(str2, null)});
                    } else if ("Boolean".equals(a)) {
                        Integer[] numArr = new Integer[1];
                        if (!sharedPreferences.getBoolean(str2, false)) {
                            i = 0;
                        }
                        numArr[0] = Integer.valueOf(i);
                        matrixCursor.addRow(numArr);
                    } else if ("Int".equals(a)) {
                        matrixCursor.addRow(new Integer[]{Integer.valueOf(sharedPreferences.getInt(str2, 0))});
                    } else if ("Float".equals(a)) {
                        matrixCursor.addRow(new Float[]{Float.valueOf(sharedPreferences.getFloat(str2, 0.0f))});
                    } else if ("Long".equals(a)) {
                        matrixCursor.addRow(new Long[]{Long.valueOf(sharedPreferences.getLong(str2, 0))});
                    } else {
                        throw new RuntimeException("cannot parse type def!");
                    }
                }
                return matrixCursor;
            }
            Cursor cursor;
            Map all = sharedPreferences.getAll();
            if (all == null) {
                cursor = null;
            } else {
                Object value;
                MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{"key", "value", "typedef"}, all.size());
                for (Entry entry : all.entrySet()) {
                    String str3 = (String) entry.getKey();
                    value = entry.getValue();
                    String b = b(value);
                    if (b != null) {
                        if ("Boolean".equals(b)) {
                            Boolean bool = (Boolean) value;
                            Object[] objArr = new Object[3];
                            objArr[0] = str3;
                            objArr[1] = Integer.valueOf(!bool.booleanValue() ? 0 : 1);
                            objArr[2] = b;
                            matrixCursor2.addRow(objArr);
                        } else {
                            matrixCursor2.addRow(new Object[]{str3, value, b});
                        }
                    }
                }
                value = matrixCursor2;
            }
            return cursor;
        }
        throw new RuntimeException(new StringBuilder().append("[query] sharedPreferences failed:").append(a).toString() != null ? "selectionshould not be null(uri=" + uri + ")" : "file name ");
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException("[update] not implement");
    }
}
