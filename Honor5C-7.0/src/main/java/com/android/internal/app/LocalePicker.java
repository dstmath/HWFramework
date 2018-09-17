package com.android.internal.app;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.ListFragment;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.RemoteException;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.util.Log;
import android.util.PtmLog;
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
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocalePicker extends ListFragment {
    private static final boolean DEBUG = false;
    private static final String TAG = "LocalePicker";
    private static final String[] pseudoLocales = null;
    LocaleSelectionListener mListener;

    /* renamed from: com.android.internal.app.LocalePicker.1 */
    static class AnonymousClass1 extends ArrayAdapter<LocaleInfo> {
        final /* synthetic */ int val$fieldId;
        final /* synthetic */ LayoutInflater val$inflater;
        final /* synthetic */ int val$layoutId;

        AnonymousClass1(Context $anonymous0, int $anonymous1, int $anonymous2, List $anonymous3, LayoutInflater val$inflater, int val$layoutId, int val$fieldId) {
            this.val$inflater = val$inflater;
            this.val$layoutId = val$layoutId;
            this.val$fieldId = val$fieldId;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            TextView text;
            if (convertView == null) {
                view = this.val$inflater.inflate(this.val$layoutId, parent, (boolean) LocalePicker.DEBUG);
                text = (TextView) view.findViewById(this.val$fieldId);
                view.setTag(text);
            } else {
                view = convertView;
                text = (TextView) convertView.getTag();
            }
            LocaleInfo item = (LocaleInfo) getItem(position);
            text.setText(item.toString());
            text.setTextLocale(item.getLocale());
            return view;
        }
    }

    public static class LocaleInfo implements Comparable<LocaleInfo> {
        static final Collator sCollator = null;
        String label;
        final Locale locale;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.LocalePicker.LocaleInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.LocalePicker.LocaleInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.LocalePicker.LocaleInfo.<clinit>():void");
        }

        public LocaleInfo(String label, Locale locale) {
            this.label = label;
            this.locale = locale;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.LocalePicker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.LocalePicker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.LocalePicker.<clinit>():void");
    }

    public LocalePicker() {
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
        Set<String> realList = new HashSet();
        realList.addAll(getRealLocaleList(context, context.getResources().getStringArray(R.array.supported_locales)));
        List<String> supportedLocales = new ArrayList(realList);
        return (String[]) supportedLocales.toArray(new String[supportedLocales.size()]);
    }

    public static String[] getPseudoLocales() {
        return pseudoLocales;
    }

    public static List<LocaleInfo> getAllAssetLocales(Context context, boolean isInDeveloperMode) {
        Resources resources = context.getResources();
        String[] locales = getSystemAssetLocales();
        List<String> localeList = new ArrayList(locales.length);
        Collections.addAll(localeList, locales);
        if (!isInDeveloperMode) {
            for (String locale : pseudoLocales) {
                localeList.remove(locale);
            }
        }
        Collections.sort(localeList);
        String[] specialLocaleCodes = resources.getStringArray(R.array.special_locale_codes);
        String[] specialLocaleNames = resources.getStringArray(R.array.special_locale_names);
        ArrayList<LocaleInfo> localeInfos = new ArrayList(localeList.size());
        for (String locale2 : localeList) {
            Locale l = Locale.forLanguageTag(locale2.replace('_', '-'));
            if (!(l == null || "und".equals(l.getLanguage()) || l.getLanguage().isEmpty() || l.getCountry().isEmpty())) {
                if (localeInfos.isEmpty()) {
                    localeInfos.add(new LocaleInfo(toTitleCase(l.getDisplayLanguage(l)), l));
                } else {
                    LocaleInfo previous = (LocaleInfo) localeInfos.get(localeInfos.size() - 1);
                    if (!previous.locale.getLanguage().equals(l.getLanguage()) || previous.locale.getLanguage().equals("zz")) {
                        localeInfos.add(new LocaleInfo(toTitleCase(l.getDisplayLanguage(l)), l));
                    } else {
                        previous.label = toTitleCase(getDisplayName(previous.locale, specialLocaleCodes, specialLocaleNames));
                        localeInfos.add(new LocaleInfo(toTitleCase(getDisplayName(l, specialLocaleCodes, specialLocaleNames)), l));
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

    public static ArrayAdapter<LocaleInfo> constructAdapter(Context context, int layoutId, int fieldId) {
        boolean isInDeveloperMode = DEBUG;
        if (Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) != 0) {
            isInDeveloperMode = true;
        }
        return new AnonymousClass1(context, layoutId, fieldId, getAllAssetLocales(context, isInDeveloperMode), (LayoutInflater) context.getSystemService("layout_inflater"), layoutId, fieldId);
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
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            config.setLocales(locales);
            config.userSetLocale = true;
            am.updatePersistentConfiguration(config);
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (RemoteException e) {
        }
    }

    public static LocaleList getLocales() {
        try {
            return ActivityManagerNative.getDefault().getConfiguration().getLocales();
        } catch (RemoteException e) {
            return LocaleList.getDefault();
        }
    }

    private static Set<String> getRealLocaleList(Context context, String[] locales) {
        int i = 0;
        Set<String> realLocales = new HashSet();
        List<String> whiteLanguage = getWhiteLanguage(context);
        List<String> blackLanguage = getBlackLanguage(context);
        for (String localeStr : locales) {
            ULocale locale = ULocale.forLanguageTag(localeStr.replace('_', '-'));
            ULocale compareLocale = ULocale.addLikelySubtags(locale);
            if ((blackLanguage.isEmpty() || !blackLanguage.contains(compareLocale.toLanguageTag())) && (whiteLanguage.isEmpty() || whiteLanguage.contains(compareLocale.getFallback().toLanguageTag()))) {
                realLocales.add(locale.toLanguageTag());
            }
        }
        String[] strArr = pseudoLocales;
        int length = strArr.length;
        while (i < length) {
            locale = ULocale.forLanguageTag(strArr[i].replace('_', '-'));
            if (!blackLanguage.contains(ULocale.addLikelySubtags(locale).toLanguageTag())) {
                realLocales.add(locale.toLanguageTag());
            }
            i++;
        }
        return realLocales;
    }

    private static ArrayList<String> getWhiteLanguage(Context context) {
        ArrayList<String> whiteLanguages = new ArrayList();
        String str = null;
        String whiteLanguagesAmendForCust = null;
        try {
            str = Systemex.getString(context.getContentResolver(), "white_languages");
            whiteLanguagesAmendForCust = System.getString(context.getContentResolver(), "white_languages_amend_for_cust");
        } catch (Exception e) {
            Log.e(TAG, "Could not load default locales", e);
        }
        if (str != null) {
            if (whiteLanguagesAmendForCust != null) {
                str = parseWhiteLanguageAmend(whiteLanguagesAmendForCust, str);
            }
            str = str.replace("tl", "fil");
            String[] assetLocales = getSystemAssetLocales();
            for (String localeStr : str.split(PtmLog.PAIRE_DELIMETER)) {
                String locale_tag = ULocale.addLikelySubtags(ULocale.forLanguageTag(localeStr.replace('_', '-'))).getFallback().toLanguageTag();
                for (String assetLocal : assetLocales) {
                    if (ULocale.addLikelySubtags(ULocale.forLanguageTag(assetLocal)).getFallback().toLanguageTag().equals(locale_tag)) {
                        whiteLanguages.add(locale_tag);
                        break;
                    }
                }
            }
        }
        context.getResources().getAssets();
        String[] downloaded_language = AssetManager.getSharedResList();
        if (downloaded_language != null) {
            for (String locale : downloaded_language) {
                boolean repeat = DEBUG;
                String down_tag = ULocale.addLikelySubtags(ULocale.forLanguageTag(locale)).getFallback().toLanguageTag();
                for (String white : whiteLanguages) {
                    if (down_tag.equals(white)) {
                        repeat = true;
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
        int length;
        int i = 0;
        String amendAddString = null;
        String amendDelStrings = null;
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
            for (String amendAddLanguage : amendAddString.split(PtmLog.PAIRE_DELIMETER)) {
                if (!whiteStrings.contains(amendAddLanguage)) {
                    whiteStrings = whiteStrings + PtmLog.PAIRE_DELIMETER + amendAddLanguage;
                }
            }
        }
        if (amendDelStrings != null) {
            String[] split = amendDelStrings.split(PtmLog.PAIRE_DELIMETER);
            length = split.length;
            while (i < length) {
                String delStr = split[i];
                if (whiteStrings.contains(delStr + PtmLog.PAIRE_DELIMETER)) {
                    whiteStrings = whiteStrings.replace(delStr + PtmLog.PAIRE_DELIMETER, "");
                } else if (whiteStrings.contains(delStr)) {
                    whiteStrings = whiteStrings.replace(delStr, "");
                }
                i++;
            }
        }
        return whiteStrings;
    }

    public static ArrayList<String> getBlackLanguage(Context context) {
        ArrayList<String> blackLanguages = new ArrayList();
        String blackStrings = null;
        try {
            blackStrings = Systemex.getString(context.getContentResolver(), "black_languages");
        } catch (Exception e) {
            Log.e(TAG, "Could not load default locales", e);
        }
        if (blackStrings != null) {
            for (String localeStr : blackStrings.replace("tl", "fil").split(PtmLog.PAIRE_DELIMETER)) {
                blackLanguages.add(ULocale.addLikelySubtags(ULocale.forLanguageTag(localeStr.replace('_', '-'))).toLanguageTag());
            }
        }
        return blackLanguages;
    }
}
