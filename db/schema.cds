namespace zplanner;

using { cuid } from '@sap/cds/common';


entity Room : cuid {
    name: String(128);
    description: String(256);
    link : String(512);
    meetingId: String(32);
    passcode: String(32);
    to_bookings : Composition of many Booking on to_bookings.parent = $self; 
}

entity Booking : cuid {
    start: Timestamp;
    ends: Timestamp;  
    parent : Association to Room; 
}

