package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.MetadataManager;
import com.android.i18n.phonenumbers.Phonemetadata;
import java.util.concurrent.atomic.AtomicReference;

final class SingleFileMetadataSourceImpl implements MetadataSource {
    private final MetadataLoader metadataLoader;
    private final String phoneNumberMetadataFileName;
    private final AtomicReference<MetadataManager.SingleFileMetadataMaps> phoneNumberMetadataRef;

    SingleFileMetadataSourceImpl(String phoneNumberMetadataFileName2, MetadataLoader metadataLoader2) {
        this.phoneNumberMetadataRef = new AtomicReference<>();
        this.phoneNumberMetadataFileName = phoneNumberMetadataFileName2;
        this.metadataLoader = metadataLoader2;
    }

    SingleFileMetadataSourceImpl(MetadataLoader metadataLoader2) {
        this("/com/android/i18n/phonenumbers/data/SingleFilePhoneNumberMetadataProto", metadataLoader2);
    }

    public Phonemetadata.PhoneMetadata getMetadataForRegion(String regionCode) {
        return MetadataManager.getSingleFileMetadataMaps(this.phoneNumberMetadataRef, this.phoneNumberMetadataFileName, this.metadataLoader).get(regionCode);
    }

    public Phonemetadata.PhoneMetadata getMetadataForNonGeographicalRegion(int countryCallingCode) {
        return MetadataManager.getSingleFileMetadataMaps(this.phoneNumberMetadataRef, this.phoneNumberMetadataFileName, this.metadataLoader).get(countryCallingCode);
    }
}
