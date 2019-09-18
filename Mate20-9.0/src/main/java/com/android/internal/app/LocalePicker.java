package com.android.internal.app;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.ListFragment;
import android.app.backup.BackupManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocalePicker extends ListFragment implements IHwLocalePickerInner {
    private static final boolean DEBUG = false;
    private static final String TAG = "LocalePicker";
    private static final String[] pseudoLocales = {"en-XA", "ar-XB"};
    LocaleSelectionListener mListener;

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

        public Locale getLocale() {
            return this.locale;
        }

        public String toString() {
            return this.label;
        }

        public int compareTo(LocaleInfo another) {
            return sCollator.compare(this.label, another.label);
        }
    }

    public interface LocaleSelectionListener {
        void onLocaleSelected(Locale locale);
    }

    public static String[] getSystemAssetLocales() {
        String[] internal = Resources.getSystem().getAssets().getLocales();
        Resources.getSystem().getAssets();
        String[] shared = AssetManager.getSharedResList();
        if (shared == null) {
            shared = new String[0];
        }
        String[] result = (String[]) Arrays.copyOf(internal, internal.length + shared.length);
        System.arraycopy(shared, 0, result, internal.length, shared.length);
        return result;
    }

    public static String[] getSupportedLocales(Context context) {
        Set<String> realList = new HashSet<>();
        realList.addAll(getRealLocaleList(context, context.getResources().getStringArray(17236083)));
        List<String> supportedLocales = new ArrayList<>(realList);
        return (String[]) supportedLocales.toArray(new String[supportedLocales.size()]);
    }

    public static List<LocaleInfo> getAllAssetLocales(Context context, boolean isInDeveloperMode) {
        Resources resources = context.getResources();
        String[] locales = getSystemAssetLocales();
        List<String> localeList = new ArrayList<>(locales.length);
        Collections.addAll(localeList, locales);
        Collections.sort(localeList);
        String[] specialLocaleCodes = resources.getStringArray(17236081);
        String[] specialLocaleNames = resources.getStringArray(17236082);
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
        return constructAdapter(context, 17367174, 16909056);
    }

    public static ArrayAdapter<LocaleInfo> constructAdapter(Context context, int layoutId, int fieldId) {
        boolean isInDeveloperMode = false;
        if (Settings.Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) != 0) {
            isInDeveloperMode = true;
        }
        List<LocaleInfo> localeInfos = getAllAssetLocales(context, isInDeveloperMode);
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        final int i = layoutId;
        final int i2 = fieldId;
        AnonymousClass1 r1 = new ArrayAdapter<LocaleInfo>(context, layoutId, fieldId, localeInfos) {
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView text;
                View view;
                if (convertView == null) {
                    view = layoutInflater.inflate(i, parent, false);
                    text = (TextView) view.findViewById(i2);
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
        return r1;
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
        return l.getDisplayName(l);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(constructAdapter(getActivity()));
    }

    public void setLocaleSelectionListener(LocaleSelectionListener listener) {
        this.mListener = listener;
    }

    public void onResume() {
        super.onResume();
        getListView().requestFocus();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        if (this.mListener != null) {
            this.mListener.onLocaleSelected(((LocaleInfo) getListAdapter().getItem(position)).locale);
        }
    }

    public static void updateLocale(Locale locale) {
        updateLocales(new LocaleList(new Locale[]{locale}));
    }

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
        List<String> blackLanguage = getBlackLanguage(context);
        for (String localeStr : locales) {
            ULocale locale = ULocale.forLanguageTag(localeStr.replace('_', '-'));
            ULocale compareLocale = ULocale.addLikelySubtags(locale);
            if ((blackLanguage.isEmpty() || !blackLanguage.contains(compareLocale.toLanguageTag())) && (whiteLanguage.isEmpty() || whiteLanguage.contains(compareLocale.getFallback().toLanguageTag()))) {
                realLocales.add(locale.toLanguageTag());
            }
        }
        for (String localeStr2 : pseudoLocales) {
            ULocale locale2 = ULocale.forLanguageTag(localeStr2.replace('_', '-'));
            if (!blackLanguage.contains(ULocale.addLikelySubtags(locale2).toLanguageTag())) {
                realLocales.add(locale2.toLanguageTag());
            }
        }
        return realLocales;
    }

    private static ArrayList<String> getWhiteLanguage(Context context) {
        ArrayList<String> whiteLanguages = new ArrayList<>();
        String whiteStrings = null;
        String whiteLanguagesAmendForCust = null;
        try {
            whiteStrings = Settings.System.getString(context.getContentResolver(), "white_languages");
            whiteLanguagesAmendForCust = Settings.System.getString(context.getContentResolver(), "white_languages_amend_for_cust");
        } catch (Exception e) {
            Log.e(TAG, "Could not load default locales", e);
        }
        if (whiteStrings != null) {
            if (whiteLanguagesAmendForCust != null) {
                whiteStrings = parseWhiteLanguageAmend(whiteLanguagesAmendForCust, whiteStrings);
            }
            String whiteStrings2 = whiteStrings.replace("tl", "fil");
            String[] assetLocales = getSystemAssetLocales();
            for (String localeStr : whiteStrings2.split(",")) {
                String locale_tag = ULocale.addLikelySubtags(ULocale.forLanguageTag(localeStr.replace('_', '-'))).getFallback().toLanguageTag();
                int length = assetLocales.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (ULocale.addLikelySubtags(ULocale.forLanguageTag(assetLocales[i])).getFallback().toLanguageTag().equals(locale_tag)) {
                        whiteLanguages.add(locale_tag);
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
        context.getResources().getAssets();
        String[] downloaded_language = AssetManager.getSharedResList();
        if (downloaded_language != null) {
            for (String locale : downloaded_language) {
                boolean repeat = false;
                String down_tag = ULocale.addLikelySubtags(ULocale.forLanguageTag(locale)).getFallback().toLanguageTag();
                Iterator<String> it = whiteLanguages.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (down_tag.equals(it.next())) {
                            repeat = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!repeat) {
                    whiteLanguages.add(down_tag);
                }
            }
        }
        return whiteLanguages;
    }

    private static String parseWhiteLanguageAmend(String whiteLanguagesAmendForCust, String whiteStrings) {
        String amendDelStrings = null;
        String amendAddString = null;
        for (String operLanguagesStr : whiteLanguagesAmendForCust.split(";")) {
            String[] operLanguagesArr = operLanguagesStr.split(":");
            if (2 == operLanguagesArr.length) {
                if ("add".equalsIgnoreCase(operLanguagesArr[0])) {
                    amendAddString = operLanguagesArr[1];
                } else {
                    amendDelStrings = operLanguagesArr[1];
                }
            }
        }
        if (amendAddString != null) {
            String whiteStrings2 = whiteStrings;
            for (String amendAddLanguage : amendAddString.split(",")) {
                if (!whiteStrings2.contains(amendAddLanguage)) {
                    whiteStrings2 = whiteStrings2 + "," + amendAddLanguage;
                }
            }
            whiteStrings = whiteStrings2;
        }
        if (amendDelStrings != null) {
            for (String delStr : amendDelStrings.split(",")) {
                if (whiteStrings.contains(delStr + ",")) {
                    whiteStrings = whiteStrings.replace(delStr + ",", "");
                } else if (whiteStrings.contains(delStr)) {
                    whiteStrings = whiteStrings.replace(delStr, "");
                }
            }
        }
        return whiteStrings;
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
            for (String localeStr : blackStrings.replace("tl", "fil").split(",")) {
                String correctLocaleStr = localeStr.replace('_', '-');
                blackLanguages.add(correctLocaleStr);
                blackLanguages.add(ULocale.addLikelySubtags(ULocale.forLanguageTag(correctLocaleStr)).toLanguageTag());
            }
        }
        return blackLanguages;
    }

    public String[] getSupportedLanguagesFromConfigEx(Context context) {
        return HwFrameworkFactory.getHwLocalePickerEx(this, context).getSupportedLanguagesFromConfig();
    }

    public Set<String> getRealLocaleListEx(Context context, String[] locales) {
        return getRealLocaleList(context, locales);
    }
}
