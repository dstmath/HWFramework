package com.android.server.wm;

import android.content.res.CompatibilityInfo;

final class StartingData {
    final CompatibilityInfo compatInfo;
    final int icon;
    final int labelRes;
    final int logo;
    final CharSequence nonLocalizedLabel;
    final String pkg;
    final int theme;
    final int windowFlags;

    StartingData(String _pkg, int _theme, CompatibilityInfo _compatInfo, CharSequence _nonLocalizedLabel, int _labelRes, int _icon, int _logo, int _windowFlags) {
        this.pkg = _pkg;
        this.theme = _theme;
        this.compatInfo = _compatInfo;
        this.nonLocalizedLabel = _nonLocalizedLabel;
        this.labelRes = _labelRes;
        this.icon = _icon;
        this.logo = _logo;
        this.windowFlags = _windowFlags;
    }
}
