package com.huawei.android.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.app.LocaleStore;

public interface IHwLocalePickerWithRegionEx {
    void chooseLanguageOrRegion(boolean z, Context context, LocaleStore.LocaleInfo localeInfo, int i, boolean z2);

    View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle);

    void onViewCreated(Context context, View view, Bundle bundle);
}
