package br.com.zone.compile.app.service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import br.com.zone.compile.app.model.UploadedClass;
import br.com.zone.compile.app.util.EntityManagerProducer;

/**
 *
 * @author daniel
 */
public class CompileService implements Serializable {

    @Inject
    private EntityManagerProducer entityManagerProducer;
    		    
    private List<Diagnostic<? extends JavaFileObject>> diagnostics;

    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        this.diagnostics = diagnostics;
    }
    
    public boolean compileSource(UploadedClass clazz) throws ClassNotFoundException, IOException {

        boolean result = false;

        final String CLASS_DIRECTORY = "WEB-INF/classes/";

        String directoryDeployClass = getDirectory(clazz.getNameFullyQualified());
        String classeName = getClassName(clazz.getNameFullyQualified());

        String directorySource = getRealPath("/") + CLASS_DIRECTORY.concat(directoryDeployClass);

        File root = new File(directorySource);
        File sourceFile = new File(root, classeName.concat(".java"));
        Files.write(sourceFile.toPath(), clazz.getSource().getBytes(StandardCharsets.UTF_8));

        File[] javaFiles = new File(directorySource).listFiles();
        List<File> javaFilesList = new ArrayList<>();

        for (File javaFile : javaFiles) {
            if (javaFile.getName().endsWith(".java")) {
                javaFilesList.add(javaFile);
            }
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(javaFilesList);

        List<File> classpath = new ArrayList<>();
        classpath.addAll(Arrays.asList(new File(getRealPath("/WEB-INF/lib/")).listFiles()));
        classpath.add(new File(directorySource + directoryDeployClass + classeName + ".class"));

        fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File(getRealPath("/") + CLASS_DIRECTORY)));

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector, null, null, compilationUnits);

        if (!task.call()) {
            diagnostics = diagnosticsCollector.getDiagnostics();            
        } else {
            Class.forName(clazz.getNameFullyQualified());
            entityManagerProducer.reload();
            result = true;
        }

        return result;

    }

    private String getDirectory(String nfq) {
        return nfq.substring(0, nfq.lastIndexOf(".")).replaceAll("\\.", Matcher.quoteReplacement(File.separator));
    }

    private String getClassName(String nfq) {
        return nfq.substring(nfq.lastIndexOf(".") + 1);
    }

    private String getRealPath(String resource) {
        return ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath(resource);
    }

}
