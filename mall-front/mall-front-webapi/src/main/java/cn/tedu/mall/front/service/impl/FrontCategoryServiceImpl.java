package cn.tedu.mall.front.service.impl;

import cn.tedu.mall.common.exception.CoolSharkServiceException;
import cn.tedu.mall.common.restful.ResponseCode;
import cn.tedu.mall.front.service.IFrontCategoryService;
import cn.tedu.mall.pojo.front.entity.FrontCategoryEntity;
import cn.tedu.mall.pojo.front.vo.FrontCategoryTreeVO;
import cn.tedu.mall.pojo.product.vo.CategoryStandardVO;
import cn.tedu.mall.product.service.front.IForFrontCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FrontCategoryServiceImpl implements IFrontCategoryService {
    public static final String CATEGORY_TREE_KEY = "category_tree";

    @DubboReference
    private IForFrontCategoryService dubboCategoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public FrontCategoryTreeVO categoryTree() {
        if (redisTemplate.hasKey(CATEGORY_TREE_KEY)){
            FrontCategoryTreeVO<FrontCategoryEntity> treeVO =
                    (FrontCategoryTreeVO<FrontCategoryEntity>) redisTemplate.boundValueOps(CATEGORY_TREE_KEY).get();
            return treeVO;
        }
        List<CategoryStandardVO> categoryList = dubboCategoryService.getCategoryList();
        FrontCategoryTreeVO<FrontCategoryEntity> treeVO = initTree(categoryList);
        redisTemplate.boundValueOps(CATEGORY_TREE_KEY).set(treeVO,1, TimeUnit.MINUTES);
        return treeVO;
    }

    private FrontCategoryTreeVO<FrontCategoryEntity> initTree(List<CategoryStandardVO> categoryList) {
        Map<Long,List<FrontCategoryEntity>> map = new HashMap<>();
        for (CategoryStandardVO categoryStandardVO : categoryList) {
            Long parentId = categoryStandardVO.getParentId();
            FrontCategoryEntity frontCategoryEntity = new FrontCategoryEntity();
            BeanUtils.copyProperties(categoryStandardVO,frontCategoryEntity);
            if (map.containsKey(parentId)){
                map.get(parentId).add(frontCategoryEntity);
            }else {
                List<FrontCategoryEntity> list = new ArrayList<>();
                list.add(frontCategoryEntity);
                map.put(parentId,list);
            }
        }
        List<FrontCategoryEntity> oneLevel = map.get(0L);
        if (oneLevel==null||oneLevel.isEmpty()){
            throw new CoolSharkServiceException(ResponseCode.INTERNAL_SERVER_ERROR,"当前数据没有根分类");
        }
        for (FrontCategoryEntity first : oneLevel) {
            Long secondLevelPId = first.getId();
            List<FrontCategoryEntity> twoLevel = map.get(secondLevelPId);
            if (twoLevel==null||twoLevel.isEmpty()){
                log.debug("此一级类别没有二级类别{}",secondLevelPId);
                continue;
            }
            for (FrontCategoryEntity second : twoLevel) {
                Long thirdLevelPId = second.getId();
                List<FrontCategoryEntity> threeLevel = map.get(thirdLevelPId);
                if (threeLevel==null||threeLevel.isEmpty()){
                    log.debug("此二级类别没有三级类别{}", thirdLevelPId);
                    continue;
                }
                second.setChildrens(threeLevel);
            }
            first.setChildrens(twoLevel);
        }
        FrontCategoryTreeVO<FrontCategoryEntity> treeVO = new FrontCategoryTreeVO<>();
        treeVO.setCategories(oneLevel);
        return treeVO;
    }
}
