package app.mobilebrainz.fastpizza.admin;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

import androidx.annotation.NonNull;

/**
 *
 */
@GlideModule
public class FastpizzaGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(
                StorageReference.class,
                InputStream.class,
                new FirebaseImageLoader.Factory()
        );
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // todo: to settings
        int memoryCacheSizeBytes = 10 * 1024 * 1024; //10mb
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));

        // todo: to settings
        int diskCacheSizeBytes = 100 * 1024 * 1024; //100mb
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }
}
