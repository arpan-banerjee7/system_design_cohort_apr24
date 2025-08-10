# Movie Seat Booking - No Lock Strategy Demo

This project demonstrates the **No Lock** database approach for handling concurrent seat bookings in a movie theater application.

## Overview

The demo simulates 200 users trying to book 200 seats simultaneously using the **No Lock** approach:

- **No Lock Strategy**: No database locking - demonstrates race conditions

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

### No Lock Strategy
- **Expected**: Race conditions, duplicate bookings, inconsistent results
- **What you'll see**: Multiple users might book the same seat, or some bookings might fail
- **Key Behavior**: Demonstrates concurrency issues without proper locking

## Key Learning Points

1. **Race Conditions**: Without proper locking, multiple transactions can interfere with each other
2. **Data Inconsistency**: Same seat can be booked by multiple users
3. **Lost Updates**: Updates can be overwritten by concurrent transactions
4. **Why Locking Matters**: Shows the importance of proper concurrency control

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

- **Reliability**: Low - race conditions and duplicate bookings
- **Performance**: Fastest - no locking overhead
- **Data Integrity**: Poor - inconsistent results
- **Use Case**: Educational demonstration of concurrency problems 