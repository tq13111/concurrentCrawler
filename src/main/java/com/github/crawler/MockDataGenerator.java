package com.github.crawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {


    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory = null;

        String resource = "db/mybatis/config.xml";
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mockData(sqlSessionFactory, 1000000);

    }

    private static void mockData(SqlSessionFactory sqlSessionFactory, int targetRowCount) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> news = session.selectList("db.mybatis.MockMapper.selectNews");

            int count = targetRowCount - news.size();
            Random random = new Random();
            while (count-- > 0) {
                int index = random.nextInt(news.size());

                News newsTobeInserted = news.get(index);
                Instant currentTime = newsTobeInserted.getCreatedAt();
                currentTime = currentTime.minusSeconds(random.nextInt(3600 * 24 ));
                newsTobeInserted.setCreatedAt(currentTime);
                newsTobeInserted.setModifiedAt(currentTime);
                session.insert("db.mybatis.MockMapper.insertNews", newsTobeInserted);

                if (count % 2000 == 0) {
                    session.flushStatements();

                }
                System.out.println("还剩：" + count + "个");
            }
            session.commit();

        }
    }


}
