package com.android.internal.app;

import android.content.Context;
import android.content.res.Configuration;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
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
    private Context mContextOverride = null;
    private final boolean mCountryMode;
    private Locale mDisplayLocale = null;
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
                for (int i = 0; i < count; i++) {
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
            for (String word : valueText.split(" ")) {
                if (word.startsWith(prefixString)) {
                    return true;
                }
            }
            return false;
        }

        protected void publishResults(CharSequence constraint, FilterResults results) {
            SuggestedLocaleAdapter.this.mLocaleOptions = (ArrayList) results.values;
            SuggestedLocaleAdapter.this.mSuggestionCount = 0;
            for (LocaleInfo li : SuggestedLocaleAdapter.this.mLocaleOptions) {
                if (li.isSuggested()) {
                    SuggestedLocaleAdapter suggestedLocaleAdapter = SuggestedLocaleAdapter.this;
                    suggestedLocaleAdapter.mSuggestionCount = suggestedLocaleAdapter.mSuggestionCount + 1;
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
                this.mSuggestionCount++;
            }
            this.mLocaleOptions.add(li);
        }
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return getItemViewType(position) == 2;
    }

    public int getItemViewType(int position) {
        if (!showHeaders()) {
            return 2;
        }
        if (position == 0) {
            return 0;
        }
        if (position == this.mSuggestionCount + 1) {
            return 1;
        }
        return 2;
    }

    public int getViewTypeCount() {
        if (showHeaders()) {
            return 3;
        }
        return 1;
    }

    public int getCount() {
        if (showHeaders()) {
            return this.mLocaleOptions.size() + 2;
        }
        return this.mLocaleOptions.size();
    }

    public Object getItem(int position) {
        int offset = 0;
        if (showHeaders()) {
            offset = position > this.mSuggestionCount ? -2 : -1;
        }
        return this.mLocaleOptions.get(position + offset);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void setDisplayLocale(Context context, Locale locale) {
        if (locale == null) {
            this.mDisplayLocale = null;
            this.mContextOverride = null;
        } else if (!locale.equals(this.mDisplayLocale)) {
            this.mDisplayLocale = locale;
            Configuration configOverride = new Configuration();
            configOverride.setLocale(locale);
            this.mContextOverride = context.createConfigurationContext(configOverride);
        }
    }

    private void setTextTo(TextView textView, int resId) {
        if (this.mContextOverride == null) {
            textView.setText(resId);
        } else {
            textView.setText(this.mContextOverride.getText(resId));
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null && this.mInflater == null) {
            this.mInflater = LayoutInflater.from(parent.getContext());
        }
        int itemType = getItemViewType(position);
        switch (itemType) {
            case 0:
            case 1:
                convertView = this.mInflater.inflate((int) R.layout.language_picker_section_header, parent, false);
                TextView textView = (TextView) convertView;
                if (itemType == 0) {
                    setTextTo(textView, R.string.language_picker_section_suggested);
                } else if (this.mCountryMode) {
                    setTextTo(textView, R.string.region_picker_section_all);
                } else {
                    setTextTo(textView, R.string.language_picker_section_all);
                }
                textView.setTextLocale(this.mDisplayLocale != null ? this.mDisplayLocale : Locale.getDefault());
                break;
            default:
                String addLable;
                if (!(convertView instanceof ViewGroup)) {
                    convertView = this.mInflater.inflate((int) R.layout.language_picker_item, parent, false);
                }
                TextView text = (TextView) convertView.findViewById(R.id.locale);
                LocaleInfo item = (LocaleInfo) getItem(position);
                int secondLineColor = parent.getContext().getColor(33882456);
                String lable = item.getLabel(this.mCountryMode);
                if (this.mCountryMode) {
                    addLable = LocaleHelper.getDisplayCountry(item.getLocale());
                } else {
                    addLable = LocaleHelper.getDisplayName(item.getLocale(), true);
                }
                CharSequence msp = new SpannableString(lable + "\n" + addLable);
                msp.setSpan(new RelativeSizeSpan(0.8666667f), lable.length() + 1, (lable.length() + addLable.length()) + 1, 33);
                msp.setSpan(new ForegroundColorSpan(secondLineColor), lable.length() + 1, (lable.length() + addLable.length()) + 1, 33);
                text.setLineSpacing(5.0f, 1.0f);
                text.setText(msp);
                text.setTextLocale(item.getLocale());
                text.setContentDescription(item.getContentDescription(this.mCountryMode));
                if (this.mCountryMode) {
                    int i;
                    int layoutDir = TextUtils.getLayoutDirectionFromLocale(item.getParent());
                    convertView.setLayoutDirection(layoutDir);
                    if (layoutDir == 1) {
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
        if (this.mCountryMode && this.mLocaleOptions.size() < 6) {
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
