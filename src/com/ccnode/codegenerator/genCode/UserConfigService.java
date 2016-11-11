package com.ccnode.codegenerator.genCode;

import com.ccnode.codegenerator.pojo.DirectoryConfig;
import com.ccnode.codegenerator.pojo.GenCodeRequest;
import com.ccnode.codegenerator.pojo.GenCodeResponse;
import com.ccnode.codegenerator.pojoHelper.ProjectHelper;
import com.ccnode.codegenerator.util.GenCodeConfig;
import com.ccnode.codegenerator.util.IOUtils;
import com.ccnode.codegenerator.util.LoggerWrapper;
import com.ccnode.codegenerator.util.PojoUtil;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * What always stop you is what you always believe.
 * <p>
 * Created by zhengjun.du on 2016/05/21 12:40
 */
public class UserConfigService {
    private static final Logger LOGGER = LoggerWrapper.getLogger(UserConfigService.class);
    public static Map<String, String> userConfigMap = Maps.newHashMap();

    public UserConfigService() {
    }

    public static void loadUserConfig(AnActionEvent event) {
        try {
            String e = ProjectHelper.getProjectPath(event);
            File propertiesFile = IOUtils.matchOnlyOneFile(e, "codehelper.properties");
            String fileName = "";
            if(propertiesFile != null) {
                fileName = propertiesFile.getAbsolutePath();
            }

            if(!StringUtils.isBlank(fileName)) {
                if(Objects.equal(fileName, "NOT_ONLY")) {
                    LOGGER.error("error, duplicated codehelper.properties file");
                    throw new RuntimeException("error, duplicated codehelper.properties file");
                } else {
                    File configFile = new File(fileName);
                    List strings = IOUtils.readLines(configFile);
                    strings = PojoUtil.avoidEmptyList(strings);
                    int lineIndex = 1;
                    HashMap configMap = Maps.newHashMap();
                    Iterator var8 = strings.iterator();

                    while(var8.hasNext()) {
                        String configLine = (String)var8.next();
                        ++lineIndex;
                        if(!StringUtils.isBlank(configLine) && !configLine.startsWith("#")) {
                            if(configLine.startsWith("=")) {
                                LOGGER.error("line: " + lineIndex + "error, config key con not be empty");
                                throw new RuntimeException("line: " + lineIndex + "error, config key con not be empty");
                            }

                            List split = Splitter.on("=").trimResults().omitEmptyStrings().splitToList(configLine);
                            if(split.size() != 2) {
                                LOGGER.error("", "line: " + lineIndex + "config error, correct format : key = value");
                                throw new RuntimeException("line: " + lineIndex + "config error, correct format : key = value");
                            }

                            configMap.put(((String)split.get(0)).toLowerCase(), split.get(1));
                        }
                    }

                    userConfigMap = configMap;
                    LOGGER.info("readConfigFile configMap:{}", configMap);
                }
            }
        } catch (IOException var11) {
            LOGGER.error(" readConfigFile config file read error ", var11);
            throw new RuntimeException(" readConfigFile config file read error ");
        } catch (Exception var12) {
            LOGGER.error(" readConfigFile config file read error ", var12);
            throw new RuntimeException("readConfigFile error occurred");
        }
    }

