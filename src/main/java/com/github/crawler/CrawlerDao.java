package com.github.crawler;


import java.sql.SQLException;

public interface CrawlerDao {
    void insertProcessedLink(String link) throws SQLException;

    void insert_to_be_processed(String link) throws SQLException;

    void insertNewsIntoDB(String link, String title, String content) throws SQLException;


    String getLinkThenDelete() throws SQLException;

    Boolean isLinkProcess(String link) throws SQLException;


}
