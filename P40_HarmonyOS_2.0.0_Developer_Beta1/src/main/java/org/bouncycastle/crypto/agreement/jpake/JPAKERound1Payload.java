package org.bouncycastle.crypto.agreement.jpake;

import java.math.BigInteger;
import org.bouncycastle.util.Arrays;

public class JPAKERound1Payload {
    private final BigInteger gx1;
    private final BigInteger gx2;
    private final BigInteger[] knowledgeProofForX1;
    private final BigInteger[] knowledgeProofForX2;
    private final String participantId;

    public JPAKERound1Payload(String str, BigInteger bigInteger, BigInteger bigInteger2, BigInteger[] bigIntegerArr, BigInteger[] bigIntegerArr2) {
        JPAKEUtil.validateNotNull(str, "participantId");
        JPAKEUtil.validateNotNull(bigInteger, "gx1");
        JPAKEUtil.validateNotNull(bigInteger2, "gx2");
        JPAKEUtil.validateNotNull(bigIntegerArr, "knowledgeProofForX1");
        JPAKEUtil.validateNotNull(bigIntegerArr2, "knowledgeProofForX2");
        this.participantId = str;
        this.gx1 = bigInteger;
        this.gx2 = bigInteger2;
        this.knowledgeProofForX1 = Arrays.copyOf(bigIntegerArr, bigIntegerArr.length);
        this.knowledgeProofForX2 = Arrays.copyOf(bigIntegerArr2, bigIntegerArr2.length);
    }

    public BigInteger getGx1() {
        return this.gx1;
    }

    public BigInteger getGx2() {
        return this.gx2;
    }

    public BigInteger[] getKnowledgeProofForX1() {
        BigInteger[] bigIntegerArr = this.knowledgeProofForX1;
        return Arrays.copyOf(bigIntegerArr, bigIntegerArr.length);
    }

    public BigInteger[] getKnowledgeProofForX2() {
        BigInteger[] bigIntegerArr = this.knowledgeProofForX2;
        return Arrays.copyOf(bigIntegerArr, bigIntegerArr.length);
    }

    public String getParticipantId() {
        return this.participantId;
    }
}
