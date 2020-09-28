package android.speech.tts;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import org.xmlpull.v1.XmlPullParserException;

public class TtsEngines {
    private static final boolean DBG = false;
    private static final String LOCALE_DELIMITER_NEW = "_";
    private static final String LOCALE_DELIMITER_OLD = "-";
    private static final String TAG = "TtsEngines";
    private static final String XML_TAG_NAME = "tts-engine";
    private static final Map<String, String> sNormalizeCountry;
    private static final Map<String, String> sNormalizeLanguage;
    private final Context mContext;

    static {
        HashMap<String, String> normalizeLanguage = new HashMap<>();
        String[] iSOLanguages = Locale.getISOLanguages();
        for (String language : iSOLanguages) {
            try {
                normalizeLanguage.put(new Locale(language).getISO3Language(), language);
            } catch (MissingResourceException e) {
            }
        }
        sNormalizeLanguage = Collections.unmodifiableMap(normalizeLanguage);
        HashMap<String, String> normalizeCountry = new HashMap<>();
        String[] iSOCountries = Locale.getISOCountries();
        for (String country : iSOCountries) {
            try {
                normalizeCountry.put(new Locale("", country).getISO3Country(), country);
            } catch (MissingResourceException e2) {
            }
        }
        sNormalizeCountry = Collections.unmodifiableMap(normalizeCountry);
    }

    @UnsupportedAppUsage
    public TtsEngines(Context ctx) {
        this.mContext = ctx;
    }

    public String getDefaultEngine() {
        String engine = Settings.Secure.getString(this.mContext.getContentResolver(), Settings.Secure.TTS_DEFAULT_SYNTH);
        return isEngineInstalled(engine) ? engine : getHighestRankedEngineName();
    }

    public String getHighestRankedEngineName() {
        List<TextToSpeech.EngineInfo> engines = getEngines();
        if (engines.size() <= 0 || !engines.get(0).system) {
            return null;
        }
        return engines.get(0).name;
    }

