package myapp.doan.tuanchau.vn.trackingapp;

/**
 * Created by tuanchau on 11/5/17.
 */

public class MapTracking {
    private String email,uid,lat,lng,phonenumber;

    public MapTracking(String email, String uid, String lat, String lng, String phonenumber) {
        this.email = email;
        this.uid = uid;
        this.lat = lat;
        this.lng = lng;
        this.phonenumber = phonenumber;
    }

    public MapTracking() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
