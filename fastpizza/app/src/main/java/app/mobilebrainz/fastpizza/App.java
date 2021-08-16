package app.mobilebrainz.fastpizza;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;

import static app.mobilebrainz.fastpizza.model.Schema.Firestorage.STORAGE_REF;


public class App extends Application {

    public static final String EMAIL = "fastpizza@gmail.com";
    public static final String CLIENT = "app.mobilebrainz.fastpizza";

    private static FirebaseStorage firestorage;
    private static Boolean firebaseInstanceInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        firestorage = FirebaseStorage.getInstance(STORAGE_REF);
        initFirebase();
    }

    private void initFirebase() {
        if (!FirebaseApp.getApps(this).isEmpty() && !firebaseInstanceInitialized) {
            firebaseInstanceInitialized = true;
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    // enable firebase caching
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
        }
    }

    public static FirebaseStorage getFirestorage() {
        return firestorage;
    }

}
