package com.android.internal.globalactions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ActionsAdapter extends BaseAdapter {
    private final Context mContext;
    private final BooleanSupplier mDeviceProvisioned;
    private final List<Action> mItems;
    private final BooleanSupplier mKeyguardShowing;

    public ActionsAdapter(Context context, List<Action> items, BooleanSupplier deviceProvisioned, BooleanSupplier keyguardShowing) {
        this.mContext = context;
        this.mItems = items;
        this.mDeviceProvisioned = deviceProvisioned;
        this.mKeyguardShowing = keyguardShowing;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        boolean keyguardShowing = this.mKeyguardShowing.getAsBoolean();
        boolean deviceProvisioned = this.mDeviceProvisioned.getAsBoolean();
        int count = 0;
        for (int i = 0; i < this.mItems.size(); i++) {
            Action action = this.mItems.get(i);
            if ((!keyguardShowing || action.showDuringKeyguard()) && (deviceProvisioned || action.showBeforeProvisioning())) {
                count++;
            }
        }
        return count;
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override // android.widget.Adapter
    public Action getItem(int position) {
        boolean keyguardShowing = this.mKeyguardShowing.getAsBoolean();
        boolean deviceProvisioned = this.mDeviceProvisioned.getAsBoolean();
        int filteredPos = 0;
        for (int i = 0; i < this.mItems.size(); i++) {
            Action action = this.mItems.get(i);
            if ((!keyguardShowing || action.showDuringKeyguard()) && (deviceProvisioned || action.showBeforeProvisioning())) {
                if (filteredPos == position) {
                    return action;
                }
                filteredPos++;
            }
        }
        throw new IllegalArgumentException("position " + position + " out of range of showable actions, filtered count=" + getCount() + ", keyguardshowing=" + keyguardShowing + ", provisioned=" + deviceProvisioned);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return (long) position;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        Action action = getItem(position);
        Context context = this.mContext;
        return action.create(context, convertView, parent, LayoutInflater.from(context));
    }
}
