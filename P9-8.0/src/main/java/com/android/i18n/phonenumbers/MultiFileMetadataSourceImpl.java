package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

final class MultiFileMetadataSourceImpl implements MetadataSource {
    private final ConcurrentHashMap<String, PhoneMetadata> geographicalRegions;
    private final MetadataLoader metadataLoader;
    private final ConcurrentHashMap<Integer, PhoneMetadata> nonGeographicalRegions;
    private final String phoneNumberMetadataFilePrefix;

    MultiFileMetadataSourceImpl(String phoneNumberMetadataFilePrefix, MetadataLoader metadataLoader) {
        this.geographicalRegions = new ConcurrentHashMap();
        this.nonGeographicalRegions = new ConcurrentHashMap();
        this.phoneNumberMetadataFilePrefix = phoneNumberMetadataFilePrefix;
        this.metadataLoader = metadataLoader;
    }

    MultiFileMetadataSourceImpl(MetadataLoader metadataLoader) {
        this("/com/android/i18n/phonenumbers/data/PhoneNumberMetadataProto", metadataLoader);
    }

    public PhoneMetadata getMetadataForRegion(String regionCode) {
        return MetadataManager.getMetadataFromMultiFilePrefix(regionCode, this.geographicalRegions, this.phoneNumberMetadataFilePrefix, this.metadataLoader);
    }

    public PhoneMetadata getMetadataForNonGeographicalRegion(int countryCallingCode) {
        if (isNonGeographical(countryCallingCode)) {
            return MetadataManager.getMetadataFromMultiFilePrefix(Integer.valueOf(countryCallingCode), this.nonGeographicalRegions, this.phoneNumberMetadataFilePrefix, this.metadataLoader);
        }
        return null;
    }

    private boolean isNonGeographical(int countryCallingCode) {
        List<String> regionCodes = (List) CountryCodeToRegionCodeMap.getCountryCodeToRegionCodeMap().get(Integer.valueOf(countryCallingCode));
        if (regionCodes.size() == 1) {
            return PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCodes.get(0));
        }
        return false;
    }
}