    public TextToSpeech.EngineInfo getEngineInfo(String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE);
        intent.setPackage(packageName);
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 65536);
        if (resolveInfos == null || resolveInfos.size() != 1) {
            return null;
        }
        return getEngineInfo(resolveInfos.get(0), pm);
    }

    @UnsupportedAppUsage
    public List<TextToSpeech.EngineInfo> getEngines() {
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(new Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 65536);
        if (resolveInfos == null) {
            return Collections.emptyList();
        }
        List<TextToSpeech.EngineInfo> engines = new ArrayList<>(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            TextToSpeech.EngineInfo engine = getEngineInfo(resolveInfo, pm);
            if (engine != null) {
                engines.add(engine);
            }
        }
        Collections.sort(engines, EngineInfoComparator.INSTANCE);
        return engines;
    }

    private boolean isSystemEngine(ServiceInfo info) {
        ApplicationInfo appInfo = info.applicationInfo;
        return (appInfo == null || (appInfo.flags & 1) == 0) ? false : true;
    }

    public boolean isEngineInstalled(String engine) {
        if (engine == null || getEngineInfo(engine) == null) {
            return false;
        }
        return true;
    }

    @UnsupportedAppUsage
    public Intent getSettingsIntent(String engine) {
        ServiceInfo service;
        String settings;
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE);
        intent.setPackage(engine);
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 65664);
        if (resolveInfos == null || resolveInfos.size() != 1 || (service = resolveInfos.get(0).serviceInfo) == null || (settings = settingsActivityFromServiceInfo(service, pm)) == null) {
            return null;
        }
        Intent i = new Intent();
        i.setClassName(engine, settings);
        return i;
    }

    private String settingsActivityFromServiceInfo(ServiceInfo si, PackageManager pm) {
        int type;
        XmlResourceParser parser = null;
        try {
            XmlResourceParser parser2 = si.loadXmlMetaData(pm, TextToSpeech.Engine.SERVICE_META_DATA);
            if (parser2 == null) {
                Log.w(TAG, "No meta-data found for :" + si);
                if (parser2 != null) {
                    parser2.close();
                }
                return null;
            }
            Resources res = pm.getResourcesForApplication(si.applicationInfo);
            do {
                type = parser2.next();
                if (type == 1) {
                    parser2.close();
                    return null;
                }
            } while (type != 2);
            if (!XML_TAG_NAME.equals(parser2.getName())) {
                Log.w(TAG, "Package " + si + " uses unknown tag :" + parser2.getName());
                parser2.close();
                return null;
            }
            TypedArray array = res.obtainAttributes(Xml.asAttributeSet(parser2), R.styleable.TextToSpeechEngine);
            String settings = array.getString(0);
            array.recycle();
            parser2.close();
            return settings;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Could not load resources for : " + si);
            if (0 != 0) {
                parser.close();
            }
            return null;
        } catch (XmlPullParserException e2) {
            Log.w(TAG, "Error parsing metadata for " + si + SettingsStringUtil.DELIMITER + e2);
            if (0 != 0) {
                parser.close();
            }
            return null;
        } catch (IOException e3) {
            Log.w(TAG, "Error parsing metadata for " + si + SettingsStringUtil.DELIMITER + e3);
            if (0 != 0) {
                parser.close();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                parser.close();
            }
            throw th;
        }
    }

    private TextToSpeech.EngineInfo getEngineInfo(ResolveInfo resolve, PackageManager pm) {
        ServiceInfo service = resolve.serviceInfo;
        if (service == null) {
            return null;
        }
        TextToSpeech.EngineInfo engine = new TextToSpeech.EngineInfo();
        engine.name = service.packageName;
        CharSequence label = service.loadLabel(pm);
        engine.label = TextUtils.isEmpty(label) ? engine.name : label.toString();
        engine.icon = service.getIconResource();
        engine.priority = resolve.priority;
        engine.system = isSystemEngine(service);
        return engine;
    }

    /* access modifiers changed from: private */
    public static class EngineInfoComparator implements Comparator<TextToSpeech.EngineInfo> {
        static EngineInfoComparator INSTANCE = new EngineInfoComparator();

        private EngineInfoComparator() {
        }

        public int compare(TextToSpeech.EngineInfo lhs, TextToSpeech.EngineInfo rhs) {
            if (lhs.system && !rhs.system) {
                return -1;
            }
            if (!rhs.system || lhs.system) {
                return rhs.priority - lhs.priority;
            }
            return 1;
        }
    }

    @UnsupportedAppUsage
    public Locale getLocalePrefForEngine(String engineName) {
        return getLocalePrefForEngine(engineName, Settings.Secure.getString(this.mContext.getContentResolver(), Settings.Secure.TTS_DEFAULT_LOCALE));
    }

    public Locale getLocalePrefForEngine(String engineName, String prefValue) {
        String localeString = parseEnginePrefFromList(prefValue, engineName);
        if (TextUtils.isEmpty(localeString)) {
            return Locale.getDefault();
        }
        Locale result = parseLocaleString(localeString);
        if (result != null) {
            return result;
        }
        Log.w(TAG, "Failed to parse locale " + localeString + ", returning en_US instead");
        return Locale.US;
    }

    public boolean isLocaleSetToDefaultForEngine(String engineName) {
        return TextUtils.isEmpty(parseEnginePrefFromList(Settings.Secure.getString(this.mContext.getContentResolver(), Settings.Secure.TTS_DEFAULT_LOCALE), engineName));
    }

    @UnsupportedAppUsage
    public Locale parseLocaleString(String localeString) {
        String language = "";
        String country = "";
        String variant = "";
        if (!TextUtils.isEmpty(localeString)) {
            String[] split = localeString.split("[-_]");
            language = split[0].toLowerCase();
            if (split.length == 0) {
                Log.w(TAG, "Failed to convert " + localeString + " to a valid Locale object. Only separators");
                return null;
            } else if (split.length > 3) {
                Log.w(TAG, "Failed to convert " + localeString + " to a valid Locale object. Too many separators");
                return null;
            } else {
                if (split.length >= 2) {
                    country = split[1].toUpperCase();
                }
                if (split.length >= 3) {
                    variant = split[2];
                }
            }
        }
        String normalizedLanguage = sNormalizeLanguage.get(language);
        if (normalizedLanguage != null) {
            language = normalizedLanguage;
        }
        String normalizedCountry = sNormalizeCountry.get(country);
        if (normalizedCountry != null) {
            country = normalizedCountry;
        }
        Locale result = new Locale(language, country, variant);
        try {
            result.getISO3Language();
            result.getISO3Country();
            return result;
        } catch (MissingResourceException e) {
            Log.w(TAG, "Failed to convert " + localeString + " to a valid Locale object.");
            return null;
        }
    }

    @UnsupportedAppUsage
    public static Locale normalizeTTSLocale(Locale ttsLocale) {
        String normalizedCountry;
        String normalizedLanguage;
        String language = ttsLocale.getLanguage();
        if (!TextUtils.isEmpty(language) && (normalizedLanguage = sNormalizeLanguage.get(language)) != null) {
            language = normalizedLanguage;
        }
        String country = ttsLocale.getCountry();
        if (!TextUtils.isEmpty(country) && (normalizedCountry = sNormalizeCountry.get(country)) != null) {
            country = normalizedCountry;
        }
        return new Locale(language, country, ttsLocale.getVariant());
    }

    public static String[] toOldLocaleStringFormat(Locale locale) {
        String[] ret = {"", "", ""};
        try {
            ret[0] = locale.getISO3Language();
            ret[1] = locale.getISO3Country();
            ret[2] = locale.getVariant();
            return ret;
        } catch (MissingResourceException e) {
            return new String[]{"eng", "USA", ""};
        }
    }

    private static String parseEnginePrefFromList(String prefValue, String engineName) {
        if (TextUtils.isEmpty(prefValue)) {
            return null;
        }
        String[] prefValues = prefValue.split(SmsManager.REGEX_PREFIX_DELIMITER);
        for (String value : prefValues) {
            int delimiter = value.indexOf(58);
            if (delimiter > 0 && engineName.equals(value.substring(0, delimiter))) {
                return value.substring(delimiter + 1);
            }
        }
        return null;
    }

    @UnsupportedAppUsage
    public synchronized void updateLocalePrefForEngine(String engineName, Locale newLocale) {
        Settings.Secure.putString(this.mContext.getContentResolver(), Settings.Secure.TTS_DEFAULT_LOCALE, updateValueInCommaSeparatedList(Settings.Secure.getString(this.mContext.getContentResolver(), Settings.Secure.TTS_DEFAULT_LOCALE), engineName, newLocale != null ? newLocale.toString() : "").toString());
    }

    private String updateValueInCommaSeparatedList(String list, String key, String newValue) {
        StringBuilder newPrefList = new StringBuilder();
        if (TextUtils.isEmpty(list)) {
            newPrefList.append(key);
            newPrefList.append(':');
            newPrefList.append(newValue);
        } else {
            String[] prefValues = list.split(SmsManager.REGEX_PREFIX_DELIMITER);
            boolean found = false;
            boolean first = true;
            for (String value : prefValues) {
                int delimiter = value.indexOf(58);
                if (delimiter > 0) {
                    if (key.equals(value.substring(0, delimiter))) {
                        if (first) {
                            first = false;
                        } else {
                            newPrefList.append(',');
                        }
                        found = true;
                        newPrefList.append(key);
                        newPrefList.append(':');
                        newPrefList.append(newValue);
                    } else {
                        if (first) {
                            first = false;
                        } else {
                            newPrefList.append(',');
                        }
                        newPrefList.append(value);
                    }
                }
            }
            if (!found) {
                newPrefList.append(',');
                newPrefList.append(key);
                newPrefList.append(':');
                newPrefList.append(newValue);
            }
        }
        return newPrefList.toString();
    }
}
