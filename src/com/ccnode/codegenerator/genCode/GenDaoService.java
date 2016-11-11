package com.ccnode.codegenerator.genCode;

import com.ccnode.codegenerator.enums.FileType;
import com.ccnode.codegenerator.enums.MethodName;
import com.ccnode.codegenerator.pojo.GenCodeResponse;
import com.ccnode.codegenerator.pojo.GeneratedFile;
import com.ccnode.codegenerator.pojo.OnePojoInfo;
import com.ccnode.codegenerator.pojoHelper.GenCodeResponseHelper;
import com.ccnode.codegenerator.util.LoggerWrapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ccnode.codegenerator.util.GenCodeUtil.ONE_RETRACT;

/**
 * What always stop you is what you always believe.
 * <p>
 * Created by zhengjun.du on 2016/05/28 21:14
 */
public class GenDaoService {
    private static final Logger LOGGER = LoggerWrapper.getLogger(GenDaoService.class);

    public GenDaoService() {
    }

    public static void genDAO(GenCodeResponse response) {
        Iterator var1 = response.getPojoInfos().iterator();

        while(var1.hasNext()) {
            OnePojoInfo pojoInfo = (OnePojoInfo)var1.next();

            try {
                GeneratedFile e = GenCodeResponseHelper.getByFileType(pojoInfo, FileType.DAO);
                genDaoFile(pojoInfo, e, GenCodeResponseHelper.isUseGenericDao(response));
            } catch (Throwable var4) {
                LOGGER.error("GenDaoService genDAO error", var4);
                response.failure("GenDaoService genDAO error");
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
                LOGGER.info("genDaoFile useGenericDao true");
                newLines = Lists.newArrayList();
                newLines.add("package " + onePojoInfo.getDaoPackage() + ";");
                newLines.add("");
                newLines.add("import java.util.List;");
                newLines.add("import org.apache.ibatis.annotations.Param;");
                newLines.add("import " + onePojoInfo.getPojoPackage() + "." + onePojoInfo.getPojoName() + ";");
                newLines.add("");
                newLines.add("public interface " + pojoNameDao + " extends GenericDao<" + pojoName + "> {");
                newLines.add("");
                newLines.add("}");
                fileInfo.setNewLines(newLines);
            } else {
                LOGGER.info("genDaoFile useGenericDao false");
                newLines = Lists.newArrayList();
                newLines.add("package " + onePojoInfo.getDaoPackage() + ";");
                newLines.add("");
                newLines.add("import org.apache.ibatis.annotations.Param;");
                newLines.add("import java.util.List;");
                newLines.add("import java.util.Map;");
                newLines.add("import " + onePojoInfo.getPojoPackage() + "." + onePojoInfo.getPojoName() + ";");
                newLines.add("");
                newLines.add("public interface " + pojoNameDao + " {");
                newLines.add("");
                newLines.add(ONE_RETRACT+"void " + MethodName.insert.name() + pojoName + "(@Param(\"" + firstLowCasePo + "\") " + pojoName + " " + firstLowCasePo + ");");
                newLines.add("");
                newLines.add(ONE_RETRACT+"void " + MethodName.delete.name() + pojoName + "(@Param(\"" + firstLowCasePo + "\") " + "Map " + firstLowCasePo + ");");
                newLines.add("");
                newLines.add(ONE_RETRACT+"int " + MethodName.getCount.name() + "(@Param(\"" + firstLowCasePo + "\") " + pojoName+" " + firstLowCasePo + ");");
                newLines.add("");
                newLines.add(ONE_RETRACT+"List<" + pojoName + "> " + MethodName.select.name() + pojoName + "(@Param(\"" + firstLowCasePo + "\") " + pojoName + " " + firstLowCasePo + ",@Param(\"pageNo\")Integer pageNo,@Param(\"pageSize\")Integer pageSize);");
                newLines.add("");
                newLines.add(ONE_RETRACT+"void " + MethodName.update.name() + pojoName + "(@Param(\"" + firstLowCasePo + "\") " + pojoName + " " + firstLowCasePo + ");");
                newLines.add("");
                newLines.add("}");
                fileInfo.setNewLines(newLines);
            }

        }
    }
}
