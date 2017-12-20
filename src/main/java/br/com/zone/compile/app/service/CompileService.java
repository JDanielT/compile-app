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

import br.com.zone.compile.app.model.UploadFile;
import br.com.zone.compile.app.repository.GenericRepository;
import br.com.zone.compile.app.util.EntityManagerProducer;
import org.jboss.logging.Logger;

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

    public boolean compileSource(UploadFile clazz) throws ClassNotFoundException, IOException {

        boolean result = false;

        //Todos os .class devem ser colocados nesse diretório após a compilação
        final String CLASS_DIRECTORY = "WEB-INF/classes/";

        //Buscando o diretório onde a classe ficará baseada no nome do pacote
        String directoryDeployClass = getDirectory(clazz.getName());

        Logger.getLogger(CompileService.class.getName()).log(Logger.Level.INFO, "CLASS DIRECTORY " + directoryDeployClass);

        //Buscando o nome da classe não qualificado (sem o prefixo de pacotes)
        String classeName = getClassName(clazz.getName());

        //Buscando o diretório onde os fontes .java estão. Vide comentários do getRealPath 
        String directorySource = getRealPath("/") + CLASS_DIRECTORY.concat(directoryDeployClass);

        Logger.getLogger(CompileService.class.getName()).log(Logger.Level.INFO, "SOURCE DIRECTORY " + directorySource);

        //Salvando .java enviado no diretório de sources
        File root = new File(directorySource);
        File sourceFile = new File(root, classeName.concat(".java"));
        Files.write(sourceFile.toPath(), clazz.getSource().getBytes(StandardCharsets.UTF_8));

        //Listando arquivos do diretório source a fim de usá-los no classpath para compilaçao do código enviado
        File[] javaFiles = new File(directorySource).listFiles();
        
        Logger.getLogger(CompileService.class.getName()).log(Logger.Level.INFO, "SOURCE DIRECTORY " + new File(directorySource).getAbsolutePath());
        
        List<File> javaFilesList = new ArrayList<>();

        //Filtrando apenas arquivos .java (evita que .class sejam adicionado ao classpath que causaria um erro)
        for (File javaFile : javaFiles) {
            if (javaFile.getName().endsWith(".java")) {
                Logger.getLogger(CompileService.class.getName()).log(Logger.Level.INFO, ".Java Files " + javaFile.getAbsolutePath());
                javaFilesList.add(javaFile);
            }
        }

        //Recuperando instância do compilador
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        //Lista para armazenar possíveis erros de compilação
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);

        //Mapeando os arquivos .java encontrados no classpath para objetos Java
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(javaFilesList);

        //Adicionando libs do projeto ao classpath
        List<File> classpath = new ArrayList<>();
        classpath.addAll(Arrays.asList(new File(getRealPath("/WEB-INF/lib/")).listFiles()));
        classpath.add(new File(directorySource + directoryDeployClass + classeName + ".class"));

        //Setando classpath ao fileManager
        fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);

        //Setando o diretório de saída da compilação do arquivo enviado
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File(getRealPath("/") + CLASS_DIRECTORY)));

        //Compilando arquivo
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector, null, null, compilationUnits);

        //Se retornar false, então temos erros de compilação que são adicionados ao 'diagnostics'
        if (!task.call()) {
            diagnostics = diagnosticsCollector.getDiagnostics();
        } else {
             // Caso a compilação seja executada com sucesso, carrega a classe rescem compilada
            Class.forName(clazz.getName());
            // Força um reload do hibernate a fim de criar tabelas no banco, caso necessário
            entityManagerProducer.reload();
            result = true;
        }

        return result;

    }

    /**
     * Método para recuperar o diretório de uma classe, baseado na nomeclatura
     * dos pacotes.
     *
     * @return String contendo o diretório
     */
    private String getDirectory(String nfq) {
        return nfq.substring(0, nfq.lastIndexOf(".")).replaceAll("\\.", Matcher.quoteReplacement(File.separator));
    }

    /**
     * Recupera o nome da classe (sem o prexifo de pacotes).
     *
     * @return String com o nome da classe não qualificado
     */
    private String getClassName(String nfq) {
        return nfq.substring(nfq.lastIndexOf(".") + 1);
    }

    /**
     * Busca o caminho absoluto de onte a aplicação está sendo executada.
     *
     * @return String com o caminho de execução da app
     */
    public static String getRealPath(String resource) {
        return ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath(resource);
    }

}
