package ru.ifmo.ctddev.kupriyanov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by pinkdonut on 21.03.16.
 */

public class Implementor implements JarImpler {

    /**
     * Starts Implementor with arguments. If number of args is two, create implementation of given interface and put it to given path.
     * If number of args is three, create implementation of given interface, pack it in .jar file and put it to given path.
     *
     * @param args arguments from command line
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be generated.
     */
    public static void main(String[] args) throws ImplerException {
        if (args != null && (args.length == 3 && args[0].equals("-jar") || args.length == 2)) {
            try {
                Implementor implementor = new Implementor();
                if (args.length == 2) {
                    implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
                } else {
                    implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found");
            }
        } else {
            System.err.println("wrong arguments");
        }
    }

    /**
     * Makes implementation of the given interface , compile it and make jar-file of the compiled implementation.
     *
     * @param token   type token to create implementation for.
     * @param jarFile directory where .jar should be placed to
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException if arguments are wrong/compilation finish with error or can't create .jar file in given directory
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (jarFile == null) {
            throw new ImplerException("null jarFile");
        }
        Path root = jarFile.getParent();
        if (root == null) {
            root = Paths.get("");
        }
        implement(token, root);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (token.getPackage() != null) {
            root = root.resolve(token.getPackage().getName().replace(".", File.separator) + File.separator);
        }
        root = root.resolve(token.getSimpleName() + "Impl.java");
        int returnCode = compiler.run(null, null, null, root.toString());
        if (returnCode != 0) {
            throw new ImplerException("Compilation error, return code : " + returnCode);
        }
        try {
            root = root.getParent();
            if (root == null) {
                root = Paths.get("");
            }
            root = root.resolve(token.getSimpleName() + "Impl.class");
            try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarFile), new Manifest());
                 InputStream in = Files.newInputStream(root)) {
                String s = "";
                if (token.getPackage() != null) {
                    s += token.getPackage().getName().replace(".", "/") + "/";
                }
                s += token.getSimpleName() + "Impl.class";
                out.putNextEntry(new JarEntry(s));
                byte[] buf = new byte[1024];
                int count;
                while ((count = in.read(buf)) != -1) {
                    out.write(buf, 0, count);
                }
                Files.delete(root);
            }
        } catch (IOException e) {
            throw new ImplerException("Can't create .jar file");
        }
    }

    /**
     * Creates a file that implements interface. Output file contains java class, that implements given interface and compiles
     * without errors. The output file is in the same package which is the source interface. Interface must not contain generics.
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw (new ImplerException("Wrong arguments"));
        }
        if (!token.isInterface()) {
            throw new ImplerException("token is not an interface");
        }
        String tokenPackage = (token.getPackage() == null) ? "" : token.getPackage().getName();
        String outputDir = root.toString() + File.separator + tokenPackage.replace(".", File.separator);
        Path dir = Paths.get(outputDir);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("Can't create directory");
        }
        try (PrintWriter out = new PrintWriter(outputDir + File.separator + token.getSimpleName() + "Impl.java")) {
            out.print(genClass(token));
        } catch (IOException e) {
            System.err.println("Can't write");
        }
    }

    /**
     * Generates implementation of the given interface.
     *
     * @param token interface to implement.
     * @return implementation of the given interface.
     */
    private String genClass(Class<?> token) {
        String res = (token.getPackage() == null) ? "" : "package " + token.getPackage().getName() + ";\n";
        res += "public class " + token.getSimpleName() + "Impl " + "implements " + token.getCanonicalName() + " { \n";
        HashSet<Method> methods = new HashSet<>();
        Collections.addAll(methods, token.getMethods());
        for (Method m : methods) {
            res += implementMethod(m);
        }
        res += "}";
        return res;
    }

    /**
     * Implements method and prints it.
     *
     * @param m method which we want to implement
     * @return implementation of method.
     */
    private String implementMethod(Method m) {
        String res = "";
        if (!Modifier.isAbstract(m.getModifiers()))
            return res;
        res += "\t";
        res += writeMethodModifiers(m);
        res += m.getReturnType().getCanonicalName() + " ";
        res += m.getName() + " " + "(";
        res += writeArguments(m);
        res += ") ";
        res += writeExceptions(m);
        res += "{";
        res += "\t\treturn " + getDefaultValue(m.getReturnType()) + ";";
        res += "\t}";
        res += "\n\n";
        return res;
    }

    /**
     * Writes all modifiers of method
     *
     * @param m method for which we want to know modifiers
     * @return all method modifiers
     */
    private String writeMethodModifiers(Method m) {
        String res = "";
        int mod = m.getModifiers();
        mod &= ~Modifier.ABSTRACT;
        mod &= ~Modifier.TRANSIENT;
        res = Modifier.toString(mod) + " ";
        return res;
    }

    /**
     * Writes all arguments that takes method
     *
     * @param m method for which we want to know arguments
     * @return all arguments of method
     */
    private String writeArguments(Method m) {
        String res = "";
        Class<?>[] args = m.getParameterTypes();
        int size = args.length;
        for (int i = 0; i < size; i++) {
            res += args[i].getCanonicalName() + " arg" + i;
            if (i != size - 1) {
                res += ", ";
            }
        }
        return res;
    }

    /**
     * Writes all exceptions that throws method
     *
     * @param m method for which we want to know exceptions
     * @return  all exceptions that throws method
     */
    private String writeExceptions(Method m) {
        String res = "";
        Class<?>[] exceptions = m.getExceptionTypes();
        int size = exceptions.length;
        if (exceptions.length == 0) {
            return res;
        }
        res += "throws ";
        for (int i = 0; i < size; i++) {
            res += exceptions[i].getCanonicalName();
            if (i != size - 1) {
                res += ", ";
            }
        }
        res +=" ";
        return res;
    }


    /**
     * Generates implementation of the given method.
     *
     * @param m method to implement.
     * @return implementation of the given method.
     */
    private String genMethod(Method m) {
        Class<?> returnType = m.getReturnType();
        String res = Modifier.toString(m.getModifiers()).replace("abstract", "").replace("transient", "") + " "
                + returnType.getCanonicalName() + " " + m.getName() + "(";
        Parameter[] parameters = m.getParameters();
        for (Parameter p : parameters) {
            res += p.getType().getCanonicalName() + " " + p.getName() + ", ";
        }
        if (parameters.length > 0) {
            res = res.substring(0, res.length() - 2);
        }
        res += ") { ";
        res += "return" + getDefaultValue(returnType);
        res += "} \n";
        return res;
    }

    /**
     * Returns default value of given type. If given type is {@code void} returns ";"
     *
     * @param type type to get default value.
     * @return default value of the given type.
     */
    private String getDefaultValue(Class<?> type) {
        if (type.equals(void.class)) {
            return "";
        } else if (type.isPrimitive()) {
            if (type.equals(boolean.class)) {
                return "false";
            } else {
                return "0";
            }
        } else {
            return "null";
        }
    }

}
