package gov.nist.javax.sip;

import gov.nist.core.Separators;
import gov.nist.javax.sip.message.SIPResponse;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Random;

public class Utils implements UtilsExt {
    private static int callIDCounter;
    private static long counter;
    private static MessageDigest digester;
    private static Utils instance;
    private static Random rand;
    private static String signature;
    private static final char[] toHex = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.javax.sip.Utils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.javax.sip.Utils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.Utils.<clinit>():void");
    }

    public static Utils getInstance() {
        return instance;
    }

    public static String toHexString(byte[] b) {
        int pos = 0;
        char[] c = new char[(b.length * 2)];
        for (int i = 0; i < b.length; i++) {
            int i2 = pos + 1;
            c[pos] = toHex[(b[i] >> 4) & 15];
            pos = i2 + 1;
            c[i2] = toHex[b[i] & 15];
        }
        return new String(c);
    }

    public static String getQuotedString(String str) {
        return '\"' + str.replace(Separators.DOUBLE_QUOTE, "\\\"") + '\"';
    }

    protected static String reduceString(String input) {
        String newString = input.toLowerCase();
        int len = newString.length();
        String retval = "";
        int i = 0;
        while (i < len) {
            if (!(newString.charAt(i) == ' ' || newString.charAt(i) == '\t')) {
                retval = retval + newString.charAt(i);
            }
            i++;
        }
        return retval;
    }

    public synchronized String generateCallIdentifier(String address) {
        long currentTimeMillis;
        int i;
        currentTimeMillis = System.currentTimeMillis();
        i = callIDCounter;
        callIDCounter = i + 1;
        return toHexString(digester.digest(Long.toString((currentTimeMillis + ((long) i)) + rand.nextLong()).getBytes())) + Separators.AT + address;
    }

    public synchronized String generateTag() {
        return Integer.toHexString(rand.nextInt());
    }

    public synchronized String generateBranchId() {
        long nextLong;
        long j;
        nextLong = rand.nextLong();
        j = counter;
        counter = 1 + j;
        return SIPConstants.BRANCH_MAGIC_COOKIE + toHexString(digester.digest(Long.toString((nextLong + j) + System.currentTimeMillis()).getBytes())) + signature;
    }

    public boolean responseBelongsToUs(SIPResponse response) {
        String branch = response.getTopmostVia().getBranch();
        return branch != null ? branch.endsWith(signature) : false;
    }

    public static String getSignature() {
        return signature;
    }

    public static void main(String[] args) {
        HashSet branchIds = new HashSet();
        for (int b = 0; b < 100000; b++) {
            String bid = getInstance().generateBranchId();
            if (branchIds.contains(bid)) {
                throw new RuntimeException("Duplicate Branch ID");
            }
            branchIds.add(bid);
        }
        System.out.println("Done!!");
    }
}
