import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single customer order: a list of MenuItems, the
 * customer who placed it, an optional applied discount code, and
 * the resulting bill amount.
 *
 * Order is responsible for managing its own list of items (add/remove)
 * and computing totals. It does NOT know about the full menu or
 * about other orders - that is OrderManager's job. This separation
 * keeps each class focused on a single responsibility.
 */
public class Order {

    public enum Status {
        OPEN, FINALIZED
    }

    private static int nextOrderNumber = 1;

    private final String orderId;
    private final Customer customer;
    private final List<MenuItem> items;
    private DiscountCode appliedDiscount;
    private Status status;

    public Order(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Order must belong to a customer.");
        }
        this.orderId = "ORD-" + (nextOrderNumber++);
        this.customer = customer;
        this.items = new ArrayList<>();
        this.appliedDiscount = null;
        this.status = Status.OPEN;
    }

    public String getOrderId() {
        return orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Status getStatus() {
        return status;
    }

    /** Read-only view of the items in this order. */
    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Adds an item to the order. Availability is re-checked here as a
     * defensive safeguard even though OrderManager also checks it,
     * so Order can never end up holding an out-of-stock item.
     */
    public void addItem(MenuItem item) throws OutOfStockException {
        ensureOpen();
        if (!item.isAvailable()) {
            throw new OutOfStockException(item.getName() + " is currently out of stock.");
        }
        items.add(item);
    }

    public void removeItem(MenuItem item) {
        ensureOpen();
        items.remove(item);
    }

    public void applyDiscount(DiscountCode discountCode) {
        ensureOpen();
        this.appliedDiscount = discountCode;
    }

    public DiscountCode getAppliedDiscount() {
        return appliedDiscount;
    }

    public double calculateSubtotal() {
        double subtotal = 0.0;
        for (MenuItem item : items) {
            subtotal += item.getPrice();
        }
        return subtotal;
    }

    /** Applies the discount (if any) to the subtotal to produce the final bill. */
    public double calculateBill() {
        double subtotal = calculateSubtotal();
        if (appliedDiscount == null) {
            return subtotal;
        }
        double discountAmount = subtotal * (appliedDiscount.getPercentOff() / 100.0);
        return subtotal - discountAmount;
    }

    /** Locks the order so it can no longer be modified, and records it in the customer's history. */
    public void finalizeOrder() {
        ensureOpen();
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot finalize an order with no items.");
        }
        this.status = Status.FINALIZED;
        this.customer.addOrder(this);
    }

    private void ensureOpen() {
        if (status != Status.OPEN) {
            throw new IllegalStateException("Order " + orderId + " is already finalized and cannot be modified.");
        }
    }

    /** Produces a human-readable bill/receipt. */
    public String generateBillText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("            RESTAURANT BILL\n");
        sb.append("=========================================\n");
        sb.append("Order ID : ").append(orderId).append("\n");
        sb.append("Customer : ").append(customer.getName()).append("\n");
        sb.append("-----------------------------------------\n");
        for (MenuItem item : items) {
            sb.append(String.format("%-25s $%8.2f%n", item.getName(), item.getPrice()));
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("Subtotal:%20s$%8.2f%n", "", calculateSubtotal()));
        if (appliedDiscount != null) {
            sb.append(String.format("Discount (%s): %8s-%.1f%%%n",
                    appliedDiscount.getCode(), "", appliedDiscount.getPercentOff()));
        }
        sb.append(String.format("TOTAL:%23s$%8.2f%n", "", calculateBill()));
        sb.append("=========================================\n");
        return sb.toString();
    }
}
