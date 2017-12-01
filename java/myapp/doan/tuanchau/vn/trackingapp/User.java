package myapp.doan.tuanchau.vn.trackingapp;

/**
 * Created by tuanchau on 11/5/17.
 */

public class User {
    private String email,status,phonenumber;

    public User(String email, String status, String phonenumber) {
        this.email = email;
        this.status = status;
        this.phonenumber = phonenumber;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
