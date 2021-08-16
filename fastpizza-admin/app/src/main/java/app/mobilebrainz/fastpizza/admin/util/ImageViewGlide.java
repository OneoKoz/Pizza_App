package app.mobilebrainz.fastpizza.admin.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import app.mobilebrainz.fastpizza.admin.R;

import static android.graphics.Bitmap.CompressFormat.JPEG;

/**
 * Класс-хелпер для загрузки и показа изображений с помощью Glide-библиотеки.
 */
public class ImageViewGlide {

    public interface ByteArrayConsumer {
        void consume(byte[] byteArray);
    }

    public interface Runnable {
        void run();
    }

    public static void show(@NonNull ImageView imageView, @NonNull StorageReference storageReference) {
        show(imageView, storageReference, null, null);
    }

    /**
     * Загружает изображение из firestorage по ссылке storageReference и передаёт для показа в imageView
     */
    public static void show(
            @NonNull ImageView imageView,
            @Nullable StorageReference storageReference,
            @Nullable Runnable onResourceReady,
            @Nullable Runnable onLoadFailed
    ) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(imageView.getContext());
        circularProgressDrawable.setStrokeWidth(5);
        circularProgressDrawable.setCenterRadius(30);
        circularProgressDrawable.start();

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(circularProgressDrawable)
                .error(R.drawable.ic_cloud_off_24)
                .fitCenter();

        RequestListener<Drawable> listener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (onLoadFailed != null) {
                    onLoadFailed.run();
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                if (onResourceReady != null) {
                    onResourceReady.run();
                }
                return false;
            }
        };

        Glide.with(imageView.getContext())
                .load(storageReference)
                .apply(requestOptions)
                .listener(listener)
                // when image (url) will be loaded by glide then this face in animation help to replace url image in the place of placeHolder (default) image.
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    /**
     * Загружает изображение по uri из галереи телефона и передаёт в imageView для показа.
     * После загрузки предоставляет доступ к byte[] byteArray изображения через лямбду ByteArrayConsumer onReady.
     */
    public static void load(
            @NonNull ImageView imageView,
            @Nullable Uri uri,
            @Nullable ByteArrayConsumer onReady,
            @Nullable Runnable onFailed
    ) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(imageView.getContext());
        circularProgressDrawable.setStrokeWidth(5);
        circularProgressDrawable.setCenterRadius(30);
        circularProgressDrawable.start();

        RequestOptions requestOptions = new RequestOptions()
                .override(250)
                .downsample(DownsampleStrategy.CENTER_INSIDE)
                //.skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(circularProgressDrawable)
                .error(R.drawable.ic_cloud_off_24);

        Glide.with(imageView)
                .asBitmap()
                .load(uri)
                .apply(requestOptions)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        imageView.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resource.compress(JPEG, 85, baos);
                        if (onReady != null) {
                            onReady.consume(baos.toByteArray());
                        }
                        imageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        imageView.setImageDrawable(errorDrawable);
                        if (onFailed != null) {
                            onFailed.run();
                        }
                    }
                });
    }
}
