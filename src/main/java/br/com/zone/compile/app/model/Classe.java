package br.com.zone.compile.app.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80)
    private String nome;

    @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Atributo> atributos;

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

    public String toJava() {

        StringBuilder retorno = new StringBuilder();

        Path path = Paths.get(getClass().getClassLoader().getResource("META-INF/template-classe").getFile());

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
                        
                        linha = linha.concat("\nprivate " + atributo.getTipo().getCodigo() + " " + atributo.getNome() + ";");

                        linha = linha.concat("\npublic " + atributo.getTipo().getCodigo() + " get" + this.capitalize(nomeAtributo) + "(){ return " + nomeAtributo +"; }");
                        
                        linha = linha.concat("\npublic void set" + this.capitalize(nomeAtributo) + "(" + atributo.getTipo().getCodigo() + " param) { " + nomeAtributo +" = param; }");

                    }

                }

                classe.add(linha);

            }

            classe.forEach(linha -> retorno.append(linha));

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
