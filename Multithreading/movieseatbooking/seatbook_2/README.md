# Movie Seat Booking - FOR UPDATE Strategy Demo

This project demonstrates the **FOR UPDATE** database locking strategy for handling concurrent seat bookings in a movie theater application.

## Overview

The demo simulates 200 users trying to book 200 seats simultaneously using the **FOR UPDATE** approach:

- **FOR UPDATE Strategy**: Pessimistic locking with `FOR UPDATE` clause

## Prerequisites

- MySQL server running locally
- Go 1.21 or later
- MySQL user with appropriate permissions

## Setup Instructions

### 1. Database Setup

First, run the database setup script:

```bash
mysql -u root -p < setup_database.sql
```

This will:
- Create a database called `movie_booking_demo`
- Create tables: `users`, `movies`, `seats`, `bookings`
- Insert 200 users, 1 movie, and 200 seats

### 2. Update Database Connection

Edit the `main.go` file and update the database connection string:

```go
const (
    dbDSN = "username:password@tcp(localhost:3306)/movie_booking_demo?parseTime=true"
)
```

Replace `username:password` with your MySQL credentials.

### 3. Install Dependencies

```bash
go mod tidy
```

### 4. Run the Demo

```bash
go run main.go
```

## Expected Results

### FOR UPDATE Strategy
- **Expected**: Consistent results, no duplicate bookings, but potential deadlocks
- **What you'll see**: All 200 seats booked successfully, but slower performance due to waiting
- **Key Behavior**: Transactions wait for locks to be released

## Key Learning Points

1. **Pessimistic Locking**: `FOR UPDATE` locks rows until transaction completion
2. **Transaction Management**: Proper transaction handling is crucial for data consistency
3. **Lock Waiting**: Transactions wait for locks, which can cause delays
4. **Deadlock Potential**: Multiple transactions waiting for each other can cause deadlocks

## Database Schema

### Users Table
- `id`: Primary key
- `username`: Unique username
- `email`: Unique email
- `created_at`: Timestamp

### Movies Table
- `id`: Primary key
- `title`: Movie title
- `duration_minutes`: Movie duration
- `release_date`: Release date
- `created_at`: Timestamp

### Seats Table
- `id`: Primary key
- `seat_number`: Seat number (1-200)
- `movie_id`: Foreign key to movies
- `is_booked`: Boolean flag
- `booked_by`: Foreign key to users (if booked)
- `booked_at`: Timestamp when booked
- `created_at`: Timestamp

### Bookings Table (Audit Trail)
- `id`: Primary key
- `user_id`: Foreign key to users
- `seat_id`: Foreign key to seats
- `movie_id`: Foreign key to movies
- `booking_time`: Timestamp

## Troubleshooting

1. **Connection Error**: Check your MySQL credentials and ensure the server is running
2. **Permission Error**: Ensure your MySQL user has CREATE, INSERT, UPDATE, DELETE permissions
3. **Import Error**: Run `go mod tidy` to download dependencies

## Performance Characteristics

- **Reliability**: High - prevents race conditions
- **Performance**: Medium - transactions wait for locks
- **Deadlock Risk**: Medium - can occur with complex transaction patterns
- **Use Case**: When data consistency is critical and some delay is acceptable 