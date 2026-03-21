package com.quanxiaoha.xiaohashu.common.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumUtils {
    public static String parseNum(Integer num){
        if(num < 10000 ) return String.valueOf(num);
        else if(num >= 10000 && num <= 100000000){
            double result = num / 10000.0;
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.DOWN);
            String format = df.format(result);
            return format + "万";
        }else{
            return "9999万";
        }
    }

    public static void main(String[] args) {
        System.out.println(parseNum(1999787));
    }
}
