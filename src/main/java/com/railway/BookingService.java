package com.railway;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

/** Handles passenger accounts, train searches and ticket bookings. */
public class BookingService {

    public LocalDate startDate() {
        return LocalDate.now();
    }

    public LocalDate endDate() {
        return LocalDate.now().plusDays(60);
    }

    public String dateMessage() {
        return "The journey date must be between " + startDate() + " and " + endDate() + ".";
    }

    public boolean register(String username, String password, String fullName) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Please enter a username and password.");
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long existingUsers = session.createQuery(
                    "select count(user) from User user where user.username = :username",
                    Long.class
            ).setParameter("username", username.trim()).getSingleResult();

            if (existingUsers > 0) {
                return false;
            }

            Transaction transaction = session.beginTransaction();
            session.persist(new User(
                    username.trim(),
                    password,
                    UserRole.PASSENGER,
                    fullName
            ));
            transaction.commit();
            return true;
        }
    }

    public User login(String username, String password, UserRole role) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.createQuery(
                    "from User user where user.username = :username and user.role = :role",
                    User.class
            )
                    .setParameter("username", username.trim())
                    .setParameter("role", role)
                    .uniqueResult();

            return user != null && user.passwordMatches(password) ? user : null;
        }
    }

    public List<Station> stations() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Station order by name",
                    Station.class
            ).getResultList();
        }
    }

    public List<Train> search(String fromCode, String toCode, LocalDate journeyDate) {
        validateJourneyDate(journeyDate);

        String query = """
                select distinct train
                from Train train
                join TrainStop startStop on startStop.train = train
                join TrainStop endStop on endStop.train = train
                join TrainRunningDay runningDay on runningDay.train = train
                where startStop.station.code = :fromCode
                  and endStop.station.code = :toCode
                  and startStop.stopOrder < endStop.stopOrder
                  and runningDay.dayOfWeek = :dayOfWeek
                """;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(query, Train.class)
                    .setParameter("fromCode", fromCode)
                    .setParameter("toCode", toCode)
                    .setParameter("dayOfWeek", journeyDate.getDayOfWeek())
                    .getResultList();
        }
    }

    public List<TrainStop> stops(Long trainId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from TrainStop stop join fetch stop.station "
                            + "where stop.train.id = :trainId order by stop.stopOrder",
                    TrainStop.class
            ).setParameter("trainId", trainId).getResultList();
        }
    }

    public List<TrainClass> classes(Long trainId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select trainClass from TrainClass trainClass "
                            + "join fetch trainClass.coachClass "
                            + "where trainClass.train.id = :trainId",
                    TrainClass.class
            ).setParameter("trainId", trainId).getResultList();
        }
    }

    public int seatsLeft(Long trainId, String classCode, LocalDate journeyDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            TrainClass trainClass = session.createQuery(
                    "from TrainClass trainClass "
                            + "where trainClass.train.id = :trainId "
                            + "and trainClass.coachClass.code = :classCode",
                    TrainClass.class
            )
                    .setParameter("trainId", trainId)
                    .setParameter("classCode", classCode)
                    .uniqueResult();

            if (trainClass == null) {
                return 0;
            }

            Long bookedSeats = session.createQuery(
                    "select coalesce(sum(booking.seatCount), 0) from Booking booking "
                            + "where booking.train.id = :trainId "
                            + "and booking.coachClass.code = :classCode "
                            + "and booking.journeyDate = :journeyDate "
                            + "and booking.status = :status",
                    Long.class
            )
                    .setParameter("trainId", trainId)
                    .setParameter("classCode", classCode)
                    .setParameter("journeyDate", journeyDate)
                    .setParameter("status", BookingStatus.CONFIRMED)
                    .getSingleResult();

            return Math.max(0, trainClass.getTotalSeats() - bookedSeats.intValue());
        }
    }

    public String book(
            User passenger,
            Long trainId,
            String classCode,
            LocalDate journeyDate,
            List<BookingPassenger> passengers
    ) {
        if (passenger == null || passenger.getRole() != UserRole.PASSENGER) {
            throw new IllegalArgumentException("Please log in with a passenger account before booking.");
        }
        validateJourneyDate(journeyDate);

        if (passengers == null || passengers.isEmpty() || passengers.size() > 6) {
            throw new IllegalArgumentException("A booking must include between 1 and 6 passengers.");
        }

        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            TrainClass trainClass = session.createQuery(
                    "from TrainClass trainClass "
                            + "where trainClass.train.id = :trainId "
                            + "and trainClass.coachClass.code = :classCode",
                    TrainClass.class
            )
                    .setParameter("trainId", trainId)
                    .setParameter("classCode", classCode)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .uniqueResult();

            if (trainClass == null) {
                throw new IllegalArgumentException("The selected class is not available on this train.");
            }

            Long runningDayCount = session.createQuery(
                    "select count(day) from TrainRunningDay day "
                            + "where day.train.id = :trainId and day.dayOfWeek = :dayOfWeek",
                    Long.class
            )
                    .setParameter("trainId", trainId)
                    .setParameter("dayOfWeek", journeyDate.getDayOfWeek())
                    .getSingleResult();

            if (runningDayCount == 0) {
                throw new IllegalArgumentException("This train does not operate on the selected day.");
            }

            Long bookedSeats = session.createQuery(
                    "select coalesce(sum(booking.seatCount), 0) from Booking booking "
                            + "where booking.train.id = :trainId "
                            + "and booking.coachClass.code = :classCode "
                            + "and booking.journeyDate = :journeyDate "
                            + "and booking.status = :status",
                    Long.class
            )
                    .setParameter("trainId", trainId)
                    .setParameter("classCode", classCode)
                    .setParameter("journeyDate", journeyDate)
                    .setParameter("status", BookingStatus.CONFIRMED)
                    .getSingleResult();

            if (bookedSeats + passengers.size() > trainClass.getTotalSeats()) {
                throw new IllegalArgumentException("There are not enough seats available for this journey.");
            }

            User managedPassenger = session.get(User.class, passenger.getId());
            Train train = session.get(Train.class, trainId);
            String pnr = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 10)
                    .toUpperCase();

            Booking booking = new Booking(
                    pnr,
                    managedPassenger,
                    train,
                    trainClass.getCoachClass(),
                    journeyDate,
                    passengers.size()
            );
            passengers.forEach(booking::addPassenger);

            session.persist(booking);
            transaction.commit();
            return pnr;
        } catch (RuntimeException exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        }
    }

    public List<Booking> mine(Long passengerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select distinct booking from Booking booking "
                            + "join fetch booking.train "
                            + "join fetch booking.coachClass "
                            + "where booking.user.id = :passengerId "
                            + "order by booking.createdAt desc",
                    Booking.class
            ).setParameter("passengerId", passengerId).getResultList();
        }
    }

    public List<Booking> all() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select distinct booking from Booking booking "
                            + "join fetch booking.user "
                            + "join fetch booking.train "
                            + "join fetch booking.coachClass "
                            + "order by booking.createdAt desc",
                    Booking.class
            ).getResultList();
        }
    }

    public boolean cancel(User passenger, String pnr) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Booking booking = session.createQuery(
                    "from Booking booking "
                            + "where booking.pnr = :pnr and booking.user.id = :passengerId",
                    Booking.class
            )
                    .setParameter("pnr", pnr)
                    .setParameter("passengerId", passenger.getId())
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .uniqueResult();

            if (booking == null || booking.getStatus() == BookingStatus.CANCELLED) {
                transaction.rollback();
                return false;
            }

            session.createMutationQuery(
                    "update Booking booking set booking.status = :status where booking.id = :bookingId"
            )
                    .setParameter("status", BookingStatus.CANCELLED)
                    .setParameter("bookingId", booking.getId())
                    .executeUpdate();

            transaction.commit();
            return true;
        }
    }

    private void validateJourneyDate(LocalDate journeyDate) {
        if (journeyDate == null
                || journeyDate.isBefore(startDate())
                || journeyDate.isAfter(endDate())) {
            throw new IllegalArgumentException(dateMessage());
        }
    }
}
