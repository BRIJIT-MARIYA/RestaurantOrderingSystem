/**
 * Represents a single item on the restaurant menu.
 * Encapsulates its own state (price, availability) and exposes
 * controlled behavior for changing that state.
 */
public class MenuItem {

    public enum Category {
        STARTER, MAIN_COURSE, DESSERT, BEVERAGE, SIDE
    }

    private final String id;
    private final String name;
    private double price;
    private final Category category;
    private boolean available;

    public MenuItem(String id, String name, double price, Category category, boolean available) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("MenuItem id cannot be empty.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("MenuItem price cannot be negative.");
        }
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("MenuItem price cannot be negative.");
        }
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s %-10s $%-6.2f %s",
                id, name, category, price, available ? "(available)" : "(OUT OF STOCK)");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuItem)) return false;
        return id.equals(((MenuItem) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
