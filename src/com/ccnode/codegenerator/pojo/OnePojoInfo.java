package com.ccnode.codegenerator.pojo;

import com.ccnode.codegenerator.util.GenCodeConfig;
import com.intellij.psi.impl.source.PsiClassImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * What always stop you is what you always believe.
 * <p>
 * Created by zhengjun.du on 2016/05/17 19:57
 */
public class OnePojoInfo {
    List<GeneratedFile> files;
    List<PojoFieldInfo> pojoFieldInfos;
    List<PojoFieldInfo> idFieldInfos;
    GenCodeConfig genCodeConfig;
    DirectoryConfig directoryConfig;
    PsiClassImpl psiClass;
    Class pojoClass;
    String pojoName;
    String pojoPackage;
    String daoPackage;
    String servicePackage;
    String pojoDirPath;
    String fullPojoPath;
    String fullDaoPath;
    String fullServicePath;
    String fullSqlPath;
    String fullMapperPath;
    String pojoClassSimpleName;

    public OnePojoInfo() {
    }

    public List<PojoFieldInfo> getIdFieldInfos() {
        return this.idFieldInfos;
    }

    public void setIdFieldInfos(List<PojoFieldInfo> idFieldInfos) {
        this.idFieldInfos = idFieldInfos;
    }

    public String getPojoName() {
        return this.pojoName;
    }

    public void setPojoName(String pojoName) {
        this.pojoName = pojoName;
    }

    public DirectoryConfig getDirectoryConfig() {
        return this.directoryConfig;
    }

    public void setDirectoryConfig(DirectoryConfig directoryConfig) {
        this.directoryConfig = directoryConfig;
    }

    public List<GeneratedFile> getFiles() {
        return this.files;
    }

    public void setFiles(List<GeneratedFile> files) {
        this.files = files;
    }

    public List<PojoFieldInfo> getPojoFieldInfos() {
        return this.pojoFieldInfos;
    }

    public void setPojoFieldInfos(List<PojoFieldInfo> pojoFieldInfos) {
        this.pojoFieldInfos = pojoFieldInfos;
    }

    public GenCodeConfig getGenCodeConfig() {
        return this.genCodeConfig;
    }

    public void setGenCodeConfig(GenCodeConfig genCodeConfig) {
        this.genCodeConfig = genCodeConfig;
    }

    public Class getPojoClass() {
        return this.pojoClass;
    }

    public void setPojoClass(@Nullable Class pojoClass) {
        this.pojoClass = pojoClass;
    }

    public String getFullPojoPath() {
        return this.fullPojoPath;
    }

    public void setFullPojoPath(String fullPojoPath) {
        this.fullPojoPath = fullPojoPath;
    }

    public String getPojoPackage() {
        return this.pojoPackage;
    }

    public void setPojoPackage(String pojoPackage) {
        this.pojoPackage = pojoPackage;
    }

    public String getDaoPackage() {
        return this.daoPackage;
    }

    public void setDaoPackage(String daoPackage) {
        this.daoPackage = daoPackage;
    }

    public String getServicePackage() {
        return this.servicePackage;
    }

    public void setServicePackage(String servicePackage) {
        this.servicePackage = servicePackage;
    }

    public String getPojoClassSimpleName() {
        return this.pojoClassSimpleName;
    }

    public void setPojoClassSimpleName(String pojoClassSimpleName) {
        this.pojoClassSimpleName = pojoClassSimpleName;
    }

    public PsiClassImpl getPsiClass() {
        return this.psiClass;
    }

    public void setPsiClass(PsiClassImpl psiClass) {
        this.psiClass = psiClass;
    }

    public String getFullDaoPath() {
        return this.fullDaoPath;
    }

    public void setFullDaoPath(String fullDaoPath) {
        this.fullDaoPath = fullDaoPath;
    }

    public String getFullServicePath() {
        return this.fullServicePath;
    }

    public void setFullServicePath(String fullServicePath) {
        this.fullServicePath = fullServicePath;
    }

    public String getFullSqlPath() {
        return this.fullSqlPath;
    }

    public void setFullSqlPath(String fullSqlPath) {
        this.fullSqlPath = fullSqlPath;
    }

    public String getFullMapperPath() {
        return this.fullMapperPath;
    }

    public void setFullMapperPath(String fullMapperPath) {
        this.fullMapperPath = fullMapperPath;
    }

    public String getPojoDirPath() {
        return this.pojoDirPath;
    }

    public void setPojoDirPath(String pojoDirPath) {
        this.pojoDirPath = pojoDirPath;
    }
}