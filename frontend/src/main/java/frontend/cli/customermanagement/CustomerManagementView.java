package frontend.cli.customermanagement;

import frontend.cli.shared.BaseManagementView;
import shareddto.customermanagement.response.CustomerDto;

public class CustomerManagementView extends BaseManagementView {

    public void menu() {
        section("Main Menu");
        System.out.println("1. Add customer");
        System.out.println("2. Get customer (by ID number)");
        System.out.println("3. Exit");
        System.out.print("Choose: ");
    }

    public void printCustomer(CustomerDto customer) {
        if (customer == null) {
            System.out.println("No data.");
            return;
        }
        System.out.println(toJson(customer));
    }
}
