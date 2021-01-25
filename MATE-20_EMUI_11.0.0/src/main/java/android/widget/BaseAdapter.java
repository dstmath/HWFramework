package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseAdapter implements ListAdapter, SpinnerAdapter {
    private CharSequence[] mAutofillOptions;
    @UnsupportedAppUsage
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override // android.widget.Adapter
    public boolean hasStableIds() {
        return false;
    }

    @Override // android.widget.Adapter
    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.registerObserver(observer);
    }

    @Override // android.widget.Adapter
    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        this.mDataSetObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        this.mDataSetObservable.notifyInvalidated();
    }

    @Override // android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override // android.widget.ListAdapter
    public boolean isEnabled(int position) {
        return true;
    }

    @Override // android.widget.SpinnerAdapter
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override // android.widget.Adapter
    public int getItemViewType(int position) {
        return 0;
    }

    @Override // android.widget.Adapter
    public int getViewTypeCount() {
        return 1;
    }

    @Override // android.widget.Adapter
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override // android.widget.Adapter
    public CharSequence[] getAutofillOptions() {
        return this.mAutofillOptions;
    }

    public void setAutofillOptions(CharSequence... options) {
        this.mAutofillOptions = options;
    }
}
