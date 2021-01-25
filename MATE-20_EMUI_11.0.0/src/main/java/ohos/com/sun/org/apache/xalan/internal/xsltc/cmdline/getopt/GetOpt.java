package ohos.com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.global.icu.impl.locale.LanguageTag;

public class GetOpt {
    private List theCmdArgs;
    private Option theCurrentOption;
    private OptionMatcher theOptionMatcher;
    private List theOptions;
    private ListIterator theOptionsIterator;

    public GetOpt(String[] strArr, String str) {
        this.theCurrentOption = null;
        this.theOptions = null;
        this.theCmdArgs = null;
        this.theOptionMatcher = null;
        this.theOptions = new ArrayList();
        this.theCmdArgs = new ArrayList();
        this.theOptionMatcher = new OptionMatcher(str);
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i2 >= strArr.length) {
                break;
            }
            String str2 = strArr[i2];
            int length = str2.length();
            if (str2.equals("--")) {
                i = i2 + 1;
                break;
            }
            if (str2.startsWith(LanguageTag.SEP) && length == 2) {
                this.theOptions.add(new Option(str2.charAt(1)));
            } else if (str2.startsWith(LanguageTag.SEP) && length > 2) {
                for (int i3 = 1; i3 < length; i3++) {
                    this.theOptions.add(new Option(str2.charAt(i3)));
                }
            } else if (!str2.startsWith(LanguageTag.SEP)) {
                if (this.theOptions.size() == 0) {
                    break;
                }
                Option option = (Option) this.theOptions.get(this.theOptions.size() - 1);
                char argLetter = option.getArgLetter();
                if (option.hasArg() || !this.theOptionMatcher.hasArg(argLetter)) {
                    break;
                }
                option.setArg(str2);
            } else {
                continue;
            }
            i2++;
        }
        i = i2;
        this.theOptionsIterator = this.theOptions.listIterator();
        while (i < strArr.length) {
            this.theCmdArgs.add(strArr[i]);
            i++;
        }
    }

    public void printOptions() {
        ListIterator listIterator = this.theOptions.listIterator();
        while (listIterator.hasNext()) {
            Option option = (Option) listIterator.next();
            PrintStream printStream = System.out;
            printStream.print("OPT =" + option.getArgLetter());
            String argument = option.getArgument();
            if (argument != null) {
                PrintStream printStream2 = System.out;
                printStream2.print(" " + argument);
            }
            System.out.println();
        }
    }

    public int getNextOption() throws IllegalArgumentException, MissingOptArgException {
        if (!this.theOptionsIterator.hasNext()) {
            return -1;
        }
        this.theCurrentOption = (Option) this.theOptionsIterator.next();
        char argLetter = this.theCurrentOption.getArgLetter();
        boolean hasArg = this.theOptionMatcher.hasArg(argLetter);
        String argument = this.theCurrentOption.getArgument();
        if (!this.theOptionMatcher.match(argLetter)) {
            throw new IllegalArgumentException(new ErrorMsg(ErrorMsg.ILLEGAL_CMDLINE_OPTION_ERR, new Character(argLetter)).toString());
        } else if (!hasArg || argument != null) {
            return argLetter;
        } else {
            throw new MissingOptArgException(new ErrorMsg(ErrorMsg.CMDLINE_OPT_MISSING_ARG_ERR, new Character(argLetter)).toString());
        }
    }

    public String getOptionArg() {
        String argument = this.theCurrentOption.getArgument();
        if (this.theOptionMatcher.hasArg(this.theCurrentOption.getArgLetter())) {
            return argument;
        }
        return null;
    }

    public String[] getCmdArgs() {
        String[] strArr = new String[this.theCmdArgs.size()];
        ListIterator listIterator = this.theCmdArgs.listIterator();
        int i = 0;
        while (listIterator.hasNext()) {
            strArr[i] = (String) listIterator.next();
            i++;
        }
        return strArr;
    }

    class Option {
        private char theArgLetter;
        private String theArgument = null;

        public Option(char c) {
            this.theArgLetter = c;
        }

        public void setArg(String str) {
            this.theArgument = str;
        }

        public boolean hasArg() {
            return this.theArgument != null;
        }

        public char getArgLetter() {
            return this.theArgLetter;
        }

        public String getArgument() {
            return this.theArgument;
        }
    }

    class OptionMatcher {
        private String theOptString = null;

        public OptionMatcher(String str) {
            this.theOptString = str;
        }

        public boolean match(char c) {
            return this.theOptString.indexOf(c) != -1;
        }

        public boolean hasArg(char c) {
            int indexOf = this.theOptString.indexOf(c) + 1;
            if (indexOf != this.theOptString.length() && this.theOptString.charAt(indexOf) == ':') {
                return true;
            }
            return false;
        }
    }
}
