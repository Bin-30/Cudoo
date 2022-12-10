package cn.tedu.mall.search.service.impl;

import cn.tedu.mall.common.restful.JsonPage;
import cn.tedu.mall.pojo.search.entity.SpuEntity;
import cn.tedu.mall.search.repository.SpuForEntityRepository;
import cn.tedu.mall.search.service.ISearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;



@Deprecated
public class SearchRemoteServiceImpl{
    @Autowired
    private SpuForEntityRepository entityRepository;

   /** @Override
    public JsonPage<SpuEntity> search(String keyword, Integer page, Integer pageSize) {
        Page<SpuEntity> spuEntities = entityRepository.queryByText(keyword, PageRequest.of(page-1, pageSize));
        JsonPage<SpuEntity> jsonPage = new JsonPage<>();
        jsonPage.setPage(page);
        jsonPage.setPageSize(pageSize);
        jsonPage.setTotalPage(spuEntities.getTotalPages());
        jsonPage.setTotal(spuEntities.getTotalElements());
        jsonPage.setList(spuEntities.getContent());
        return jsonPage;
    }

    @Override
    public void loadSpuByPage() {

    }*/
}
