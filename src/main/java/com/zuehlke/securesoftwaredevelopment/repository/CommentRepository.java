package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CommentRepository {

    private static final Logger LOG = LoggerFactory.getLogger(CommentRepository.class);


    private DataSource dataSource;

    public CommentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void create(Comment comment) {
        String query = "insert into comments(movieId, userId, comment) values (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setInt(1, comment.getMovieId());
            statement.setInt(2, comment.getUserId());
            statement.setString(3, comment.getComment());
            statement.executeUpdate();
            LOG.info("Comment on movie " + comment.getMovieId() + " successfully added by user " + comment.getUserId());
        } catch (SQLException e) {
            e.printStackTrace();
            LOG.warn("Comment adding by user " + comment.getUserId() + " failed on movie " + comment.getMovieId(), e);
        }
    }

    public List<Comment> getAll(String movieId) {
        List<Comment> commentList = new ArrayList<>();
        String query = "SELECT movieId, userId, comment FROM comments WHERE movieId = " + movieId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                commentList.add(new Comment(rs.getInt(1), rs.getInt(2), rs.getString(3)));
            }
            LOG.info("Comments successfully fetched on movie " + movieId);
        } catch (SQLException e) {
            e.printStackTrace();
            LOG.warn("Comment fetch failed on movie " + movieId, e);
        }
        return commentList;
    }
}