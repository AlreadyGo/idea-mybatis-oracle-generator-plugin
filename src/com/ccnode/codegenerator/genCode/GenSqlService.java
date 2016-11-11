package com.ccnode.codegenerator.genCode;

import com.ccnode.codegenerator.enums.FileType;
import com.ccnode.codegenerator.pojo.GenCodeResponse;
import com.ccnode.codegenerator.pojo.GeneratedFile;
import com.ccnode.codegenerator.pojo.OnePojoInfo;
import com.ccnode.codegenerator.pojo.PojoFieldInfo;
import com.ccnode.codegenerator.pojoHelper.GenCodeResponseHelper;
import com.ccnode.codegenerator.storage.SettingService;
import com.ccnode.codegenerator.util.*;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ccnode.codegenerator.util.GenCodeUtil.ONE_RETRACT;

/**
 * What always stop you is what you always believe.
 * <p>
 * Created by zhengjun.du on 2016/05/17 20:12
 */
public class GenSqlService {
    private static final Logger LOGGER = LoggerWrapper.getLogger(GenSqlService.class);

    public GenSqlService() {
    }

    public static void genSQL(GenCodeResponse response) {
        Iterator var1 = response.getPojoInfos().iterator();

        while(var1.hasNext()) {
            OnePojoInfo onePojoInfo = (OnePojoInfo)var1.next();

            try {
                genSQLFile(onePojoInfo, response);
            } catch (Exception var4) {
                LOGGER.error("GenSqlService genSQL error", var4);
            }
        }

    }

    private static void genSQLFile(OnePojoInfo onePojoInfo, GenCodeResponse response) {
        LOGGER.info("genSQLFile :{}", onePojoInfo.getPojoName());
        GeneratedFile fileInfo = GenCodeResponseHelper.getByFileType(onePojoInfo, FileType.SQL);
        Boolean canReplace = canReplace(fileInfo, response);
        List newLines;
        if(fileInfo.getOldLines().isEmpty()) {
            newLines = genSql(onePojoInfo, response);
            fileInfo.setNewLines(newLines);
        } else if(canReplace.booleanValue()) {
            newLines = replaceSql(onePojoInfo, fileInfo, response);
            fileInfo.setNewLines(newLines);
        }
    }

    private static Boolean canReplace(@NotNull GeneratedFile fileInfo, GenCodeResponse response) {
        return GenCodeUtil.sqlContain(fileInfo.getOldLines(), "CREATE TABLE") && GenCodeUtil.sqlContain(fileInfo.getOldLines(), ")ENGINE=")?Boolean.valueOf(true):Boolean.valueOf(false);
    }

    private static List<String> genSql(@NotNull OnePojoInfo onePojoInfo, GenCodeResponse response) {
        ArrayList retList = Lists.newArrayList();
        String tableName = response.getCodeConfig().getTableName();
        retList.add(String.format("-- auto Generated on %s ", new Object[]{DateUtil.formatLong(new Date())}));
        retList.add("-- DROP TABLE IF EXISTS \"" + tableName + "\"; ");
        retList.add("CREATE TABLE \"" + tableName + "\" (");
        List pojoFieldInfos = onePojoInfo.getPojoFieldInfos();
        List idFieldInfos = onePojoInfo.getIdFieldInfos();
        Iterator i = pojoFieldInfos.iterator();

        PojoFieldInfo field;
        while(i.hasNext()) {
            field = (PojoFieldInfo)i.next();
            String fieldSql = genfieldSql(field, response);
            retList.add(fieldSql);
        }

        for(int var9 = 0; var9 < idFieldInfos.size(); ++var9) {
            field = (PojoFieldInfo)idFieldInfos.get(var9);
            if(field.getId().booleanValue()) {
                retList.add("    PRIMARY KEY (\"" + GenCodeUtil.getUnderScore(field.getFieldName()).toUpperCase() + "\")" + (var9 != idFieldInfos.size() - 1?",":""));
            }
        }

        retList.add(");");
        return retList;
    }

