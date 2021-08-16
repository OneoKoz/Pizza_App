package app.mobilebrainz.fastpizza.model;

import com.google.firebase.firestore.PropertyName;

import static app.mobilebrainz.fastpizza.model.Schema.Basket.COUNT_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.Basket.ORDER_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.Basket.PIZZA_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.Basket.SIZE_FIELD;

/**
 * Модель корзины (пиццы в корзине)
 */
public class Basket {

    /**
     * id пиццы
     */
    private String pizza;

    /**
     * id заказа, задаётся после отправки заказа этой корзины-пицы
     */
    private String order;

    /**
     * размер пиццы
     */
    private int size;

    /**
     * количество пицц
     */
    private int count;

    public Basket() {

    }

    public Basket(String pizza, int size, int count) {
        this.pizza = pizza;
        this.size = size;
        this.count = count;
        this.order = "";
    }

    @PropertyName(PIZZA_FIELD)
    public String getPizza() {
        return pizza;
    }

    @PropertyName(PIZZA_FIELD)
    public void setPizza(String pizza) {
        this.pizza = pizza;
    }

    @PropertyName(SIZE_FIELD)
    public int getSize() {
        return size;
    }

    @PropertyName(SIZE_FIELD)
    public void setSize(int size) {
        this.size = size;
    }

    @PropertyName(COUNT_FIELD)
    public int getCount() {
        return count;
    }

    @PropertyName(COUNT_FIELD)
    public void setCount(int count) {
        this.count = count;
    }

    @PropertyName(ORDER_FIELD)
    public String getOrder() {
        return order;
    }

    @PropertyName(ORDER_FIELD)
    public void setOrder(String order) {
        this.order = order;
    }
}
