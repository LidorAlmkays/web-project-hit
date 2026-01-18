package shareddto.customermanagement.request;

public class CustomerGetRequest {
    private String idNumber;

    public CustomerGetRequest() {
    }

    public CustomerGetRequest(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
}
