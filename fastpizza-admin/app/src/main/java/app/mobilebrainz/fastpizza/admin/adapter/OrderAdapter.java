package app.mobilebrainz.fastpizza.admin.adapter;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import app.mobilebrainz.fastpizza.admin.R;
import app.mobilebrainz.fastpizza.admin.model.Order;

import static app.mobilebrainz.fastpizza.admin.fragment.OrdersFragmentDirections.toOrderFragment;

/**
 * Адаптер для заполнения ресайклера заказами.
 */
public class OrderAdapter extends FirestoreRecyclerAdapter<Order, OrderAdapter.OrderHolder> {

    public OrderAdapter(@NonNull FirestoreRecyclerOptions<Order> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull OrderHolder holder, int position, @NonNull Order model) {
        holder.bindTo(model);
        String id = getSnapshots().getSnapshot(position).getId();
        holder.itemView.setOnClickListener(view ->
                Navigation.findNavController(view).navigate(toOrderFragment(id))
        );
    }

    @NonNull
    @Override
    public OrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_order, parent, false);
        return new OrderHolder(v);
    }

    /**
     * Контроллер ячейки ресайклера. Заполняется данными (заказами) из адаптера и отображает их в вьюхе.
     */
    static class OrderHolder extends RecyclerView.ViewHolder {

        private final TextView dateView;
        private final TextView addressView;
        private final TextView phoneView;

        public OrderHolder(View itemView) {
            super(itemView);
            addressView = itemView.findViewById(R.id.addressView);
            phoneView = itemView.findViewById(R.id.phoneView);
            dateView = itemView.findViewById(R.id.dateView);
        }

        public void bindTo(@NonNull Order order) {
            Resources res = itemView.getResources();

            phoneView.setText(res.getString(R.string.phone_view, order.getPhone()));
            addressView.setText(res.getString(R.string.address_view, order.getAddress()));

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            String strDate = formatter.format(order.getDate().toDate());
            dateView.setText(res.getString(R.string.date_view, strDate));
        }
    }
}