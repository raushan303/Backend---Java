// Liskov Substitution Principle (LSP)
// A child class should be replaceable with its parent class without breaking the program.
// If a class promises a behavior, every subtype should honor that behavior.
// Here, Bird does not force flying, because not all birds fly.

abstract class Bird {
    public abstract void move();
}

class Sparrow extends Bird {
    public void move() {
        System.out.println("Sparrow flies in the sky.");
    }
}

class Penguin extends Bird {
    public void move() {
        System.out.println("Penguin swims and walks on land.");
    }
}

public class LiskovSubstitutionExample {
    public static void makeBirdMove(Bird bird) {
        bird.move(); // Any Bird subtype should work here safely
    }

    public static void main(String[] args) {
        makeBirdMove(new Sparrow()); // Sparrow behaves as expected
        makeBirdMove(new Penguin()); // Penguin also behaves correctly
    }
}