package com.github.crawler;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, ParseException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:D:/Desktop/j/project/concurrentCrawler/news");
        updateUrlFromDB(connection, "insert INTO LINK_TO_BE_PROCESSED (LINK)values (?)", "HTTPS://sina.cn");
        String linkURL;

        while ((linkURL = getUrlsFromDB(connection)) != null) {
            System.out.println(linkURL);
            updateUrlFromDB(connection, "DELETE FROM LINK_TO_BE_PROCESSED WHERE LINK = ?", linkURL);


            if (isLinkProcess(connection, linkURL)) {
                continue;
            }

            Document document = getAndParse(linkURL);

            for (Element item : document.select("a")) {
                selectNewLinkJoinList(connection, item);
            }

            storeIntoDBIfNewsPage(connection, document, linkURL);
            updateUrlFromDB(connection, "insert INTO LINK_ALREADY_PROCESSED (LINK)values (?)", linkURL);
        }
    }

    private static String getUrlsFromDB(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from LINK_TO_BE_PROCESSED");
             ResultSet result = preparedStatement.executeQuery()) {
            if (result.next()) {
                return result.getString(1);
            } else {
                return null;
            }
        }

    }

    private static Boolean isLinkProcess(Connection connection, String siteUrl) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT LINK from LINK_ALREADY_PROCESSED where LINK = ?")) {
            preparedStatement.setString(1, siteUrl);
            try (ResultSet result = preparedStatement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static void updateUrlFromDB(Connection connection, String sql, String siteUrl) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, siteUrl);
            preparedStatement.executeUpdate();
        }
    }


    private static void selectNewLinkJoinList(Connection connection, Element aTag) throws SQLException {
        if (aTag.attr("href").contains("news.sina")) {
            updateUrlFromDB(connection, "insert INTO LINK_TO_BE_PROCESSED (LINK)values (?)", aTag.attr("href"));
        }
    }


    private static void storeIntoDBIfNewsPage(Connection connection, Document document, String link) throws SQLException {
        Elements articleTagList = document.select("article");
        if (!articleTagList.isEmpty()) {
            for (Element articleTag : articleTagList) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("p")
                        .stream()
                        .map(Element::text)
                        .collect(Collectors.joining("\n"));

                try (PreparedStatement preparedStatement = connection.prepareStatement("insert INTO NEWS (url,title,content,created_at,modified_at)values(?,?,?,now(),now())")) {
                    preparedStatement.setString(1, link);
                    preparedStatement.setString(2, title);
                    preparedStatement.setString(3, content);
                    preparedStatement.executeUpdate();
                }


            }
        }
    }

    public static Document getAndParse(String url) throws IOException, ParseException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                String html = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                return Jsoup.parse(html);


            }
        }
    }
}
