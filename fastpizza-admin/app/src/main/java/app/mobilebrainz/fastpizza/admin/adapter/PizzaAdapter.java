package app.mobilebrainz.fastpizza.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import app.mobilebrainz.fastpizza.admin.App;
import app.mobilebrainz.fastpizza.admin.R;
import app.mobilebrainz.fastpizza.admin.model.Pizza;
import app.mobilebrainz.fastpizza.admin.model.Schema;
import app.mobilebrainz.fastpizza.admin.util.ImageViewGlide;

import static app.mobilebrainz.fastpizza.admin.fragment.PizzasFragmentDirections.toPizzaFragment;

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
    }

}