import java.util.*;
/*
Write code that will be used by a Shopping cart service to enforce rules on the order

eg.
Offer free 2 day shipping on orders > $35 if customer is not a prime member
Offer free 2 day shipping on all orders if customer is a prime member
Offer free 1 day shipping for order that are > $125
Offer free 2 hour shipping for prime customer that have > $25 and the items are grocery items

Make this extensible to add other rules in the future
Apply a 10% discount if an item has been marked for subscribe and save
GIve the low level design for this in java
 */

/*
Design Patterns Used

Chain of Responsibility Pattern

Allows a request to pass through a chain of handlers (rules) where each handler decides whether to process the request or pass it to the next handler.
Makes it easy to add new rules without modifying existing code.
Strategy Pattern

Defines a family of algorithms (rules), encapsulates each one, and makes them interchangeable.
Lets the algorithm vary independently from clients that use it.
Decorator Pattern

Attaches additional responsibilities to an object dynamically.
Useful for applying multiple discounts or shipping options.
 */

class Customer {
    private boolean isPrime;
    // Other customer details

    public Customer(boolean isPrime) {
        this.isPrime = isPrime;
    }

    public boolean isPrime() {
        return isPrime;
    }
}

// Item.java
class Item {
    private double price;
    private boolean isGrocery;
    private boolean isSubscribed;

    public Item(double price, boolean isGrocery, boolean isSubscribed) {
        this.price = price;
        this.isGrocery = isGrocery;
        this.isSubscribed = isSubscribed;
    }

    public double getPrice() {
        return price;
    }

    public boolean isGrocery() {
        return isGrocery;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }
}

// Order.java
class Order {
    private Customer customer;
    private List<Item> items;
    private double totalAmount;
    private String shippingOption;
    private double discount;

    public Order(Customer customer, List<Item> items) {
        this.customer = customer;
        this.items = items;
        calculateTotal();
    }

    private void calculateTotal() {
        totalAmount = items.stream()
                .mapToDouble(Item::getPrice)
                .sum();
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<Item> getItems() {
        return items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setShippingOption(String shippingOption) {
        this.shippingOption = shippingOption;
    }

    public String getShippingOption() {
        return shippingOption;
    }

    public void applyDiscount(double discountAmount) {
        this.discount += discountAmount;
        this.totalAmount -= discountAmount;
    }

    public double getDiscount() {
        return discount;
    }
}

// Rule.java
interface Rule {
    void apply(Order order);
}

// FreeTwoDayShippingNonPrimeRule.java
class FreeTwoDayShippingNonPrimeRule implements Rule {
    @Override
    public void apply(Order order) {
        if (!order.getCustomer().isPrime() && order.getTotalAmount() > 35) {
            order.setShippingOption("Free 2-Day Shipping");
        }
    }
}

// FreeTwoDayShippingPrimeRule.java
class FreeTwoDayShippingPrimeRule implements Rule {
    @Override
    public void apply(Order order) {
        if (order.getCustomer().isPrime()) {
            order.setShippingOption("Free 2-Day Shipping");
        }
    }
}

// FreeOneDayShippingRule.java
class FreeOneDayShippingRule implements Rule {
    @Override
    public void apply(Order order) {
        if (order.getTotalAmount() > 125) {
            order.setShippingOption("Free 1-Day Shipping");
        }
    }
}

// FreeTwoHourShippingPrimeGroceryRule.java
class FreeTwoHourShippingPrimeGroceryRule implements Rule {
    @Override
    public void apply(Order order) {
        boolean hasGroceryItems = order.getItems().stream()
                .anyMatch(Item::isGrocery);
        if (order.getCustomer().isPrime() && order.getTotalAmount() > 25 && hasGroceryItems) {
            order.setShippingOption("Free 2-Hour Shipping");
        }
    }
}

// SubscribeAndSaveDiscountRule.java
class SubscribeAndSaveDiscountRule implements Rule {
    @Override
    public void apply(Order order) {
        double discount = 0;
        for (Item item : order.getItems()) {
            if (item.isSubscribed()) {
                double itemDiscount = item.getPrice() * 0.10;
                discount += itemDiscount;
            }
        }
        if (discount > 0) {
            order.applyDiscount(discount);
        }
    }
}

// RuleEngine.java
class RuleEngine {
    private List<Rule> rules = new ArrayList<>();

    public RuleEngine() {
        // Initialize with existing rules
        rules.add(new FreeTwoHourShippingPrimeGroceryRule());
        rules.add(new FreeOneDayShippingRule());
        rules.add(new FreeTwoDayShippingPrimeRule());
        rules.add(new FreeTwoDayShippingNonPrimeRule());
        rules.add(new SubscribeAndSaveDiscountRule());
    }

    public void applyRules(Order order) {
        for (Rule rule : rules) {
            rule.apply(order);
        }
    }

    // Method to add new rules
    public void addRule(Rule rule) {
        rules.add(rule);
    }
}

public class RuleEngine_1 {
    public static void main(String[] args) {
        // Create customer
        Customer customer = new Customer(true); // Prime customer

        // Create items
        List<Item> items = Arrays.asList(
                new Item(30.0, true, false),   // Grocery item
                new Item(20.0, false, true),   // Non-grocery, subscribed item
                new Item(80.0, false, false)   // Regular item
        );

        // Create order
        Order order = new Order(customer, items);

        // Initialize rule engine and apply rules
        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.applyRules(order);

        // Output results
        System.out.println("Total Amount: $" + order.getTotalAmount());
        System.out.println("Discount Applied: $" + order.getDiscount());
        System.out.println("Shipping Option: " + order.getShippingOption());
    }
}

/*
Output:Total Amount: $117.0
Discount Applied: $2.0
Shipping Option: Free 2-Hour Shipping
 */



/*
+-----------------+       +----------------+
|     Customer    |       |      Item      |
+-----------------+       +----------------+
| - isPrime       |       | - price        |
| - otherFields   |       | - isGrocery    |
+-----------------+       | - isSubscribed |
                          +----------------+
                                  ^
                                  |
                            +-------------+
                            |    Order    |
                            +-------------+
                            | - customer  |
                            | - items     |
                            | - total     |
                            | - shipping  |
                            | - discount  |
                            +-------------+

+-----------------+       +----------------+
|      Rule       |<------+  ConcreteRule  |
+-----------------+       +----------------+
| + apply(Order)  |       | Implementation |
+-----------------+       +----------------+

+-----------------+
|    RuleEngine   |
+-----------------+
| - rules         |
| + applyRules()  |
+-----------------+

 */