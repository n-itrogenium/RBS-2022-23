package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.SecurityUtil;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class UserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(UserRepository.class);
    private DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findUser(String username) {
        String query = "SELECT id, username, password FROM users WHERE username='" + username + "'";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                int id = rs.getInt(1);
                String username1 = rs.getString(2);
                String password = rs.getString(3);
                LOG.info("User " + username + " successfully found");
                return new User(id, username1, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG.warn("User " + username + " not found", e);
        }
        return null;
    }

    public boolean validCredentials(String username, String password) {
        String query = "SELECT username FROM users WHERE username='" + username + "' AND password='" + password + "'";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            LOG.info("Credential validation for user " + username + " successful");
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            LOG.warn("Credential validation for user " + username + " failed", e);
        }
        return false;
    }

    public void delete(int userId) {
        String query = "DELETE FROM users WHERE id = " + userId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);
            AuditLogger.getAuditLogger(UserRepository.class).audit("Deleting user " + userId + " by '"
                    + SecurityUtil.getCurrentUser().getUsername() + "' successful");
        } catch (SQLException e) {
            e.printStackTrace();
            AuditLogger.getAuditLogger(UserRepository.class).audit("Deleting user " + userId + " by '"
                    + SecurityUtil.getCurrentUser().getUsername() + "' failed");
        }
    }
}
