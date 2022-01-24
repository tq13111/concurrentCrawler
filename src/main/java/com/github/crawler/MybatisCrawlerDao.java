package com.github.crawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MybatisCrawlerDao implements CrawlerDao {
    SqlSessionFactory sqlSessionFactory;

    public MybatisCrawlerDao() {
        String resource = "db/mybatis/config.xml";
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertProcessedLink(String link) {
        Map<String, String> map = new HashMap<>();
        map.put("tableName", "LINK_ALREADY_PROCESSED");
        map.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.selectOne("db.mybatis.MyMapper.insertLink", map);
        }
    }

    @Override
    public void insert_to_be_processed(String link) {
        Map<String, String> map = new HashMap<>();
        map.put("tableName", "LINK_TO_BE_PROCESSED");
        map.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.selectOne("db.mybatis.MyMapper.insertLink", map);
        }
    }

    @Override
    public void insertNewsIntoDB(String link, String title, String content) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.selectOne("db.mybatis.MyMapper.insertNews", new News(link, title, content));
        }
    }

    @Override
    public void deleteLinkFromDB(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.selectOne("db.mybatis.MyMapper.deleteLink", link);
        }
    }

    @Override
    public String getLinksFromDB() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectOne("db.mybatis.MyMapper.selectNextLink");
        }
    }

    @Override
    public Boolean isLinkProcess(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("db.mybatis.MyMapper.countLink");
            return count != 0;
        }
    }
}
