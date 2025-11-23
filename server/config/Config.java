package server.config;

public class Config {
    public static final String DATA_DIR = "./data";
    public static final String USER_ACCOUNTS_DIR = DATA_DIR + "/user_accounts";
    public static final String CHAT_ROOMS_DIR = DATA_DIR + "/chat_rooms";
    public static final String INVENTORY_ITEMS_DIR = DATA_DIR + "/inventory_items";
    public static final String BRANCH_INVENTORY_ITEMS_DIR = DATA_DIR + "/branch_inventory_items";
    public static final String CUSTOMERS_DIR = DATA_DIR + "/customers";
    public static final String LOG_FILE_PATH = "./logs/server.log";
    public static final int PORT = 7000;
    public static final String HOST = "localhost";

    private Config() {
    }

    public static String getDataDir() {
        return DATA_DIR;
    }

    public static String getUserAccountsDir() {
        return USER_ACCOUNTS_DIR;
    }

    public static String getChatRoomsDir() {
        return CHAT_ROOMS_DIR;
    }

    public static String getInventoryItemsDir() {
        return INVENTORY_ITEMS_DIR;
    }

    public static String getLogFilePath() {
        return LOG_FILE_PATH;
    }

    public static String getBranchInventoryItemsDir() {
        return BRANCH_INVENTORY_ITEMS_DIR;
    }

    public static String getCustomersDir() {
        return CUSTOMERS_DIR;
    }
}

