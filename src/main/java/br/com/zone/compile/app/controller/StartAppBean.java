package br.com.zone.compile.app.controller;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.omnifaces.cdi.Eager;

import br.com.zone.compile.app.model.UploadedClass;
import br.com.zone.compile.app.repository.UploadedClassRepository;
import br.com.zone.compile.app.service.CompileService;
import br.com.zone.compile.app.util.FacesMessages;
import java.io.Serializable;

@Eager
@ApplicationScoped
public class StartAppBean implements Serializable {

    @Inject
    private CompileService compileService;

    @Inject
    private UploadedClassRepository repository;

    @Inject
    private FacesMessages messages;

    @PostConstruct
    public void init() {
        System.out.println(" **************************** INICIOU ******************************* ");
        List<UploadedClass> classes = repository.listarTodos();
        if (classes != null && !classes.isEmpty()) {
            classes.forEach(c -> {
                try {
                    compileService.compileSource(c);
                } catch (ClassNotFoundException | IOException ex) {
                    Logger.getLogger(UploadClassBean.class.getName()).log(Level.SEVERE, null, ex);
                    messages.error("Um erro ocorreu " + ex.getMessage());
                }
            });
        }
    }

}
