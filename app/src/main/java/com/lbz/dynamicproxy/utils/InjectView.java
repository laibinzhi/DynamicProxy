package com.lbz.dynamicproxy.utils;

import android.app.Activity;
import android.view.View;

import com.lbz.dynamicproxy.annotation.BindView;
import com.lbz.dynamicproxy.annotation.OnClick;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: laibinzhi
 * @date: 2022-04-22 16:33
 * @desc:
 */
public class InjectView {

    public static void inject(Activity activity) {
        Class<? extends Activity> activityClass = activity.getClass();
        Field[] declaredFields = activityClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(BindView.class)) {
                BindView annotation = field.getAnnotation(BindView.class);
                if (annotation != null) {
                    int viewId = annotation.value();
                    View targetView = activity.findViewById(viewId);
                    if (targetView != null) {
                        field.setAccessible(true);
                        try {
                            field.set(activity, targetView);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        Method[] declaredMethods = activityClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(OnClick.class)) {
                OnClick annotation = declaredMethod.getAnnotation(OnClick.class);
                if (annotation!=null){
                    int[] ids = annotation.value();
                    for (int id : ids) {
                        View view = activity.findViewById(id);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    declaredMethod.invoke(activity);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }

            }
        }

    }
}
