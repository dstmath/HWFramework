package com.android.internal.app;

import android.common.HwFrameworkFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocaleStore;
import com.huawei.android.app.IHwSuggestedLocaleAdapterEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class SuggestedLocaleAdapter extends BaseAdapter implements Filterable, IHwSuggestedLocaleAdapterInner {
    private static final int MIN_REGIONS_FOR_SUGGESTIONS = 6;
    private static final int TYPE_HEADER_ADDED = 3;
    private static final int TYPE_HEADER_ALL_OTHERS = 1;
    private static final int TYPE_HEADER_NOVALID = -1;
    private static final int TYPE_HEADER_SUGGESTED = 0;
    private static final int TYPE_LOCALE = 2;
    private Context mContextOverride;
    /* access modifiers changed from: private */
    public boolean mCountryMode;
    private Locale mDisplayLocale;
    /* access modifiers changed from: private */
    public IHwSuggestedLocaleAdapterEx mHex;
    private LayoutInflater mInflater;
    /* access modifiers changed from: private */
    public ArrayList<LocaleStore.LocaleInfo> mLocaleOptions;
    /* access modifiers changed from: private */
    public ArrayList<LocaleStore.LocaleInfo> mOriginalLocaleOptions;
    /* access modifiers changed from: private */
    public int mSuggestionCount;

    class FilterByNativeAndUiNames extends Filter {
        private CharSequence mPrefix;

        FilterByNativeAndUiNames() {
        }

        /* access modifiers changed from: protected */
        public Filter.FilterResults performFiltering(CharSequence prefix) {
            this.mPrefix = prefix;
            Filter.FilterResults results = new Filter.FilterResults();
            if (SuggestedLocaleAdapter.this.mOriginalLocaleOptions == null) {
                ArrayList unused = SuggestedLocaleAdapter.this.mOriginalLocaleOptions = new ArrayList(SuggestedLocaleAdapter.this.mLocaleOptions);
            }
            ArrayList<LocaleStore.LocaleInfo> values = SuggestedLocaleAdapter.this.mHex.changePerformFiltering(prefix);
            if (prefix == null || prefix.length() == 0) {
                results.values = values;
                results.count = values.size();
            } else {
                Locale locale = Locale.getDefault();
                String prefixString = LocaleHelper.normalizeForSearch(prefix.toString(), locale);
                int count = values.size();
                ArrayList<LocaleStore.LocaleInfo> newValues = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    LocaleStore.LocaleInfo value = values.get(i);
                    String nameToCheck = LocaleHelper.normalizeForSearch(value.getLabel(SuggestedLocaleAdapter.this.mCountryMode), locale);
                    if (wordMatches(LocaleHelper.normalizeForSearch(value.getContentDescription(SuggestedLocaleAdapter.this.mCountryMode), locale), prefixString) || wordMatches(nameToCheck, prefixString)) {
                        newValues.add(value);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        /* access modifiers changed from: package-private */
        public boolean wordMatches(String valueText, String prefixString) {
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

        /* access modifiers changed from: protected */
        public void publishResults(CharSequence constraint, Filter.FilterResults results) {
            ArrayList unused = SuggestedLocaleAdapter.this.mLocaleOptions = (ArrayList) results.values;
            int unused2 = SuggestedLocaleAdapter.this.mSuggestionCount = 0;
            if (SuggestedLocaleAdapter.this.mLocaleOptions != null) {
                Iterator it = SuggestedLocaleAdapter.this.mLocaleOptions.iterator();
                while (it.hasNext()) {
                    if (((LocaleStore.LocaleInfo) it.next()).isSuggested()) {
                        int unused3 = SuggestedLocaleAdapter.this.mSuggestionCount = SuggestedLocaleAdapter.this.mSuggestionCount + 1;
                    }
                }
            }
            int unused4 = SuggestedLocaleAdapter.this.mSuggestionCount = SuggestedLocaleAdapter.this.mHex.getSuggestionCount(this.mPrefix, SuggestedLocaleAdapter.this.mSuggestionCount);
            if (results.count > 0) {
                SuggestedLocaleAdapter.this.notifyDataSetChanged();
            } else {
                SuggestedLocaleAdapter.this.notifyDataSetInvalidated();
            }
        }
    }

    public SuggestedLocaleAdapter(Set<LocaleStore.LocaleInfo> localeOptions, boolean countryMode) {
        this.mDisplayLocale = null;
        this.mContextOverride = null;
        this.mHex = null;
        this.mCountryMode = countryMode;
        this.mLocaleOptions = new ArrayList<>(localeOptions.size());
        for (LocaleStore.LocaleInfo li : localeOptions) {
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
        int tR = this.mHex.preGetItemViewType(position);
        if (tR != -1) {
            return tR;
        }
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
        if (!showHeaders() && !this.mHex.isAddedLanguages()) {
            return 1;
        }
        return 4;
    }

    public int getCount() {
        int tR = this.mHex.preGetCount(this.mLocaleOptions);
        if (tR != -1) {
            return tR;
        }
        if (showHeaders()) {
            return this.mLocaleOptions.size() + 2;
        }
        return this.mLocaleOptions.size();
    }

    public Object getItem(int position) {
        return this.mHex.rePlaceGetItem(position, this.mLocaleOptions);
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
        String str;
        SpannableString msp;
        int i;
        int i2 = position;
        View convertView2 = convertView;
        ViewGroup viewGroup = parent;
        if (this.mLocaleOptions == null) {
            return null;
        }
        if (convertView2 == null && this.mInflater == null) {
            this.mInflater = LayoutInflater.from(parent.getContext());
        }
        int itemType = getItemViewType(position);
        if (itemType != 3) {
            switch (itemType) {
                case 0:
                case 1:
                    break;
                default:
                    if (!(convertView2 instanceof ViewGroup)) {
                        convertView2 = this.mInflater.inflate(34013354, viewGroup, false);
                    }
                    this.mHex.setDefaultItemDivider(convertView2, i2, getCount() - 1);
                    TextView text = (TextView) convertView2.findViewById(34603345);
                    LocaleStore.LocaleInfo item = (LocaleStore.LocaleInfo) getItem(position);
                    ImageView image = (ImageView) convertView2.findViewById(34603379);
                    if (item != null) {
                        String systemCountry = Locale.getDefault().getCountry();
                        String itemCountry = item.getLocale().getCountry();
                        String id = item.getId();
                        if (!this.mCountryMode || !systemCountry.equals(itemCountry)) {
                            image.setImageResource(33751080);
                            image.setVisibility(8);
                        } else {
                            image.setImageResource(33751080);
                            image.setVisibility(0);
                        }
                        int secondLineColor = parent.getContext().getColor(33882535);
                        String lable = HwFrameworkFactory.getHwLocaleStoreEx().getFullLanguageName(parent.getContext(), item.getLocale(), item.getLocale());
                        if (this.mCountryMode) {
                            str = LocaleHelper.getDisplayCountry(item.getLocale());
                            String str2 = systemCountry;
                        } else {
                            String str3 = systemCountry;
                            str = HwFrameworkFactory.getHwLocaleStoreEx().getFullLanguageName(parent.getContext(), item.getLocale(), Locale.getDefault());
                        }
                        String addLable = str;
                        if (this.mCountryMode) {
                            msp = new SpannableString(addLable);
                            ImageView imageView = image;
                            String str4 = itemCountry;
                        } else {
                            msp = new SpannableString(lable + "\n" + addLable);
                            ImageView imageView2 = image;
                            String str5 = itemCountry;
                            msp.setSpan(new RelativeSizeSpan(0.8666667f), lable.length() + 1, lable.length() + addLable.length() + 1, 33);
                            msp.setSpan(new ForegroundColorSpan(secondLineColor), lable.length() + 1, lable.length() + addLable.length() + 1, 33);
                        }
                        text.setLineSpacing(5.0f, 1.0f);
                        text.setText(msp);
                        text.setTextLocale(item.getLocale());
                        text.setContentDescription(item.getContentDescription(this.mCountryMode));
                        if (this.mCountryMode) {
                            int layoutDir = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault());
                            convertView2.setLayoutDirection(layoutDir);
                            if (layoutDir == 1) {
                                i = 4;
                            } else {
                                i = 3;
                            }
                            text.setTextDirection(i);
                            break;
                        }
                    } else {
                        return null;
                    }
                    break;
            }
        }
        convertView2 = this.mInflater.inflate(34013353, viewGroup, false);
        this.mHex.setHeaderDivider(convertView2, i2);
        TextView textView = (TextView) convertView2.findViewById(34603428);
        if (itemType == 0) {
            if (this.mCountryMode) {
                textView.setText(parent.getContext().getText(33686218));
            } else {
                setTextTo(textView, 17040316);
            }
        } else if (itemType == 3) {
            if (this.mHex.isAddedLanguages()) {
                textView.setText(parent.getContext().getText(33685529));
            }
        } else if (this.mCountryMode) {
            setTextTo(textView, 17040982);
        } else {
            setTextTo(textView, 17040315);
        }
        textView.setTextLocale(this.mDisplayLocale != null ? this.mDisplayLocale : Locale.getDefault());
        return convertView2;
    }

    private boolean showHeaders() {
        boolean z = false;
        if (this.mLocaleOptions == null) {
            return false;
        }
        if (this.mCountryMode && this.mLocaleOptions.size() < 6) {
            return false;
        }
        if (!(this.mSuggestionCount == 0 || this.mSuggestionCount == this.mLocaleOptions.size())) {
            z = true;
        }
        return z;
    }

    public void sort(LocaleHelper.LocaleInfoComparator comp) {
        Collections.sort(this.mLocaleOptions, comp);
        this.mHex.changeAddedLanguagesPos(comp, this.mLocaleOptions);
    }

    public Filter getFilter() {
        return new FilterByNativeAndUiNames();
    }

    public SuggestedLocaleAdapter(Set<LocaleStore.LocaleInfo> localeOptions, boolean countryMode, Context context, boolean isShowAddedHeaders) {
        this(localeOptions, countryMode);
        this.mHex = HwFrameworkFactory.getHwSuggestedLocaleAdapterEx(this, context, isShowAddedHeaders);
        this.mHex.init(localeOptions, this.mLocaleOptions);
    }

    public boolean isCountryMode() {
        return this.mCountryMode;
    }

    public int getSuggestionCount() {
        return this.mSuggestionCount;
    }

    public void setSuggestionCount(int suggestionCount) {
        this.mSuggestionCount = suggestionCount;
    }

    public boolean isShowHeaders() {
        return showHeaders();
    }

    public void setCountryMode(boolean countryMode) {
        this.mCountryMode = countryMode;
    }

    public ArrayList<LocaleStore.LocaleInfo> getmOriginalLocaleOptions() {
        return this.mOriginalLocaleOptions;
    }

    public void setmOriginalLocaleOptions(ArrayList<LocaleStore.LocaleInfo> mOriginalLocaleOptions2) {
        this.mOriginalLocaleOptions = mOriginalLocaleOptions2;
    }

    public ArrayList<LocaleStore.LocaleInfo> getmLocaleOptions() {
        return this.mLocaleOptions;
    }

    public void setmLocaleOptions(ArrayList<LocaleStore.LocaleInfo> mLocaleOptions2) {
        this.mLocaleOptions = mLocaleOptions2;
    }

    public boolean isSuggestedLocale(LocaleStore.LocaleInfo li) {
        return li.isSuggested();
    }

    public String getLocaleInfoScript(LocaleStore.LocaleInfo li) {
        return new LocaleStore().getLangScriptKeyEx(li);
    }

    public boolean getIsClickable() {
        return this.mHex.isAddedLanguages();
    }

    public int getItemViewTypeEx(int position) {
        return getItemViewType(position);
    }
}
