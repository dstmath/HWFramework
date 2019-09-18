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
    private static final char[] toHex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

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
            int pos2 = pos + 1;
            c[pos] = toHex[(b[i] >> 4) & 15];
            pos = pos2 + 1;
            c[pos2] = toHex[b[i] & 15];
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
        for (int i = 0; i < len; i++) {
            if (!(newString.charAt(i) == ' ' || newString.charAt(i) == 9)) {
                retval = retval + newString.charAt(i);
            }
        }
        return retval;
    }

    public synchronized String generateCallIdentifier(String address) {
        String cidString;
        long currentTimeMillis = System.currentTimeMillis();
        int i = callIDCounter;
        callIDCounter = i + 1;
        cidString = toHexString(digester.digest(Long.toString(currentTimeMillis + ((long) i) + rand.nextLong()).getBytes()));
        return cidString + Separators.AT + address;
    }

    public synchronized String generateTag() {
        return Integer.toHexString(rand.nextInt());
    }

    public synchronized String generateBranchId() {
        byte[] bid;
        long nextLong = rand.nextLong();
        long j = counter;
        counter = 1 + j;
        bid = digester.digest(Long.toString(nextLong + j + System.currentTimeMillis()).getBytes());
        return SIPConstants.BRANCH_MAGIC_COOKIE + toHexString(bid) + signature;
    }

    public boolean responseBelongsToUs(SIPResponse response) {
        String branch = response.getTopmostVia().getBranch();
        return branch != null && branch.endsWith(signature);
    }

    public static String getSignature() {
        return signature;
    }

    public static void main(String[] args) {
        HashSet branchIds = new HashSet();
        int b = 0;
        while (b < 100000) {
            String bid = getInstance().generateBranchId();
            if (!branchIds.contains(bid)) {
                branchIds.add(bid);
                b++;
            } else {
                throw new RuntimeException("Duplicate Branch ID");
            }
        }
        System.out.println("Done!!");
    }
}
