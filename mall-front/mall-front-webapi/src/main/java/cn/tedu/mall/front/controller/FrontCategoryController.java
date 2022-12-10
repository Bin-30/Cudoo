package cn.tedu.mall.front.controller;

import cn.tedu.mall.common.restful.JsonResult;
import cn.tedu.mall.front.service.IFrontCategoryService;
import cn.tedu.mall.pojo.front.entity.FrontCategoryEntity;
import cn.tedu.mall.pojo.front.vo.FrontCategoryTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "分类查询")
@RequestMapping("/front/category")
public class FrontCategoryController {
    @Autowired
    private IFrontCategoryService frontCategoryService;

    @ApiOperation("查询所有三级分类树")
    @GetMapping("/all")
    public JsonResult<FrontCategoryTreeVO<FrontCategoryEntity>> getTreeVO(){
        FrontCategoryTreeVO<FrontCategoryEntity> treeVO = frontCategoryService.categoryTree();
        return JsonResult.ok(treeVO);
    }
}
