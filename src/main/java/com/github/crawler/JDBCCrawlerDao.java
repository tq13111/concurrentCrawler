package com.github.crawler;

import java.sql.*;

public class JDBCCrawlerDao implements CrawlerDao {
    Connection connection;

    public JDBCCrawlerDao() {
        try {
            connection = DriverManager.getConnection("jdbc:h2:file:D:/Desktop/j/project/concurrentCrawler/news");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


    public void insertUrlIntoDB(String link, String title, String content) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert INTO NEWS (url,title,content,created_at,modified_at)values(?,?,?,now(),now())")) {
            preparedStatement.setString(1, link);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, content);
            preparedStatement.executeUpdate();
        }
    }

    public void updateUrlFromDB(String sql, String siteUrl) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, siteUrl);
            preparedStatement.executeUpdate();
        }
    }

    public String getUrlsFromDB() throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from LINK_TO_BE_PROCESSED");
             ResultSet result = preparedStatement.executeQuery()) {
            if (result.next()) {
                return result.getString(1);
            } else {
                return null;
            }
        }

    }

    public Boolean isLinkProcess(String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT LINK from LINK_ALREADY_PROCESSED where LINK = ?")) {
            preparedStatement.setString(1, link);
            try (ResultSet result = preparedStatement.executeQuery()) {
                return result.next();
            }
        }
    }
}
