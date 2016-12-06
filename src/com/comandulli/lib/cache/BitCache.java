package com.comandulli.lib.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.widget.ImageView.ScaleType;

import com.android.volley.toolbox.ImageRequest;
import com.comandulli.lib.MD5;
import com.comandulli.lib.rest.NetworkRequestQueue;
import com.comandulli.lib.rest.VolleyRequest;
import com.comandulli.lib.rest.exception.NoInternetConnectionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * BitCache was designed to manage image caching in android.
 * This library was designed entirely with static methods as to allow its
 * from any point of your code.
 * <p>
 * Please do call {@link #init(Context)} before using it.
 * <p>
 * When a file is needed you create a BitCacheRequest {@see package com.comandulli.lib.cache.BitCacheRequest}, this binds
 * the request to a hash entry, if the entry is already existent the cached file is returned, otherwise the file
 * is acquired in the provided URL and then cached and referenced in the hash entry.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class BitCache {

    /**
     * Android Data Folder.
     */
    public static final String DATA_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/";
    /**
     * Cache folder.
     */
    public static final String CACHE_FOLDER = "/cache";
    /**
     * Image folder withing cache.
     */
    public static final String IMAGE_FOLDER = "/images";
    /**
     * Video folder withing cache.
     */
    public static final String VIDEO_FOLDER = "/videos";
    /**
     * Compression size.
     */
    public static final int COMPRESSION_WIDTH = 512;

    /**
     * Resulting data folder, appointed in {@link #init(Context)};
     */
    private static String dataFolder;
    /**
     * Current android context, appointed in {@link #init(Context)};
     */
    private static Context currContext;
    /**
     * List of all already cached items.
     */
    private static List<String> cached;
    /**
     * List of all requests to cached items.
     */
    private static Hashtable<String, List<BitCacheRequest<?>>> queued;

    /**
     * Initializes all variables.
     *
     * @param context Android context to be used, needed to generate the cache folder.
     */
    public static void init(Context context) {
        cached = new ArrayList<>();
        queued = new Hashtable<>();
        dataFolder = DATA_FOLDER + context.getPackageName() + CACHE_FOLDER;
        currContext = context;
        File cacheFolder = new File(dataFolder);
        if (cacheFolder.exists()) {
            listFiles(cacheFolder, cached, File.separator);
        } else {
            if (!cacheFolder.mkdirs()) {
                Log.w("Folder creation", "Failed to create folder");
            }
        }
    }

    /**
     * Method that responds all requests when a cache request is done.
     *
     * @param cachePath Path of the item in the cache folder.
     * @param response  Bitmap to be responded.
     */
    @SuppressWarnings("unchecked")
    public static void respondImage(final String cachePath, final Bitmap response) {
        // cache it
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean cacheError = false;
                File cacheFile = new File(dataFolder + cachePath);
                if (cacheFile.exists()) {
                    cacheError = true;
                } else {
                    if (!cacheFile.getParentFile().exists()) {
                        if (!cacheFile.getParentFile().mkdirs()) {
                            Log.w("Folder creation", "Failed to create folder");
                        }
                    }
                    try {
                        if (!cacheFile.createNewFile()) {
                            Log.w("File creation", "Failed to create file");
                        }
                    } catch (IOException e) {
                        cacheError = true;
                    }
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(dataFolder + cachePath);
                        response.compress(CompressFormat.PNG, 100, out);
                    } catch (Exception e) {
                        cacheError = true;
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            cacheError = true;
                        }
                    }
                }
                if (!cacheError) {
                    cached.add(cachePath);
                }
            }
        }).start();
        // respond
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<BitCacheRequest<?>> requests = queued.get(cachePath);
                Bitmap scaled = scaleDownBitmap(response, true);
                for (BitCacheRequest<?> request : requests) {
                    ((BitCacheRequest<Bitmap>) request).onResponse(scaled);
                }
                queued.remove(cachePath);
            }
        }).start();
    }

    /**
     * List all files contained underneath a provided folder.
     *
     * @param file         folder to be listed.
     * @param list         list to be filled with the results.
     * @param relativePath relative path, usually pointing to the folder provided.
     */
    public static void listFiles(File file, List<String> list, String relativePath) {
        File[] children = file.listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                listFiles(child, list, relativePath + child.getName() + File.separator);
            } else {
                list.add(relativePath + child.getName());
            }
        }
    }

    /**
     * Creates a request for a specific file in cache or to be acquired an then cached.
     *
     * @param url     Url or path to the required resource.
     *                It can be a HTTP request as "http://example.com/examples/image".
     *                A android resource in apk as "android.resource://com.example.my/R.drawable.example".
     *                Or a file in the filesystem as "file:///tmp/android.txt". (Don't forget file permissions)
     * @param request Request object.
     * @param query   Any query param needed in the url. Has no influence in cache location.
     */
    public static void requestImage(String url, BitCacheRequest<Bitmap> request, String query) {
        String cachePath = IMAGE_FOLDER + File.separator + MD5.encode(url);
        boolean isCached = false;
        // check if in cache
        if (cached.contains(cachePath)) {
            // return cached one
            File cacheFile = new File(dataFolder + cachePath);
            if (cacheFile.exists()) {
                isCached = true;
                Bitmap bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
                if (bitmap != null) {
                    request.onResponse(scaleDownBitmap(bitmap, false));
                } else {
                    isCached = false;
                }
            }
        }
        if (!isCached) {
            // check if downloading
            if (queued.containsKey(cachePath)) {
                // put into queue
                queued.get(cachePath).add(request);
            } else {
                final ImageRequestListener listener = new ImageRequestListener(cachePath);
                // see if it is for resource
                if (url.startsWith("android.resource://")) {
                    // test resource name
                    String[] split = url.split("/");
                    String lastSegment = split[split.length - 1];
                    try {
                        // valid resource
                        //noinspection ResultOfMethodCallIgnored
                        Integer.parseInt(lastSegment);
                    } catch (NumberFormatException e) {
                        // must resolve name
                        String[] dotSplit = lastSegment.split("[.]");
                        String name = dotSplit[dotSplit.length - 1];
                        int drawableResourceId = currContext.getResources().getIdentifier(name, "drawable", currContext.getPackageName());
                        int lastBreak = url.lastIndexOf("/");
                        url = url.substring(0, lastBreak + 1) + drawableResourceId;
                    }
                    final Uri uri = Uri.parse(url);
                    new AsyncTask<Integer, Integer, Integer>() {
                        @Override
                        protected Integer doInBackground(Integer... params) {
                            try {
                                Bitmap bitmap = Media.getBitmap(currContext.getContentResolver(), uri);
                                listener.onResponse(scaleDownBitmap(bitmap, true));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    }.execute();
                } else {
                    // start download
                    ImageRequest volleyRequest = new ImageRequest(url + query, listener, 0, 0, ScaleType.CENTER_INSIDE, null, listener) {
                        @Override
                        public Map<String, String> getHeaders() {
                            return VolleyRequest.headers;
                        }
                    };
                    try {
                        NetworkRequestQueue.makeRequest(volleyRequest);
                    } catch (NoInternetConnectionException e) {
                        listener.onNoConnection();
                    }
                }
                // put into queue
                List<BitCacheRequest<?>> requests = new ArrayList<>();
                requests.add(request);
                queued.put(cachePath, requests);
            }
        }
    }

    /**
     * Removes all cache entries and delete all cached data.
     *
     * @param ignore Values to be ignored.
     */
    public static void cleanCache(List<String> ignore) {
        List<String> toRemove = new ArrayList<>();
        for (String cachedValue : cached) {
            if (!ignore.contains(cachedValue)) {
                File invalidCache = new File(dataFolder + cachedValue);
                if (!invalidCache.delete()) {
                    Log.w("File deletion", "Failed to delete file");
                }
                toRemove.add(cachedValue);
            }
        }
        for (String value : toRemove) {
            cached.remove(value);
        }
    }

    /**
     * Removes all cache requests to this entry.
     *
     * @param cachePath Path of the cache entry.
     */
    public static void removeFromQueue(String cachePath) {
        queued.remove(cachePath);
    }

    /**
     * Small private method that scales down large bitmaps that may cause memory problems.
     *
     * @param bitmap  Image to be reduced.
     * @param recycle Check if recycling of the bitmap object is needed.
     * @return The reduced Bitmap.
     */
    private static Bitmap scaleDownBitmap(Bitmap bitmap, boolean recycle) {
        int nh = (int) (bitmap.getHeight() * ((float) COMPRESSION_WIDTH / bitmap.getWidth()));
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, COMPRESSION_WIDTH, nh, true);
        if (recycle) {
            bitmap.recycle();
        }
        return scaled;
    }

}
