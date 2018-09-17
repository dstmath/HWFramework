package com.hianalytics.android.a.a;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class c {
    public static SharedPreferences a(Context context, String str) {
        return context.getSharedPreferences("hianalytics_" + str + "_" + context.getPackageName(), 0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(Context context, JSONObject jSONObject, String str) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(d(context, str), 0);
            fileOutputStream.write(jSONObject.toString().getBytes("UTF-8"));
            fileOutputStream.flush();
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e2) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (IOException e4) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            FileOutputStream fileOutputStream2 = fileOutputStream;
            Throwable th3 = th2;
            if (fileOutputStream2 != null) {
                try {
                    fileOutputStream2.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th3;
        }
    }

    public static JSONObject b(Context context, String str) {
        FileInputStream openFileInput;
        BufferedReader bufferedReader;
        FileInputStream fileInputStream;
        BufferedReader bufferedReader2;
        JSONException jSONException;
        Throwable th;
        Exception exception;
        BufferedReader bufferedReader3 = null;
        try {
            openFileInput = context.openFileInput(d(context, str));
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(openFileInput, "UTF-8"));
                try {
                    StringBuffer stringBuffer = new StringBuffer("");
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(readLine);
                    }
                    if (stringBuffer.length() != 0) {
                        JSONObject jSONObject = new JSONObject(stringBuffer.toString());
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (openFileInput != null) {
                            try {
                                openFileInput.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        return jSONObject;
                    }
                    try {
                        bufferedReader.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    return null;
                } catch (FileNotFoundException e3) {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    return null;
                } catch (IOException e4) {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e222222) {
                            e222222.printStackTrace();
                        }
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e2222222) {
                            e2222222.printStackTrace();
                        }
                    }
                    return null;
                } catch (JSONException e5) {
                    JSONException jSONException2 = e5;
                    fileInputStream = openFileInput;
                    bufferedReader2 = bufferedReader;
                    jSONException = jSONException2;
                    try {
                        jSONException.printStackTrace();
                        c(context, str);
                        if (bufferedReader2 != null) {
                            try {
                                bufferedReader2.close();
                            } catch (IOException e22222222) {
                                e22222222.printStackTrace();
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e222222222) {
                                e222222222.printStackTrace();
                            }
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader3 = bufferedReader2;
                        openFileInput = fileInputStream;
                        if (bufferedReader3 != null) {
                            try {
                                bufferedReader3.close();
                            } catch (IOException e6) {
                                e6.printStackTrace();
                            }
                        }
                        if (openFileInput != null) {
                            try {
                                openFileInput.close();
                            } catch (IOException e62) {
                                e62.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Exception e7) {
                    Exception exception2 = e7;
                    fileInputStream = openFileInput;
                    bufferedReader2 = bufferedReader;
                    exception = exception2;
                    exception.printStackTrace();
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e2222222222) {
                            e2222222222.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22222222222) {
                            e22222222222.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th3) {
                    Throwable th4 = th3;
                    bufferedReader3 = bufferedReader;
                    th = th4;
                    if (bufferedReader3 != null) {
                        bufferedReader3.close();
                    }
                    if (openFileInput != null) {
                        openFileInput.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                bufferedReader = null;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (openFileInput != null) {
                    openFileInput.close();
                }
                return null;
            } catch (IOException e9) {
                bufferedReader = null;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (openFileInput != null) {
                    openFileInput.close();
                }
                return null;
            } catch (JSONException e10) {
                jSONException = e10;
                fileInputStream = openFileInput;
                bufferedReader2 = null;
                jSONException.printStackTrace();
                c(context, str);
                if (bufferedReader2 != null) {
                    bufferedReader2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return null;
            } catch (Exception e11) {
                exception = e11;
                fileInputStream = openFileInput;
                bufferedReader2 = null;
                exception.printStackTrace();
                if (bufferedReader2 != null) {
                    bufferedReader2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return null;
            } catch (Throwable th5) {
                th = th5;
                if (bufferedReader3 != null) {
                    bufferedReader3.close();
                }
                if (openFileInput != null) {
                    openFileInput.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e12) {
            bufferedReader = null;
            openFileInput = null;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (openFileInput != null) {
                openFileInput.close();
            }
            return null;
        } catch (IOException e13) {
            bufferedReader = null;
            openFileInput = null;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (openFileInput != null) {
                openFileInput.close();
            }
            return null;
        } catch (JSONException e14) {
            jSONException = e14;
            bufferedReader2 = null;
            fileInputStream = null;
            jSONException.printStackTrace();
            c(context, str);
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (Exception e15) {
            exception = e15;
            bufferedReader2 = null;
            fileInputStream = null;
            exception.printStackTrace();
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (Throwable th6) {
            th = th6;
            openFileInput = null;
            if (bufferedReader3 != null) {
                bufferedReader3.close();
            }
            if (openFileInput != null) {
                openFileInput.close();
            }
            throw th;
        }
    }

    public static void c(Context context, String str) {
        context.deleteFile(d(context, str));
    }

    private static String d(Context context, String str) {
        return "hianalytics_" + str + "_" + context.getPackageName();
    }
}
