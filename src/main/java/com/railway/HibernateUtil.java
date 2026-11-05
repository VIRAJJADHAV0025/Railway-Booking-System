package com.railway;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Creates and manages the application's Hibernate SessionFactory.
 */
public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY =
            buildSessionFactory();

    private HibernateUtil() {
        // Utility class. Do not create instances.
    }

    private static String environmentValue(
            String name,
            String defaultValue) {

        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    private static SessionFactory buildSessionFactory() {

        Map<String, Object> settings = new HashMap<>();

        settings.put(
                "hibernate.connection.url",
                environmentValue(
                        "DB_URL",
                        "jdbc:mysql://localhost:3306/railway"
                )
        );

        settings.put(
                "hibernate.connection.username",
                environmentValue("DB_USER", "root")
        );

        settings.put(
                "hibernate.connection.password",
                environmentValue("DB_PASSWORD", "root")
        );

        settings.put(
                "hibernate.connection.driver_class",
                "com.mysql.cj.jdbc.Driver"
        );

        settings.put(
                "hibernate.dialect",
                "org.hibernate.dialect.MySQLDialect"
        );

        settings.put("hibernate.hbm2ddl.auto", "validate");
        settings.put("hibernate.show_sql", "false");
        settings.put("hibernate.format_sql", "false");
        settings.put("hibernate.use_sql_comments", "false");

        StandardServiceRegistry registry =
                new StandardServiceRegistryBuilder()
                        .applySettings(settings)
                        .build();

        try {

            return new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Station.class)
                    .addAnnotatedClass(Train.class)
                    .addAnnotatedClass(TrainStop.class)
                    .addAnnotatedClass(TrainRunningDay.class)
                    .addAnnotatedClass(CoachClass.class)
                    .addAnnotatedClass(TrainClass.class)
                    .addAnnotatedClass(Booking.class)
                    .addAnnotatedClass(BookingPassenger.class)
                    .buildMetadata()
                    .buildSessionFactory();

        } catch (Exception exception) {

            System.err.println(
                    "Failed to initialize Hibernate."
            );

            exception.printStackTrace();

            StandardServiceRegistryBuilder.destroy(registry);

            throw new ExceptionInInitializerError(exception);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        SESSION_FACTORY.close();
    }
}
