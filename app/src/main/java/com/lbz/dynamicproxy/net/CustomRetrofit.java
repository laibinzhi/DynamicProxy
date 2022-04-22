package com.lbz.dynamicproxy.net;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * @author: laibinzhi
 * @date: 2022-04-22 16:51
 * @desc:
 */
public class CustomRetrofit {

    final HttpUrl baseUrl;
    final Call.Factory callFactory;
    final Map<Method, ServiceMethod> serviceMethodCache = new ConcurrentHashMap<>();

    public CustomRetrofit(HttpUrl baseUrl, Call.Factory callFactory) {
        this.baseUrl = baseUrl;
        this.callFactory = callFactory;
    }

    public <T> T create(Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                //解析这个method上所有注解的信息
                ServiceMethod serviceMethod = loadServiceMethod(method);
                return serviceMethod.invoke(args);
            }
        });
    }

    private ServiceMethod loadServiceMethod(Method method) {
        ServiceMethod result = serviceMethodCache.get(method);
        if (result != null) return result;
        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder(CustomRetrofit.this, method).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    public static class Builder {

        HttpUrl baseUrl;
        Call.Factory callFactory;


        public CustomRetrofit build() {
            if (baseUrl == null) throw new IllegalArgumentException("基础url不能为空");
            Call.Factory callFactory = this.callFactory;
            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }
            return new CustomRetrofit(baseUrl, callFactory);
        }

        public Builder baseUrl(String url) {
            this.baseUrl = HttpUrl.get(url);
            return this;
        }
    }
}
