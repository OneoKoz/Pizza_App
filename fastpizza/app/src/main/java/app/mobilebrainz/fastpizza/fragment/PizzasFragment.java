package app.mobilebrainz.fastpizza.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import app.mobilebrainz.fastpizza.R;
import app.mobilebrainz.fastpizza.adapter.PizzaAdapter;
import app.mobilebrainz.fastpizza.model.Pizza;

import static app.mobilebrainz.fastpizza.model.Schema.PIZZAS_COLLECTION;
import static app.mobilebrainz.fastpizza.model.Schema.Pizza.NAME_FIELD;


/**
 * Фрагмент списка пицц.
 */
public class PizzasFragment extends BaseFragment {

    private PizzaAdapter pizzaAdapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflate(R.layout.fragment_pizzas, container);
        recyclerView = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpRecyclerView();
    }

    /**
     * Загрузить список пицц из firestore и передать в recyclerView
     */
    private void setUpRecyclerView() {
        Query query = FirebaseFirestore.getInstance()
                .collection(PIZZAS_COLLECTION)
                .orderBy(NAME_FIELD);

        FirestoreRecyclerOptions<Pizza> options = new FirestoreRecyclerOptions.Builder<Pizza>()
                .setQuery(query, Pizza.class)
                .build();

        pizzaAdapter = new PizzaAdapter(options);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(pizzaAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        pizzaAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        pizzaAdapter.stopListening();
    }
}