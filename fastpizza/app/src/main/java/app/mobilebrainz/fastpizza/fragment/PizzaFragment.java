package app.mobilebrainz.fastpizza.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import app.mobilebrainz.fastpizza.App;
import app.mobilebrainz.fastpizza.R;
import app.mobilebrainz.fastpizza.model.Basket;
import app.mobilebrainz.fastpizza.model.Pizza;
import app.mobilebrainz.fastpizza.model.Schema;
import app.mobilebrainz.fastpizza.util.ImageViewGlide;

import static app.mobilebrainz.fastpizza.model.Schema.BASKET_COLLECTION;
import static app.mobilebrainz.fastpizza.model.Schema.Basket.ORDER_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.Basket.PIZZA_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.Basket.SIZE_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.PIZZAS_COLLECTION;
import static app.mobilebrainz.fastpizza.model.Schema.USERS_COLLECTION;

/**
 * Фрагмент описания пиццы, поля для ввода количества и размера пиц, кнопка добавить в корзину.
 */
public class PizzaFragment extends BaseFragment {

    private String pizzaId;
    private Pizza pizza;
    private int size = 1;
    private int count = 1;
    private String userId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageView;
    private TextView nameView;
    private TextView consistView;
    private TextView priceView;
    private TextView countView;
    private ImageButton plusBtn;
    private ImageButton minusBtn;
    private ChipGroup sizeChipGroup;
    private Button addToBasketBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflate(R.layout.fragment_pizza, container);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        nameView = view.findViewById(R.id.nameView);
        consistView = view.findViewById(R.id.consistView);
        priceView = view.findViewById(R.id.priceView);
        imageView = view.findViewById(R.id.imageView);
        countView = view.findViewById(R.id.countView);
        plusBtn = view.findViewById(R.id.plusBtn);
        minusBtn = view.findViewById(R.id.minusBtn);
        addToBasketBtn = view.findViewById(R.id.addToBasketBtn);
        sizeChipGroup = view.findViewById(R.id.sizeChipGroup);

        initListeners();

        return view;
    }

    private void initListeners() {
        addToBasketBtn.setOnClickListener(v -> {
            if (userId != null && !swipeRefreshLayout.isRefreshing()) {
                loadBasket();
            }
        });

        sizeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.middleSizeChip) {
                size = 2;
            } else if (checkedId == R.id.largeSizeChip) {
                size = 3;
            } else {
                size = 1;
            }
            setPrice();
        });

        plusBtn.setOnClickListener(v -> {
            count++;
            setCount();
            setPrice();
        });

        minusBtn.setOnClickListener(v -> {
            count--;
            if (count < 1) count = 1;
            setCount();
            setPrice();
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            PizzaFragmentArgs args = PizzaFragmentArgs.fromBundle(getArguments());
            pizzaId = args.getId();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userId = user.getUid();
                loadPizza();
            }
        }
    }

    private void loadPizza() {
        swipeRefreshLayout.setRefreshing(true);
        db.collection(PIZZAS_COLLECTION).document(pizzaId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    swipeRefreshLayout.setRefreshing(false);
                    pizza = documentSnapshot.toObject(Pizza.class);
                    initFields();
                    showImage();
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> loadPizza());
                });
    }

    private void showImage() {
        if (pizza != null && !pizza.getImage().isEmpty()) {
            String imageRef = Schema.Firestorage.getPizzaImageRef(pizza.getImage());
            ImageViewGlide.show(imageView, App.getFirestorage().getReference(imageRef));
        }
    }

    private void initFields() {
        if (pizza != null) {
            nameView.setText(pizza.getName());
            consistView.setText(pizza.getConsist());
            setCount();
            setPrice();
            setSizeChip(size);
        }
    }

    private void setPrice() {
        if (pizza != null) {
            priceView.setText(getString(R.string.pizza_price, pizza.getPrice() * count * size));
        }
    }

    private void setCount() {
        countView.setText(String.valueOf(count));
    }

    private void setSizeChip(int size) {
        switch (size) {
            case 1:
                sizeChipGroup.check(R.id.smallSizeChip);
                break;
            case 2:
                sizeChipGroup.check(R.id.middleSizeChip);
                break;
            case 3:
                sizeChipGroup.check(R.id.largeSizeChip);
                break;
        }
    }

    /**
     * Проверить есть ли уже в корзине пицца pizzaId с заданным размером size.
     * Если нет - добавить пиццу в корзину, если есть - обновить пиццу в корзине с добавлением введённого
     * количества пиц.
     */
    private void loadBasket() {
        swipeRefreshLayout.setRefreshing(true);
        getBasketCollecion()
                .whereEqualTo(PIZZA_FIELD, pizzaId)
                .whereEqualTo(SIZE_FIELD, size)
                .whereEqualTo(ORDER_FIELD, "")
                .get()
                .addOnSuccessListener(snapshots -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (snapshots.isEmpty()) {
                        Basket basket = new Basket(pizzaId, size, count);
                        addToBasket(basket);
                    } else {
                        Basket basket = snapshots.toObjects(Basket.class).get(0);
                        String basketId = snapshots.getDocuments().get(0).getId();
                        basket.setCount(basket.getCount() + count);
                        updateBasket(basketId, basket);
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> loadBasket());
                });
    }

    /**
     * Сохранить пиццу в корзине
     */
    private void addToBasket(Basket basket) {
        swipeRefreshLayout.setRefreshing(true);
        getBasketCollecion().add(basket)
                .addOnSuccessListener(docRef -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showInfoSnackbar(R.string.info_added_to_basket);
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> addToBasket(basket));
                });
    }

    /**
     * Обновить пиццу в корзине
     */
    private void updateBasket(String basketId, Basket basket) {
        swipeRefreshLayout.setRefreshing(true);
            getBasketCollecion().document(basketId).set(basket)
                    .addOnSuccessListener(docRef -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showInfoSnackbar(R.string.info_basket_updated);
                    })
                    .addOnFailureListener(e -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> updateBasket(basketId, basket));
                    });
    }

    private CollectionReference getBasketCollecion() {
        return db.collection(USERS_COLLECTION).document(userId)
                .collection(BASKET_COLLECTION);
    }

}