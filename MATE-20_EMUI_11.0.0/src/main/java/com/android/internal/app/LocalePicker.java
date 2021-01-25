package com.android.internal.app;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.ListFragment;
import android.app.backup.BackupManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.HwAssetManagerEx;
import android.content.res.Resources;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.R;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocalePicker extends ListFragment implements IHwLocalePickerInner {
    private static final boolean DEBUG = false;
    private static final String TAG = "LocalePicker";
    private static final String[] pseudoLocales = {"en-XA", "ar-XB"};
    LocaleSelectionListener mListener;

    public interface LocaleSelectionListener {
        void onLocaleSelected(Locale locale);
    }

    public static class LocaleInfo implements Comparable<LocaleInfo> {
        static final Collator sCollator = Collator.getInstance();
        String label;
        final Locale locale;

        public LocaleInfo(String label2, Locale locale2) {
            this.label = label2;
            this.locale = locale2;
        }

        public String getLabel() {
            return this.label;
        }

        @UnsupportedAppUsage
        public Locale getLocale() {
            return this.locale;
        }

        @Override // java.lang.Object
        public String toString() {
            return this.label;
        }

        public int compareTo(LocaleInfo another) {
            return sCollator.compare(this.label, another.label);
        }
    }

    public static String[] getSystemAssetLocales() {
        String[] internal = Resources.getSystem().getAssets().getLocales();
        String[] shared = HwAssetManagerEx.getSharedResList();
        if (shared == null) {
            shared = new String[0];
        }
        String[] result = (String[]) Arrays.copyOf(internal, internal.length + shared.length);
        System.arraycopy(shared, 0, result, internal.length, shared.length);
        return result;
    }

    public static String[] getSupportedLocales(Context context) {
        List<String> supportedLocales = new ArrayList<>(getRealLocaleList(context, context.getResources().getStringArray(R.array.supported_locales)));
        return (String[]) supportedLocales.toArray(new String[supportedLocales.size()]);
    }

    public static List<LocaleInfo> getAllAssetLocales(Context context, boolean isInDeveloperMode) {
        Resources resources = context.getResources();
        String[] locales = getSystemAssetLocales();
        List<String> localeList = new ArrayList<>(locales.length);
        Collections.addAll(localeList, locales);
        Collections.sort(localeList);
        String[] specialLocaleCodes = resources.getStringArray(R.array.special_locale_codes);
        String[] specialLocaleNames = resources.getStringArray(R.array.special_locale_names);
        ArrayList<LocaleInfo> localeInfos = new ArrayList<>(localeList.size());
        for (String locale : localeList) {
            Locale l = Locale.forLanguageTag(locale.replace('_', '-'));
            if (l != null && !"und".equals(l.getLanguage()) && !l.getLanguage().isEmpty() && !l.getCountry().isEmpty()) {
                if (isInDeveloperMode || !LocaleList.isPseudoLocale(l)) {
                    if (localeInfos.isEmpty()) {
                        localeInfos.add(new LocaleInfo(toTitleCase(l.getDisplayLanguage(l)), l));
                    } else {
                        LocaleInfo previous = localeInfos.get(localeInfos.size() - 1);
                        if (!previous.locale.getLanguage().equals(l.getLanguage()) || previous.locale.getLanguage().equals("zz")) {
                            localeInfos.add(new LocaleInfo(toTitleCase(l.getDisplayLanguage(l)), l));
                        } else {
                            previous.label = toTitleCase(getDisplayName(previous.locale, specialLocaleCodes, specialLocaleNames));
                            localeInfos.add(new LocaleInfo(toTitleCase(getDisplayName(l, specialLocaleCodes, specialLocaleNames)), l));
                        }
                    }
                }
            }
        }
        Collections.sort(localeInfos);
        return localeInfos;
    }

    public static ArrayAdapter<LocaleInfo> constructAdapter(Context context) {
        return constructAdapter(context, R.layout.locale_picker_item, R.id.locale);
    }

    public static ArrayAdapter<LocaleInfo> constructAdapter(Context context, final int layoutId, final int fieldId) {
        boolean isInDeveloperMode = false;
        if (Settings.Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) != 0) {
            isInDeveloperMode = true;
        }
        List<LocaleInfo> localeInfos = getAllAssetLocales(context, isInDeveloperMode);
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ArrayAdapter<LocaleInfo>(context, localeInfos, layoutId, fieldId) {
            /* class com.android.internal.app.LocalePicker.AnonymousClass1 */

            @Override // android.widget.ArrayAdapter, android.widget.Adapter
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView text;
                View view;
                if (convertView == null) {
                    view = inflater.inflate(layoutId, parent, false);
                    text = (TextView) view.findViewById(fieldId);
                    view.setTag(text);
                } else {
                    view = convertView;
                    text = (TextView) view.getTag();
                }
                LocaleInfo item = (LocaleInfo) getItem(position);
                text.setText(item.toString());
                text.setTextLocale(item.getLocale());
                return view;
            }
        };
    }

    private static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String getDisplayName(Locale l, String[] specialLocaleCodes, String[] specialLocaleNames) {
        String code = l.toString();
        for (int i = 0; i < specialLocaleCodes.length; i++) {
            if (specialLocaleCodes[i].equals(code)) {
                return specialLocaleNames[i];
            }
        }
        return LocaleHelper.getDisplayName(l, true);
    }

    @Override // android.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(constructAdapter(getActivity()));
    }

    public void setLocaleSelectionListener(LocaleSelectionListener listener) {
        this.mListener = listener;
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        getListView().requestFocus();
    }

    @Override // android.app.ListFragment
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (this.mListener != null) {
            this.mListener.onLocaleSelected(((LocaleInfo) getListAdapter().getItem(position)).locale);
        }
    }

    @UnsupportedAppUsage
    public static void updateLocale(Locale locale) {
        updateLocales(new LocaleList(locale));
    }

    @UnsupportedAppUsage
    public static void updateLocales(LocaleList locales) {
        try {
            IActivityManager am = ActivityManager.getService();
            Configuration config = am.getConfiguration();
            config.setLocales(checkLocaleList(locales));
            config.userSetLocale = true;
            am.updatePersistentConfiguration(config);
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (RemoteException e) {
        }
    }

    private static LocaleList checkLocaleList(LocaleList locales) {
        List<Locale> nList = new ArrayList<>();
        int length = locales.size();
        for (int i = 0; i < length; i++) {
            Locale locale = locales.get(i);
            if (!locale.getLanguage().equals("my") || locale.getUnicodeLocaleType("nu") != null) {
                nList.add(locale);
            } else {
                String languageTag = locale.toLanguageTag();
                nList.add(Locale.forLanguageTag(languageTag + "-u-nu-latn"));
            }
        }
        return new LocaleList((Locale[]) nList.toArray(new Locale[0]));
    }

    @UnsupportedAppUsage
    public static LocaleList getLocales() {
        try {
            return ActivityManager.getService().getConfiguration().getLocales();
        } catch (RemoteException e) {
            return LocaleList.getDefault();
        }
    }

    private static Set<String> getRealLocaleList(Context context, String[] locales) {
        Set<String> realLocales = new HashSet<>();
        List<String> whiteLanguage = getWhiteLanguage(context);
        for (String localeStr : locales) {
            ULocale locale = ULocale.forLanguageTag(localeStr);
            ULocale.Builder localeBuilder = new ULocale.Builder().setLanguageTag("en");
            try {
                localeBuilder.setLocale(locale).setExtension('u', "");
            } catch (IllformedLocaleException e) {
                Log.e(TAG, "Error locale: " + locale.toLanguageTag());
            }
            ULocale compareLocale = ULocale.addLikelySubtags(localeBuilder.build());
            if (whiteLanguage.isEmpty() || whiteLanguage.contains(compareLocale.getFallback().toLanguageTag())) {
                realLocales.add(locale.toLanguageTag());
            }
        }
        return realLocales;
    }

    private static ArrayList<String> getWhiteLanguage(Context context) {
        return HwFrameworkFactory.getHwLocalePickerEx().getWhiteLanguage(context);
    }

    public static ArrayList<String> getBlackLanguage(Context context) {
        ArrayList<String> blackLanguages = new ArrayList<>();
        String blackStrings = null;
        try {
            blackStrings = Settings.System.getString(context.getContentResolver(), "black_languages");
        } catch (Exception e) {
            Log.e(TAG, "Could not load default locales", e);
        }
        if (blackStrings != null) {
            for (String localeStr : blackStrings.replace("tl", "fil").split(SmsManager.REGEX_PREFIX_DELIMITER)) {
                String correctLocaleStr = localeStr.replace('_', '-');
                blackLanguages.add(correctLocaleStr);
                if (correctLocaleStr.length() >= 5) {
                    blackLanguages.add(ULocale.addLikelySubtags(ULocale.forLanguageTag(correctLocaleStr)).toLanguageTag());
                }
            }
        }
        return blackLanguages;
    }

    public static Set<String> getRealLocaleListStaticEx(Context context, String[] locales) {
        return getRealLocaleList(context, locales);
    }

    @Override // com.android.internal.app.IHwLocalePickerInner
    public Set<String> getRealLocaleListEx(Context context, String[] locales) {
        return getRealLocaleList(context, locales);
    }
}
