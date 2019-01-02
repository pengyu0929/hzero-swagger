package script.db

databaseChangeLog(logicalFilePath: 'script/db/hsgp_swagger.groovy') {
    changeSet(author: "bojiangzhou", id: "2018-12-27-hsgp_swagger") {
    if(helper.dbType().isSupportSequence()){
            createSequence(sequenceName: 'hsgp_swagger_s', startValue:"1")
        }
        createTable(tableName: "hsgp_swagger", remarks: "") {
        column(name: "id", type: "bigint(20)", autoIncrement: true ,   remarks: "")  {constraints(primaryKey: true)} 
        column(name: "service_name", type: "varchar(128)",  remarks: "服务名,如hap-user-service")  {constraints(nullable:"false")}  
        column(name: "service_version", type: "varchar(64)",   defaultValue:"0.0.0",   remarks: "服务版本")  {constraints(nullable:"false")}  
        column(name: "value", type: "mediumtext",  remarks: "接口文档json数据")  {constraints(nullable:"false")}  
        column(name: "object_version_number", type: "bigint(20)",   defaultValue:"1",   remarks: "")   
        column(name: "created_by", type: "bigint(20)",   defaultValue:"0",   remarks: "")   
        column(name: "creation_date", type: "datetime",   defaultValueComputed:"CURRENT_TIMESTAMP",   remarks: "")   
        column(name: "last_updated_by", type: "bigint(20)",   defaultValue:"0",   remarks: "")   
        column(name: "last_update_date", type: "datetime",   defaultValueComputed:"CURRENT_TIMESTAMP",   remarks: "")   

    }
   createIndex(tableName: "hsgp_swagger", indexName: "hsgp_swagger_u1") {
            column(name: "service_name")
            column(name: "service_version")
        }          

  }    
}