package xyz.erupt.core.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptAndEruptFieldModel;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.EruptPageModel;
import xyz.erupt.core.service.InitService;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.SpringUtil;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Erupt 页面结构构建信息
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_BUILD)
public class EruptBuildController {

    @GetMapping("/list/{erupt}")
    @ResponseBody
    public EruptPageModel getEruptTableView(@PathVariable("erupt") String eruptName, HttpServletResponse response) {
        EruptPageModel eruptPageModel = new EruptPageModel();
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        if (null != eruptModel) {
            try {
                Object eruptInstance = SpringUtil.getBean(eruptModel.getClazz());
                for (EruptFieldModel fm : eruptModel.getEruptFieldModels()) {
                    Field field = fm.getField();
                    field.setAccessible(true);
                    fm.setValue(field.get(eruptInstance));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            eruptPageModel.setEruptModel(eruptModel);
            List<EruptAndEruptFieldModel> eruptAndEruptFieldModels = new ArrayList<>();
            for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
                if (fieldModel.getEruptField().edit().type() == EditType.TAB) {
                    EruptModel subEruptModel = InitService.ERUPTS.get(ReflectUtil.getFieldGenericName(fieldModel.getField()).get(0));
                    if (null == subEruptModel) {
                        throw new RuntimeException("请使用Erupt注解管理：" + fieldModel.getField().getName());
                    }
                    EruptAndEruptFieldModel eruptAndEruptFieldModel = new EruptAndEruptFieldModel(fieldModel, subEruptModel);
                    eruptAndEruptFieldModels.add(eruptAndEruptFieldModel);
                }
            }
            eruptPageModel.setSubErupts(eruptAndEruptFieldModels);
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
        return eruptPageModel;
    }


}