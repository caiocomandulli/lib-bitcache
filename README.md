# BitCache - Android Library

BitCache was designed to manage image caching in android.

Making it easier for you to pre-load images and reuse them.

## Usage
 
Call the init method before using this library.
 
```java
 BitCache.init(context);
 ```
 
You create a BitCacheRequest<T> object to make a request to cache.

As in the example we request a Bitmap to use in a ImageView.

```java
  final ImageView imageView = (ImageView) findViewById(resource);
  BitCache.requestImage(requestPath, new BitCacheRequest<Bitmap>() {
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

The request path can be a HTTP request as "http://example.com/examples/image".

A android resource in APK as "android.resource://com.app.with.cache/R.drawable.example".

Or a file in the filesystem as "file:///tmp/android.txt". (Don't forget file permissions)

All responses are persisted in the cache folder. 

Next initialization the file will be already in cache, and whenever you make a request it will already be ready to use.

## Install Library

__Step 1.__ Get this code and compile it

__Step 2.__ Define a dependency within your project. For that, access to Properties > Android > Library and click on add and select the library

##  License

MIT License. See the file LICENSE.md with the full license text.

## Author

[![Caio Comandulli](https://avatars3.githubusercontent.com/u/3738961?v=3&s=150)](https://github.com/caiocomandulli "On Github")

Copyright (c) 2016 Caio Comandulli

## Third party libraries

This library uses REST Android Library by Caio Comandulli (myself), version 1.0. Copyright (C) 2016 Caio Comandulli. Licensed under MIT License.

## Compatibility

This Library is valid for Android systems from version Android 4.4 (android:minSdkVersion="19" android:targetSdkVersion="19").
