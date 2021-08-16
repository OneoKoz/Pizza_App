package app.mobilebrainz.fastpizza.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import static app.mobilebrainz.fastpizza.model.Schema.Order.ADDRESS_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.Order.DATE_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.Order.PHONE_FIELD;
import static app.mobilebrainz.fastpizza.model.Schema.Order.USER_FIELD;

/**
 * Модель Заказа
 */
public class Order {

    /**
     * id пользователя
     */
    private String user;

    /**
     * аддрес
     */
    private String address;

    /**
     *  телефон
     */
    private String phone;

    /**
     * дата и время
     */
    private Timestamp date;

    public Order() {

    }

    public Order(String user, String address, String phone, Timestamp date) {
        this.user = user;
        this.address = address;
        this.phone = phone;
        this.date = date;
    }

    @PropertyName(USER_FIELD)
    public String getUser() {
        return user;
    }

    @PropertyName(USER_FIELD)
    public void setUser(String user) {
        this.user = user;
    }

    @PropertyName(ADDRESS_FIELD)
    public String getAddress() {
        return address;
    }

    @PropertyName(ADDRESS_FIELD)
    public void setAddress(String address) {
        this.address = address;
    }

    @PropertyName(PHONE_FIELD)
    public String getPhone() {
        return phone;
    }

    @PropertyName(PHONE_FIELD)
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @PropertyName(DATE_FIELD)
    public Timestamp getDate() {
        return date;
    }

    @PropertyName(DATE_FIELD)
    public void setDate(Timestamp date) {
        this.date = date;
    }
}
