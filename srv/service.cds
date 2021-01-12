using { zplanner.Room, zplanner.Booking } from '../db/schema';


// @path:'browse'
service ScheduleService {

entity ZoomRooms as projection on Room;

entity ZoomRoomBookings as projection on Booking;

action checkAvailability(start:Timestamp, ends:Timestamp) returns array of ZoomRooms;

}



