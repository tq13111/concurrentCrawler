package com.github.crawler;

public class Main {

    public static void main(String[] args) {
        MybatisCrawlerDao dao = new MybatisCrawlerDao();

        for (int i = 0; i <200; i++) {
            new Crawler(dao).start();
        }
    }
}
