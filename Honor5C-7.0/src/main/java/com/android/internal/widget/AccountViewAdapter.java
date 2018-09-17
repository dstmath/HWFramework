package com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

public class AccountViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<AccountElements> mData;

    public static class AccountElements {
        private Drawable mDrawable;
        private int mIcon;
        private String mName;
        private String mNumber;

        public AccountElements(int icon, String name, String number) {
            this(icon, null, name, number);
        }

        public AccountElements(Drawable drawable, String name, String number) {
            this(0, drawable, name, number);
        }

        private AccountElements(int icon, Drawable drawable, String name, String number) {
            this.mIcon = icon;
            this.mDrawable = drawable;
            this.mName = name;
            this.mNumber = number;
        }

        public int getIcon() {
            return this.mIcon;
        }

        public String getName() {
            return this.mName;
        }

        public String getNumber() {
            return this.mNumber;
        }

        public Drawable getDrawable() {
            return this.mDrawable;
        }
    }

    public AccountViewAdapter(Context context, List<AccountElements> data) {
        this.mContext = context;
        this.mData = data;
    }

    public int getCount() {
        return this.mData.size();
    }

    public Object getItem(int position) {
        return this.mData.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void updateData(List<AccountElements> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        AccountItemView view;
        if (convertView == null) {
            view = new AccountItemView(this.mContext);
        } else {
            view = (AccountItemView) convertView;
        }
        view.setViewItem((AccountElements) getItem(position));
        return view;
    }
}
