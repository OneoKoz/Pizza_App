package app.mobilebrainz.fastpizza.util;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import app.mobilebrainz.fastpizza.R;

/**
 * Класс-хелпер для загрузки и показа изображений с помощью Glide-библиотеки.
 */
public class ImageViewGlide {

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
}
