LIBRARY MANAGEMENT SYSTEM (Java 17+)

Compile:  javac *.java
Run:      java Main

Librarian password: admin123
Member login:       any registered ID (samples: S001, S002, F001)
Data is saved in the ./data folder (books.txt, members.txt, transactions.txt, reports/).
Sample books and members are added automatically on first run.

Concept map
  Classes & Objects : Book, Transaction, Library, FileHandler ...
  Encapsulation     : private fields + getters/setters, controlled copy changes in Book
  Inheritance       : Person -> Member -> Student / Faculty ; Person -> Librarian
  Polymorphism      : overridden getRole(), displayInfo(), getBorrowLimit(), getLoanDays(), getFinePerDay();
                      overloaded Library.searchBooks(...)
  Collections       : LinkedHashMap (books, members), ArrayList (transactions), streams, Comparator
  Exception Handling: LibraryException hierarchy, try/catch, try-with-resources
  File Handling     : FileHandler (BufferedReader/Writer, Files, report export)
