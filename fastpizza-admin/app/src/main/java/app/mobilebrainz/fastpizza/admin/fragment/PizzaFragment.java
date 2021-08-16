package app.mobilebrainz.fastpizza.admin.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.StorageException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import app.mobilebrainz.fastpizza.admin.App;
import app.mobilebrainz.fastpizza.admin.R;
import app.mobilebrainz.fastpizza.admin.model.Pizza;
import app.mobilebrainz.fastpizza.admin.model.Schema;
import app.mobilebrainz.fastpizza.admin.util.ImageViewGlide;

import static app.mobilebrainz.fastpizza.admin.fragment.PizzaFragmentDirections.toEditPizzaFragment;
import static app.mobilebrainz.fastpizza.admin.fragment.PizzaFragmentDirections.toPizzasFragment;
import static app.mobilebrainz.fastpizza.admin.model.Schema.PIZZAS_COLLECTION;
import static com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND;

/**
 * Фрагмент вывода данных пиццы.
 */
public class PizzaFragment extends BaseFragment {

    private String id;
    private Pizza pizza;
    private ListenerRegistration registration;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageView;
    private TextView nameView;
    private TextView consistView;
    private TextView priceView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflate(R.layout.fragment_pizza, container);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        nameView = view.findViewById(R.id.nameView);
        consistView = view.findViewById(R.id.consistView);
        priceView = view.findViewById(R.id.priceView);
        imageView = view.findViewById(R.id.imageView);

        swipeRefreshLayout.setEnabled(false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            PizzaFragmentArgs args = PizzaFragmentArgs.fromBundle(getArguments());
            id = args.getId();
            load();
        }
    }

    @Override
    @SuppressLint("RestrictedApi")
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.pizza_fragment_options_menu, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!swipeRefreshLayout.isRefreshing()) {
            switch (item.getItemId()) {
                case R.id.edit:
                    navigate(toEditPizzaFragment(id));
                    return true;
                case R.id.delete:
                    new AlertDialog.Builder(requireContext())
                            .setMessage(R.string.dialog_delete_pizza)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteImageFromStorage())
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            }).create().show();
                    return true;
            }
        }
        return false;
    }

    private void deleteFromFirestore() {
        swipeRefreshLayout.setRefreshing(true);
        db.collection(PIZZAS_COLLECTION).document(id).delete()
                .addOnSuccessListener(r -> {
                    swipeRefreshLayout.setRefreshing(false);
                    navigate(toPizzasFragment());
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> deleteFromFirestore());
                });
    }

    private void load() {
        swipeRefreshLayout.setRefreshing(true);
        registration = db.collection(PIZZAS_COLLECTION).document(id)
                .addSnapshotListener((snapshot, error) -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (error != null) {
                        showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> load());
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        pizza = snapshot.toObject(Pizza.class);
                        showData();
                        showImage();
                    }
                });
    }

    private void showData() {
        if (pizza != null) {
            nameView.setText(pizza.getName());
            consistView.setText(pizza.getConsist());
            priceView.setText(getString(R.string.pizza_price, pizza.getPrice()));
        }
    }

    private void showImage() {
        if (pizza != null && !pizza.getImage().isEmpty()) {
            String imageRef = Schema.Firestorage.getPizzaImageRef(pizza.getImage());
            ImageViewGlide.show(imageView, App.getFirestorage().getReference(imageRef));
        }
    }

    private void deleteImageFromStorage() {
        if (pizza != null)
            if (!pizza.getImage().isEmpty()) {
                swipeRefreshLayout.setRefreshing(true);
                App.getFirestorage().getReference()
                        .child(Schema.Firestorage.getPizzaImageRef(pizza.getImage()))
                        .delete()
                        .addOnSuccessListener(taskSnapshot -> {
                            swipeRefreshLayout.setRefreshing(false);
                            deleteFromFirestore();
                        })
                        .addOnFailureListener(e -> {
                            swipeRefreshLayout.setRefreshing(false);
                            if (e instanceof StorageException) {
                                int errorCode = ((StorageException) e).getErrorCode();
                                if (errorCode == ERROR_OBJECT_NOT_FOUND) {
                                    deleteFromFirestore();
                                } else {
                                    showInfoSnackbar(R.string.error_image_deleting);
                                }
                            }
                        });
            } else {
                deleteFromFirestore();
            }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (registration != null) {
            registration.remove();
        }
    }

}