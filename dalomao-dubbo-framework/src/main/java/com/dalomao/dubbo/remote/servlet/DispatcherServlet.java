package com.dalomao.dubbo.remote.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dalomao.dubbo.pojo.Service;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 给生产者接收HTTP用的servlet的，只有http请求才能调用到
 */
public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 4356057841344125624L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        try {
            JSONObject requestParam = httpProcess(req, resp);
            String serviceId = requestParam.getString("serviceId");
            String methodName = requestParam.getString("methodName");
            JSONArray paramTypes = requestParam.getJSONArray("paramTypes");
            //对应方法参数
            JSONArray methodParamJa = requestParam.getJSONArray("methodParams");
            Object[] objs = null;
            if (methodParamJa != null) {
                objs = new Object[methodParamJa.size()];
                int i = 0;
                for (Object o : methodParamJa) {
                    objs[i++] = o;
                }
            }

            //拿到spring上下文
            ApplicationContext application = Service.getApplication();
            //服务层的实例
            Object serviceBean = application.getBean(serviceId);
            //获取实例的方法，并考虑到方法的重载情况
            Method method = getMethod(serviceBean, methodName, paramTypes);
            if (method != null) {
                Object result = method.invoke(serviceBean, objs);
                //返回
                PrintWriter pw = resp.getWriter();
                pw.write(result.toString());
            } else {
                PrintWriter pw = resp.getWriter();
                pw.write("-------------no such method-------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取请求参数
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public static JSONObject httpProcess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuffer sb = new StringBuffer();
        InputStream is = request.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = "";
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }

        return sb.toString().length() <= 0 ? null : JSONObject.parseObject(sb.toString());
    }

    private Method getMethod(Object bean, String methodName, JSONArray paramTypes) {
        //获取该bean的所有方法
        Method[] methods = bean.getClass().getMethods();

        //获取方法名匹配的所有方法（重载方法）
        List<Method> matchingMethodList = new ArrayList<Method>();
        for (Method method : methods) {
            if (method.getName().trim().equals(methodName)) {
                matchingMethodList.add(method);
            }
        }

        if (matchingMethodList.size() == 1) {
            return matchingMethodList.get(0);
        }

        //存在多个重载方法，则根据参数类型筛选出最符合的那个方法
        for (Method method : matchingMethodList) {
            boolean isSameType = true;
            Class<?>[] types = method.getParameterTypes();
            //参数类型个数不匹配
            if (types.length != paramTypes.size()) {
                continue;
            }

            //匹配参数类型
            for (int i=0; i<types.length; i++) {
                if (!types[i].toString().contains(paramTypes.getString(i))) {
                    isSameType = false;
                    break;
                }
            }

            if (isSameType) {
                return method;
            }
        }
        return null;
    }
}
