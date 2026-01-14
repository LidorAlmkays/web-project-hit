package server.domain;

public class PasswordSettings {
    private boolean passwordlength8;
    private boolean oneUpperletter;
    private boolean oneNumber;

    public PasswordSettings(boolean passwordlength8, boolean oneUpperletter, boolean oneNumber) {
        this.passwordlength8 = passwordlength8;
        this.oneUpperletter = oneUpperletter;
        this.oneNumber = oneNumber;
    }

    public boolean isPasswordlength8() {
        return passwordlength8;
    }

    public void setPasswordlength8(boolean passwordlength8) {
        this.passwordlength8 = passwordlength8;
    }

    public boolean isOneUpperletter() {
        return oneUpperletter;
    }

    public void setOneUpperletter(boolean oneUpperletter) {
        this.oneUpperletter = oneUpperletter;
    }

    public boolean isOneNumber() {
        return oneNumber;
    }

    public void setOneNumber(boolean oneNumber) {
        this.oneNumber = oneNumber;
    }

    public PasswordSettings createCopy() {
        return new PasswordSettings(this.passwordlength8, this.oneUpperletter, this.oneNumber);
    }

    @Override
    public String toString() {
        return "PasswordSettings{" +
                "passwordlength8=" + passwordlength8 +
                ", oneUpperletter=" + oneUpperletter +
                ", oneNumber=" + oneNumber +
                '}';
    }
}
