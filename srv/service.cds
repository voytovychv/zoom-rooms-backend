using { zplanner.Room, zplanner.Booking, zplanner.Year, zplanner.TimeSlot } from '../db/schema';


// @path:'browse'
service ScheduleService {

entity Rooms as projection on Room;

entity Bookings as projection on Booking;

entity Years as projection on Year;

entity TimeSlots as projection on TimeSlot actions {
   action BookRoom(roomId:UUID, name:String) returns Bookings;
   action UnbookRoom(bookingId:UUID) returns Boolean;
};

extend TimeSlot with {
    to_allRooms : Association to many Room;
};
action checkAvailability(start:Timestamp, ends:Timestamp) returns array of Rooms;

}



