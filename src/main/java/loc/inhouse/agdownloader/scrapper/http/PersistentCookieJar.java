/*
 * Copyright (C) 2016 Francisco José Montiel Navarro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package loc.inhouse.agdownloader.scrapper.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

public class PersistentCookieJar implements CookieJar {
    private CookieCache cache;

    public PersistentCookieJar(CookieCache cache) {
        this.cache = cache;
    }

    synchronized public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cache.addAll(cookies);
    }

    @NotNull
    synchronized public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> validCookies = new ArrayList<>();

        for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
            Cookie currentCookie = it.next();

            if (currentCookie.expiresAt() < System.currentTimeMillis()) {
                it.remove();

            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie);
            }
        }

        return validCookies;
    }
}
