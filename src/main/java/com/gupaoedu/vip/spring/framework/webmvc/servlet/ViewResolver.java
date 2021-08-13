package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import java.io.File;

/**
 * @author Meng 2021/8/9
 */
public class ViewResolver {
    private final String DEFAULT_FILE_SUFFIX = ".html";
    private File templateRootDir;

    public ViewResolver(String templateRoot) {
        String file = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.templateRootDir = new File(file);
    }

    public View resolverViewName(String viewName) {
        if (null == viewName || "".equals(viewName)) {
            return null;
        }
        viewName = viewName.endsWith(DEFAULT_FILE_SUFFIX)?viewName:viewName+DEFAULT_FILE_SUFFIX;
        File file = new File((templateRootDir.getPath()+"/" + viewName).replaceAll("/+","/"));
        return new View(file);
    }
}
