package br.com.zone.compile.app.repository;

import br.com.zone.compile.app.model.BaseEntity;
import br.com.zone.compile.app.util.Transacional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author daniel
 * @param <T>
 */
public class GenericRepository implements Serializable {

    @Inject
    private EntityManager manager;

    @Transacional
    public void salvar(BaseEntity t) throws Exception {
        if (t.getId() == null) {
            manager.persist(t);
        } else {
            manager.merge(t);
        }
    }

    @Transacional
    public void excluir(BaseEntity entity, Class entityClass) throws Exception {
        BaseEntity entityToBeRemoved = (BaseEntity) manager.find(entityClass, entity.getId());
        manager.remove(entityToBeRemoved);
    }

    public BaseEntity buscarPorId(Object id, Class entityClass) {
        return (BaseEntity) manager.find(entityClass, id);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<BaseEntity> listarTodos(Class entityClass) {
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<BaseEntity> cq = cb.createQuery(entityClass);
        Root<BaseEntity> root = cq.from(entityClass);
        cq.orderBy(cb.asc(root.get("id")));
        cq.select(root);
        List<BaseEntity> resultado = manager.createQuery(cq).getResultList();
        return resultado;
    }

    protected List<BaseEntity> listar(String namedQuery, Object... params) {
        Query q = manager.createNamedQuery(namedQuery);
        for (int i = 0; i < params.length; i++) {
            q.setParameter(i + 1, params[i]);
        }
        List<BaseEntity> resultado = q.getResultList();
        return resultado;
    }

    @SuppressWarnings("unchecked")
    protected BaseEntity buscarUmResultado(String namedQuery, Object... params) {
        BaseEntity result = null;
        Query q = manager.createNamedQuery(namedQuery);
        for (int i = 0; i < params.length; i++) {
            q.setParameter(i + 1, params[i]);
        }
        result = (BaseEntity) q.getSingleResult();
        return result;
    }

}