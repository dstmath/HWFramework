package gov.nist.javax.sip;

import javax.sip.SipProvider;

public interface SipProviderExt extends SipProvider {
    void setDialogErrorsAutomaticallyHandled();
}
