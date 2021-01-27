package org.bouncycastle.i18n.filter;

public interface Filter {
    String doFilter(String str);

    String doFilterUrl(String str);
}
