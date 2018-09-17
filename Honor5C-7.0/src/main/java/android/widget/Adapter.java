package android.widget;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

public interface Adapter {
    public static final int IGNORE_ITEM_VIEW_TYPE = -1;
    public static final int NO_SELECTION = Integer.MIN_VALUE;

    int getCount();

    Object getItem(int i);

    long getItemId(int i);

    int getItemViewType(int i);

    View getView(int i, View view, ViewGroup viewGroup);

    int getViewTypeCount();

    boolean hasStableIds();

    boolean isEmpty();

    void registerDataSetObserver(DataSetObserver dataSetObserver);

    void unregisterDataSetObserver(DataSetObserver dataSetObserver);
}
