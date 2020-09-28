package com.android.internal.app;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiEnterpriseConfig;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.hwext.internal.R;
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
    private boolean mCountryMode;
    private Locale mDisplayLocale;
    private LinearLayout mEmptyRoot;
    private IHwSuggestedLocaleAdapterEx mHex;
    private LayoutInflater mInflater;
    private FrameLayout mListviewContainer;
    private ArrayList<LocaleStore.LocaleInfo> mLocaleOptions;
    private ArrayList<LocaleStore.LocaleInfo> mOriginalLocaleOptions;
    private int mSuggestionCount;

    static /* synthetic */ int access$408(SuggestedLocaleAdapter x0) {
        int i = x0.mSuggestionCount;
        x0.mSuggestionCount = i + 1;
        return i;
    }

    public SuggestedLocaleAdapter(Set<LocaleStore.LocaleInfo> localeOptions, boolean countryMode) {
        this.mDisplayLocale = null;
        this.mContextOverride = null;
        this.mHex = null;
        this.mListviewContainer = null;
        this.mEmptyRoot = null;
        this.mCountryMode = countryMode;
        this.mLocaleOptions = new ArrayList<>(localeOptions.size());
        for (LocaleStore.LocaleInfo li : localeOptions) {
            if (li.isSuggested()) {
                this.mSuggestionCount++;
            }
            this.mLocaleOptions.add(li);
        }
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean isEnabled(int position) {
        return getItemViewType(position) == 2;
    }

    @Override // android.widget.Adapter, android.widget.BaseAdapter
    public int getItemViewType(int position) {
        int tR;
        IHwSuggestedLocaleAdapterEx iHwSuggestedLocaleAdapterEx = this.mHex;
        if (iHwSuggestedLocaleAdapterEx != null && (tR = iHwSuggestedLocaleAdapterEx.preGetItemViewType(position)) != -1) {
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

    @Override // android.widget.Adapter, android.widget.BaseAdapter
    public int getViewTypeCount() {
        if (this.mHex != null) {
            if (!showHeaders() && !this.mHex.isAddedLanguages()) {
                return 1;
            }
            return 4;
        } else if (showHeaders()) {
            return 3;
        } else {
            return 1;
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        int tR;
        IHwSuggestedLocaleAdapterEx iHwSuggestedLocaleAdapterEx = this.mHex;
        if (iHwSuggestedLocaleAdapterEx != null && (tR = iHwSuggestedLocaleAdapterEx.preGetCount(this.mLocaleOptions)) != -1) {
            return tR;
        }
        if (showHeaders()) {
            return this.mLocaleOptions.size() + 2;
        }
        return this.mLocaleOptions.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        IHwSuggestedLocaleAdapterEx iHwSuggestedLocaleAdapterEx = this.mHex;
        if (iHwSuggestedLocaleAdapterEx != null) {
            return iHwSuggestedLocaleAdapterEx.replaceGetItem(position, this.mLocaleOptions);
        }
        int offset = 0;
        if (showHeaders()) {
            offset = position > this.mSuggestionCount ? -2 : -1;
        }
        return this.mLocaleOptions.get(position + offset);
    }

    @Override // android.widget.Adapter
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
        Context context = this.mContextOverride;
        if (context == null) {
            textView.setText(resId);
        } else {
            textView.setText(context.getText(resId));
        }
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        String addLable;
        SpannableString msp;
        int i;
        View convertView2 = convertView;
        if (this.mLocaleOptions == null) {
            return null;
        }
        if (convertView2 == null && this.mInflater == null) {
            this.mInflater = LayoutInflater.from(parent.getContext());
        }
        int itemType = getItemViewType(position);
        if (itemType == 0 || itemType == 1 || itemType == 3) {
            convertView2 = this.mInflater.inflate(R.layout.language_picker_section_header_emui, parent, false);
            IHwSuggestedLocaleAdapterEx iHwSuggestedLocaleAdapterEx = this.mHex;
            if (iHwSuggestedLocaleAdapterEx != null) {
                iHwSuggestedLocaleAdapterEx.setHeaderDivider(convertView2, position);
            }
            TextView textView = (TextView) convertView2.findViewById(R.id.title_locale_header);
            if (itemType == 0) {
                if (this.mCountryMode) {
                    textView.setText(parent.getContext().getText(R.string.section_picker_suggested));
                } else {
                    setTextTo(textView, com.android.internal.R.string.language_picker_section_suggested);
                }
            } else if (itemType == 3) {
                if (this.mHex.isAddedLanguages()) {
                    textView.setText(parent.getContext().getText(R.string.already_added));
                }
            } else if (this.mCountryMode) {
                setTextTo(textView, R.string.region_picker_section_all_emui);
            } else {
                setTextTo(textView, com.android.internal.R.string.language_picker_section_all);
            }
            Locale locale = this.mDisplayLocale;
            if (locale == null) {
                locale = Locale.getDefault();
            }
            textView.setTextLocale(locale);
        } else {
            if (!(convertView2 instanceof LinearLayout)) {
                convertView2 = this.mInflater.inflate(R.layout.language_region_picker_item, parent, false);
            }
            IHwSuggestedLocaleAdapterEx iHwSuggestedLocaleAdapterEx2 = this.mHex;
            if (iHwSuggestedLocaleAdapterEx2 != null) {
                iHwSuggestedLocaleAdapterEx2.setDefaultItemDivider(convertView2, position, getCount() - 1);
            }
            TextView text = (TextView) convertView2.findViewById(R.id.locale);
            LocaleStore.LocaleInfo item = (LocaleStore.LocaleInfo) getItem(position);
            ImageView image = (ImageView) convertView2.findViewById(R.id.region_selected);
            if (item == null) {
                return null;
            }
            String systemCountry = Locale.getDefault().getCountry();
            String itemCountry = item.getLocale().getCountry();
            item.getId();
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
                addLable = LocaleHelper.getDisplayCountry(item.getLocale());
            } else {
                addLable = HwFrameworkFactory.getHwLocaleStoreEx().getFullLanguageName(parent.getContext(), item.getLocale(), Locale.getDefault());
            }
            if (this.mCountryMode) {
                msp = new SpannableString(addLable);
            } else {
                msp = new SpannableString(lable + "\n" + addLable);
                msp.setSpan(new RelativeSizeSpan(0.8666667f), lable.length() + 1, lable.length() + addLable.length() + 1, 33);
                msp.setSpan(new ForegroundColorSpan(secondLineColor), lable.length() + 1, lable.length() + addLable.length() + 1, 33);
            }
            text.setLineSpacing(5.0f, 1.0f);
            text.setText(msp);
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
            }
        }
        return convertView2;
    }

    private boolean showHeaders() {
        int i;
        ArrayList<LocaleStore.LocaleInfo> arrayList = this.mLocaleOptions;
        if (arrayList == null) {
            return false;
        }
        if ((this.mCountryMode && arrayList.size() < 6) || (i = this.mSuggestionCount) == 0 || i == this.mLocaleOptions.size()) {
            return false;
        }
        return true;
    }

    public void sort(LocaleHelper.LocaleInfoComparator comp) {
        Collections.sort(this.mLocaleOptions, comp);
        IHwSuggestedLocaleAdapterEx iHwSuggestedLocaleAdapterEx = this.mHex;
        if (iHwSuggestedLocaleAdapterEx != null) {
            iHwSuggestedLocaleAdapterEx.changeAddedLanguagesPos(comp, this.mLocaleOptions);
        }
    }

    class FilterByNativeAndUiNames extends Filter {
        private CharSequence mPrefix;

        FilterByNativeAndUiNames() {
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Filter
        public Filter.FilterResults performFiltering(CharSequence prefix) {
            ArrayList<LocaleStore.LocaleInfo> values;
            this.mPrefix = prefix;
            Filter.FilterResults results = new Filter.FilterResults();
            if (SuggestedLocaleAdapter.this.mOriginalLocaleOptions == null) {
                SuggestedLocaleAdapter suggestedLocaleAdapter = SuggestedLocaleAdapter.this;
                suggestedLocaleAdapter.mOriginalLocaleOptions = new ArrayList(suggestedLocaleAdapter.mLocaleOptions);
            }
            if (SuggestedLocaleAdapter.this.mHex != null) {
                values = SuggestedLocaleAdapter.this.mHex.changePerformFiltering(prefix);
            } else {
                values = new ArrayList<>(SuggestedLocaleAdapter.this.mOriginalLocaleOptions);
            }
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
                    String valueContentDescription = value.getContentDescription(SuggestedLocaleAdapter.this.mCountryMode);
                    if (valueContentDescription != null && (wordMatches(LocaleHelper.normalizeForSearch(valueContentDescription, locale), prefixString) || wordMatches(nameToCheck, prefixString))) {
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
            if (valueText.contains(prefixString)) {
                return true;
            }
            for (String word : valueText.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER)) {
                if (word.contains(prefixString)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Filter
        public void publishResults(CharSequence constraint, Filter.FilterResults results) {
            SuggestedLocaleAdapter.this.mLocaleOptions = (ArrayList) results.values;
            SuggestedLocaleAdapter.this.mSuggestionCount = 0;
            if (SuggestedLocaleAdapter.this.mLocaleOptions != null) {
                Iterator it = SuggestedLocaleAdapter.this.mLocaleOptions.iterator();
                while (it.hasNext()) {
                    if (((LocaleStore.LocaleInfo) it.next()).isSuggested()) {
                        SuggestedLocaleAdapter.access$408(SuggestedLocaleAdapter.this);
                    }
                }
            }
            if (SuggestedLocaleAdapter.this.mHex != null) {
                SuggestedLocaleAdapter suggestedLocaleAdapter = SuggestedLocaleAdapter.this;
                suggestedLocaleAdapter.mSuggestionCount = suggestedLocaleAdapter.mHex.getSuggestionCount(this.mPrefix, SuggestedLocaleAdapter.this.mSuggestionCount);
            }
            if (results.count > 0) {
                SuggestedLocaleAdapter.this.notifyDataSetChanged();
                if (SuggestedLocaleAdapter.this.mListviewContainer != null && SuggestedLocaleAdapter.this.mEmptyRoot != null) {
                    SuggestedLocaleAdapter.this.mListviewContainer.setVisibility(0);
                    SuggestedLocaleAdapter.this.mEmptyRoot.setVisibility(8);
                    return;
                }
                return;
            }
            SuggestedLocaleAdapter.this.notifyDataSetInvalidated();
            if (SuggestedLocaleAdapter.this.mListviewContainer != null && SuggestedLocaleAdapter.this.mEmptyRoot != null) {
                SuggestedLocaleAdapter.this.mEmptyRoot.setVisibility(0);
                SuggestedLocaleAdapter.this.mListviewContainer.setVisibility(8);
            }
        }
    }

    @Override // android.widget.Filterable
    public Filter getFilter() {
        return new FilterByNativeAndUiNames();
    }

    public SuggestedLocaleAdapter(Set<LocaleStore.LocaleInfo> localeOptions, boolean countryMode, Context context, boolean isShowAddedHeaders) {
        this(localeOptions, countryMode);
        this.mHex = HwFrameworkFactory.getHwSuggestedLocaleAdapterEx(this, context, isShowAddedHeaders);
        this.mHex.init(localeOptions, this.mLocaleOptions);
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public boolean isCountryMode() {
        return this.mCountryMode;
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public int getSuggestionCount() {
        return this.mSuggestionCount;
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public void setSuggestionCount(int suggestionCount) {
        this.mSuggestionCount = suggestionCount;
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public boolean isShowHeaders() {
        return showHeaders();
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public void setCountryMode(boolean countryMode) {
        this.mCountryMode = countryMode;
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public ArrayList<LocaleStore.LocaleInfo> getOriginalLocaleOptions() {
        return this.mOriginalLocaleOptions;
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public ArrayList<LocaleStore.LocaleInfo> getLocaleOptions() {
        return this.mLocaleOptions;
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public boolean isSuggestedLocale(LocaleStore.LocaleInfo li) {
        return li.isSuggested();
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public String getLocaleInfoScript(LocaleStore.LocaleInfo li) {
        return new LocaleStore().getLangScriptKeyEx(li);
    }

    public boolean getIsClickable() {
        return this.mHex.isAddedLanguages();
    }

    public void setEmptyView(FrameLayout listviewContainer, LinearLayout emptyRoot) {
        this.mListviewContainer = listviewContainer;
        this.mEmptyRoot = emptyRoot;
    }

    @Override // com.android.internal.app.IHwSuggestedLocaleAdapterInner
    public int getItemViewTypeEx(int position) {
        return getItemViewType(position);
    }
}
