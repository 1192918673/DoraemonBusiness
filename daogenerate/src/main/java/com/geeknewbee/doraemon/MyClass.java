package com.geeknewbee.doraemon;


import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class MyClass {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "com.geeknewbee.doraemon.database");// 数据库版本，生成的类的命名空间

        schema.enableKeepSectionsByDefault();//保留生存类中自己的代码
        schema.enableActiveEntitiesByDefault();

        addEntity(schema);

        new DaoGenerator().generateAll(schema, "./app/src-dao"); // 生成的类文件对应的位置
    }

    private static void addEntity(Schema schema) {
        /*------------------会员列表数据结构-------------------------------*/

//        <city id="2" name="北京市" name_en="Beijing" name_py="beijingshi" province="北京市" weathercnid="101010100"/>
        Entity city = schema.addEntity("Weather_City");
        city.addIdProperty().autoincrement();
        city.addStringProperty("cityId");
        city.addStringProperty("name");
        city.addStringProperty("nameEn");
        city.addStringProperty("namePy");
        city.addStringProperty("province");
        city.addStringProperty("weatherCnId");
    }
}
