package br.com.zone.compile.app.controller;

import br.com.zone.compile.app.model.Atributo;
import br.com.zone.compile.app.model.Classe;
import br.com.zone.compile.app.model.UploadFile;
import br.com.zone.compile.app.repository.GenericRepository;
import br.com.zone.compile.app.service.CompileService;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.faces.context.FacesContext;
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
        ((Classe) getEntity()).setAtributos(new HashSet<>());
        ((Classe) getEntity()).setFiles(new HashSet<>());
    }

    @Override
    public String salvar() {

        Classe c = (Classe) getEntity();

        if (c.getAtributos().stream().filter(a -> a.isMain()).collect(Collectors.toList()).isEmpty()) {
            messages.error("É necessário pelo menos um atributo principal!");
            FacesContext.getCurrentInstance().validationFailed();
            return null;
        }

        c.getFiles().clear();

        UploadFile javaFile = new UploadFile();
        javaFile.setName("br.com.zone.compile.app.model." + c.getNome());
        javaFile.setSource(c.toJava());
        javaFile.setClasse(c);

        UploadFile xhtmlFile = new UploadFile();
        xhtmlFile.setName(c.getNome() + ".xhtml");
        xhtmlFile.setSource(c.toXHTML());
        xhtmlFile.setClasse(c);

        try {
            if (compileService.compileSource(javaFile)) {

                if (c.getId() == null) {
                    repository.executeUpdateNativeQuery(c.toDDL());
                } else {
                    c.getAtributos().stream().filter(a -> a.getId() == null).forEach(a -> {

                        String ddl = "ALTER TABLE " + c.getNome().toLowerCase() + " ADD ";
                        
                        if (a.getTipo().endsWith(Atributo.DATE)) {

                            ddl += a.getNome() + " DATE ";

                        } else if (a.getTipo().endsWith(Atributo.DOUBLE)) {

                            ddl += a.getNome() + " DOUBLE ";

                        } else if (a.getTipo().endsWith(Atributo.INTEGER)) {

                            ddl += a.getNome() + " INT ";

                        } else if (a.getTipo().endsWith(Atributo.STRING)) {

                            ddl += a.getNome() + " VARCHAR(255) ";

                        } else if (!Arrays.asList(Atributo.tipos).contains(a.getTipo())) {


                            ddl += a.getNome() + "_id BIGINT, ADD CONSTRAINT FOREIGN KEY (" + a.getNome() + "_id)" + " REFERENCES " + a.getTipo().toLowerCase() + "(id) ";

                        }
                                                
                        repository.executeUpdateNativeQuery(ddl);

                    });
                }

                this.uploadXhtml(xhtmlFile);

                c.getFiles().add(javaFile);
                c.getFiles().add(xhtmlFile);

                repository.salvar(c);

            } else {
                messages.error("Ocorreram erros na compilação:");
                compileService.getDiagnostics().forEach(d -> {
                    messages.error("Erro: " + d.getMessage(Locale.ENGLISH));
                });
            }
        } catch (Exception ex) {
            Logger.getLogger(ClasseBean.class.getName()).log(Level.SEVERE, null, ex);
            messages.error("Um erro ocorreu. Erro: " + ex.getMessage());
        }
        
        limparDados();

        return null;

    }

    @Override
    public String excluir() {
        Classe c = (Classe) getEntity();
        repository.executeUpdateNativeQuery("DROP DATATABLE "+ c.getNome().toLowerCase());
        return super.excluir();
    }
    

    private void uploadXhtml(UploadFile xhtml) throws IOException, Exception {

        File root = new File(CompileService.getRealPath("/"));
        File file = new File(root, xhtml.getName());
        Files.write(file.toPath(), xhtml.getSource().getBytes(StandardCharsets.UTF_8));

    }

    @Override
    public void limparDados() {
        super.limparDados();
        atributo = new Atributo();
    }

    /**
     * Métodos relativos a atributos
     */
    public void adicionarAtributo() {
        if (atributo.getRotulo() != null && !atributo.getRotulo().isEmpty()) {

            atributo.setClasse((Classe) getEntity());

            ((Classe) getEntity()).getAtributos().remove(atributo);
            ((Classe) getEntity()).getAtributos().add(atributo);
            atributo = new Atributo();

        }
    }

    public void preEditarAtributo(Atributo a) {
        this.atributo = a;
    }

    public void excluirAtributo(Atributo a) {
        ((Classe) getEntity()).getAtributos().remove(a);
        messages.info("Atributo excluído!");
    }

    public List<String> getTiposAtributos() {
        List<String> tipos = new ArrayList<>();
        tipos.addAll(Arrays.asList(Atributo.tipos));
        repository.listarTodos(Classe.class).forEach((c) -> {
            tipos.add(((Classe) c).getNome());
        });
        return tipos;
    }

}
