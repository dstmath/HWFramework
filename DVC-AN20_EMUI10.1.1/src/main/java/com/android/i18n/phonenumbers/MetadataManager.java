package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/* access modifiers changed from: package-private */
public final class MetadataManager {
    private static final String ALTERNATE_FORMATS_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/PhoneNumberAlternateFormatsProto";
    static final MetadataLoader DEFAULT_METADATA_LOADER = new MetadataLoader() {
        /* class com.android.i18n.phonenumbers.MetadataManager.AnonymousClass1 */

        @Override // com.android.i18n.phonenumbers.MetadataLoader
        public InputStream loadMetadata(String metadataFileName) {
            return MetadataManager.class.getResourceAsStream(metadataFileName);
        }
    };
    static final String MULTI_FILE_PHONE_NUMBER_METADATA_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/PhoneNumberMetadataProto";
    private static final String SHORT_NUMBER_METADATA_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/ShortNumberMetadataProto";
    static final String SINGLE_FILE_PHONE_NUMBER_METADATA_FILE_NAME = "/com/android/i18n/phonenumbers/data/SingleFilePhoneNumberMetadataProto";
    private static final Set<Integer> alternateFormatsCountryCodes = AlternateFormatsCountryCodeSet.getCountryCodeSet();
    private static final ConcurrentHashMap<Integer, Phonemetadata.PhoneMetadata> alternateFormatsMap = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(MetadataManager.class.getName());
    private static final ConcurrentHashMap<String, Phonemetadata.PhoneMetadata> shortNumberMetadataMap = new ConcurrentHashMap<>();
    private static final Set<String> shortNumberMetadataRegionCodes = ShortNumbersRegionCodeSet.getRegionCodeSet();

    private MetadataManager() {
    }

    static Phonemetadata.PhoneMetadata getAlternateFormatsForCountry(int countryCallingCode) {
        if (!alternateFormatsCountryCodes.contains(Integer.valueOf(countryCallingCode))) {
            return null;
        }
        return getMetadataFromMultiFilePrefix(Integer.valueOf(countryCallingCode), alternateFormatsMap, ALTERNATE_FORMATS_FILE_PREFIX, DEFAULT_METADATA_LOADER);
    }

    static Phonemetadata.PhoneMetadata getShortNumberMetadataForRegion(String regionCode) {
        if (!shortNumberMetadataRegionCodes.contains(regionCode)) {
            return null;
        }
        return getMetadataFromMultiFilePrefix(regionCode, shortNumberMetadataMap, SHORT_NUMBER_METADATA_FILE_PREFIX, DEFAULT_METADATA_LOADER);
    }

    static Set<String> getSupportedShortNumberRegions() {
        return Collections.unmodifiableSet(shortNumberMetadataRegionCodes);
    }

    static <T> Phonemetadata.PhoneMetadata getMetadataFromMultiFilePrefix(T key, ConcurrentHashMap<T, Phonemetadata.PhoneMetadata> map, String filePrefix, MetadataLoader metadataLoader) {
        Phonemetadata.PhoneMetadata metadata = map.get(key);
        if (metadata != null) {
            return metadata;
        }
        String fileName = filePrefix + "_" + ((Object) key);
        List<Phonemetadata.PhoneMetadata> metadataList = getMetadataFromSingleFileName(fileName, metadataLoader);
        if (metadataList.size() > 1) {
            logger.log(Level.WARNING, "more than one metadata in file " + fileName);
        }
        Phonemetadata.PhoneMetadata metadata2 = metadataList.get(0);
        Phonemetadata.PhoneMetadata oldValue = map.putIfAbsent(key, metadata2);
        return oldValue != null ? oldValue : metadata2;
    }

    static class SingleFileMetadataMaps {
        private final Map<Integer, Phonemetadata.PhoneMetadata> countryCallingCodeToMetadata;
        private final Map<String, Phonemetadata.PhoneMetadata> regionCodeToMetadata;

        static SingleFileMetadataMaps load(String fileName, MetadataLoader metadataLoader) {
            List<Phonemetadata.PhoneMetadata> metadataList = MetadataManager.getMetadataFromSingleFileName(fileName, metadataLoader);
            Map<String, Phonemetadata.PhoneMetadata> regionCodeToMetadata2 = new HashMap<>();
            Map<Integer, Phonemetadata.PhoneMetadata> countryCallingCodeToMetadata2 = new HashMap<>();
            for (Phonemetadata.PhoneMetadata metadata : metadataList) {
                String regionCode = metadata.getId();
                if (PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCode)) {
                    countryCallingCodeToMetadata2.put(Integer.valueOf(metadata.getCountryCode()), metadata);
                } else {
                    regionCodeToMetadata2.put(regionCode, metadata);
                }
            }
            return new SingleFileMetadataMaps(regionCodeToMetadata2, countryCallingCodeToMetadata2);
        }

        private SingleFileMetadataMaps(Map<String, Phonemetadata.PhoneMetadata> regionCodeToMetadata2, Map<Integer, Phonemetadata.PhoneMetadata> countryCallingCodeToMetadata2) {
            this.regionCodeToMetadata = Collections.unmodifiableMap(regionCodeToMetadata2);
            this.countryCallingCodeToMetadata = Collections.unmodifiableMap(countryCallingCodeToMetadata2);
        }

        /* access modifiers changed from: package-private */
        public Phonemetadata.PhoneMetadata get(String regionCode) {
            return this.regionCodeToMetadata.get(regionCode);
        }

        /* access modifiers changed from: package-private */
        public Phonemetadata.PhoneMetadata get(int countryCallingCode) {
            return this.countryCallingCodeToMetadata.get(Integer.valueOf(countryCallingCode));
        }
    }

    static SingleFileMetadataMaps getSingleFileMetadataMaps(AtomicReference<SingleFileMetadataMaps> ref, String fileName, MetadataLoader metadataLoader) {
        SingleFileMetadataMaps maps = ref.get();
        if (maps != null) {
            return maps;
        }
        ref.compareAndSet(null, SingleFileMetadataMaps.load(fileName, metadataLoader));
        return ref.get();
    }

    /* access modifiers changed from: private */
    public static List<Phonemetadata.PhoneMetadata> getMetadataFromSingleFileName(String fileName, MetadataLoader metadataLoader) {
        InputStream source = metadataLoader.loadMetadata(fileName);
        if (source != null) {
            List<Phonemetadata.PhoneMetadata> metadataList = loadMetadataAndCloseInput(source).getMetadataList();
            if (metadataList.size() != 0) {
                return metadataList;
            }
            throw new IllegalStateException("empty metadata: " + fileName);
        }
        throw new IllegalStateException("missing metadata: " + fileName);
    }

    private static Phonemetadata.PhoneMetadataCollection loadMetadataAndCloseInput(InputStream source) {
        try {
            ObjectInputStream ois = new ObjectInputStream(source);
            try {
                Phonemetadata.PhoneMetadataCollection metadataCollection = new Phonemetadata.PhoneMetadataCollection();
                try {
                    metadataCollection.readExternal(ois);
                    try {
                        ois.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "error closing input stream (ignored)", (Throwable) e);
                    }
                    return metadataCollection;
                } catch (IOException e2) {
                    throw new RuntimeException("cannot load/parse metadata", e2);
                }
            } catch (Throwable th) {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e3) {
                        logger.log(Level.WARNING, "error closing input stream (ignored)", (Throwable) e3);
                    }
                } else {
                    source.close();
                }
                throw th;
            }
        } catch (IOException e4) {
            throw new RuntimeException("cannot load/parse metadata", e4);
        }
    }
}
