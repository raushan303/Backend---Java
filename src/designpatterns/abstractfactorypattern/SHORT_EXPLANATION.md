# Abstract Factory Pattern

## Interview description

The **Abstract Factory Pattern** provides an interface for creating families of related objects without exposing their concrete classes. Changing the factory changes the whole product family while the client code stays the same.

In this implementation:
- `product/` contains Chair and Sofa contracts with Modern and Victorian variants.
- `factory/` contains one factory interface and a factory for each furniture family.
- `example/` contains a store that works only with factory and product interfaces.

Use this pattern when products must be created together in compatible families, such as UI controls, database components, or cloud services.