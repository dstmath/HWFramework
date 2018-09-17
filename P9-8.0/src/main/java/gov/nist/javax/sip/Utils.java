package gov.nist.javax.sip;

import gov.nist.core.Separators;
import gov.nist.javax.sip.message.SIPResponse;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Random;

public class Utils implements UtilsExt {
    private static int callIDCounter;
    private static long counter = 0;
    private static MessageDigest digester;
    private static Utils instance = new Utils();
    private static Random rand = new Random();
    private static String signature = toHexString(Integer.toString(Math.abs(rand.nextInt() % 1000)).getBytes());
    private static final char[] toHex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static {
        try {
            digester = MessageDigest.getInstance("MD5");
        } catch (Exception ex) {
            throw new RuntimeException("Could not intialize Digester ", ex);
        }
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
            if (!(newString.charAt(i) == ' ' || newString.charAt(i) == 9)) {
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
