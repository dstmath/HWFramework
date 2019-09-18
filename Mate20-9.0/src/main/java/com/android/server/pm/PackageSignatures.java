package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.Signature;
import com.android.internal.util.XmlUtils;
import com.android.server.am.AssistDataRequester;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class PackageSignatures {
    PackageParser.SigningDetails mSigningDetails;

    PackageSignatures(PackageSignatures orig) {
        if (orig == null || orig.mSigningDetails == PackageParser.SigningDetails.UNKNOWN) {
            this.mSigningDetails = PackageParser.SigningDetails.UNKNOWN;
        } else {
            this.mSigningDetails = new PackageParser.SigningDetails(orig.mSigningDetails);
        }
    }

    PackageSignatures(PackageParser.SigningDetails signingDetails) {
        this.mSigningDetails = signingDetails;
    }

    PackageSignatures() {
        this.mSigningDetails = PackageParser.SigningDetails.UNKNOWN;
    }

    /* access modifiers changed from: package-private */
    public void writeXml(XmlSerializer serializer, String tagName, ArrayList<Signature> writtenSignatures) throws IOException {
        if (this.mSigningDetails.signatures != null) {
            serializer.startTag(null, tagName);
            serializer.attribute(null, AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT, Integer.toString(this.mSigningDetails.signatures.length));
            serializer.attribute(null, "schemeVersion", Integer.toString(this.mSigningDetails.signatureSchemeVersion));
            writeCertsListXml(serializer, writtenSignatures, this.mSigningDetails.signatures, null);
            if (this.mSigningDetails.pastSigningCertificates != null) {
                serializer.startTag(null, "pastSigs");
                serializer.attribute(null, AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT, Integer.toString(this.mSigningDetails.pastSigningCertificates.length));
                writeCertsListXml(serializer, writtenSignatures, this.mSigningDetails.pastSigningCertificates, this.mSigningDetails.pastSigningCertificatesFlags);
                serializer.endTag(null, "pastSigs");
            }
            serializer.endTag(null, tagName);
        }
    }

    private void writeCertsListXml(XmlSerializer serializer, ArrayList<Signature> writtenSignatures, Signature[] signatures, int[] flags) throws IOException {
        for (int i = 0; i < signatures.length; i++) {
            serializer.startTag(null, "cert");
            Signature sig = signatures[i];
            int sigHash = sig.hashCode();
            int numWritten = writtenSignatures.size();
            int j = 0;
            while (true) {
                if (j >= numWritten) {
                    break;
                }
                Signature writtenSig = writtenSignatures.get(j);
                if (writtenSig.hashCode() == sigHash && writtenSig.equals(sig)) {
                    serializer.attribute(null, AssistDataRequester.KEY_RECEIVER_EXTRA_INDEX, Integer.toString(j));
                    break;
                }
                j++;
            }
            if (j >= numWritten) {
                writtenSignatures.add(sig);
                serializer.attribute(null, AssistDataRequester.KEY_RECEIVER_EXTRA_INDEX, Integer.toString(numWritten));
                serializer.attribute(null, "key", sig.toCharsString());
            }
            if (flags != null) {
                serializer.attribute(null, "flags", Integer.toString(flags[i]));
            }
            serializer.endTag(null, "cert");
        }
    }

    /* access modifiers changed from: package-private */
    public void readXml(XmlPullParser parser, ArrayList<Signature> readSignatures) throws IOException, XmlPullParserException {
        int signatureSchemeVersion;
        PackageParser.SigningDetails.Builder builder = new PackageParser.SigningDetails.Builder();
        String countStr = parser.getAttributeValue(null, AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT);
        if (countStr == null) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <sigs> has no count at " + parser.getPositionDescription());
            XmlUtils.skipCurrentTag(parser);
        }
        int count = Integer.parseInt(countStr);
        String schemeVersionStr = parser.getAttributeValue(null, "schemeVersion");
        if (schemeVersionStr == null) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <sigs> has no schemeVersion at " + parser.getPositionDescription());
            signatureSchemeVersion = 0;
        } else {
            signatureSchemeVersion = Integer.parseInt(schemeVersionStr);
        }
        builder.setSignatureSchemeVersion(signatureSchemeVersion);
        Signature[] signatures = new Signature[count];
        int pos = readCertsListXml(parser, readSignatures, signatures, null, builder);
        builder.setSignatures(signatures);
        if (pos < count) {
            Signature[] newSigs = new Signature[pos];
            System.arraycopy(signatures, 0, newSigs, 0, pos);
            builder = builder.setSignatures(newSigs);
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <sigs> count does not match number of  <cert> entries" + parser.getPositionDescription());
        }
        try {
            this.mSigningDetails = builder.build();
        } catch (CertificateException e) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <sigs> unable to convert certificate(s) to public key(s).");
            this.mSigningDetails = PackageParser.SigningDetails.UNKNOWN;
        }
    }

    private int readCertsListXml(XmlPullParser parser, ArrayList<Signature> readSignatures, Signature[] signatures, int[] flags, PackageParser.SigningDetails.Builder builder) throws IOException, XmlPullParserException {
        char c;
        String countStr;
        XmlPullParser xmlPullParser = parser;
        ArrayList<Signature> arrayList = readSignatures;
        Signature[] signatureArr = signatures;
        int count = signatureArr.length;
        int outerDepth = parser.getDepth();
        PackageParser.SigningDetails.Builder builder2 = builder;
        int pos = 0;
        while (true) {
            int outerDepth2 = outerDepth;
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth2)) {
                return pos;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals("cert")) {
                    if (pos < count) {
                        String index = xmlPullParser.getAttributeValue(null, AssistDataRequester.KEY_RECEIVER_EXTRA_INDEX);
                        if (index != null) {
                            try {
                                int idx = Integer.parseInt(index);
                                String key = xmlPullParser.getAttributeValue(null, "key");
                                if (key != null) {
                                    while (readSignatures.size() <= idx) {
                                        arrayList.add(null);
                                    }
                                    Signature sig = new Signature(key);
                                    arrayList.set(idx, sig);
                                    signatureArr[pos] = sig;
                                } else if (idx < 0 || idx >= readSignatures.size()) {
                                    PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> index " + index + " is out of bounds at " + parser.getPositionDescription());
                                } else if (arrayList.get(idx) != null) {
                                    signatureArr[pos] = arrayList.get(idx);
                                } else {
                                    PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> index " + index + " is not defined at " + parser.getPositionDescription());
                                }
                            } catch (NumberFormatException e) {
                                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> index " + index + " is not a number at " + parser.getPositionDescription());
                            } catch (IllegalArgumentException e2) {
                                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> index " + index + " has an invalid signature at " + parser.getPositionDescription() + ": " + e2.getMessage());
                            }
                            if (flags != null) {
                                String flagsStr = xmlPullParser.getAttributeValue(null, "flags");
                                if (flagsStr != null) {
                                    try {
                                        flags[pos] = Integer.parseInt(flagsStr);
                                    } catch (NumberFormatException e3) {
                                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> flags " + flagsStr + " is not a number at " + parser.getPositionDescription());
                                    }
                                } else {
                                    PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> has no flags at " + parser.getPositionDescription());
                                }
                            }
                        } else {
                            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> has no index at " + parser.getPositionDescription());
                        }
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: too many <cert> tags, expected " + count + " at " + parser.getPositionDescription());
                    }
                    pos++;
                    XmlUtils.skipCurrentTag(parser);
                } else if (!tagName.equals("pastSigs")) {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <sigs>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                } else if (flags == null) {
                    String countStr2 = xmlPullParser.getAttributeValue(null, AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT);
                    if (countStr2 == null) {
                        c = 5;
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <pastSigs> has no count at " + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        c = 5;
                    }
                    try {
                        int pastSigsCount = Integer.parseInt(countStr2);
                        int[] pastSignaturesFlags = new int[pastSigsCount];
                        Signature[] pastSignatures = new Signature[pastSigsCount];
                        ArrayList<Signature> arrayList2 = arrayList;
                        countStr = countStr2;
                        char c2 = c;
                        String str = tagName;
                        try {
                            int pastSigsPos = readCertsListXml(xmlPullParser, arrayList2, pastSignatures, pastSignaturesFlags, builder2);
                            Signature[] pastSignatures2 = pastSignatures;
                            int[] pastSignaturesFlags2 = pastSignaturesFlags;
                            builder2 = builder2.setPastSigningCertificates(pastSignatures2).setPastSigningCertificatesFlags(pastSignaturesFlags2);
                            if (pastSigsPos < pastSigsCount) {
                                Signature[] newSigs = new Signature[pastSigsPos];
                                System.arraycopy(pastSignatures2, 0, newSigs, 0, pastSigsPos);
                                int[] newFlags = new int[pastSigsPos];
                                System.arraycopy(pastSignaturesFlags2, 0, newFlags, 0, pastSigsPos);
                                builder2 = builder2.setPastSigningCertificates(newSigs).setPastSigningCertificatesFlags(newFlags);
                                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <pastSigs> count does not match number of <cert> entries " + parser.getPositionDescription());
                            }
                        } catch (NumberFormatException e4) {
                            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <pastSigs> count " + countStr + " is not a number at " + parser.getPositionDescription());
                            outerDepth = outerDepth2;
                            arrayList = readSignatures;
                            signatureArr = signatures;
                        }
                    } catch (NumberFormatException e5) {
                        countStr = countStr2;
                        String str2 = tagName;
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <pastSigs> count " + countStr + " is not a number at " + parser.getPositionDescription());
                        outerDepth = outerDepth2;
                        arrayList = readSignatures;
                        signatureArr = signatures;
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "<pastSigs> encountered multiple times under the same <sigs> at " + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
            outerDepth = outerDepth2;
            arrayList = readSignatures;
            signatureArr = signatures;
        }
        return pos;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(128);
        buf.append("PackageSignatures{");
        buf.append(Integer.toHexString(System.identityHashCode(this)));
        buf.append(" version:");
        buf.append(this.mSigningDetails.signatureSchemeVersion);
        buf.append(", signatures:[");
        int i = 0;
        if (this.mSigningDetails.signatures != null) {
            for (int i2 = 0; i2 < this.mSigningDetails.signatures.length; i2++) {
                if (i2 > 0) {
                    buf.append(", ");
                }
                buf.append(Integer.toHexString(this.mSigningDetails.signatures[i2].hashCode()));
            }
        }
        buf.append("]");
        buf.append(", past signatures:[");
        if (this.mSigningDetails.pastSigningCertificates != null) {
            while (true) {
                int i3 = i;
                if (i3 >= this.mSigningDetails.pastSigningCertificates.length) {
                    break;
                }
                if (i3 > 0) {
                    buf.append(", ");
                }
                buf.append(Integer.toHexString(this.mSigningDetails.pastSigningCertificates[i3].hashCode()));
                buf.append(" flags: ");
                buf.append(Integer.toHexString(this.mSigningDetails.pastSigningCertificatesFlags[i3]));
                i = i3 + 1;
            }
        }
        buf.append("]}");
        return buf.toString();
    }
}
