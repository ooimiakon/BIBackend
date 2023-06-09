package org.swzn.bibackend.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.swzn.bibackend.entity.Click;
import org.swzn.bibackend.service.ClickService;
import org.swzn.bibackend.mapper.ClickMapper;
import org.springframework.stereotype.Service;

/**
* @author HDM
* @description 针对表【Click】的数据库操作Service实现
* @createDate 2023-06-09 22:49:23
*/
@Service
public class ClickServiceImpl extends ServiceImpl<ClickMapper, Click>
implements ClickService{

}
