package huawei.com.android.internal.app;

import android.app.AlertDialog;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SearchView;
import com.android.internal.app.IHwLocalePickerWithRegionInner;
import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore;
import com.huawei.android.app.IHwLocalePickerWithRegionEx;
import java.util.IllformedLocaleException;
import java.util.Locale;

public final class HwLocalePickerWithRegionEx implements IHwLocalePickerWithRegionEx, SearchView.OnQueryTextListener {
    IHwLocalePickerWithRegionInner mHWlpsInner = null;

    private class DialogListener implements DialogInterface.OnClickListener {
        LocaleStore.LocaleInfo locale;

        DialogListener(LocaleStore.LocaleInfo locale2) {
            this.locale = locale2;
        }

        public void onClick(DialogInterface dialogInterface, int which) {
            String localeId = this.locale.getId();
            String region = Locale.getDefault().getCountry();
            Locale.Builder localeBuilder = new Locale.Builder().setLanguageTag("en");
            try {
                localeBuilder = new Locale.Builder().setLocale(this.locale.getLocale()).setRegion(region);
            } catch (IllformedLocaleException e) {
                Log.e("HwLocalePickerWithRegionEx", "Error locale: " + this.locale.getLocale().toLanguageTag());
            }
            LocaleStore.LocaleInfo newLocaleInfo = LocaleStore.getLocaleInfo(localeBuilder.setExtension('u', "").build());
            if ("ar-XB".equals(localeId) || "en-XA".equals(localeId)) {
                newLocaleInfo = this.locale;
            }
            if (which != -1) {
                HwLocalePickerWithRegionEx.this.mHWlpsInner.onLocaleSelectedEx(newLocaleInfo, false);
                HwLocalePickerWithRegionEx.this.mHWlpsInner.returnToParentFrameEx();
                return;
            }
            HwLocalePickerWithRegionEx.this.mHWlpsInner.onLocaleSelectedEx(newLocaleInfo, true);
            HwLocalePickerWithRegionEx.this.mHWlpsInner.returnToParentFrameEx();
        }
    }

    private class SetLanguageDialogListener implements DialogInterface.OnClickListener {
        LocaleStore.LocaleInfo locale;

        SetLanguageDialogListener(LocaleStore.LocaleInfo locale2) {
            this.locale = locale2;
        }

        public void onClick(DialogInterface dialogInterface, int which) {
            if (which != -2) {
                HwLocalePickerWithRegionEx.this.mHWlpsInner.createCountry(this.locale);
                return;
            }
            HwLocalePickerWithRegionEx.this.mHWlpsInner.onLocaleSelectedEx(this.locale, false);
            HwLocalePickerWithRegionEx.this.mHWlpsInner.returnToParentFrameEx();
        }
    }

    public HwLocalePickerWithRegionEx(IHwLocalePickerWithRegionInner lpw) {
        this.mHWlpsInner = lpw;
    }

