import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    private final List<MenuItem> menu;
    private final List<Order> orders;
    private final List<DiscountCode> discountCodes;

    public OrderManager() {
        menu = new ArrayList<>();
        orders = new ArrayList<>();
        discountCodes = new ArrayList<>();

        loadMenu();
        loadDiscountCodes();
    }

    // Add sample menu items
    private void loadMenu() {
        menu.add(new MenuItem("M01", "French Fries", 80,
                MenuItem.Category.STARTER, true));

        menu.add(new MenuItem("M02", "Chicken Wings", 150,
                MenuItem.Category.STARTER, true));

        menu.add(new MenuItem("M03", "Chicken Biriyani", 180,
                MenuItem.Category.MAIN_COURSE, true));

        menu.add(new MenuItem("M04", "Veg Fried Rice", 140,
                MenuItem.Category.MAIN_COURSE, true));

        menu.add(new MenuItem("M05", "Chocolate Cake", 100,
                MenuItem.Category.DESSERT, true));

        menu.add(new MenuItem("M06", "Ice Cream", 70,
                MenuItem.Category.DESSERT, true));

        menu.add(new MenuItem("M07", "Fresh Lime", 50,
                MenuItem.Category.BEVERAGE, true));
    }

    // Add discount codes
    private void loadDiscountCodes() {
        discountCodes.add(new DiscountCode("SAVE10", 10));
        discountCodes.add(new DiscountCode("SAVE20", 20));
    }

    // Display only available menu items
    public void displayMenu() {
        System.out.println("\n========== MENU ==========");

        for (MenuItem item : menu) {
            if (item.isAvailable()) {
                System.out.println(item);
            }
        }

        System.out.println("==========================");
    }

    // Filter menu according to category
    public void filterMenu(MenuItem.Category category) {
        System.out.println("\n===== " + category + " =====");

        boolean found = false;

        for (MenuItem item : menu) {
            if (item.isAvailable() && item.getCategory() == category) {
                System.out.println(item);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No available items in this category.");
        }
    }

    // Find menu item using ID
    public MenuItem findMenuItem(String id) {
        for (MenuItem item : menu) {
            if (item.getId().equalsIgnoreCase(id)) {
                return item;
            }
        }

        return null;
    }

    // Create a new order
    public Order createOrder(Customer customer) {
        Order order = new Order(customer);
        orders.add(order);
        return order;
    }

    // Add item to an order
    public void addItemToOrder(Order order, String itemId)
            throws OutOfStockException {

        MenuItem item = findMenuItem(itemId);

        if (item == null) {
            throw new IllegalArgumentException("Item not found.");
        }

        if (!item.isAvailable()) {
            throw new OutOfStockException(
                    item.getName() + " is currently out of stock.");
        }

        order.addItem(item);
    }

    // Apply discount code
    public boolean applyDiscount(Order order, String code) {

        for (DiscountCode discount : discountCodes) {

            if (discount.getCode().equalsIgnoreCase(code)) {
                order.applyDiscount(discount);
                return true;
            }
        }

        return false;
    }

    // Finalize an order
    public void finalizeOrder(Order order) {
        order.finalizeOrder();
    }

    // Display bill
    public void displayBill(Order order) {
        System.out.println(order.generateBillText());
    }

    // Display customer order history
    public void displayOrderHistory(Customer customer) {

        System.out.println("\n===== ORDER HISTORY =====");

        if (customer.getOrderHistory().isEmpty()) {
            System.out.println("No previous orders.");
            return;
        }

        for (Order order : customer.getOrderHistory()) {
            System.out.println(
                    order.getOrderId() +
                    " - Total: $" +
                    String.format("%.2f", order.calculateBill())
            );
        }

        System.out.println("=========================");
    }

    // Display available discount codes
    public void displayDiscountCodes() {

        System.out.println("\n===== DISCOUNT CODES =====");

        for (DiscountCode discount : discountCodes) {
            System.out.println(discount);
        }
    }
}