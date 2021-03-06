package br.com.zone.compile.app.controller;

import br.com.zone.compile.app.model.BaseEntity;
import br.com.zone.compile.app.model.UploadFile;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.zone.compile.app.repository.GenericRepository;
import br.com.zone.compile.app.service.CompileService;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;

@Named
@ApplicationScoped
public class StartAppBean implements Serializable {

    @Inject
    private CompileService compileService;

    @Inject
    private GenericRepository repository;

    private boolean isStarted = Boolean.FALSE;;

    public void init() {
        if (!isStarted) {
            
            isStarted = Boolean.TRUE;
            
            List<BaseEntity> classes = repository.listarTodos(UploadFile.class);
            if (classes != null && !classes.isEmpty()) {

                final String FORMATO = ".xhtml";

                classes.forEach(c -> {

                    UploadFile f = (UploadFile) c;

                    try {

                        if (!f.getName().endsWith(FORMATO)) {

                            compileService.compileSource(f);

                        } else {

                            File root = new File(CompileService.getRealPath("/"));
                            File file = new File(root, f.getName());
                            Files.write(file.toPath(), f.getSource().getBytes(StandardCharsets.UTF_8));

                        }

                    } catch (ClassNotFoundException | IOException ex) {
                        Logger.getLogger(StartAppBean.class.getName()).log(Level.SEVERE, null, ex);
                    }

                });
            }
            
        }
        
    }

}
