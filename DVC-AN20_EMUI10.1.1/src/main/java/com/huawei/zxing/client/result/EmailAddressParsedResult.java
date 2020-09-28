package com.huawei.zxing.client.result;

public final class EmailAddressParsedResult extends ParsedResult {
    private final String body;
    private final String emailAddress;
    private final String mailtoURI;
    private final String subject;

    EmailAddressParsedResult(String emailAddress2, String subject2, String body2, String mailtoURI2) {
        super(ParsedResultType.EMAIL_ADDRESS);
        this.emailAddress = emailAddress2;
        this.subject = subject2;
        this.body = body2;
        this.mailtoURI = mailtoURI2;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getBody() {
        return this.body;
    }

    public String getMailtoURI() {
        return this.mailtoURI;
    }

    @Override // com.huawei.zxing.client.result.ParsedResult
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(30);
        maybeAppend(this.emailAddress, result);
        maybeAppend(this.subject, result);
        maybeAppend(this.body, result);
        return result.toString();
    }
}
