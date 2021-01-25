package org.bouncycastle.cms;

import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.asn1.cms.OriginatorInfo;

public class CMSAuthEnvelopedGenerator extends CMSEnvelopedGenerator {
    protected CMSAttributeTableGenerator authAttrsGenerator = null;
    protected OriginatorInfo originatorInfo;
    final List recipientInfoGenerators = new ArrayList();
    protected CMSAttributeTableGenerator unauthAttrsGenerator = null;

    protected CMSAuthEnvelopedGenerator() {
    }

    @Override // org.bouncycastle.cms.CMSEnvelopedGenerator
    public void addRecipientInfoGenerator(RecipientInfoGenerator recipientInfoGenerator) {
        this.recipientInfoGenerators.add(recipientInfoGenerator);
    }

    public void setAuthenticatedAttributeGenerator(CMSAttributeTableGenerator cMSAttributeTableGenerator) {
        this.authAttrsGenerator = cMSAttributeTableGenerator;
    }

    @Override // org.bouncycastle.cms.CMSEnvelopedGenerator
    public void setOriginatorInfo(OriginatorInformation originatorInformation) {
        this.originatorInfo = originatorInformation.toASN1Structure();
    }

    public void setUnauthenticatedAttributeGenerator(CMSAttributeTableGenerator cMSAttributeTableGenerator) {
        this.unauthAttrsGenerator = cMSAttributeTableGenerator;
    }
}
