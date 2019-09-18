package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata;

interface MetadataSource {
    Phonemetadata.PhoneMetadata getMetadataForNonGeographicalRegion(int i);

    Phonemetadata.PhoneMetadata getMetadataForRegion(String str);
}
