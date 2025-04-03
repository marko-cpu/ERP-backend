package com.app.erp.setting;

import com.app.erp.entity.Setting;
import com.app.erp.entity.SettingCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingRepository  extends CrudRepository<Setting, String> {

    public List<Setting> findByCategory(SettingCategory category);
}
