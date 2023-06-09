package org.swzn.bibackend.utils;

import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSON;

public class Result {
    public int errorCode;
    public boolean status;

    public Map<String, Object> data = new HashMap<>();

    public Result()
    {
        errorCode = 300;
        status = false;
    }

    public String ReturnJson(){
        String jsonStr = JSON.toJSONString(this);
        return jsonStr;
    }

    public static Result error() {
        return new Result();
    }
}
