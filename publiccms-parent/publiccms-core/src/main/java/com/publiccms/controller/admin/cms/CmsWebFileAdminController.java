package com.publiccms.controller.admin.cms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.publiccms.common.annotation.Csrf;
import com.publiccms.common.constants.CommonConstants;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.tools.CmsFileUtils;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ControllerUtils;
import com.publiccms.common.tools.ImageUtils;
import com.publiccms.common.tools.RequestUtils;
import com.publiccms.common.tools.VerificationUtils;
import com.publiccms.common.tools.ZipUtils;
import com.publiccms.entities.log.LogOperate;
import com.publiccms.entities.log.LogUpload;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.logic.component.config.SafeConfigComponent;
import com.publiccms.logic.component.site.SiteComponent;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.log.LogOperateService;
import com.publiccms.logic.service.log.LogUploadService;
import com.publiccms.views.pojo.entities.FileUploadResult;

/**
 * 
 * CmsWebFileAdminController
 *
 */
@Controller
@RequestMapping("cmsWebFile")
public class CmsWebFileAdminController {
    protected final Log log = LogFactory.getLog(getClass());
    @Resource
    protected LogUploadService logUploadService;
    @Resource
    protected LogOperateService logOperateService;
    @Resource
    protected SafeConfigComponent safeConfigComponent;
    @Resource
    protected SiteComponent siteComponent;

    /**
     * @param site
     * @param admin
     * @param path
     * @param content
     * @param request
     * @param model
     * @return view name
     */
    @RequestMapping("save")
    @Csrf
    public String save(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, String path, String content,
            HttpServletRequest request, ModelMap model) {
        if (CommonUtils.notEmpty(path)) {
            try {
                String suffix = CmsFileUtils.getSuffix(path);
                if (ArrayUtils.contains(safeConfigComponent.getSafeSuffix(site), suffix)) {
                    String filepath = siteComponent.getWebFilePath(site.getId(), path);
                    content = new String(VerificationUtils.base64Decode(content), StandardCharsets.UTF_8);
                    String action = null;
                    if (CmsFileUtils.createFile(filepath, content)) {
                        action = "save.web.webfile";
                    } else {
                        String historyFilePath = siteComponent.getWebHistoryFilePath(site.getId(), path, true);
                        CmsFileUtils.updateFile(filepath, historyFilePath, content);
                        action = "update.web.webfile";
                    }
                    if (CmsFileUtils.isSafe(filepath, suffix)) {
                        logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                                LogLoginService.CHANNEL_WEB_MANAGER, action, RequestUtils.getIpAddress(request),
                                CommonUtils.getDate(), path));
                    } else {
                        CmsFileUtils.delete(filepath);
                        model.addAttribute(CommonConstants.ERROR, "verify.custom.file.unsafe");
                        return CommonConstants.TEMPLATE_ERROR;
                    }
                } else {
                    model.addAttribute(CommonConstants.ERROR, "verify.custom.fileType");
                    return CommonConstants.TEMPLATE_ERROR;
                }
            } catch (IOException e) {
                model.addAttribute(CommonConstants.ERROR, e.getMessage());
                log.error(e.getMessage(), e);
                return CommonConstants.TEMPLATE_ERROR;
            }
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param files
     * @param path
     * @param privatefile
     * @param overwrite
     * @param unzip
     * @param encoding
     * @param here
     * @param zipOverwrite
     * @param request
     * @param model
     * @return view name
     */
    @RequestMapping("doUpload")
    @Csrf
    public String upload(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, MultipartFile[] files, String path,
            boolean privatefile, boolean overwrite, boolean unzip, String encoding, boolean here, boolean zipOverwrite,
            HttpServletRequest request, ModelMap model) {
        if (null != files) {
            try {
                for (MultipartFile file : files) {
                    String originalName = file.getOriginalFilename();
                    String suffix = CmsFileUtils.getSuffix(originalName);
                    String filepath = CommonUtils.joinString(path, Constants.SEPARATOR, originalName);
                    String fuleFilePath = siteComponent.getWebFilePath(site.getId(), filepath);
                    if (ArrayUtils.contains(safeConfigComponent.getSafeSuffix(site), suffix)) {
                        if (CommonUtils.notEmpty(suffix) && suffix.equalsIgnoreCase(".zip") && unzip) {
                            try {
                                File dest = File.createTempFile("temp_", suffix);
                                file.transferTo(dest);
                                if (here) {
                                    ZipUtils.unzipHere(dest.getAbsolutePath(), encoding, zipOverwrite, (f, e) -> {
                                        String historyFilePath = siteComponent.getTemplateHistoryFilePath(site.getId(),
                                                e.getName(), true);
                                        if (ArrayUtils.contains(safeConfigComponent.getSafeSuffix(site), suffix)) {
                                            try {
                                                CmsFileUtils.copyInputStreamToFile(f.getInputStream(e), historyFilePath);
                                            } catch (IOException e1) {
                                            }
                                        }
                                        return true;
                                    });
                                } else {
                                    ZipUtils.unzip(dest.getAbsolutePath(), encoding, zipOverwrite, (f, e) -> {
                                        String historyFilePath = siteComponent.getWebHistoryFilePath(site.getId(), e.getName(),
                                                true);
                                        try {
                                            CmsFileUtils.copyInputStreamToFile(f.getInputStream(e), historyFilePath);
                                        } catch (IOException e1) {
                                        }
                                        return true;
                                    });
                                }
                                Files.delete(dest.toPath());
                            } catch (IOException e) {
                                model.addAttribute(CommonConstants.ERROR, e.getMessage());
                                log.error(e.getMessage(), e);
                            }
                        } else if (overwrite || !CmsFileUtils.exists(fuleFilePath)) {
                            if (CmsFileUtils.exists(fuleFilePath)) {
                                String historyFilePath = siteComponent.getWebHistoryFilePath(site.getId(), filepath, true);
                                try {
                                    CmsFileUtils.copyFileToFile(historyFilePath, historyFilePath);
                                } catch (IOException e1) {
                                }
                            }
                            CmsFileUtils.upload(file, fuleFilePath);
                            if (CmsFileUtils.isSafe(fuleFilePath, suffix)) {
                                FileUploadResult uploadResult = CmsFileUtils.getFileSize(fuleFilePath, originalName, suffix);
                                logUploadService.save(new LogUpload(site.getId(), admin.getId(),
                                        LogLoginService.CHANNEL_WEB_MANAGER, originalName, privatefile,
                                        CmsFileUtils.getFileType(CmsFileUtils.getSuffix(originalName)), file.getSize(),
                                        uploadResult.getWidth(), uploadResult.getHeight(), RequestUtils.getIpAddress(request),
                                        CommonUtils.getDate(), filepath));
                            } else {
                                CmsFileUtils.delete(fuleFilePath);
                                model.addAttribute(CommonConstants.ERROR, "verify.custom.file.unsafe");
                                return CommonConstants.TEMPLATE_ERROR;
                            }
                        }
                    } else {
                        model.addAttribute(CommonConstants.ERROR, "verify.custom.fileType");
                        return CommonConstants.TEMPLATE_ERROR;
                    }
                }
            } catch (IOException e) {
                model.addAttribute(CommonConstants.ERROR, e.getMessage());
                log.error(e.getMessage(), e);
                return CommonConstants.TEMPLATE_ERROR;
            }
        }
        return CommonConstants.TEMPLATE_DONE;

    }

