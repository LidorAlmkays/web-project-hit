package frontend.cli.shared;

import java.util.Locale;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static boolean isValidEmail(String value) {
        if (value == null) {
            return false;
        }
        String email = value.trim();
        if (email.isEmpty()) {
            return false;
        }
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@')) {
            return false;
        }
        int dot = email.lastIndexOf('.');
        return dot > at + 1 && dot < email.length() - 1;
    }

    public static boolean isValidPhoneDigits(String value) {
        if (value == null) {
            return false;
        }
        String phone = value.trim();
        if (!isDigitsOnly(phone)) {
            return false;
        }
        return phone.length() == 9 || phone.length() == 10;
    }

    public static boolean isValidIsraeliId(String value) {
        if (value == null) {
            return false;
        }
        String id = value.trim();
        if (!isDigitsOnly(id)) {
            return false;
        }
        if (id.length() < 5 || id.length() > 9) {
            return false;
        }
        String padded = String.format(Locale.ROOT, "%9s", id).replace(' ', '0');
        int sum = 0;
        for (int i = 0; i < padded.length(); i++) {
            int digit = padded.charAt(i) - '0';
            int multiplier = (i % 2) + 1;
            int product = digit * multiplier;
            sum += (product > 9) ? product - 9 : product;
        }
        return sum % 10 == 0;
    }

    public static boolean isDigitsOnly(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
