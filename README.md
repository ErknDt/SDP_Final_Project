# E-Commerce Shopping Cart

We use six design patterns in a Java 17 console application for configurable laptops.
The application includes a cart, replaceable discounts, product notifications and
checkout through a simulated payment service.

## Build and run

Open `pom.xml` as a Maven project in IntelliJ IDEA and select JDK 17.
Run `kz.aitu.sdp.shop.app.ShopApplication.main` for the interactive menu.

From a terminal with JDK 17 and Maven available:

```sh
mvn verify
java -jar target/SDP_Final_Project-1.0-SNAPSHOT.jar
```

Run the reproducible purchase scenario:

```sh
java -jar target/SDP_Final_Project-1.0-SNAPSHOT.jar --demo
```

The build runs 49 JUnit tests. Production code uses the Java standard library.

## Patterns and team responsibilities

| Team member | Patterns | Main classes |
| --- | --- | --- |
| Allazhar Kuat | Strategy, Facade | `ShoppingCart`, discount strategies, `CheckoutFacade` |
| Akylzhan Khafiz | Builder, Decorator | `Laptop.Builder`, `Laptop`, product decorators |
| Yerkhan Dautbekov | Observer, Adapter | `ProductCatalog`, observers, `PaymentAdapter` |

Group SE-2515. These are the assigned areas for integration, review and explanation.

## Application behavior

- Configure RAM, SSD capacity and color using `Laptop.Builder`.
- Add gift wrapping for 3000.00 KZT and extended warranty for 25000.00 KZT.
- Add, remove and change the quantity of cart lines.
- Switch between no discount, 10 percent and a 50000 KZT discount from 700000 KZT.
- Change a catalog price or stock amount and receive immediate observer callbacks.
- Demonstrate payment approval, a declined payment and a successful retry.

The catalog starts with CAMPUS at 300000.00 KZT with five units and PRO at
500000.00 KZT with two units. RAM 16/32 GB adds 20000/50000 KZT. SSD 512/1024 GB
adds 15000/35000 KZT. The default 8 GB RAM and 256 GB SSD add zero.

The interactive menu also has a command to decline the next simulated payment.

## Shared contracts

All interfaces are in `kz.aitu.sdp.shop.contracts`.

| Interface | Purpose |
| --- | --- |
| `PurchasableItem` | Identifier, description and per-unit surcharge |
| `DiscountStrategy` | Discount amount calculated from the subtotal |
| `Catalog` | Current base prices, stock and product subscriptions |
| `ProductObserver` | Callback after a product change |
| `PaymentGateway` | Checkout-facing payment interface |

Configured laptops and decorators preserve the same catalog identifier. Stock is
tracked per base model. Checkout aggregates quantities when several lines share an
identifier. Each quote reads current catalog prices and then stores immutable order
lines, so an existing quote retains the prices used to create it.

Line subtotal = (catalog base price + configuration and service surcharges) * quantity.
Payable total = subtotal - the active strategy's discount amount.

Money uses `BigDecimal` in KZT, with two decimal places. Percentage results use
`RoundingMode.HALF_UP`. Prices, quantities and strategy arguments are validated.

## Checkout guarantees and scope

`CheckoutFacade` validates the complete cart and stock before calling payment.
A decline preserves cart contents and stock. Approval decreases stock, notifies
observers and clears the cart. Catalog stock changes validate all requested products
before updating any of them. Observer runtime exceptions are logged separately so
other subscribers and checkout can continue. Reentrant calls to the same facade are
rejected while checkout is in progress.

This version uses in-memory data, synchronous callbacks, one currency and a simulated
payment service. Application actions run sequentially on one thread. Supporting
concurrent customers and an external payment provider would require persistent
orders, stock reservations, transactional updates and payment idempotency.

## Project layout

- `product` contains Builder and Decorator.
- `discount`, `cart` and `checkout` contain Strategy, Facade and order snapshots.
- `catalog` and `payment` contain Observer and Adapter.
- `common/Money` centralizes monetary input validation.
- `app/ShopApplication` constructs the objects and handles console input/output.
- `src/test/java` contains seven test classes covering all six patterns and the menu.

## Verification and project materials

`docs/test-results.txt` summarizes the successful test run. `docs/demo-output.txt`
contains actual output from the runnable JAR.

- `docs/uml` contains three class diagrams and a purchase sequence diagram in
  PlantUML source, PNG and SVG formats.
- `docs/screenshots` contains screenshots of the running application.
- `docs/SDP_Final_Project_Report.pdf` contains the shared project report.

The submission consists of the PDF report and the source ZIP. The source ZIP includes
`pom.xml`, this README, Java code, tests and documentation.

## Working together

Use a personal branch for new changes and update it from `main`. Discuss changes to
shared interfaces before editing them. Run `mvn verify` before merging changes.
Each member should review their assigned patterns and contribute their own changes.

## References

- Course material: Final Project Defense Requirements, AITU.
- Eric Freeman et al., *Head First Design Patterns*, O'Reilly.
- Robert C. Martin, *Clean Code*, Chapters 16 and 17, Prentice Hall.
- [JUnit 5.12.2 user guide](https://docs.junit.org/5.12.2/user-guide/).
- [PlantUML class diagrams](https://plantuml.com/class-diagram).
