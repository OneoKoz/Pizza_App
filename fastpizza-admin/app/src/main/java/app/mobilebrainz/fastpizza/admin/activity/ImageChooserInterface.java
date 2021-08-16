package app.mobilebrainz.fastpizza.admin.activity;

import android.net.Uri;

/**
 * Интерфейс для передачи выбранного изображения из галереи
 */
public interface ImageChooserInterface {

    /**
     * Лямбда интерфейс для потребления Uri uri изображения после возвращения в главную активность
     * после выбора изображения в галерее телефона
     */
    interface UriConsumer {
        void accept(Uri uri);
    }

    /**
     * В качестве аргутента служит Лямбда UriConsumer, которая будет вызвана  после возвращения в главную активность
     * после выбора изображения в галерее телефона
     */
    void showImageChooser(UriConsumer response);

}
