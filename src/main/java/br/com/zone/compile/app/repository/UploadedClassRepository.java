package br.com.zone.compile.app.repository;

import br.com.zone.compile.app.model.UploadedClass;

public class UploadedClassRepository extends GenericRepository<UploadedClass> {

    public UploadedClassRepository() {
        super(UploadedClass.class);
    }

}
