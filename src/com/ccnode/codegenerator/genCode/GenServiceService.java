package com.ccnode.codegenerator.genCode;

import com.ccnode.codegenerator.enums.FileType;
import com.ccnode.codegenerator.enums.MethodName;
import com.ccnode.codegenerator.pojo.GenCodeResponse;
import com.ccnode.codegenerator.pojo.GeneratedFile;
import com.ccnode.codegenerator.pojo.OnePojoInfo;
import com.ccnode.codegenerator.pojoHelper.GenCodeResponseHelper;
import com.ccnode.codegenerator.util.GenCodeUtil;
import com.ccnode.codegenerator.util.LoggerWrapper;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ccnode.codegenerator.util.GenCodeUtil.ONE_RETRACT;
import static com.ccnode.codegenerator.util.GenCodeUtil.TWO_RETRACT;

/**
 * What always stop you is what you always believe.
 * <p>
 * Created by zhengjun.du on 2016/05/28 21:14
 */
public class GenServiceService {
    private static final Logger LOGGER = LoggerWrapper.getLogger(GenServiceService.class);

    public GenServiceService() {
    }

    public static void genService(GenCodeResponse response) {
        Iterator var1 = response.getPojoInfos().iterator();

        while(var1.hasNext()) {
            OnePojoInfo pojoInfo = (OnePojoInfo)var1.next();

            try {
                GeneratedFile e = GenCodeResponseHelper.getByFileType(pojoInfo, FileType.SERVICE);
                Boolean useGenericDao = Boolean.valueOf(Objects.equal(response.getUserConfigMap().get("usegenericdao"), "true"));
                genDaoFile(pojoInfo, e, useGenericDao);
            } catch (Throwable var5) {
                LOGGER.error("GenServiceService genService error", var5);
                response.failure("GenServiceService genService error");
            }
        }

    }

    private static void genDaoFile(OnePojoInfo onePojoInfo, GeneratedFile fileInfo, Boolean useGenericDao) {
        String pojoName = onePojoInfo.getPojoName();
        String pojoNameDao = pojoName + "Dao";
        String firstLowCasePo = pojoName.substring(0, 1).toLowerCase() + pojoName.substring(1);
        if(!fileInfo.getOldLines().isEmpty()) {
            fileInfo.setNewLines(fileInfo.getOldLines());
        } else {
            ArrayList newLines;
            if(useGenericDao.booleanValue()) {
                newLines = Lists.newArrayList();
                newLines.add("package " + onePojoInfo.getServicePackage() + ";");
                newLines.add("");
                newLines.add("import org.springframework.stereotype.Service;");
                newLines.add("import javax.annotation.Resource;");
                newLines.add("import java.util.List;");
                newLines.add("import " + onePojoInfo.getPojoPackage() + "." + onePojoInfo.getPojoName() + ";");
                newLines.add("import " + onePojoInfo.getDaoPackage() + "." + onePojoInfo.getPojoName() + "Dao;");
                newLines.add("");
                newLines.add("@Service");
                newLines.add("public class " + pojoName + "Service extends GenericService<" + pojoName + "> {");
                newLines.add("");
                newLines.add(ONE_RETRACT+"@Resource");
                newLines.add(ONE_RETRACT+"private " + pojoName + "Dao " + GenCodeUtil.getLowerCamel(pojoName) + "Dao;");
                newLines.add("");
                newLines.add(ONE_RETRACT+"@Override");
                newLines.add(ONE_RETRACT+"public GenericDao<" + pojoName + "> getGenericDao() {");
                newLines.add(ONE_RETRACT+"    return " + GenCodeUtil.getLowerCamel(pojoNameDao) + ";");
                newLines.add(ONE_RETRACT+"}");
                newLines.add("}");
                fileInfo.setNewLines(newLines);
            } else {
                newLines = Lists.newArrayList();
                String daoName = GenCodeUtil.getLowerCamel(pojoName) + "Dao";
                newLines.add("package " + onePojoInfo.getServicePackage() + ";");
                newLines.add("");
                newLines.add("import org.springframework.stereotype.Service;");
                newLines.add("import javax.annotation.Resource;");
                newLines.add("import java.util.List;");
                newLines.add("import java.util.Map;");
                newLines.add("import " + onePojoInfo.getPojoPackage() + "." + onePojoInfo.getPojoName() + ";");
                newLines.add("import " + onePojoInfo.getDaoPackage() + "." + onePojoInfo.getPojoName() + "Dao;");
                newLines.add("");
                newLines.add("@Service");
                newLines.add("public class " + pojoName + "Service {");
                newLines.add("");
                newLines.add(ONE_RETRACT+"@Resource");
                newLines.add(ONE_RETRACT+"private " + pojoName + "Dao " + daoName + ";");
                newLines.add("");
                newLines.add(ONE_RETRACT+"public void " + MethodName.insert.name() + pojoName + "(" + pojoName + " " + firstLowCasePo + "){");
                newLines.add(TWO_RETRACT+ daoName + "." + MethodName.insert.name() + pojoName + "(" + firstLowCasePo + ");");
                newLines.add(ONE_RETRACT+"}");
                newLines.add("");
                newLines.add(ONE_RETRACT+"public void " + MethodName.delete.name() + pojoName + "(Map map){");
                newLines.add(TWO_RETRACT+ daoName + "." + MethodName.delete.name() + pojoName + "(map);");
                newLines.add(ONE_RETRACT+"}");
                newLines.add("");
                newLines.add(ONE_RETRACT+"public List<" + pojoName + "> " + MethodName.select.name() + pojoName + "(" + pojoName + " " + firstLowCasePo + ",Integer pageNo,Integer pageSize){");
                newLines.add(TWO_RETRACT+"return " + daoName + "." + MethodName.select.name() + pojoName + "(" + firstLowCasePo + ",pageNo,pageSize);");
                newLines.add(ONE_RETRACT+"}");
                newLines.add("");
                newLines.add(ONE_RETRACT+"public void " + MethodName.update.name() + pojoName + "(" + pojoName + " " + firstLowCasePo + "){");
                newLines.add(TWO_RETRACT+ daoName + "." + MethodName.update.name() + pojoName + "(" + firstLowCasePo + ");");
                newLines.add(ONE_RETRACT+"}");
                newLines.add("");
                newLines.add(ONE_RETRACT+"public int " + MethodName.getCount.name() + "(" + pojoName + " " + firstLowCasePo + "){");
                newLines.add(TWO_RETRACT+"return "+ daoName + "." + MethodName.getCount.name() +  "(" + firstLowCasePo + ");");
                newLines.add(ONE_RETRACT+"}");
                newLines.add("");
                newLines.add("}");
                fileInfo.setNewLines(newLines);
            }

        }
    }
}
