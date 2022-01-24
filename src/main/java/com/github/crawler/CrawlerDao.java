package com.github.crawler;


import java.sql.SQLException;

public interface CrawlerDao {
    void insertUrlIntoDB(String link, String title, String content) throws SQLException;

    void updateUrlFromDB(String sql, String siteUrl) throws SQLException;

    String getUrlsFromDB() throws SQLException;

    Boolean isLinkProcess(String link) throws SQLException;
}
