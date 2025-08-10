package main

import (
	"database/sql"
	"fmt"
	"log"
	"sync"
	"time"

	_ "github.com/lib/pq"
)

const (
	dbDSN = "host=localhost port=5432 user=arpan.banerjee dbname=movie_booking_demo sslmode=disable"
)

type BookingResult struct {
	UserID   int
	SeatID   int
	SeatName string
	Success  bool
	Error    string
	Duration time.Duration
}

// No Lock Strategy - Race condition prone
func bookSeat(db *sql.DB, userID int) BookingResult {
	start := time.Now()

	// Find an available seat (no locking - race condition!)
	var seatID int
	var seatName string
	var movieID int
	var currentUserID sql.NullInt64

	err := db.QueryRow("SELECT id, name, movie_id, user_id from seats where movie_id = $1 and user_id IS null ORDER BY id LIMIT 1", 1).Scan(&seatID, &seatName, &movieID, &currentUserID)
	if err != nil {
		return BookingResult{
			UserID:   userID,
			Success:  false,
			Error:    fmt.Sprintf("No available seats: %v", err),
			Duration: time.Since(start),
		}
	}

	// Try to book the seat (no locking - race condition!)
	result, err := db.Exec("UPDATE seats set user_id = $1 where id = $2", userID, seatID)
	if err != nil {
		return BookingResult{
			UserID:   userID,
			SeatID:   seatID,
			SeatName: seatName,
			Success:  false,
			Error:    fmt.Sprintf("Failed to book seat: %v", err),
			Duration: time.Since(start),
		}
	}

	rowsAffected, _ := result.RowsAffected()
	if rowsAffected == 0 {
		return BookingResult{
			UserID:   userID,
			SeatID:   seatID,
			SeatName: seatName,
			Success:  false,
			Error:    "Seat was already booked by another user",
			Duration: time.Since(start),
		}
	}

	// Insert into bookings table for audit
	_, err = db.Exec("INSERT INTO bookings (user_id, seat_id, movie_id) VALUES ($1, $2, $3)", userID, seatID, 1)
	if err != nil {
		log.Printf("Warning: Failed to insert booking record: %v", err)
	}

	return BookingResult{
		UserID:   userID,
		SeatID:   seatID,
		SeatName: seatName,
		Success:  true,
		Duration: time.Since(start),
	}
}

func resetDatabase(db *sql.DB) error {
	log.Println("Resetting database...")

	// Reset all seats to unbooked
	_, err := db.Exec("UPDATE seats SET user_id = NULL WHERE movie_id = $1", 1)
	if err != nil {
		return fmt.Errorf("failed to reset seats: %v", err)
	}

	// Clear bookings table
	_, err = db.Exec("DELETE FROM bookings WHERE movie_id = $1", 1)
	if err != nil {
		return fmt.Errorf("failed to clear bookings: %v", err)
	}

	log.Println("Database reset completed")
	return nil
}

func runBookingTest(db *sql.DB, numUsers int) {
	log.Printf("\n=== Testing No Lock Strategy ===\n")

	// Reset database before test
	err := resetDatabase(db)
	if err != nil {
		log.Printf("Failed to reset database: %v", err)
		return
	}

	log.Printf("Simulating %d users", numUsers)

	var wg sync.WaitGroup
	wg.Add(numUsers)

	// Launch concurrent booking requests
	for i := 1; i <= numUsers; i++ {
		go func(userID int) {
			defer wg.Done()

			start := time.Now()
			result := bookSeat(db, userID)
			duration := time.Since(start)

			// Log directly in goroutine
			if result.Success {
				log.Printf("✅ User%03d was assigned the seat %s (took %v)", result.UserID, result.SeatName, duration)
			} else {
				log.Printf("❌ User%03d could not be assigned a seat: %s", result.UserID, result.Error)
			}
		}(i)
	}

	// Wait for all goroutines to complete
	wg.Wait()

	// Query database to get total seats booked
	var totalBooked int
	err = db.QueryRow("SELECT COUNT(*) FROM seats WHERE movie_id = $1 AND user_id IS NOT NULL", 1).Scan(&totalBooked)
	if err != nil {
		log.Printf("Error counting booked seats: %v", err)
	} else {
		log.Println()
		log.Printf("📊 Total seats booked: %d", totalBooked)
	}

	// Display final seat layout
	displayFinalSeatLayout(db)
}

func displayFinalSeatLayout(db *sql.DB) {
	fmt.Println("\n🎬 Final Seat Layout:")
	fmt.Println("   (Dots = Empty, X = Booked)")

	// Get all seats ordered by ID
	rows, err := db.Query("SELECT id, name, user_id FROM seats WHERE movie_id = $1 ORDER BY id", 1)
	if err != nil {
		fmt.Printf("Error fetching seats: %v\n", err)
		return
	}
	defer rows.Close()

	seatCount := 0
	for rows.Next() {
		var seatID int
		var seatName string
		var userID sql.NullInt64
		if err := rows.Scan(&seatID, &seatName, &userID); err != nil {
			continue
		}

		// Print seat status
		if userID.Valid {
			fmt.Print(" X")
		} else {
			fmt.Print(" .")
		}

		seatCount++

		// New line every 20 seats for better readability
		if seatCount%20 == 0 {
			fmt.Println()
		}
	}
	fmt.Println()
}

func main() {
	log.Println("🎬 Movie Seat Booking - No Lock Strategy Demo")
	log.Println("=============================================")

	// Connect to database
	db, err := sql.Open("postgres", dbDSN)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}
	defer db.Close()

	// Test connection
	err = db.Ping()
	if err != nil {
		log.Fatalf("Failed to ping database: %v", err)
	}
	log.Println("✅ Connected to PostgreSQL database")

	// Get user count
	var userCount int
	err = db.QueryRow("SELECT COUNT(*) FROM users").Scan(&userCount)
	if err != nil {
		log.Fatalf("Failed to get user count: %v", err)
	}

	log.Printf("📊 Database setup: %d users, 200 seats, 1 movie", userCount)

	// Run the No Lock test
	runBookingTest(db, 200)

	log.Println("\nKey Takeaways:")
	log.Println("• No Lock: Shows race conditions and duplicate bookings")
	log.Println("• Multiple users can book the same seat")
	log.Println("• Fastest but unreliable - demonstrates concurrency issues")
}
