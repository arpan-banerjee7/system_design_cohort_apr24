#!/bin/bash

echo "🎬 Movie Seat Booking - Multithreading Locking Demo"
echo "=================================================="
echo ""

# Check if MySQL is running
if ! mysqladmin ping -h localhost -u root -p --silent; then
    echo "❌ MySQL server is not running. Please start MySQL first."
    exit 1
fi

echo "✅ MySQL server is running"

# Check if Go is installed
if ! command -v go &> /dev/null; then
    echo "❌ Go is not installed. Please install Go 1.21 or later."
    exit 1
fi

echo "✅ Go is installed"

# Check if setup_database.sql exists
if [ ! -f "setup_database.sql" ]; then
    echo "❌ setup_database.sql not found. Please ensure you're in the correct directory."
    exit 1
fi

echo "✅ Database setup script found"

# Ask for MySQL credentials
echo ""
echo "Please enter your MySQL root password:"
read -s MYSQL_PASSWORD

# Setup database
echo ""
echo "Setting up database..."
mysql -u root -p"$MYSQL_PASSWORD" < setup_database.sql

if [ $? -eq 0 ]; then
    echo "✅ Database setup completed"
else
    echo "❌ Database setup failed"
    exit 1
fi

# Update database connection in main.go
echo ""
echo "Please update the database connection string in main.go with your MySQL credentials:"
echo "Change: dbDSN = \"root:password@tcp(localhost:3306)/movie_booking_demo?parseTime=true\""
echo "To:    dbDSN = \"root:YOUR_PASSWORD@tcp(localhost:3306)/movie_booking_demo?parseTime=true\""
echo ""

# Install Go dependencies
echo "Installing Go dependencies..."
go mod tidy

if [ $? -eq 0 ]; then
    echo "✅ Dependencies installed"
else
    echo "❌ Failed to install dependencies"
    exit 1
fi

echo ""
echo "🚀 Ready to run the demo!"
echo "Run: go run main.go"
echo ""
echo "The demo will test three different locking strategies:"
echo "1. No Lock Strategy (shows race conditions)"
echo "2. FOR UPDATE Strategy (pessimistic locking)"
echo "3. FOR UPDATE SKIP LOCKED Strategy (optimistic locking)" 