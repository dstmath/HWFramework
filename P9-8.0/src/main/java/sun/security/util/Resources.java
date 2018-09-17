package sun.security.util;

import java.util.ListResourceBundle;

public class Resources extends ListResourceBundle {
    private static final Object[][] contents;

    static {
        Object[][] objArr = new Object[65][];
        objArr[0] = new Object[]{"invalid.null.input.s.", "invalid null input(s)"};
        objArr[1] = new Object[]{"actions.can.only.be.read.", "actions can only be 'read'"};
        objArr[2] = new Object[]{"permission.name.name.syntax.invalid.", "permission name [{0}] syntax invalid: "};
        objArr[3] = new Object[]{"Credential.Class.not.followed.by.a.Principal.Class.and.Name", "Credential Class not followed by a Principal Class and Name"};
        objArr[4] = new Object[]{"Principal.Class.not.followed.by.a.Principal.Name", "Principal Class not followed by a Principal Name"};
        objArr[5] = new Object[]{"Principal.Name.must.be.surrounded.by.quotes", "Principal Name must be surrounded by quotes"};
        objArr[6] = new Object[]{"Principal.Name.missing.end.quote", "Principal Name missing end quote"};
        objArr[7] = new Object[]{"PrivateCredentialPermission.Principal.Class.can.not.be.a.wildcard.value.if.Principal.Name.is.not.a.wildcard.value", "PrivateCredentialPermission Principal Class can not be a wildcard (*) value if Principal Name is not a wildcard (*) value"};
        objArr[8] = new Object[]{"CredOwner.Principal.Class.class.Principal.Name.name", "CredOwner:\n\tPrincipal Class = {0}\n\tPrincipal Name = {1}"};
        objArr[9] = new Object[]{"provided.null.name", "provided null name"};
        objArr[10] = new Object[]{"provided.null.keyword.map", "provided null keyword map"};
        objArr[11] = new Object[]{"provided.null.OID.map", "provided null OID map"};
        objArr[12] = new Object[]{"NEWLINE", "\n"};
        objArr[13] = new Object[]{"invalid.null.AccessControlContext.provided", "invalid null AccessControlContext provided"};
        objArr[14] = new Object[]{"invalid.null.action.provided", "invalid null action provided"};
        objArr[15] = new Object[]{"invalid.null.Class.provided", "invalid null Class provided"};
        objArr[16] = new Object[]{"Subject.", "Subject:\n"};
        objArr[17] = new Object[]{".Principal.", "\tPrincipal: "};
        objArr[18] = new Object[]{".Public.Credential.", "\tPublic Credential: "};
        objArr[19] = new Object[]{".Private.Credentials.inaccessible.", "\tPrivate Credentials inaccessible\n"};
        objArr[20] = new Object[]{".Private.Credential.", "\tPrivate Credential: "};
        objArr[21] = new Object[]{".Private.Credential.inaccessible.", "\tPrivate Credential inaccessible\n"};
        objArr[22] = new Object[]{"Subject.is.read.only", "Subject is read-only"};
        objArr[23] = new Object[]{"attempting.to.add.an.object.which.is.not.an.instance.of.java.security.Principal.to.a.Subject.s.Principal.Set", "attempting to add an object which is not an instance of java.security.Principal to a Subject's Principal Set"};
        objArr[24] = new Object[]{"attempting.to.add.an.object.which.is.not.an.instance.of.class", "attempting to add an object which is not an instance of {0}"};
        objArr[25] = new Object[]{"LoginModuleControlFlag.", "LoginModuleControlFlag: "};
        objArr[26] = new Object[]{"Invalid.null.input.name", "Invalid null input: name"};
        objArr[27] = new Object[]{"No.LoginModules.configured.for.name", "No LoginModules configured for {0}"};
        objArr[28] = new Object[]{"invalid.null.Subject.provided", "invalid null Subject provided"};
        objArr[29] = new Object[]{"invalid.null.CallbackHandler.provided", "invalid null CallbackHandler provided"};
        objArr[30] = new Object[]{"null.subject.logout.called.before.login", "null subject - logout called before login"};
        objArr[31] = new Object[]{"unable.to.instantiate.LoginModule.module.because.it.does.not.provide.a.no.argument.constructor", "unable to instantiate LoginModule, {0}, because it does not provide a no-argument constructor"};
        objArr[32] = new Object[]{"unable.to.instantiate.LoginModule", "unable to instantiate LoginModule"};
        objArr[33] = new Object[]{"unable.to.instantiate.LoginModule.", "unable to instantiate LoginModule: "};
        objArr[34] = new Object[]{"unable.to.find.LoginModule.class.", "unable to find LoginModule class: "};
        objArr[35] = new Object[]{"unable.to.access.LoginModule.", "unable to access LoginModule: "};
        objArr[36] = new Object[]{"Login.Failure.all.modules.ignored", "Login Failure: all modules ignored"};
        objArr[37] = new Object[]{"java.security.policy.error.parsing.policy.message", "java.security.policy: error parsing {0}:\n\t{1}"};
        objArr[38] = new Object[]{"java.security.policy.error.adding.Permission.perm.message", "java.security.policy: error adding Permission, {0}:\n\t{1}"};
        objArr[39] = new Object[]{"java.security.policy.error.adding.Entry.message", "java.security.policy: error adding Entry:\n\t{0}"};
        objArr[40] = new Object[]{"alias.name.not.provided.pe.name.", "alias name not provided ({0})"};
        objArr[41] = new Object[]{"unable.to.perform.substitution.on.alias.suffix", "unable to perform substitution on alias, {0}"};
        objArr[42] = new Object[]{"substitution.value.prefix.unsupported", "substitution value, {0}, unsupported"};
        objArr[43] = new Object[]{"LPARAM", "("};
        objArr[44] = new Object[]{"RPARAM", ")"};
        objArr[45] = new Object[]{"type.can.t.be.null", "type can't be null"};
        objArr[46] = new Object[]{"keystorePasswordURL.can.not.be.specified.without.also.specifying.keystore", "keystorePasswordURL can not be specified without also specifying keystore"};
        objArr[47] = new Object[]{"expected.keystore.type", "expected keystore type"};
        objArr[48] = new Object[]{"expected.keystore.provider", "expected keystore provider"};
        objArr[49] = new Object[]{"multiple.Codebase.expressions", "multiple Codebase expressions"};
        objArr[50] = new Object[]{"multiple.SignedBy.expressions", "multiple SignedBy expressions"};
        objArr[51] = new Object[]{"duplicate.keystore.domain.name", "duplicate keystore domain name: {0}"};
        objArr[52] = new Object[]{"duplicate.keystore.name", "duplicate keystore name: {0}"};
        objArr[53] = new Object[]{"SignedBy.has.empty.alias", "SignedBy has empty alias"};
        objArr[54] = new Object[]{"can.not.specify.Principal.with.a.wildcard.class.without.a.wildcard.name", "can not specify Principal with a wildcard class without a wildcard name"};
        objArr[55] = new Object[]{"expected.codeBase.or.SignedBy.or.Principal", "expected codeBase or SignedBy or Principal"};
        objArr[56] = new Object[]{"expected.permission.entry", "expected permission entry"};
        objArr[57] = new Object[]{"number.", "number "};
        objArr[58] = new Object[]{"expected.expect.read.end.of.file.", "expected [{0}], read [end of file]"};
        objArr[59] = new Object[]{"expected.read.end.of.file.", "expected [;], read [end of file]"};
        objArr[60] = new Object[]{"line.number.msg", "line {0}: {1}"};
        objArr[61] = new Object[]{"line.number.expected.expect.found.actual.", "line {0}: expected [{1}], found [{2}]"};
        objArr[62] = new Object[]{"null.principalClass.or.principalName", "null principalClass or principalName"};
        objArr[63] = new Object[]{"PKCS11.Token.providerName.Password.", "PKCS11 Token [{0}] Password: "};
        objArr[64] = new Object[]{"unable.to.instantiate.Subject.based.policy", "unable to instantiate Subject-based policy"};
        contents = objArr;
    }

    public Object[][] getContents() {
        return contents;
    }
}
