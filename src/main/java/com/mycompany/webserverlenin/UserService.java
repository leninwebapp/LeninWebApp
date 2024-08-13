

package com.mycompany.webserverlenin;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final MangoDBConnection mangoDBConnection;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(MangoDBConnection mangoDBConnection, PasswordEncoder passwordEncoder) {
        this.mangoDBConnection = mangoDBConnection;
        this.passwordEncoder = passwordEncoder;
    }

    public void saveUser(String username, String name, String email, String rawPassword, String role) throws Exception {
        MongoCollection<Document> collection = mangoDBConnection.getConfiguration();
        Document existingUser = collection.find(new Document("user_name", username)).first();
        if (existingUser != null) {
            throw new Exception("Username already exists.");
        }

        String encryptedPassword = passwordEncoder.encode(rawPassword);
        Document user = new Document("user_name", username)
                .append("name", name)
                .append("email", email)
                .append("password", encryptedPassword)
                .append("role", role);
        collection.insertOne(user);
    }

    public boolean authenticate(String username, String rawPassword) {
        MongoCollection<Document> collection = mangoDBConnection.getConfiguration();
        Document user = collection.find(new Document("user_name", username)).first();
        if (user != null) {
            String storedPassword = user.getString("password");
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) {
        MongoCollection<Document> collection = mangoDBConnection.getConfiguration();
        Document user = collection.find(new Document("user_name", username)).first();
        if (user != null) {
            String encryptedPassword = passwordEncoder.encode(newPassword);
            Document update = new Document("$set", new Document("password", encryptedPassword));
            collection.updateOne(new Document("user_name", username), update);
            return true;
        }
        System.err.println("User not found: " + username);
        return false;
    }

    public String getUserRole(String username) {
        MongoCollection<Document> collection = mangoDBConnection.getConfiguration();
        Document user = collection.find(new Document("user_name", username)).first();
        if (user != null) {
            return user.getString("role");
        }
        return null; // or a default role
    }
    
    public String getUserName(String username) {
        MongoCollection<Document> collection = mangoDBConnection.getConfiguration();
        Document user = collection.find(new Document("user_name", username)).first();
        return user.getString("name");

    }
}
