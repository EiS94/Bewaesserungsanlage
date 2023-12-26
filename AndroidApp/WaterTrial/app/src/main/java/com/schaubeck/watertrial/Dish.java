package com.schaubeck.watertrial;

import androidx.annotation.NonNull;

public class Dish {

    String name, url, zutaten;
    boolean veggy;

    public Dish(String name, boolean veggy) {
        this.name = name;
        this.veggy = veggy;
    }

    /*
    public Dish(String name, String url, String zutaten, boolean veggy) {
        this.name = name;
        this.url = url;
        this.zutaten = zutaten;
        this.veggy = veggy;
    }
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getZutaten() {
        return zutaten;
    }

    public void setZutaten(String zutaten) {
        this.zutaten = zutaten;
    }

    public boolean isVeggy() {
        return veggy;
    }

    /*
    public void setVeggy(boolean veggy) {
        this.veggy = veggy;
    }
     */

    @NonNull
    @Override
    public String toString() {
        if (veggy) return name + ", vegetarisch: ja";
        else return name + ", vegetarisch: nein";
    }
}
