package tech.powerjob.worker.container;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class loader
 * The parent delegation model is not broken, which may result in ClassNotFoundException of the same class (such as tool classes that are incompatible with each other in different versions)
 * Why not destroy?
 * 1. After destruction, the container needs to load more classes, and the meta space may explode...
 * 2. The problem can be solved by keeping the public class consistent with the Worker, and the visual probability of CNF is not very high.
 *
 * @author tjq
 * @since 2020/3/23
 */
@Slf4j
public class OhMyClassLoader extends URLClassLoader {

    public OhMyClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Actively load classes, otherwise the class loader will be empty and the Spring IOC container will not initialize anything.
     * @param packageName package path, actively loading classes written by users
     * @throws Exception loading exception
     */
    public void load(String packageName) throws Exception {
        URL[] urLs = getURLs();
        for (URL jarURL : urLs) {
            JarFile jarFile = new JarFile(new File(jarURL.toURI()));
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.isDirectory()) {
                    continue;
                }
                String name = jarEntry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }

                // Convert org/spring/AAA.class -> org.spring.AAA
                String tmp = name.substring(0, name.length() - 6);
                String res = StringUtils.replace(tmp, "/", ".");

                if (res.startsWith(packageName)) {
                    loadClass(res);
                    log.info("[OhMyClassLoader] load class({}) successfully.", res);
                }
            }
        }
    }
}
