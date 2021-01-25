package huawei.com.android.internal.app;

import android.content.Context;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import com.android.internal.app.IHwLocalePickerWithRegionInner;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore;
import com.huawei.android.app.IHwLocalePickerWithRegionEx;
import java.util.Locale;

public final class HwLocalePickerWithRegionEx implements IHwLocalePickerWithRegionEx, SearchView.OnQueryTextListener {
    private IHwLocalePickerWithRegionInner mInner = null;

    public HwLocalePickerWithRegionEx(IHwLocalePickerWithRegionInner inner) {
        this.mInner = inner;
    }

    public void chooseLanguageOrRegion(boolean isClickable, Context context, LocaleStore.LocaleInfo locale, int position, boolean isLanguageSelect) {
        IHwLocalePickerWithRegionInner iHwLocalePickerWithRegionInner;
        if (!isClickable || position >= LocalePicker.getLocales().size() + 1) {
            Locale defaultLocale = Locale.getDefault();
            if ((isLanguageSelect || !locale.getLocale().getCountry().equals(defaultLocale.getCountry())) && (iHwLocalePickerWithRegionInner = this.mInner) != null) {
                iHwLocalePickerWithRegionInner.onLocaleSelectedEx(locale);
                this.mInner.returnToParentFrameEx();
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(34013415, container, false);
    }

    public void onViewCreated(final Context context, View view, Bundle savedInstanceState) {
        final LinearLayout searchViewContainer = (LinearLayout) view.findViewById(34603476);
        searchViewContainer.setVisibility(0);
        FrameLayout listViewContainer = (FrameLayout) view.findViewById(34603406);
        LinearLayout emptyRoot = (LinearLayout) view.findViewById(34603070);
        IHwLocalePickerWithRegionInner iHwLocalePickerWithRegionInner = this.mInner;
        if (iHwLocalePickerWithRegionInner != null) {
            iHwLocalePickerWithRegionInner.setEmptyView(listViewContainer, emptyRoot);
            final int leftPadding = searchViewContainer.getPaddingLeft();
            final int rightPadding = searchViewContainer.getPaddingRight();
            final int topPadding = searchViewContainer.getPaddingTop();
            final int bottomPadding = searchViewContainer.getPaddingBottom();
            searchViewContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                /* class huawei.com.android.internal.app.HwLocalePickerWithRegionEx.AnonymousClass1 */

                @Override // android.view.View.OnApplyWindowInsetsListener
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    DisplayCutout cutout = windowInsets.getDisplayCutout();
                    if (cutout != null) {
                        int rotate = HwLocalePickerWithRegionEx.this.getDisplayRotate(context);
                        if (rotate == 1) {
                            searchViewContainer.setPadding(leftPadding + cutout.getSafeInsetLeft(), topPadding, rightPadding, bottomPadding);
                        } else if (rotate == 3) {
                            searchViewContainer.setPadding(leftPadding, topPadding, rightPadding + cutout.getSafeInsetRight(), bottomPadding);
                        } else {
                            searchViewContainer.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
                        }
                    } else {
                        searchViewContainer.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
                    }
                    return windowInsets;
                }
            });
            setQuery(view);
        }
    }

    private void setQuery(View view) {
        SearchView searchView = (SearchView) view.findViewById(34603016);
        if (this.mInner.getParentLocaleEx() != null) {
            searchView.setQueryHint(this.mInner.getTextEx(33686204));
        } else {
            searchView.setQueryHint(this.mInner.getTextEx(33685799));
        }
        searchView.setOnQueryTextListener(this);
        if (this.mInner.getPreviousSearch() != null) {
            searchView.setIconified(false);
            searchView.setActivated(true);
            if (this.mInner.getPreviousSearchHadFocus()) {
                searchView.requestFocus();
            }
            searchView.setQuery(this.mInner.getPreviousSearch(), true);
        } else {
            searchView.setQuery(null, false);
        }
        this.mInner.setSearchView(searchView);
        this.mInner.getListViewEx().setSelectionFromTop(this.mInner.getFirstVisiblePosition(), this.mInner.getTopDistance());
    }

    @Override // android.widget.SearchView.OnQueryTextListener
    public boolean onQueryTextSubmit(String query) {
        IHwLocalePickerWithRegionInner iHwLocalePickerWithRegionInner = this.mInner;
        if (iHwLocalePickerWithRegionInner == null) {
            return false;
        }
        return iHwLocalePickerWithRegionInner.onQueryTextSubmitEx(query);
    }

    @Override // android.widget.SearchView.OnQueryTextListener
    public boolean onQueryTextChange(String newText) {
        IHwLocalePickerWithRegionInner iHwLocalePickerWithRegionInner = this.mInner;
        if (iHwLocalePickerWithRegionInner == null) {
            return false;
        }
        return iHwLocalePickerWithRegionInner.onQueryTextChangeEx(newText);
    }

    public int getDisplayRotate(Context context) {
        WindowManager windowManager;
        if (context == null || (windowManager = (WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM)) == null) {
            return 0;
        }
        return windowManager.getDefaultDisplay().getRotation();
    }
}
