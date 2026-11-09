# E-Commerce Shopping Cart

We are building a configurable laptop shop with six integrated design patterns.

## Setup

- Clone this repository and open `pom.xml` as a Maven project in IntelliJ IDEA.
- Select JDK 17 and reload the Maven project.
- With Maven available, run `mvn verify` from the project directory.

The current foundation contains the Maven configuration, package layout and
shared interfaces. The next milestone is implementing the three team modules.

## Team ownership

| Contributor | Patterns | Packages |
| --- | --- | --- |
| Allazhar | Strategy, Facade | `discount`, `cart`, `checkout` |
| Akylzhan | Builder, Decorator | `product`, `app` |
| Erhan | Observer, Adapter | `catalog`, `payment` |

All packages are under `kz.aitu.sdp.shop`. Shared interfaces are in `contracts`.
Tests belong under `src/test/java` using the same package names as their module.
Each contributor supplies their module's tests, UML and report sections.

## Shared contracts

| Interface | Implementation owner | Purpose |
| --- | --- | --- |
| `PurchasableItem` | Akylzhan | Configured product and service decorators |
| `DiscountStrategy` | Allazhar | Discount amount calculated from a subtotal |
| `Catalog` | Erhan | Product registration, prices, stock and subscriptions |
| `ProductObserver` | Erhan and subscribing clients | Product change callbacks |
| `PaymentGateway` | Erhan | Checkout-facing payment interface |

### Product identity and prices

- A configured laptop and its decorators keep the same catalog `productId`.
- Catalog stock is tracked per base model identifier.
- `getAdditionalPrice()` includes all configuration and service surcharges for
  one unit. Each decorator adds its surcharge to the wrapped item's surcharge.
- The catalog stores the current base price. Cart totals read current prices
  from `Catalog` whenever they are calculated.
- Line subtotal = (catalog unit price + item additional price) * quantity.
- Order subtotal is the sum of its line subtotals. The active strategy calculates
  a discount amount. Payable total = subtotal - discount amount.
- Money uses `BigDecimal` in KZT. Monetary amounts have at most two decimal
  places. Calculated monetary results use `RoundingMode.HALF_UP`.
- Item quantities are positive integers. When several cart lines share a product
  identifier, checkout sums their quantities before checking or decreasing stock.

### Catalog updates and observers

- `addProduct` registers each base model before it is used in the cart.
- `updatePrice` and `updateStock` notify observers after the state changes.
- Subscriptions are per product. A callback receives the identifier, updated
  base price and updated stock.
- Subscribing starts delivery of future changes. Initial values can be read
  with `getUnitPrice` and `getStock`.
- A failing observer is reported separately. It must allow other observers to
  receive the update and must not interrupt checkout after stock was committed.

### Checkout and payment

The initial application executes sequentially on one thread. Checkout validates
the complete cart, reads current prices and stock, calculates the payable total,
calls `PaymentGateway.pay`, and decreases stock after a successful payment.

A declined payment preserves cart contents and stock. A successful checkout
creates an order result and clears the purchased cart. Stock updates also notify
observers. The payment adapter connects the shared interface to a simulated
payment service with a different calling convention.

## Working together

1. Update local `main` before starting a task.
2. Create a personal branch from the shared foundation, for example
   `codex/allazhar-checkout`, `codex/akylzhan-products` or
   `codex/erhan-catalog-payment`.
3. Implement against the shared interfaces and keep changes in the owned packages.
4. Discuss shared interface and `pom.xml` changes before editing them.
5. Run the build and relevant tests before opening a pull request into `main`.
6. After a merge, update the other branches from `main`.

Package placeholder files named `.gitkeep` preserve empty directories in Git.

## Final deliverables

We will prepare one report with an introduction, implementation sections,
UML diagrams, application screenshots, Java code excerpts, conclusion and further
work. The Moodle deliverables are the PDF report and a ZIP of the source code.
Each member implements and explains their two patterns and their integration
with the rest of the application.
