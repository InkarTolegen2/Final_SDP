package restaurant.facade;

import java.util.List;

public class OrderRequest {
    private final List<OrderItem> items;
    private final int table;
    private final String comment;
    private final String discount; 
    private final String promoCode;

    public OrderRequest(List<OrderItem> items, int table, String comment,
                        String discount, String promoCode) {
        this.items = items;
        this.table = table;
        this.comment = comment;
        this.discount = discount;
        this.promoCode = promoCode;
    }

    public List<OrderItem> getItems() { return items; }
    public int getTable() { return table; }
    public String getComment() { return comment; }
    public String getDiscount() { return discount; }
    public String getPromoCode() { return promoCode; }
}