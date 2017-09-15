package br.com.zone.compile.app.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import br.com.zone.compile.app.model.UploadedClass;
import br.com.zone.compile.app.repository.UploadedClassRepository;
import br.com.zone.compile.app.service.CompileService;
import br.com.zone.compile.app.util.FacesMessages;
import java.io.Serializable;
import javax.faces.view.ViewScoped;

/**
 *
 * @author daniel
 */
@Named
@ViewScoped
public class UploadClassBean implements Serializable {

    @Inject
    private FacesMessages messages;

    @Inject
    private UploadedClass uploadedClass;

    @Inject
    private CompileService compileService;
    
    @Inject
    private UploadedClassRepository repository;

    public UploadedClass getUploadedClass() {
        return uploadedClass;
    }

    public void setUploadedClass(UploadedClass uploadedClass) {
        this.uploadedClass = uploadedClass;
    }

    public void compileSouce() {
        try {
            if (compileService.compileSource(uploadedClass)) {
                messages.info("Código compilado com sucesso");
                try {
					repository.salvar(uploadedClass);
				} catch (Exception ex) {
					Logger.getLogger(UploadClassBean.class.getName()).log(Level.SEVERE, null, ex);
		            messages.error("Um erro ocorreu ao persistir o código" + ex.getMessage());
				}
            } else {
                for (Diagnostic<? extends JavaFileObject> diagnostic : compileService.getDiagnostics()) {
                    messages.error("Um erro ocorreu na compilação do arquivo" + diagnostic.getMessage(null));
                    Logger.getLogger(UploadClassBean.class.getName()).log(Level.SEVERE, null, diagnostic.getMessage(null));
                }
            }
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(UploadClassBean.class.getName()).log(Level.SEVERE, null, ex);
            messages.error("Um erro ocorreu " + ex.getMessage());
        }
    }

}