    public static GenCodeResponse initConfig(GenCodeResponse response) {
        LOGGER.info("initConfig");

        try {
            GenCodeConfig e = new GenCodeConfig();
            response.setCodeConfig(e);
            Map userConfigMap = response.getUserConfigMap();
            String pojos = (String)userConfigMap.get("pojos");
            if(StringUtils.isBlank(pojos)) {
                pojos = Messages.showInputDialog(response.getRequest().getProject(), "Please input Pojo Name : ", "Input Pojos", Messages.getQuestionIcon());
            }

            if(StringUtils.isBlank(pojos)) {
                return (GenCodeResponse)response.failure("no config or input pojo name");
            } else {
                pojos = pojos.replace(",", "|");
                pojos = pojos.replace("，", "|");
                pojos = pojos.replace(";", "|");
                pojos = pojos.replace("；", "|");
                response.getRequest().setPojoNames(Splitter.on("|").trimResults().omitEmptyStrings().splitToList(pojos));
                DirectoryConfig directoryConfig = new DirectoryConfig();
                response.setDirectoryConfig(directoryConfig);
                e.setDaoDir(removeStartAndEndSplitter((String)userConfigMap.get("dao.path")));
                e.setSqlDir(removeStartAndEndSplitter((String)userConfigMap.get("sql.path")));
                e.setMapperDir(removeStartAndEndSplitter((String)userConfigMap.get("mapper.path")));
                e.setServiceDir(removeStartAndEndSplitter((String)userConfigMap.get("service.path")));
                String tableName = removeStartAndEndSplitter((String)userConfigMap.get("table.name"));
                e.setTableName(StringUtils.isEmpty(tableName)?pojos:tableName);
                return response;
            }
        } catch (Exception var6) {
            LOGGER.error(" status error occurred :{}", response, var6);
            return (GenCodeResponse)response.failure(" status error occurred");
        }
    }

    public static String removeStartAndEndSplitter(String s) {
        if(StringUtils.isBlank(s)) {
            return s;
        } else {
            String splitter = System.getProperty("file.separator");
            String ret = s;
            if(s.startsWith(splitter)) {
                ret = s.substring(1);
            }

            if(s.endsWith(splitter)) {
                ret = ret.substring(0, ret.length() - 1);
            }

            return ret;
        }
    }

    public static GenCodeResponse readConfigFile(String projectPath) {
        LOGGER.info("readConfigFile");
        GenCodeResponse ret = new GenCodeResponse();
        ret.accept();

        try {
            File e = IOUtils.matchOnlyOneFile(projectPath, "codehelper.properties");
            String fileName = "";
            if(e != null) {
                fileName = e.getAbsolutePath();
            }

            if(StringUtils.isBlank(fileName)) {
                return ret;
            } else if(Objects.equal(fileName, "NOT_ONLY")) {
                LOGGER.error("error, duplicated codehelper.properties file");
                return (GenCodeResponse)ret.failure("", "error, duplicated codehelper.properties file");
            } else {
                File configFile = new File(fileName);
                List strings = IOUtils.readLines(configFile);
                strings = PojoUtil.avoidEmptyList(strings);
                int lineIndex = 1;
                HashMap configMap = Maps.newHashMap();
                Iterator var8 = strings.iterator();

                while(var8.hasNext()) {
                    String configLine = (String)var8.next();
                    ++lineIndex;
                    if(!StringUtils.isBlank(configLine) && !configLine.startsWith("#")) {
                        if(configLine.startsWith("=")) {
                            LOGGER.error("line: " + lineIndex + "error, config key con not be empty");
                            return (GenCodeResponse)ret.failure("", "line: " + lineIndex + "error, config key con not be empty");
                        }

                        List split = Splitter.on("=").trimResults().omitEmptyStrings().splitToList(configLine);
                        if(split.size() != 2) {
                            LOGGER.error("", "line: " + lineIndex + "config error, correct format : key = value");
                            return (GenCodeResponse)ret.failure("", "line: " + lineIndex + "config error, correct format : key = value");
                        }

                        configMap.put(((String)split.get(0)).toLowerCase(), split.get(1));
                    }
                }

                ret.setUserConfigMap(configMap);
                LOGGER.info("readConfigFile configMap:{}", configMap);
                return ret;
            }
        } catch (IOException var11) {
            LOGGER.error(" readConfigFile config file read error ", var11);
            return (GenCodeResponse)ret.failure("", " readConfigFile config file read error ");
        } catch (Exception var12) {
            LOGGER.error(" readConfigFile config file read error ", var12);
            return (GenCodeResponse)ret.failure("", "readConfigFile error occurred");
        }
    }

    public static void main(String[] args) {
        String s = "dsfasdjfasdjf";
        System.out.println(s.substring(0, s.length() - 1));
    }
}
