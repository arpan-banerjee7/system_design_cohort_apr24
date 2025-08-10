package main

import (
	"database/sql"
	"fmt"
	"log"

	_ "github.com/lib/pq"
)

func main() {
	// Connect to PostgreSQL server (create database if not exists)
	// Note: You need to create the database manually first: createdb movie_booking_demo
	db, err := sql.Open("postgres", "host=localhost port=5432 user=arpan.banerjee dbname=movie_booking_demo sslmode=disable")
	if err != nil {
		log.Fatalf("Failed to connect to PostgreSQL: %v", err)
	}
	defer db.Close()

	// Test the connection
	err = db.Ping()
	if err != nil {
		log.Fatalf("Failed to ping PostgreSQL: %v", err)
	}
	log.Println("✅ Connected to PostgreSQL database")

	// Drop tables if they exist (in correct order due to foreign keys)
	log.Println("🗑️  Dropping existing tables...")
	_, err = db.Exec("DROP TABLE IF EXISTS bookings")
	if err != nil {
		log.Fatalf("Failed to drop bookings table: %v", err)
	}

	_, err = db.Exec("DROP TABLE IF EXISTS seats")
	if err != nil {
		log.Fatalf("Failed to drop seats table: %v", err)
	}

	_, err = db.Exec("DROP TABLE IF EXISTS movies")
	if err != nil {
		log.Fatalf("Failed to drop movies table: %v", err)
	}

	_, err = db.Exec("DROP TABLE IF EXISTS users")
	if err != nil {
		log.Fatalf("Failed to drop users table: %v", err)
	}

	// Create users table
	log.Println("👥 Creating users table...")
	_, err = db.Exec(`
		CREATE TABLE users (
			id SERIAL PRIMARY KEY,
			username VARCHAR(50) UNIQUE NOT NULL,
			email VARCHAR(100) UNIQUE NOT NULL,
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
		)
	`)
	if err != nil {
		log.Fatalf("Failed to create users table: %v", err)
	}

	// Create movies table
	log.Println("🎬 Creating movies table...")
	_, err = db.Exec(`
		CREATE TABLE movies (
			id SERIAL PRIMARY KEY,
			title VARCHAR(200) NOT NULL,
			duration_minutes INTEGER NOT NULL,
			release_date DATE NOT NULL,
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
		)
	`)
	if err != nil {
		log.Fatalf("Failed to create movies table: %v", err)
	}

	// Create seats table
	log.Println("💺 Creating seats table...")
	_, err = db.Exec(`
		CREATE TABLE seats (
			id SERIAL PRIMARY KEY,
			name VARCHAR(50) NOT NULL,
			movie_id INTEGER NOT NULL,
			user_id INTEGER NULL,
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY (movie_id) REFERENCES movies(id),
			FOREIGN KEY (user_id) REFERENCES users(id)
		)
	`)
	if err != nil {
		log.Fatalf("Failed to create seats table: %v", err)
	}

	// Create bookings table
	log.Println("📋 Creating bookings table...")
	_, err = db.Exec(`
		CREATE TABLE bookings (
			id SERIAL PRIMARY KEY,
			user_id INTEGER NOT NULL,
			seat_id INTEGER NOT NULL,
			movie_id INTEGER NOT NULL,
			booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY (user_id) REFERENCES users(id),
			FOREIGN KEY (seat_id) REFERENCES seats(id),
			FOREIGN KEY (movie_id) REFERENCES movies(id)
		)
	`)
	if err != nil {
		log.Fatalf("Failed to create bookings table: %v", err)
	}

	// Insert 1 movie
	log.Println("🎭 Inserting movie...")
	_, err = db.Exec("INSERT INTO movies (title, duration_minutes, release_date) VALUES ($1, $2, $3)",
		"The Multithreading Adventure", 120, "2024-01-15")
	if err != nil {
		log.Fatalf("Failed to insert movie: %v", err)
	}

	// Insert 200 users
	log.Println("👤 Inserting 200 users...")
	for i := 1; i <= 200; i++ {
		username := fmt.Sprintf("user%03d", i)
		email := fmt.Sprintf("user%03d@example.com", i)
		_, err = db.Exec("INSERT INTO users (username, email) VALUES ($1, $2)", username, email)
		if err != nil {
			log.Fatalf("Failed to insert user %d: %v", i, err)
		}
	}

	// Insert 200 seats
	log.Println("💺 Inserting 200 seats...")
	for i := 1; i <= 200; i++ {
		seatName := fmt.Sprintf("Seat-%d", i)
		_, err = db.Exec("INSERT INTO seats (name, movie_id, user_id) VALUES ($1, $2, $3)",
			seatName, 1, nil)
		if err != nil {
			log.Fatalf("Failed to insert seat %d: %v", i, err)
		}
	}

	// Verify the data
	log.Println("✅ Verifying data...")
	var userCount, seatCount, movieCount int

	err = db.QueryRow("SELECT COUNT(*) FROM users").Scan(&userCount)
	if err != nil {
		log.Fatalf("Failed to count users: %v", err)
	}

	err = db.QueryRow("SELECT COUNT(*) FROM seats").Scan(&seatCount)
	if err != nil {
		log.Fatalf("Failed to count seats: %v", err)
	}

	err = db.QueryRow("SELECT COUNT(*) FROM movies").Scan(&movieCount)
	if err != nil {
		log.Fatalf("Failed to count movies: %v", err)
	}

	log.Printf("📊 Database setup completed!")
	log.Printf("   Users: %d", userCount)
	log.Printf("   Seats: %d", seatCount)
	log.Printf("   Movies: %d", movieCount)
	log.Println("🎉 PostgreSQL database is ready for the seat booking demo!")
}
