package app.mobilebrainz.fastpizza.admin.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import app.mobilebrainz.fastpizza.admin.App;
import app.mobilebrainz.fastpizza.admin.R;
import app.mobilebrainz.fastpizza.admin.activity.ImageChooserInterface;
import app.mobilebrainz.fastpizza.admin.model.Pizza;
import app.mobilebrainz.fastpizza.admin.model.Schema;
import app.mobilebrainz.fastpizza.admin.util.ImageViewGlide;

import static app.mobilebrainz.fastpizza.admin.model.Schema.PIZZAS_COLLECTION;
import static com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND;

/**
 * Фрагмент редактирования данных пиццы.
 */
public class EditPizzaFragment extends BaseFragment {

    private byte[] imageByteArray;

    private String id;
    private Pizza pizza;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageView;
    private Button imageDeleteBtn;
    private Button imageChooserBtn;
    private EditText nameView;
    private EditText consistView;
    private EditText priceView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflate(R.layout.fragment_edit_pizza, container);

        imageView = view.findViewById(R.id.imageView);
        imageChooserBtn = view.findViewById(R.id.imageChooserBtn);
        imageDeleteBtn = view.findViewById(R.id.imageDeleteBtn);
        nameView = view.findViewById(R.id.nameView);
        consistView = view.findViewById(R.id.consistView);
        priceView = view.findViewById(R.id.priceView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);

        Button submitBtn = view.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(v -> {
            if (!swipeRefreshLayout.isRefreshing()) {
                save();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            EditPizzaFragmentArgs args = EditPizzaFragmentArgs.fromBundle(getArguments());
            id = args.getId();
            load();

            imageChooserBtn.setOnClickListener(v -> {
                if (!swipeRefreshLayout.isRefreshing()) loadImage();
            });

            imageDeleteBtn.setOnClickListener(v -> {
                if (!swipeRefreshLayout.isRefreshing()) {
                    imageByteArray = null;
                    if (pizza != null && !pizza.getImage().isEmpty()) {
                        deleteImageFromStorage(false);
                    }
                }
            });
        }
    }

    @Override
    @SuppressLint("RestrictedApi")
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.submit_options_menu, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.submit) {
            if (!swipeRefreshLayout.isRefreshing()) {
                save();
            }
            return true;
        }
        return false;
    }

    private void load() {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore.getInstance().collection(PIZZAS_COLLECTION).document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    swipeRefreshLayout.setRefreshing(false);
                    pizza = documentSnapshot.toObject(Pizza.class);
                    initFields();
                    showImage();
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> load());
                });

    }

    private void initFields() {
        if (pizza != null) {
            nameView.setText(pizza.getName());
            consistView.setText(pizza.getConsist());
            priceView.setText(String.valueOf(pizza.getPrice()));
        }
    }

    private void showImage() {
        if (pizza != null && !pizza.getImage().isEmpty()) {
            String imageRef = Schema.Firestorage.getPizzaImageRef(pizza.getImage());
            ImageViewGlide.show(
                    imageView,
                    App.getFirestorage().getReference(imageRef),
                    () -> imageDeleteBtn.setVisibility(View.VISIBLE),
                    () -> imageDeleteBtn.setVisibility(View.GONE)
            );
        } else {
            imageDeleteBtn.setVisibility(View.GONE);
        }
    }

    private void save() {
        String name = nameView.getText().toString().trim();
        String consist = consistView.getText().toString().trim();
        String priceStr = priceView.getText().toString().trim();
        int price = 0;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException ignored) {
        }
        if (name.isEmpty()) {
            nameView.requestFocus();
            nameView.setError(getString(R.string.pizza_name_field_error));
        } else if (consist.isEmpty()) {
            consistView.requestFocus();
            consistView.setError(getString(R.string.pizza_consist_field_error));
        } else if (price == 0) {
            priceView.requestFocus();
            priceView.setError(getString(R.string.pizza_price_field_error));
        } else {
            if (pizza != null) {
                pizza.setName(name);
                pizza.setConsist(consist);
                pizza.setPrice(price);
                swipeRefreshLayout.setRefreshing(true);
                FirebaseFirestore.getInstance().collection(PIZZAS_COLLECTION).document(id).set(pizza)
                        .addOnSuccessListener(docRef -> {
                            swipeRefreshLayout.setRefreshing(false);
                            showInfoSnackbar(R.string.info_updated_pizza);
                            saveImageInStorage(pizza);
                        })
                        .addOnFailureListener(e -> {
                            swipeRefreshLayout.setRefreshing(false);
                            showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> save());
                        });
            }
        }
    }

    private void loadImage() {
        if (getActivity() instanceof ImageChooserInterface) {
            ImageChooserInterface imageChooser = (ImageChooserInterface) getActivity();
            imageChooser.showImageChooser(uri -> {
                ImageViewGlide.load(imageView, uri, byteArray -> {
                    imageByteArray = byteArray;
                    if (imageByteArray != null) {
                        if (!pizza.getImage().isEmpty()) {
                            deleteImageFromStorage(true);
                        } else {
                            pizza.setImage(String.valueOf(System.currentTimeMillis()));
                            save();
                        }
                    }
                }, () -> {
                    showInfoSnackbar(R.string.error_image_uploading);
                    imageByteArray = null;
                });
            });
        }
    }

    private void saveImageInStorage(Pizza pizza) {
        if (imageByteArray != null && !pizza.getImage().isEmpty()) {
            swipeRefreshLayout.setRefreshing(true);
            App.getFirestorage().getReference()
                    .child(Schema.Firestorage.getPizzaImageRef(pizza.getImage()))
                    .putBytes(imageByteArray)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageByteArray = null;
                        swipeRefreshLayout.setRefreshing(false);
                        imageDeleteBtn.setVisibility(View.VISIBLE);
                        showInfoSnackbar(R.string.info_image_uploaded);
                        //imageView.setImageResource(R.drawable.ic_image_24);
                        //
                    })
                    .addOnFailureListener(e -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showErrorSnackbar(R.string.error_image_uploading, R.string.snackbar_retry, v -> saveImageInStorage(pizza));
                    });
        }
    }

    private void deleteImageFromStorage(Boolean saveImage) {
        if (!pizza.getImage().isEmpty()) {
            swipeRefreshLayout.setRefreshing(true);
            App.getFirestorage().getReference()
                    .child(Schema.Firestorage.getPizzaImageRef(pizza.getImage()))
                    .delete()
                    .addOnSuccessListener(taskSnapshot -> {
                        swipeRefreshLayout.setRefreshing(false);
                        deleteImageFromFirestore(saveImage);
                    })
                    .addOnFailureListener(e -> {
                        swipeRefreshLayout.setRefreshing(false);
                        if (e instanceof StorageException) {
                            int errorCode = ((StorageException) e).getErrorCode();
                            if (errorCode == ERROR_OBJECT_NOT_FOUND) {
                                deleteImageFromFirestore(saveImage);
                            } else {
                                showInfoSnackbar(R.string.error_image_deleting);
                            }
                        }
                    });
        }
    }

    private void deleteImageFromFirestore(Boolean saveImage) {
        imageDeleteBtn.setVisibility((saveImage) ? View.VISIBLE : View.GONE);
        if (!saveImage) {
            imageView.setImageResource(R.drawable.ic_image_24);
            pizza.setImage("");
        } else {
            pizza.setImage(String.valueOf(System.currentTimeMillis()));
        }
        save();
    }

}