// Interface Segregation Principle (ISP)
// A class should not be forced to implement methods it does not use.
// Instead of one big interface, create smaller and focused interfaces.
// This keeps implementations simple and avoids unnecessary methods.

interface Printer {
    void print();
}

interface Scanner {
    void scan();
}

class SimplePrinter implements Printer {
    public void print() {
        System.out.println("Printing document...");
    }
}

class AllInOneMachine implements Printer, Scanner {
    public void print() {
        System.out.println("Printing document...");
    }

    public void scan() {
        System.out.println("Scanning document...");
    }
}

public class InterfaceSegregationExample {
    public static void main(String[] args) {
        Printer printer = new SimplePrinter(); // Only printer behavior is needed here
        printer.print();

        AllInOneMachine machine = new AllInOneMachine(); // Machine supports both actions
        machine.print();
        machine.scan();
    }
}