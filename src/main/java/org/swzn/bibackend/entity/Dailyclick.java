package org.swzn.bibackend.entity;


import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName DailyClick
 */
@TableName(value ="DailyClick")
@Data
public class Dailyclick implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer dailyclickid;

    /**
     * 
     */
    @TableId
    private Integer day;

    /**
     * 
     */
    private String category;

    /**
     * 
     */
    private String clicknews;

    /**
     * 
     */
    private Integer num;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}