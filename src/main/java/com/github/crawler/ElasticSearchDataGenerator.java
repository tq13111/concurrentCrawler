package com.github.crawler;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ElasticSearchDataGenerator {
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory = null;

        String resource = "db/mybatis/config.xml";
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<News> news = getNewsFromMysql(sqlSessionFactory);

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
    }

    public static List<News> getNewsFromMysql(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
           return session.selectOne("db.mybatis.MockMapper.selectNews");
        }
    }
}
