package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;

interface MetadataSource {
    PhoneMetadata getMetadataForNonGeographicalRegion(int i);

    PhoneMetadata getMetadataForRegion(String str);
}
