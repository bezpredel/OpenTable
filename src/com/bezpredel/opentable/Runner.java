package com.bezpredel.opentable;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Date: 7/30/12
 * Time: 2:26 PM
 */
public class Runner {


    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("Usage: 1and1_mail_username 1and1_mail_password destination_emails_comma_separated restaurant_id hour_in_pm(between 1 and 11) [delay]");
            return;
        }



        String username = args[0];
        String password = args[1];
        String destinations = args[2];
        int restaurantId = Integer.parseInt(args[3]);
        int pmHour = Integer.parseInt(args[4]);


        int delay = 5 * 60 * 1000;
        if (args.length > 5) {
            delay = Integer.parseInt(args[5]) * 1000;
        }

        //21790 Babbo Ristorante
        //211 11Madison
        RequestSender rs = new RequestSender();

        RequestSender.RestaurantList list = rs.requestRestaurants();
        String name = list.get(restaurantId);
        if (name == null) {
            System.err.println("Restaurant with id " + restaurantId + " not found");
            return;
        }

        if (pmHour < 1 || pmHour > 12) {
            System.err.println("Illegal hour " + pmHour + ": must be between 1 and 12");
            return;
        }

        Mailer mailer = new Mailer("smtp.1and1.com", "587", username, password, username, destinations);

        PrintStream log = new PrintStream(new FileOutputStream("OpenTable.log"));

        AvailabilityChecker availabilityChecker = new AvailabilityChecker(
                log, name, mailer, rs, restaurantId, pmHour, delay, destinations
        );

        availabilityChecker.start();
    }


    public static class AvailabilityChecker {
        private static final int CONSECUTIVE_ERROR_THRESHOLD = 10;
        private static final double DELAY_RANDOMIZATION = 0.25;
        private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");


        private final Random random = new Random();
        private final PrintStream log;
        private final String name;
        private final Mailer mailer;
        private final RequestSender requestSender;
        private final int restaurantId;
        private final int pmHour;
        private final int delay;

        private final String destinations;


        public AvailabilityChecker(PrintStream log, String name, Mailer mailer, RequestSender sender, int id, int hour, int delay, String destinations) {
            this.log = log;
            this.name = name;
            this.mailer = mailer;
            requestSender = sender;
            restaurantId = id;
            pmHour = hour;
            this.delay = delay;
            this.destinations = destinations;


        }

        public void start() throws Exception {
            String startReport = createStartString();
            log(startReport);
            mailer.sendMail(startReport, startReport);


            int consecutiveErrorCount = 0;
            List<Date> lastAvailability = Collections.emptyList();

            while (true) {
                if (consecutiveErrorCount > CONSECUTIVE_ERROR_THRESHOLD) {
                    log("Too many failed attempts");
                    break;
                }

                try {
                    log("Requesting...");
                    List<Date> availability = requestSender.getAvailability(restaurantId, pmHour);

                    if (availability.equals(lastAvailability)) {
                        log("No new availability");
                    } else {
                        String[] report = createReport(availability, lastAvailability, name);
                        lastAvailability = availability;
                        log(report[1]);
                        mailer.sendMail(report[0], report[1]);
                    }
                    consecutiveErrorCount = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    log("Failed: " + e.getMessage());
                    ++consecutiveErrorCount;
                }


                int delay1 = getDelay(delay, DELAY_RANDOMIZATION);
                log("Sleeping for " + delay1 / 1000);
                Thread.sleep(delay1);
            }
        }

        private String createStartString() {
            return dateFormat.format(new Date()) + ": Starting for " + restaurantId + ": [" + name + "] around " + pmHour + "PM every " + this.delay / 1000 + " seconds";
        }


        private int getDelay(int delay, double randomization) {
            return (int) (delay + (random.nextDouble() - 0.5) * delay * randomization);
        }

        private String[] createReport(List<Date> availability, List<Date> lastAvailability, String name) {
            StringBuilder sb = new StringBuilder("Availability changed for ").append(name).append(":\n");
            HashSet<Date> newA = new HashSet<Date>(availability);
            HashSet<Date> oldA = new HashSet<Date>(lastAvailability);

            ArrayList<Date> addedA = new ArrayList<Date>(newA);
            addedA.removeAll(oldA);

            ArrayList<Date> removedA = new ArrayList<Date>(oldA);
            removedA.removeAll(newA);

            ArrayList<Date> retainedA = new ArrayList<Date>(newA);
            retainedA.retainAll(oldA);

            Collections.sort(addedA);
            Collections.sort(removedA);
            Collections.sort(retainedA);

            if (addedA.isEmpty()) {
                sb.append("No new spots became available\n");
            } else {
                sb.append("New spots became available:\n");
                for (Date s : addedA) {
                    sb.append(dateFormat.format(s)).append("\n");
                }
            }

            sb.append("\n");

            if (!removedA.isEmpty()) {
                sb.append("No longer available available:\n");
                for (Date s : removedA) {
                    sb.append(dateFormat.format(s)).append("\n");
                }
                sb.append("\n");
            }

            if (!retainedA.isEmpty()) {
                sb.append("Still available:\n");
                for (Date s : retainedA) {
                    sb.append(dateFormat.format(s)).append("\n");
                }
                sb.append("\n");
            }

            String subject = "Availability update for [" + name + "]: +" + addedA.size() + " -" + removedA.size();

            return new String[]{subject, sb.toString()};
        }



        private void log(String s) {
            String prefix = timestampFormat.format(new Date()) + ": ";
            s = prefix + s;

            System.out.println(s);
            log.println(s);
            log.flush();
        }
    }
}
