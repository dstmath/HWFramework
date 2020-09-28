package huawei.android.widget;

import android.content.Context;
import android.util.Log;
import android.widget.HwWidgetColumn;
import huawei.android.widget.columnsystem.HwColumnSystem;

public class HwWidgetColumnImpl implements HwWidgetColumn {
    private static final String TAG = "HwWidgetColumnImpl";
    private Context mContext;

    public HwWidgetColumnImpl(Context context) {
        this.mContext = context;
    }

    public int getMaxColumnWidth(int columnType) {
        HwColumnSystem hwColumnSystem = new HwColumnSystem(this.mContext);
        if (columnType == 0) {
            hwColumnSystem.setColumnType(10);
        } else if (columnType == 1) {
            hwColumnSystem.setColumnType(5);
        } else if (columnType != 2) {
            Log.w(TAG, "Parameter error");
            return -1;
        } else {
            hwColumnSystem.setColumnType(12);
        }
        return hwColumnSystem.getMaxColumnWidth();
    }

    public int getMinColumnWidth(int columnType) {
        HwColumnSystem hwColumnSystem = new HwColumnSystem(this.mContext);
        if (columnType == 0) {
            hwColumnSystem.setColumnType(10);
        } else if (columnType == 1) {
            hwColumnSystem.setColumnType(5);
        } else if (columnType != 2) {
            Log.w(TAG, "Parameter error");
            return -1;
        } else {
            hwColumnSystem.setColumnType(12);
        }
        return hwColumnSystem.getMinColumnWidth();
    }
}
