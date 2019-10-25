package com.digitoll.commons.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

public class BasicUtils {

    public static String[] getNullPropertyNames (Object source, List<String> nonEmpty,List<String> skip) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null ||
                    (skip != null &&
                            skip.contains(pd.getName())) ||
                    ( nonEmpty != null &&
                            nonEmpty.contains(pd.getName()) &&
                            !pd.getName().equals("") &&
                            srcValue instanceof String &&
                            srcValue.toString().equals("")
                    )){
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**Copies props from src to target. Cant copy null props
     List<String> nonEmpty list of props that won't be copied if they are empty ( for example you cant delete company
     name or email of a subscription, so even if SubscriptionDTO has no mail it will be skipped
     **/
    public static void copyProps(Object src, Object target, List<String> nonEmpty) {
        if(src == null){
            target=null;
            return;
        }
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src, nonEmpty, new ArrayList<>()));
    }

    public static void copyPropsSkip(Object src, Object target, List<String> skip) {
        if(src == null){
            target=null;
            return;
        }
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src, new ArrayList<>(), skip));
    }

    public static void copyNonNullProps(Object src, Object target) {
        if(src == null){
            target=null;
            return;
        }
        copyProps(src, target, new ArrayList<>());
    }

    public static int getIntFromDate(Date date, TimeZone timeZone){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(timeZone);
        int result = Integer.valueOf(sdf.format(date).replaceAll("-",""));
        return result;
    }

}