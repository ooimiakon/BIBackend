package org.swzn.bibackend.controller;

import com.baomidou.mybatisplus.core.assist.ISqlRunner;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swzn.bibackend.entity.Click;
import org.swzn.bibackend.entity.ClicksInt;
import org.swzn.bibackend.entity.Dailyclick;
import org.swzn.bibackend.entity.News;
import org.swzn.bibackend.mapper.ClickMapper;
import org.swzn.bibackend.mapper.ClicksIntMapper;
import org.swzn.bibackend.mapper.DailyclickMapper;
import org.swzn.bibackend.mapper.NewsMapper;
import org.swzn.bibackend.service.ClickService;
import org.swzn.bibackend.service.NewsService;
import org.swzn.bibackend.utils.Result;


import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CrossOrigin("*")
//@Slf4j
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
    @Resource
    private ClicksIntMapper clicksIntMapper;
    @Resource
    private DailyclickMapper dailyclickMapper;

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

    //时间戳转int
    public static int convertTimestampToInt(String timestampString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = dateFormat.parse(timestampString);
        long timestamp = date.getTime();
        return (int) (timestamp / 1000);
    }

    //日期转Int
    public static int convertDateStringToInt(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dateString);
            return Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static int convertStringToStartstamp(String dateString) {
        try {
            String dateTimeString = dateString + " 00:00:00";

            // 定义日期格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 解析字符串为日期对象
            Date date = sdf.parse(dateTimeString);

            // 获取时间戳（以秒为单位）
            long timestamp = date.getTime() / 1000;

            // 将时间戳转换为int类型
            int timestampInt = (int) timestamp;

            return timestampInt;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0; // 如果转换失败，返回0或其他默认值
    }
    public static int convertStringToEndstamp(String dateString) {
        try {
            String dateTimeString = dateString + " 23:59:59";

            // 定义日期格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 解析字符串为日期对象
            Date date = sdf.parse(dateTimeString);

            // 获取时间戳（以秒为单位）
            long timestamp = date.getTime() / 1000;

            // 将时间戳转换为int类型
            int timestampInt = (int) timestamp;

            return timestampInt;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0; // 如果转换失败，返回0或其他默认值
    }


    //推荐新闻
    @GetMapping("/Recommend")
    public Result getRecommend(String userId, String date) {
        Result result = new Result();

        int intstartdate = convertStringToStartstamp(date);
        int intenddate = convertStringToEndstamp(date);

        // 查询用户当天点击过的所有新闻
        QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("UserId", userId)
                .between("ClickTime", intstartdate,  intenddate);
        List<ClicksInt> clickList = clicksIntMapper.selectList(clickWrapper);

        // 统计每个Category的点击量
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (ClicksInt click : clickList) {
            String category = click.getCategory();
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
                .last("ORDER BY RAND()")
                .last("LIMIT 5");// 随机排序并限制返回结果为3条记录
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

    //组合查询List<String> userIds
    @GetMapping("/CombineSearch")
    public Result getCombineSearch(String userId, String date, String category, Integer headlineLength, Integer newsBodyLength) {
        Result result = new Result();

        int intstartdate = convertStringToStartstamp(date);
        int intenddate = convertStringToEndstamp(date);

        // 查询当天用户点击过的新闻ID
        QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("UserId", userId)
                .between("ClickTime", intstartdate,  intenddate);
        List<ClicksInt> clickList = clicksIntMapper.selectList(clickWrapper);

        List<String> newsIds = new ArrayList<>();
        for (ClicksInt click : clickList) {
            newsIds.add(click.getClicknews());
        }

        // 构建News表的查询条件
        QueryWrapper<News> newsWrapper = new QueryWrapper<>();
        newsWrapper.in("News_ID", newsIds);

        // 添加新闻主题的查询条件
        if (category != null && !category.isEmpty()) {
            newsWrapper.eq("Category", category);
        }

        // 添加新闻标题长度的查询条件
        if (headlineLength != null) {
            newsWrapper.apply("LENGTH(Headline) < " + headlineLength);
        }

        // 添加新闻内容长度的查询条件
        if (newsBodyLength != null) {
            newsWrapper.apply("LENGTH(News_Body) < " + newsBodyLength);
        }

        // 随机返回最多3条符合条件的新闻
        newsWrapper .last("ORDER BY RAND()")
                .last("LIMIT 5");// 随机排序并限制返回结果为3条记录

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

//    //新闻点击量变化
//    @GetMapping("/NewsClickCount")
//    public Result getNewsClickCount(String newsId, String startDate, String endDate) {
//        Result result = new Result();
//        //int日期
//        int intstartdate=0, intenddate=0;
//        String integerStringS = startDate.replace("-", "");
//        String integerStringE = endDate.replace("-", "");
//        int integerValueS = Integer.parseInt(integerStringS);
//        int integerValueE = Integer.parseInt(integerStringE);
//        result.data.put("total",integerValueE-integerValueS+1);
//        //List<Integer> list= new ArrayList<>();
//        int[] array = new int[integerValueE-integerValueS+1];
//        for (int i = 0; i < array.length; i++) {
//            array[i] = 0;
//        }
//        QueryWrapper<Dailyclick> wrapper=new QueryWrapper<>();
//        wrapper.eq("ClickNews",newsId).between("Day",integerValueS,integerValueE);
//        List<Dailyclick> tmpList=dailyclickMapper.selectList(wrapper);
//
//        for(Dailyclick d:tmpList){
//            int index=d.getDay()-integerValueS;
//            if(index<integerValueE-integerValueS+1){
//                array[index]=d.getNum();
//            }
//        }
//        result.data.put("list",array);
//        return result;
//    }
public static List<String> getDateRange(String startDate, String endDate, String dateFormat) {
    List<String> dateRange = new ArrayList<>();
    try {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(sdf.parse(startDate));

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(sdf.parse(endDate));

        while (!startCalendar.after(endCalendar)) {
            dateRange.add(sdf.format(startCalendar.getTime()));
            startCalendar.add(Calendar.DATE, 1);
        }
    } catch (ParseException e) {
        e.printStackTrace();
    }
    return dateRange;
}

    //新闻点击量变化
    @GetMapping("/NewsClickCount")
    public Result getNewsClickCount(String newsId, String startDate, String endDate) {
        Result result = new Result();

        int intstartdate, intenddate;

        // 默认起始日期为该新闻最早被点击的日期
        if (startDate == null) {
            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("ClickNews", newsId)
                    .orderByAsc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
            if (click != null) {
                intstartdate = click.getClicktime();
            }
            else{
                return result;
            }
        }
        else {
            intstartdate = convertStringToStartstamp(startDate);
        }

        // 默认结束日期为该新闻最后被点击的日期
        if (endDate == null) {
            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("ClickNews", newsId)
                    .orderByDesc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
            if (click != null) {
                intenddate = click.getClicktime();
            }
            else {
                return result;
            }
        }
        else{
            intenddate = convertStringToEndstamp(endDate);
        }

        // 查询日期范围内该新闻每一天的点击量
        QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("ClickNews", newsId)
                .between("ClickTime", intstartdate,  intenddate)
                .groupBy("DATE(FROM_UNIXTIME(ClickTime))")
                .orderByAsc("DATE(FROM_UNIXTIME(ClickTime))")
                .select("DATE(FROM_UNIXTIME(ClickTime)) AS clickDate, COUNT(*) AS clickCount");
        List<Map<String, Object>> clickCountList = clicksIntMapper.selectMaps(clickWrapper);
//
//        // 封装结果
//        List<Map<String, Object>> clickData = new ArrayList<>();
//        for (Map<String, Object> clickCount : clickCountList) {
//            Map<String, Object> clickInfo = new HashMap<>();
//            clickInfo.put("date", clickCount.get("clickDate"));
//            clickInfo.put("clickCount", clickCount.get("clickCount"));
//            clickData.add(clickInfo);
//        }

        // 构建日期范围
        List<String> dateRange = getDateRange(startDate, endDate, "yyyy-MM-dd");

        // 封装结果初始化为0
        List<Map<String, Object>> clickData = new ArrayList<>();
        for (String date : dateRange) {
            Map<String, Object> clickInfo = new HashMap<>();
            clickInfo.put("date", date);
            clickInfo.put("clickCount", 0);
            clickData.add(clickInfo);
        }

        // 更新结果中存在的点击数据
        for (Map<String, Object> clickCount : clickCountList) {
            String clickDate = clickCount.get("clickDate").toString();
            int clickCountValue = Integer.parseInt(clickCount.get("clickCount").toString());

            // 查找对应日期的索引
            int index = dateRange.indexOf(clickDate);
            if (index >= 0) {
                // 更新点击量
                clickData.get(index).put("clickCount", clickCountValue);
            }
        }

        result.data.put("clicks", clickData);
        return result;
    }

    //新闻类别点击量变化(slow!!!!!!!!!!!!!!)
//    @GetMapping("/CategoryClickCount")
//    public Result getCategoryClickCount(String category, String startDate, String endDate) throws ParseException {
//        Result result = new Result();
//        //int日期
//        int intstartdate=0, intenddate=0;
//        int s_day=Integer.parseInt(startDate);
//        int e_day=Integer.parseInt(endDate);
//        int[] array = new int[e_day-s_day+1];
//        for (int i = 0; i < array.length; i++) {
//            array[i] = 0;
//        }
//        result.data.put("total",e_day-s_day+1);
//        QueryWrapper<Dailyclick> wrapper=new QueryWrapper<>();
//        wrapper.eq("Category",category).between("Day", s_day,e_day);
//        List<Dailyclick> tmpList=dailyclickMapper.selectList(wrapper);
//
//
//        for(Dailyclick d:tmpList){
//            int index=d.getDay()-s_day;
//            if(index<e_day-s_day+1){
//                array[index]=d.getNum();
//            }
//        }
//        result.data.put("list",array);
//         return result;
//}

    //新闻类别点击量变化
    @GetMapping("/CategoryClickCount")
    public Result getCategoryClickCount(String category, String startDate, String endDate){
        Result result = new Result();

        int intstartdate, intenddate;

        // 默认起始日期为该新闻类别最早被点击的日期
        if (startDate == null) {
            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("Category", category)
                    .orderByAsc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
            if (click != null) {
                intstartdate = click.getClicktime();
            }
            else{
                return result;
            }
        }
        else {
            intstartdate = convertStringToStartstamp(startDate);
        }

        // 默认结束日期为该新闻类别最后被点击的日期
        if (endDate == null) {
            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("Category", category)
                    .orderByDesc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
            if (click != null) {
                intenddate = click.getClicktime();
            }
            else {
                return result;
            }
        }
        else{
            intenddate = convertStringToEndstamp(endDate);
        }

        // 查询日期范围内该类别新闻每一天的总点击量
        QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("Category", category)
                .between("ClickTime", intstartdate, intenddate)
                .groupBy("DATE(FROM_UNIXTIME(ClickTime))")
                .orderByAsc("DATE(FROM_UNIXTIME(ClickTime))")
                .select("DATE(FROM_UNIXTIME(ClickTime)) AS clickDate, COUNT(*) AS clickCount");
        List<Map<String, Object>> clickCountList = clicksIntMapper.selectMaps(clickWrapper);

        // 封装结果
        List<Map<String, Object>> clickData = new ArrayList<>();
        for (Map<String, Object> clickCount : clickCountList) {
            Map<String, Object> clickInfo = new HashMap<>();
            clickInfo.put("date", clickCount.get("clickDate"));
            clickInfo.put("clickCount", clickCount.get("clickCount"));
            clickData.add(clickInfo);
        }

        result.data.put("clicks", clickData);
        return result;
    }

//    //用户新闻类别点击量变化
//    @GetMapping("/UserCategoryClickCount")
//    public Result getUserCategoryClickCount(String userId, String startDate, String endDate) throws ParseException {
//        Result result = new Result();
//        //int日期
//        int intstartdate=0, intenddate=0;
//        String startdate=startDate+" 00:00:00";
//        String enddate=endDate+" 23:59:59";
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date s_date = format.parse(startdate);
//        int startdate_i = (int)(s_date.getTime() / 1000);  // 转换为秒级时间戳
//        Date e_date = format.parse(enddate);
//        int enddate_i = (int)(e_date.getTime() / 1000);  // 转换为秒级时间戳
//        QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
//        List<ClicksInt> resultList=new ArrayList<>();
//
//        int currentTimestamp = startdate_i;
//        int interval = 86400; // 时间段的间隔，即86400秒
//        List<Integer> segmentPoints = new ArrayList<>();
//        while (currentTimestamp < enddate_i) {
//            segmentPoints.add(currentTimestamp);
//            currentTimestamp += interval;
//        }
//        segmentPoints.add(currentTimestamp);
//        //有几个区间
//        int numOfTime=segmentPoints.size();
//        result.data.put("total",numOfTime-1);
//
//        for(int i=0;i<numOfTime-1;i++){
//            QueryWrapper<ClicksInt> clickWrapperCopy = clickWrapper;
//            int start= segmentPoints.get(i);
//            int end= segmentPoints.get(i + 1);
//            String aa = "date" + i;
//            // 将整数时间戳转换为Date对象
//            Date date = new Date((long) start * 1000);
//            // 格式化Date对象为"yyyy-MM-dd"格式的字符串
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            String formattedDate = sdf.format(date);
//            result.data.put(aa,formattedDate);
//            long startTime = System.currentTimeMillis();
//            clickWrapperCopy.eq("UserId",userId).between("ClickTime", start, end);
//            long endTime = System.currentTimeMillis();
//
//            // 计算时间差
//            long elapsedTime = endTime - startTime;
//            System.out.println("执行时间: " + elapsedTime + " 毫秒");
//            List<ClicksInt> list=clicksIntMapper.selectList(clickWrapperCopy);
//            List<Integer> numList=new ArrayList<>();
//            List<String> categoryList=new ArrayList<>();
//            // 创建一个空的字典
//            Map<String, Integer> dictionary = new HashMap<>();
//            // 遍历 ClicksInt 列表并统计数量
//            for (ClicksInt clicksInt : list) {
//                String category = clicksInt.getCategory();
//                int index = categoryList.indexOf(clicksInt.getCategory());
//                if (index != -1) {
//                    numList.set(index, numList.get(index) + 1);
//                } else {
//                    categoryList.add(clicksInt.getCategory());
//                    numList.add(1);
//                }
//            }
//            String bb = "category" + i;
//            String cc = "clickNum" + i;
//
//            result.data.put(bb,categoryList);
//            result.data.put(cc,numList);
//        }
//        result.status=true;
//        result.errorCode=200;
//        return result;
//    }

    //用户新闻类别点击量变化
    @GetMapping("/UserCategoryClickCount")
    public Result getUserCategoryClickCount(String userId, String startDate, String endDate) {
        Result result = new Result();
        //int日期
        int intstartdate, intenddate;

        // 默认起始日期为该用户点击的新闻最早日期
        if (startDate == null) {
            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("UserId", userId)
                    .orderByAsc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
            if (click != null) {
                intstartdate = click.getClicktime();
            } else {
                return result;
            }
        } else {
            intstartdate = convertStringToStartstamp(startDate);
        }
        // 默认结束日期为该用户点击的新闻最后日期
        if (endDate == null) {
            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
            clickWrapper.eq("UserId", userId)
                    .orderByDesc("ClickTime")
                    .select("ClickTime")
                    .last("LIMIT 1");
            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
            if (click != null) {
                intenddate = click.getClicktime();
            } else {
                return result;
            }
        } else {
            intenddate = convertStringToEndstamp(endDate);
        }

        // 检索日期范围内该用户点击的所有新闻类别
        QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("UserId", userId)
                .between("ClickTime", intstartdate, intenddate)
                .groupBy("Category")
                .select("Category");
        List<Object> categoryObjects = clicksIntMapper.selectObjs(clickWrapper);

        // 将结果转换为 List<String>
        List<String> categories = new ArrayList<>();
        for (Object categoryObject : categoryObjects) {
            categories.add(String.valueOf(categoryObject));
        }

        // 计算日期范围内每天每个类别的新闻总点击量
        QueryWrapper<ClicksInt> countWrapper = new QueryWrapper<>();
        countWrapper.eq("UserId", userId)
                .between("ClickTime", intstartdate, intenddate)
                .groupBy("DATE(FROM_UNIXTIME(ClickTime)), Category")
                .orderByAsc("DATE(FROM_UNIXTIME(ClickTime))")
                .select("DATE(FROM_UNIXTIME(ClickTime)) AS clickDate, Category, COUNT(*) as clickCount");
        List<Map<String, Object>> clickCountList = clicksIntMapper.selectMaps(countWrapper);

        // 获取日期范围内的所有日期和新闻类别
        List<String> dateRange = getDateRange(startDate, endDate, "yyyy-MM-dd");

        // 封装结果
        List<Map<String, Object>> clickData = new ArrayList<>();

        for (String date : dateRange) {
            for (String category : categories) {
                Map<String, Object> clickInfo = new HashMap<>();
                clickInfo.put("date", date);
                clickInfo.put("category", category);
                clickInfo.put("clickCount", 0); // 默认点击量为0

                // 查找对应的点击量数据
                for (Map<String, Object> clickCount : clickCountList) {
                    String clickDate = clickCount.get("clickDate").toString();
                    String clickCategory = clickCount.get("Category").toString();
                    int clickCountValue = Integer.parseInt(clickCount.get("clickCount").toString());

                    if (date.equals(clickDate) && category.equals(clickCategory)) {
                        clickInfo.put("clickCount", clickCountValue); // 更新点击量
                        break;
                    }
                }
                clickData.add(clickInfo);
            }
        }
//        // 封装结果
//        List<Map<String, Object>> clickData = new ArrayList<>();
//        for (Map<String, Object> clickCount : clickCountList) {
//            Map<String, Object> clickInfo = new HashMap<>();
//            clickInfo.put("date", clickCount.get("clickDate"));
//            clickInfo.put("category", clickCount.get("Category"));
//            clickInfo.put("clickCount", clickCount.get("clickCount"));
//            clickData.add(clickInfo);
//        }
        result.data.put("clicks", clickData);
        result.data.put("categories", categories);
        return result;
    }


}
