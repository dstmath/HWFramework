package ohos.com.sun.org.apache.regexp.internal;

public class recompile {
    public static void main(String[] strArr) {
        RECompiler rECompiler = new RECompiler();
        if (strArr.length <= 0 || strArr.length % 2 != 0) {
            System.out.println("Usage: recompile <patternname> <pattern>");
            System.exit(0);
        }
        for (int i = 0; i < strArr.length; i += 2) {
            try {
                String str = strArr[i];
                String str2 = strArr[i + 1];
                String str3 = str + "PatternInstructions";
                System.out.print("\n    // Pre-compiled regular expression '" + str2 + "'\n    private static char[] " + str3 + " = \n    {");
                char[] instructions = rECompiler.compile(str2).getInstructions();
                for (int i2 = 0; i2 < instructions.length; i2++) {
                    if (i2 % 7 == 0) {
                        System.out.print("\n        ");
                    }
                    String hexString = Integer.toHexString(instructions[i2]);
                    while (hexString.length() < 4) {
                        hexString = "0" + hexString;
                    }
                    System.out.print("0x" + hexString + ", ");
                }
                System.out.println("\n    };");
                System.out.println("\n    private static RE " + str + "Pattern = new RE(new REProgram(" + str3 + "));");
            } catch (RESyntaxException e) {
                System.out.println("Syntax error in expression \"" + strArr[i] + "\": " + e.toString());
            } catch (Exception e2) {
                System.out.println("Unexpected exception: " + e2.toString());
            } catch (Error e3) {
                System.out.println("Internal error: " + e3.toString());
            }
        }
    }
}
