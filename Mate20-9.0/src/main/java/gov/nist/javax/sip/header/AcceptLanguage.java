package gov.nist.javax.sip.header;

import gov.nist.core.NameValue;
import gov.nist.core.Separators;
import java.util.Locale;
import javax.sip.InvalidArgumentException;
import javax.sip.header.AcceptLanguageHeader;

public final class AcceptLanguage extends ParametersHeader implements AcceptLanguageHeader {
    private static final long serialVersionUID = -4473982069737324919L;
    protected String languageRange;

    public AcceptLanguage() {
        super("Accept-Language");
    }

    /* access modifiers changed from: protected */
    public String encodeBody() {
        StringBuffer encoding = new StringBuffer();
        if (this.languageRange != null) {
            encoding.append(this.languageRange);
        }
        if (!this.parameters.isEmpty()) {
            encoding.append(Separators.SEMICOLON);
            encoding.append(this.parameters.encode());
        }
        return encoding.toString();
    }

    public String getLanguageRange() {
        return this.languageRange;
    }

    public float getQValue() {
        if (!hasParameter("q")) {
            return -1.0f;
        }
        return ((Float) this.parameters.getValue("q")).floatValue();
    }

    public boolean hasQValue() {
        return hasParameter("q");
    }

    public void removeQValue() {
        removeParameter("q");
    }

    public void setLanguageRange(String languageRange2) {
        this.languageRange = languageRange2.trim();
    }

    public void setQValue(float q) throws InvalidArgumentException {
        if (((double) q) < 0.0d || ((double) q) > 1.0d) {
            throw new InvalidArgumentException("qvalue out of range!");
        } else if (q == -1.0f) {
            removeParameter("q");
        } else {
            setParameter(new NameValue("q", Float.valueOf(q)));
        }
    }

    public Locale getAcceptLanguage() {
        if (this.languageRange == null) {
            return null;
        }
        int dash = this.languageRange.indexOf(45);
        if (dash >= 0) {
            return new Locale(this.languageRange.substring(0, dash), this.languageRange.substring(dash + 1));
        }
        return new Locale(this.languageRange);
    }

    public void setAcceptLanguage(Locale language) {
        if ("".equals(language.getCountry())) {
            this.languageRange = language.getLanguage();
            return;
        }
        this.languageRange = language.getLanguage() + '-' + language.getCountry();
    }
}
