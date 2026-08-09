// Dependency Inversion Principle (DIP)
// High-level modules should not depend on low-level modules.
// Both should depend on abstractions.
// This makes the code flexible, easier to test, and easier to extend.
// Here, NotificationService does not care whether messages are sent by email, SMS, or any other method.

interface NotificationSender {
    void send(String message);
}

class EmailSender implements NotificationSender {
    public void send(String message) {
        System.out.println("Sending email: " + message);
    }
}

class SmsSender implements NotificationSender {
    public void send(String message) {
        System.out.println("Sending SMS: " + message);
    }
}

class NotificationService {
    private final NotificationSender sender; // Depend on interface, so caller can choose the implementation

    public NotificationService(NotificationSender sender) {
        this.sender = sender; // The actual sender is injected from outside
    }

    public void notifyUser(String message) {
        sender.send(message); // Service uses abstraction, not concrete email/SMS classes
    }
}

public class DependencyInversionExample {
    public static void main(String[] args) {
        NotificationService emailService = new NotificationService(new EmailSender());
        // Here we choose EmailSender, but NotificationService stays unchanged
        emailService.notifyUser("Your order has been shipped.");

        NotificationService smsService = new NotificationService(new SmsSender());
        // Here we choose SmsSender, showing that implementation is flexible
        smsService.notifyUser("Your OTP is 123456.");
    }
}