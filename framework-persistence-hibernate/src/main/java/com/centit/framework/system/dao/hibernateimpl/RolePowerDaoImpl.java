package com.centit.framework.system.dao.hibernateimpl;

import com.centit.framework.hibernate.dao.BaseDaoImpl;
import com.centit.framework.hibernate.dao.DatabaseOptUtils;
import com.centit.framework.system.dao.RolePowerDao;
import com.centit.framework.system.po.RolePower;
import com.centit.framework.system.po.RolePowerId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sx
 * Date: 14-10-29
 * Time: 下午3:18
 * To change this template use File | Settings | File Templates.
 */

@Repository("rolePowerDao")
public class RolePowerDaoImpl extends BaseDaoImpl<RolePower, RolePowerId> implements RolePowerDao {


    public Map<String, String> getFilterField() {
        if (filterField == null) {
            filterField = new HashMap<>();
            filterField.put("optCode", "id.optCode = :optCode");
            filterField.put("roleCode", "id.roleCode = :roleCode");

        }
        return filterField;
    }

    @Transactional
    public void deleteRolePowersByRoleCode(String rolecode) {
        DatabaseOptUtils.doExecuteHql(this, "DELETE FROM RolePower rp where rp.id.roleCode=?", rolecode);
    }

    @Transactional
    public void deleteRolePowersByOptCode(String optecode) {
        DatabaseOptUtils.doExecuteHql(this, "DELETE FROM RolePower rp where rp.id.optCode=?", optecode);
    }


    @Transactional
    public List<RolePower> listRolePowersByRoleCode(String rolecode) {
        return listObjects("FROM RolePower rp where rp.id.roleCode=?", rolecode);
    }

    @Transactional
    public void mergeBatchObject(List<RolePower> rolePowers) {
        for (int i = 0; i < rolePowers.size(); i++) {
            super.mergeObject(rolePowers.get(i));

            if (0 == i % 20) {
                DatabaseOptUtils.flush(this.getCurrentSession());
            }
        }
    }

    @Override
    public void updateRolePower(RolePower rolePower){
        super.updateObject(rolePower);
    }

    @Override
    public void saveNewRolePower(RolePower rolePower){
        super.saveNewObject(rolePower);
    }
}
