package jc.sky.modules.structure;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import jc.sky.SKYHelper;
import jc.sky.common.utils.SKYAppUtil;
import jc.sky.common.utils.SKYCheckUtils;
import jc.sky.core.Impl;
import jc.sky.core.SKYBiz;
import jc.sky.display.SKYIDisplay;
import jc.sky.modules.methodProxy.SKYProxy;

/**
 * @创建人 sky
 * @创建时间 16/4/11 下午1:29
 * @类描述
 */
public class SKYStructureModel {

    final int key;

    SKYProxy SKYProxy;

    private Object view;

    private Class service;

    private Stack<Class> supper;

    private ConcurrentHashMap<Class<?>, Object> stackHttp;

    private ConcurrentHashMap<Class<?>, Object> stackImpl;

    private ConcurrentHashMap<Class<?>, Object> stackDisplay;

    public SKYStructureModel(Object view) {
        // 唯一标记
        key = view.hashCode();
        // 视图
        this.view = view;
        // 业务初始化
        service = SKYAppUtil.getClassGenricType(view.getClass(), 0);
        SKYCheckUtils.checkNotNull(service, "获取不到泛型");
        SKYCheckUtils.validateServiceInterface(service);

        Object impl = getImplClass(service);
        // 找到父类
        supper = new Stack<>();
        Class tempClass = impl.getClass().getSuperclass();

        if (tempClass != null) {
            while (!tempClass.equals(SKYBiz.class)) {

                if (tempClass.getInterfaces() != null) {
                    Class clazz = tempClass.getInterfaces()[0];
                    supper.add(clazz);
                }
                tempClass = tempClass.getSuperclass();
            }
        }

        SKYProxy = SKYHelper.methodsProxy().create(service, impl);
        stackHttp = new ConcurrentHashMap<>();
        stackImpl = new ConcurrentHashMap<>();
        stackDisplay = new ConcurrentHashMap<>();
    }

    /**
     * 清空
     */
    public void clearAll() {
        this.view = null;
        service = null;
        SKYProxy.clearProxy();
        SKYProxy = null;
        stackHttp.clear();
        stackHttp = null;
        stackImpl.clear();
        stackImpl = null;
        stackDisplay.clear();
        stackDisplay = null;
        supper.clear();
        supper = null;
    }

    /**
     * 调度
     *
     * @param displayClazz
     * @param <D>
     * @return
     */
    public <D extends SKYIDisplay> D display(Class<D> displayClazz) {
        if (stackDisplay == null) {
            return SKYHelper.display(displayClazz);
        }

        D display = (D) stackDisplay.get(displayClazz);
        if (display == null) {
            synchronized (stackDisplay) {
                if (display == null) {
                    SKYCheckUtils.checkNotNull(displayClazz, "display接口不能为空");
                    SKYCheckUtils.validateServiceInterface(displayClazz);
                    Object impl = getImplClass(displayClazz);
                    SKYProxy SKYProxy = SKYHelper.methodsProxy().createDisplay(displayClazz, impl);
                    stackDisplay.put(displayClazz, SKYProxy.proxy);
                    display = (D) SKYProxy.proxy;
                }
            }
        }
        return display;
    }

    /**
     * 网络
     *
     * @param httpClazz
     * @param <H>
     * @return
     */
    public <H> H http(Class<H> httpClazz) {
        H http = (H) stackHttp.get(httpClazz);
        if (http == null) {
            synchronized (stackHttp) {
                if (http == null) {
                    SKYCheckUtils.checkNotNull(httpClazz, "http接口不能为空");
                    SKYCheckUtils.validateServiceInterface(httpClazz);
                    http = SKYHelper.httpAdapter().create(httpClazz);
                    stackHttp.put(httpClazz, http);
                }
            }
        }
        if (http == null) {
            http = SKYHelper.structureHelper().createNullService(httpClazz);
        }
        return http;
    }

    /**
     * 实现
     *
     * @param implClazz
     * @param <P>
     * @return
     */
    public <P> P impl(Class<P> implClazz) {
        P impl = (P) stackImpl.get(implClazz);

        if (impl == null) {
            synchronized (stackImpl) {
                if (impl == null) {
                    SKYCheckUtils.checkNotNull(implClazz, "impl接口不能为空");
                    SKYCheckUtils.validateServiceInterface(implClazz);
                    impl = SKYHelper.methodsProxy().createImpl(implClazz, getImplClass(implClazz));
                    stackImpl.put(implClazz, impl);
                }
            }
        }
        return impl;
    }

    /**
     * 获取实现类
     *
     * @param service
     * @param <D>
     * @return
     */
    private <D> Object getImplClass(@NotNull Class<D> service) {
        validateServiceClass(service);
        try {
            // 获取注解
            Impl impl = service.getAnnotation(Impl.class);
            SKYCheckUtils.checkNotNull(impl, "该接口没有指定实现类～");
            /** 加载类 **/
            Class clazz = Class.forName(impl.value().getName());
            Constructor c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            SKYCheckUtils.checkNotNull(clazz, "业务类为空～");
            /** 创建类 **/
            Object o = c.newInstance();
            // 如果是业务类
            if (o instanceof SKYBiz) {
                ((SKYBiz) o).initUI(this);
            }
            return o;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.valueOf(service) + "，没有找到业务类！");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(String.valueOf(service) + "，实例化异常！");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.valueOf(service) + "，访问权限异常！");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.valueOf(service) + "，没有找到构造方法！");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(String.valueOf(service) + "，反射异常！");
        }
    }

    /**
     * 验证类 - 判断是否是一个接口
     *
     * @param service
     * @param <T>
     */
    private <T> void validateServiceClass(Class<T> service) {
        if (service == null || !service.isInterface()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(service);
            stringBuilder.append("，该类不是接口！");
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public Object getView() {
        return view;
    }

    public Class getService() {
        return service;
    }

    public SKYProxy getSKYProxy() {
        return SKYProxy;
    }

    public boolean isSupterClass(Class clazz) {
        return supper.search(clazz) != -1;
    }
}
