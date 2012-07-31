package com.bezpredel.opentable.ui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observable;

/**
* Date: 7/30/12
* Time: 6:58 PM
*/
public class ActiveTask extends Observable {
    public final Restaurant restaurant;
    public final int hour;

    private boolean dead;
    private List<Date> lastResult;
    private Date lastCheckTime;
    private Date nextCheckTime;

    private Timer nextTimer;
    private int consecutiveErrorCount = 0;

    ActiveTask(Restaurant restaurant, int hour) {
        this.restaurant = restaurant;
        this.hour = hour;
        this.lastResult = Collections.emptyList();
    }


    public synchronized void stopNextTimer() {
        if(nextTimer!=null) {
            nextTimer.stop();
            nextTimer = null;
        }
    }

    public synchronized void scheduleTimer(ActionListener listener, int delay) {
        setNextCheckTime(new Date(System.currentTimeMillis() + delay));
        stopNextTimer();
        nextTimer = new Timer(delay, listener);
        nextTimer.setRepeats(false);
        nextTimer.start();
    }

    public synchronized boolean isDead() {
        return dead;
    }

    public synchronized boolean hasAny() {
        return !lastResult.isEmpty();
    }


    public synchronized List<Date> getLastResult() {
        return lastResult;
    }

    public synchronized Date getLastCheckTime() {
        return lastCheckTime;
    }

    public synchronized Date getNextCheckTime() {
        return nextCheckTime;
    }

    public synchronized void setDead() {
        this.dead = true;
        fireChanged();
    }

    public synchronized void setLastResult(List<Date> lastResult) {
        this.lastResult = lastResult;
        fireChanged();
    }

    public synchronized void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
        fireChanged();
    }



    public synchronized void setNextCheckTime(Date nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
        fireChanged();
    }


    private void fireChanged() {
        setChanged();

        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        notifyObservers();
                    }
                }
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActiveTask task = (ActiveTask) o;

        if (hour != task.hour) return false;
        if (!restaurant.equals(task.restaurant)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = restaurant.hashCode();
        result = 31 * result + hour;
        return result;
    }

    public void incConsecutiveErrorCount() {
        ++consecutiveErrorCount;
    }
    public void clearConsecutiveErrorCount() {
        consecutiveErrorCount = 0;
    }

    public int getConsecutiveErrorCount() {
        return consecutiveErrorCount;
    }
}
