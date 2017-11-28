package br.com.zone.compile.app.controller;

import br.com.zone.compile.app.model.BaseEntity;
import br.com.zone.compile.app.repository.GenericRepository;
import br.com.zone.compile.app.util.FacesMessages;
import org.hibernate.exception.ConstraintViolationException;

import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author daniel
 * 
 */
@Named
@ViewScoped
public class AbstractBean implements Serializable {

    private String className;
    
    private BaseEntity entity;
    protected Collection<BaseEntity> itens;
    
    @Inject
    private GenericRepository repository;

    @Inject
    protected FacesMessages messages;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public BaseEntity getEntity() {
        return entity;
    }

    public void setEntity(BaseEntity entity) {
        this.entity = entity;
    }

    public Collection<BaseEntity> getItens() {
        if (itens == null) {
            itens = getRepository().listarTodos(getEntityClass());
        }
        return itens;
    }

    public void setItens(Collection<BaseEntity> itens) {
        this.itens = itens;
    }

    public void preCadastro() {
        try {
            entity = (BaseEntity) getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(AbstractBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String salvar() {

        String pagina = null;

        try {

            getRepository().salvar(entity);

            limparDados();

        } catch (Exception ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);

            if (ex.getCause() instanceof ConstraintViolationException) {
                messages.error("Ooops, Registro já cadastrado!");
            } else {
                messages.error("Erro: " + ex.getMessage());
            }
        }

        return pagina;

    }

    public String excluir() {

        String pagina = null;

        try {

            getRepository().excluir(entity, getEntityClass());

        } catch (Exception ex) {
            messages.error("Esse registro está sendo utilizado em outra tabela");
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        limparDados();

        return pagina;

    }
    
    private Class getEntityClass(){
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AbstractBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return clazz;
    }
    
    public void limparDados() {
        itens = null;
        entity = null;
    }

    protected GenericRepository getRepository() {
        return repository;
    }
}