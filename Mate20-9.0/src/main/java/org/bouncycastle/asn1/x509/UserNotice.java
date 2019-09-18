package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class UserNotice extends ASN1Object {
    private final DisplayText explicitText;
    private final NoticeReference noticeRef;

    private UserNotice(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2) {
            this.noticeRef = NoticeReference.getInstance(aSN1Sequence.getObjectAt(0));
            this.explicitText = DisplayText.getInstance(aSN1Sequence.getObjectAt(1));
            return;
        }
        if (aSN1Sequence.size() == 1) {
            if (aSN1Sequence.getObjectAt(0).toASN1Primitive() instanceof ASN1Sequence) {
                this.noticeRef = NoticeReference.getInstance(aSN1Sequence.getObjectAt(0));
            } else {
                this.explicitText = DisplayText.getInstance(aSN1Sequence.getObjectAt(0));
                this.noticeRef = null;
                return;
            }
        } else if (aSN1Sequence.size() == 0) {
            this.noticeRef = null;
        } else {
            throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
        }
        this.explicitText = null;
    }

    public UserNotice(NoticeReference noticeReference, String str) {
        this(noticeReference, new DisplayText(str));
    }

    public UserNotice(NoticeReference noticeReference, DisplayText displayText) {
        this.noticeRef = noticeReference;
        this.explicitText = displayText;
    }

    public static UserNotice getInstance(Object obj) {
        if (obj instanceof UserNotice) {
            return (UserNotice) obj;
        }
        if (obj != null) {
            return new UserNotice(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public DisplayText getExplicitText() {
        return this.explicitText;
    }

    public NoticeReference getNoticeRef() {
        return this.noticeRef;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.noticeRef != null) {
            aSN1EncodableVector.add(this.noticeRef);
        }
        if (this.explicitText != null) {
            aSN1EncodableVector.add(this.explicitText);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
