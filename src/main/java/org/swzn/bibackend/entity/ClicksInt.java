package org.swzn.bibackend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName Clicks_Int
 */
@TableName(value ="Clicks_Int")
@Data
public class ClicksInt implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer clickid;

    /**
     * 
     */
    @TableId
    private Integer clicktime;

    /**
     * 
     */
    private String userid;

    /**
     * 
     */
    private String clicknews;

    /**
     * 
     */
    private String category;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}