    /**
     * @param site
     * @param admin
     * @param file
     * @param filename
     * @param base64File
     * @param originalFilename
     * @param size
     * @param overwrite
     * @param request
     * @param model
     * @return view name
     */
    @RequestMapping("doUploadIco")
    @Csrf
    public String uploadIco(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, MultipartFile file, String filename,
            String base64File, String originalFilename, int size, boolean overwrite, HttpServletRequest request, ModelMap model) {
        if (null != file && !file.isEmpty() || CommonUtils.notEmpty(base64File)) {
            String originalName;
            String suffix;
            if (null != file && !file.isEmpty()) {
                originalName = file.getOriginalFilename();
            } else {
                originalName = originalFilename;
            }
            suffix = CmsFileUtils.getSuffix(originalName);
            try {
                String filepath = CommonUtils.joinString(Constants.SEPARATOR, filename);
                String fuleFilePath = siteComponent.getWebFilePath(site.getId(), filepath);
                if (overwrite || !CmsFileUtils.exists(fuleFilePath)) {
                    CmsFileUtils.mkdirsParent(fuleFilePath);
                    if (CommonUtils.notEmpty(base64File)) {
                        try (InputStream inputStream = new ByteArrayInputStream(VerificationUtils.base64Decode(base64File))) {
                            ImageUtils.image2Ico(inputStream, suffix, size, fuleFilePath);
                        }
                    } else {
                        try (InputStream inputStream = file.getInputStream()) {
                            ImageUtils.image2Ico(inputStream, suffix, size, fuleFilePath);
                        }
                    }
                    FileUploadResult uploadResult = CmsFileUtils.getFileSize(fuleFilePath, originalName, suffix);
                    logUploadService.save(new LogUpload(site.getId(), admin.getId(), LogLoginService.CHANNEL_WEB_MANAGER,
                            filename, false, CmsFileUtils.FILE_TYPE_IMAGE, uploadResult.getFileSize(), uploadResult.getWidth(),
                            uploadResult.getHeight(), RequestUtils.getIpAddress(request), CommonUtils.getDate(), filepath));
                }
            } catch (IOException e) {
                model.addAttribute(CommonConstants.ERROR, e.getMessage());
                log.error(e.getMessage(), e);
                return CommonConstants.TEMPLATE_ERROR;
            }
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param fileNames
     * @param path
     * @return view name
     */
    @RequestMapping("check")
    @Csrf
    @ResponseBody
    public boolean check(@RequestAttribute SysSite site, @RequestParam("fileNames[]") String[] fileNames, String path) {
        if (null != fileNames) {
            for (String fileName : fileNames) {
                String filepath = CommonUtils.joinString(path, Constants.SEPARATOR, fileName);
                if (CmsFileUtils.exists(siteComponent.getWebFilePath(site.getId(), filepath))) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * @param site
     * @param admin
     * @param paths
     * @param request
     * @param model
     * @return view name
     */
    @RequestMapping("delete")
    @Csrf
    public String delete(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, String[] paths,
            HttpServletRequest request, ModelMap model) {
        if (CommonUtils.notEmpty(paths)) {
            for (String path : paths) {
                String filepath = siteComponent.getWebFilePath(site.getId(), path);
                String backupFilePath = siteComponent.getWebBackupFilePath(site.getId(), path);
                if (ControllerUtils.errorCustom("notExist.webfile", !CmsFileUtils.moveFile(filepath, backupFilePath), model)) {
                    return CommonConstants.TEMPLATE_ERROR;
                }
            }
            logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "delete.web.webfile", RequestUtils.getIpAddress(request),
                    CommonUtils.getDate(), StringUtils.join(paths, Constants.COMMA)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param path
     * @param request
     * @param model
     * @return view name
     */
    @RequestMapping("zip")
    @Csrf
    public String doZip(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, String path, HttpServletRequest request,
            ModelMap model) {
        String filepath = siteComponent.getWebFilePath(site.getId(), path);
        if (CmsFileUtils.isDirectory(filepath)) {
            try {
                String zipFileName = null;
                if (CommonUtils.empty(path) || path.endsWith("/") || path.endsWith("\\")) {
                    zipFileName = CommonUtils.joinString(filepath, "files.zip");
                } else {
                    zipFileName = CommonUtils.joinString(filepath, ".zip");
                }
                ZipUtils.zip(filepath, zipFileName);
            } catch (IOException e) {
                model.addAttribute(CommonConstants.ERROR, e.getMessage());
                log.error(e.getMessage(), e);
            }
        }
        logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(), LogLoginService.CHANNEL_WEB_MANAGER,
                "zip.web.webfile", RequestUtils.getIpAddress(request), CommonUtils.getDate(), path));
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param path
     * @param encoding
     * @param here
     * @param overwrite
     * @param request
     * @param model
     * @return view name
     */
    @RequestMapping("unzip")
    @Csrf
    public String doUnzip(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, String path, String encoding,
            boolean here, boolean overwrite, HttpServletRequest request, ModelMap model) {
        if (CommonUtils.notEmpty(path) && path.toLowerCase().endsWith(".zip")) {
            String filepath = siteComponent.getWebFilePath(site.getId(), path);
            if (CmsFileUtils.isFile(filepath)) {
                try {
                    if (here) {
                        ZipUtils.unzipHere(filepath, encoding, overwrite, (f, e) -> {
                            String historyFilePath = siteComponent.getTemplateHistoryFilePath(site.getId(), e.getName(), true);
                            try {
                                CmsFileUtils.copyInputStreamToFile(f.getInputStream(e), historyFilePath);
                            } catch (IOException e1) {
                            }
                            return true;
                        });
                    } else {
                        ZipUtils.unzip(filepath, encoding, overwrite, (f, e) -> {
                            String historyFilePath = siteComponent.getWebHistoryFilePath(site.getId(), e.getName(), true);
                            try {
                                CmsFileUtils.copyInputStreamToFile(f.getInputStream(e), historyFilePath);
                            } catch (IOException e1) {
                            }
                            return true;
                        });
                    }
                } catch (IOException e) {
                    model.addAttribute(CommonConstants.ERROR, e.getMessage());
                    log.error(e.getMessage(), e);
                }
            }
            logOperateService
                    .save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(), LogLoginService.CHANNEL_WEB_MANAGER,
                            "unzip.web.webfile", RequestUtils.getIpAddress(request), CommonUtils.getDate(), path));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param path
     * @param fileName
     * @param request
     * @return view name
     */
    @RequestMapping("createDirectory")
    @Csrf
    public String createDirectory(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, String path, String fileName,
            HttpServletRequest request) {
        if (null != path && CommonUtils.notEmpty(fileName)) {
            path = CommonUtils.joinString(path, Constants.SEPARATOR, fileName);
            String filepath = siteComponent.getWebFilePath(site.getId(), path);
            CmsFileUtils.mkdirs(filepath);
            logOperateService
                    .save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(), LogLoginService.CHANNEL_WEB_MANAGER,
                            "createDirectory.web.webfile", RequestUtils.getIpAddress(request), CommonUtils.getDate(), path));
        }
        return CommonConstants.TEMPLATE_DONE;
    }
}
