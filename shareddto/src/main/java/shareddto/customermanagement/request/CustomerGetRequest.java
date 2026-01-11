package shareddto.customermanagement.request;

/**
 * Request payload for fetching a customer by id number.
 */
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
