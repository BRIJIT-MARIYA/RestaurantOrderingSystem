import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        OrderManager manager = new OrderManager();

        System.out.println("====================================");
        System.out.println("     RESTAURANT ORDERING SYSTEM");
        System.out.println("====================================");

        // Customer details
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();

        System.out.print("Enter contact information: ");
        String contact = scanner.nextLine();

        Customer customer = new Customer(name, contact);

        Order currentOrder = null;

        int choice;

        do {

            System.out.println("\n========== MAIN MENU ==========");
            System.out.println("1. Browse Menu");
            System.out.println("2. Filter Menu");
            System.out.println("3. Place Order");
            System.out.println("4. Apply Discount");
            System.out.println("5. Finalize Order");
            System.out.println("6. Generate Bill");
            System.out.println("7. View Order History");
            System.out.println("8. View Discount Codes");
            System.out.println("9. Exit");
            System.out.println("===============================");

            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            try {

                switch (choice) {

                    case 1:
                        manager.displayMenu();
                        break;

                    case 2:

                        System.out.println("\nSelect Category:");
                        System.out.println("1. Starter");
                        System.out.println("2. Main Course");
                        System.out.println("3. Dessert");
                        System.out.println("4. Beverage");
                        System.out.println("5. Side");

                        System.out.print("Enter category: ");
                        int categoryChoice = scanner.nextInt();
                        scanner.nextLine();

                        MenuItem.Category category = null;

                        switch (categoryChoice) {
                            case 1:
                                category = MenuItem.Category.STARTER;
                                break;
                            case 2:
                                category = MenuItem.Category.MAIN_COURSE;
                                break;
                            case 3:
                                category = MenuItem.Category.DESSERT;
                                break;
                            case 4:
                                category = MenuItem.Category.BEVERAGE;
                                break;
                            case 5:
                                category = MenuItem.Category.SIDE;
                                break;
                            default:
                                System.out.println("Invalid category.");
                        }

                        if (category != null) {
                            manager.filterMenu(category);
                        }

                        break;

                    case 3:

                        if (currentOrder == null ||
                                currentOrder.getStatus() == Order.Status.FINALIZED) {

                            currentOrder = manager.createOrder(customer);
                        }

                        manager.displayMenu();

                        System.out.print(
                                "Enter item ID to add (or type DONE): ");

                        while (true) {

                            String itemId = scanner.nextLine();

                            if (itemId.equalsIgnoreCase("DONE")) {
                                break;
                            }

                            manager.addItemToOrder(currentOrder, itemId);

                            System.out.println("Item added successfully.");
                            System.out.print(
                                    "Enter another item ID (or DONE): ");
                        }

                        break;

                    case 4:

                        if (currentOrder == null) {
                            System.out.println("Please place an order first.");
                            break;
                        }

                        manager.displayDiscountCodes();

                        System.out.print("Enter discount code: ");
                        String code = scanner.nextLine();

                        if (manager.applyDiscount(currentOrder, code)) {
                            System.out.println("Discount applied successfully.");
                        } else {
                            System.out.println("Invalid discount code.");
                        }

                        break;

                    case 5:

                        if (currentOrder == null) {
                            System.out.println("Please place an order first.");
                            break;
                        }

                        manager.finalizeOrder(currentOrder);

                        System.out.println(
                                "Order finalized successfully.");

                        break;

                    case 6:

                        if (currentOrder == null) {
                            System.out.println("No order available.");
                            break;
                        }

                        manager.displayBill(currentOrder);
                        break;

                    case 7:

                        manager.displayOrderHistory(customer);
                        break;

                    case 8:

                        manager.displayDiscountCodes();
                        break;

                    case 9:

                        System.out.println(
                                "Thank you for using the Restaurant Ordering System!");
                        break;

                    default:

                        System.out.println("Invalid choice.");

                }

            } catch (Exception e) {

                System.out.println("Error: " + e.getMessage());
            }

        } while (choice != 9);

        scanner.close();
    }
}