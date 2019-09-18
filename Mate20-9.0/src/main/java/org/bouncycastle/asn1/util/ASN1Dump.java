package org.bouncycastle.asn1.util;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1ApplicationSpecific;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.BERApplicationSpecific;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.BERSet;
import org.bouncycastle.asn1.BERTaggedObject;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERExternal;
import org.bouncycastle.asn1.DERGraphicString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERT61String;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DERVideotexString;
import org.bouncycastle.asn1.DERVisibleString;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

public class ASN1Dump {
    private static final int SAMPLE_SIZE = 32;
    private static final String TAB = "    ";

    static void _dumpAsString(String str, boolean z, ASN1Primitive aSN1Primitive, StringBuffer stringBuffer) {
        String str2;
        StringBuilder sb;
        String obj;
        StringBuilder sb2;
        BigInteger value;
        String str3;
        String time;
        DERBitString dERBitString;
        byte[] bytes;
        StringBuilder sb3;
        int length;
        String lineSeparator = Strings.lineSeparator();
        if (aSN1Primitive instanceof ASN1Sequence) {
            Enumeration objects = ((ASN1Sequence) aSN1Primitive).getObjects();
            String str4 = str + TAB;
            stringBuffer.append(str);
            String str5 = aSN1Primitive instanceof BERSequence ? "BER Sequence" : aSN1Primitive instanceof DERSequence ? "DER Sequence" : "Sequence";
            loop1:
            while (true) {
                stringBuffer.append(str5);
                stringBuffer.append(lineSeparator);
                while (objects.hasMoreElements()) {
                    Object nextElement = objects.nextElement();
                    if (nextElement == null || nextElement.equals(DERNull.INSTANCE)) {
                        stringBuffer.append(str4);
                        str5 = "NULL";
                    } else {
                        _dumpAsString(str4, z, nextElement instanceof ASN1Primitive ? (ASN1Primitive) nextElement : ((ASN1Encodable) nextElement).toASN1Primitive(), stringBuffer);
                    }
                }
                break loop1;
            }
        } else {
            if (aSN1Primitive instanceof ASN1TaggedObject) {
                String str6 = str + TAB;
                stringBuffer.append(str);
                stringBuffer.append(aSN1Primitive instanceof BERTaggedObject ? "BER Tagged [" : "Tagged [");
                ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) aSN1Primitive;
                stringBuffer.append(Integer.toString(aSN1TaggedObject.getTagNo()));
                stringBuffer.append(']');
                if (!aSN1TaggedObject.isExplicit()) {
                    stringBuffer.append(" IMPLICIT ");
                }
                stringBuffer.append(lineSeparator);
                if (aSN1TaggedObject.isEmpty()) {
                    stringBuffer.append(str6);
                    stringBuffer.append("EMPTY");
                } else {
                    _dumpAsString(str6, z, aSN1TaggedObject.getObject(), stringBuffer);
                    return;
                }
            } else if (aSN1Primitive instanceof ASN1Set) {
                Enumeration objects2 = ((ASN1Set) aSN1Primitive).getObjects();
                String str7 = str + TAB;
                stringBuffer.append(str);
                String str8 = aSN1Primitive instanceof BERSet ? "BER Set" : "DER Set";
                loop3:
                while (true) {
                    stringBuffer.append(str8);
                    stringBuffer.append(lineSeparator);
                    while (objects2.hasMoreElements()) {
                        Object nextElement2 = objects2.nextElement();
                        if (nextElement2 == null) {
                            stringBuffer.append(str7);
                            str8 = "NULL";
                        } else {
                            _dumpAsString(str7, z, nextElement2 instanceof ASN1Primitive ? (ASN1Primitive) nextElement2 : ((ASN1Encodable) nextElement2).toASN1Primitive(), stringBuffer);
                        }
                    }
                    break loop3;
                }
            } else {
                if (aSN1Primitive instanceof ASN1OctetString) {
                    ASN1OctetString aSN1OctetString = (ASN1OctetString) aSN1Primitive;
                    if (aSN1Primitive instanceof BEROctetString) {
                        sb3 = new StringBuilder();
                        sb3.append(str);
                        sb3.append("BER Constructed Octet String[");
                        length = aSN1OctetString.getOctets().length;
                    } else {
                        sb3 = new StringBuilder();
                        sb3.append(str);
                        sb3.append("DER Octet String[");
                        length = aSN1OctetString.getOctets().length;
                    }
                    sb3.append(length);
                    sb3.append("] ");
                    stringBuffer.append(sb3.toString());
                    if (z) {
                        bytes = aSN1OctetString.getOctets();
                    }
                } else {
                    if (aSN1Primitive instanceof ASN1ObjectIdentifier) {
                        sb2 = new StringBuilder();
                        sb2.append(str);
                        sb2.append("ObjectIdentifier(");
                        sb2.append(((ASN1ObjectIdentifier) aSN1Primitive).getId());
                    } else if (aSN1Primitive instanceof ASN1Boolean) {
                        sb2 = new StringBuilder();
                        sb2.append(str);
                        sb2.append("Boolean(");
                        sb2.append(((ASN1Boolean) aSN1Primitive).isTrue());
                    } else {
                        if (aSN1Primitive instanceof ASN1Integer) {
                            sb2 = new StringBuilder();
                            sb2.append(str);
                            sb2.append("Integer(");
                            value = ((ASN1Integer) aSN1Primitive).getValue();
                        } else if (aSN1Primitive instanceof DERBitString) {
                            stringBuffer.append(str + "DER Bit String[" + dERBitString.getBytes().length + ", " + ((DERBitString) aSN1Primitive).getPadBits() + "] ");
                            if (z) {
                                bytes = dERBitString.getBytes();
                            }
                        } else {
                            if (aSN1Primitive instanceof DERIA5String) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("IA5String(");
                                time = ((DERIA5String) aSN1Primitive).getString();
                            } else if (aSN1Primitive instanceof DERUTF8String) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("UTF8String(");
                                time = ((DERUTF8String) aSN1Primitive).getString();
                            } else if (aSN1Primitive instanceof DERPrintableString) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("PrintableString(");
                                time = ((DERPrintableString) aSN1Primitive).getString();
                            } else if (aSN1Primitive instanceof DERVisibleString) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("VisibleString(");
                                time = ((DERVisibleString) aSN1Primitive).getString();
                            } else if (aSN1Primitive instanceof DERBMPString) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("BMPString(");
                                time = ((DERBMPString) aSN1Primitive).getString();
                            } else if (aSN1Primitive instanceof DERT61String) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("T61String(");
                                time = ((DERT61String) aSN1Primitive).getString();
                            } else if (aSN1Primitive instanceof DERGraphicString) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("GraphicString(");
                                time = ((DERGraphicString) aSN1Primitive).getString();
                            } else if (aSN1Primitive instanceof DERVideotexString) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("VideotexString(");
                                time = ((DERVideotexString) aSN1Primitive).getString();
                            } else if (aSN1Primitive instanceof ASN1UTCTime) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("UTCTime(");
                                time = ((ASN1UTCTime) aSN1Primitive).getTime();
                            } else if (aSN1Primitive instanceof ASN1GeneralizedTime) {
                                sb = new StringBuilder();
                                sb.append(str);
                                sb.append("GeneralizedTime(");
                                time = ((ASN1GeneralizedTime) aSN1Primitive).getTime();
                            } else {
                                if (aSN1Primitive instanceof BERApplicationSpecific) {
                                    str3 = ASN1Encoding.BER;
                                } else if (aSN1Primitive instanceof DERApplicationSpecific) {
                                    str3 = ASN1Encoding.DER;
                                } else if (aSN1Primitive instanceof ASN1Enumerated) {
                                    sb2 = new StringBuilder();
                                    sb2.append(str);
                                    sb2.append("DER Enumerated(");
                                    value = ((ASN1Enumerated) aSN1Primitive).getValue();
                                } else if (aSN1Primitive instanceof DERExternal) {
                                    DERExternal dERExternal = (DERExternal) aSN1Primitive;
                                    stringBuffer.append(str + "External " + lineSeparator);
                                    StringBuilder sb4 = new StringBuilder();
                                    sb4.append(str);
                                    sb4.append(TAB);
                                    String sb5 = sb4.toString();
                                    if (dERExternal.getDirectReference() != null) {
                                        stringBuffer.append(sb5 + "Direct Reference: " + dERExternal.getDirectReference().getId() + lineSeparator);
                                    }
                                    if (dERExternal.getIndirectReference() != null) {
                                        stringBuffer.append(sb5 + "Indirect Reference: " + dERExternal.getIndirectReference().toString() + lineSeparator);
                                    }
                                    if (dERExternal.getDataValueDescriptor() != null) {
                                        _dumpAsString(sb5, z, dERExternal.getDataValueDescriptor(), stringBuffer);
                                    }
                                    stringBuffer.append(sb5 + "Encoding: " + dERExternal.getEncoding() + lineSeparator);
                                    _dumpAsString(sb5, z, dERExternal.getExternalContent(), stringBuffer);
                                    return;
                                } else {
                                    sb = new StringBuilder();
                                    sb.append(str);
                                    obj = aSN1Primitive.toString();
                                    sb.append(obj);
                                    sb.append(lineSeparator);
                                    str2 = sb.toString();
                                    stringBuffer.append(str2);
                                    return;
                                }
                                str2 = outputApplicationSpecific(str3, str, z, aSN1Primitive, lineSeparator);
                                stringBuffer.append(str2);
                                return;
                            }
                            sb.append(time);
                            obj = ") ";
                            sb.append(obj);
                            sb.append(lineSeparator);
                            str2 = sb.toString();
                            stringBuffer.append(str2);
                            return;
                        }
                        sb2.append(value);
                    }
                    obj = ")";
                    sb.append(obj);
                    sb.append(lineSeparator);
                    str2 = sb.toString();
                    stringBuffer.append(str2);
                    return;
                }
                str2 = dumpBinaryDataAsString(str, bytes);
                stringBuffer.append(str2);
                return;
            }
            stringBuffer.append(lineSeparator);
        }
    }

    private static String calculateAscString(byte[] bArr, int i, int i2) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i3 = i; i3 != i + i2; i3++) {
            if (bArr[i3] >= 32 && bArr[i3] <= 126) {
                stringBuffer.append((char) bArr[i3]);
            }
        }
        return stringBuffer.toString();
    }

    public static String dumpAsString(Object obj) {
        return dumpAsString(obj, false);
    }

    public static String dumpAsString(Object obj, boolean z) {
        String str;
        ASN1Primitive aSN1Primitive;
        StringBuffer stringBuffer = new StringBuffer();
        if (obj instanceof ASN1Primitive) {
            str = "";
            aSN1Primitive = (ASN1Primitive) obj;
        } else if (obj instanceof ASN1Encodable) {
            str = "";
            aSN1Primitive = ((ASN1Encodable) obj).toASN1Primitive();
        } else {
            return "unknown object type " + obj.toString();
        }
        _dumpAsString(str, z, aSN1Primitive, stringBuffer);
        return stringBuffer.toString();
    }

    private static String dumpBinaryDataAsString(String str, byte[] bArr) {
        String calculateAscString;
        String lineSeparator = Strings.lineSeparator();
        StringBuffer stringBuffer = new StringBuffer();
        String str2 = str + TAB;
        stringBuffer.append(lineSeparator);
        for (int i = 0; i < bArr.length; i += 32) {
            if (bArr.length - i > 32) {
                stringBuffer.append(str2);
                stringBuffer.append(Strings.fromByteArray(Hex.encode(bArr, i, 32)));
                stringBuffer.append(TAB);
                calculateAscString = calculateAscString(bArr, i, 32);
            } else {
                stringBuffer.append(str2);
                stringBuffer.append(Strings.fromByteArray(Hex.encode(bArr, i, bArr.length - i)));
                for (int length = bArr.length - i; length != 32; length++) {
                    stringBuffer.append("  ");
                }
                stringBuffer.append(TAB);
                calculateAscString = calculateAscString(bArr, i, bArr.length - i);
            }
            stringBuffer.append(calculateAscString);
            stringBuffer.append(lineSeparator);
        }
        return stringBuffer.toString();
    }

    private static String outputApplicationSpecific(String str, String str2, boolean z, ASN1Primitive aSN1Primitive, String str3) {
        ASN1ApplicationSpecific instance = ASN1ApplicationSpecific.getInstance(aSN1Primitive);
        StringBuffer stringBuffer = new StringBuffer();
        if (instance.isConstructed()) {
            try {
                ASN1Sequence instance2 = ASN1Sequence.getInstance(instance.getObject(16));
                stringBuffer.append(str2 + str + " ApplicationSpecific[" + instance.getApplicationTag() + "]" + str3);
                Enumeration objects = instance2.getObjects();
                while (objects.hasMoreElements()) {
                    _dumpAsString(str2 + TAB, z, (ASN1Primitive) objects.nextElement(), stringBuffer);
                }
            } catch (IOException e) {
                stringBuffer.append(e);
            }
            return stringBuffer.toString();
        }
        return str2 + str + " ApplicationSpecific[" + instance.getApplicationTag() + "] (" + Strings.fromByteArray(Hex.encode(instance.getContents())) + ")" + str3;
    }
}
