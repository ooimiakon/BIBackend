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
import java.text.SimpleDateFormat;
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

    //推荐新闻
    @GetMapping("/Recommend")
    public Result getRecommend(String userId, String date) {
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
            newsInfo.put("title", news.getHeadline());
            newsInfo.put("content", news.getNewsBody());
            newsData.add(newsInfo);
        }
        result.data.put("news", newsData);

        return result;
    }

    //组合查询
    @GetMapping("/CombineSearch")
    public Result getCombineSearch(List<String> userIds, String date, String topic, Integer headlineLength, Integer newsBodyLength) {
        Result result = new Result();

        // 查询当天用户点击过的新闻ID
        QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
        clickWrapper.in("UserId", userIds)
                .eq("ClickTime", date);
        List<Click> clickList = clickMapper.selectList(clickWrapper);

        List<String> newsIds = new ArrayList<>();
        for (Click click : clickList) {
            newsIds.add(click.getClicknews());
        }

        // 构建News表的查询条件
        QueryWrapper<News> newsWrapper = new QueryWrapper<>();
        newsWrapper.in("News_ID", newsIds);

        // 添加新闻主题的查询条件
        if (topic != null && !topic.isEmpty()) {
            newsWrapper.eq("Topic", topic);
        }

        // 添加新闻标题长度的查询条件
        if (headlineLength != null) {
            newsWrapper.apply("LENGTH(Headline) = " + headlineLength);
        }

        // 添加新闻内容长度的查询条件
        if (newsBodyLength != null) {
            newsWrapper.apply("LENGTH(NewsBody) = " + newsBodyLength);
        }

        // 随机返回最多3条符合条件的新闻
        newsWrapper.last("ORDER BY RAND() LIMIT 3");

        // 检索新闻的标题和内容
        List<News> newsList = newsMapper.selectList(newsWrapper);

        // 封装结果
        List<Map<String, Object>> newsData = new ArrayList<>();
        for (News news : newsList) {
            Map<String, Object> newsInfo = new HashMap<>();
            newsInfo.put("title", news.getHeadline());
            newsInfo.put("content", news.getNewsBody());
            newsData.add(newsInfo);
        }

        result.data.put("news", newsData);
        return result;
    }



    public String formatDateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    //新闻点击量变化
    @GetMapping("/NewsClickCount")
    public Result getNewsClickCount(String newsId, String startDate, String endDate) {
        Result result = new Result();

        // 默认起始日期为该新闻最早被点击的日期
        if (startDate == null) {
            QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("ClickNews", newsId)
                    .orderByAsc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            Click click = clickMapper.selectOne(clickWrapper);
            if (click != null) {
                startDate = formatDateToString(click.getClicktime());
            }
            else{
                return result;
            }
        }

        // 默认结束日期为该新闻最后被点击的日期
        if (endDate == null) {
            QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("ClickNews", newsId)
                    .orderByDesc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            Click click = clickMapper.selectOne(clickWrapper);
            if (click != null) {
                startDate = formatDateToString(click.getClicktime());
            }
            else {
                return result;
            }
        }

        // 查询日期范围内该新闻每一天的点击量
        QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("ClickNews", newsId)
                .between("ClickTime", startDate, endDate)
                .groupBy("ClickTime")
                .select("ClickTime, COUNT(*) as clickCount");
        List<Map<String, Object>> clickCountList = clickMapper.selectMaps(clickWrapper);

        // 封装结果
        List<Map<String, Object>> clickData = new ArrayList<>();
        for (Map<String, Object> clickCount : clickCountList) {
            Map<String, Object> clickInfo = new HashMap<>();
            clickInfo.put("date", clickCount.get("ClickTime"));
            clickInfo.put("clickCount", clickCount.get("clickCount"));
            clickData.add(clickInfo);
        }

        result.data.put("clicks", clickData);
        return result;
    }

    //新闻类别点击量变化
    @GetMapping("/CategoryClickCount")
    public Result getCategoryClickCount(String category, String startDate, String endDate) {
        Result result = new Result();

        // 默认起始日期为该新闻类别最早被点击的日期
        if (startDate == null) {
            QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("Category", category)
                    .orderByAsc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            Click click = clickMapper.selectOne(clickWrapper);
            if (click != null) {
                startDate = formatDateToString(click.getClicktime());
            }
            else{
                return result;
            }
        }

        // 默认结束日期为该新闻类别最后被点击的日期
        if (endDate == null) {
            QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("Category", category)
                    .orderByDesc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            Click click = clickMapper.selectOne(clickWrapper);
            if (click != null) {
                startDate = formatDateToString(click.getClicktime());
            }
            else {
                return result;
            }
        }

        // 查询日期范围内该类别新闻每一天的总点击量
        QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("Category", category)
                .between("ClickTime", startDate, endDate)
                .groupBy("ClickTime")
                .select("ClickTime, COUNT(*) as clickCount");
        List<Map<String, Object>> clickCountList = clickMapper.selectMaps(clickWrapper);

        // 封装结果
        List<Map<String, Object>> clickData = new ArrayList<>();
        for (Map<String, Object> clickCount : clickCountList) {
            Map<String, Object> clickInfo = new HashMap<>();
            clickInfo.put("date", clickCount.get("ClickTime"));
            clickInfo.put("clickCount", clickCount.get("clickCount"));
            clickData.add(clickInfo);
        }

        result.data.put("clicks", clickData);
        return result;
    }

    //用户新闻类别点击量变化
    @GetMapping("/UserCategoryClickCount")
    public Result getUserCategoryClickCount(String userId, String startDate, String endDate) {
        Result result = new Result();

        // 默认起始日期为该用户点击的新闻最早日期
        if (startDate == null) {
            QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("UserId", userId)
                    .orderByAsc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            Click click = clickMapper.selectOne(clickWrapper);
            if (click != null) {
                startDate = formatDateToString(click.getClicktime());
            }
            else{
                return result;
            }
        }

        // 默认结束日期为该用户点击的新闻最后日期
        if (endDate == null) {
            QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("UserId", userId)
                    .orderByDesc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            Click click = clickMapper.selectOne(clickWrapper);
            if (click != null) {
                startDate = formatDateToString(click.getClicktime());
            }
            else {
                return result;
            }
        }

        // 检索日期范围内该用户点击的所有新闻类别
        QueryWrapper<Click> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("UserId", userId)
                .between("ClickTime", startDate, endDate)
                .groupBy("Category")
                .select("Category");
        List<Object> categoryObjects = clickMapper.selectObjs(clickWrapper);

        // 将结果转换为 List<String>
        List<String> categories = new ArrayList<>();
        for (Object categoryObject : categoryObjects) {
            categories.add(String.valueOf(categoryObject));
        }

        // 计算日期范围内每天每个类别的新闻总点击量
        QueryWrapper<Click> countWrapper = new QueryWrapper<>();
        countWrapper.eq("UserId", userId)
                .between("ClickTime", startDate, endDate)
                .groupBy("ClickTime, Category")
                .select("ClickTime, Category, COUNT(*) as clickCount");
        List<Map<String, Object>> clickCountList = clickMapper.selectMaps(countWrapper);

        // 封装结果
        List<Map<String, Object>> clickData = new ArrayList<>();
        for (Map<String, Object> clickCount : clickCountList) {
            Map<String, Object> clickInfo = new HashMap<>();
            clickInfo.put("date", clickCount.get("ClickTime"));
            clickInfo.put("category", clickCount.get("Category"));
            clickInfo.put("clickCount", clickCount.get("clickCount"));
            clickData.add(clickInfo);
        }

        result.data.put("clicks", clickData);
        result.data.put("categories", categories);
        return result;
    }




}
