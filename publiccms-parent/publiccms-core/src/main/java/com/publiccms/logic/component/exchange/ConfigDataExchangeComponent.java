package com.publiccms.logic.component.exchange;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.annotation.Priority;
import javax.annotation.Resource;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractDataExchange;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysConfigData;
import com.publiccms.entities.sys.SysConfigDataId;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.config.ConfigComponent;
import com.publiccms.logic.component.config.ConfigDataComponent;
import com.publiccms.logic.service.sys.SysConfigDataService;

/**
 * ConfigDataExchangeComponent 站点配置导出组件
 * 
 */
@Component
@Priority(2)
public class ConfigDataExchangeComponent extends AbstractDataExchange<SysConfigData, SysConfigData> {
    @Resource
    private ConfigComponent configComponent;
    @Resource
    private ConfigDataComponent configDataComponent;
    @Resource
    private SysConfigDataService service;

    @Override
    public void exportAll(SysSite site, String directory, ByteArrayOutputStream outputStream, ArchiveOutputStream<ZipArchiveEntry> archiveOutputStream) {
        Set<String> configCodeSet = configComponent.getExportableConfigCodeList(site.getId());
        for (String code : configCodeSet) {
            SysConfigData entity = service.getEntity(new SysConfigDataId(site.getId(), code));
            if (null != entity) {
                exportEntity(site, directory, entity, outputStream, archiveOutputStream);
            }
        }
    }

    @Override
    public void importData(SysSite site, long userId, String directory, boolean overwrite, ZipFile zipFile) {
        super.importData(site, userId, directory, overwrite, zipFile);
        configDataComponent.clear(site.getId());
    }

    @Override
    public void exportEntity(SysSite site, String directory, SysConfigData entity, ByteArrayOutputStream outputStream,
            ArchiveOutputStream<ZipArchiveEntry> archiveOutputStream) {
        if (needReplace(entity.getData(), site.getDynamicPath())) {
            entity.setData(Strings.CS.replace(entity.getData(), site.getDynamicPath(), "#DYNAMICPATH#"));
        }
        if (needReplace(entity.getData(), site.getSitePath())) {
            entity.setData(Strings.CS.replace(entity.getData(), site.getSitePath(), "#SITEPATH#"));
        }
        export(directory, outputStream, archiveOutputStream, entity, CommonUtils.joinString(entity.getId().getCode(), ".json"));
    }

    @Override
    public void save(SysSite site, long userId, boolean overwrite, SysConfigData data) {
        if (null != data && null != data.getId()) {
            data.getId().setSiteId(site.getId());
            SysConfigData oldEntity = service.getEntity(data.getId());
            if (overwrite || null == oldEntity) {
                if (CommonUtils.notEmpty(data.getData())) {
                    data.setData(Strings.CS.replace(data.getData(), "#DYNAMICPATH#", site.getDynamicPath()));
                }
                if (CommonUtils.notEmpty(data.getData())) {
                    data.setData(Strings.CS.replace(data.getData(), "#SITEPATH#", site.getSitePath()));
                }
                if (null == oldEntity) {
                    service.save(data);
                } else {
                    service.update(data.getId(), data);
                }
            }
        }
    }

    @Override
    public String getDirectory() {
        return "config";
    }
}
