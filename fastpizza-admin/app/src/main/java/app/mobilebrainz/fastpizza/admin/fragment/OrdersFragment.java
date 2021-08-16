package app.mobilebrainz.fastpizza.admin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import app.mobilebrainz.fastpizza.admin.R;
import app.mobilebrainz.fastpizza.admin.adapter.OrderAdapter;
import app.mobilebrainz.fastpizza.admin.model.Order;

import static app.mobilebrainz.fastpizza.admin.model.Schema.ORDERS_COLLECTION;
import static app.mobilebrainz.fastpizza.admin.model.Schema.Order.DATE_FIELD;

/**
 * Фрагмент списка заказов.
 */
public class OrdersFragment extends BaseFragment {

    private OrderAdapter orderAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflate(R.layout.fragment_orders, container);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpRecyclerView();
    }

    /**
     * Загрузить список заказов из firestore и передать в recyclerView.
     */
    private void setUpRecyclerView() {
        swipeRefreshLayout.setRefreshing(true);
        Query query = FirebaseFirestore.getInstance()
                .collection(ORDERS_COLLECTION)
                .orderBy(DATE_FIELD, Query.Direction.DESCENDING);

        query.addSnapshotListener((value, error) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (error != null) {
                showErrorSnackbar(R.string.error_app, R.string.snackbar_retry, v -> setUpRecyclerView());
            }
        });

        FirestoreRecyclerOptions<Order> options = new FirestoreRecyclerOptions.Builder<Order>()
                .setQuery(query, Order.class)
                .build();

        orderAdapter = new OrderAdapter(options);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(orderAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        orderAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        orderAdapter.stopListening();
    }
}