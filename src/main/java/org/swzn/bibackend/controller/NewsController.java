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
import java.util.*;

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


    @GetMapping("/Recommend")
    public Result getNewsByUserAndDate(String userId, String date) {
        Result result = new Result();

        // 查询用户当天点击过的所有新闻
        QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("UserId", userId)
                .eq("ClickTime", date);
        List<Click> clickList = clickMapper.selectList(clickWrapper);

        // 统计每个Category的点击量
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (Click click : clickList) {
            String category = click.getClicknews();
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }

        // 获取点击量最高的Category
        String maxCategory = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCategory = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        // 检索点击量最高的Category的新闻的标题和内容（随机3条）
        QueryWrapper<News> newsWrapper = new QueryWrapper<>();
        newsWrapper.eq("Category", maxCategory)
                .last("ORDER BY RAND() LIMIT 3");  // 随机排序并限制返回结果为3条记录
        List<News> newsList = newsMapper.selectList(newsWrapper);

        // 封装结果
        List<Map<String, Object>> newsData = new ArrayList<>();
        for (News news : newsList) {
            Map<String, Object> newsInfo = new HashMap<>();
            newsInfo.put("title", news.getTitle());
            newsInfo.put("content", news.getContent());
            newsData.add(newsInfo);
        }
        result.data.put("news", newsData);

        return result;
    }
}
