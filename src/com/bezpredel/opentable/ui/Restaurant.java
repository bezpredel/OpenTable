package com.bezpredel.opentable.ui;

/**
* Date: 7/30/12
* Time: 6:58 PM
*/
public class Restaurant {
    private final String name;
    private final Integer id;

    Restaurant(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }
}
