package ohos.global.icu.impl.duration.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.util.ICUUncheckedIOException;

public class ResourceBasedPeriodFormatterDataService extends PeriodFormatterDataService {
    private static final String PATH = "data/";
    private static final ResourceBasedPeriodFormatterDataService singleton = new ResourceBasedPeriodFormatterDataService();
    private Collection<String> availableLocales;
    private Map<String, PeriodFormatterData> cache = new HashMap();
    private PeriodFormatterData lastData = null;
    private String lastLocale = null;

    public static ResourceBasedPeriodFormatterDataService getInstance() {
        return singleton;
    }

    private ResourceBasedPeriodFormatterDataService() {
        ArrayList arrayList = new ArrayList();
        InputStream requiredStream = ICUData.getRequiredStream(getClass(), "data/index.txt");
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(requiredStream, "UTF-8"));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                String trim = readLine.trim();
                if (!trim.startsWith(DMSDPConfig.SPLIT)) {
                    if (trim.length() != 0) {
                        arrayList.add(trim);
                    }
                }
            }
            bufferedReader.close();
            try {
                requiredStream.close();
            } catch (IOException unused) {
            }
            this.availableLocales = Collections.unmodifiableList(arrayList);
        } catch (IOException e) {
            throw new IllegalStateException("IO Error reading data/index.txt: " + e.toString());
        } catch (Throwable th) {
            try {
                requiredStream.close();
            } catch (IOException unused2) {
            }
            throw th;
        }
    }

    @Override // ohos.global.icu.impl.duration.impl.PeriodFormatterDataService
    public PeriodFormatterData get(String str) {
        int indexOf = str.indexOf(64);
        if (indexOf != -1) {
            str = str.substring(0, indexOf);
        }
        synchronized (this) {
            if (this.lastLocale == null || !this.lastLocale.equals(str)) {
                PeriodFormatterData periodFormatterData = this.cache.get(str);
                if (periodFormatterData == null) {
                    String str2 = str;
                    while (true) {
                        if (!this.availableLocales.contains(str2)) {
                            int lastIndexOf = str2.lastIndexOf("_");
                            if (lastIndexOf <= -1) {
                                if (Constants.ATTRNAME_TEST.equals(str2)) {
                                    str2 = null;
                                    break;
                                }
                                str2 = Constants.ATTRNAME_TEST;
                            } else {
                                str2 = str2.substring(0, lastIndexOf);
                            }
                        } else {
                            break;
                        }
                    }
                    if (str2 != null) {
                        String str3 = "data/pfd_" + str2 + ".xml";
                        try {
                            InputStreamReader inputStreamReader = new InputStreamReader(ICUData.getRequiredStream(getClass(), str3), "UTF-8");
                            DataRecord read = DataRecord.read(str2, new XMLRecordReader(inputStreamReader));
                            inputStreamReader.close();
                            if (read != null) {
                                periodFormatterData = new PeriodFormatterData(str, read);
                            }
                            this.cache.put(str, periodFormatterData);
                        } catch (UnsupportedEncodingException unused) {
                            throw new MissingResourceException("Unhandled encoding for resource " + str3, str3, "");
                        } catch (IOException e) {
                            throw new ICUUncheckedIOException("Failed to close() resource " + str3, e);
                        }
                    } else {
                        throw new MissingResourceException("Duration data not found for  " + str, PATH, str);
                    }
                }
                this.lastData = periodFormatterData;
                this.lastLocale = str;
                return periodFormatterData;
            }
            return this.lastData;
        }
    }

    @Override // ohos.global.icu.impl.duration.impl.PeriodFormatterDataService
    public Collection<String> getAvailableLocales() {
        return this.availableLocales;
    }
}
