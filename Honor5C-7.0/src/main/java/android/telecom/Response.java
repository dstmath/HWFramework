package android.telecom;

public interface Response<IN, OUT> {
    void onError(IN in, int i, String str);

    void onResult(IN in, OUT... outArr);
}
