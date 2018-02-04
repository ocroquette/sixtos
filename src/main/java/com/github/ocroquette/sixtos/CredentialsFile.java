package com.github.ocroquette.sixtos;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.*;

public class CredentialsFile {
    private final File file;

    // Length of the salt in chars
    private static final int SALT_LENGTH = 32;

    // Length of the hashes with salt in chars
    private static final int SALT_HASH_LENGTH = 72;

    static class IllegalOperation extends Exception {
        public IllegalOperation(String msg) {
            super(msg);
        }
    }

    static class UserData {
        private String saltedHash = new String();
        private List<String> roles = new ArrayList<>();
    }

    public CredentialsFile(File file) {
        this.file = file;
    }

    public static CredentialsFile with(File file) throws IOException {
        CredentialsFile credentialsFile = new CredentialsFile(file);
        return credentialsFile;
    }

    public File getFile() {
        return this.file;
    }

    public void addUser(String username, String password) throws IllegalOperation, IOException {
        addUser(username, password, new ArrayList<String>());
    }

    public void addUser(String username, String password, List<String> roles) throws IllegalOperation, IOException {
        Map<String, UserData> userMap = parse(file);
        if (userMap.containsKey(username)) {
            throw new IllegalOperation("User already exists: " + username);
        }
        UserData userData = new UserData();
        userData.saltedHash = generateSaltedHash(password);
        userData.roles = new ArrayList<>(roles);
        userMap.put(username, userData);
        writeFile(userMap);
    }

    public void updatePassword(String username, String password) throws IllegalOperation, IOException {
        Map<String, UserData> userMap = parse(file);
        if (!userMap.containsKey(username)) {
            throw new IllegalOperation("User already exists: " + username);
        }
        userMap.get(username).saltedHash = generateSaltedHash(password);
        writeFile(userMap);
    }

    public boolean validateCredentials(String username, String password) throws IOException {
        Map<String, UserData> userMap = parse(file);
        UserData userData = userMap.get(username);
        if (userData == null)
            return false;
        String saltedHash = userData.saltedHash;
        String salt;
        try {
            salt = extractSalt(saltedHash);
        } catch (IllegalOperation illegalOperation) {
            // TODO logging
            return false;
        }
        return generateSaltedHash(password, salt).equals(userData.saltedHash);
    }

    public boolean containsUser(String username) throws IOException {
        Map<String, UserData> userMap = parse(file);
        return userMap.containsKey(username);
    }

    public List<String> getRoles(String username) throws IOException {
        Map<String, UserData> userMap = parse(file);
        UserData userData = userMap.get(username);
        if (userData == null)
            return null;
        return new ArrayList<>(userData.roles);
    }

    public static String generateRandomSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateSaltedHash(String password) {
        return generateSaltedHash(password, generateRandomSalt());
    }

    public static String generateSaltedHash(String password, String salt) {
        if ( salt.length() != SALT_LENGTH ) {
            throw new RuntimeException("Invalid salt: \"" + salt + "\", expected length: " + SALT_LENGTH + " actual length: " + salt.length());
        }
        return salt + DigestUtils.sha1Hex(salt + password);
    }

    private static String extractSalt(String saltedHash) throws IllegalOperation {
        if ( saltedHash.length() != SALT_HASH_LENGTH )
            throw new IllegalOperation("Invalid salted hash: \"" + saltedHash + "\"");
        return saltedHash.substring(0, SALT_LENGTH);
    }

    private static String extractHash(String saltedHash) throws IllegalOperation {
        if ( saltedHash.length() != SALT_HASH_LENGTH )
            throw new IllegalOperation("Invalid hash: \"" + saltedHash + "\"");
        return saltedHash.substring(SALT_LENGTH);
    }

    private void writeFile(Map<String, UserData> userMap) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, UserData> entry : userMap.entrySet()) {
            sb.append(entry.getKey()); // username
            sb.append(":");
            sb.append(entry.getValue().saltedHash); // password
            sb.append(":");
            sb.append(String.join(",", entry.getValue().roles)); // roles
            sb.append("\n");
        }

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(sb.toString());
        fileWriter.close();
    }

    private static Map<String, UserData> parse(File file) throws IOException {
        TreeMap<String, UserData> userMap = new TreeMap<>();

        if ( ! file.exists() )
            return userMap;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(":");
                if (split.length < 2 || split.length > 3)
                    throw new IOException("Invalid line: \"" + line + "\"");
                UserData userData = new UserData();
                userData.saltedHash = split[1];
                if (split.length > 2)
                    userData.roles = Arrays.asList(split[2].split(","));
                userMap.put(split[0], userData);
            }

        }
        catch(IOException e) {
            if ( reader != null )
                reader.close();
            throw new IOException(e.getMessage(), e);
        }
        return userMap;
    }

    public static void main(final String[] args) throws IOException, IllegalOperation {
        if (args.length != 2) {
            System.err.println(String.format("Please provide the mandatory parameters: [file] [user]"));
            System.exit(1);
        }

        CredentialsFile credentialsFile = new CredentialsFile(new File(args[0]));

        System.out.print("Password: ");
        String password1 = new String(System.console().readPassword());
        System.out.print("Repeat password: ");
        String password2 = new String(System.console().readPassword());

        if ( ! password1.equals(password2) ) {
            System.err.println(String.format("Passwords don't match"));
            System.exit(1);
        }

        String username = args[1];
        if ( credentialsFile.containsUser(username) ) {
            credentialsFile.updatePassword(username, password1);
        }
        else {
            credentialsFile.addUser(username, password1);
        }

    }
}
