package com.android.server.pm;

import android.content.pm.Signature;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class PackageSignatures {
    Signature[] mSignatures;

    PackageSignatures(PackageSignatures orig) {
        if (orig != null && orig.mSignatures != null) {
            this.mSignatures = (Signature[]) orig.mSignatures.clone();
        }
    }

    PackageSignatures(Signature[] sigs) {
        assignSignatures(sigs);
    }

    PackageSignatures() {
    }

    void writeXml(XmlSerializer serializer, String tagName, ArrayList<Signature> pastSignatures) throws IOException {
        if (this.mSignatures != null) {
            serializer.startTag(null, tagName);
            serializer.attribute(null, "count", Integer.toString(this.mSignatures.length));
            for (Signature sig : this.mSignatures) {
                serializer.startTag(null, "cert");
                int sigHash = sig.hashCode();
                int numPast = pastSignatures.size();
                int j = 0;
                while (j < numPast) {
                    Signature pastSig = (Signature) pastSignatures.get(j);
                    if (pastSig.hashCode() == sigHash && pastSig.equals(sig)) {
                        serializer.attribute(null, "index", Integer.toString(j));
                        break;
                    }
                    j++;
                }
                if (j >= numPast) {
                    pastSignatures.add(sig);
                    serializer.attribute(null, "index", Integer.toString(numPast));
                    serializer.attribute(null, "key", sig.toCharsString());
                }
                serializer.endTag(null, "cert");
            }
            serializer.endTag(null, tagName);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readXml(XmlPullParser parser, ArrayList<Signature> pastSignatures) throws IOException, XmlPullParserException {
        String countStr = parser.getAttributeValue(null, "count");
        if (countStr == null) {
            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <signatures> has no count at " + parser.getPositionDescription());
            XmlUtils.skipCurrentTag(parser);
        }
        int count = Integer.parseInt(countStr);
        this.mSignatures = new Signature[count];
        int pos = 0;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (pos < count) {
                    Signature[] newSigs = new Signature[pos];
                    System.arraycopy(this.mSignatures, 0, newSigs, 0, pos);
                    this.mSignatures = newSigs;
                }
            } else if (!(type == 3 || type == 4)) {
                if (!parser.getName().equals("cert")) {
                    PackageManagerService.reportSettingsProblem(5, "Unknown element under <cert>: " + parser.getName());
                } else if (pos < count) {
                    String index = parser.getAttributeValue(null, "index");
                    if (index != null) {
                        try {
                            int idx = Integer.parseInt(index);
                            String key = parser.getAttributeValue(null, "key");
                            if (key != null) {
                                while (pastSignatures.size() <= idx) {
                                    pastSignatures.add(null);
                                }
                                Signature sig = new Signature(key);
                                pastSignatures.set(idx, sig);
                                this.mSignatures[pos] = sig;
                                pos++;
                            } else if (idx < 0 || idx >= pastSignatures.size()) {
                                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> index " + index + " is out of bounds at " + parser.getPositionDescription());
                            } else if (((Signature) pastSignatures.get(idx)) != null) {
                                this.mSignatures[pos] = (Signature) pastSignatures.get(idx);
                                pos++;
                            } else {
                                PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> index " + index + " is not defined at " + parser.getPositionDescription());
                            }
                        } catch (NumberFormatException e) {
                            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> index " + index + " is not a number at " + parser.getPositionDescription());
                        } catch (IllegalArgumentException e2) {
                            PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> index " + index + " has an invalid signature at " + parser.getPositionDescription() + ": " + e2.getMessage());
                        }
                    } else {
                        PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: <cert> has no index at " + parser.getPositionDescription());
                    }
                } else {
                    PackageManagerService.reportSettingsProblem(5, "Error in package manager settings: too many <cert> tags, expected " + count + " at " + parser.getPositionDescription());
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
        if (pos < count) {
            Signature[] newSigs2 = new Signature[pos];
            System.arraycopy(this.mSignatures, 0, newSigs2, 0, pos);
            this.mSignatures = newSigs2;
        }
    }

    void assignSignatures(Signature[] sigs) {
        if (sigs == null) {
            this.mSignatures = null;
            return;
        }
        this.mSignatures = new Signature[sigs.length];
        for (int i = 0; i < sigs.length; i++) {
            this.mSignatures[i] = sigs[i];
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(DumpState.DUMP_PACKAGES);
        buf.append("PackageSignatures{");
        buf.append(Integer.toHexString(System.identityHashCode(this)));
        buf.append(" [");
        if (this.mSignatures != null) {
            for (int i = 0; i < this.mSignatures.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(Integer.toHexString(this.mSignatures[i].hashCode()));
            }
        }
        buf.append("]}");
        return buf.toString();
    }
}
