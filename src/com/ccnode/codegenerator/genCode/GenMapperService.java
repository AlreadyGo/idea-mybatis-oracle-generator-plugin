package com.ccnode.codegenerator.genCode;

import com.ccnode.codegenerator.enums.FileType;
import com.ccnode.codegenerator.enums.MethodName;
import com.ccnode.codegenerator.function.EqualCondition;
import com.ccnode.codegenerator.function.MapperCondition;
import com.ccnode.codegenerator.pojo.*;
import com.ccnode.codegenerator.pojoHelper.GenCodeResponseHelper;
import com.ccnode.codegenerator.pojoHelper.OnePojoInfoHelper;
import com.ccnode.codegenerator.storage.SettingService;
import com.ccnode.codegenerator.util.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ccnode.codegenerator.util.GenCodeUtil.*;

/**
 * What always stop you is what you always believe.
 * <p>
 * Created by zhengjun.du on 2016/05/28 21:14
 */
public class GenMapperService {

    private final static Logger LOGGER = LoggerWrapper.getLogger(GenMapperService.class);

    private static String COMMA = ",";

    public static void genMapper( GenCodeResponse response) {
        for (OnePojoInfo pojoInfo : response.getPojoInfos()) {
            try{
                GeneratedFile fileInfo = GenCodeResponseHelper.getByFileType(pojoInfo, FileType.MAPPER);
                String mapperExpandStr = response.getUserConfigMap().get("mapper.expand");
                Boolean expand = false;
                if("true".equals(mapperExpandStr)){
                    expand = true;
                }
                genMapper(response,pojoInfo,fileInfo,expand);
            }catch(Throwable e){
                LOGGER.error("GenMapperService genMapper error", e);
                response.failure("GenMapperService genMapper error");
            }


        }
    }

