package com.comandulli.lib.cache;

import android.graphics.Bitmap;

import com.beenoculus.platform.video.lib.volley.Response.ErrorListener;
import com.beenoculus.platform.video.lib.volley.Response.Listener;
import com.beenoculus.platform.video.lib.volley.VolleyError;

/**
 * Listener class used by Cache, this maps a cache item request to all cache requests.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class ImageRequestListener implements Listener<Bitmap>, ErrorListener {

    /**
     * Cache path of the item to be acquired.
     */
    private final String cachePath;

    /**
     * Builds a listener to a cache item.
     *
     * @param cachePath Cache path of the item to be acquired.
     */
    public ImageRequestListener(String cachePath) {
        this.cachePath = cachePath;
    }

    /**
     * Response callback, when cache item is ready.
     *
     * @param response Item.
     */
    @Override
    public void onResponse(Bitmap response) {
        Cache.respondImage(cachePath, response);
    }

    /**
     * Callback when the item cannot be acquired.
     *
     * @param error Volley Error.
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        // unable to fetch request, leave as it is
        Cache.removeFromQueue(cachePath);
    }

    /**
     * Callback when there is no connection.
     */
    public void onNoConnection() {
        onErrorResponse(null);
    }

}
