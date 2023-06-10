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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    @Resource
    private ClicksIntMapper clicksIntMapper;
    @Resource
    private DailyclickMapper dailyclickMapper;

    @GetMapping("/test")
    public String test(){
        return "hello!";
    }

    //新闻点击量变化
    @GetMapping("/NewsClickCount")
    public Result getNewsClickCount(String newsId, String startDate, String endDate) {
        Result result = new Result();
        //int日期
        int intstartdate=0, intenddate=0;
        String integerStringS = startDate.replace("-", "");
        String integerStringE = endDate.replace("-", "");
        int integerValueS = Integer.parseInt(integerStringS);
        int integerValueE = Integer.parseInt(integerStringE);
        result.data.put("total",integerValueE-integerValueS+1);
        //List<Integer> list= new ArrayList<>();
        int[] array = new int[integerValueE-integerValueS+1];
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
        QueryWrapper<Dailyclick> wrapper=new QueryWrapper<>();
        wrapper.eq("ClickNews",newsId).between("Day",integerValueS,integerValueE);
        List<Dailyclick> tmpList=dailyclickMapper.selectList(wrapper);

        for(Dailyclick d:tmpList){
            int index=d.getDay()-integerValueS;
            if(index<integerValueE-integerValueS+1){
                array[index]=d.getNum();
            }
        }
        result.data.put("list",array);


//        // 默认起始日期为该新闻最早被点击的日期
//        if (startDate == null) {
//            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
//            clickWrapper.eq("ClickNews", newsId)
//                    .orderByAsc("ClickTime")
//                    .select("ClickTime")
//                    .last("LIMIT 1");
//            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
//            if (click != null) {
//                intstartdate = click.getClicktime();
//            }
//            else{
//                return result;
//            }
//        }
//        else {
//            intstartdate = convertDateStringToInt(startDate);
//        }
//
//        // 默认结束日期为该新闻最后被点击的日期
//        if (endDate == null) {
//            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
//            clickWrapper.eq("ClickNews", newsId)
//                    .orderByDesc("ClickTime")
//                    .select("ClickTime")
//                    .last("LIMIT 1");
//            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
//            if (click != null) {
//                intenddate = click.getClicktime();
//            }
//            else {
//                return result;
//            }
//        }
//        else{
//            intenddate = convertDateStringToInt(endDate);
//        }
//
//
//        // 查询日期范围内该新闻每一天的点击量
//        QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
//        clickWrapper.eq("ClickNews", newsId)
//                .between("ClickTime", intstartdate,  intenddate)
//                .groupBy("ClickTime")
//                .select("ClickTime, COUNT(*) as clickCount");
//        List<Map<String, Object>> clickCountList = clicksIntMapper.selectMaps(clickWrapper);
//
//        // 封装结果
//        List<Map<String, Object>> clickData = new ArrayList<>();
//        for (Map<String, Object> clickCount : clickCountList) {
//            Map<String, Object> clickInfo = new HashMap<>();
//            clickInfo.put("date", clickCount.get("ClickTime"));
//            clickInfo.put("clickCount", clickCount.get("clickCount"));
//            clickData.add(clickInfo);
//        }
//
//        result.data.put("clicks", clickData);
        return result;
    }

    //新闻类别点击量变化(slow!!!!!!!!!!!!!!)
    @GetMapping("/CategoryClickCount")
    public Result getCategoryClickCount(String category, String startDate, String endDate) throws ParseException {
        Result result = new Result();
        //int日期
        int intstartdate=0, intenddate=0;
        int s_day=Integer.parseInt(startDate);
        int e_day=Integer.parseInt(endDate);
        int[] array = new int[e_day-s_day+1];
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
        result.data.put("total",e_day-s_day+1);
        QueryWrapper<Dailyclick> wrapper=new QueryWrapper<>();
        wrapper.eq("Category",category).between("Day", s_day,e_day);
        List<Dailyclick> tmpList=dailyclickMapper.selectList(wrapper);


        for(Dailyclick d:tmpList){
            int index=d.getDay()-s_day;
            if(index<e_day-s_day+1){
                array[index]=d.getNum();
            }
        }
        result.data.put("list",array);


//        // 默认起始日期为该新闻类别最早被点击的日期
//        if (startDate == null) {
//            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
//            clickWrapper.eq("Category", category)
//                    .orderByAsc("ClickTime")
//                    .select("ClickTime")
//                    .last("LIMIT 1");
//            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
//            if (click != null) {
//                intstartdate = click.getClicktime();
//            }
//            else{
//                return result;
//            }
//        }
//        else {
//            intstartdate = convertDateStringToInt(startDate);
//        }
//
//        // 默认结束日期为该新闻类别最后被点击的日期
//        if (endDate == null) {
//            QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
//            clickWrapper.eq("Category", category)
//                    .orderByDesc("ClickTime")
//                    .select("ClickTime")
//                    .last("LIMIT 1");
//            ClicksInt click = clicksIntMapper.selectOne(clickWrapper);
//            if (click != null) {
//                intenddate = click.getClicktime();
//            }
//            else {
//                return result;
//            }
//        }
//        else{
//            intenddate = convertDateStringToInt(endDate);
//        }
//
//        // 查询日期范围内该类别新闻每一天的总点击量
//        QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
//        clickWrapper.eq("Category", category)
//                .between("ClickTime", intstartdate, intenddate)
//                .groupBy("ClickTime")
//                .select("ClickTime, COUNT(*) as clickCount");
//        List<Map<String, Object>> clickCountList = clicksIntMapper.selectMaps(clickWrapper);
//
//        // 封装结果
//        List<Map<String, Object>> clickData = new ArrayList<>();
//        for (Map<String, Object> clickCount : clickCountList) {
//            Map<String, Object> clickInfo = new HashMap<>();
//            clickInfo.put("date", clickCount.get("ClickTime"));
//            clickInfo.put("clickCount", clickCount.get("clickCount"));
//            clickData.add(clickInfo);
//        }

//        result.data.put("clicks", clickData);
        return result;
    }

    //用户新闻类别点击量变化
    @GetMapping("/UserCategoryClickCount")
    public Result getUserCategoryClickCount(String userId, String startDate, String endDate) throws ParseException {
        Result result = new Result();
        //int日期
        int intstartdate=0, intenddate=0;
        String startdate=startDate+" 00:00:00";
        String enddate=endDate+" 23:59:59";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date s_date = format.parse(startdate);
        int startdate_i = (int)(s_date.getTime() / 1000);  // 转换为秒级时间戳
        Date e_date = format.parse(enddate);
        int enddate_i = (int)(e_date.getTime() / 1000);  // 转换为秒级时间戳
        QueryWrapper<ClicksInt> clickWrapper = new QueryWrapper<>();
        List<ClicksInt> resultList=new ArrayList<>();

        int currentTimestamp = startdate_i;
        int interval = 86400; // 时间段的间隔，即86400秒
        List<Integer> segmentPoints = new ArrayList<>();
        while (currentTimestamp < enddate_i) {
            segmentPoints.add(currentTimestamp);
            currentTimestamp += interval;
        }
        segmentPoints.add(currentTimestamp);
        //有几个区间
        int numOfTime=segmentPoints.size();
        result.data.put("total",numOfTime-1);

        for(int i=0;i<numOfTime-1;i++){
            QueryWrapper<ClicksInt> clickWrapperCopy = clickWrapper;
            int start= segmentPoints.get(i);
            int end= segmentPoints.get(i + 1);
            String aa = "时间段" + i;
            // 将整数时间戳转换为Date对象
            Date date = new Date((long) start * 1000);
            // 格式化Date对象为"yyyy-MM-dd"格式的字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(date);
            result.data.put(aa,formattedDate);
            long startTime = System.currentTimeMillis();
            clickWrapperCopy.eq("UserId",userId).between("ClickTime", start, end);
            long endTime = System.currentTimeMillis();

            // 计算时间差
            long elapsedTime = endTime - startTime;
            System.out.println("执行时间: " + elapsedTime + " 毫秒");
            List<ClicksInt> list=clicksIntMapper.selectList(clickWrapperCopy);
            List<Integer> numList=new ArrayList<>();
            List<String> categoryList=new ArrayList<>();
            // 创建一个空的字典
            Map<String, Integer> dictionary = new HashMap<>();
            // 遍历 ClicksInt 列表并统计数量
            for (ClicksInt clicksInt : list) {
                String category = clicksInt.getCategory();
                int index = categoryList.indexOf(clicksInt.getCategory());
                if (index != -1) {
                    numList.set(index, numList.get(index) + 1);
                } else {
                    categoryList.add(clicksInt.getCategory());
                    numList.add(1);
                }
            }
            String bb = "类别" + i;
            String cc = "数量" + i;

            result.data.put(bb,categoryList);
            result.data.put(cc,numList);
        }
        result.status=true;
        result.errorCode=200;
        return result;
    }




}
