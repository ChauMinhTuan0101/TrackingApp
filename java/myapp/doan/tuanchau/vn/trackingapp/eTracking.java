package myapp.doan.tuanchau.vn.trackingapp;

/**
 * Created by tuanchau on 11/21/17.
 */

public class eTracking {

    String imei;
    String date;
    String lat;
    String lng;
    String name;

    public eTracking(String name, String imei, String date, String lat, String lng) {
        this.imei = imei;
        this.date = date;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public eTracking() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
