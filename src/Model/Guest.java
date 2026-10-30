package Model;

public class Guest extends User {
    private String idProofType;
    private String idProofNumber;

    public Guest(String id, String name, String email, String phone,
                 String idProofType, String idProofNumber) {
        super(id, name, email, phone);
        this.idProofType = idProofType;
        this.idProofNumber = idProofNumber;
    }

    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }
    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }
}
