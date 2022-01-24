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

public class Crawler {
    JDBCCrawlerDao dao = new JDBCCrawlerDao();

    public static void main(String[] args) throws ParseException, SQLException, IOException {
        new Crawler().run();
    }

    public void run() throws IOException, ParseException, SQLException {
        String linkURL;

        while ((linkURL = dao.getUrlsFromDB()) != null) {
            System.out.println(linkURL);
            dao.updateUrlFromDB("DELETE FROM LINK_TO_BE_PROCESSED WHERE LINK = ?", linkURL);


            if (dao.isLinkProcess(linkURL)) {
                continue;
            }

            Document document = getAndParse(linkURL);

            for (Element item : document.select("a")) {
                selectNewLinkJoinList(item);
            }

            storeIntoDBIfNewsPage(document, linkURL);
            dao.updateUrlFromDB("insert INTO LINK_ALREADY_PROCESSED (LINK)values (?)", linkURL);
        }
    }


    private void selectNewLinkJoinList(Element aTag) throws SQLException {
        if (aTag.attr("href").contains("news.sina")) {
            dao.updateUrlFromDB("insert INTO LINK_TO_BE_PROCESSED (LINK)values (?)", aTag.attr("href"));
        }
    }


    private void storeIntoDBIfNewsPage(Document document, String link) throws SQLException {
        Elements articleTagList = document.select("article");
        if (!articleTagList.isEmpty()) {
            for (Element articleTag : articleTagList) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("p")
                        .stream()
                        .map(Element::text)
                        .collect(Collectors.joining("\n"));
                dao.insertUrlIntoDB(link, title, content);


            }
        }
    }

    public Document getAndParse(String url) throws IOException, ParseException {
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
