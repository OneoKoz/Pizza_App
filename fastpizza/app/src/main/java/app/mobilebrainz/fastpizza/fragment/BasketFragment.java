package app.mobilebrainz.fastpizza.fragment;

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

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.tfcporciuncula.phonemoji.PhonemojiTextInputEditText;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import app.mobilebrainz.fastpizza.R;
import app.mobilebrainz.fastpizza.adapter.BasketAdapter;
import app.mobilebrainz.fastpizza.model.Basket;
import app.mobilebrainz.fastpizza.model.Order;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;
import static androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback;
import static app.mobilebrainz.fastpizza.model.Schema.BASKET_COLLECTION;
import static app.mobilebrainz.fastpizza.model.Schema.Basket.ORDER_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.ORDERS_COLLECTION;
import static app.mobilebrainz.fastpizza.model.Schema.USERS_COLLECTION;


/**
 * Фрагмент корзины пицц с полями для отправки заказа.
 */
public class BasketFragment extends BaseFragment {

    private String userId;
    private BasketAdapter basketAdapter;
    private List<DocumentSnapshot> basketDocs;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private CardView submitLayout;
    private PhonemojiTextInputEditText phoneView;
    private EditText addressView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflate(R.layout.fragment_basket, container);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        submitLayout = view.findViewById(R.id.submitLayout);
        phoneView = view.findViewById(R.id.phoneView);
        addressView = view.findViewById(R.id.addressView);

        swipeRefreshLayout.setEnabled(false);

        Button submitOrderBtn = view.findViewById(R.id.submitOrderBtn);
        submitOrderBtn.setOnClickListener(v -> {
            if (userId != null && basketDocs != null && !basketDocs.isEmpty()) {
                submit();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            setUpRecyclerView();
        }
    }

    @Override
    @SuppressLint("RestrictedApi")
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.basket_fragment_options_menu, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (userId != null && item.getItemId() == R.id.delete) {
            delete();
            return true;
        }
        return false;
    }

    /**
     * Загрузить список пицц(корзин) из firestore и передать в recyclerView
     */
    private void setUpRecyclerView() {
        swipeRefreshLayout.setRefreshing(true);
        Query query = getBasketCollecion(userId).whereEqualTo(ORDER_FIELD, "");

        query.addSnapshotListener((value, error) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (error != null) {
                showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> setUpRecyclerView());
            }
            if (value != null && !value.isEmpty()) {
                submitLayout.setVisibility(View.VISIBLE);
                basketDocs = value.getDocuments();
            } else {
                submitLayout.setVisibility(View.GONE);
            }
        });

        FirestoreRecyclerOptions<Basket> options = new FirestoreRecyclerOptions.Builder<Basket>()
                .setQuery(query, Basket.class)
                .build();

        basketAdapter = new BasketAdapter(options);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(basketAdapter);
        initDeleteOnSwipe();
    }

    /**
     * Создать удаление пиццы из корзины свайпом влево или вправо.
     */
    private void initDeleteOnSwipe() {
        new ItemTouchHelper(new SimpleCallback(0, LEFT | RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, @NonNull ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull ViewHolder viewHolder, int direction) {
                basketAdapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();
        basketAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        basketAdapter.stopListening();
    }

    private CollectionReference getBasketCollecion(String userId) {
        return db.collection(USERS_COLLECTION).document(userId)
                .collection(BASKET_COLLECTION);
    }

    /**
     * Отправить заказ на сервер
     */
    private void submit() {
        String phone = phoneView.getText().toString().trim();
        String address = addressView.getText().toString().trim();

        if (phone.isEmpty()) {
            phoneView.requestFocus();
            phoneView.setError(getString(R.string.phone_field_error));
        } else if (address.isEmpty()) {
            addressView.requestFocus();
            addressView.setError(getString(R.string.address_field_error));
        } else {
            Order order = new Order(userId, address, phone, Timestamp.now());
            batchOrder(order);
        }
    }

    /**
     * Сохранить заказ и обновить корзины с id заказа в firestore
     */
    private void batchOrder(Order order) {
        WriteBatch batch = db.batch();
        DocumentReference orderDoc = db.collection(ORDERS_COLLECTION).document();
        // Create order
        batch.set(orderDoc, order);
        // update baskets with orderId
        if (basketDocs != null) {
            for (DocumentSnapshot doc : basketDocs) {
                DocumentReference basketDoc = getBasketCollecion(userId).document(doc.getId());
                batch.update(basketDoc, ORDER_FIELD, orderDoc.getId());
            }
        }
        swipeRefreshLayout.setRefreshing(true);
        batch.commit()
                .addOnSuccessListener(docRef -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showInfoSnackbar(R.string.info_added_order);
                    basketDocs = null;
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> submit());
                });
    }

    /**
     * Очистить корзину
     */
    private void delete() {
        if (basketDocs != null) {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : basketDocs) {
                DocumentReference basketDoc = getBasketCollecion(userId).document(doc.getId());
                batch.delete(basketDoc);
            }
            batch.commit()
                    .addOnSuccessListener(docRef -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showInfoSnackbar(R.string.info_basket_deleted);
                    })
                    .addOnFailureListener(e -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> submit());
                    });
        }
    }
}