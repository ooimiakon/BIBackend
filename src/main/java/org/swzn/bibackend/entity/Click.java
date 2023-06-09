package org.swzn.bibackend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName Click
 */
@TableName(value ="Click")
@Data
public class Click implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer clickid;

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
    private Date clicktime;

    /**
     * 
     */
    private String category;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}