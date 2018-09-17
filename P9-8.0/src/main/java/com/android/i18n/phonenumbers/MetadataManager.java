package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadataCollection;
import com.google.i18n.phonenumbers.Phonemetadata;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

final class MetadataManager {
    private static final String ALTERNATE_FORMATS_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/PhoneNumberAlternateFormatsProto";
    static final MetadataLoader DEFAULT_METADATA_LOADER = new MetadataLoader() {
        public InputStream loadMetadata(String metadataFileName) {
            return MetadataManager.class.getResourceAsStream(metadataFileName);
        }
    };
    static final String MULTI_FILE_PHONE_NUMBER_METADATA_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/PhoneNumberMetadataProto";
    private static final String SHORT_NUMBER_METADATA_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/ShortNumberMetadataProto";
    static final String SINGLE_FILE_PHONE_NUMBER_METADATA_FILE_NAME = "/com/android/i18n/phonenumbers/data/SingleFilePhoneNumberMetadataProto";
    private static final Set<Integer> alternateFormatsCountryCodes = AlternateFormatsCountryCodeSet.getCountryCodeSet();
    private static final ConcurrentHashMap<Integer, PhoneMetadata> alternateFormatsMap = new ConcurrentHashMap();
    private static final Logger logger = Logger.getLogger(MetadataManager.class.getName());
    private static final ConcurrentHashMap<String, PhoneMetadata> shortNumberMetadataMap = new ConcurrentHashMap();
    private static final Set<String> shortNumberMetadataRegionCodes = ShortNumbersRegionCodeSet.getRegionCodeSet();

    static class SingleFileMetadataMaps {
        private final Map<Integer, PhoneMetadata> countryCallingCodeToMetadata;
        private final Map<String, PhoneMetadata> regionCodeToMetadata;

        static SingleFileMetadataMaps load(String fileName, MetadataLoader metadataLoader) {
            List<Phonemetadata.PhoneMetadata> metadataList = MetadataManager.getMetadataFromSingleFileName(fileName, metadataLoader);
            Map<String, Phonemetadata.PhoneMetadata> regionCodeToMetadata = new HashMap();
            Map<Integer, Phonemetadata.PhoneMetadata> countryCallingCodeToMetadata = new HashMap();
            Iterator metadata$iterator = metadataList.iterator();
            while (metadata$iterator.hasNext()) {
                PhoneMetadata metadata = (PhoneMetadata) metadata$iterator.next();
                String regionCode = metadata.getId();
                if (PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCode)) {
                    countryCallingCodeToMetadata.put(Integer.valueOf(metadata.getCountryCode()), metadata);
                } else {
                    regionCodeToMetadata.put(regionCode, metadata);
                }
            }
            return new SingleFileMetadataMaps(regionCodeToMetadata, countryCallingCodeToMetadata);
        }

        private SingleFileMetadataMaps(Map<String, PhoneMetadata> regionCodeToMetadata, Map<Integer, PhoneMetadata> countryCallingCodeToMetadata) {
            this.regionCodeToMetadata = Collections.unmodifiableMap(regionCodeToMetadata);
            this.countryCallingCodeToMetadata = Collections.unmodifiableMap(countryCallingCodeToMetadata);
        }

        PhoneMetadata get(String regionCode) {
            return (PhoneMetadata) this.regionCodeToMetadata.get(regionCode);
        }

