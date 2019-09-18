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
import java.util.Set;

public class HwSuggestedLocaleAdapterEx implements IHwSuggestedLocaleAdapterEx {
    private static final int MIN_REGIONS_FOR_SUGGESTIONS = 6;
    private static final int TYPE_HEADER_ADDED = 3;
    private static final int TYPE_HEADER_ALL_OTHERS = 1;
    private static final int TYPE_HEADER_SUGGESTED = 0;
    private static final int TYPE_LOCALE = 2;
    private ArrayList<LocaleStore.LocaleInfo> addedList;
    private int mAddedCount;
    private Context mContext;
    private boolean mIsAddedLanguages;
    private ArrayList<LocaleStore.LocaleInfo> mOriginalLocaleInfo;
    private IHwSuggestedLocaleAdapterInner mSlaInner;

    public HwSuggestedLocaleAdapterEx(IHwSuggestedLocaleAdapterInner inner, Context context, boolean isAddedLanguages) {
        this.mSlaInner = inner;
        this.mContext = context;
        this.mIsAddedLanguages = isAddedLanguages;
    }

    public void init(Set<LocaleStore.LocaleInfo> localeOptions, ArrayList<LocaleStore.LocaleInfo> mLocaleOptions) {
        this.addedList = new ArrayList<>();
        if (this.mIsAddedLanguages) {
            LocaleList localeList = LocalePicker.getLocales();
            int localeListSize = localeList.size();
            for (int i = 0; i < localeListSize; i++) {
                Locale userLocale = localeList.get(i);
                String country = userLocale.getCountry();
                String language = userLocale.getLanguage();
                String languageTag = userLocale.toLanguageTag();
                Iterator<LocaleStore.LocaleInfo> it = localeOptions.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    LocaleStore.LocaleInfo li = it.next();
                    Locale locale = li.getLocale();
                    boolean isSupport = HwFrameworkFactory.getHwLocaleStoreEx().isSupportRegion(this.mContext, locale, country);
                    String localeId = li.getId();
                    if (!"en-XA".equals(languageTag) && !"ar-XB".equals(languageTag) && !"en-XA".equals(localeId) && !"ar-XB".equals(localeId)) {
                        if (locale.getLanguage().equals(language) && isSupport) {
                            if (!language.equals("zh")) {
                                if (!localeId.contains("Latn") || !languageTag.contains("Latn")) {
                                    if (!localeId.contains("Latn") && !languageTag.contains("Latn")) {
                                        this.addedList.add(li);
                                        mLocaleOptions.remove(li);
                                        break;
                                    }
                                } else {
                                    this.addedList.add(li);
                                    mLocaleOptions.remove(li);
                                    break;
                                }
                            } else if (languageTag.startsWith(this.mSlaInner.getLocaleInfoScript(li))) {
                                this.addedList.add(li);
                                mLocaleOptions.remove(li);
                            }
                        }
                    } else if (languageTag.equals(localeId)) {
                        this.addedList.add(li);
                        mLocaleOptions.remove(li);
                        break;
                    }
                }
            }
        }
    }

    public int preGetItemViewType(int position) {
        if (!this.mIsAddedLanguages) {
            return -1;
        }
        if (!this.mSlaInner.isShowHeaders()) {
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
            return position == (this.mSlaInner.getSuggestionCount() + this.mAddedCount) + 2 ? 1 : 2;
        }
    }

    public int preGetCount(ArrayList<LocaleStore.LocaleInfo> mLocaleOptions) {
        if (mLocaleOptions == null || mLocaleOptions.size() == 0) {
            return 0;
        }
        if (!this.mIsAddedLanguages) {
            return -1;
        }
        if (this.mSlaInner.isShowHeaders()) {
            return mLocaleOptions.size() + 3;
        }
        return mLocaleOptions.size() + 2;
    }

    public Object rePlaceGetItem(int position, ArrayList<LocaleStore.LocaleInfo> mLocaleOptions) {
        if (mLocaleOptions == null) {
            return null;
        }
        int offset = 0;
        int size = mLocaleOptions.size();
        int i = -2;
        if (this.mSlaInner.isShowHeaders()) {
            if (!this.mIsAddedLanguages) {
                if (position < this.mSlaInner.getSuggestionCount() + 1) {
                    i = -1;
                }
                offset = i;
            } else if (position < this.mAddedCount + 1) {
                offset = -1;
            } else if (position < this.mAddedCount + this.mSlaInner.getSuggestionCount() + 2) {
                offset = -2;
            } else {
                offset = -3;
            }
        } else if (this.mIsAddedLanguages) {
            if (position < this.mAddedCount + 1) {
                i = -1;
            }
            offset = i;
        }
        if (position + offset < size) {
            return mLocaleOptions.get(position + offset);
        }
        return mLocaleOptions.get((position + offset) - 1);
    }

    public void changeAddedLanguagesPos(LocaleHelper.LocaleInfoComparator comp, ArrayList<LocaleStore.LocaleInfo> mLocaleOptions) {
        Collections.sort(this.addedList, comp);
        this.mAddedCount = this.addedList.size();
        if (!this.mSlaInner.isCountryMode()) {
            for (int i = 0; i < this.mAddedCount; i++) {
                LocaleStore.LocaleInfo li = this.addedList.get(i);
                if (this.mSlaInner.isSuggestedLocale(li)) {
                    this.mSlaInner.setSuggestionCount(this.mSlaInner.getSuggestionCount() - 1);
                }
                mLocaleOptions.add(i, li);
            }
        }
    }

    public ArrayList<LocaleStore.LocaleInfo> changePerformFiltering(CharSequence prefix) {
        ArrayList<LocaleStore.LocaleInfo> values;
        CharSequence mPrefix = prefix;
        this.mIsAddedLanguages = false;
        if (this.mOriginalLocaleInfo == null) {
            this.mOriginalLocaleInfo = new ArrayList<>(this.mSlaInner.getmLocaleOptions());
            for (int i = 0; i < this.mAddedCount; i++) {
                this.mOriginalLocaleInfo.remove(0);
            }
        }
        if (mPrefix == null || mPrefix.length() == 0) {
            if (!this.mSlaInner.isCountryMode()) {
                this.mIsAddedLanguages = true;
            }
            values = new ArrayList<>(this.mSlaInner.getmOriginalLocaleOptions());
        } else if (mPrefix.length() > 0) {
            this.mSlaInner.setSuggestionCount(0);
            values = new ArrayList<>(this.mOriginalLocaleInfo);
        } else {
            values = new ArrayList<>(this.mSlaInner.getmOriginalLocaleOptions());
        }
        if ((prefix == null || prefix.length() == 0) && !this.mSlaInner.isCountryMode()) {
            this.mIsAddedLanguages = true;
        }
        return values;
    }

    public boolean isAddedLanguages() {
        return this.mIsAddedLanguages;
    }

    public int getSuggestionCount(CharSequence prefix, int count) {
        int suggestionCount = count;
        if ((prefix == null || prefix.length() == 0) && this.addedList != null) {
            int addedSize = this.addedList.size();
            for (int i = 0; i < addedSize; i++) {
                if (this.mSlaInner.isSuggestedLocale(this.addedList.get(i)) && suggestionCount > 0) {
                    suggestionCount--;
                }
            }
        }
        return suggestionCount;
    }

    public void setHeaderDivider(View convertView, int position) {
        View divider = convertView.findViewById(34603437);
        if (position == 0) {
            divider.setVisibility(8);
        } else {
            divider.setVisibility(0);
        }
    }

    public void setDefaultItemDivider(View convertView, int position, int lastPosition) {
        View divider = convertView.findViewById(34603437);
        if ((position >= lastPosition || !(this.mSlaInner.getItemViewTypeEx(position + 1) == 0 || 1 == this.mSlaInner.getItemViewTypeEx(position + 1))) && position != lastPosition) {
            divider.setVisibility(0);
        } else {
            divider.setVisibility(8);
        }
    }
}
