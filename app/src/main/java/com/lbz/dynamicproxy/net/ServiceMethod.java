package com.lbz.dynamicproxy.net;

import com.lbz.dynamicproxy.annotation.Field;
import com.lbz.dynamicproxy.annotation.GET;
import com.lbz.dynamicproxy.annotation.POST;
import com.lbz.dynamicproxy.annotation.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * @author: laibinzhi
 * @date: 2022-04-22 17:08
 * @desc:
 */
public class ServiceMethod {

    private final HttpUrl baseUrl;
    CustomRetrofit customRetrofit;
    String httpMethod;
    boolean hasBody;
    String relativeUrl;
    private final Call.Factory callFactory;

    ParameterHandle[] parameterHandles;
    HttpUrl.Builder urlBuilder = null;
    private FormBody.Builder formBodyBuilder;


    public ServiceMethod(Builder builder) {
        customRetrofit = builder.customRetrofit;
        baseUrl = customRetrofit.baseUrl;
        httpMethod = builder.httpMethod;
        hasBody = builder.hasBody;
        relativeUrl = builder.relativeUrl;
        callFactory = customRetrofit.callFactory;
        parameterHandles = builder.parameterHandles;
        //如果是有请求体，创建一个okhttp的请求体对象
        if (hasBody) {
            formBodyBuilder = new FormBody.Builder();
        }
    }

    public Object invoke(Object[] args) {
        /**
         * 1.处理请求的地址和参数
         */
        for (int i = 0; i < parameterHandles.length; i++) {
            ParameterHandle parameterHandle = parameterHandles[i];
            parameterHandle.apply(ServiceMethod.this, args[i].toString());
        }

        //获取最终请求地址
        HttpUrl url;
        if (urlBuilder == null) {
            urlBuilder = baseUrl.newBuilder(relativeUrl);
        }
        url = urlBuilder.build();

        //请求体
        FormBody formBody = null;
        if (hasBody) {
            formBody = formBodyBuilder.build();
        }


        Request request = new Request.Builder().url(url).method(httpMethod, formBody).build();
        Call call = callFactory.newCall(request);
        return call;
    }

    //get请求，把k-v拼到url里面
    public void addQueryParameter(String key, String value) {
        if (urlBuilder == null) {
            urlBuilder = baseUrl.newBuilder(relativeUrl);
        }
        urlBuilder.addQueryParameter(key, value);
    }

    //post请求，把k-v放到请求体中
    public void addFieldParameter(String key, String value) {
        formBodyBuilder.add(key, value);
    }

    public static class Builder {

        CustomRetrofit customRetrofit;
        Annotation[] methodAnnotation;
        Annotation[][] parameterAnnotations;
        ParameterHandle[] parameterHandles;
        String httpMethod;
        boolean hasBody;
        String relativeUrl;

        public Builder(CustomRetrofit customRetrofit, Method method) {
            this.customRetrofit = customRetrofit;
            //获取方法上的所有注解
            methodAnnotation = method.getAnnotations();
            //获取方法参数上的所有注解（一个参数可以有多个注解，一个方法又会有多个参数）
            parameterAnnotations = method.getParameterAnnotations();
        }

        public ServiceMethod build() {
            /**
             * 1.解析方法上的注解，只处理POST和GET
             */
            for (Annotation annotation : methodAnnotation) {
                if (annotation instanceof POST) {
                    //记录当前的请求方式
                    httpMethod = "POST";
                    //是否有请求体
                    hasBody = true;
                    //记录请求url的path
                    this.relativeUrl = ((POST) annotation).value();
                } else if (annotation instanceof GET) {
                    //记录当前的请求方式
                    httpMethod = "GET";
                    //是否有请求体
                    hasBody = false;
                    //记录请求url的path
                    this.relativeUrl = ((GET) annotation).value();
                }
            }

            /**
             * 2.解析方法参数的注解
             */
            int length = parameterAnnotations.length;
            parameterHandles = new ParameterHandle[length];
            for (int i = 0; i < length; i++) {
                //一个参数上的所有注解
                Annotation[] parameterAnnotation = parameterAnnotations[i];
                //处理参数上的每一个注解
                for (Annotation annotation : parameterAnnotation) {
                    if (annotation instanceof Field) {
                        //得到主街上的value：请求参数的key
                        String key = ((Field) annotation).value();
                        parameterHandles[i] = new ParameterHandle.FieldParameterHandle(key);
                    } else if (annotation instanceof Query) {
                        String key = ((Query) annotation).value();
                        parameterHandles[i] = new ParameterHandle.QueryParameterHandle(key);
                    }
                }
            }

            return new ServiceMethod(this);
        }
    }

}
