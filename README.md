# Library Management System

A robust, console-based Library Management System implemented in **Java 17+**, illustrating core Object-Oriented Programming (OOP) design principles, file-based persistence, custom exception handling, and modern Java features.

---

## 📖 Overview

The Library Management System provides separate workflows for **Librarians** and **Members** (Students and Faculty). It handles complete book cataloging, member registration, borrowing/returning of books with loan period calculations and fine tracking, automated activity reporting, and persistent data storage.

---

## ✨ Features

### 🔐 Multi-Role Access Control
- **Librarian Portal**:
  - Secure login with password authentication.
  - Book catalog management: Add, update, delete, view, and search books.
  - Member management: Register new students and faculty, update details, or remove members.
  - Issue and return books with automated fine calculations.
  - View overdue books and accumulated fines.
  - Generate and export formatted library activity reports.
- **Member Portal (Students & Faculty)**:
  - Quick login using Member ID.
  - View available books and search by title, author, or category.
  - Check active borrowings, loan limits, due dates, and outstanding fines.
  - Self-service book return.

### 📚 Role-Based Borrowing Policies
| Role | Borrow Limit | Loan Duration | Fine per Day |
| :--- | :---: | :---: | :---: |
| **Student** | 3 books | 14 days | \$1.00 / day |
| **Faculty** | 10 books | 30 days | \$0.50 / day |

### 💾 Data Persistence
- Automatic file persistence in `./data/`:
  - `books.txt`: Inventory records.
  - `members.txt`: Member profiles.
  - `transactions.txt`: Active and completed book loan histories.
  - `reports/`: Exported summary and audit logs.
- Automatic initial seed data generation on first startup.

---

## 🏛️ OOP Concepts & Architecture

| Concept | Implementation in Project |
| :--- | :--- |
| **Classes & Objects** | Real-world entities modeled as `Book`, `Member`, `Student`, `Faculty`, `Librarian`, `Transaction`, and `Library`. |
| **Encapsulation** | Strict private fields with validated getters/setters and controlled copy adjustments in `Book`. |
| **Inheritance** | Hierarchical design: `Person` &rarr; `Member` &rarr; `Student` / `Faculty`; `Person` &rarr; `Librarian`. |
| **Polymorphism** | Method overriding for `getRole()`, `displayInfo()`, `getBorrowLimit()`, `getLoanDays()`, and `getFinePerDay()`; method overloading for `Library.searchBooks(...)`. |
| **Collections & Streams** | `LinkedHashMap` for deterministic order lookups, `ArrayList` for transaction records, Java Streams API for filtering, and `Comparator` for sorting. |
| **Exception Handling** | Custom hierarchy extending `LibraryException`: `BookUnavailableException`, `DuplicateRecordException`, `InvalidInputException`, `LimitExceededException`, `RecordNotFoundException`. |
| **File I/O** | `FileHandler` using `BufferedReader`/`BufferedWriter`, `Files` API, and try-with-resources for resource cleanup. |

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK) 17 or higher** installed.

### Compilation
Open a terminal in the project directory and compile all Java files:
```bash
javac *.java
```

### Running the Application
Launch the system with:
```bash
java Main
```

---

## 🔑 Default Credentials & Sample Accounts

On the first run, sample books and members are seeded automatically:

* **Librarian Account:**
  - Password: `admin123`
* **Sample Member IDs:**
  - Student: `S001`, `S002`
  - Faculty: `F001`

---

## 📁 Project Structure

```
├── Book.java
├── BookUnavailableException.java
├── DuplicateRecordException.java
├── Faculty.java
├── FileHandler.java
├── InputHelper.java
├── InvalidInputException.java
├── Librarian.java
├── Library.java
├── LibraryException.java
├── LimitExceededException.java
├── Main.java
├── Member.java
├── Person.java
├── RecordNotFoundException.java
├── ReportGenerator.java
├── Student.java
├── Transaction.java
├── LICENSE
├── README.md
└── .gitignore
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
