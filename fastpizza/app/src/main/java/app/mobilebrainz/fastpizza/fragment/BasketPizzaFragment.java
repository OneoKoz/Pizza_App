package app.mobilebrainz.fastpizza.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import app.mobilebrainz.fastpizza.App;
import app.mobilebrainz.fastpizza.NavGraphDirections;
import app.mobilebrainz.fastpizza.R;
import app.mobilebrainz.fastpizza.model.Basket;
import app.mobilebrainz.fastpizza.model.Pizza;
import app.mobilebrainz.fastpizza.model.Schema;
import app.mobilebrainz.fastpizza.util.ImageViewGlide;

import static app.mobilebrainz.fastpizza.model.Schema.BASKET_COLLECTION;
import static app.mobilebrainz.fastpizza.model.Schema.PIZZAS_COLLECTION;
import static app.mobilebrainz.fastpizza.model.Schema.USERS_COLLECTION;

/**
 * Фрагмент описания пиццы, поля для ввода количества и размера пиц, кнопка добавить в корзину.
 */
public class BasketPizzaFragment extends BaseFragment {

    private String pizzaId;
    private Pizza pizza;
    private String basketId;
    private Basket basket;
    private int count = 1;
    private String userId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageView;
    private TextView nameView;
    private TextView consistView;
    private TextView priceView;
    private TextView countView;
    private TextView sizeView;
    private ImageButton plusBtn;
    private ImageButton minusBtn;
    private Button addToBasketBtn;
    private Button deleteFromBasketBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflate(R.layout.fragment_basket_pizza, container);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        nameView = view.findViewById(R.id.nameView);
        consistView = view.findViewById(R.id.consistView);
        priceView = view.findViewById(R.id.priceView);
        imageView = view.findViewById(R.id.imageView);
        countView = view.findViewById(R.id.countView);
        sizeView = view.findViewById(R.id.sizeView);
        plusBtn = view.findViewById(R.id.plusBtn);
        minusBtn = view.findViewById(R.id.minusBtn);
        addToBasketBtn = view.findViewById(R.id.addToBasketBtn);
        deleteFromBasketBtn = view.findViewById(R.id.deleteFromBasketBtn);

        initListeners();

        return view;
    }

    private void initListeners() {
        addToBasketBtn.setOnClickListener(v -> {
            if (userId != null && !swipeRefreshLayout.isRefreshing()) {
                update();
            }
        });

        deleteFromBasketBtn.setOnClickListener(v -> {
            if (userId != null && !swipeRefreshLayout.isRefreshing()) {
                deletePizza();
            }
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
            BasketPizzaFragmentArgs args = BasketPizzaFragmentArgs.fromBundle(getArguments());
            pizzaId = args.getPizzaid();
            basketId = args.getBasketid();
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
                    loadBasket();
                    showImage();
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> loadPizza());
                });
    }

    private void loadBasket() {
        swipeRefreshLayout.setRefreshing(true);
        getBasketCollecion().document(basketId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    swipeRefreshLayout.setRefreshing(false);
                    basket = documentSnapshot.toObject(Basket.class);
                    initFields();
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> loadBasket());
                });
    }

    private void showImage() {
        if (pizza != null && !pizza.getImage().isEmpty()) {
            String imageRef = Schema.Firestorage.getPizzaImageRef(pizza.getImage());
            ImageViewGlide.show(imageView, App.getFirestorage().getReference(imageRef));
        }
    }

    private void initFields() {
        nameView.setText(pizza.getName());
        consistView.setText(pizza.getConsist());
        count = basket.getCount();
        setCount();
        setPrice();

        int sizeRes;
        switch (basket.getSize()) {
            case 3:
                sizeRes = R.string.pizza_large_size;
                break;
            case 2:
                sizeRes = R.string.pizza_middle_size;
                break;
            default:
                sizeRes = R.string.pizza_small_size;
        }
        sizeView.setText(sizeRes);
    }

    private void setPrice() {
        if (pizza != null) {
            priceView.setText(getString(R.string.pizza_price, pizza.getPrice() * count * basket.getSize()));
        }
    }

    private void setCount() {
        countView.setText(String.valueOf(count));
    }

    private CollectionReference getBasketCollecion() {
        return db.collection(USERS_COLLECTION).document(userId)
                .collection(BASKET_COLLECTION);
    }

    /**
     * Обновить пиццу в корзине
     */
    private void update() {
        swipeRefreshLayout.setRefreshing(true);
        if (basketId != null) {
            basket.setCount(count);
            getBasketCollecion().document(basketId).set(basket)
                    .addOnSuccessListener(docRef -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showInfoSnackbar(R.string.info_basket_updated);
                    })
                    .addOnFailureListener(e -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> update());
                    });
        }
    }

    /**
     * Удалить пиццу из корзины
     */
    private void deletePizza() {
        if (basketId != null) {
            swipeRefreshLayout.setRefreshing(true);
            getBasketCollecion().document(basketId).delete()
                    .addOnSuccessListener(r -> {
                        swipeRefreshLayout.setRefreshing(false);
                        navigate(NavGraphDirections.toBasketFragment());
                    })
                    .addOnFailureListener(e -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> deletePizza());
                    });
        }
    }

}