    private static List<String> replaceSql(@NotNull OnePojoInfo onePojoInfo, GeneratedFile fileInfo, GenCodeResponse response) {
        List oldList = fileInfo.getOldLines();
        int oldIndex = findFirstFieldPos(oldList);
        Iterator replaceList = onePojoInfo.getPojoFieldInfos().iterator();

        String line;
        while(replaceList.hasNext()) {
            PojoFieldInfo fieldInfo = (PojoFieldInfo)replaceList.next();
            if(oldSqlContainField(oldList, fieldInfo)) {
                oldList = updateSqlComment(oldList, fieldInfo, response);
                ++oldIndex;
            } else {
                line = genfieldSql(fieldInfo, response);
                oldList.add(oldIndex, line);
                ++oldIndex;
            }
        }

        ArrayList var8 = Lists.newArrayList();
        oldList = PojoUtil.avoidEmptyList(oldList);
        Iterator var9 = oldList.iterator();

        while(var9.hasNext()) {
            line = (String)var9.next();
            var8.add(line);
        }

        oldList = removeDeleteField(onePojoInfo.getPojoFieldInfos(), var8);
        return Lists.newArrayList(oldList);
    }

    private static List<String> removeDeleteField(List<PojoFieldInfo> pojoFieldInfos, List<String> oldList) {
        oldList = PojoUtil.avoidEmptyList(oldList);
        ArrayList retList = Lists.newArrayList();
        Iterator var3 = oldList.iterator();

        while(true) {
            while(var3.hasNext()) {
                String line = (String)var3.next();
                String prefix = RegexUtil.getMatch("^[\\s]*\".+\"", line);
                if(StringUtils.isNotBlank(prefix)) {
                    Boolean containField = Boolean.valueOf(false);
                    Iterator var7 = pojoFieldInfos.iterator();

                    while(var7.hasNext()) {
                        PojoFieldInfo pojoFieldInfo = (PojoFieldInfo)var7.next();
                        String fieldName = GenCodeUtil.getUnderScore(pojoFieldInfo.getFieldName());
                        if(prefix.contains(fieldName)) {
                            containField = Boolean.valueOf(true);
                        }
                    }

                    if(containField.booleanValue()) {
                        retList.add(line);
                    }
                } else {
                    retList.add(line);
                }
            }

            return retList;
        }
    }

    private static List<String> updateSqlComment(@NotNull List<String> oldList, @NotNull PojoFieldInfo fieldInfo, GenCodeResponse response) {
        String keyWord = "\"" + GenCodeUtil.getUnderScore(fieldInfo.getFieldName()) + "\"";
        ArrayList retList = Lists.newArrayList();
        Iterator var5 = oldList.iterator();

        while(var5.hasNext()) {
            String s = (String)var5.next();
            if(s.contains(keyWord)) {
                String match = RegexUtil.getMatch("COMMENT[\\s]*\'(.*)\',", s);
                if(StringUtils.isNotBlank(match)) {
                    String comment = getFieldComment(response, fieldInfo);
                    String newComment = "COMMENT \'" + comment + "\',";
                    if(Objects.equal(newComment, match)) {
                        return oldList;
                    }

                    String replaced = s.replace(match, newComment);
                    retList.add(replaced);
                } else {
                    retList.add(s);
                }
            } else {
                retList.add(s);
            }
        }

        return retList;
    }

    private static boolean oldSqlContainField(@NotNull List<String> oldList, @NotNull PojoFieldInfo fieldInfo) {
        String keyWord = "\"" + GenCodeUtil.getUnderScore(fieldInfo.getFieldName()).toUpperCase() + "\"";
        Iterator var3 = oldList.iterator();

        String s;
        do {
            if(!var3.hasNext()) {
                return false;
            }

            s = (String)var3.next();
        } while(!s.contains(keyWord));

        return true;
    }

    private static int findFirstFieldPos(@NotNull List<String> oldList) {
        int index = 0;
        Iterator var2 = oldList.iterator();

        while(var2.hasNext()) {
            String fieldSql = (String)var2.next();
            if(StringUtils.isBlank(fieldSql)) {
                ++index;
            } else {
                if(!GenCodeUtil.sqlContain(fieldSql, "DROP TABLE") && !GenCodeUtil.sqlContain(fieldSql, "CREATE TABLE") && !GenCodeUtil.sqlContain(fieldSql, "auto Generated on")) {
                    break;
                }

                ++index;
            }
        }

        return index;
    }


