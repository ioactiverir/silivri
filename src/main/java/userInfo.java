import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "Users",uniqueConstraints = {
        @UniqueConstraint(columnNames = "id")})

public class userInfo implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,updatable = false)
    private int id;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "userFirstName")
    private String userFirstName;

    @Column(name = "userLastName")
    private String userLastName;

    @Column(name = "bankNo")
    private String bankNo;

    @Column(name = "userMail")
    private String userMail;

    @Column(name = "userCreditValue")
    private int userCreditValue;

    @Column(name = "userGiftValue")
    private int userGiftValue;


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public int getUserCreditValue() {
        return userCreditValue;
    }

    public void setUserCreditValue(int userCreditValue) {
        this.userCreditValue = userCreditValue;
    }

    public int getUserGiftValue() {
        return userGiftValue;
    }

    public void setUserGiftValue(int userGiftValue) {
        this.userGiftValue = userGiftValue;
    }

    public int getUserId() {
        return userId;
    }

    private int userId;

    public void setUserId(int userId) {
        this.userId = id;
    }


}




/*
    create table userInfo (
            userId INT NOT NULL auto_increment,
            phoneNumber VARCHAR(20) default NULL,
            userFirstName VARCHAR(20) default NULL,
            userLastName VARCHAR(20) default NULL,
            bankNo VARCHAR(20) default NULL,
            userMail VARCHAR(40)  default NULL,
            userCreditValue INT default 0,
            userGiftValue INT default 0,
            PRIMARY KEY (userId) );
    */