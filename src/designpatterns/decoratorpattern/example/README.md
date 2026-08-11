# Real-World Order Pricing Example

This folder shows how the **Decorator Pattern** can be used in an e-commerce or ordering system where customers choose optional services at runtime.

## Files

- `OrderPricingDecoratorExample.java` - runnable example of an order enhanced with gift wrap, priority shipping, and purchase protection.

---

## Why decoration helps here

In real systems, every order does not need the same extra features.

Examples:

- some customers want only gift wrap
- some want gift wrap + fast delivery
- some want all options together

If we used inheritance alone, we would need many classes for every combination.

With decorators:

- start with `StandardOrder`
- wrap it with only the options selected for that order
- calculate final summary and total price dynamically

---

## Mapping to decorator concepts

- `Order` -> Component
- `StandardOrder` -> Concrete Component
- `OrderOptionDecorator` -> Abstract Decorator
- `GiftWrapDecorator`, `PriorityShippingDecorator`, `PurchaseProtectionDecorator` -> Concrete Decorators

---

## Class diagram for the order example

```text
                    +------------------+
                    |      Order       |
                    +------------------+
                    | +summary()       |
                    | +totalCost()     |
                    +--------^---------+
                             |
               +-------------+-------------+
               |                           |
      +-------------------+      +------------------------+
      |   StandardOrder   |      |  OrderOptionDecorator  |
      +-------------------+      +------------------------+
                                 | -order: Order          |
                                 +-----------^------------+
                                             |
             +-------------------------------+----------------------------------+
             |                               |                                  |
   +----------------------+       +---------------------------+      +-----------------------------+
   |  GiftWrapDecorator   |       | PriorityShippingDecorator |      | PurchaseProtectionDecorator |
   +----------------------+       +---------------------------+      +-----------------------------+
```

---

## Runtime flow

```text
1. Create a base order with base amount.
2. Choose optional services based on user request.
3. Wrap the base order with decorators.
4. Ask the final wrapped object for summary and total cost.
5. Final result includes all selected options without changing the base class.
```

This is a practical example of dynamic behavior composition in a business workflow.
