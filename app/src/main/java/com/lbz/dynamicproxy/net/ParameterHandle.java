package com.lbz.dynamicproxy.net;

import java.security.Key;

/**
 * @author: laibinzhi
 * @date: 2022-04-22 17:22
 * @desc:
 */
abstract class ParameterHandle {

    abstract void apply(ServiceMethod serviceMethod,String value);


    static class QueryParameterHandle extends ParameterHandle{
        String key;

        public QueryParameterHandle(String key) {
            this.key = key;
        }

        @Override
        void apply(ServiceMethod serviceMethod, String value) {
            serviceMethod. addQueryParameter(key,value);
        }
    }


    static class FieldParameterHandle extends ParameterHandle{

        String key;

        public FieldParameterHandle(String key) {
            this.key = key;
        }

        @Override
        void apply(ServiceMethod serviceMethod, String value) {
            serviceMethod. addFieldParameter(key,value);

        }
    }
}
