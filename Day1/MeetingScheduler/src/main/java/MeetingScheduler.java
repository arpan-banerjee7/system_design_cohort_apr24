
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.*;
/*
https://leetcode.com/discuss/interview-question/5026730/UBER-or-Senior-Software-Engineer-or-Phone-Screen

Build a reservation system for a predefined set of conference rooms given as a list of room Ids [‘roomA’, roomB’...].
It should have a method like scheduleMeeting(startTime, endTime) should return a reservation identifier (including roomId) and reserve it or an error if no rooms are available.


Assume there are 100 rooms, later can talk about scaling to N
Assume any number of meetings can be scheduled on any conference room
 */
/**
 * ENTITY
 */
class ConferenceRoom {
    private final String roomId;

    public ConferenceRoom(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}

/**
 * ENTITY
 */
class MeetingReservation {
    private final String reservationId;
    private final ConferenceRoom room;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public MeetingReservation(ConferenceRoom room, LocalDateTime startTime, LocalDateTime endTime) {
        this.reservationId = UUID.randomUUID().toString();
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getReservationId() {
        return reservationId;
    }

    public ConferenceRoom getRoom() {
        return room;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}

/**
 * REPOSITORY LAYER-- INHERITANCE-- DB to store list of rooms
 */
interface RoomRepository {
    List<ConferenceRoom> getAllRooms();
}

class InMemoryRoomRepository implements RoomRepository {
    private final List<ConferenceRoom> rooms;

    public InMemoryRoomRepository(List<String> roomIds) {
        this.rooms = new ArrayList<>();
        for (String rId : roomIds) {
            rooms.add(new ConferenceRoom(rId));
        }
    }

    @Override
    public List<ConferenceRoom> getAllRooms() {
        return rooms;
    }
}

/**
 * RESERVATION Repository to store details of meeting reservations
 */
interface ReservationRepository {
    void saveReservation(MeetingReservation reservation);

    List<MeetingReservation> getAllReservations();
}


class InMemoryReservationRepository implements ReservationRepository {
    private final List<MeetingReservation> reservations = new ArrayList<>();

    @Override
    public synchronized void saveReservation(MeetingReservation reservation) {
        reservations.add(reservation);
    }

    @Override
    public synchronized List<MeetingReservation> getAllReservations() {
        return Collections.unmodifiableList(reservations);
    }
}

/**
 * Efficient storage of reservations
 */
class TreeMapReservationRepository implements ReservationRepository {
    // For each room, maintain a TreeMap keyed by reservation start time
    private final Map<String, TreeMap<LocalDateTime, MeetingReservation>> roomReservations = new HashMap<>();

    @Override
    public synchronized void saveReservation(MeetingReservation reservation) {
        roomReservations.computeIfAbsent(
                reservation.getRoom().getRoomId(),
                k -> new TreeMap<>()
        ).put(reservation.getStartTime(), reservation);
    }

    @Override
    public synchronized List<MeetingReservation> getAllReservations() {
        List<MeetingReservation> all = new ArrayList<>();
        for (TreeMap<LocalDateTime, MeetingReservation> map : roomReservations.values()) {
            all.addAll(map.values());
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Returns the TreeMap of reservations for a particular room.
     */
    synchronized TreeMap<LocalDateTime, MeetingReservation> getReservationsForRoom(String roomId) {
        return roomReservations.getOrDefault(roomId, new TreeMap<>());
    }
}


/**
 * STRTEGY PATTERN - diff availability checking strategy
 */
interface AvailabilityChecker {
    boolean isRoomAvailable(ConferenceRoom room, LocalDateTime start, LocalDateTime end);
}


// TC -O(m*n)
class SimpleAvailabilityChecker implements AvailabilityChecker {
    private final ReservationRepository reservationRepository;

    public SimpleAvailabilityChecker(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public boolean isRoomAvailable(ConferenceRoom room, LocalDateTime start, LocalDateTime end) {
        List<MeetingReservation> reservations = reservationRepository.getAllReservations();
        for (MeetingReservation res : reservations) {
            if (res.getRoom().getRoomId().equals(room.getRoomId())) {
                // Check overlap:
                if (!(end.isBefore(res.getStartTime()) || start.isAfter(res.getEndTime()))) {
                    // There is an overlap
                    return false;
                }
            }
        }
        return true;
    }
}


class TreeMapAvailabilityChecker implements AvailabilityChecker {
    private final TreeMapReservationRepository reservationRepository;

    public TreeMapAvailabilityChecker(TreeMapReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public boolean isRoomAvailable(ConferenceRoom room, LocalDateTime start, LocalDateTime end) {
        TreeMap<LocalDateTime, MeetingReservation> reservations = reservationRepository.getReservationsForRoom(room.getRoomId());

        if (reservations.isEmpty()) {
            return true; // No reservations for this room
        }

        // Check reservation that might start before or at 'start'
        Map.Entry<LocalDateTime, MeetingReservation> floorEntry = reservations.floorEntry(start);
        if (floorEntry != null) {
            MeetingReservation floorRes = floorEntry.getValue();
            // If the floor reservation ends after our start time, overlap
            if (!floorRes.getEndTime().isBefore(start)) {
                return false;
            }
        }

        // Check reservation that might start just after 'start'
        Map.Entry<LocalDateTime, MeetingReservation> ceilingEntry = reservations.ceilingEntry(start);
        if (ceilingEntry != null) {
            MeetingReservation ceilRes = ceilingEntry.getValue();
            // If this reservation starts before our requested end time, overlap
            if (!ceilRes.getStartTime().isAfter(end)) {
                return false;
            }
        }

        return true;
    }
}

/**
 * STRATEGY PATTERN- Room selection strategy
 */
interface RoomSelectionStrategy {
    ConferenceRoom selectRoom(List<ConferenceRoom> availableRooms);
}

class FirstAvailableRoomStrategy implements RoomSelectionStrategy {

    @Override
    public ConferenceRoom selectRoom(List<ConferenceRoom> availableRooms) {
        if (availableRooms.isEmpty()) return null;
        return availableRooms.get(0);
    }
}


/**
 * MAIN INTERFACE
 */
interface MeetingScheduler {
    MeetingReservation scheduleMeeting(LocalDateTime start, LocalDateTime end) throws Exception;
}

class ConferenceRoomScheduler implements MeetingScheduler {
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final AvailabilityChecker availabilityChecker;
    private final RoomSelectionStrategy roomSelectionStrategy;

    public ConferenceRoomScheduler(RoomRepository roomRepository,
                                   ReservationRepository reservationRepository,
                                   AvailabilityChecker availabilityChecker,
                                   RoomSelectionStrategy roomSelectionStrategy) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.availabilityChecker = availabilityChecker;
        this.roomSelectionStrategy = roomSelectionStrategy;
    }

    @Override
    public MeetingReservation scheduleMeeting(LocalDateTime start, LocalDateTime end) throws Exception {
        // Step 1: Get list of all rooms
        List<ConferenceRoom> availableRooms = new ArrayList<>();
        for (ConferenceRoom room : roomRepository.getAllRooms()) {
            // Step 2: check if current room is available for that slot
            if (availabilityChecker.isRoomAvailable(room, start, end)) {
                availableRooms.add(room);
            }
        }

        // Step 3: Select the room out of all found available rooms
        ConferenceRoom selectedRoom = roomSelectionStrategy.selectRoom(availableRooms);
        if (selectedRoom == null) {
            throw new Exception("No rooms available for the requested time slot.");
        }

        // Step 4: Save the current reservation
        MeetingReservation reservation = new MeetingReservation(selectedRoom, start, end);
        reservationRepository.saveReservation(reservation);
        return reservation;
    }
}

/**
 * DRIVER MAIN CLASS
 */
class Main {
    public static void main(String[] args) {
        // Initialize Room Repository with given 100 rooms (example)
        RoomRepository roomRepository = new InMemoryRoomRepository(Arrays.asList("roomA", "roomB"));

        // Initialize Reservation Repository
        ReservationRepository reservationRepository = new InMemoryReservationRepository();
        TreeMapReservationRepository treeMapReservationRepository = new TreeMapReservationRepository();

        // Initialize Availability Checker with the reservation repository
        AvailabilityChecker availabilityChecker = new SimpleAvailabilityChecker(reservationRepository);

        TreeMapAvailabilityChecker treeMapAvailabilityChecker = new TreeMapAvailabilityChecker(treeMapReservationRepository);

        // Initialize a room selection strategy
        RoomSelectionStrategy roomSelectionStrategy = new FirstAvailableRoomStrategy();

        // Create the scheduler
        MeetingScheduler scheduler = new ConferenceRoomScheduler(
                roomRepository,
                treeMapReservationRepository,
                treeMapAvailabilityChecker,
                roomSelectionStrategy
        );

        try {
            LocalDateTime start = LocalDateTime.of(2024, 12, 9, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 12, 9, 12, 0);
            MeetingReservation reservation = scheduler.scheduleMeeting(start, end);
            System.out.println("Meeting reserved. ID: " + reservation.getReservationId() + " in room: " + reservation.getRoom().getRoomId());

            LocalDateTime start1 = LocalDateTime.of(2024, 12, 9, 11, 0);
            LocalDateTime end1 = LocalDateTime.of(2024, 12, 9, 14, 30);
            MeetingReservation reservation1 = scheduler.scheduleMeeting(start1, end1);
            System.out.println("Meeting reserved. ID: " + reservation1.getReservationId() + " in room: " + reservation1.getRoom().getRoomId());

            LocalDateTime start2 = LocalDateTime.of(2024, 12, 9, 11, 0);
            LocalDateTime end2 = LocalDateTime.of(2024, 12, 9, 13, 30);
            MeetingReservation reservation2 = scheduler.scheduleMeeting(start2, end2);
            System.out.println("Meeting reserved. ID: " + reservation2.getReservationId() + " in room: " + reservation2.getRoom().getRoomId());
        } catch (Exception e) {
            System.err.println("Failed to schedule meeting: " + e.getMessage());
        }
    }
}