    private static void genMapper(GenCodeResponse response, OnePojoInfo onePojoInfo, GeneratedFile fileInfo, Boolean expand) {
        List oldLines = fileInfo.getOldLines();
        ListInfo listInfo = new ListInfo();
        String pojoName = onePojoInfo.getPojoName();
        String tableName = response.getCodeConfig().getTableName();
        listInfo.setFullList(getMapperHeader(onePojoInfo, tableName));
        Pair posPair = ReplaceUtil.getPos(listInfo.getFullList(), "<resultMap id=\"AllColumnMap\" type=", "</resultMap>", new MapperCondition());
        listInfo.setPos(posPair);
        listInfo.setNewSegments(genAllColumnMap(onePojoInfo));
        ReplaceUtil.merge(listInfo, new EqualCondition<String>() {
            public boolean isEqual(String o1, String o2) {
                String match1 = RegexUtil.getMatch("result(.*)property", o1);
                String match2 = RegexUtil.getMatch("result(.*)property", o2);
                return StringUtils.isBlank(match1)?false:match1.equals(match2);
            }
        });
        posPair = ReplaceUtil.getPos(listInfo.getFullList(), "<sql id=\"all_column\">", "</sql>", new MapperCondition());
        listInfo.setPos(posPair);
        listInfo.setNewSegments(genAllColumn(onePojoInfo));
        ReplaceUtil.merge(listInfo, new EqualCondition<String>() {
            public boolean isEqual(String o1, String o2) {
                String match1 = RegexUtil.getMatch("[0-9A-Za-z_ ,]{1,100}", o1);
                String match2 = RegexUtil.getMatch("[0-9A-Za-z_ ,]{1,100}", o2);
                return StringUtils.isBlank(match1)?false:match1.equals(match2);
            }
        });
        posPair = ReplaceUtil.getPos(listInfo.getFullList(), "<insert id=\"" + MethodName.insert.name() + pojoName + "\"", "</insert>", new MapperCondition());
        listInfo.setPos(posPair);
        listInfo.setNewSegments(genAddMethod(onePojoInfo, tableName));
        ReplaceUtil.merge(listInfo, new EqualCondition<String>() {
            public boolean isEqual(String o1, String o2) {
                String match1 = RegexUtil.getMatch("test=(.*)</if>", o1);
                String match2 = RegexUtil.getMatch("test=(.*)</if>", o2);
                return StringUtils.isBlank(match1)?false:match1.equals(match2);
            }
        });
        fileInfo.setNewLines(listInfo.getFullList());
        posPair = ReplaceUtil.getPos(listInfo.getFullList(), "<update id=\"" + MethodName.update.name() + pojoName + "\"", "</update>", new MapperCondition());
        listInfo.setPos(posPair);
        listInfo.setNewSegments(genUpdateMethod(response, onePojoInfo, tableName));
        ReplaceUtil.merge(listInfo, new EqualCondition<String>() {
            public boolean isEqual(String o1, String o2) {
                return o1.equals(o2);
            }
        });
        fileInfo.setNewLines(listInfo.getFullList());
        posPair = ReplaceUtil.getPos(listInfo.getFullList(), "<select id=\"" + MethodName.select.name() + pojoName + "\"", "</select>", new MapperCondition());
        listInfo.setPos(posPair);
        listInfo.setNewSegments(genSelectMethod(response, onePojoInfo, tableName));
        ReplaceUtil.merge(listInfo, new EqualCondition<String>() {
            public boolean isEqual(String o1, String o2) {
                return o1.equals(o2);
            }
        });
        fileInfo.setNewLines(listInfo.getFullList());
        posPair = ReplaceUtil.getPos(listInfo.getFullList(),"<select id=\"getCount\" parameterType=\"map\" resultType=\"int\">","</select>", new MapperCondition());
        listInfo.setPos(posPair);
        listInfo.setNewSegments(genSelectCountMethod(response, onePojoInfo, tableName));
        ReplaceUtil.merge(listInfo, new EqualCondition<String>() {
            public boolean isEqual(String o1, String o2) {
                return o1.equals(o2);
            }
        });
        fileInfo.setNewLines(listInfo.getFullList());
        List newList = listInfo.getFullList();
        newList = adjustList(newList);
        fileInfo.setNewLines(newList);
    }

    private static List<String> adjustList(List<String> newList) {
        newList = PojoUtil.avoidEmptyList(newList);
        ArrayList retList = Lists.newArrayList();
        Iterator var2 = newList.iterator();

        while(var2.hasNext()) {
            String s = (String)var2.next();
            if(!s.contains("</mapper>")) {
                retList.add(s);
            }
        }

        retList.add("</mapper>");
        return retList;
    }

