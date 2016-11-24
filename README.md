# Cache Android Library

Cache was designed to manage image caching in android.

Making it easier for you to pre-load images and reuse them.

## Usage
 
Call the init method before using this library.
 
```java
 Cache.init(context);
 ```
 
You create a CacheRequest<T> object to make a request to cache.

As in the example we request a Bitmap to use in a ImageView.
 
```java
  final ImageView imageView = (ImageView) findViewById(resource);
  Cache.requestImage("myPathInCache", new CacheRequest<Bitmap>() {
      @Override
      public void onResponse(final Bitmap response) {
          context.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  imageView.setImageBitmap(response);
              }
          });
      }
  }, "?query=myquery";
  ```

All responses are persisted in the cache folder. 

Next initialization the file will be already in cache, and whenever you make a request it will already be ready to use.

## Install Library

__Step 1.__ Get this code and compile it

__Step 2.__ Define a dependency within your project. For that, access to Properties > Android > Library and click on add and select the library

##  License

MIT License. See the file LICENSE.md with the full license text.

### Third party libraries

This library uses REST Android Library by Caio Comandulli (myself), version 1.0. Copyright (C) 2016 Caio Comandulli. Licensed under MIT License.

## Compatibility

This Library is valid for Android systems from version Android 4.4 (android:minSdkVersion="19" android:targetSdkVersion="19").
