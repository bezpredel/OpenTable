package com.bezpredel.opentable.ui;

import com.bezpredel.opentable.Mailer;
import com.bezpredel.opentable.RequestSender;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
* Date: 7/30/12
* Time: 6:58 PM
*/
public class TaskRunner {
    private static final int CONSECUTIVE_ERROR_THRESHOLD = 10;
    private static final double DELAY_RANDOMIZATION = 0.25;
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final RequestSender requestSender;
    private volatile Mailer mailer;
    private final Logger logger;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private int delay = 300000;

    public TaskRunner(RequestSender requestSender, Mailer mailer, Logger logger) {
        this.requestSender = requestSender;
        this.mailer = mailer;
        this.logger = logger;
    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

    public void cancelTask(final ActiveTask task) {
        task.stopNextTimer();
        log("Cancelled", task.restaurant.getName());
    }

    public void runTask(final ActiveTask task) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                _runTask(task);
            }
        });
    }

    private void _runTask(ActiveTask task) {
        String name = task.restaurant.getName();
        task.stopNextTimer();

        try {
            log("Requesting...", name);
            List<Date> availability = requestSender.getAvailability(task.restaurant.getId(), task.hour);

            List<Date> lastAvailability = task.getLastResult();

            if (availability.equals(lastAvailability)) {
                log("No new availability", name);
            } else {
                String[] report = createReport(availability, lastAvailability, name);
                task.setLastResult(availability);

                log(report[1], name);

                if (mailer == null) {
                    log("EMAIL SETTING MISSING", name);
                } else {
                    try {
                        mailer.sendMail(report[0], report[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log("Failed to send email notification: " + e.getMessage(), name);
                    }
                }
            }
            task.clearConsecutiveErrorCount();
            task.setLastCheckTime(new Date());

        } catch (Exception e) {
            e.printStackTrace();
            log("Failed: " + e.getMessage(), name);
            task.incConsecutiveErrorCount();
        }

        if(task.getConsecutiveErrorCount() > CONSECUTIVE_ERROR_THRESHOLD) {
            task.setDead();
        } else {
            scheduleRepeat(task);
        }

    }

    private void scheduleRepeat(final ActiveTask task) {
        int delay = getDelay(this.delay, DELAY_RANDOMIZATION);
        task.scheduleTimer(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        runTask(task);
                    }
                },
                delay
        );
    }

    private final Random random = new Random();
    private int getDelay(int delay, double randomization) {
        return (int) (delay + (random.nextDouble() - 0.5) * delay * randomization);
    }

    private void log(final String s, final String name) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        logger.log(s, name);
                    }
                }
        );

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

    public void setDelay(int delay) {
        this.delay = delay * 1000;
    }
}
