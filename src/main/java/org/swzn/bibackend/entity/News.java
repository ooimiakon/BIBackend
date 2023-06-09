package org.swzn.bibackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName News
 */
@TableName(value ="News")
@Data
public class News implements Serializable {
    /**
     * 
     */
    private String newsId;

    /**
     * 
     */
    private String category;

    /**
     * 
     */
    private String topic;

    /**
     * 
     */
    private String headline;

    /**
     * 
     */
    private String newsBody;

    /**
     * 
     */
    private Integer cutnum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}