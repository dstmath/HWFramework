package java.util.regex;

public interface MatchResult {
    int end();

    int end(int i);

    String group();

    String group(int i);

    int groupCount();

    int start();

    int start(int i);
}
