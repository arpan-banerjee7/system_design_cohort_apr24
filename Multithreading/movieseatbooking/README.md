# Movie Seat Booking Concurrency Demo

This project demonstrates the effects of different database locking mechanisms on concurrent seat booking using Go and PostgreSQL.

## Locking Mechanisms in Relational Databases

Locking is a fundamental concept in RDBMS that ensures concurrent access to data doesn’t result in inconsistent or corrupt states. It’s crucial for **isolation**, which is one of the ACID properties. In SQL, these lock types are typically used with `SELECT ...` statements to control how rows are locked during a transaction.

### 1. `FOR SHARE`
- **Purpose:** Acquire a shared lock on the selected rows.
- **Behavior:**
  - Allows other transactions to also acquire shared locks on the same rows.
  - Prevents other transactions from modifying or deleting the rows.
- **Use Case:**
  ```sql
  SELECT * FROM orders WHERE status = 'pending' FOR SHARE;
  ```
- **Notes:** Supported in PostgreSQL. MySQL equivalent is `LOCK IN SHARE MODE` (deprecated).

### 2. `FOR UPDATE`
- **Purpose:** Acquire an exclusive lock on the selected rows.
- **Behavior:**
  - Prevents any other transaction from acquiring either a shared or exclusive lock on those rows.
  - Ensures that the rows can be safely updated by the current transaction.
- **Use Case:**
  ```sql
  SELECT * FROM accounts WHERE id = 101 FOR UPDATE;
  ```
- **Notes:** Supported in PostgreSQL, MySQL, Oracle.

### 3. `FOR UPDATE SKIP LOCKED`
- **Purpose:** Acquire an exclusive lock, but skip rows that are already locked.
- **Behavior:**
  - Doesn’t wait or block on locked rows.
  - Useful for task queue processing or distributed workers.
- **Use Case:**
  ```sql
  SELECT * FROM jobs WHERE status = 'pending' FOR UPDATE SKIP LOCKED;
  ```
- **Notes:** Supported in PostgreSQL 9.5+, Oracle 10g+, MySQL 8.0+.

### 4. `FOR UPDATE NOWAIT`
- **Purpose:** Acquire an exclusive lock, but fail immediately if the row is already locked.
- **Behavior:**
  - Instead of waiting, throws an error if a lock cannot be acquired.
  - Helps avoid long waits or deadlocks.
- **Use Case:**
  ```sql
  SELECT * FROM inventory WHERE product_id = 123 FOR UPDATE NOWAIT;
  ```
- **Notes:** Supported in PostgreSQL, Oracle.

## Summary Table

| Lock Type                | Prevents Other Reads | Prevents Other Writes | Skips Locked Rows | Waits on Lock |
|------------------------- |:-------------------:|:--------------------:|:----------------:|:-------------:|
| `FOR SHARE`              | ❌                  | ✅                   | ❌               | ✅            |
| `FOR UPDATE`             | ✅                  | ✅                   | ❌               | ✅            |
| `FOR UPDATE SKIP LOCKED` | ✅                  | ✅                   | ✅               | ❌            |
| `FOR UPDATE NOWAIT`      | ✅                  | ✅                   | ❌               | ❌ (fails)    |

## Practical Scenarios
- **Read-only transaction with consistency:** `FOR SHARE`
- **Ensuring record is not updated by others:** `FOR UPDATE`
- **Distributed queue or worker pool:** `FOR UPDATE SKIP LOCKED`
- **Optimistic concurrency, no blocking:** `FOR UPDATE NOWAIT`

---

## This Project: Movie Seat Booking

This project simulates 200 users trying to book 200 seats for a movie concurrently, using three different strategies:

1. **No Lock (Race Condition Demo):**
   - No explicit locking in SQL.
   - Demonstrates race conditions and data inconsistency.
   - Many users may book the same seat; not all seats are filled.

2. **FOR UPDATE (Pessimistic Locking):**
   - Uses `SELECT ... FOR UPDATE` to lock rows.
   - Ensures only one user can book a seat at a time (sequentially).
   - All seats are booked, but performance is slower due to waiting.

3. **FOR UPDATE SKIP LOCKED (Optimistic/Parallel Locking):**
   - Uses `SELECT ... FOR UPDATE SKIP LOCKED`.
   - Allows multiple users to book seats in parallel, skipping locked rows.
   - All seats are booked, and performance is much faster.

### How to Run
- Each strategy is implemented in its own directory:
  - `seatbook_1`: No Lock
  - `seatbook_2`: FOR UPDATE
  - `seatbook_3`: FOR UPDATE SKIP LOCKED
- Run each with `go run main.go` inside the respective directory.
- See the logs for booking order, race conditions, and performance metrics.

### What You’ll Learn
- The impact of different locking strategies on concurrency and data consistency
- How PostgreSQL’s MVCC and lock types affect real-world workloads
- Why correct locking is essential for reliable applications

---

**For more details, see the code and comments in each `main.go`.** 