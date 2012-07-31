package com.bezpredel.opentable.ui;

import java.util.prefs.Preferences;

/**
 * Date: 7/30/12
 * Time: 7:03 PM
 */
public class Defaults {
    private static final String DEF_RESTAURANT = "RESTAURANT";
    private static final String DEF_TIME = "TIME";
    private static final String DEF_RECIPIENTS = "RECIPIENTS";
    private static final String DEF_HOST = "MAIL_HOST";
    private static final String DEF_PORT = "MAIL_PORT";
    private static final String DEF_USER = "MAIL_USER";
    private static final String DEF_PASSWORD = "MAIL_PASSWORD";
    private static final String DEF_DELAY = "DELAY";


    private final Preferences prefs;

    public Defaults() throws Exception{
        prefs = Preferences.userNodeForPackage(Defaults.class);
    }

    public Integer getDefaultRestaurant() {
        String val = prefs.get(DEF_RESTAURANT, null);
        try {
            return val==null ? null : Integer.parseInt(val);
        }catch (NumberFormatException e) {
            return null;
        }
    }

    public void setDefaultRestaurant(Integer id) {
        if(id==null) {
            prefs.remove(DEF_RESTAURANT);
        } else {
            prefs.put(DEF_RESTAURANT, id.toString());
        }
    }

    public Integer getDefaultTime() {
        String val = prefs.get(DEF_TIME, "18");
        try {
            return Integer.parseInt(val);
        }catch (NumberFormatException e) {
            return 18;
        }
    }

    public void setDefaultTime(int val) {
        prefs.put(DEF_TIME, Integer.toString(val));
    }

    public String getDefaultMailRecipients() {
        return prefs.get(DEF_RECIPIENTS, "").trim();
    }

    public void setDefaultMailRecipients(String val) {
        prefs.put(DEF_RECIPIENTS, val);
    }

    public boolean hasHostAndPort() {
        return prefs.get(DEF_HOST, "").trim().length() > 0 && prefs.get(DEF_PORT, "").trim().length() > 0;
    }

    public String getDefaultMailHost() {
        return prefs.get(DEF_HOST, "").trim();
    }

    public void setDefaultMailHost(String val) {
        prefs.put(DEF_HOST, val);
    }

    public String getDefaultMailPort() {
        return (prefs.get(DEF_PORT, ""));
    }

    public void setDefaultMailPort(String val) {
        prefs.put(DEF_PORT, (val));
    }

    public String getDefaultMailUser() {
        return prefs.get(DEF_USER, "").trim();
    }

    public void setDefaultMailUser(String val) {
        prefs.put(DEF_USER, val);
    }

    public String getDefaultMailPassword() {
        return prefs.get(DEF_PASSWORD, "").trim();
    }

    public void setDefaultMailPassword(String val) {
        prefs.put(DEF_PASSWORD, val);
    }

    public int getDefaultDelay() {
        try {
            return Integer.parseInt(prefs.get(DEF_DELAY, "300"));
        }catch (NumberFormatException e) {
            return 300;
        }
    }

    public void setDefaultDelay(int val) {
        prefs.put(DEF_DELAY, Integer.toString(val));
    }
}
