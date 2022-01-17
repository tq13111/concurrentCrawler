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
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String SINA_URL = "https://sina.cn/";
    private static final List<String> siteList = new ArrayList<>();
    private static final List<String> usedList = new ArrayList<>();

    public static void main(String[] args) throws IOException, ParseException {

        siteList.add(SINA_URL);

        while (siteList.size() > 0) {
            String siteUrl = siteList.remove(0);
            if (usedList.contains(siteUrl)) {
                continue;
            }


            Document document = getAndParse(siteUrl);
            document.select("a").forEach(Main::selectNewSiteJoinList);

            storeIntoDBIfNewsPage(document);
            usedList.add(siteUrl);
        }
    }

    private static void selectNewSiteJoinList(Element aTag) {
        if (aTag.attr("href").contains("news.sina")) {
            siteList.add(aTag.attr("href"));
        }
    }

    private static void storeIntoDBIfNewsPage(Document document) {
        Elements articleTagList = document.select("article");
        if (!articleTagList.isEmpty()) {
            for (Element articleTag : articleTagList) {
                System.out.println(articleTag.child(0).text());
            }
        }
    }

    public static Document getAndParse(String url) throws IOException, ParseException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                System.out.println(url);
                HttpEntity entity = response.getEntity();
                EntityUtils.consume(entity);
                return Jsoup.parse(EntityUtils.toString(entity));
            }
        }
    }
}
