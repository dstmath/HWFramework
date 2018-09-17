package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import java.util.concurrent.atomic.AtomicReference;

final class SingleFileMetadataSourceImpl implements MetadataSource {
    private final MetadataLoader metadataLoader;
    private final String phoneNumberMetadataFileName;
    private final AtomicReference<SingleFileMetadataMaps> phoneNumberMetadataRef;

    SingleFileMetadataSourceImpl(String phoneNumberMetadataFileName, MetadataLoader metadataLoader) {
        this.phoneNumberMetadataRef = new AtomicReference();
        this.phoneNumberMetadataFileName = phoneNumberMetadataFileName;
        this.metadataLoader = metadataLoader;
    }

    SingleFileMetadataSourceImpl(MetadataLoader metadataLoader) {
        this("/com/android/i18n/phonenumbers/data/SingleFilePhoneNumberMetadataProto", metadataLoader);
    }

    public PhoneMetadata getMetadataForRegion(String regionCode) {
        return MetadataManager.getSingleFileMetadataMaps(this.phoneNumberMetadataRef, this.phoneNumberMetadataFileName, this.metadataLoader).get(regionCode);
    }

    public PhoneMetadata getMetadataForNonGeographicalRegion(int countryCallingCode) {
        return MetadataManager.getSingleFileMetadataMaps(this.phoneNumberMetadataRef, this.phoneNumberMetadataFileName, this.metadataLoader).get(countryCallingCode);
    }
}
