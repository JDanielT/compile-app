package br.com.zone.compile.app.controller;

import br.com.zone.compile.app.model.Atributo;
import br.com.zone.compile.app.model.Classe;
import br.com.zone.compile.app.model.TipoAtributo;
import br.com.zone.compile.app.model.UploadFile;
import br.com.zone.compile.app.repository.GenericRepository;
import br.com.zone.compile.app.service.CompileService;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author daniel
 */
@Named
@ViewScoped
public class ClasseBean extends AbstractBean {

    @Inject
    private CompileService compileService;
    
    @Inject
    private GenericRepository repository;
    
    @Inject
    private Atributo atributo;

    public ClasseBean() {
    }

    public Atributo getAtributo() {
        return atributo;
    }

    public void setAtributo(Atributo atributo) {
        this.atributo = atributo;
    }

    @Override
    public void preCadastro() {
        super.preCadastro();
        ((Classe)getEntity()).setAtributos(new ArrayList<>());
    }

    @Override
    public String salvar() {
        
        Classe c = (Classe) getEntity();
        
        UploadFile uploadFile = new UploadFile();
        uploadFile.setName("br.com.zone.compile.app.model." + c.getNome());
        uploadFile.setSource(c.toJava());
        
        try {
            if(compileService.compileSource(uploadFile)){                
                repository.salvar(c);
                repository.salvar(uploadFile);
            }else{
                messages.error("Ocorreram erros na compilação:");
                compileService.getDiagnostics().forEach(d -> {messages.error("Erro: " + d.getMessage(Locale.ENGLISH));});
            }
        } catch (Exception ex) {
            Logger.getLogger(ClasseBean.class.getName()).log(Level.SEVERE, null, ex);
            messages.error("Um erro ocorreu. Erro: " + ex.getMessage());
        }
        
        super.limparDados();
        
        return null;
        
    }
    
    /**
     * Métodos relativos a atributos
     */
    public void adicionarAtributo() {
        if (atributo.getRotulo()!= null) {

            atributo.setClasse((Classe)getEntity());
            
            ((Classe)getEntity()).getAtributos().add(atributo);
            atributo = new Atributo();

        }
    }

    public void excluirAtributo(Atributo a) {
        ((Classe)getEntity()).getAtributos().remove(a);
        messages.info("Atributo excluído!");
    }

    public TipoAtributo[] getTiposAtributos() {
        return TipoAtributo.values();
    }

}
