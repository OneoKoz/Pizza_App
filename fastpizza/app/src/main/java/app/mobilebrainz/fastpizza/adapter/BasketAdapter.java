package app.mobilebrainz.fastpizza.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import app.mobilebrainz.fastpizza.App;
import app.mobilebrainz.fastpizza.R;
import app.mobilebrainz.fastpizza.model.Basket;
import app.mobilebrainz.fastpizza.model.Pizza;
import app.mobilebrainz.fastpizza.model.Schema;
import app.mobilebrainz.fastpizza.util.ImageViewGlide;

import static app.mobilebrainz.fastpizza.fragment.BasketFragmentDirections.toBasketPizzaFragment;
import static app.mobilebrainz.fastpizza.model.Schema.PIZZAS_COLLECTION;

/**
 * Адаптер для заполнения ресайклера корзинами(пицами в общей корзине).
 */
public class BasketAdapter extends FirestoreRecyclerAdapter<Basket, BasketAdapter.BasketHolder> {

    public BasketAdapter(@NonNull FirestoreRecyclerOptions<Basket> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull BasketHolder holder, int position, @NonNull Basket model) {
        String basketid = getSnapshots().getSnapshot(position).getId();
        holder.bindTo(model, basketid);
    }

    @NonNull
    @Override
    public BasketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_basket, parent, false);
        return new BasketHolder(v);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    /**
     * Контроллер ячейки ресайклера. Заполняется данными (пицами в общей корзине) из адаптера и отображает их в вьюхе.
     */
    static class BasketHolder extends RecyclerView.ViewHolder {

        private Basket basket;
        private ListenerRegistration registration;

        private final ImageView pizzaImage;
        private final TextView sizeView;
        private final TextView countView;
        private final TextView pizzaView;
        private final TextView priceView;

        public BasketHolder(View itemView) {
            super(itemView);
            pizzaView = itemView.findViewById(R.id.pizzaView);
            sizeView = itemView.findViewById(R.id.sizeView);
            countView = itemView.findViewById(R.id.countView);
            pizzaImage = itemView.findViewById(R.id.pizzaImage);
            priceView = itemView.findViewById(R.id.priceView);
        }

        public void bindTo(@NonNull Basket basket, String basketId) {
            this.basket = basket;

            Resources res = itemView.getResources();
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
            sizeView.setText(res.getString(R.string.pizza_size, res.getString(sizeRes)));
            countView.setText(res.getString(R.string.pizza_count, basket.getCount()));

            itemView.setOnClickListener(view -> Navigation.findNavController(view).navigate(
                    toBasketPizzaFragment(basket.getPizza(), basketId))
            );
            loadPizza(basket.getPizza());
        }

        private void loadPizza(String pizzaId) {
            registration = FirebaseFirestore.getInstance()
                    .collection(PIZZAS_COLLECTION).document(pizzaId)
                    .addSnapshotListener((snapshot, error) -> {
                        if (error != null) {
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            Pizza pizza = snapshot.toObject(Pizza.class);
                            int price = basket.getCount() * basket.getSize() * pizza.getPrice();
                            pizzaView.setText(pizza.getName());
                            priceView.setText(itemView.getResources().getString(R.string.pizza_price, price));
                            showImage(pizza.getImage());
                        }
                    });
        }

        private void showImage(String imageName) {
            if (imageName != null && !imageName.isEmpty()) {
                String imageRef = Schema.Firestorage.getPizzaImageRef(imageName);
                ImageViewGlide.show(pizzaImage, App.getFirestorage().getReference(imageRef));
            } else {
                pizzaImage.setImageResource(R.drawable.ic_image_24);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (registration != null) {
                registration.remove();
            }
        }
    }
}