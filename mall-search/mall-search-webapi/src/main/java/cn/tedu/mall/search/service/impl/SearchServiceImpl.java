package cn.tedu.mall.search.service.impl;

import cn.tedu.mall.common.restful.JsonPage;
import cn.tedu.mall.pojo.product.model.Spu;
import cn.tedu.mall.pojo.search.entity.SpuEntity;
import cn.tedu.mall.pojo.search.entity.SpuForElastic;
import cn.tedu.mall.product.service.front.IForFrontSpuService;
import cn.tedu.mall.search.repository.SpuForElasticRepository;
import cn.tedu.mall.search.service.ISearchService;

import org.springframework.data.domain.Page;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements  ISearchService{
    @Autowired
    private SpuForElasticRepository spuForElasticRepository;
    @DubboReference
    private IForFrontSpuService dubboSpuService;

    @Override
    public JsonPage<SpuForElastic> search(String keyword, Integer page, Integer pageSize) {
        Page<SpuForElastic> spus = spuForElasticRepository.querySearch(keyword, PageRequest.of(page - 1, pageSize));
        JsonPage<SpuForElastic> jsonPage = new JsonPage<>();
        jsonPage.setPage(page);
        jsonPage.setPageSize(pageSize);
        jsonPage.setTotal(spus.getTotalElements());
        jsonPage.setTotalPage(spus.getTotalPages());
        jsonPage.setList(spus.getContent());
        return jsonPage;
    }


    @Override
    public void loadSpuByPage() {
        int i = 1;
        int pages = 0;
        do {
            JsonPage<Spu> spuByPage = dubboSpuService.getSpuByPage(i, 2);
            List<SpuForElastic> elasticList = new ArrayList<>();
            for (Spu spu : spuByPage.getList()) {
                SpuForElastic spuForElastic = new SpuForElastic();
                BeanUtils.copyProperties(spu,spuForElastic);
                elasticList.add(spuForElastic);
            }
            spuForElasticRepository.saveAll(elasticList);
            pages = spuByPage.getTotalPage();
            i++;
        } while (i <= pages);
    }
}
