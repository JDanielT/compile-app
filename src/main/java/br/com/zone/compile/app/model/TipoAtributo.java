package br.com.zone.compile.app.model;

/**
 *
 * @author daniel
 */
public enum TipoAtributo {
    
    STRING("String"), INT("int"), DOUBLE("double");
    
    private String codigo;

    private TipoAtributo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

}
