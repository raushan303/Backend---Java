// Open/Closed Principle (OCP)
// Code should be open for extension but closed for modification.
// That means we should be able to add new behavior without changing existing code.
// Here, new payment types can be added by implementing the PaymentMethod interface.

interface PaymentMethod {
    void pay(double amount);
}

class CreditCardPayment implements PaymentMethod {
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using Credit Card");
    }
}

class UpiPayment implements PaymentMethod {
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using UPI");
    }
}

class PaymentProcessor {
    public void processPayment(PaymentMethod paymentMethod, double amount) {
        paymentMethod.pay(amount); // Processor depends on abstraction, not concrete class
    }
}

public class OpenClosedExample {
    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor();

        processor.processPayment(new CreditCardPayment(), 500.0); // Existing code unchanged
        processor.processPayment(new UpiPayment(), 300.0);        // New type used without modifying processor
    }
}