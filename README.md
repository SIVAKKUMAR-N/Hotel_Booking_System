# Hotel Booking System

A beginner-friendly, console-based hotel booking application written with Core Java. It uses in-memory collections, so data is available only while the application is running. No database or external libraries are required.

## Requirements

- JDK 8 or later
- A terminal opened at the project folder

## Compile and run on Windows

```powershell
New-Item -ItemType Directory -Force out
javac -d out src\Main.java src\CLI\*.java src\enums\*.java src\Model\*.java src\payment\*.java src\Repository\*.java src\Service\*.java
java -cp out Main
```

The application starts with sample hotels and rooms in Bengaluru and Chennai. Use menu option **1** to register a guest, then search and create a booking. Dates must use `YYYY-MM-DD`.

## Main features

- Add hotels and typed rooms as an Admin or Hotel Manager; room numbers are unique within each hotel.
- Search by city, dates, and guest count; results exclude overlapping bookings and maintenance periods and are sorted by nightly room price.
- Create bookings for up to three rooms at one hotel. Pending payment holds rooms for 15 minutes.
- Pay with simulated UPI or card, or choose Pay at Hotel for stays up to two nights.
- Calculate nightly rates, weekend surcharge, and GST; support cancellation, date changes, check-in/out, hotel cancellation, and reviews.
- Keep hotels, guests, bookings, payments, and reviews in shared in-memory repositories for the full menu session.

## OOP concepts shown

- **Encapsulation:** private model fields with getters and setters.
- **Inheritance:** `Guest`, `Admin`, and `HotelManager` extend `User`; each room type extends `Room`.
- **Abstraction:** `Room` is abstract and `PaymentMethod` is an interface.
- **Polymorphism:** UPI, Card, and Pay at Hotel use the `PaymentMethod` interface.

Source files use the existing `CLI`, `enums`, `Model`, `payment`, `Repository`, and `Service` packages. `Main.java` stays in the default package as the entry point.
