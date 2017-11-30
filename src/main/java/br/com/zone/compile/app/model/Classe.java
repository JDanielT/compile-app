package br.com.zone.compile.app.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author daniel
 */
@Entity
@Table(name = "classe")
public class Classe implements BaseEntity {

    public final static String CLASS_NAME = "_CLASS_NAME_";
    public final static String TABLE_NAME = "_TABLE_NAME_";
    public final static String ATTR_START = "_ATTR_START_";
    public final static String COLUMNS_START_TABLE = "_COLUMNS_START_TABLE_";
    public final static String FIELDS_START = "_FIELDS_START_";
    public final static String TO_STRING = "_TO_STRING_";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80)
    private String nome;

    @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Atributo> atributos;

    @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UploadFile> files;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Atributo> getAtributos() {
        return atributos;
    }

    public void setAtributos(List<Atributo> atributos) {
        this.atributos = atributos;
    }

    public List<UploadFile> getFiles() {
        return files;
    }

    public void setFiles(List<UploadFile> files) {
        this.files = files;
    }

    public String toJava() {

        StringBuilder retorno = new StringBuilder();
        
        boolean IS_WINDOWS = System.getProperty("os.name").contains("indow");
        String caminho = getClass().getClassLoader().getResource("META-INF/template-classe").getFile();
        
        Path path = Paths.get(IS_WINDOWS ? caminho.substring(1) : caminho);

        try {

            List<String> template = Files.readAllLines(path);
            List<String> classe = new ArrayList<>();

            for (String linha : template) {

                if (linha.contains(Classe.CLASS_NAME)) {
                    linha = linha.replaceAll(Classe.CLASS_NAME, this.nome);
                }

                if (linha.contains(Classe.TABLE_NAME)) {
                    linha = linha.replaceAll(Classe.TABLE_NAME, this.nome.toLowerCase());
                }

                if (linha.contains(Classe.ATTR_START) && atributos != null) {

                    for (Atributo atributo : atributos) {

                        String nomeAtributo = atributo.getNome();

                        linha = linha.concat("\nprivate " + atributo.getTipo() + " " + atributo.getNome() + ";");

                        linha = linha.concat("\npublic " + atributo.getTipo() + " get" + this.capitalize(nomeAtributo) + "(){ return " + nomeAtributo + "; }");

                        linha = linha.concat("\npublic void set" + this.capitalize(nomeAtributo) + "(" + atributo.getTipo() + " param) { " + nomeAtributo + " = param; }");

                    }

                }
                
                if(linha.contains(TO_STRING)){

                    linha = linha.concat("\npublic String toString(){ return ");
                    
                    linha = linha.concat(concatAtrrs(atributos.stream().filter(a -> a.isMain()).collect(Collectors.toList())));
                    
                    linha = linha.concat("; }");
                    
                }

                classe.add(linha);

            }

            classe.forEach(linha -> {
                retorno.append(linha);
            });

        } catch (IOException ex) {
            Logger.getLogger(Classe.class.getName()).log(Level.SEVERE, null, ex);
        }

        return retorno.toString();

    }
    
    private String concatAtrrs(List<Atributo> atributos){
        String retorno = new String();
        for(int i = 0; i < atributos.size(); i++){
            retorno = retorno + atributos.get(i).getNome() + (atributos.size() == i  + 1 ? "" : " + \" - \" + ");
        }
        return retorno;
    }

    public String toXHTML() {

        StringBuilder retorno = new StringBuilder();

        boolean IS_WINDOWS = System.getProperty("os.name").contains("indow");
        String caminho = getClass().getClassLoader().getResource("META-INF/template-xhtml").getFile();
        
        Path path = Paths.get(IS_WINDOWS ? caminho.substring(1) : caminho);

        try {

            List<String> template = Files.readAllLines(path);
            List<String> xhtml = new ArrayList<>();

            for (String linha : template) {

                if (linha.contains(Classe.CLASS_NAME)) {
                    linha = linha.replaceAll(Classe.CLASS_NAME, this.nome);
                }

                if (linha.contains(Classe.TABLE_NAME)) {
                    linha = linha.replaceAll(Classe.TABLE_NAME, this.nome.toLowerCase());
                }

                if (linha.contains(Classe.COLUMNS_START_TABLE) && atributos != null) {

                    for (Atributo atributo : atributos) {

                        String nomeAtributo = atributo.getNome();
                        String rotuloAtributo = atributo.getRotulo();

                        String column = "\n<p:column headerText=\"" + rotuloAtributo + "\" >\n"
                                        + "<h:outputText value=\"#{item." + nomeAtributo + "}\" />\n"
                                        + "</p:column>";

                        linha = linha.concat(column);

                    }

                }

                if (linha.contains(Classe.FIELDS_START) && atributos != null) {

                    for (Atributo atributo : atributos) {

                        String nomeAtributo = atributo.getNome();
                        String rotuloAtributo = atributo.getRotulo();
                        String tipo = atributo.getTipo();

                        String input = null;

                        if (tipo.endsWith(Atributo.DATE)) {

                            input = "\n<div>\n"
                                    + "<p:outputLabel value=\"" + rotuloAtributo + "\" /><br/>\n"
                                    + "<p:inputMask id=\"date\" value=\"#{abstractBean.entity." + nomeAtributo + "}\" mask=\"99/99/9999\" >\n"
                                    + "<f:convertDateTime pattern=\"dd/MM/yyyy\" />"
                                    + "</p:inputMask>"
                                    + "</div>";

                        } else if (!Arrays.asList(Atributo.tipos).contains(tipo)) {

                            input = "\n<div>\n"
                                    + "<p:outputLabel value=\"" + rotuloAtributo + "\" /><br/>\n"
                                    + "<p:selectOneMenu value=\"#{abstractBean.entity." + nomeAtributo + "}\" converter=\"simpleEntityConverter\" >\n"
                                    + "<f:selectItems value=\"#{abstractBean.repository.listarTodos('br.com.zone.compile.app.model." + tipo + "')}\" "
                                    + " var=\"item\" itemLabel=\"#{item.toString()}\" itemValue=\"#{item}\" /> "
                                    + "</p:selectOneMenu>\n"
                                    + "</div>";

                        } else {

                            input = "\n<div>\n"
                                    + "<p:outputLabel value=\"" + rotuloAtributo + "\" /><br/>\n"
                                    + "<p:inputText value=\"#{abstractBean.entity." + nomeAtributo + "}\">\n"
                                    + "</p:inputText>"
                                    + "</div>";

                        }

                        linha = linha.concat(input);

                    }

                }

                xhtml.add(linha);

            }

            xhtml.forEach(linha -> retorno.append(linha));

        } catch (IOException ex) {
            Logger.getLogger(Classe.class.getName()).log(Level.SEVERE, null, ex);
        }

        return retorno.toString();

    }

    private String capitalize(final String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Classe other = (Classe) obj;
        return Objects.equals(this.id, other.id);
    }
    
}
