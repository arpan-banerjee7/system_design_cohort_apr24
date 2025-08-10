# Movie Seat Booking - FOR UPDATE SKIP LOCKED Strategy Demo

This project demonstrates the **FOR UPDATE SKIP LOCKED** database locking strategy for handling concurrent seat bookings in a movie theater application.

## Overview

The demo simulates 200 users trying to book 200 seats simultaneously using the **FOR UPDATE SKIP LOCKED** approach:

- **FOR UPDATE SKIP LOCKED Strategy**: Optimistic locking with `FOR UPDATE SKIP LOCKED` clause

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

### FOR UPDATE SKIP LOCKED Strategy
- **Expected**: Best performance, consistent results, no deadlocks
- **What you'll see**: All 200 seats booked successfully with fastest performance
- **Key Behavior**: Transactions skip already-locked rows and find available ones

## Key Learning Points

1. **Optimistic Locking**: `FOR UPDATE SKIP LOCKED` skips already-locked rows
2. **No Deadlocks**: Transactions never wait for locks, eliminating deadlock scenarios
3. **High Performance**: Fastest approach for high-concurrency scenarios
4. **Efficient Resource Usage**: No blocking, optimal throughput

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
- **Performance**: Excellent - no waiting for locks
- **Deadlock Risk**: None - transactions skip locked rows
- **Use Case**: High-concurrency scenarios where performance is critical 