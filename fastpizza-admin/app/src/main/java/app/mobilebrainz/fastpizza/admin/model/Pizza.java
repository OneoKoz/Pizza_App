package app.mobilebrainz.fastpizza.admin.model;

import com.google.firebase.firestore.PropertyName;

import static app.mobilebrainz.fastpizza.admin.model.Schema.Pizza.CONSIST_FIELD;
import static app.mobilebrainz.fastpizza.admin.model.Schema.Pizza.IMAGE_FIELD;
import static app.mobilebrainz.fastpizza.admin.model.Schema.Pizza.NAME_FIELD;
import static app.mobilebrainz.fastpizza.admin.model.Schema.Pizza.PRICE_FIELD;

/**
 * Модель пиццы
 */
public class Pizza {

    /**
     * Название пиццы
     */
    private String name;

    /**
     * Состав пиццы
     */
    private String consist;

    /**
     * Цена пиццы
     */
    private int price;

    /**
     * Изображение пиццы (id из firestorage)
     */
    private String image;

    public Pizza() {

    }

    public Pizza(String name, String consist, int price, String image) {
        this.name = name;
        this.consist = consist;
        this.price = price;
        this.image = image;
    }

    @PropertyName(NAME_FIELD)
    public String getName() {
        return name;
    }

    @PropertyName(NAME_FIELD)
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName(CONSIST_FIELD)
    public String getConsist() {
        return consist;
    }

    @PropertyName(CONSIST_FIELD)
    public void setConsist(String consist) {
        this.consist = consist;
    }

    @PropertyName(PRICE_FIELD)
    public int getPrice() {
        return price;
    }

    @PropertyName(PRICE_FIELD)
    public void setPrice(int price) {
        this.price = price;
    }

    @PropertyName(IMAGE_FIELD)
    public String getImage() {
        return image;
    }

    @PropertyName(IMAGE_FIELD)
    public void setImage(String image) {
        this.image = image;
    }
}
