package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import com.gupaoedu.vip.spring.framework.annotation.GPController;
import com.gupaoedu.vip.spring.framework.annotation.GPRequestMapping;
import com.gupaoedu.vip.spring.framework.annotation.GPService;
import com.gupaoedu.vip.spring.framework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author 孟祥骞
 */
public class GPDispatcherServlet extends HttpServlet {

    private GPApplicationContext applicationContext;
    private List<HandlerMapping> handlerMappings = new ArrayList<HandlerMapping>();
    private Map<HandlerMapping, HandlerAdapter> handlerAdapters = new HashMap<HandlerMapping, HandlerAdapter>();
    private List<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();


    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doDispatcher(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                processDispatcherResult(req,resp,new ModelAndView("500"));
            } catch (Exception ex) {
                ex.printStackTrace();
                resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        HandlerMapping handlerMapping = getHandler(req);
        if (null == handlerMapping) {
            processDispatcherResult(req,resp,new ModelAndView("404"));
            return;
        }
        HandlerAdapter handlerAdapter = handlerAdapters.get(handlerMapping);
        ModelAndView modelAndView = handlerAdapter.handler(req,resp,handlerMapping);
        processDispatcherResult(req,resp,modelAndView);
    }

    private void processDispatcherResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView modelAndView) throws Exception {
        if (modelAndView == null) {
            return;
        }
        if (viewResolvers.isEmpty()) {
            return;
        }
        String viewName = modelAndView.getViewName();
        for (ViewResolver viewResolver : viewResolvers) {
            View view = viewResolver.resolverViewName(viewName);
            if (null==view) {
                continue;
            }
            view.render(modelAndView.getModel(), req, resp);
            return;
        }



    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        uri = uri.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (HandlerMapping mapping : handlerMappings) {
            Matcher matcher = mapping.getPatternUrl().matcher(uri);
            if (!matcher.matches()) {
                continue;
            }
            return mapping;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化Spring核心容器IoC
        applicationContext = new GPApplicationContext(config.getInitParameter("contextConfigLocation"));
        //初始化组件
        initStrategies(applicationContext);


    }

    private void initStrategies(GPApplicationContext context) {
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
        //初始化视图转换器
        initViewResolvers(context);
        System.out.println("=========组件初始化完成==========");
    }

    private void initViewResolvers(GPApplicationContext context) {
        String templateRoot = this.applicationContext.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            viewResolvers.add(new ViewResolver(templateRoot));
        }


    }

    private void initHandlerAdapters(GPApplicationContext context) {
        if (handlerMappings.isEmpty()) {
            return;
        }
        for (HandlerMapping handlerMapping : handlerMappings) {
            handlerAdapters.put(handlerMapping, new HandlerAdapter());
        }
    }

    private void initHandlerMappings(GPApplicationContext context) {
        if (applicationContext.getBeanDefinitionCount() == 0) {
            return;
        }

        Class<?> clazz;
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            clazz = applicationContext.getBean(beanName).getClass();

            if (!clazz.isAnnotationPresent(GPController.class)&&!clazz.isAnnotationPresent(GPService.class)) {
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                baseUrl = clazz.getAnnotation(GPRequestMapping.class).value();
            }
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(GPRequestMapping.class)) {
                    continue;
                }
                //提取每个方法上面配置的url
                GPRequestMapping methodAnnotation = method.getAnnotation(GPRequestMapping.class);
                String regex = (baseUrl + "/" + methodAnnotation.value()).replaceAll("/+", "/").replaceAll("\\*",".*");
                Pattern pattern = Pattern.compile(regex);
                Object bean = applicationContext.getBean(beanName);
                handlerMappings.add(new HandlerMapping(pattern,method,applicationContext.getBean(beanName)));
                System.out.println("Mapped : " + pattern + "," + method);
            }
        }
    }
}
