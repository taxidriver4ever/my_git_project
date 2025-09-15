package org.example.tiktokproject.repository;

import org.example.tiktokproject.pojo.ESUser;
import org.example.tiktokproject.pojo.Video;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserESRepository extends ElasticsearchRepository<ESUser, String> {
    @Query("""
        {
          "match": {
            "userName": "?0"
          }
        }
        """)
    List<ESUser> findByUserName(String userName);
}
