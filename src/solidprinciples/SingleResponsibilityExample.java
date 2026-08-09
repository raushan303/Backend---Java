// Single Responsibility Principle (SRP)
// A class should have only one job or one reason to change.
// Here, the Invoice class only stores invoice data.
// The calculation and printing logic are moved to separate classes.

class Invoice {
    private final String customerName;
    private final double amount;

    public Invoice(String customerName, double amount) {
        this.customerName = customerName;
        this.amount = amount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getAmount() {
        return amount;
    }
}

class InvoiceCalculator {
    public double calculateTax(Invoice invoice) {
        return invoice.getAmount() * 0.18; // Tax calculation is kept in one place
    }

    public double calculateTotal(Invoice invoice) {
        return invoice.getAmount() + calculateTax(invoice);
    }
}

class InvoicePrinter {
    public void print(Invoice invoice, double total) {
        System.out.println("Customer: " + invoice.getCustomerName());
        System.out.println("Amount: " + invoice.getAmount());
        System.out.println("Total with tax: " + total);
    }
}

public class SingleResponsibilityExample {
    public static void main(String[] args) {
        Invoice invoice = new Invoice("Alice", 1000.0); // Only invoice data is created here

        InvoiceCalculator calculator = new InvoiceCalculator();
        double total = calculator.calculateTotal(invoice); // Business logic handled separately

        InvoicePrinter printer = new InvoicePrinter();
        printer.print(invoice, total); // Printing logic handled separately
    }
}