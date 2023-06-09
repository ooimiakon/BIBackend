package org.swzn.bibackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.swzn.bibackend.entity.News;
import org.swzn.bibackend.service.NewsService;
import org.swzn.bibackend.mapper.NewsMapper;
import org.springframework.stereotype.Service;

/**
* @author hunyingzhong
* @description 针对表【News】的数据库操作Service实现
* @createDate 2023-06-09 19:25:10
*/
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
    implements NewsService{

}




