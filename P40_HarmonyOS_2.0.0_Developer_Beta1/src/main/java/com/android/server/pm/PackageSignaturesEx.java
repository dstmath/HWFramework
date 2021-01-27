package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;

public class PackageSignaturesEx {
    private PackageSignatures packageSignatures;

    public PackageSignatures getPackageSignatures() {
        return this.packageSignatures;
    }

    public void setPackageSignatures(PackageSignatures packageSignatures2) {
        this.packageSignatures = packageSignatures2;
    }

    public PackageParserEx.SigningDetailsEx getSigningDetails() {
        PackageParser.SigningDetails signingDetails = this.packageSignatures.mSigningDetails;
        PackageParserEx.SigningDetailsEx detailsEx = new PackageParserEx.SigningDetailsEx();
        detailsEx.setSigningDetails(signingDetails);
        return detailsEx;
    }

    public void setSigningDetails(PackageParserEx.SigningDetailsEx detailsEx) {
        this.packageSignatures.mSigningDetails = detailsEx.getSigningDetails();
    }
}
