package com.dalomao.dubbo.invoke;


import com.dalomao.dubbo.pojo.Reference;

import java.lang.reflect.Method;

public class Invocation {

    private Method method;

    private Object[] objs;

    private Reference reference;

    private Invoke invoke;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getObjs() {
        return objs;
    }

    public void setObjs(Object[] objs) {
        this.objs = objs;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Invoke getInvoke() {
        return invoke;
    }

    public void setInvoke(Invoke invoke) {
        this.invoke = invoke;
    }
}