    public void chooseLanguageOrRegion(boolean isClickable, Context mContext, LocaleStore.LocaleInfo locale, int position, boolean mIsLanguageSelect) {
        Context context = mContext;
        LocaleStore.LocaleInfo localeInfo = locale;
        if (!isClickable) {
            int i = position;
        } else if (position < LocalePicker.getLocales().size() + 1) {
            return;
        }
        Locale defaultLocale = Locale.getDefault();
        if (mIsLanguageSelect) {
            boolean isSupportRegion = HwFrameworkFactory.getHwLocaleStoreEx().isSupportRegion(context, locale.getLocale(), defaultLocale.getCountry());
            String displayCountry = LocaleHelper.getDisplayCountry(defaultLocale);
            String displayName = HwFrameworkFactory.getHwLocaleStoreEx().getFullLanguageName(context, locale.getLocale(), defaultLocale);
            if (isSupportRegion) {
                DialogListener mDialogListener = new DialogListener(localeInfo);
                String changeLanguageDesc = context.getString(33686093, new Object[]{displayName});
                String addLanguage = context.getString(17039360);
                String str = changeLanguageDesc;
                new AlertDialog.Builder(context).setMessage(changeLanguageDesc).setCancelable(false).setNegativeButton(addLanguage, mDialogListener).setPositiveButton(context.getString(33685964), mDialogListener).create().show();
            } else {
                SetLanguageDialogListener setLanguageListener = new SetLanguageDialogListener(localeInfo);
                String desc = context.getString(33686092, new Object[]{displayCountry, displayName});
                new AlertDialog.Builder(context).setTitle(context.getString(33686094)).setMessage(desc).setCancelable(false).setNegativeButton(context.getString(17039360), setLanguageListener).setPositiveButton(context.getString(33685969), setLanguageListener).create().show();
            }
        } else if (!locale.getLocale().getCountry().equals(defaultLocale.getCountry())) {
            this.mHWlpsInner.onLocaleSelectedEx(localeInfo, true);
            this.mHWlpsInner.returnToParentFrameEx();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(34013356, container, false);
    }

    public void onViewCreated(Context context, View view, Bundle savedInstanceState) {
        View view2 = view;
        LinearLayout mSearchviewContainer = (LinearLayout) view2.findViewById(34603392);
        mSearchviewContainer.setVisibility(0);
        int mLeftPadding = mSearchviewContainer.getPaddingLeft();
        int mRightPadding = mSearchviewContainer.getPaddingRight();
        final Context context2 = context;
        final LinearLayout linearLayout = mSearchviewContainer;
        final int i = mLeftPadding;
        final int paddingTop = mSearchviewContainer.getPaddingTop();
        final int i2 = mRightPadding;
        AnonymousClass1 r11 = r0;
        final int paddingBottom = mSearchviewContainer.getPaddingBottom();
        AnonymousClass1 r0 = new View.OnApplyWindowInsetsListener() {
            public WindowInsets onApplyWindowInsets(View v, WindowInsets windowInsets) {
                DisplayCutout cutout = windowInsets.getDisplayCutout();
                if (cutout != null) {
                    int rotate = HwLocalePickerWithRegionEx.this.getDisplayRotate(context2);
                    if (1 == rotate) {
                        linearLayout.setPadding(i + cutout.getSafeInsetLeft(), paddingTop, i2, paddingBottom);
                    } else if (3 == rotate) {
                        linearLayout.setPadding(i, paddingTop, i2 + cutout.getSafeInsetRight(), paddingBottom);
                    } else {
                        linearLayout.setPadding(i, paddingTop, i2, paddingBottom);
                    }
                } else {
                    linearLayout.setPadding(i, paddingTop, i2, paddingBottom);
                }
                return windowInsets;
            }
        };
        mSearchviewContainer.setOnApplyWindowInsetsListener(r11);
        SearchView mSearchView = (SearchView) view2.findViewById(34603016);
        if (this.mHWlpsInner.getParentLocaleEx() != null) {
            mSearchView.setQueryHint(this.mHWlpsInner.getTextEx(33686217));
        } else {
            mSearchView.setQueryHint(this.mHWlpsInner.getTextEx(33686132));
        }
        mSearchView.setOnQueryTextListener(this);
        if (this.mHWlpsInner.getPreviousSearch() != null) {
            mSearchView.setIconified(false);
            mSearchView.setActivated(true);
            if (this.mHWlpsInner.getPreviousSearchHadFocus()) {
                mSearchView.requestFocus();
            }
            mSearchView.setQuery(this.mHWlpsInner.getPreviousSearch(), true);
        } else {
            mSearchView.setQuery(null, false);
        }
        this.mHWlpsInner.setSearchView(mSearchView);
        this.mHWlpsInner.getListViewEx().setSelectionFromTop(this.mHWlpsInner.getFirstVisiblePosition(), this.mHWlpsInner.getTopDistance());
    }

    public boolean onQueryTextSubmit(String query) {
        return this.mHWlpsInner.onQueryTextSubmitEx(query);
    }

    public boolean onQueryTextChange(String newText) {
        return this.mHWlpsInner.onQueryTextChangeEx(newText);
    }

    public int getDisplayRotate(Context context) {
        if (context != null) {
            WindowManager wmManager = (WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM);
            if (wmManager != null) {
                return wmManager.getDefaultDisplay().getRotation();
            }
        }
        return 0;
    }
}
