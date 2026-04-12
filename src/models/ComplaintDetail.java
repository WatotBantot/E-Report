package models;

import java.sql.Timestamp;

public class ComplaintDetail {
    private String currentStatus, subject, type, street, purok, personsInvolved, details;
    private String photoAttachment;
    private Timestamp dateTime;
    private double longitude, latitude;

    public ComplaintDetail(String currentStatus, String subject, String type, Timestamp dateTime, double longitude,
            double latitude, String street, String purok, String personsInvolved, String details,
            String photoAttachment) {

        this.currentStatus = currentStatus;
        this.subject = subject;
        this.type = type;
        this.dateTime = dateTime;
        this.longitude = longitude;
        this.latitude = latitude;
        this.street = street;
        this.purok = purok;
        this.personsInvolved = personsInvolved;
        this.details = details;
        this.photoAttachment = photoAttachment;
    }

    public ComplaintDetail() {
    };

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPurok() {
        return purok;
    }

    public void setPurok(String purok) {
        this.purok = purok;
    }

    public String getPersonsInvolved() {
        return personsInvolved;
    }

    public void setPersonsInvolved(String personsInvolved) {
        this.personsInvolved = personsInvolved;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPhotoAttachment() {
        return photoAttachment;
    }

    public void setPhotoAttachment(String photoAttachment) {
        this.photoAttachment = photoAttachment;
    }

}
