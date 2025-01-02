package pt.psoft.g1.psoftg1.readermanagement.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
//import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.services.UpdateReaderRequest;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

import java.nio.file.InvalidPathException;
import java.util.List;

@Entity
@Table(name = "READER_DETAILS")
public class ReaderDetails extends EntityWithPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

//    @Getter
//    @Setter
//    @OneToOne
//    private Reader reader;

    @Column(name = "user_id", nullable = false)
    @Setter
    @Getter
    private long userId;

    @Setter
    @Getter
    private String username;

    private ReaderNumber readerNumber;

    @Embedded
    @Getter
    private BirthDate birthDate;

    @Embedded
    private PhoneNumber phoneNumber;

    @Setter
    @Getter
    @Basic
    private boolean gdprConsent;

    @Setter
    @Basic
    @Getter
    private boolean marketingConsent;

    @Setter
    @Basic
    @Getter
    private boolean thirdPartySharingConsent;

    @Version
    @Getter
    private Long version;

//    @Getter
//    @Setter
//    @ManyToMany
//    private List<Genre> interestList;

    public ReaderDetails(int readerNumber, /*Reader reader,*/ String birthDate, String phoneNumber, boolean gdpr, boolean marketing, boolean thirdParty, String photoURI/*, List<Genre> interestList*/, String username, long userId) {
//        if(reader == null || phoneNumber == null) {
//            throw new IllegalArgumentException("Provided argument resolves to null object");
//        }

        if(!gdpr) {
            throw new IllegalArgumentException("Readers must agree with the GDPR rules");
        }

        //setReader(reader);
        setReaderNumber(new ReaderNumber(readerNumber));
        setPhoneNumber(new PhoneNumber(phoneNumber));
        setBirthDate(new BirthDate(birthDate));
        //By the client specifications, gdpr can only have the value of true. A setter will be created anyways in case we have accept no gdpr consent later on the project
        setGdprConsent(true);

        setPhotoInternal(photoURI);
        setMarketingConsent(marketing);
        setThirdPartySharingConsent(thirdParty);
        setUsername(username);
        setUserId(userId);
        //setInterestList(interestList);
    }

    public ReaderDetails(String readerNumber, /*Reader reader,*/ String birthDate, String phoneNumber, boolean gdpr, boolean marketing, boolean thirdParty, String photoURI/*, List<Genre> interestList*/, String username) {
        setReaderNumber(new ReaderNumber(readerNumber));
        setPhoneNumber(new PhoneNumber(phoneNumber));
        setBirthDate(new BirthDate(birthDate));
        setGdprConsent(true);
        setPhotoInternal(photoURI);
        setMarketingConsent(marketing);
        setThirdPartySharingConsent(thirdParty);
        setUsername(username);
        //setInterestList(interestList);
    }

    private void setPhoneNumber(PhoneNumber number) {
        if(number != null) {
            this.phoneNumber = number;
        }
    }

    private void setReaderNumber(ReaderNumber readerNumber) {
        if(readerNumber != null) {
            this.readerNumber = readerNumber;
        }
    }

    private void setBirthDate(BirthDate date) {
        if(date != null) {
            this.birthDate = date;
        }
    }

    public void applyPatch(ReaderViewAMQP readerViewAMQP) {
        if(readerViewAMQP.getVersion() != this.version) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }

        String birthDate = readerViewAMQP.getBirthDate();
        String phoneNumber = readerViewAMQP.getPhoneNumber();
        boolean marketing = readerViewAMQP.isMarketing();
        boolean thirdParty = readerViewAMQP.isThirdParty();
        String fullName = readerViewAMQP.getFullName();
        String username = readerViewAMQP.getUsername();
        String password = readerViewAMQP.getPassword();
        String photoURI = readerViewAMQP.getPhotoURI();

        if(username != null) {
            this.setUsername(username);
        }


        if(birthDate != null) {
            setBirthDate(new BirthDate(birthDate));
        }

        if(phoneNumber != null) {
            setPhoneNumber(new PhoneNumber(phoneNumber));
        }

        if(marketing != this.marketingConsent) {
            setMarketingConsent(marketing);
        }

        if(thirdParty != this.thirdPartySharingConsent) {
            setThirdPartySharingConsent(thirdParty);
        }

        if(photoURI != null) {
            try {
                setPhotoInternal(photoURI);
            } catch(InvalidPathException ignored) {}
        }

//        if(interestList != null) {
//            this.interestList = interestList;
//        }
    }

    public void removePhoto(long desiredVersion) {
        if(desiredVersion != this.version) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }

        setPhotoInternal(null);
    }

    public String getReaderNumber(){
        return this.readerNumber.toString();
    }

    public String getPhoneNumber() { return this.phoneNumber.toString();}

    protected ReaderDetails() {
        // for ORM only
    }
}
