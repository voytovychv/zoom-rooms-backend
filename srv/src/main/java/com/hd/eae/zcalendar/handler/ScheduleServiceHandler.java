package com.hd.eae.zcalendar.handler;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.hd.eae.pojo.scheduleservice.BookRoomContext;
import com.hd.eae.pojo.scheduleservice.Bookings;
import com.hd.eae.pojo.scheduleservice.Rooms;
import com.hd.eae.pojo.scheduleservice.Rooms_;
import com.hd.eae.pojo.scheduleservice.ScheduleService_;
import com.hd.eae.pojo.scheduleservice.TimeSlots;
import com.hd.eae.pojo.scheduleservice.TimeSlots_;
import com.hd.eae.pojo.scheduleservice.Years_;
import com.hd.eae.pojo.zplanner.Booking;
import com.hd.eae.pojo.zplanner.Booking_;
import com.hd.eae.pojo.zplanner.TimeSlot;
import com.hd.eae.pojo.zplanner.Year;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.services.cds.CdsService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;

@Component
@ServiceName(ScheduleService_.CDS_NAME)
public class ScheduleServiceHandler implements EventHandler {
// I want you very very
	private Logger log = Logger.getLogger(ScheduleServiceHandler.class.getName());
	private final PersistenceService db;

	public ScheduleServiceHandler(PersistenceService db) {
		this.db = db;
	}

	@On(entity = TimeSlots_.CDS_NAME, event = BookRoomContext.CDS_NAME)
	public void addBookToOrder(BookRoomContext context) {
	
		TimeSlot slot = db.run(context.getCqn()).first(TimeSlot.class).get();
		
		Booking booking = Booking.create();
		booking.setId(UUID.randomUUID().toString());
		booking.setToTimeSlotId(slot.getId());
		booking.setName(context.getName());
		booking.setToRoomId(context.getRoomId());
		
		db.run(Insert.into(Booking_.CDS_NAME).entry(booking));
		
		Bookings result = Bookings.create();
		result.setName(context.getName());
		result.setIsReserved(true);
		result.setId(booking.getId());
		context.setResult(result);
		context.setCompleted();
		
	}
	
	@After(event = CdsService.EVENT_READ, entity = TimeSlots_.CDS_NAME)
	public void afterReadBookings(Stream<TimeSlots> slots) {
		List<Rooms> rooms = db.run(Select.from(Rooms_.CDS_NAME)).listOf(Rooms.class);
		slots.forEach(slot -> slot.setToAllRooms(rooms));
	}

	@After(event = CdsService.EVENT_CREATE, entity = Years_.CDS_NAME)
	public void afterCreateYear(List<Year> years) {

		for(Year year : years) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(year.getYearNumber(), 0, 0, 0, 0, 0);
			int days = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);

			int startHour = 7;
			int finishHour = 22;

			for (int i = 0; i <= days; i++) {
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				List<TimeSlots> timeslots = new ArrayList<>();
				
				for (int h = startHour; h <= finishHour; h++) {
						calendar.set(Calendar.HOUR_OF_DAY, h);

						TimeSlots slot = TimeSlots.create();
						slot.setDate(LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
								calendar.get(Calendar.DAY_OF_MONTH)));
						slot.setStart(this.getForm(calendar));
						slot.setEnds(this.getTo(calendar));
						slot.setYearNumberYearNumber(year.getYearNumber());
						timeslots.add(slot);
						log.info("== " + slot.getStart() + " - " + slot.getEnds() + "===");
						add1Hour(calendar);
				}
								
				log.info("Creating schedule for " + calendar.getTime());
				db.run(Insert.into(TimeSlots_.CDS_NAME).entries(timeslots));
				timeslots = new ArrayList<>();

			}
		}

	}

	private void add1Hour(Calendar c) {
		c.add(Calendar.HOUR_OF_DAY, 1);
	}

	private Instant getForm(Calendar c) {
		return c.getTime().toInstant();
	}

	private Instant getTo(Calendar c) {
		c.add(Calendar.HOUR_OF_DAY, 1);
		Date to = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, -1);
		return to.toInstant();
	}
}
