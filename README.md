# Web Project Hit

## Requriments

- **Java 11 or higher** - Make sure Java is installed on your computer
  - Check by running: `java -version` in your terminal

## First Time Setup

1. **Open a terminal** in the project folder (`web-project-hit`)
2. **Build everything** - This downloads dependencies and compiles all modules:

   On Windows:

   ```bash
   .\mvnw.cmd clean install
   ```

   On Mac/Linux:

   ```bash
   ./mvnw clean install
   ```

   This might take a minute the first time as it downloads libraries from the internet. Don't worry, that's normal!
3. **That's it!** You're all set.

## Running the Projects

### Start the Server

Open a terminal and run:

**Windows:**

```bash
.\mvnw.cmd -pl server exec:java
```

**Mac/Linux:**

```bash
./mvnw -pl server exec:java
```

The server will start and wait for connections. You'll see messages like "Starting application" when it's ready.

### Run the Frontend (Client)

Open a **different terminal** (keep the server running!) and run:

**Windows:**

```bash
.\mvnw.cmd -pl frontend exec:java
```

**Mac/Linux:**

```bash
./mvnw -pl frontend exec:java
```

The frontend will connect to the server and you can interact with it.
