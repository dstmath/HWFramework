package gov.nist.javax.sip.header.ims;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ParametersHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class PUserDatabase extends ParametersHeader implements PUserDatabaseHeader, SIPHeaderNamesIms, ExtensionHeader {
    private String databaseName;

    public PUserDatabase(String databaseName) {
        super("P-User-Database");
        this.databaseName = databaseName;
    }

    public PUserDatabase() {
        super("P-User-Database");
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String databaseName) {
        if (databaseName == null || databaseName.equals(Separators.SP)) {
            throw new NullPointerException("Database name is null");
        } else if (databaseName.contains("aaa://")) {
            this.databaseName = databaseName;
        } else {
            this.databaseName = new StringBuffer().append("aaa://").append(databaseName).toString();
        }
    }

    protected String encodeBody() {
        StringBuffer retval = new StringBuffer();
        retval.append(Separators.LESS_THAN);
        if (getDatabaseName() != null) {
            retval.append(getDatabaseName());
        }
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON + this.parameters.encode());
        }
        retval.append(Separators.GREATER_THAN);
        return retval.toString();
    }

    public boolean equals(Object other) {
        return other instanceof PUserDatabaseHeader ? super.equals(other) : false;
    }

    public Object clone() {
        return (PUserDatabase) super.clone();
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
