package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/* access modifiers changed from: package-private */
public final class MultiFileMetadataSourceImpl implements MetadataSource {
    private final ConcurrentHashMap<String, Phonemetadata.PhoneMetadata> geographicalRegions;
    private final MetadataLoader metadataLoader;
    private final ConcurrentHashMap<Integer, Phonemetadata.PhoneMetadata> nonGeographicalRegions;
    private final String phoneNumberMetadataFilePrefix;

    MultiFileMetadataSourceImpl(String phoneNumberMetadataFilePrefix2, MetadataLoader metadataLoader2) {
        this.geographicalRegions = new ConcurrentHashMap<>();
        this.nonGeographicalRegions = new ConcurrentHashMap<>();
        this.phoneNumberMetadataFilePrefix = phoneNumberMetadataFilePrefix2;
        this.metadataLoader = metadataLoader2;
    }

    MultiFileMetadataSourceImpl(MetadataLoader metadataLoader2) {
        this("/com/android/i18n/phonenumbers/data/PhoneNumberMetadataProto", metadataLoader2);
    }

    @Override // com.android.i18n.phonenumbers.MetadataSource
    public Phonemetadata.PhoneMetadata getMetadataForRegion(String regionCode) {
        return MetadataManager.getMetadataFromMultiFilePrefix(regionCode, this.geographicalRegions, this.phoneNumberMetadataFilePrefix, this.metadataLoader);
    }

    @Override // com.android.i18n.phonenumbers.MetadataSource
    public Phonemetadata.PhoneMetadata getMetadataForNonGeographicalRegion(int countryCallingCode) {
        if (!isNonGeographical(countryCallingCode)) {
            return null;
        }
        return MetadataManager.getMetadataFromMultiFilePrefix(Integer.valueOf(countryCallingCode), this.nonGeographicalRegions, this.phoneNumberMetadataFilePrefix, this.metadataLoader);
    }

    private boolean isNonGeographical(int countryCallingCode) {
        List<String> regionCodes = CountryCodeToRegionCodeMap.getCountryCodeToRegionCodeMap().get(Integer.valueOf(countryCallingCode));
        return regionCodes.size() == 1 && PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCodes.get(0));
    }
}
