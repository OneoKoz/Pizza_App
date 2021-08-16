package app.mobilebrainz.fastpizza.admin;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;

import static app.mobilebrainz.fastpizza.admin.model.Schema.Firestorage.STORAGE_REF;


public class App extends Application {

    private static App instance;
    private static FirebaseStorage firestorage;

    private static Boolean firebaseInstanceInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
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

    public static App getInstance() {
        return instance;
    }

    public static FirebaseStorage getFirestorage() {
        return firestorage;
    }
}
