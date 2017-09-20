package br.com.zone.compile.app.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import br.com.zone.compile.app.model.UploadFile;
import br.com.zone.compile.app.repository.GenericRepository;
import br.com.zone.compile.app.service.CompileService;
import br.com.zone.compile.app.util.FacesMessages;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

/**
 *
 * @author daniel
 */
@Named(value = "uploadFileBean")
@ViewScoped
public class UploadFileBean implements Serializable {

    @Inject
    private FacesMessages messages;

    private UploadFile uploadedFile;

    @Inject
    private CompileService compileService;

    @Inject
    private GenericRepository repository;

    @PostConstruct
    public void init() {
        uploadedFile = new UploadFile();
    }

    public UploadFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public void compileSouce() {
        try {
            if (compileService.compileSource(uploadedFile)) {
                messages.info("Código compilado com sucesso");
                try {
                    repository.salvar(uploadedFile);
                    uploadedFile = new UploadFile();
                } catch (Exception ex) {
                    Logger.getLogger(UploadFileBean.class.getName()).log(Level.SEVERE, null, ex);
                    messages.error("Um erro ocorreu ao persistir o código" + ex.getMessage());
                }
            } else {
                for (Diagnostic<? extends JavaFileObject> diagnostic : compileService.getDiagnostics()) {
                    messages.error("Um erro ocorreu na compilação do arquivo" + diagnostic.getMessage(null));
                    Logger.getLogger(UploadFileBean.class.getName()).log(Level.SEVERE, null, diagnostic.getMessage(null));
                }
            }
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(UploadFileBean.class.getName()).log(Level.SEVERE, null, ex);
            messages.error("Um erro ocorreu " + ex.getMessage());
        }
    }

    public void uploadXhtml() {
        try {
            File root = new File(CompileService.getRealPath("/"));
            File file = new File(root, uploadedFile.getName().concat(".xhtml"));
            Files.write(file.toPath(), uploadedFile.getSource().getBytes(StandardCharsets.UTF_8));

            messages.info("Código enviado com sucesso");

            try {
                repository.salvar(uploadedFile);
                uploadedFile = new UploadFile();
            } catch (Exception ex) {
                Logger.getLogger(UploadFileBean.class.getName()).log(Level.SEVERE, null, ex);
                messages.error("Um erro ocorreu ao persistir o código" + ex.getMessage());
            }

        } catch (IOException ex) {
            Logger.getLogger(UploadFileBean.class.getName()).log(Level.SEVERE, null, ex);
            messages.error("Um erro ocorreu " + ex.getMessage());
        }
    }

}
