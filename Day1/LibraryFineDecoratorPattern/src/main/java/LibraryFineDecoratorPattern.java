// Step 1: Fine interface for calculating fine
interface Fine {
    double calculateFine(int overdueDays);
}

// Step 2: Base fine calculation for the first n days
class BaseFine implements Fine {
    public static final int BASE_DAYS = 5;
    public static final double BASE_FINE = 10.0;

    @Override
    public double calculateFine(int overdueDays) {
        if (overdueDays <= BASE_DAYS) {
            return overdueDays * BASE_FINE;
        } else {
            return BASE_DAYS * BASE_FINE;
        }
    }
}

// Step 3: Decorator for additional fine after base period
class AdditionalFineDecorator implements Fine {
    protected Fine fine;
    private static final int DAYS_THRESHOLD = 15;
    private static final double FINE_AFTER_10_DAYS = 50.0;

    public AdditionalFineDecorator(Fine fine) {
        this.fine = fine;
    }

    @Override
    public double calculateFine(int overdueDays) {
        double baseFine = fine.calculateFine(overdueDays);
        int daysToCharge10 = 10;
        if (overdueDays <= DAYS_THRESHOLD) {
            daysToCharge10 = overdueDays - 5;
        }
        baseFine += daysToCharge10 * FINE_AFTER_10_DAYS;
        return baseFine;
    }
}

// New decorator for additional fine after 20 days
class AdditionalFineAfter20DaysDecorator implements Fine {

    private static final double FINE_AFTER_20_DAYS = 200.0;
    private Fine fine;

    public AdditionalFineAfter20DaysDecorator(Fine fine) {
        this.fine = fine;
    }

    @Override
    public double calculateFine(int overdueDays) {
        double baseFine = fine.calculateFine(overdueDays);
        if (overdueDays > 15) {
            baseFine += (overdueDays - 15) * FINE_AFTER_20_DAYS;
        }
        return baseFine;
    }
}


// Step 4: PaymentMethod interface
interface PaymentMethod {
    void collectPayment(double amount);
}

// Step 5: Cash payment method
class CashPayment implements PaymentMethod {
    @Override
    public void collectPayment(double amount) {
        System.out.println("Collecting " + amount + " via Cash.");
    }
}

// Step 6: UPI payment method
class UPIPayment implements PaymentMethod {
    @Override
    public void collectPayment(double amount) {
        System.out.println("Collecting " + amount + " via UPI.");
    }
}

// Step 7: Card payment method
class CardPayment implements PaymentMethod {
    @Override
    public void collectPayment(double amount) {
        System.out.println("Collecting " + amount + " via Card.");
    }
}

public class LibraryFineDecoratorPattern {
    public static void main(String[] args) {
        // Step 1: Create a Fine object with the base fine logic
        Fine fine = new BaseFine();

        // Step 2: Wrap it with AdditionalFineDecorator (for days 6-15)
        Fine fineWithAdditional = new AdditionalFineDecorator(fine);

        // Step 3: Now wrap it with the new AdditionalFineAfter20DaysDecorator
        Fine fineWithMoreAdditional = new AdditionalFineAfter20DaysDecorator(fineWithAdditional);

        // Step 4: Calculate fine for different overdue days
        int overdueDays1 = 18;
        double totalFine1 = fineWithMoreAdditional.calculateFine(overdueDays1);
        System.out.println("Total Fine for " + overdueDays1 + " overdue days: Rs " + totalFine1);

        // Collect fine using different payment methods
        PaymentMethod cashPayment = new CashPayment();
        cashPayment.collectPayment(totalFine1);
    }
}


