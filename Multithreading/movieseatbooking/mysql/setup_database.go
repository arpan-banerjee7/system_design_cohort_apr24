package main

import (
	"database/sql"
	"fmt"
	"log"

	_ "github.com/go-sql-driver/mysql"
)

func main() {
	// Connect to MySQL server (create database if not exists)
	db, err := sql.Open("mysql", "root:@tcp(127.0.0.1:3306)/")
	if err != nil {
		log.Fatalf("Failed to connect to MySQL: %v", err)
	}
	defer db.Close()

	// Test connection
	err = db.Ping()
	if err != nil {
		log.Fatalf("Failed to ping MySQL: %v", err)
	}
	log.Println("✅ Connected to MySQL server")

	// Create database
	_, err = db.Exec("CREATE DATABASE IF NOT EXISTS movie_booking_demo")
	if err != nil {
		log.Fatalf("Failed to create database: %v", err)
	}
	log.Println("✅ Created database: movie_booking_demo")

	// Connect to the specific database
	db.Close()
	db, err = sql.Open("mysql", "root:@tcp(127.0.0.1:3306)/movie_booking_demo?parseTime=true")
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}
	defer db.Close()

	// Drop tables if they exist
	log.Println("🗑️  Dropping existing tables...")
	dropQueries := []string{
		"DROP TABLE IF EXISTS bookings",
		"DROP TABLE IF EXISTS seats",
		"DROP TABLE IF EXISTS movies",
		"DROP TABLE IF EXISTS users",
	}

	for _, query := range dropQueries {
		_, err = db.Exec(query)
		if err != nil {
			log.Printf("Warning: Failed to drop table: %v", err)
		}
	}

	// Create tables
	log.Println("🏗️  Creating tables...")

	// Create users table
	_, err = db.Exec(`
		CREATE TABLE users (
			id INT PRIMARY KEY AUTO_INCREMENT,
			username VARCHAR(50) UNIQUE NOT NULL,
			email VARCHAR(100) UNIQUE NOT NULL,
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
		)
	`)
	if err != nil {
		log.Fatalf("Failed to create users table: %v", err)
	}
	log.Println("✅ Created users table")

	// Create movies table
	_, err = db.Exec(`
		CREATE TABLE movies (
			id INT PRIMARY KEY AUTO_INCREMENT,
			title VARCHAR(200) NOT NULL,
			duration_minutes INT NOT NULL,
			release_date DATE NOT NULL,
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
		)
	`)
	if err != nil {
		log.Fatalf("Failed to create movies table: %v", err)
	}
	log.Println("✅ Created movies table")

	// Create seats table
	_, err = db.Exec(`
		CREATE TABLE seats (
			id INT PRIMARY KEY AUTO_INCREMENT,
			name VARCHAR(50) NOT NULL,
			movie_id INT NOT NULL,
			user_id INT NULL,
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY (movie_id) REFERENCES movies(id),
			FOREIGN KEY (user_id) REFERENCES users(id)
		)
	`)
	if err != nil {
		log.Fatalf("Failed to create seats table: %v", err)
	}
	log.Println("✅ Created seats table")

	// Create bookings table
	_, err = db.Exec(`
		CREATE TABLE bookings (
			id INT PRIMARY KEY AUTO_INCREMENT,
			user_id INT NOT NULL,
			seat_id INT NOT NULL,
			movie_id INT NOT NULL,
			booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY (user_id) REFERENCES users(id),
			FOREIGN KEY (seat_id) REFERENCES seats(id),
			FOREIGN KEY (movie_id) REFERENCES movies(id)
		)
	`)
	if err != nil {
		log.Fatalf("Failed to create bookings table: %v", err)
	}
	log.Println("✅ Created bookings table")

	// Insert 1 movie
	log.Println("🎬 Inserting movie...")
	_, err = db.Exec(`
		INSERT INTO movies (title, duration_minutes, release_date) VALUES 
		('The Multithreading Adventure', 120, '2024-01-15')
	`)
	if err != nil {
		log.Fatalf("Failed to insert movie: %v", err)
	}
	log.Println("✅ Inserted movie")

	// Insert 200 users
	log.Println("👥 Inserting 200 users...")
	userQuery := `
		INSERT INTO users (username, email) VALUES 
		(?, ?)
	`

	for i := 1; i <= 200; i++ {
		username := fmt.Sprintf("user%03d", i)
		email := fmt.Sprintf("user%03d@example.com", i)

		_, err = db.Exec(userQuery, username, email)
		if err != nil {
			log.Fatalf("Failed to insert user %d: %v", i, err)
		}
	}
	log.Println("✅ Inserted 200 users")

	// Insert 200 seats
	log.Println("💺 Inserting 200 seats...")
	seatQuery := `
		INSERT INTO seats (name, movie_id, user_id) VALUES 
		(?, 1, NULL)
	`

	for i := 1; i <= 200; i++ {
		seatName := fmt.Sprintf("Seat-%d", i)

		_, err = db.Exec(seatQuery, seatName)
		if err != nil {
			log.Fatalf("Failed to insert seat %d: %v", i, err)
		}
	}
	log.Println("✅ Inserted 200 seats")

	// Verify the data
	log.Println("📊 Verifying data...")

	var userCount, seatCount, movieCount int

	err = db.QueryRow("SELECT COUNT(*) FROM users").Scan(&userCount)
	if err != nil {
		log.Printf("Warning: Failed to count users: %v", err)
	}

	err = db.QueryRow("SELECT COUNT(*) FROM seats").Scan(&seatCount)
	if err != nil {
		log.Printf("Warning: Failed to count seats: %v", err)
	}

	err = db.QueryRow("SELECT COUNT(*) FROM movies").Scan(&movieCount)
	if err != nil {
		log.Printf("Warning: Failed to count movies: %v", err)
	}

	log.Printf("✅ Database setup complete!")
	log.Printf("   Users: %d", userCount)
	log.Printf("   Seats: %d", seatCount)
	log.Printf("   Movies: %d", movieCount)
	log.Printf("   Database: movie_booking_demo")
	log.Printf("   Connection: root:@tcp(127.0.0.1:3306)/movie_booking_demo")
}
