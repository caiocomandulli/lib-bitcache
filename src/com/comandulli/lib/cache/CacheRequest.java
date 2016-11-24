package com.comandulli.lib.cache;

/**
 * A cache request, works as a listener, onResponse will be called when the file is available.
 *
 * @param <T> File Type.
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public interface CacheRequest<T> {

    /**
     * Called when the file is available.
     *
     * @param response File cached.
     */
    void onResponse(T response);

}
