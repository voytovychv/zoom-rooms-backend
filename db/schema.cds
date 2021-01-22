namespace zplanner;

using { cuid } from '@sap/cds/common';

entity Room : cuid {
    name: String(128);
    description: String(256);
    link : String(512);
    meetingId: String(32);
    passcode: String(32);
    @cascade:{all}
    to_bookings : Composition of many Booking on to_bookings.to_room = $self; 
}

entity TimeSlot : cuid {
    date: Date;
    start: Timestamp;
    ends: Timestamp;
    to_bookings: Association to many Booking on to_bookings.to_timeSlot=$self; 
    yearNumber: Association to Year;
}

entity Booking : cuid {
    name: String(64);
    isReserved: Boolean default false;
    to_timeSlot: Association to TimeSlot;
    to_room : Association to Room; 
}

entity Year {
    key yearNumber: Integer;
    to_bookings : Composition of many TimeSlot on to_bookings.yearNumber = $self;
}