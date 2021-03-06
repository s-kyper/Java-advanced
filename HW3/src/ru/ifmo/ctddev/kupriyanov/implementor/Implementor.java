package ru.ifmo.ctddev.kupriyanov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Created by pinkdonut on 21.03.16.
 */

public class Implementor implements JarImpler {

    /**
     * Stream for writing .java file that was generated by class
     * @see java.io.PrintWriter
     */
    private PrintWriter writer;

    /**
     * String that contains a name of class which we want to generate
     */
    private String className;

    /**
     * Class for which we want to generate an implementation
     * @see java.lang.Class
     */
    Class<?> clazz;

    /**
     * Starts to generate an implementation of the interface
     * @throws FileNotFoundException if interface was not found
     */
    private void generate() throws FileNotFoundException {
        if (clazz.isInterface())
            generateInterface();
        writer.close();
    }

    /**
     * Generates class from interface and prints it;
     */
    private void generateInterface() {
        if (clazz.getPackage() != null) {
            writer.println("package " + clazz.getPackage().getName() + ";");
            writer.println();
        }
        writer.println("class " + className + "Impl implements " + clazz.getCanonicalName() + " {");
        HashSet<Method> methods = new HashSet<Method>();
        Collections.addAll(methods, clazz.getMethods());
        for (Method m : methods) {
            implementMethod(m);
        }
        writer.println("}");
    }

    /**
     * Implements method and prints it.
     * @param m method which we want to implement
     * @see java.lang.reflect.Method
     */
    private void implementMethod(Method m) {
        if (!Modifier.isAbstract(m.getModifiers()))
            return;
        writer.print("\t");
        writeMethodModifiers(m);
        writer.print(m.getReturnType().getCanonicalName() + " ");
        writer.print(m.getName() + " ");
        writer.print("(");
        writeArguments(m);
        writer.print(") ");
        writeExceptions(m);
        writer.println("{");
        writer.println("\t\treturn " + getDefaultValue(m.getReturnType()) + ";");
        writer.println("\t}");
        writer.print("\n\n");
    }

    /**
     * Writes all modifiers of method
     * @param m method for which we want to know modifiers
     * @see java.lang.reflect.Method
     * @see java.lang.reflect.Modifier
     */
    private void writeMethodModifiers(Method m) {
        int mod = m.getModifiers();
        mod &= ~Modifier.ABSTRACT;
        mod &= ~Modifier.TRANSIENT;
        writer.print(Modifier.toString(mod) + " ");
    }

    /**
     * Writes all arguments that takes method
     * @param m method for which we want to know arguments
     * @see java.lang.reflect.Method
     */
    private void writeArguments(Method m) {
        Class<?>[] args = m.getParameterTypes();
        int size = args.length;
        for (int i = 0; i < size; i++) {
            writer.print(args[i].getCanonicalName() + " arg" + i);
            if (i != size - 1)
                writer.print(", ");
        }
    }

    /**
     * Writes all exceptions that throws method
     * @param m method for which we want to know exceptions
     * @see java.lang.reflect.Method
     */
    private void writeExceptions(Method m) {
        Class<?>[] exceptions = m.getExceptionTypes();
        int size = exceptions.length;
        if (exceptions.length == 0)
            return;
        writer.print("throws ");
        for (int i = 0; i < size; i++) {
            writer.print(exceptions[i].getCanonicalName());
            if (i != size - 1)
                writer.print(", ");
        }
        writer.print(" ");
    }

    /**
     * Returns a default value for class
     * @param clazz a class or interface for which we want to know default value
     * @return string with default value
     * @see java.lang.Class
     * @see java.lang.String
     */
    private String getDefaultValue(Class<?> clazz) {
        if (clazz.equals(void.class)) {
            return "";
        } else if (clazz.isPrimitive()) {
            if (clazz.equals(boolean.class)) {
                return "false";
            } else {
                return "0";
            }
        } else {
            return "null";
        }
    }