    public static List<String> getMapperHeader(OnePojoInfo onePojoInfo, String tableName) {
        ArrayList retList = Lists.newArrayList();
        String pojoName = onePojoInfo.getPojoName();
        String firstCamelPo = GenCodeUtil.getLowerCamel(pojoName);
        List infos = onePojoInfo.getIdFieldInfos();
        retList.add("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        retList.add("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >");
        retList.add("<mapper namespace=\"" + onePojoInfo.getDaoPackage() + "." + onePojoInfo.getPojoName() + "Dao\">");
        retList.add("");
        retList.add("<!--auto generated Code-->");
        retList.add(ONE_RETRACT+"<resultMap id=\"AllColumnMap\" type=\"" + onePojoInfo.getPojoPackage() + "." + onePojoInfo.getPojoName() + "\">");
        retList.add(ONE_RETRACT+"</resultMap>");
        retList.add("");
        retList.add("<!--auto generated Code-->");
        retList.add(ONE_RETRACT+"<sql id=\"all_column\">");
        retList.add(ONE_RETRACT+"</sql>");
        retList.add("");
        retList.add("<!--auto generated Code-->");
        retList.add(ONE_RETRACT+"<insert id=\"" + MethodName.insert.name() + pojoName + "\" parameterType=\"" + firstCamelPo + "\">");
        retList.add(ONE_RETRACT+"</insert>");
        retList.add("");
        retList.add("<!--auto generated Code-->");
        retList.add(ONE_RETRACT+"<update id=\"" + MethodName.update.name() + pojoName + "\" parameterType=\"" + firstCamelPo + "\">");
        retList.add(ONE_RETRACT+"</update>");
        retList.add("");
        retList.add("<!--auto generated Code-->");
        retList.add(ONE_RETRACT+"<select id=\"" + MethodName.select.name() + pojoName + "\" parameterType=\"map\" resultMap=\"AllColumnMap\">");
        retList.add(ONE_RETRACT+"</select>");
        retList.add("<!--auto generated Code-->");
        retList.add(ONE_RETRACT+"<select id=\"getCount\" parameterType=\"map\" resultType=\"int\">");
        retList.add(ONE_RETRACT+"</select>");
        retList.add("");
        retList.add("<!--auto generated Code-->");
        retList.add(ONE_RETRACT+"<delete id=\"" + MethodName.delete.name() + pojoName + "\" parameterType=\"map\">");
        if(infos!=null && infos.size()>0){
            for(int i = 0; i < infos.size(); ++i) {
                String name = ((PojoFieldInfo)infos.get(i)).getFieldName();
                if(i == 0) {
                    retList.add(TWO_RETRACT+"DELETE FROM " + tableName + " where \"" + GenCodeUtil.getUnderScore(name).toUpperCase() + "\"=#{" + firstCamelPo + "." + name + "}");
                } else {
                    retList.add(THREE_RETRACT+"and \"" + GenCodeUtil.getUnderScore(name) + "\"=#{" + firstCamelPo + "." + name + "}");
                }
            }
        }
        retList.add(ONE_RETRACT+"</delete>");
        retList.add("</mapper>");
        return retList;
    }

    private static List<String> genAllColumnMap(OnePojoInfo onePojoInfo) {
        ArrayList retList = Lists.newArrayList();
        retList.add(ONE_RETRACT+"<resultMap id=\"AllColumnMap\" type=\"" + onePojoInfo.getPojoPackage() + "." + onePojoInfo.getPojoName() + "\">");
        Iterator var2 = onePojoInfo.getPojoFieldInfos().iterator();

        while(var2.hasNext()) {
            PojoFieldInfo fieldInfo = (PojoFieldInfo)var2.next();
            String fieldName = fieldInfo.getFieldName();
            retList.add(String.format("%s<result column=\"%s\" property=\"%s\"/>", new Object[]{TWO_RETRACT+"", GenCodeUtil.getUnderScore(fieldName), fieldName}));
        }

        retList.add(ONE_RETRACT+"</resultMap>");
        return retList;
    }

    private static List<String> genAllColumn(OnePojoInfo onePojoInfo) {
        ArrayList retList = Lists.newArrayList();
        retList.add(ONE_RETRACT+"<sql id=\"all_column\">");
        int index = 0;

        for(Iterator var3 = onePojoInfo.getPojoFieldInfos().iterator(); var3.hasNext(); ++index) {
            PojoFieldInfo fieldInfo = (PojoFieldInfo)var3.next();
            String s = TWO_RETRACT+"\"" + GenCodeUtil.getUnderScore(fieldInfo.getFieldName()).toUpperCase() + "\"" + COMMA;
            if(index == onePojoInfo.getPojoFieldInfos().size() - 1) {
                s = s.replace(COMMA, "");
            }

            retList.add(s);
        }

        retList.add(ONE_RETRACT+"</sql>");
        return retList;
    }

    private static List<String> genAddMethod(OnePojoInfo onePojoInfo, String tableName) {
        ArrayList retList = Lists.newArrayList();
        String pojoName = onePojoInfo.getPojoName();
        String firstCamelPo = GenCodeUtil.getLowerCamel(pojoName);
        List fieldInfos = onePojoInfo.getPojoFieldInfos();
        retList.add(ONE_RETRACT+"<insert id=\"" + MethodName.insert.name() + pojoName + "\" parameterType=\"" + firstCamelPo + "\">");
        retList.add(TWO_RETRACT+"insert into " + tableName);
        retList.add(TWO_RETRACT+"(");

        int i;
        PojoFieldInfo pojoFieldInfo;
        for(i = 0; i < fieldInfos.size(); ++i) {
            pojoFieldInfo = (PojoFieldInfo)fieldInfos.get(i);
            retList.add(THREE_RETRACT+"\"" + GenCodeUtil.getUnderScore(pojoFieldInfo.getFieldName()).toUpperCase() + "\"" + (i == fieldInfos.size() - 1?"":","));
        }

        retList.add(TWO_RETRACT+")values(");

        for(i = 0; i < fieldInfos.size(); ++i) {
            pojoFieldInfo = (PojoFieldInfo)fieldInfos.get(i);
            retList.add(THREE_RETRACT+"#{" + firstCamelPo + "." + pojoFieldInfo.getFieldName() + "}" + (i == fieldInfos.size() - 1?"":","));
        }

        retList.add(TWO_RETRACT+")");
        retList.add(ONE_RETRACT+"</insert>");
        return retList;
    }

    private static List<String> genUpdateMethod(GenCodeResponse response, OnePojoInfo onePojoInfo, String tableName) {
        ArrayList retList = Lists.newArrayList();
        String pojoName = onePojoInfo.getPojoName();
        String firstCamelPo = GenCodeUtil.getLowerCamel(pojoName);
        List fieldInfos = onePojoInfo.getPojoFieldInfos();
        List idInfos = onePojoInfo.getIdFieldInfos();
        retList.add(ONE_RETRACT+"<update id=\"" + MethodName.update.name() + pojoName + "\" parameterType=\"" + firstCamelPo + "\">");
        retList.add(TWO_RETRACT+"update " + tableName);
        retList.add(TWO_RETRACT+"<trim prefix=\"set\" suffixOverrides=\",\">");

        int i;
        for(i = 0; i < fieldInfos.size(); ++i) {
            PojoFieldInfo name = (PojoFieldInfo)fieldInfos.get(i);
            String fieldName = name.getFieldName();
            String fieldClass = name.getFieldClass();
            retList.add(TWO_RETRACT+"<if test=\"" + firstCamelPo + "." + fieldName + " != null " + (fieldClass.equals("String")?"and " + firstCamelPo + "." + fieldName + "!=\'\'":"") + "\">");
            retList.add(THREE_RETRACT+"\"" + GenCodeUtil.getUnderScore(fieldName).toUpperCase() + "\"=#{" + firstCamelPo + "." + fieldName + "},");
            retList.add(TWO_RETRACT+"</if>");
        }

        retList.add(TWO_RETRACT+"</trim>");
        if(idInfos!=null && idInfos.size()>0){
            for(i = 0; i < idInfos.size(); ++i) {
                String name = ((PojoFieldInfo)idInfos.get(i)).getFieldName();
                if(i == 0) {
                    retList.add(TWO_RETRACT+"where \"" + GenCodeUtil.getUnderScore(name).toUpperCase() + "\"=#{" + firstCamelPo + "." + name + "}");
                } else {
                    retList.add(THREE_RETRACT+"and \"" + GenCodeUtil.getUnderScore(name).toUpperCase() + "\"=#{" + firstCamelPo + "." + name + "}");
                }
            }
        }
        retList.add(ONE_RETRACT+"</update>");
        return retList;
    }

    private static List<String> genSelectMethod(GenCodeResponse response, OnePojoInfo onePojoInfo, String tableName) {
        ArrayList retList = Lists.newArrayList();
        String pojoName = onePojoInfo.getPojoName();
        String firstCamelPo = getFirstCamel(pojoName);
        List fieldInfos = onePojoInfo.getPojoFieldInfos();
        retList.add(ONE_RETRACT+"<select id=\"" + MethodName.select.name() + pojoName + "\" parameterType=\"map\" resultMap=\"AllColumnMap\">");
        retList.add(TWO_RETRACT+"select tt.* from (");
        retList.add(TWO_RETRACT+"select t.*,rownum rn from " + tableName + " t");
        retList.add(TWO_RETRACT+"where 1=1");

        for(int i = 0; i < fieldInfos.size(); ++i) {
            PojoFieldInfo pojoFieldInfo = (PojoFieldInfo)fieldInfos.get(i);
            String fieldName = pojoFieldInfo.getFieldName();
            String fieldClass = pojoFieldInfo.getFieldClass();
            retList.add(TWO_RETRACT+"<if test=\"" + firstCamelPo + "." + fieldName + " != null " + (fieldClass.equals("String")?"and " + firstCamelPo + "." + fieldName + "!=\'\'":"") + "\">");
            retList.add(THREE_RETRACT+" and \"" + GenCodeUtil.getUnderScore(fieldName).toUpperCase() + "\"=#{" + firstCamelPo + "." + fieldName + "}");
            retList.add(TWO_RETRACT+"</if>");
        }

        retList.add(TWO_RETRACT+") tt");
        retList.add(TWO_RETRACT+"WHERE 1=1");
        retList.add(TWO_RETRACT+"<if test=\"pageNo!=null\">");
        retList.add(THREE_RETRACT+"<![CDATA[");
        retList.add(THREE_RETRACT+"and tt.rn >= ((#{pageNo}-1)*(#{pageSize})+1) and tt.rn<=(#{pageNo})*(#{pageSize})");
        retList.add(THREE_RETRACT+"]]>");
        retList.add(TWO_RETRACT+"</if>");
        retList.add(ONE_RETRACT+"</select>");
        return retList;
    }

    private static List<String> genSelectCountMethod(GenCodeResponse response, OnePojoInfo onePojoInfo, String tableName) {
        ArrayList retList = Lists.newArrayList();
        String pojoName = onePojoInfo.getPojoName();
        String firstCamelPo = getFirstCamel(pojoName);
        List fieldInfos = onePojoInfo.getPojoFieldInfos();
        retList.add(ONE_RETRACT+"<select id=\"getCount\" parameterType=\"map\" resultType=\"int\">");
        retList.add(TWO_RETRACT+"select count(0) from " + tableName + " t");
        retList.add(TWO_RETRACT+"where 1=1");

        for(int i = 0; i < fieldInfos.size(); ++i) {
            PojoFieldInfo fieldInfo = (PojoFieldInfo)fieldInfos.get(i);
            String fieldName = fieldInfo.getFieldName();
            retList.add(TWO_RETRACT+"<if test=\"" + firstCamelPo + "." + fieldName + " != null " + (fieldInfo.getFieldClass().equals("String")?"and " + firstCamelPo + "." + fieldName + "!=\'\'":"") + "\">");
            retList.add(THREE_RETRACT+" and \"" + GenCodeUtil.getUnderScore(fieldName).toUpperCase() + "\"=#{" + firstCamelPo + "." + fieldName + "}");
            retList.add(TWO_RETRACT+"</if>");
        }

        retList.add(ONE_RETRACT+"</select>");
        return retList;
    }

    private static String getFirstCamel(String pojoName) {
        return pojoName.substring(0, 1).toLowerCase() + pojoName.substring(1);
    }

    public static void main(String[] args) {
//        Pattern day3DataPattern = Pattern.compile("var dataSK = (.*)");
        String match = RegexUtil.getMatch("(.*)pojo.(.*)",
                "refund_finish_time = #{pojo.refundFinishTime},");
        System.out.println(match);

    }

}
