package huawei.com.android.internal.app;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.LocaleList;
import android.view.View;
import com.android.internal.app.IHwSuggestedLocaleAdapterInner;
import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore;
import com.huawei.android.app.IHwSuggestedLocaleAdapterEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class HwSuggestedLocaleAdapterEx implements IHwSuggestedLocaleAdapterEx {
    private static final int MIN_REGIONS_FOR_SUGGESTIONS = 6;
    private static final int NUM_OFFSET = -2;
    private static final int NUM_OFFSET_THREE = -3;
    private static final int NUM_RETURN = -1;
    private static final int TYPE_HEADER_ADDED = 3;
    private static final int TYPE_HEADER_ALL_OTHERS = 1;
    private static final int TYPE_HEADER_SUGGESTED = 0;
    private static final int TYPE_LOCALE = 2;
    private int mAddedCount;
    private ArrayList<LocaleStore.LocaleInfo> mAddedList;
    private Context mContext;
    private IHwSuggestedLocaleAdapterInner mInner;
    private boolean mIsAddedLanguages;
    private ArrayList<LocaleStore.LocaleInfo> mOriginalLocaleInfo;

    public HwSuggestedLocaleAdapterEx(IHwSuggestedLocaleAdapterInner inner, Context context, boolean isAddedLanguages) {
        this.mInner = inner;
        this.mContext = context;
        this.mIsAddedLanguages = isAddedLanguages;
    }

    public void init(Set<LocaleStore.LocaleInfo> localeOptions, ArrayList<LocaleStore.LocaleInfo> arrayLocaleOptions) {
        this.mAddedList = new ArrayList<>();
        if (this.mIsAddedLanguages && arrayLocaleOptions != null) {
            LocaleList localeList = LocalePicker.getLocales();
            int localeListSize = localeList.size();
            for (int i = 0; i < localeListSize; i++) {
                String userFullLanguageName = HwFrameworkFactory.getHwLocaleStoreEx().getFullLanguageName(this.mContext, localeList.get(i), Locale.ENGLISH);
                Iterator<LocaleStore.LocaleInfo> it = localeOptions.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    LocaleStore.LocaleInfo li = it.next();
                    if (userFullLanguageName.equals(HwFrameworkFactory.getHwLocaleStoreEx().getFullLanguageName(this.mContext, li.getLocale(), Locale.ENGLISH))) {
                        this.mAddedList.add(li);
                        arrayLocaleOptions.remove(li);
                        break;
                    }
                }
            }
        }
    }

    public int preGetItemViewType(int position) {
        IHwSuggestedLocaleAdapterInner iHwSuggestedLocaleAdapterInner;
        if (!this.mIsAddedLanguages || (iHwSuggestedLocaleAdapterInner = this.mInner) == null) {
            return -1;
        }
        if (!iHwSuggestedLocaleAdapterInner.isShowHeaders()) {
            if (position == 0) {
                return 3;
            }
            return position == this.mAddedCount + 1 ? 1 : 2;
        } else if (position == 0) {
            return 3;
        } else {
            if (position == this.mAddedCount + 1) {
                return 0;
            }
            return position == (this.mInner.getSuggestionCount() + this.mAddedCount) + 2 ? 1 : 2;
        }
    }

    public int preGetCount(ArrayList<LocaleStore.LocaleInfo> localeOptions) {
        IHwSuggestedLocaleAdapterInner iHwSuggestedLocaleAdapterInner;
        if (localeOptions == null || localeOptions.size() == 0 || (iHwSuggestedLocaleAdapterInner = this.mInner) == null || !this.mIsAddedLanguages) {
            return -1;
        }
        if (iHwSuggestedLocaleAdapterInner.isShowHeaders()) {
            return localeOptions.size() + 3;
        }
        return localeOptions.size() + 2;
    }

    public Object replaceGetItem(int position, ArrayList<LocaleStore.LocaleInfo> localeOptions) {
        if (localeOptions == null || this.mInner == null) {
            return Optional.empty();
        }
        int offset = 0;
        int size = localeOptions.size();
        int i = -1;
        if (this.mInner.isShowHeaders()) {
            if (this.mIsAddedLanguages) {
                int i2 = this.mAddedCount;
                if (position < i2 + 1) {
                    offset = -1;
                } else if (position < i2 + this.mInner.getSuggestionCount() + 2) {
                    offset = -2;
                } else {
                    offset = -3;
                }
            } else {
                if (position >= this.mInner.getSuggestionCount() + 1) {
                    i = -2;
                }
                offset = i;
            }
        } else if (this.mIsAddedLanguages) {
            if (position >= this.mAddedCount + 1) {
                i = -2;
            }
            offset = i;
        }
        if (position + offset < size) {
            return localeOptions.get(position + offset);
        }
        if (position + offset < size + 1) {
            return localeOptions.get((position + offset) - 1);
        }
        return Optional.empty();
    }

    public void changeAddedLanguagesPos(LocaleHelper.LocaleInfoComparator comp, ArrayList<LocaleStore.LocaleInfo> localeOptions) {
        if (this.mInner != null) {
            Collections.sort(this.mAddedList, comp);
            this.mAddedCount = this.mAddedList.size();
            if (!this.mInner.isCountryMode()) {
                for (int i = 0; i < this.mAddedCount; i++) {
                    LocaleStore.LocaleInfo li = this.mAddedList.get(i);
                    if (this.mInner.isSuggestedLocale(li)) {
                        IHwSuggestedLocaleAdapterInner iHwSuggestedLocaleAdapterInner = this.mInner;
                        iHwSuggestedLocaleAdapterInner.setSuggestionCount(iHwSuggestedLocaleAdapterInner.getSuggestionCount() - 1);
                    }
                    localeOptions.add(i, li);
                }
            }
        }
    }

    public ArrayList<LocaleStore.LocaleInfo> changePerformFiltering(CharSequence prefix) {
        ArrayList<LocaleStore.LocaleInfo> values;
        IHwSuggestedLocaleAdapterInner iHwSuggestedLocaleAdapterInner = this.mInner;
        if (iHwSuggestedLocaleAdapterInner == null) {
            return null;
        }
        this.mIsAddedLanguages = false;
        if (this.mOriginalLocaleInfo == null) {
            this.mOriginalLocaleInfo = new ArrayList<>(iHwSuggestedLocaleAdapterInner.getLocaleOptions());
            for (int i = 0; i < this.mAddedCount; i++) {
                this.mOriginalLocaleInfo.remove(0);
            }
        }
        if (prefix == null || prefix.length() == 0) {
            if (!this.mInner.isCountryMode()) {
                this.mIsAddedLanguages = true;
            }
            values = new ArrayList<>(this.mInner.getOriginalLocaleOptions());
        } else if (prefix.length() > 0) {
            this.mInner.setSuggestionCount(0);
            values = new ArrayList<>(this.mOriginalLocaleInfo);
        } else {
            values = new ArrayList<>(this.mInner.getOriginalLocaleOptions());
        }
        if ((prefix == null || prefix.length() == 0) && !this.mInner.isCountryMode()) {
            this.mIsAddedLanguages = true;
        }
        return values;
    }

    public boolean isAddedLanguages() {
        return this.mIsAddedLanguages;
    }

    public int getSuggestionCount(CharSequence prefix, int count) {
        ArrayList<LocaleStore.LocaleInfo> arrayList;
        if (this.mInner == null) {
            return -1;
        }
        int suggestionCount = count;
        if ((prefix == null || prefix.length() == 0) && (arrayList = this.mAddedList) != null) {
            int addedSize = arrayList.size();
            for (int i = 0; i < addedSize; i++) {
                if (this.mInner.isSuggestedLocale(this.mAddedList.get(i)) && suggestionCount > 0) {
                    suggestionCount--;
                }
            }
        }
        return suggestionCount;
    }

    public void setHeaderDivider(View convertView, int position) {
        if (convertView != null) {
            View divider = convertView.findViewById(34603543);
            if (position == 0) {
                divider.setVisibility(8);
            } else {
                divider.setVisibility(0);
            }
        }
    }

    public void setDefaultItemDivider(View convertView, int position, int lastPosition) {
        if (convertView != null && this.mInner != null) {
            View divider = convertView.findViewById(34603543);
            if ((position >= lastPosition || !(this.mInner.getItemViewTypeEx(position + 1) == 0 || this.mInner.getItemViewTypeEx(position + 1) == 1)) && position != lastPosition) {
                divider.setVisibility(0);
            } else {
                divider.setVisibility(8);
            }
        }
    }
}