    private static String genfieldSql(@NotNull PojoFieldInfo fieldInfo, GenCodeResponse response) {
        StringBuilder ret = new StringBuilder();
        if(fieldInfo.getFieldName().equalsIgnoreCase("lastUpdate")) {
            ret.append("    ").append("\"last_update\" DATE NOT NULL ,");
            return ret.toString();
        } else if(fieldInfo.getFieldName().equalsIgnoreCase("updateTime")) {
            ret.append("    ").append("\"update_time\" DATE NOT NULL ,");
            return ret.toString();
        } else if(fieldInfo.getFieldName().equalsIgnoreCase("createTime")) {
            ret.append("    ").append("\"create_time\" DATE NOT NULL ,");
            return ret.toString();
        } else {
            String filedClassDefault;
            filedClassDefault = getDefaultField(fieldInfo, response);
            ret.append("    \"").append(GenCodeUtil.getUnderScore(fieldInfo.getFieldName()).toUpperCase()).append("\" ").append(filedClassDefault).append(",");
            return ret.toString();
        }
    }

    private static String getFieldComment(GenCodeResponse response, PojoFieldInfo fieldInfo) {
        String language = (String)response.getUserConfigMap().get("language");
        HashMap commentMap = Maps.newHashMap();
        commentMap.put("lastUpdate", "最后更新时间");
        commentMap.put("updateTime", "更新时间");
        commentMap.put("createTime", "创建时间");
        commentMap.put("id", "主键");
        return commentMap.get(fieldInfo.getFieldName()) != null?(!StringUtils.isBlank(language) && !StringUtils.equalsIgnoreCase(language, "EN")?(String)commentMap.get(fieldInfo.getFieldName()):fieldInfo.getFieldName()):(StringUtils.isNotBlank(fieldInfo.getFieldComment())?fieldInfo.getFieldComment():fieldInfo.getFieldName());
    }

    private static String getDefaultField(PojoFieldInfo fieldInfo, GenCodeResponse response) {
        Map userConfigMap = response.getUserConfigMap();
        String key = fieldInfo.getFieldClass().toLowerCase();
        String value = (String)userConfigMap.get(key);
        if(StringUtils.isBlank(value)) {
            if(StringUtils.equalsIgnoreCase(key, "String")) {
                return "VARCHAR(50) NOT NULL";
            } else if(!StringUtils.equalsIgnoreCase(key, "Integer") && !StringUtils.equalsIgnoreCase(key, "int")) {
                if(StringUtils.equalsIgnoreCase(key, "short")) {
                    return "TINYINT NOT NULL";
                } else if(StringUtils.equalsIgnoreCase(key, "date")) {
                    return "DATE NOT NULL";
                } else if(StringUtils.equalsIgnoreCase(key, "Long")) {
                    return "NUMBER(20) NOT NULL";
                } else if(StringUtils.equalsIgnoreCase(key, "BigDecimal")) {
                    return "DECIMAL(14,4) NOT NULL";
                } else if(StringUtils.equalsIgnoreCase(key, "double")) {
                    return "DECIMAL(14,4) NOT NULL";
                } else if(StringUtils.equalsIgnoreCase(key, "float")) {
                    return "DECIMAL(14,4) NOT NULL";
                } else {
                    throw new RuntimeException("unSupport field type :" + fieldInfo.getFieldClass());
                }
            } else {
                return "INTEGER(12) NOT NULL";
            }
        } else {
            return value;
        }
    }

    public static void main(String[] args) {
        Pattern commentPattern = Pattern.compile("COMMENT[\\s]*\'(.*)\',");
        String s = "ULT \'\' COMMENTff\'联系人电话\',";
        Matcher matcher = commentPattern.matcher(s);
        if(matcher.find()) {
            String list = matcher.group();
            System.out.println(list);
        }

        ArrayList list1 = Lists.newArrayList();
        list1.add(Integer.valueOf(0));
        list1.add(Integer.valueOf(1));
        list1.add(Integer.valueOf(2));
        list1.add(1, Integer.valueOf(3));
        System.out.println(list1);
        System.out.println(list1.subList(1, 4));
    }
}

