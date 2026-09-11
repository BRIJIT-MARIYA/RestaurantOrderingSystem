import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a customer of the restaurant.
 * Maintains a private list of past orders (order history) that can
 * only be modified through addOrder(), keeping the class in control
 * of its own invariants (encapsulation).
 */
public class Customer {

    private final String name;
    private final String contactInfo;
    private final List<Order> orderHistory;

    public Customer(String name, String contactInfo) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
        this.name = name;
        this.contactInfo = contactInfo;
        this.orderHistory = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    /** Package-private/controlled entry point: only OrderManager calls this when an order is finalized. */
    void addOrder(Order order) {
        orderHistory.add(order);
    }

    /** Returns a read-only view so external code cannot mutate history directly. */
    public List<Order> getOrderHistory() {
        return Collections.unmodifiableList(orderHistory);
    }

    @Override
    public String toString() {
        return name + " (" + contactInfo + ")";
    }
}
