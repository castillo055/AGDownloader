package loc.inhouse.agdownloader.scrapper.http;

import okhttp3.Cookie;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CookieCache {
    private Set<Cookie> cookies = new HashSet<>();

    public void addAll(Collection<Cookie> newCookies) {
        for (Cookie cookie : newCookies) {
            this.cookies.remove(cookie);
            this.cookies.add(cookie);
        }
    }

    public Iterator<Cookie> iterator() {
        return cookies.iterator();
    }
}
