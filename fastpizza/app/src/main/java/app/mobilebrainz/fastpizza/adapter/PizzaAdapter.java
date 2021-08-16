package app.mobilebrainz.fastpizza.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import app.mobilebrainz.fastpizza.App;
import app.mobilebrainz.fastpizza.R;
import app.mobilebrainz.fastpizza.model.Basket;
import app.mobilebrainz.fastpizza.model.Pizza;
import app.mobilebrainz.fastpizza.model.Schema;
import app.mobilebrainz.fastpizza.util.ImageViewGlide;

import static app.mobilebrainz.fastpizza.fragment.PizzasFragmentDirections.toPizzaFragment;
import static app.mobilebrainz.fastpizza.model.Schema.BASKET_COLLECTION;
import static app.mobilebrainz.fastpizza.model.Schema.Basket.ORDER_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.Basket.PIZZA_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.USERS_COLLECTION;

/**
 * Адаптер для заполнения ресайклера пиццами.
 */
public class PizzaAdapter extends FirestoreRecyclerAdapter<Pizza, PizzaAdapter.PizzaHolder> {


    public PizzaAdapter(@NonNull FirestoreRecyclerOptions<Pizza> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PizzaHolder holder, int position, @NonNull Pizza model) {
        holder.bindTo(model);
        String id = getSnapshots().getSnapshot(position).getId();
        holder.findBasket(id);
        holder.itemView.setOnClickListener(view ->
                Navigation.findNavController(view).navigate(toPizzaFragment(id))
        );
    }

    @NonNull
    @Override
    public PizzaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_pizza, parent, false);
        return new PizzaHolder(v);
    }

    /**
     * Контроллер ячейки ресайклера. Заполняется данными (пицами) из адаптера и отображает их в вьюхе.
     */
    static class PizzaHolder extends RecyclerView.ViewHolder {

        private ListenerRegistration basketsListener;

        private final ImageView basketImage;
        private final ImageView pizzaImage;
        private final TextView nameView;
        private final TextView consistView;
        private final TextView priceView;

        public PizzaHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.nameView);
            consistView = itemView.findViewById(R.id.consistView);
            priceView = itemView.findViewById(R.id.priceView);
            pizzaImage = itemView.findViewById(R.id.pizzaImage);
            basketImage = itemView.findViewById(R.id.basketImage);
        }

        public void bindTo(@NonNull Pizza pizza) {
            nameView.setText(pizza.getName());
            consistView.setText(pizza.getConsist());
            priceView.setText(itemView.getResources().getString(R.string.pizza_price, pizza.getPrice()));
            showImage(pizza.getImage());
        }

        private void showImage(String imageName) {
            if (imageName != null && !imageName.isEmpty()) {
                String imageRef = Schema.Firestorage.getPizzaImageRef(imageName);
                ImageViewGlide.show(pizzaImage, App.getFirestorage().getReference(imageRef));
            } else {
                pizzaImage.setImageResource(R.drawable.ic_image_24);
            }
        }

        /**
         * load basket, where basket.pizza == pizza.id and order == ""
         * показывать значок корзины над ценой
         */
        public void findBasket(String pizzaId) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                basketsListener = FirebaseFirestore.getInstance()
                        .collection(USERS_COLLECTION).document(userId)
                        .collection(BASKET_COLLECTION)
                        .whereEqualTo(PIZZA_FIELD, pizzaId)
                        .whereEqualTo(ORDER_FIELD, "")
                        .addSnapshotListener((value, error) -> {
                            if (value != null && !value.isEmpty()) {
                                basketImage.setVisibility(View.VISIBLE);
                            } else {
                                basketImage.setVisibility(View.GONE);
                            }
                        });
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (basketsListener != null) {
                basketsListener.remove();
            }
        }
    }
}