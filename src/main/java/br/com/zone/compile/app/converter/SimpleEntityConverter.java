package br.com.zone.compile.app.converter;

import br.com.zone.compile.app.model.BaseEntity;

import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "simpleEntityConverter")
public class SimpleEntityConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext ctx, UIComponent component, String value) {
        try {
            if (value != null) {
                return this.getAttributesFrom(component).get(value);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent component, Object value) {
        try {
            if (value != null && !"".equals(value)) {

                BaseEntity entity = (BaseEntity) value;

                this.addAttribute(component, entity);

                Long codigo = entity.getId();
                
                if (codigo != null) {
                    return String.valueOf(codigo);
                }
            }

        } catch (Exception e) {
        }
        return "";
    }

    protected void addAttribute(UIComponent component, BaseEntity o) {
        try {
            String key = o.getId().toString();
            this.getAttributesFrom(component).put(key, o);
        } catch (Exception e) {
        }
    }

    protected Map<String, Object> getAttributesFrom(UIComponent component) {
        try {
            return component.getAttributes();
        } catch (Exception e) {
            return null;
        }
    }
}