        PhoneMetadata get(int countryCallingCode) {
            return (PhoneMetadata) this.countryCallingCodeToMetadata.get(Integer.valueOf(countryCallingCode));
        }
    }

    private MetadataManager() {
    }

    static PhoneMetadata getAlternateFormatsForCountry(int countryCallingCode) {
        if (alternateFormatsCountryCodes.contains(Integer.valueOf(countryCallingCode))) {
            return getMetadataFromMultiFilePrefix(Integer.valueOf(countryCallingCode), alternateFormatsMap, ALTERNATE_FORMATS_FILE_PREFIX, DEFAULT_METADATA_LOADER);
        }
        return null;
    }

    static PhoneMetadata getShortNumberMetadataForRegion(String regionCode) {
        if (shortNumberMetadataRegionCodes.contains(regionCode)) {
            return getMetadataFromMultiFilePrefix(regionCode, shortNumberMetadataMap, SHORT_NUMBER_METADATA_FILE_PREFIX, DEFAULT_METADATA_LOADER);
        }
        return null;
    }

    static Set<String> getSupportedShortNumberRegions() {
        return Collections.unmodifiableSet(shortNumberMetadataRegionCodes);
    }

    static <T> PhoneMetadata getMetadataFromMultiFilePrefix(T key, ConcurrentHashMap<T, PhoneMetadata> map, String filePrefix, MetadataLoader metadataLoader) {
        PhoneMetadata metadata = (PhoneMetadata) map.get(key);
        if (metadata != null) {
            return metadata;
        }
        String fileName = filePrefix + "_" + key;
        List<Phonemetadata.PhoneMetadata> metadataList = getMetadataFromSingleFileName(fileName, metadataLoader);
        if (metadataList.size() > 1) {
            logger.log(Level.WARNING, "more than one metadata in file " + fileName);
        }
        metadata = (PhoneMetadata) metadataList.get(0);
        PhoneMetadata oldValue = (PhoneMetadata) map.putIfAbsent(key, metadata);
        if (oldValue == null) {
            oldValue = metadata;
        }
        return oldValue;
    }

    static SingleFileMetadataMaps getSingleFileMetadataMaps(AtomicReference<SingleFileMetadataMaps> ref, String fileName, MetadataLoader metadataLoader) {
        SingleFileMetadataMaps maps = (SingleFileMetadataMaps) ref.get();
        if (maps != null) {
            return maps;
        }
        ref.compareAndSet(null, SingleFileMetadataMaps.load(fileName, metadataLoader));
        return (SingleFileMetadataMaps) ref.get();
    }

    private static List<PhoneMetadata> getMetadataFromSingleFileName(String fileName, MetadataLoader metadataLoader) {
        InputStream source = metadataLoader.loadMetadata(fileName);
        if (source == null) {
            throw new IllegalStateException("missing metadata: " + fileName);
        }
        List<Phonemetadata.PhoneMetadata> metadataList = loadMetadataAndCloseInput(source).getMetadataList();
        if (metadataList.size() != 0) {
            return metadataList;
        }
        throw new IllegalStateException("empty metadata: " + fileName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0042 A:{SYNTHETIC, Splitter: B:30:0x0042} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0021 A:{SYNTHETIC, Splitter: B:17:0x0021} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static PhoneMetadataCollection loadMetadataAndCloseInput(InputStream source) {
        Throwable th;
        ObjectInputStream ois = null;
        try {
            ObjectInputStream ois2 = new ObjectInputStream(source);
            try {
                PhoneMetadataCollection metadataCollection = new PhoneMetadataCollection();
                metadataCollection.readExternal(ois2);
                if (ois2 != null) {
                    try {
                        ois2.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "error closing input stream (ignored)", e);
                    }
                } else {
                    source.close();
                }
                return metadataCollection;
            } catch (IOException e2) {
                throw new RuntimeException("cannot load/parse metadata", e2);
            } catch (Throwable th2) {
                th = th2;
                ois = ois2;
                if (ois == null) {
                }
                throw th;
            }
        } catch (IOException e22) {
            throw new RuntimeException("cannot load/parse metadata", e22);
        } catch (Throwable th3) {
            th = th3;
            if (ois == null) {
                try {
                    ois.close();
                } catch (IOException e222) {
                    logger.log(Level.WARNING, "error closing input stream (ignored)", e222);
                }
            } else {
                source.close();
            }
            throw th;
        }
    }
}