    /**
     * Takes interface and root and make an implementation of interface and writes it to the root
     * @param token type token to create implementation for
     * @param root root directory
     * @throws ImplerException When impossible to create implementation for input token
     * @see java.lang.Class
     * @see java.io.File
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        int mod = token.getModifiers();
        if (Modifier.isFinal(mod) || token.isPrimitive()) {
            throw new ImplerException();
        }

        try {
            String path = "";
            if (token.getPackage() != null) {
                path = root.getFileName() + "/" + token.getPackage().getName().replaceAll("\\.", "/");
            } else {
                path = root.getFileName().toString();
            }

            File f = new File(path);
            f.mkdirs();
            f = new File(path + "/" + token.getSimpleName() + "Impl.java");
            writer = new PrintWriter(f);
            clazz = token;
            className = token.getSimpleName();
            generate();
        } catch (Exception e) {
            System.out.println("Error implementing");
        }
    }

    /**
     * Implements class and then make jar with its implementation.
     * @param token type token to create implementation for.
     * @param root   file path where we need to write our jar file
     * @throws ImplerException in case we cant implement this class.
     * @see java.lang.Class
     * @see java.io.File
     */
    public void implementJar(Class<?> token, Path root) throws ImplerException {
        try {
            String path = "tmp/";
            if (token.getPackage() != null) {
                path += token.getPackage().getName().replaceAll("\\.", "/");
                path += "/";
            }

            File f = new File(path);
            f.mkdirs();
            path += token.getSimpleName() + "Impl.java";
            writer = new PrintWriter(path);
            clazz = token;
            className = token.getSimpleName();
            generate();
            compileFile(path);
            String classPath = path.substring(0, path.indexOf(".java")) + ".class";
            createJar(root.toString(), classPath);
            f = new File("tmp/");
            recDelete(f);
        } catch (IOException e) {
            System.out.println("Error in implementing");
        }
    }

    /**
     * Compiles file. Compiled file will be placed to the default directory.
     * @param path file which we want to compile
     * @throws IOException in case of something bad happened when we were trying to make a jar
     * @see java.io.File
     */
    private static void compileFile(String path) throws IOException {
        File f = new File(path);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        File[] files = {f};
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));
        compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
    }

    /**
     * Makes jar from compiled class file with our implementation.
     * @param classPath path to compiled file with implementation.
     * @param jarPath   path to place where jar will be placed
     * @throws IOException in case of something bad happened when we were trying to make a jar
     */
    private static void createJar(String jarPath, String classPath) throws IOException {
        File jarFile = new File(jarPath);
        File parent = jarFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        jarFile.createNewFile();
        String newClassPath = classPath.substring(classPath.indexOf("tmp/") + 4);
        FileOutputStream fout = new FileOutputStream(jarFile);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream jarOutputStream = new JarOutputStream(fout, manifest);
        jarOutputStream.putNextEntry(new ZipEntry(newClassPath));
        FileInputStream fit = new FileInputStream(classPath);
        BufferedInputStream bis = new BufferedInputStream(fit);
        byte[] buff = new byte[1024];
        int bytesRead;
        while ((bytesRead = bis.read(buff)) != -1) {
            jarOutputStream.write(buff, 0, bytesRead);
        }
        jarOutputStream.closeEntry();
        jarOutputStream.close();
        fout.close();
    }

    /**
     * Deletes directory and all files in it.
     * @param file path to directory or file to delete.
     */
    private void recDelete(File file) {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list != null) {
                for (File f : list) {
                    recDelete(f);
                }
            }
        }
        file.delete();
    }

    /**
     * Starts Implementor with arguments
     * @param args arg[0] - interface to implement, arg[1] - path to place where jar must be placed
     */
    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            System.out.println("wrong args");
            return;
        }
        try {
            String name, filename;
            if (args.length == 3) {
                if (!"-jar".equals(args[0])) {
                    System.out.println("wrong flag");
                    return;
                }
                name = args[1];
                filename = args[2].substring(0, args[2].indexOf(".jar"));
            } else {
                name = args[0];
                filename = args[1];
            }
            File root = new File(filename);
            Class<?> token = Class.forName(name);
            Implementor implementor = new Implementor();
            if (args.length == 3) {
                implementor.implementJar(token, new File(args[2]).toPath());
            } else {
                implementor.implement(token, root.toPath());
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
        } catch (ImplerException e) {
            System.out.println("Some errors");
        }
    }
}