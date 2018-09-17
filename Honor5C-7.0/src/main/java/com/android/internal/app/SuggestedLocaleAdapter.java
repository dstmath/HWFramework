package com.android.internal.app;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.app.LocaleHelper.LocaleInfoComparator;
import com.android.internal.app.LocaleStore.LocaleInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class SuggestedLocaleAdapter extends BaseAdapter implements Filterable {
    private static final int MIN_REGIONS_FOR_SUGGESTIONS = 6;
    private static final int TYPE_HEADER_ALL_OTHERS = 1;
    private static final int TYPE_HEADER_SUGGESTED = 0;
    private static final int TYPE_LOCALE = 2;
    private final boolean mCountryMode;
    private LayoutInflater mInflater;
    private ArrayList<LocaleInfo> mLocaleOptions;
    private ArrayList<LocaleInfo> mOriginalLocaleOptions;
    private int mSuggestionCount;

    class FilterByNativeAndUiNames extends Filter {
        FilterByNativeAndUiNames() {
        }

        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (SuggestedLocaleAdapter.this.mOriginalLocaleOptions == null) {
                SuggestedLocaleAdapter.this.mOriginalLocaleOptions = new ArrayList(SuggestedLocaleAdapter.this.mLocaleOptions);
            }
            ArrayList<LocaleInfo> values = new ArrayList(SuggestedLocaleAdapter.this.mOriginalLocaleOptions);
            if (prefix == null || prefix.length() == 0) {
                results.values = values;
                results.count = values.size();
            } else {
                Locale locale = Locale.getDefault();
                String prefixString = LocaleHelper.normalizeForSearch(prefix.toString(), locale);
                int count = values.size();
                ArrayList<LocaleInfo> newValues = new ArrayList();
                for (int i = SuggestedLocaleAdapter.TYPE_HEADER_SUGGESTED; i < count; i += SuggestedLocaleAdapter.TYPE_HEADER_ALL_OTHERS) {
                    LocaleInfo value = (LocaleInfo) values.get(i);
                    String nameToCheck = LocaleHelper.normalizeForSearch(value.getFullNameInUiLanguage(), locale);
                    if (wordMatches(LocaleHelper.normalizeForSearch(value.getFullNameNative(), locale), prefixString) || wordMatches(nameToCheck, prefixString)) {
                        newValues.add(value);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        boolean wordMatches(String valueText, String prefixString) {
            if (valueText.startsWith(prefixString)) {
                return true;
            }
            String[] words = valueText.split(" ");
            int length = words.length;
            for (int i = SuggestedLocaleAdapter.TYPE_HEADER_SUGGESTED; i < length; i += SuggestedLocaleAdapter.TYPE_HEADER_ALL_OTHERS) {
                if (words[i].startsWith(prefixString)) {
                    return true;
                }
            }
            return false;
        }

        protected void publishResults(CharSequence constraint, FilterResults results) {
            SuggestedLocaleAdapter.this.mLocaleOptions = (ArrayList) results.values;
            SuggestedLocaleAdapter.this.mSuggestionCount = SuggestedLocaleAdapter.TYPE_HEADER_SUGGESTED;
            for (LocaleInfo li : SuggestedLocaleAdapter.this.mLocaleOptions) {
                if (li.isSuggested()) {
                    SuggestedLocaleAdapter suggestedLocaleAdapter = SuggestedLocaleAdapter.this;
                    suggestedLocaleAdapter.mSuggestionCount = suggestedLocaleAdapter.mSuggestionCount + SuggestedLocaleAdapter.TYPE_HEADER_ALL_OTHERS;
                }
            }
            if (results.count > 0) {
                SuggestedLocaleAdapter.this.notifyDataSetChanged();
            } else {
                SuggestedLocaleAdapter.this.notifyDataSetInvalidated();
            }
        }
    }

    public SuggestedLocaleAdapter(Set<LocaleInfo> localeOptions, boolean countryMode) {
        this.mCountryMode = countryMode;
        this.mLocaleOptions = new ArrayList(localeOptions.size());
        for (LocaleInfo li : localeOptions) {
            if (li.isSuggested()) {
                this.mSuggestionCount += TYPE_HEADER_ALL_OTHERS;
            }
            this.mLocaleOptions.add(li);
        }
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_LOCALE;
    }

    public int getItemViewType(int position) {
        if (!showHeaders()) {
            return TYPE_LOCALE;
        }
        if (position == 0) {
            return TYPE_HEADER_SUGGESTED;
        }
        if (position == this.mSuggestionCount + TYPE_HEADER_ALL_OTHERS) {
            return TYPE_HEADER_ALL_OTHERS;
        }
        return TYPE_LOCALE;
    }

    public int getViewTypeCount() {
        if (showHeaders()) {
            return 3;
        }
        return TYPE_HEADER_ALL_OTHERS;
    }

    public int getCount() {
        if (showHeaders()) {
            return this.mLocaleOptions.size() + TYPE_LOCALE;
        }
        return this.mLocaleOptions.size();
    }

    public Object getItem(int position) {
        int offset = TYPE_HEADER_SUGGESTED;
        if (showHeaders()) {
            offset = position > this.mSuggestionCount ? -2 : -1;
        }
        return this.mLocaleOptions.get(position + offset);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null && this.mInflater == null) {
            this.mInflater = LayoutInflater.from(parent.getContext());
        }
        int itemType = getItemViewType(position);
        switch (itemType) {
            case TYPE_HEADER_SUGGESTED /*0*/:
            case TYPE_HEADER_ALL_OTHERS /*1*/:
                if (!(convertView instanceof TextView)) {
                    convertView = this.mInflater.inflate((int) R.layout.language_picker_section_header, parent, false);
                }
                TextView textView = (TextView) convertView;
                if (itemType == 0) {
                    textView.setText((int) R.string.language_picker_section_suggested);
                } else {
                    textView.setText((int) R.string.language_picker_section_all);
                }
                textView.setTextLocale(Locale.getDefault());
                break;
            default:
                if (!(convertView instanceof ViewGroup)) {
                    convertView = this.mInflater.inflate((int) R.layout.language_picker_item, parent, false);
                }
                TextView text = (TextView) convertView.findViewById(R.id.locale);
                LocaleInfo item = (LocaleInfo) getItem(position);
                text.setText(item.getLabel(this.mCountryMode));
                text.setTextLocale(item.getLocale());
                text.setContentDescription(item.getContentDescription(this.mCountryMode));
                if (this.mCountryMode) {
                    int i;
                    int layoutDir = TextUtils.getLayoutDirectionFromLocale(item.getParent());
                    convertView.setLayoutDirection(layoutDir);
                    if (layoutDir == TYPE_HEADER_ALL_OTHERS) {
                        i = 4;
                    } else {
                        i = 3;
                    }
                    text.setTextDirection(i);
                    break;
                }
                break;
        }
        return convertView;
    }

    private boolean showHeaders() {
        boolean z = false;
        if (this.mCountryMode && this.mLocaleOptions.size() < MIN_REGIONS_FOR_SUGGESTIONS) {
            return false;
        }
        if (!(this.mSuggestionCount == 0 || this.mSuggestionCount == this.mLocaleOptions.size())) {
            z = true;
        }
        return z;
    }

    public void sort(LocaleInfoComparator comp) {
        Collections.sort(this.mLocaleOptions, comp);
    }

    public Filter getFilter() {
        return new FilterByNativeAndUiNames();
    }
}
