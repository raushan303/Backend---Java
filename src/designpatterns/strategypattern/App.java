package designpatterns.strategypattern;

public class App {
    private PaymentStrategy paymentStrategy;

    public App(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout(int amount) {
        paymentStrategy.pay(amount);
    }

    public static void main(String[] args) {
        App app1 = new App(new CardPayment());
        app1.checkout(500);

        App app2 = new App(new UpiPayment());
        app2.checkout(300);

        App app3 = new App(new CashPayment());
        app3.checkout(100);
    }
}