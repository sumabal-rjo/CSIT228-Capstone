# Smart Inventory Management System - F3 Group F

## Group Members
- Sasan, Jimsel Joel C.
- Sumabal, Romryl John O.
- Markle, Brendan Wayne S.
- Gustilo, Gian Carlo B.
- Bacus, Joshua B.

## Project Description
A Java desktop application that helps small businesses efficiently track and manage their product inventory. It solves the problem of manual, error-prone stock tracking by providing a centralized GUI-based system with product management, sales recording, low stock monitoring, and report generation.

## Proposed Features
- User login with role-based access (Admin / Staff)
- View and search inventory with filter support
- Manage products — Add, Edit, Delete
- Record sales and auto-update inventory
- View low stock alerts
- Generate and export inventory reports

## Planned Technologies
- Java
- JavaFX
- JDBC
- Database (SQLite)

## Evaluation Criteria Mapping (Initial)
- OOP: Classes include Product, Inventory, User, Transaction, Report, LowStockAlert, DatabaseConnection
- GUI: JavaFX with FXML — MainView.fxml, AddProductView.fxml
- UML: Use Case Diagram and Class Diagram included in /diagrams/
- Singleton pattern applied to DatabaseConnection
- Database: JDBC integration planned for full CRUD on products and transactions
- Multithreading: Planned for background stock monitoring via LowStockAlert