package org.swzn.bibackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swzn.bibackend.entity.Click;
import org.swzn.bibackend.entity.News;
import org.swzn.bibackend.mapper.ClickMapper;
import org.swzn.bibackend.mapper.NewsMapper;
import org.swzn.bibackend.service.ClickService;
import org.swzn.bibackend.service.NewsService;
import org.swzn.bibackend.utils.Result;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/news")
public class NewsController {

    @Resource
    private NewsService newsService;
    @Resource
    private NewsMapper newsMapper;
    @Resource
    private ClickService clickService;
    @Resource
    private ClickMapper clickMapper;

    @GetMapping("/test")
    public String test(){
        return "hello!";
    }
    @GetMapping("/GetNewsById")
    public Result GetNewsById(String id){
        Result result=new Result();
        QueryWrapper<Click> wrapper=new QueryWrapper<>();
        wrapper.eq("UserId",id);
        List<Click> newsList=clickMapper.selectList(wrapper);
        List<String> temp=new ArrayList<>();
        for(Click n:newsList){

            temp.add(n.getClicknews());
        }
        result.data.put("id",temp);
        return result;
    }
}
