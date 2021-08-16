package app.mobilebrainz.fastpizza.admin.model;

/**
 * Схема бд firestore
 */
public class Schema {

    /**
     * Схема бд firestorage
     */
    public static class Firestorage {
        public static final String STORAGE_REF = "gs://fastpizza-25ec1.appspot.com";
        public static final String IMAGE_ROOT_DIR = "images/";
        public static final String IMAGE_PIZZAS_DIR = "/pizzas/";
        public static final String JPG_EXT = ".jpg";

        public static String getPizzaImageRef(String imageName) {
            return IMAGE_ROOT_DIR + IMAGE_PIZZAS_DIR + imageName + JPG_EXT;
        }
    }

    public static final String USERS_COLLECTION = "users";
    public static final String PIZZAS_COLLECTION = "pizzas";
    public static final String ORDERS_COLLECTION = "orders";
    public static final String BASKET_COLLECTION = "basket";

    public static class Pizza {
        public static final String NAME_FIELD = "name";
        public static final String CONSIST_FIELD = "consist";
        public static final String PRICE_FIELD = "price";
        public static final String IMAGE_FIELD = "image";
    }

    public static class Order {
        public static final String USER_FIELD = "user";
        public static final String ADDRESS_FIELD = "address";
        public static final String PHONE_FIELD = "phone";
        public static final String DATE_FIELD = "date";
    }

    public static class Basket {
        public static final String PIZZA_FIELD = "pizza";
        public static final String ORDER_FIELD = "order";
        public static final String SIZE_FIELD = "size";
        public static final String COUNT_FIELD = "count";
    }

}
