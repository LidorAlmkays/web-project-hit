package server.config;

public class Config {
    public static final String DATA_DIR = "./data";
    public static final String BRANCH_INVENTORY_ITEMS_DIR = DATA_DIR + "/branch_inventory_items";
    public static final String CUSTOMERS_DIR = DATA_DIR + "/customers";
    public static final String BRANCHES_DIR = DATA_DIR + "/branches";
    public static final String EMPLOYEES_DIR = DATA_DIR + "/employees";
    public static final String LOGS_DIR = DATA_DIR + "/logs";
    public static final String LOG_FILE_PATH = "./logs/server.log";
    public static final int SOCKET_PORT = 8080;

    private Config() {
    }
}
