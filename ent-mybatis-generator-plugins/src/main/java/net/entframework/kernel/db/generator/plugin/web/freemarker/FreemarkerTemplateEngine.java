package net.entframework.kernel.db.generator.plugin.web.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import freemarker.template.Version;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class FreemarkerTemplateEngine {

    private static Configuration configuration;

    public static synchronized void init(String baseDir) throws IOException {
        if (configuration == null) {
            configuration = new Configuration(Configuration.VERSION_2_3_31);
            configuration.setDefaultEncoding("UTF-8");
            if (StringUtils.isNotEmpty(baseDir)) {
                TemplateLoader classTemplateLoader = new ClassTemplateLoader(FreemarkerTemplateEngine.class, "/");
                TemplateLoader fileTemplateLoader = new FileTemplateLoader(new File(baseDir));
                ProxyTemplateLoader multiTemplateLoader = new ProxyTemplateLoader(
                        new TemplateLoader[] { classTemplateLoader, fileTemplateLoader });

                configuration.setTemplateLoader(multiTemplateLoader);
            } else {
                TemplateLoader classTemplateLoader = new ClassTemplateLoader(FreemarkerTemplateEngine.class, "/");
                configuration.setTemplateLoader(classTemplateLoader);
            }
            configuration.setClassicCompatible(true);
            configuration.getCacheStorage().clear();
        }
    }

    public static String process(Map<String, Object> objectMap, String templatePath) throws Exception {
        Template template = configuration.getTemplate(templatePath);

        BeansWrapper wrapper = new BeansWrapper(new Version(2, 3, 31));
        TemplateModel statics = wrapper.getStaticModels();
        objectMap.put("statics", statics);
        String output = null;
        try (StringWriter stringWriter = new StringWriter()) {
            template.process(objectMap, stringWriter);
            output = stringWriter.getBuffer().toString();
        }
        return output;
    }

}
