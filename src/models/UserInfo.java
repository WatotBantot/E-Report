package models;

public class UserInfo {
    private String fName, mName, lName, sex, contact, email, houseNum, street, purok;
    private int UI_ID;

    public UserInfo(int UI_ID, String fName, String mName, String lName, String sex, String contact, String email,
            String houseNum,
            String street, String purok) {
        this.fName = fName;
        this.mName = mName;
        this.lName = lName;
        this.sex = sex;
        this.contact = contact;
        this.email = email;
        this.street = street;
        this.purok = purok;
        this.houseNum = houseNum;
    }

    public UserInfo() {
    };

    public int getUI_ID() {
        return UI_ID;
    }

    public void setUI_ID(int UI_ID) {
        this.UI_ID = UI_ID;
    }

    public String getFName() {
        return fName;
    }

    public void setFName(String fName) {
        this.fName = fName;
    }

    public String getMName() {
        return mName;
    }

    public void setMName(String mName) {
        this.mName = mName;
    }

    public String getLName() {
        return lName;
    }

    public void setLName(String lName) {
        this.lName = lName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHouseNum() {
        return houseNum;
    }

    public void setHouseNum(String houseNum) {
        this.houseNum = houseNum;
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

}
