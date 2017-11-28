package br.com.zone.compile.app.controller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.zone.compile.app.repository.GenericRepository;
import br.com.zone.compile.app.service.CompileService;
import java.io.Serializable;
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
//        if (!isStarted) {
//            
//            isStarted = Boolean.TRUE;
//            
//            List<BaseEntity> classes = repository.listarTodos(UploadFile.class);
//            if (classes != null && !classes.isEmpty()) {
//
//                final String FORMATO = ".xhtml";
//
//                classes.forEach(c -> {
//
//                    UploadFile f = (UploadFile) c;
//
//                    try {
//
//                        if (!f.getName().endsWith(FORMATO)) {
//
//                            compileService.compileSource(f);
//
//                        } else {
//
//                            File root = new File(CompileService.getRealPath("/"));
//                            File file = new File(root, f.getName());
//                            Files.write(file.toPath(), f.getSource().getBytes(StandardCharsets.UTF_8));
//
//                        }
//
//                    } catch (ClassNotFoundException | IOException ex) {
//                        Logger.getLogger(StartAppBean.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                });
//            }
//            
//        }
        
    }

}
