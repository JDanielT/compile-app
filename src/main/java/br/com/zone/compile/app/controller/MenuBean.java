package br.com.zone.compile.app.controller;

import br.com.zone.compile.app.model.BaseEntity;
import br.com.zone.compile.app.model.UploadFile;
import br.com.zone.compile.app.repository.GenericRepository;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author daniel
 */
@Named
@ViewScoped
public class MenuBean implements Serializable {
    
    @Inject
    private GenericRepository repository;
    
    public List<UploadFile> getUploadFiles(){
        List<BaseEntity> bases = repository.listarTodos(UploadFile.class);
        List<UploadFile> uploadFiles = new ArrayList<>();
        bases.stream()
                .map((b) -> (UploadFile) b)
                .filter((u) -> (u.getName().endsWith(".xhtml")))
                .forEachOrdered((u) -> {
            uploadFiles.add(u);
        });
        return uploadFiles;
    }
    
}
