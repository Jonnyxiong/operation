package com.ucpaas.sms.util;


import com.ucpaas.sms.dto.ClientConsumeVO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by dylan on 2017/7/26.
 */
public class SuperConvertChild<S ,C extends S> {

    public static SuperConvertChild getInstance(){
        return new SuperConvertChild();
    }

    public C convertToChild(S supers,Class son) {
        try {
            Method[] methods = supers.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if(method.getName().startsWith("get")){
                    Object param = supers.getClass().getMethod(method.getName()).invoke(supers);
                    if (param != null){

                        son.getDeclaredMethod(method.getName().replaceFirst("get", "set"), method.getReturnType())
                                .invoke(son,param);
                    }

                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IllegalAccessException, NoSuchMethodException {
//        Page page = new Page();
//        page.setPageNo(23);
//        Field[] declaredFields = page.getClass().getDeclaredFields();
//        for (Field field : declaredFields) {
//            System.out.println(field);
//            System.out.println(field.getType());
//            System.out.println(field.get(page));
//        }
//
//
//        Method[] methods = page.getClass().getDeclaredMethods();
//        for (Method method : methods) {
//            System.out.println( method.getName());
//            System.out.println( method.getReturnType());
//            System.out.println(method.getDefaultValue());
//
//        }


        Method getId = ClientConsumeVO.class.getMethod("getId");

        Class<?> returnType = getId.getReturnType();
        Method setId = ClientConsumeVO.class.getMethod("setId",returnType);
        System.out.println(setId);
        System.out.println(getId);

    }

}
