package cn.tedu.mall.search.repository;

import cn.tedu.mall.pojo.search.entity.SpuEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpuForEntityRepository extends ElasticsearchRepository<SpuEntity,Long> {

    @Query("{\"match\":{\"search_text\":{\"query\":\"?0\"}}}")
    Page<SpuEntity> queryByText(String keyword, Pageable pageable);
}
