package app.mobilebrainz.fastpizza.admin.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import app.mobilebrainz.fastpizza.admin.R;
import app.mobilebrainz.fastpizza.admin.adapter.BasketAdapter;
import app.mobilebrainz.fastpizza.admin.model.Basket;
import app.mobilebrainz.fastpizza.admin.model.Order;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;
import static app.mobilebrainz.fastpizza.admin.fragment.OrderFragmentDirections.toOrdersFragment;
import static app.mobilebrainz.fastpizza.admin.model.Schema.BASKET_COLLECTION;
import static app.mobilebrainz.fastpizza.admin.model.Schema.Basket.ORDER_FIELD;
import static app.mobilebrainz.fastpizza.admin.model.Schema.Basket.SIZE_FIELD;
import static app.mobilebrainz.fastpizza.admin.model.Schema.ORDERS_COLLECTION;
import static app.mobilebrainz.fastpizza.admin.model.Schema.USERS_COLLECTION;

/**
 * Фрагмент отображения данных заказа, списка пиц в заказе и кнопки удаления заказа.
 */
public class OrderFragment extends BaseFragment {

    private String orderId;
    private Order order;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration registration;
    private BasketAdapter basketAdapter;
    private List<DocumentSnapshot> basketDocs;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView phoneView;
    private TextView addressView;
    private TextView dateView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflate(R.layout.fragment_order, container);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        phoneView = view.findViewById(R.id.phoneView);
        addressView = view.findViewById(R.id.addressView);
        dateView = view.findViewById(R.id.dateView);

        swipeRefreshLayout.setEnabled(false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            OrderFragmentArgs args = OrderFragmentArgs.fromBundle(getArguments());
            orderId = args.getId();
            loadOrder();
        }
    }

    @Override
    @SuppressLint("RestrictedApi")
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.order_fragment_options_menu, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete && orderId != null) {
            deleteOrder();
            return true;
        }
        return false;
    }

    /**
     * Загрузить заказ из firestore сервер.
     */
    private void loadOrder() {
        swipeRefreshLayout.setRefreshing(true);
        registration = db.collection(ORDERS_COLLECTION).document(orderId)
                .addSnapshotListener((snapshot, error) -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (error != null) {
                        showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> loadOrder());
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        order = snapshot.toObject(Order.class);
                        initData();
                        setUpRecyclerView();
                    }
                });
    }

    private void initData() {
        phoneView.setText(getString(R.string.phone_view, order.getPhone()));
        addressView.setText(getString(R.string.address_view, order.getAddress()));

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        String strDate= formatter.format(order.getDate().toDate());
        dateView.setText(getString(R.string.date_view, strDate));
    }

    /**
     * Загрузить список пицц из firestore и передать в recyclerView
     */
    private void setUpRecyclerView() {
        swipeRefreshLayout.setRefreshing(true);
        Query query = getBasketCollecion(order.getUser())
                .whereEqualTo(ORDER_FIELD, orderId)
                .orderBy(SIZE_FIELD);

        query.addSnapshotListener((value, error) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (error != null) {
                showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> setUpRecyclerView());
            }
            if (value != null && !value.isEmpty()) {
                basketDocs = value.getDocuments();
            }
        });

        FirestoreRecyclerOptions<Basket> options = new FirestoreRecyclerOptions.Builder<Basket>()
                .setQuery(query, Basket.class)
                .build();

        basketAdapter = new BasketAdapter(options);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(basketAdapter);
        basketAdapter.startListening();
        initDeleteOnSwipe();
    }

    /**
     * Создать удаление пиццы из заказа свайпом влево или вправо.
     */
    private void initDeleteOnSwipe() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, LEFT | RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                basketAdapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }

    /**
     * Delete order and all baskets with orderId
     */
    private void deleteOrder() {
        WriteBatch batch = db.batch();
        // delete baskets with orderId
        if (basketDocs != null) {
            for (DocumentSnapshot doc : basketDocs) {
                DocumentReference basketDoc = getBasketCollecion(order.getUser()).document(doc.getId());
                batch.delete(basketDoc);
            }
        }
        // Delete order
        DocumentReference orderDoc = db.collection(ORDERS_COLLECTION).document(orderId);
        batch.delete(orderDoc);
        swipeRefreshLayout.setRefreshing(true);
        batch.commit()
                .addOnSuccessListener(docRef -> {
                    swipeRefreshLayout.setRefreshing(false);
                    navigate(toOrdersFragment());
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> deleteOrder());
                });
    }

    private CollectionReference getBasketCollecion(String userId) {
        return db.collection(USERS_COLLECTION).document(userId)
                .collection(BASKET_COLLECTION);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (basketAdapter != null) {
            basketAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        basketAdapter.stopListening();
        if (registration != null) {
            registration.remove();
        }
    }
}