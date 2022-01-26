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
import java.util.stream.Collectors;

public class Crawler extends Thread {
    MybatisCrawlerDao dao;

    public Crawler(MybatisCrawlerDao dao) {
        this.dao = dao;
    }


    @Override
    public void run() {
        String linkURL;

        while ((linkURL = dao.getLinkThenDelete()) != null) {
            System.out.println(linkURL);


            if (dao.isLinkProcess(linkURL)) {
                continue;
            }

            try {
                Document document = getAndParse(linkURL);
                for (Element item : document.select("a")) {
                    selectNewLinkJoinList(item);
                }

                storeIntoDBIfNewsPage(document, linkURL);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }


            dao.insertProcessedLink(linkURL);
        }
    }

    private void selectNewLinkJoinList(Element aTag) {
        if (aTag.attr("href").contains("news.sina")) {
            dao.insert_to_be_processed(aTag.attr("href"));
        }
    }

    private void storeIntoDBIfNewsPage(Document document, String link) {
        Elements articleTagList = document.select("article");
        if (!articleTagList.isEmpty()) {
            for (Element articleTag : articleTagList) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("p")
                        .stream()
                        .map(Element::text)
                        .collect(Collectors.joining("\n"));
                dao.insertNewsIntoDB(link, title, content);
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
