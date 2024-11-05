

import java.util.*;
import java.util.function.BiFunction;

/*
In real-world applications, especially in systems that require complex decision-making based on multiple conditions, developers often:

Use a Rule Engine: Utilize a dedicated rule engine like Drools, Jess, or Easy Rules that allows for declarative rule definitions, often in a human-readable format.

Adopt a Data-Driven Approach: Define rules as data rather than code, storing them in configuration files, databases, or using domain-specific languages (DSLs).

Implement a Predicate-Based System: Use predicates and expressions to evaluate conditions at runtime, reducing the need for numerous hardcoded rule classes.

Utilize Decision Tables or Trees: Represent rules in a tabular form where conditions and actions are mapped, simplifying the management of complex rule sets.
 */

// Condition.java
interface Condition {
    boolean evaluate(Order order);
}

// Action.java
interface Action {
    void execute(Order order);
}


/**
 * CONDITIONS
 **/

// IsPrimeCondition.java
class IsPrimeCondition implements Condition {
    @Override
    public boolean evaluate(Order order) {
        return order.getCustomer().isPrime();
    }
}

// TotalAmountCondition.java
class TotalAmountCondition implements Condition {
    private double threshold;

    TotalAmountCondition(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean evaluate(Order order) {
        return order.getTotalAmount() > threshold;
    }
}

// HasGroceryItemsCondition.java
class HasGroceryItemsCondition implements Condition {
    @Override
    public boolean evaluate(Order order) {
        return order.getItems().stream().anyMatch(Item::isGrocery);
    }
}

// CompositeCondition.java
class CompositeCondition implements Condition {
    private List<Condition> conditions;
    private BiFunction<Boolean, Boolean, Boolean> combiner;

    CompositeCondition(List<Condition> conditions, BiFunction<Boolean, Boolean, Boolean> combiner) {
        this.conditions = conditions;
        this.combiner = combiner;
    }

    @Override
    public boolean evaluate(Order order) {
        boolean result = conditions.get(0).evaluate(order);
        for (int i = 1; i < conditions.size(); i++) {
            result = combiner.apply(result, conditions.get(i).evaluate(order));
        }
        return result;
    }
}

// NegateCondition.java
class NegateCondition implements Condition {
    private Condition condition;

    NegateCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean evaluate(Order order) {
        return !condition.evaluate(order);
    }
}


/**
 * ACTIONS
 **/

// SetShippingOptionAction.java
class SetShippingOptionAction implements Action {
    private String shippingOption;

    SetShippingOptionAction(String shippingOption) {
        this.shippingOption = shippingOption;
    }

    @Override
    public void execute(Order order) {
        order.setShippingOption(shippingOption);
    }
}

// ApplyDiscountAction.java
class ApplyDiscountAction implements Action {
    private double discountPercentage;

    ApplyDiscountAction(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public void execute(Order order) {
        double discountAmount = order.getTotalAmount() * (discountPercentage / 100);
        order.applyDiscount(discountAmount);
    }
}

// Rule.java
class Rule2 {
    private Condition condition;
    private Action action;

    public Rule2(Condition condition, Action action) {
        this.condition = condition;
        this.action = action;
    }

    public void apply(Order order) {
        if (condition.evaluate(order)) {
            action.execute(order);
        }
    }
}

// RuleEngine.java
class RuleEngine2 {
    private List<Rule2> rules = new ArrayList<>();

    public void registerRule(Rule2 rule) {
        rules.add(rule);
    }

    public void applyRules(Order order) {
        for (Rule2 rule : rules) {
            rule.apply(order);
        }
    }
}

public class RuleEngine_2 {
    public static void main(String[] args) {
        // Initialize Rule Engine
        RuleEngine2 ruleEngine = new RuleEngine2();

        // Define Conditions
        Condition isPrimeCondition = new IsPrimeCondition();
        Condition totalAmountOver35 = new TotalAmountCondition(35);
        Condition totalAmountOver125 = new TotalAmountCondition(125);
        Condition hasGroceryItems = new HasGroceryItemsCondition();

        // Define Actions
        Action freeTwoDayShipping = new SetShippingOptionAction("Free 2-Day Shipping");
        Action freeOneDayShipping = new SetShippingOptionAction("Free 1-Day Shipping");
        Action freeTwoHourShipping = new SetShippingOptionAction("Free 2-Hour Shipping");

        // Define Composite Conditions
        Condition primeAndGroceryAndOver25 = new CompositeCondition(
                Arrays.asList(isPrimeCondition, hasGroceryItems, new TotalAmountCondition(25)),
                Boolean::logicalAnd
        );

        // Register Rules
        ruleEngine.registerRule(new Rule2(isPrimeCondition, freeTwoDayShipping));
        ruleEngine.registerRule(new Rule2(new CompositeCondition(
                Arrays.asList(new NegateCondition(isPrimeCondition), totalAmountOver35),
                Boolean::logicalAnd
        ), freeTwoDayShipping));
        ruleEngine.registerRule(new Rule2(totalAmountOver125, freeOneDayShipping));
        ruleEngine.registerRule(new Rule2(primeAndGroceryAndOver25, freeTwoHourShipping));

        // Create Order
        Customer customer = new Customer(true);
        List<Item> items = Arrays.asList(
                new Item(30.0, true, false),
                new Item(20.0, false, true),
                new Item(80.0, false, false)
        );
        Order order = new Order(customer, items);

        // Apply Rules
        ruleEngine.applyRules(order);

        // Output Results
        System.out.println("Total Amount: $" + order.getTotalAmount());
        System.out.println("Discount Applied: $" + order.getDiscount());
        System.out.println("Shipping Option: " + order.getShippingOption());
    }
}

/*
Output:
Total Amount: $130.0
Discount Applied: $0.0
Shipping Option: Free 2-Hour Shipping


[
    {
        "conditions": [
            {"type": "isPrime", "value": true}
        ],
        "action": {"type": "setShippingOption", "value": "Free 2-Day Shipping"}
    },
    {
        "conditions": [
            {"type": "isPrime", "value": false},
            {"type": "totalAmountOver", "value": 35}
        ],
        "action": {"type": "setShippingOption", "value": "Free 2-Day Shipping"}
    },
    {
        "conditions": [
            {"type": "totalAmountOver", "value": 125}
        ],
        "action": {"type": "setShippingOption", "value": "Free 1-Day Shipping"}
    },
    {
        "conditions": [
            {"type": "isPrime", "value": true},
            {"type": "hasGroceryItems", "value": true},
            {"type": "totalAmountOver", "value": 25}
        ],
        "action": {"type": "setShippingOption", "value": "Free 2-Hour Shipping"}
    }
]

 */