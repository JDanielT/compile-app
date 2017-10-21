package br.com.zone.compile.app.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import br.com.zone.compile.app.model.UploadFile;
import br.com.zone.compile.app.repository.GenericRepository;
import br.com.zone.compile.app.service.CompileService;
import br.com.zone.compile.app.util.FacesMessages;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 *
 * @author daniel
 */
@Named(value = "uploadFileBean")
@ViewScoped
public class UploadFileBean implements Serializable {

    @Inject
    private FacesMessages messages;

    private UploadFile sourceJava;
    private UploadFile sourceXhtml;

    @Inject
    private CompileService compileService;

    @Inject
    private GenericRepository repository;

    @PostConstruct
    public void init() {
        sourceJava = new UploadFile();
        sourceXhtml = new UploadFile();
    }

    public UploadFile getSourceJava() {
        return sourceJava;
    }

    public void setSourceJava(UploadFile sourceJava) {
        this.sourceJava = sourceJava;
    }

    public UploadFile getSourceXhtml() {
        return sourceXhtml;
    }

    public void setSourceXhtml(UploadFile sourceXhtml) {
        this.sourceXhtml = sourceXhtml;
    }

    public void upload() {
        try {
            if (!this.uploadJava()) {
                List<Diagnostic<? extends JavaFileObject>> diagnostics = compileService.getDiagnostics();
                messages.error("Ocorreram erros de compilação");
                diagnostics.forEach(d -> {
                    messages.error("Erro: " + d.getMessage(Locale.ENGLISH));
                });
                return;
            }
            this.uploadXhtml();
            messages.info("Código enviado com sucesso");
        } catch (Exception ex) {
            Logger.getLogger(UploadFileBean.class.getName()).log(Level.SEVERE, null, ex);
            messages.error("Um erro ocorreu: " + ex.getMessage());
        }
    }

    private boolean uploadJava() throws Exception {
        boolean resultado;
        if (resultado = compileService.compileSource(sourceJava)) {
            repository.salvar(sourceJava);
            sourceJava = new UploadFile();
        }
        return resultado;
    }

    private void uploadXhtml() throws IOException, Exception {

        final String FORMATO = ".xhtml";

        File root = new File(CompileService.getRealPath("/"));
        File file = new File(root, sourceXhtml.getName().concat(FORMATO));
        Files.write(file.toPath(), sourceXhtml.getSource().getBytes(StandardCharsets.UTF_8));

        sourceXhtml.setName(sourceXhtml.getName().concat(FORMATO));
        repository.salvar(sourceXhtml);
        sourceXhtml = new UploadFile();

    }

}
